package com.example.wheatherfit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.wheatherfit.data.local.AppDatabase
import com.example.wheatherfit.data.repository.UserRepository
import com.example.wheatherfit.viewmodel.UserViewModel
import com.example.wheatherfit.viewmodel.UserViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LogoutFragment : Fragment() {
    lateinit var auth: FirebaseAuth
    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = Firebase.auth
        auth.signOut()

        val userDao = AppDatabase.getDatabase(requireContext()).userDao()
        val repository = UserRepository(userDao)
        val factory = UserViewModelFactory(repository)
        userViewModel = ViewModelProvider(this, factory).get(UserViewModel::class.java)
        userViewModel.logout()

        val view = inflater.inflate(R.layout.fragment_logout, container, false)

        findNavController().navigate(R.id.action_logoutFragment_to_loginFragment)

        return view
    }

}