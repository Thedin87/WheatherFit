package com.example.wheatherfit

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.wheatherfit.viewmodel.WeatherViewModel
import kotlinx.coroutines.launch

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

        val cityAndCountry = "Mevasseret Zion"
        val apiKey = "<api_key>"
        viewLifecycleOwner.lifecycleScope.launch {
            val weather = weatherViewModel.fetchWeather(apiKey, cityAndCountry)
            Log.d("Weather", "Updated temp: ${weather?.current?.temp_c}")  // Now logs correctly
        }

        return view
    }

}