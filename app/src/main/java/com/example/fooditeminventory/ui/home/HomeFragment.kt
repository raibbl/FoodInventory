package com.example.fooditeminventory.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
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
import com.example.fooditeminventory.api.Product
import com.example.fooditeminventory.ui.ProductList


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
                        .align(Alignment.BottomEnd)  // Ensure button is positioned at the bottom right
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        content = { padding ->
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(padding)) {
                val sampleProducts = listOf(
                    Product("Apple", "Fresh Fruits", "Apples", null, null),
                    Product("Banana", "Tropical Fruits", "Bananas", null, null),
                    Product("Banana", "Tropical Fruits", "Bananas", null, null),
                    Product("Banana", "Tropical Fruits", "Bananas", null, null),
                    Product("Banana", "Tropical Fruits", "Bananas", null, null),
                    Product("Banana", "Tropical Fruits", "Bananas", null, null),
                    Product("Banana", "Tropical Fruits", "Bananas", null, null)
                )
                ProductList(products = sampleProducts)
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

