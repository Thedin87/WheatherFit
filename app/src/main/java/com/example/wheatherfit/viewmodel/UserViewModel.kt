package com.example.wheatherfit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wheatherfit.data.local.User
import com.example.wheatherfit.data.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel (private val repository: com.example.wheatherfit.data.repository.UserRepository) : ViewModel() {

    fun addUser(user: User) {
        viewModelScope.launch {
            repository.insertUser(user)
        }
    }

    fun getUsers(callback: (List<User>) -> Unit) {
        viewModelScope.launch {
            callback(repository.getAllUsers())
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            repository.deleteUser(user)
        }

    }

}