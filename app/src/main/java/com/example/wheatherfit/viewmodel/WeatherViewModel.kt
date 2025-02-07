package com.example.wheatherfit.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wheatherfit.data.models.Weather
import com.example.wheatherfit.data.repository.WeatherRepository
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {
    private val repository = WeatherRepository()

    fun fetchWeather(apiKey: String, cityAndCountry: String, onResult: (Weather?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.getWeather(apiKey, cityAndCountry)
                onResult(response)
            } catch (e: Exception) {
                Log.d("Weather", e.message.toString())
                e.printStackTrace()
                onResult(null)
            }
        }
    }
}