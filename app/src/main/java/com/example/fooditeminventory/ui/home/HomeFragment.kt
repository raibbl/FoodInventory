package com.example.fooditeminventory.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val database = ProductDatabase.getDatabase(context)
    val productDao = database.productDao()
    val products = remember { mutableStateOf<List<ProductEntity>>(emptyList()) }
    val mealType = remember { mutableStateOf("Dinner") }
    val suggestions = remember { mutableStateOf<String>("") }
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            products.value = productDao.getAllProducts()
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Get AI Generated Meal") },
                icon = { Icon(Icons.Filled.Info, contentDescription = null) },
                onClick = {
                    showBottomSheet = true
                }
            )
        }, content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    ProductList(products = products.value, onDelete = { product ->
                        coroutineScope.launch(Dispatchers.IO) {
                            productDao.deleteProduct(product)
                            products.value = productDao.getAllProducts()
                        }
                    })
                }
            }
        }
    )

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
        ) {
            MealSuggestionSection(
                mealType = mealType.value,
                suggestions = suggestions.value,
                onMealTypeChange = { mealType.value = it },
                onFetchSuggestions = {
                    coroutineScope.launch {
                        fetchMealSuggestions(products.value, mealType.value, suggestions)
                        showSuggestions = true
                    }
                },
                onDismiss = {
                    coroutineScope.launch {
                        sheetState.hide()
                        showBottomSheet = false
                    }
                },
                showSuggestions = showSuggestions
            )
        }
    }
}

@Composable
fun MealSuggestionSection(
    mealType: String,
    suggestions: String,
    onMealTypeChange: (String) -> Unit,
    onFetchSuggestions: () -> Unit,
    onDismiss: () -> Unit,
    showSuggestions: Boolean
) {
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            MealTypeDropdown(
                mealType = mealType,
                onMealTypeChange = onMealTypeChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )
            Button(
                onClick = {
                    isLoading = true
                    coroutineScope.launch {
                        onFetchSuggestions()
                        isLoading = false
                    }
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Get Meal Suggestions")
            }
        }
        if (isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else if (showSuggestions) {
            LazyColumn(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                item {
                    Text(
                        suggestions,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
        Button(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
        ) {
            Text("Hide bottom sheet")
        }
    }
}

@Composable
fun MealTypeDropdown(
    mealType: String,
    onMealTypeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    mealTypes: List<String> = listOf("Breakfast", "Lunch", "Dinner", "Snack")
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        TextButton(onClick = { expanded = true }) {
            Text(text = mealType)
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
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
