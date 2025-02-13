package com.example.wheatherfit.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = false) val id: String = "",
    val email: String? = null,
    val firstname: String = "",
    val lastname: String = "",
    val city: String = "",
    val country: String = "",
    val profileImageUrl: String? = null,
    val imageBlob: ByteArray? = null
)