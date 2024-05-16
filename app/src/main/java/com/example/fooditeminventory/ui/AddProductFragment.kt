package com.example.fooditeminventory.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.fooditeminventory.databinding.FragmentAddProductBinding

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

        // Handle the save button click
        binding.saveButton.setOnClickListener {
            // Get the values from the EditText fields
            val updatedProductName = binding.productName.text.toString()
            val updatedProductBrand = binding.productBrand.text.toString()
            val updatedProductIngredients = binding.productIngredients.text.toString()

            // Save the product information to the app's data storage (logic here)
            // Consider adding validation for user input before saving
            Toast.makeText(requireContext(), "Product saved!", Toast.LENGTH_SHORT).show()

            // Optionally, navigate back or to another screen
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
