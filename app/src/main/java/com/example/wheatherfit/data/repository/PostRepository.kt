package com.example.wheatherfit.data.repository

import android.content.Context
import android.widget.Toast
import com.example.wheatherfit.adapters.PostAdapter
import com.example.wheatherfit.data.models.Post
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class PostRepository {
    fun updatePostRating(context: Context, postId: String, user_id: String, rating: Int) {

        val db = FirebaseFirestore.getInstance()

        // Check if the user has already rated the post
        db.collection("posts")
            .document(postId)
            .get().addOnSuccessListener {
                val post = it.toObject(Post::class.java)
                val users = post!!.rating_users_ids.toMutableList()
                var updated = false

                for (i in users.indices) {

                    if (users[i].startsWith(user_id)) {

                        var currentRating = users[i].split(":::")[users[i].split(":::").size - 1].toInt()
                        var change: Int = rating - currentRating

                        users[i] = "${user_id}:::$rating"

                        db.collection("posts")
                            .document(postId)
                            .update("rating_users_ids", users)

                        db.collection("posts")
                            .document(postId)
                            .update("rating_sum", FieldValue.increment(change.toLong()))

                        updated = true
                        Toast.makeText(context, "Review updated", Toast.LENGTH_SHORT).show()

                    }

                }
                if (updated == false) {

                    // Insert rating
                    db.collection("posts")
                        .document(postId)
                        .update("rating_users_ids", FieldValue.arrayUnion("${user_id}:::$rating"))

                    db.collection("posts")
                        .document(postId)
                        .update("rating_sum", FieldValue.increment(rating.toLong()))

                    db.collection("posts")
                        .document(postId)
                        .update("rating_count", FieldValue.increment(1))

                    Toast.makeText(context, "Rating added", Toast.LENGTH_SHORT).show()

                }
            }

        // Firebase logic to update the rating of a post
        db.collection("posts")
            .document(postId)
            .update("rating", rating)
            .addOnSuccessListener {
                // Handle success (maybe notify UI if needed)
            }
            .addOnFailureListener { exception ->
                // Handle failure (e.g., show error to the user)
            }
    }
}