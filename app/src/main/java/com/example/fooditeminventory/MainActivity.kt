package com.example.fooditeminventory

import SimpleOnboardingScreen
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.fooditeminventory.ui.theme.FoodItemInventoryTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth



    public override fun onStart() {
        super.onStart()
        // Initialize Firebase Auth
        auth = Firebase.auth
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser==null){
            auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInAnonymously:success")
                        val user = auth.currentUser
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInAnonymously:failure", task.exception)
                    }
                }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("onboarding_prefs", MODE_PRIVATE)
        val hasCompletedOnboarding = sharedPreferences.getBoolean("hasCompletedOnboarding", false)

        if (!hasCompletedOnboarding) {
            setContentView(ComposeView(this).apply {
                setContent {
                    FoodItemInventoryTheme {
                        SimpleOnboardingScreen(onFinished = {
                            sharedPreferences.edit().putBoolean("hasCompletedOnboarding", true).apply()
                            restartMainActivity()
                        })
                    }
                }
            })
        } else {
            // Regular setup after onboarding is completed
            setContentView(R.layout.activity_main)
            setSupportActionBar(findViewById(R.id.toolbar))
            setupActionBarWithNavController(findNavController(R.id.nav_host_fragment_content_main))
        }
    }

    private fun restartMainActivity() {
        finish()
        startActivity(intent)
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
