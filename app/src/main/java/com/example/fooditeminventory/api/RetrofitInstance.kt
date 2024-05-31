package com.example.fooditeminventory.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://world.openfoodfacts.org/"
    private const val BASE_URL_MEAL = "https://meal-suggestions-api-pngv3dyqfa-uc.a.run.app/foodplan/us-central1/meal_suggestions_api/"

    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    val foodApi: OpenFoodFactsService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenFoodFactsService::class.java)
    }

    val mealApi: MealSuggestionService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_MEAL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MealSuggestionService::class.java)
    }
}
