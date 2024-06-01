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
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.fooditeminventory.R
import com.example.fooditeminventory.api.Product
import com.example.fooditeminventory.databinding.FragmentBarcodeScannerBinding
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.example.fooditeminventory.api.RetrofitInstance
import com.example.fooditeminventory.api.ProductResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BarcodeScannerFragment : Fragment() {

    private var _binding: FragmentBarcodeScannerBinding? = null

    private val binding get() = _binding!!

    private lateinit var cameraExecutor: ExecutorService

    private var scannedProduct: Product? = null // Class variable to store product information


    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBarcodeScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE
            )
        }
        binding.navigateButton.setOnClickListener {
            // Extract product information from scannedProduct (if available)
            val productName = scannedProduct?.product_name ?: ""
            val productBrand = scannedProduct?.brands ?: ""
            val productIngredients = scannedProduct?.ingredients_text ?: ""
            val image_url = scannedProduct?.image_url?:""

            // Create the navigation action with arguments
            val action = BarcodeScannerFragmentDirections.actionBarcodeScannerFragmentToAddProductFragment(
                productName = productName,
                productBrand = productBrand,
                productIngredients = productIngredients,
                productImageUrl = image_url
            )

            findNavController().navigate(action)
        }




    }

    private fun startCamera() {
        val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val barcodeScannerOptions = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build()

            val barcodeScanner = BarcodeScanning.getClient(barcodeScannerOptions)

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, { imageProxy ->
                        processImageProxy(barcodeScanner, imageProxy)
                    })
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer)
            } catch (exc: Exception) {
                Log.e("BarcodeScannerFragment", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner, imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        handleBarcode(barcode)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to scan barcode: ${e.message}", Toast.LENGTH_LONG).show()
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    private fun handleBarcode(barcode: Barcode) {
        // Handle the scanned barcode
        val barcodeValue =barcode.rawValue
//        binding.bottomText.text = getString(R.string.barcode_value, barcodeValue)
        if (barcodeValue != null) {
            println(barcodeValue)
            fetchProductInfo(barcodeValue)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission is required to use this feature", Toast.LENGTH_LONG).show()
        }
    }

    private fun fetchProductInfo(barcode: String) {
        _binding?.apply {
            progressBar.visibility = View.VISIBLE
        }

        val call = RetrofitInstance.foodApi.getProduct(barcode)
        call.enqueue(object : Callback<ProductResponse> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<ProductResponse>, response: Response<ProductResponse>) {
                _binding?.apply {
                    progressBar.visibility = View.GONE
                }
                if (response.isSuccessful) {
                    val product = response.body()?.product
                    product?.let {
                        // Update UI with product information
                        scannedProduct = it
                        println("Product: ${it.product_name}\nBrand: ${it.brands}\nIngredients: ${it.ingredients_text}")
                        _binding?.apply {
                            scannedProduct = it
                            // Extract product information from scannedProduct (if available)
                            val productName = scannedProduct?.product_name ?: ""
                            val productBrand = scannedProduct?.brands ?: ""
                            val productIngredients = scannedProduct?.ingredients_text ?: ""
                            val image_url = scannedProduct?.image_url?:""

                            // Create the navigation action with arguments
                            val action = BarcodeScannerFragmentDirections.actionBarcodeScannerFragmentToAddProductFragment(
                                productName = productName,
                                productBrand = productBrand,
                                productIngredients = productIngredients,
                                productImageUrl = image_url
                            )

                            if (findNavController().currentDestination?.id == R.id.barcodeScannerFragment) {
                                findNavController().navigate(action)
                            } else {
                                println("Navigation action not executed. Current destination is not barcodeScannerFragment.")
                            }
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Product not found", Toast.LENGTH_LONG).show()
                    println(response)
                    _binding?.apply {
                        statusTextView.text = "Product not found"
                        progressBar.visibility = View.GONE
                    }
                }
            }

            override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                if (isAdded && context != null) {
                    Toast.makeText(requireContext(), "Failed to fetch product info", Toast.LENGTH_LONG).show()
                    _binding?.apply {
                        statusTextView.text = "Failed to fetch product info"
                    }
                } else {
                    // Optionally log the error or handle the case where the fragment is not attached
                    Log.e("BarcodeScannerFragment", "Fragment not attached to context", t)
                }
            }
        })
    }

}
