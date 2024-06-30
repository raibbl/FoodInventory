package com.example.fooditeminventory.db


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fooditeminventory.api.Nutriments
import java.util.UUID

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val uuid: String = UUID.randomUUID().toString(),
    val barcode: String,
    var name: String,
    var brand: String,
    var ingredients: String,
    val nutriments: Nutriments?,
    val allergens:String?,
    val images: List<String>,
    var quantity:Int,
    var product_quantity:String?,
    var product_quantity_unit:String?,
    var quantityAndUnit:String?,
    val serving_size: String?,
)
