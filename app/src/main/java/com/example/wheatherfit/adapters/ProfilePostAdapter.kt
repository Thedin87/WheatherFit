package com.example.wheatherfit.adapters

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.os.registerForAllProfilingResults
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wheatherfit.R
import com.example.wheatherfit.data.models.Post
import com.example.wheatherfit.viewmodel.PostViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlin.math.roundToInt



class ProfilePostAdapter(private val postList: List<Post>, postViewModel: PostViewModel, context: Context) : RecyclerView.Adapter<ProfilePostAdapter.ProfilePostViewHolder>() {
    val context: Context = context


    class ProfilePostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userName: TextView = view.findViewById(R.id.post_user_name_profile)
        val postImage: ImageView = view.findViewById(R.id.post_image_profile)
        val postDescription: TextView = view.findViewById(R.id.post_description_profile)
        val postWeather: TextView = view.findViewById(R.id.post_weather_profile)
        val starContainer: LinearLayout = view.findViewById(R.id.star_container_profile)
        val postProfilePicture: ImageView = view.findViewById(R.id.profile_picture_in_post_profile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfilePostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post_profile, parent, false)
        return ProfilePostViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfilePostViewHolder, position: Int) {
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
                    val profileUrl = document.getString("profileImageUrl") ?: ""
                    holder.userName.text = "$firstName $lastName"
                    if (!profileUrl.isNullOrEmpty()) {
                        Picasso.get()
                            .load(profileUrl)
                            .placeholder(R.drawable.profile_foreground) // Show default while loading
                            .error(R.drawable.profile_foreground) // Show default if URL is broken
                            .resize(200, 200) // Resize to fit ImageView (optional)
                            .centerCrop()
                            .into(holder.postProfilePicture)
                    }
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

    }

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

    override fun getItemCount(): Int = postList.size
}
