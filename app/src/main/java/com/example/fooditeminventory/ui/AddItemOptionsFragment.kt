package com.example.fooditeminventory

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.fooditeminventory.databinding.FragmentAddItemOptionsBinding

class AddItemOptionsFragment : Fragment() {

    private var _binding: FragmentAddItemOptionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddItemOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonAddManually.setOnClickListener {
            findNavController().navigate(R.id.action_addItemOptionsFragment_to_addManuallyFragment)
        }

        binding.buttonUseBarcodeScanner.setOnClickListener {
            findNavController().navigate(R.id.action_addItemOptionsFragment_to_barcodeScannerFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
