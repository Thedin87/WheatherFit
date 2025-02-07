package com.example.wheatherfit

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.wheatherfit.viewmodel.WeatherViewModel

class HomeFragment : Fragment() {
    private val weatherViewModel: WeatherViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(com.example.wheatherfit.R.layout.fragment_home, container, false)

        val childFragment = BottomNavFragment()
        val bundle = Bundle()
        bundle.putString("current_page", "home")

        childFragment.arguments = bundle

        // Add the child fragment to the container
        childFragmentManager.beginTransaction()
            .replace(com.example.wheatherfit.R.id.navbar_container, childFragment)
            .commit()

        val apiKey = "APIKEY"
        val cityAndCountry = "Mevasseret Zion"

        weatherViewModel.fetchWeather(apiKey, cityAndCountry) { weather ->
            if (weather != null) {
                Log.d("Weather", "City: ${weather.location.name}, Temp: ${weather.current.temp_c}°C, Condition: ${weather.current.condition.text}")
            } else {
                Log.e("Weather", "Failed to fetch weather")
            }
        }

        return view
    }

}