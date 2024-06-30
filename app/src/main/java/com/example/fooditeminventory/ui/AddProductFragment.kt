package com.example.fooditeminventory.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.fooditeminventory.api.Nutriments
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
    var productEntity by remember { mutableStateOf<ProductEntity?>(null) }

    LaunchedEffect(args.productUuid) {
        if (args.productUuid.isNotEmpty()) {
            coroutineScope.launch(Dispatchers.IO) {
                productEntity = db.productDao().getProductByUuid(args.productUuid)
            }
        }
    }

    var selectedTabIndex by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
    ) {
        TabRow(selectedTabIndex = selectedTabIndex) {
            Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }) {
                Text("Details")
            }
            Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }) {
                Text("Nutritional Info")
            }
        }
        productEntity?.let { entity ->
            var productName by remember { mutableStateOf(entity.name) }
            var productBrand by remember { mutableStateOf(entity.brand) }
            var productIngredients by remember { mutableStateOf(entity.ingredients) }
            var productImageUrl by remember { mutableStateOf(if (entity.images.isNotEmpty()) entity.images[0] else "") }
            var productQuantity by remember { mutableStateOf(entity.quantity) }
            var productNutriments by remember { mutableStateOf(entity.nutriments) }
            var productAllergens by remember { mutableStateOf(entity.allergens) }

            when (selectedTabIndex) {
                0 -> ProductDetails(
                    productName = productName,
                    onProductNameChange = { productName = it },
                    productBrand = productBrand,
                    onProductBrandChange = { productBrand = it },
                    productIngredients = productIngredients,
                    onProductIngredientsChange = { productIngredients = it },
                    productImageUrl = productImageUrl,
                    productQuantity = productQuantity,
                    onProductQuantityChange = { productQuantity = it },
                    saveProduct = {
                        coroutineScope.launch(Dispatchers.IO) {
                            if (args.productUuid.isNotEmpty()) {
                                // Update existing product
                                val updatedProduct = entity.copy(
                                    name = productName,
                                    brand = productBrand,
                                    ingredients = productIngredients,
                                    images = listOfNotNull(if (productImageUrl.isNotEmpty()) productImageUrl else null),
                                    quantity = productQuantity,
                                    nutriments = productNutriments,
                                    allergens = productAllergens
                                )
                                db.productDao().insert(updatedProduct)
                            }

                            launch(Dispatchers.Main) {
                                Toast.makeText(context, "Product saved!", Toast.LENGTH_SHORT).show()
                                navController.navigate(AddProductFragmentDirections.actionAddProductFragmentToHomeFragment())
                            }
                        }
                    }
                )
                1 -> NutritionalInfo(nutriments = productNutriments)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetails(
    productName: String,
    onProductNameChange: (String) -> Unit,
    productBrand: String,
    onProductBrandChange: (String) -> Unit,
    productIngredients: String,
    onProductIngredientsChange: (String) -> Unit,
    productImageUrl: String,
    productQuantity: Int,
    onProductQuantityChange: (Int) -> Unit,
    saveProduct: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
    ) {
        item {
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
            TextField(
                value = productName,
                onValueChange = onProductNameChange,
                label = { Text("Product Name") },
                colors = TextFieldDefaults.textFieldColors(
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
                    containerColor = MaterialTheme.colorScheme.surfaceDim
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = productBrand,
                onValueChange = onProductBrandChange,
                label = { Text("Product Brand") },
                colors = TextFieldDefaults.textFieldColors(
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
                    containerColor = MaterialTheme.colorScheme.surfaceDim
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = productIngredients,
                onValueChange = onProductIngredientsChange,
                label = { Text("Product Ingredients") },
                colors = TextFieldDefaults.textFieldColors(
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
                    containerColor = MaterialTheme.colorScheme.surfaceDim
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            QuantityControls(productQuantity = productQuantity, onQuantityChange = onProductQuantityChange)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = saveProduct,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Save Product", color = MaterialTheme.colorScheme.surface)
            }
        }
    }
}

@Composable
fun NutritionalInfo(nutriments: Nutriments?) {
    if (nutriments != null) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Text(text = "Nutritional Information", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            nutriments.energy?.let { Text(text = "Energy: $it", color = MaterialTheme.colorScheme.onSurface) }
            nutriments.fat?.let { Text(text = "Fat: $it", color = MaterialTheme.colorScheme.onSurface) }
            nutriments.saturated_fat?.let { Text(text = "Saturated Fat: $it", color = MaterialTheme.colorScheme.onSurface) }
            nutriments.carbohydrates?.let { Text(text = "Carbohydrates: $it", color = MaterialTheme.colorScheme.onSurface) }
            nutriments.sugars?.let { Text(text = "Sugars: $it", color = MaterialTheme.colorScheme.onSurface) }
            nutriments.proteins?.let { Text(text = "Proteins: $it", color = MaterialTheme.colorScheme.onSurface) }
            nutriments.salt?.let { Text(text = "Salt: $it", color = MaterialTheme.colorScheme.onSurface) }
            nutriments.fiber?.let { Text(text = "Fiber: $it", color = MaterialTheme.colorScheme.onSurface) }
        }
    }
}
