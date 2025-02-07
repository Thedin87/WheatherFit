package com.example.wheatherfit

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

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

        childFragment.arguments = bundle

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

        db.collection("users").document(userId!!)
            .get()
            .addOnSuccessListener { document ->
                val profileImageUrl = document.getString("profileImageUrl")

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