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
    var product_quantity:String?,
    var product_quantity_unit:String?,
    var quantity:String?,
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
    val energy: Double?,
    val energy_unit: String?,
    val fat: Double?,
    val fat_unit: String?,
    val saturated_fat: Double?,
    val saturated_fat_unit: String?,
    val carbohydrates: Double?,
    val carbohydrates_unit: String?,
    val sugars: Double?,
    val sugars_unit: String?,
    val proteins: Double?,
    val proteins_unit: String?,
    val salt: Double?,
    val salt_unit: String?,
    val fiber: Double?,
    val fiber_unit: String?,
    val calcium: Double?,
    val calcium_unit: String?,
    val iodine: Double?,
    val iodine_unit: String?,
    val vitamin_b12: Double?,
    val vitamin_b12_unit: String?,
    val vitamin_b2: Double?,
    val vitamin_b2_unit: String?,
    val vitamin_d: Double?,
    val vitamin_d_unit: String?
)


interface OpenFoodFactsService {
    @GET("api/v3/product/{barcode}.json")
    fun getProduct(@Path("barcode") barcode: String): Call<ProductResponse>
}
