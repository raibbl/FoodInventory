package com.example.fooditeminventory.ui.home.mealSuggestion

import androidx.compose.runtime.MutableState
import com.example.fooditeminventory.api.MealSuggestionRequest
import com.example.fooditeminventory.api.MealSuggestionResponse
import com.example.fooditeminventory.api.RetrofitInstance
import com.example.fooditeminventory.db.ProductEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun fetchMealSuggestions(
    products: List<ProductEntity>,
    mealType: String,
    suggestions: MutableState<String>,
    isLoading: MutableState<Boolean>
) {
    val ingredients = products.joinToString(", ") { "${it.name}, ${it.brand}" }
    val request = MealSuggestionRequest(ingredients, mealType, "")
    val auth: FirebaseAuth = Firebase.auth
    val currentUser = auth.currentUser
    if (currentUser == null) {
        suggestions.value = "User not authenticated"
        return
    }
    isLoading.value = true

    currentUser.getIdToken(false).addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val idToken = task.result?.token ?: ""
            try {
                RetrofitInstance.mealApi.getMealSuggestions("Bearer $idToken", request).enqueue(object :
                    Callback<MealSuggestionResponse> {
                    override fun onResponse(
                        call: Call<MealSuggestionResponse>,
                        response: Response<MealSuggestionResponse>
                    ) {
                        suggestions.value = if (response.isSuccessful) {
                            response.body()?.suggestions.toString()
                        } else {
                            "Failed to fetch suggestions"
                        }
                    }

                    override fun onFailure(call: Call<MealSuggestionResponse>, t: Throwable) {
                        suggestions.value = "Failed to fetch suggestions"
                    }
                })
            } finally {
                isLoading.value = false
            }
        } else {
            suggestions.value = "Failed to fetch authentication token"
            isLoading.value = false
        }
    }
}