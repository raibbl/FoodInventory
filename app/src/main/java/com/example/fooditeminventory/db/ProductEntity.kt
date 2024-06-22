package com.example.fooditeminventory.db


import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val uuid: String = UUID.randomUUID().toString(),
    val barcode: String,
    val name: String,
    val brand: String,
    val ingredients: String,
    val imageUrl: String?
)
