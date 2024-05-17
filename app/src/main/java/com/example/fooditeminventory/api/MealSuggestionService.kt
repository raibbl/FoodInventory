// MealSuggestionService.kt
package com.example.fooditeminventory.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Header

data class MealSuggestionRequest(val ingredients: String, val mealType: String)
data class MealSuggestionResponse(val suggestions: String)

interface MealSuggestionService {
    @POST("/get_meal_suggestions")
    fun getMealSuggestions(
        @Header("Authorization") authHeader: String,
        @Body request: MealSuggestionRequest
    ): Call<MealSuggestionResponse>
}
