package com.example.wheatherfit

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.wheatherfit.adapters.PostAdapter
import com.example.wheatherfit.adapters.ProfilePostAdapter
import com.example.wheatherfit.data.local.AppDatabase
import com.example.wheatherfit.data.models.Post
import com.example.wheatherfit.data.repository.UserRepository
import com.example.wheatherfit.viewmodel.PostViewModel
import com.example.wheatherfit.viewmodel.UserViewModel
import com.example.wheatherfit.viewmodel.UserViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.ByteArrayInputStream

class ProfileFragment : Fragment() {
    private lateinit var profilePostAdapter: ProfilePostAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var postList = mutableListOf<Post>()
    val postViewModel = PostViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val childFragment = BottomNavFragment()
        val bundle = Bundle()
        bundle.putString("current_page", "profile")

        val profileImage = view.findViewById<ImageView>(R.id.profile_picture)
        loadProfilePicture(profileImage)

        val editProfilePictureText = view.findViewById<TextView>(R.id.edit_profile_picture_text)

        editProfilePictureText.setOnClickListener{
            Log.d("ProfileFragment", "Edit Profile Picture Text Clicked")
            openGallery()
            loadProfilePicture(profileImage)
        }

        // Set up SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout_profile)
        swipeRefreshLayout.setOnRefreshListener {
            // Fetch posts when the user swipes to refresh
            fetchPosts()
        }

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view_profile)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        profilePostAdapter = ProfilePostAdapter(postList, postViewModel, requireContext())
        recyclerView.adapter = profilePostAdapter

        childFragment.arguments = bundle

        fetchPosts()

        // Add the child fragment to the container
        childFragmentManager.beginTransaction()
            .replace(R.id.navbar_container, childFragment)
            .commit()
        return view
    }

    val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            uploadProfilePicture(it)
        }
    }

    fun openGallery() {
        pickImageLauncher.launch("image/*")
    }
    fun uploadProfilePicture(imageUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val profileImageRef = storageRef.child("profile_pictures/$userId.jpg")

        // Upload the file
        profileImageRef.putFile(imageUri)
            .addOnSuccessListener {
                profileImageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    saveImageUrlToFirestore(downloadUri.toString())  // Save URL to Firestore or Database
                }
                Toast.makeText(requireContext(), "Profile Picture Uploaded!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Upload Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun saveImageUrlToFirestore(imageUrl: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId!!)
            .update("profileImageUrl", imageUrl)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile Updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update profile: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadProfilePicture(profileImage: ImageView) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        val userDao = AppDatabase.getDatabase(requireContext()).userDao()
        val repository = UserRepository(userDao)
        val factory = UserViewModelFactory(repository)
        val userViewModel = ViewModelProvider(this, factory).get(UserViewModel::class.java)

        userViewModel.getUser(userId!!) { user ->
            Log.d("ProfileFragment", "User: ${user.toString()}")
            val imageBlob = user!!.imageBlob
            if (imageBlob != null) {

                // extract user info
                val firstName = user!!.firstname
                val lastName = user!!.lastname
                val email = user!!.email
                val city = user!!.city
                val country = user!!.country
                val bitmap = BitmapFactory.decodeByteArray(imageBlob, 0, imageBlob.size)

                // render user info
                val fullNameTextView = view?.findViewById<TextView>(R.id.fullname_text)
                fullNameTextView?.text = "$firstName $lastName"
                val emailTextView = view?.findViewById<TextView>(R.id.email_text)
                emailTextView?.text = email
                val cityTextView = view?.findViewById<TextView>(R.id.city_text)
                cityTextView?.text = city
                val countryTextView = view?.findViewById<TextView>(R.id.country_text)
                countryTextView?.text = country

                profileImage.setImageBitmap(bitmap)
            }
            else {

                db.collection("users").document(userId!!)
                    .get()
                    .addOnSuccessListener { document ->
                        val profileImageUrl = document.getString("profileImageUrl")
                        val firstName = document.getString("firstname")
                        val lastName = document.getString("lastname")
                        val email = document.getString("email")
                        val city = document.getString("city")
                        val country = document.getString("country")

                        // Update the UI with the fetched data
                        val fullNameTextView = view?.findViewById<TextView>(R.id.fullname_text)
                        fullNameTextView?.text = "$firstName $lastName"
                        val emailTextView = view?.findViewById<TextView>(R.id.email_text)
                        emailTextView?.text = email
                        val cityTextView = view?.findViewById<TextView>(R.id.city_text)
                        cityTextView?.text = city
                        val countryTextView = view?.findViewById<TextView>(R.id.country_text)
                        countryTextView?.text = country

                        if (!profileImageUrl.isNullOrEmpty()) {
                            // Load profile picture from Firebase
                            Picasso.get()
                                .load(profileImageUrl)
                                .placeholder(R.drawable.profile_foreground) // Show default while loading
                                .error(R.drawable.profile_foreground) // Show default if URL is broken
                                .resize(200, 200) // Resize to fit ImageView (optional)
                                .centerCrop()
                                .into(profileImage)
                        } else {
                            // No profile picture found, set default image
                            profileImage.setImageResource(R.drawable.profile_foreground)
                        }
                    }
                    .addOnFailureListener {
                        // If Firestore fetch fails, set default image
                        profileImage.setImageResource(R.drawable.profile_foreground)
                    }
            }
        }

    }
    private fun fetchPosts() {
        // Show the loading spinner while fetching posts
        swipeRefreshLayout.isRefreshing = true

        FirebaseFirestore.getInstance().collection("posts")
            .whereEqualTo("user_id", FirebaseAuth.getInstance().currentUser?.uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                postList.clear()
                for (document in documents) {
                    val post = document.toObject(Post::class.java)
                    postList.add(post)
                }
                profilePostAdapter.notifyDataSetChanged()
                swipeRefreshLayout.isRefreshing = false  // Hide the loading spinner after the data is loaded
            }
            .addOnFailureListener { exception ->
                Log.e("ProfileFragment", "Error fetching posts", exception)
                Toast.makeText(requireContext(), "Failed to load posts", Toast.LENGTH_SHORT).show()
                swipeRefreshLayout.isRefreshing = false  // Hide the loading spinner if there is an error
            }
    }


}