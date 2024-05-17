package com.example.fooditeminventory

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        setupActionBarWithNavController(findNavController(R.id.nav_host_fragment_content_main))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_add_product -> {
                showAddProductMenu(findViewById(R.id.menu_add_product))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAddProductMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.menu_add_product, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_add_manually -> {
                    findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.addProductFragment)
                    true
                }
                R.id.menu_use_barcode_scanner -> {
                    findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.barcodeScannerFragment)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
