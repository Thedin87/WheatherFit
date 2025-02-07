package com.example.wheatherfit.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wheatherfit.R
import com.example.wheatherfit.data.models.Post
import com.example.wheatherfit.viewmodel.PostViewModel
import com.example.wheatherfit.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.roundToInt

class PostAdapter(private val postList: List<Post>, postViewModel: PostViewModel, context: Context) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    val postViewModel: PostViewModel = postViewModel
    val context: Context = context

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userName: TextView = view.findViewById(R.id.post_user_name)
        val postImage: ImageView = view.findViewById(R.id.post_image)
        val postDescription: TextView = view.findViewById(R.id.post_description)
        val postWeather: TextView = view.findViewById(R.id.post_weather)
        val starContainer: LinearLayout = view.findViewById(R.id.star_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]

        // Load image using Glide
        Glide.with(holder.itemView.context)
            .load(post.image_url)
            .into(holder.postImage)

        // Set description and weather
        holder.postDescription.text = post.description
        holder.postWeather.text = "${post.weather.roundToInt()}°C"

        // Fetch user name from Firestore
        FirebaseFirestore.getInstance().collection("users")
            .document(post.user_id)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val firstName = document.getString("firstname") ?: ""
                    val lastName = document.getString("lastname") ?: ""
                    holder.userName.text = "$firstName $lastName"
                } else {
                    holder.userName.text = "Unknown User"
                }
            }
            .addOnFailureListener {
                holder.userName.text = "Unknown User"
            }

        // Calculate and set stars dynamically
        val rating = if (post.rating_count > 0) (post.rating_sum / post.rating_count).roundToInt() else 0
        setStarRating(holder.starContainer, rating, holder.itemView, post)

        // Set click listener for star clicks
        setStarClickListener(holder.starContainer, post)
    }

    override fun getItemCount(): Int = postList.size

    private fun setStarRating(starContainer: LinearLayout, rating: Int, view: View, post: Post) {
        starContainer.removeAllViews()  // Clear previous stars

        // Show 5 stars
        for (i in 0 until 5) {
            val star = ImageView(view.context)
            val drawableRes = if (i < rating) {
                R.drawable.ic_star_filled_foreground // Full star
            } else {
                R.drawable.ic_star_grayed_foreground // Grayed star
            }

            star.setImageDrawable(ContextCompat.getDrawable(view.context, drawableRes))

            // Set fixed width and height to make the stars small
            val size = 44 // Adjust the size as needed
            val layoutParams = LinearLayout.LayoutParams(size, size).apply {
                marginEnd = 8  // Space between stars
            }
            star.layoutParams = layoutParams

            starContainer.addView(star)
        }
    }

    private fun setStarClickListener(starContainer: LinearLayout, post: Post) {
        for (i in 0 until starContainer.childCount) {
            val star = starContainer.getChildAt(i)
            star.setOnClickListener {

                // Log the clicked star's position and the post ID
                Log.d("StarClick", "Star ${i + 1} clicked for Post ID: ${post.id}")

                val user_id = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                Log.d("StarClick", "User ID: $user_id")

                postViewModel.updateRating(context, post.id, user_id, i + 1)
            }
        }
    }
}
