package com.example.fooditeminventory.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.fooditeminventory.databinding.FragmentAddProductBinding
import com.example.fooditeminventory.db.ProductDatabase
import com.example.fooditeminventory.db.ProductEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddProductFragment : Fragment() {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    private val args: AddProductFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the arguments passed from BarcodeScannerFragment
        val productName = args.productName ?: ""
        val productBrand = args.productBrand ?: ""
        val productIngredients = args.productIngredients ?: ""
        val productImageUrl = args.productImageUrl ?: ""

        // Set the values to the EditText fields
        binding.productName.setText(productName)
        binding.productBrand.setText(productBrand)
        binding.productIngredients.setText(productIngredients)

        // Load the product image if available
        if (productImageUrl.isNotEmpty()) {
            binding.productImage.load(productImageUrl)
        }

        binding.saveButton.setOnClickListener {
            val updatedProductName = binding.productName.text.toString()
            val updatedProductBrand = binding.productBrand.text.toString()
            val updatedProductIngredients = binding.productIngredients.text.toString()
            val updatedProductImageUrl = if (productImageUrl.isNotEmpty()) productImageUrl else null

            val product = ProductEntity(
                name = updatedProductName,
                brand = updatedProductBrand,
                ingredients = updatedProductIngredients,
                imageUrl = updatedProductImageUrl
            )

            lifecycleScope.launch(Dispatchers.IO) {
                val db = ProductDatabase.getDatabase(requireContext())
                db.productDao().insert(product)
                launch(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Product saved!", Toast.LENGTH_SHORT).show()
                    val action = AddProductFragmentDirections.actionAddProductFragmentToHomeFragment()
                    findNavController().navigate(action)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
