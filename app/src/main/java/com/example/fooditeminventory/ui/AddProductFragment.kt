package com.example.fooditeminventory.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
fun QuantityControls(productQuantity: Int, onQuantityChange: (Int) -> Unit) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Quantity:",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { if (productQuantity > 1) onQuantityChange(productQuantity - 1) }) {
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = "Decrease quantity",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "$productQuantity",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = { onQuantityChange(productQuantity + 1) }) {
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowUp,
                    contentDescription = "Increase quantity",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    var productQuantity by remember { mutableStateOf(1) }

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
                    productQuantity = it.quantity
                }
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = productName,
            onValueChange = { productName = it },
            label = { Text("Product Name") },
            colors = TextFieldDefaults.textFieldColors(
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
                containerColor = MaterialTheme.colorScheme.surfaceDim // Changed this line to set the background color of the text field
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = productBrand,
            onValueChange = { productBrand = it },
            label = { Text("Product Brand") },
            colors = TextFieldDefaults.textFieldColors(
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
                containerColor = MaterialTheme.colorScheme.surfaceDim // Changed this line to set the background color of the text field
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = productIngredients,
            onValueChange = { productIngredients = it },
            label = { Text("Product Ingredients") },
            colors = TextFieldDefaults.textFieldColors(
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
                containerColor = MaterialTheme.colorScheme.surfaceDim // Changed this line to set the background color of the text field
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (productImageUrl.isNotEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(productImageUrl),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quantity controls
        QuantityControls(productQuantity = productQuantity, onQuantityChange = { productQuantity = it })

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
                            barcode = productBarcode,
                            quantity = productQuantity
                        )
                        db.productDao().insert(product)
                    } else {
                        // Insert new product
                        val product = ProductEntity(
                            name = productName,
                            brand = productBrand,
                            ingredients = productIngredients,
                            imageUrl = if (productImageUrl.isNotEmpty()) productImageUrl else null,
                            barcode = productBarcode,
                            quantity = productQuantity
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
            Text(text = "Save Product", color = MaterialTheme.colorScheme.surface)
        }
    }
}
