package com.example.fooditeminventory.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

data class ProductResponse(
    val code: String,
    val product: Product
)



data class Product(
    var product_name: String,
    var brands: String,
    var ingredients_text: String,
    val code :String,
    val nutriments: Nutriments? = null,
    val image_url:String? = null,
    val selected_images: SelectedImages? = null,
    val allergens: String? = null,
    val serving_size:String? = null,
)

data class SelectedImages(
    val front: ImageType?,
    val ingredients: ImageType?,
    val nutrition: ImageType?,
    val packaging: ImageType?
)

data class ImageType(
    val display: ImageTypeProps?,
    val small: ImageTypeProps?,
    val thumb: ImageTypeProps?
)

data class ImageTypeProps(
    val en: String,
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
    @GET("api/v3/product/{barcode}.json")
    fun getProduct(@Path("barcode") barcode: String): Call<ProductResponse>
}
