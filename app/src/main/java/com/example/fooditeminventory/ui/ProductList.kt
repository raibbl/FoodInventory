package com.example.fooditeminventory.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.DismissDirection
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.fooditeminventory.db.ProductEntity

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProductList(products: List<ProductEntity>, onDelete: (ProductEntity) -> Unit) {
    LazyColumn(modifier = Modifier.padding(5.dp)) {
        items(products, key = { it.id }) { product ->
            val dismissState = rememberDismissState()

            if (dismissState.isDismissed(DismissDirection.EndToStart)) {
                onDelete(product)
            }

            SwipeToDismiss(
                state = dismissState,
                directions = setOf(DismissDirection.EndToStart),
                background = {
                    val color = when (dismissState.dismissDirection) {
                        DismissDirection.EndToStart -> Color.Red
                        else -> Color.Transparent
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color)
                            .padding(8.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White
                        )
                    }
                },
                dismissContent = {
                    ProductItem(product)
                }
            )
        }
    }
}

@Composable
fun ProductItem(product: ProductEntity) {
    Card(
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(12.dp)
        ) {
            Text(text = product.name, style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
            Text(text = "Brand: ${product.brand}", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
        }
    }
}
