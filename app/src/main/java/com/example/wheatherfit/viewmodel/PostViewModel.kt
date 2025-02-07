package com.example.wheatherfit.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.wheatherfit.data.repository.PostRepository

class PostViewModel : ViewModel() {
    private val repository = PostRepository()

    fun updateRating(context: Context, postId: String, user_id: String, rating: Int) {
        // Call the repository to update the rating in Firebase
        repository.updatePostRating(context, postId, user_id, rating)
    }
}