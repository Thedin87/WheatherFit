package com.example.wheatherfit.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wheatherfit.data.models.Weather
import com.example.wheatherfit.data.repository.WeatherRepository
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {
    private val repository = WeatherRepository()

    suspend fun fetchWeather(apiKey: String, city: String): Weather? {
        return try {
            repository.getWeather(apiKey, city)
        } catch (e: Exception) {
            Log.e("Weather", "Error fetching weather: ${e.message}")
            null
        }
    }
}