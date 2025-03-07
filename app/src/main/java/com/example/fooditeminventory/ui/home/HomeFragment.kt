package com.example.fooditeminventory.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.fooditeminventory.db.AppDatabase
import com.example.fooditeminventory.db.ProductEntity
import com.example.fooditeminventory.ui.ProductList
import com.example.fooditeminventory.ui.home.mealSuggestion.MealSuggestionSection
import com.example.fooditeminventory.ui.home.mealSuggestion.fetchMealSuggestions
import com.example.fooditeminventory.ui.theme.FoodItemInventoryTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val navController = findNavController()
                FoodItemInventoryTheme {
                    HomeScreen(navController = navController)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val productDao = database.productDao()
    val products = remember { mutableStateOf<List<ProductEntity>>(emptyList()) }
    val mealType = remember { mutableStateOf("Dinner") }
    val suggestions = remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            products.value = productDao.getAllProducts()
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Get AI Generated Meal", color = Color.White) },
                icon = { Icon(Icons.Filled.Info, contentDescription = null, tint =  Color.White) },
                onClick = {
                    showBottomSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )

        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    ProductList(
                        products = products.value,
                        onDelete = { product ->
                            coroutineScope.launch(Dispatchers.IO) {
                                productDao.deleteProduct(product)
                                products.value = productDao.getAllProducts()
                            }
                        },
                        navController = navController
                    )
                }
            }
        }
    )

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            MealSuggestionSection(
                mealType = mealType.value,
                suggestions = suggestions.value,
                onMealTypeChange = { mealType.value = it },
                onFetchSuggestions = { isLoading ->
                    coroutineScope.launch {
                        fetchMealSuggestions(products.value, mealType.value, suggestions, isLoading)
                    }
                },
            )
        }
    }
}
