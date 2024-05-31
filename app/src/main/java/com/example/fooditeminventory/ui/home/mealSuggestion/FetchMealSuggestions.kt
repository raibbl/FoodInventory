package com.example.fooditeminventory.ui.home.mealSuggestion

import androidx.compose.runtime.MutableState
import com.example.fooditeminventory.api.MealSuggestionRequest
import com.example.fooditeminventory.api.MealSuggestionResponse
import com.example.fooditeminventory.api.RetrofitInstance
import com.example.fooditeminventory.db.ProductEntity
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
    val request = MealSuggestionRequest(ingredients, mealType, "baba1969r1911")
    isLoading.value = true
    RetrofitInstance.mealApi.getMealSuggestions(request).enqueue(object :
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

            isLoading.value = false

        }

        override fun onFailure(call: Call<MealSuggestionResponse>, t: Throwable) {
            suggestions.value = "Failed to fetch suggestions"
            isLoading.value = false
        }


    })
}