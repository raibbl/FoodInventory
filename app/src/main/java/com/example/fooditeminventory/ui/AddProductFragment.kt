package com.example.fooditeminventory.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.compose.rememberAsyncImagePainter
import com.example.fooditeminventory.db.AppDatabase
import com.example.fooditeminventory.db.ProductEntity
import com.example.fooditeminventory.ui.theme.FoodItemInventoryTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddProductFragment : Fragment() {

    private val args: AddProductFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val navController = findNavController()
                FoodItemInventoryTheme {
                    AddProductScreen(navController = navController, args = args)
                }
            }
        }
    }
}

@Composable
fun AddProductScreen(navController: NavController, args: AddProductFragmentArgs) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val coroutineScope = rememberCoroutineScope()

    var productName by remember { mutableStateOf(args.productName ?: "") }
    var productBrand by remember { mutableStateOf(args.productBrand ?: "") }
    var productIngredients by remember { mutableStateOf(args.productIngredients ?: "") }
    var productBarcode by remember { mutableStateOf(args.productBarcode ?: "") }
    var productImageUrl = args.productImageUrl ?: ""

    LaunchedEffect(args.productUuid) {
        if (args.productUuid.isNotEmpty()) {
            coroutineScope.launch(Dispatchers.IO) {
                val product = db.productDao().getProductByUuid(args.productUuid)
                product?.let {
                    productName = it.name
                    productBrand = it.brand
                    productIngredients = it.ingredients
                    productBarcode = it.barcode
                    productImageUrl = it.imageUrl ?: ""
                }
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .background(MaterialTheme.colorScheme.background)  // Set background color from theme
    ) {
        OutlinedTextField(
            value = productName,
            onValueChange = { productName = it },
            label = { Text("Product Name") },
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)  // Set text field background from theme
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = productBrand,
            onValueChange = { productBrand = it },
            label = { Text("Product Brand") },
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)  // Set text field background from theme
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = productIngredients,
            onValueChange = { productIngredients = it },
            label = { Text("Product Ingredients") },
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)  // Set text field background from theme
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (productImageUrl.isNotEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(productImageUrl),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.surface)  // Set image background from theme
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    if (args.productUuid.isNotEmpty()) {
                        // Update existing product
                        val product = ProductEntity(
                            uuid = args.productUuid,
                            name = productName,
                            brand = productBrand,
                            ingredients = productIngredients,
                            imageUrl = if (productImageUrl.isNotEmpty()) productImageUrl else null,
                            barcode = productBarcode
                        )
                        db.productDao().insert(product)
                    } else {
                        // Insert new product
                        val product = ProductEntity(
                            name = productName,
                            brand = productBrand,
                            ingredients = productIngredients,
                            imageUrl = if (productImageUrl.isNotEmpty()) productImageUrl else null,
                            barcode = productBarcode
                        )
                        db.productDao().insert(product)
                    }

                    launch(Dispatchers.Main) {
                        Toast.makeText(context, "Product saved!", Toast.LENGTH_SHORT).show()
                        navController.navigate(AddProductFragmentDirections.actionAddProductFragmentToHomeFragment())
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Product")
        }
    }
}
