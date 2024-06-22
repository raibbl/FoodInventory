package com.example.fooditeminventory.ui
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.fooditeminventory.R
import com.example.fooditeminventory.api.Product
import com.example.fooditeminventory.api.ProductResponse
import com.example.fooditeminventory.api.RetrofitInstance
import com.example.fooditeminventory.ui.theme.FoodItemInventoryTheme
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BarcodeScannerFragment : Fragment() {

    private lateinit var cameraExecutor: ExecutorService
    private var scannedProduct: Product? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        cameraExecutor = Executors.newSingleThreadExecutor()
        return ComposeView(requireContext()).apply {
            setContent {
                FoodItemInventoryTheme {
                    val navController = findNavController()
                    BarcodeScannerScreen(navController)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }

    @Composable
    fun BarcodeScannerScreen(navController: NavController) {
        val context = LocalContext.current
        var hasCameraPermission by remember { mutableStateOf(false) }
        val cameraPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { granted ->
                hasCameraPermission = granted
            }
        )

        LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                hasCameraPermission = true
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        if (hasCameraPermission) {
            CameraPreviewScreen(navController)
        } else {
            Text("Camera permission is required to use this feature.")
        }
    }

    @Composable
    fun CameraPreviewScreen(navController: NavController) {
        var scanResult by remember { mutableStateOf<Product?>(null) }
        var isLoading by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        Box(modifier = Modifier.fillMaxSize()) {
            CameraPreview(
                onBarcodeDetected = { barcodeValue ->
                    coroutineScope.launch(Dispatchers.IO) {
                        isLoading = true
                        fetchProductInfo(barcodeValue) { product ->
                            scanResult = product
                            isLoading = false
                            scannedProduct = product
                            navigateToAddProductFragment(navController, product)
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            Button(
                onClick = { navigateToAddProductFragment(navController, scannedProduct) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text("Add Manually")
            }

            Text(
                text = "Scanning for product...",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            )
        }
    }

    private fun fetchProductInfo(barcode: String, onResult: (Product?) -> Unit) {
        val call = RetrofitInstance.foodApi.getProduct(barcode)
        call.enqueue(object : Callback<ProductResponse> {
            override fun onResponse(call: Call<ProductResponse>, response: Response<ProductResponse>) {
                if (response.isSuccessful) {
                    onResult(response.body()?.product)
                } else {
                    Log.e("BarcodeScannerFragment", "Error: ${response.errorBody()?.string()}")
                    onResult(null)
                }
            }

            override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                Log.e("BarcodeScannerFragment", "Failed to fetch product info", t)
                onResult(null)
            }
        })
    }

    private fun navigateToAddProductFragment(navController: NavController, product: Product?) {
        if (navController.currentDestination?.id == R.id.barcodeScannerFragment) {
            navController.navigate(
                BarcodeScannerFragmentDirections.actionBarcodeScannerFragmentToAddProductFragment(
                    productName = product?.product_name ?: "",
                    productBrand = product?.brands ?: "",
                    productIngredients = product?.ingredients_text ?: "",
                    productImageUrl = product?.image_url ?: "",
                    productBarcode = product?.code?: ""
                )
            )
        } else {
            Log.e("BarcodeScannerFragment", "Current destination is not barcodeScannerFragment")
        }
    }
}

@Composable
fun CameraPreview(
    onBarcodeDetected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraProvider = cameraProviderFuture.get()
    val preview = remember { Preview.Builder().build() }
    val previewView = remember { androidx.camera.view.PreviewView(context) }
    val imageAnalyzer = remember {
        ImageAnalysis.Builder()
            .build()
            .also { imageAnalysis ->
                imageAnalysis.setAnalyzer(
                    ContextCompat.getMainExecutor(context),
                    BarcodeAnalyzer(context, onBarcodeDetected)
                )
            }
    }

    DisposableEffect(Unit) {
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview.setSurfaceProvider(previewView.surfaceProvider)

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalyzer
            )
        } catch (exc: Exception) {
            Log.e("CameraPreview", "Use case binding failed", exc)
        }

        onDispose {
            cameraProvider.unbindAll()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}

class BarcodeAnalyzer(
    private val context: android.content.Context,
    private val onBarcodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {
    private val barcodeScanner by lazy {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
        BarcodeScanning.getClient(options)
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        barcode.rawValue?.let { onBarcodeDetected(it) }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("BarcodeAnalyzer", "Barcode scan failed", e)
                    Toast.makeText(context, "Failed to scan barcode: ${e.message}", Toast.LENGTH_LONG).show()
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }
}
