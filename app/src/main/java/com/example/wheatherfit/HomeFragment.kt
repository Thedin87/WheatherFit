package com.example.wheatherfit

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.wheatherfit.data.local.AppDatabase
import com.example.wheatherfit.data.local.User
import com.example.wheatherfit.data.repository.UserRepository
import com.example.wheatherfit.viewmodel.UserViewModel
import com.example.wheatherfit.viewmodel.UserViewModelFactory
import com.example.wheatherfit.viewmodel.WeatherViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private val weatherViewModel: WeatherViewModel by viewModels()
    private lateinit var userViewModel: UserViewModel

    fun renderNav(user: User) {

        val childFragment = BottomNavFragment()
        val bundle = Bundle()
        bundle.putString("current_page", "home")
        bundle.putString("firstname", user.firstname)

        childFragment.arguments = bundle

        // Add the child fragment to the container
        childFragmentManager.beginTransaction()
            .replace(R.id.navbar_container, childFragment)
            .commit()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val cityAndCountry = "Mevasseret Zion"
        val apiKey = "7f21c93531c74c408f893537253101"
        viewLifecycleOwner.lifecycleScope.launch {
            val weather = weatherViewModel.fetchWeather(apiKey, cityAndCountry)
            val currentTemp = weather?.current?.temp_c?.toFloat() ?: 0f // Get the temperature and convert to float
            Log.d("Weather", "Updated temp: $currentTemp")  // Now logs correctly

        }

        val userDao = AppDatabase.getDatabase(requireContext()).userDao()
        val repository = UserRepository(userDao)
        val factory = UserViewModelFactory(repository)
        userViewModel = ViewModelProvider(this, factory).get(UserViewModel::class.java)

        val users = userViewModel.getUsers(
            callback = {
                users ->
                    run {
                        Log.d("Home", users.toString())
                        if (users.isEmpty()) {
                            Toast.makeText(requireActivity(), "Not logged in", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_homeFragment_to_logoutFragment)
                        }
                        else {

                            renderNav(users[0])

                        }
                    }
            }
        );

        return view
    }

}