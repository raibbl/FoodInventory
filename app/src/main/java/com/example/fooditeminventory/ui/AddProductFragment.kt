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
import com.example.fooditeminventory.ui.gallery.AutoSlidingCarousel
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
            if(productEntity?.nutriments != null){
                Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }) {
                    Text("Nutritional Info")
                }
            }
        }

            var productName by remember { mutableStateOf(productEntity?.name ?: "") }
            var productBrand by remember { mutableStateOf(productEntity?.brand ?:"") }
            var productIngredients by remember { mutableStateOf(productEntity?.ingredients?:"") }
            var productQuantity by remember { mutableStateOf(productEntity?.quantity?:1) }
            val productNutriments by remember { mutableStateOf(productEntity?.nutriments) }
            val productAllergens by remember { mutableStateOf(productEntity?.allergens) }
            val images = remember { productEntity?.images ?: listOf() }

            when (selectedTabIndex) {
                0 -> ProductDetails(
                    productName = productName,
                    onProductNameChange = { productName = it },
                    productBrand = productBrand,
                    onProductBrandChange = { productBrand = it },
                    productIngredients = productIngredients,
                    onProductIngredientsChange = { productIngredients = it },
                    images = images,
                    productQuantity = productQuantity,
                    onProductQuantityChange = { productQuantity = it }
                ) {
                    coroutineScope.launch(Dispatchers.IO) {
                        if (args.productUuid.isNotEmpty()) {
                            // Update existing product
                            val updatedProduct = productEntity?.copy(
                                name = productName,
                                brand = productBrand,
                                ingredients = productIngredients,
                                images = images,
                                quantity = productQuantity,
                                nutriments = productNutriments,
                                allergens = productAllergens
                            )
                            if (updatedProduct != null) {
                                db.productDao().insert(updatedProduct)
                            }
                        }

                        launch(Dispatchers.Main) {
                            Toast.makeText(context, "Product saved!", Toast.LENGTH_SHORT).show()
                            navController.navigate(AddProductFragmentDirections.actionAddProductFragmentToHomeFragment())
                        }
                    }
                }

                1 -> {
                    if (productNutriments != null) {
                        NutritionalInfo(
                            nutriments = productNutriments,
                            servingSize = productEntity?.serving_size,
                            quantityAndUnit = productEntity?.quantityAndUnit
                        )
                    }
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
    images: List<String>,
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
            if (images.isNotEmpty()) {
                AutoSlidingCarousel(
                    itemsCount = images.size,
                    itemContent = { index ->
                        Image(
                            painter = rememberAsyncImagePainter(images[index]),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
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
fun NutritionalInfo(nutriments: Nutriments?, servingSize: String?,quantityAndUnit: String?) {
    if (nutriments != null) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Text(text = "Nutritional Information", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            quantityAndUnit?.let { Text(text = "Total Quantity: ${quantityAndUnit}", color = MaterialTheme.colorScheme.onSurface) }
            servingSize?.let { Text(text = "Serving Size: $it", color = MaterialTheme.colorScheme.onSurface) }
            nutriments.energyKcalServing?.let{ Text(text = "Calories PerServing: ${it} ${nutriments.energyKcalUnit}", color = MaterialTheme.colorScheme.onSurface) }
            nutriments.energy?.let { Text(text = "Energy PerServing: ${it} ${nutriments.energy_unit}", color = MaterialTheme.colorScheme.onSurface) }
            nutriments.fat?.let { Text(text = "Fat: ${it} ${nutriments.fat_unit}", color = MaterialTheme.colorScheme.onSurface) }
            nutriments.saturated_fat?.let { Text(text = "Saturated Fat: ${it} ${nutriments.saturated_fat_unit}", color = MaterialTheme.colorScheme.onSurface) }
            nutriments.carbohydrates?.let { Text(text = "Carbohydrates: ${it} ${nutriments.carbohydrates_unit}", color = MaterialTheme.colorScheme.onSurface) }
            nutriments.sugars?.let { Text(text = "Sugars: ${it} ${nutriments.sugars_unit}", color = MaterialTheme.colorScheme.onSurface) }
            nutriments.proteins?.let { Text(text = "Proteins: ${it} ${nutriments.proteins_unit}", color = MaterialTheme.colorScheme.onSurface) }
            nutriments.salt?.let { Text(text = "Salt: ${it} ${nutriments.salt_unit}", color = MaterialTheme.colorScheme.onSurface) }
            nutriments.fiber?.let { Text(text = "Fiber: ${it} ${nutriments.fiber_unit}", color = MaterialTheme.colorScheme.onSurface) }
            nutriments.calcium?.let { Text(text = "Calcium: ${it} ${nutriments.calcium_unit}", color = MaterialTheme.colorScheme.onSurface) }
            nutriments.iodine?.let { Text(text = "Iodine: ${it} ${nutriments.iodine_unit}", color = MaterialTheme.colorScheme.onSurface) }
            nutriments.vitamin_b12?.let { Text(text = "Vitamin B12: ${it} ${nutriments.vitamin_b12_unit}", color = MaterialTheme.colorScheme.onSurface) }
            nutriments.vitamin_b2?.let { Text(text = "Vitamin B2: ${it} ${nutriments.vitamin_b2_unit}", color = MaterialTheme.colorScheme.onSurface) }
            nutriments.vitamin_d?.let { Text(text = "Vitamin D: ${it} ${nutriments.vitamin_d_unit}", color = MaterialTheme.colorScheme.onSurface) }
        }
    }
}

