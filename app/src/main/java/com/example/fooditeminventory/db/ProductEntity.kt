package com.example.fooditeminventory.db


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fooditeminventory.api.Nutriments
import java.util.UUID

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val uuid: String = UUID.randomUUID().toString(),
    val barcode: String,
    val name: String,
    val brand: String,
    val ingredients: String,
    val nutriments: Nutriments?,
    val allergens:String?,
    val images: List<String>,
    val quantity:Int,
    val serving_size: String?,
)
