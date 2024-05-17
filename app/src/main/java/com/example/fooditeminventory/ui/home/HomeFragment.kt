package com.example.fooditeminventory.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.fooditeminventory.R
import com.example.fooditeminventory.api.MealSuggestionRequest
import com.example.fooditeminventory.api.MealSuggestionResponse
import com.example.fooditeminventory.api.RetrofitInstance
import com.example.fooditeminventory.db.ProductDatabase
import com.example.fooditeminventory.db.ProductEntity
import com.example.fooditeminventory.ui.ProductList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Credentials
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val navController = findNavController()
                MaterialTheme {
                    HomeScreen(navController = navController)
                }
            }
        }
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val database = ProductDatabase.getDatabase(context)
    val productDao = database.productDao()
    val products = remember { mutableStateOf<List<ProductEntity>>(emptyList()) }
    val mealType = remember { mutableStateOf("Dinner") }
    val suggestions = remember { mutableStateOf<String>("") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            products.value = productDao.getAllProducts()
        }
    }

    Scaffold(
        floatingActionButton = {
            Box {
                var anchorView: View? = null
                AndroidView(
                    factory = { context ->
                        View(context).apply {
                            anchorView = this
                        }
                    },
                    modifier = Modifier.matchParentSize()
                )
                FloatingActionButton(
                    onClick = {
                        anchorView?.let {
                            showPopupMenu(context, it, navController)
                        }
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                ProductList(products = products.value)
                MealSuggestionSection(
                    mealType = mealType.value,
                    suggestions = suggestions.value,
                    onMealTypeChange = { mealType.value = it }
                ) {
                    fetchMealSuggestions(products.value, mealType.value, suggestions)
                }
            }
        }
    )
}

fun showPopupMenu(context: Context, anchor: View, navController: NavController) {
    PopupMenu(context, anchor).apply {
        menuInflater.inflate(R.menu.menu_fab, menu)
        setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_add_manually -> {
                    navController.navigate(R.id.addProductFragment)
                    true
                }

                R.id.menu_use_barcode_scanner -> {
                    navController.navigate(R.id.barcodeScannerFragment)
                    true
                }

                else -> false
            }
        }
    }.show()
}

@Composable
fun MealSuggestionSection(
    mealType: String,
    suggestions: String,
    onMealTypeChange: (String) -> Unit,
    onFetchSuggestions: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Meal Suggestions", style = MaterialTheme.typography.titleMedium)
        MealTypeDropdown(
            mealType = mealType,
            onMealTypeChange = onMealTypeChange
        )
        Button(onClick = onFetchSuggestions, modifier = Modifier.padding(top = 8.dp)) {
            Text("Get Suggestions")
        }
        LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
            item {
                Text(suggestions, modifier = Modifier.padding(4.dp))
            }
        }
    }
}

@Composable
fun MealTypeDropdown(
    mealType: String,
    onMealTypeChange: (String) -> Unit,
    mealTypes: List<String> = listOf("Breakfast", "Lunch", "Dinner", "Snack")
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp)) {
        TextButton(onClick = { expanded = true }) {
            Text(text = mealType)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            mealTypes.forEach { type ->
                DropdownMenuItem(
                    text = { Text(text = type) },
                    onClick = {
                        onMealTypeChange(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

fun fetchMealSuggestions(
    products: List<ProductEntity>,
    mealType: String,
    suggestions: MutableState<String>
) {
    val ingredients = products.joinToString(", ") { "${it.name}, ${it.brand}" }
    val request = MealSuggestionRequest(ingredients, mealType)
    val authHeader = Credentials.basic("raibbl", "baba1969r19r")

    RetrofitInstance.mealApi.getMealSuggestions(authHeader, request).enqueue(object :
        Callback<MealSuggestionResponse> {
        override fun onResponse(
            call: Call<MealSuggestionResponse>,
            response: Response<MealSuggestionResponse>
        ) {
            if (response.isSuccessful) {
                suggestions.value = response.body()?.suggestions.toString()
            } else {
                suggestions.value = "Failed to fetch suggestions"
            }
        }

        override fun onFailure(call: Call<MealSuggestionResponse>, t: Throwable) {
            suggestions.value = "Failed to fetch suggestions"
        }
    })
}
