package com.example.fooditeminventory.db

import androidx.room.TypeConverter
import com.example.fooditeminventory.api.Nutriments
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    @TypeConverter
    fun fromNutriments(nutriments: Nutriments?): String? {
        if (nutriments == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<Nutriments>() {}.type
        return gson.toJson(nutriments, type)
    }

    @TypeConverter
    fun toNutriments(nutrimentsString: String?): Nutriments? {
        if (nutrimentsString == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<Nutriments>() {}.type
        return gson.fromJson(nutrimentsString, type)
    }
}
