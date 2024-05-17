package com.example.fooditeminventory.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

data class ProductResponse(
    val code: String,
    val product: Product
)



data class Product(
    val product_name: String,
    val brands: String,
    val ingredients_text: String,
    val nutriments: Nutriments? = null,
    val image_url:String? = null
)

data class Nutriments(
    val energy: String?,
    val fat: String?,
    val saturated_fat: String?,
    val carbohydrates: String?,
    val sugars: String?,
    val proteins: String?,
    val salt: String?,
    val fiber: String?
)

interface OpenFoodFactsService {
    @GET("api/v0/product/{barcode}.json")
    fun getProduct(@Path("barcode") barcode: String): Call<ProductResponse>
}
