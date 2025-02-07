package com.example.wheatherfit.data.repository

import android.util.Log
import com.example.wheatherfit.data.api.RetrofitInstance
import com.example.wheatherfit.data.models.Weather

class WeatherRepository {
    suspend fun getWeather(apiKey: String, cityAndCountry: String): Weather {
        return RetrofitInstance.api.getCurrentWeather(apiKey, cityAndCountry)
    }
}