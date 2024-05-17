package com.example.fooditeminventory.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.fooditeminventory.api.Product

@Composable
fun ProductList(products: List<Product>) {
    LazyColumn(modifier = Modifier.padding(5.dp)) {
        items(products) { product ->
            Column(modifier = Modifier.padding(8.dp)) {
                ProductItem(product)
            }
        }
    }
}
@Composable
fun ProductItem(product: Product) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(text = product.product_name, style = MaterialTheme.typography.headlineSmall)
            Text(text = "Brand: ${product.brands}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Ingredients: ${product.ingredients_text}", style = MaterialTheme.typography.bodyMedium)
//            val imageUrl = product.image_url ?: "https://via.placeholder.com/150"
//            Image(
//                painter = rememberAsyncImagePainter(imageUrl),
//                contentDescription = null,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(150.dp)
//                    .padding(top = 8.dp)
//            )
        }
    }
}