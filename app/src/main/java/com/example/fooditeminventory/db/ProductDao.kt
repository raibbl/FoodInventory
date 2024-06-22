package com.example.fooditeminventory.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(product: ProductEntity)

    @Query("SELECT * FROM products WHERE uuid = :uuid")
    fun getProductByUuid(uuid: String): ProductEntity?

    @Query("SELECT * FROM products")
    fun getAllProducts(): List<ProductEntity>
    @Delete
    fun deleteProduct(product: ProductEntity)
}
