package com.example.wheatherfit

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.wheatherfit.viewmodel.WeatherViewModel
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

class UploadFragment : Fragment() {
    private val weatherViewModel: WeatherViewModel by viewModels()
    private var selectedImageUri: Uri? = null  // Store selected image URI
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it  // Save URI for later upload
            Toast.makeText(requireContext(), "Image selected", Toast.LENGTH_SHORT).show()
        }
    }

    fun uploadPostPicture(imageUri: Uri, description: String, weather: Float) {
        val storageRef = FirebaseStorage.getInstance().reference
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val postImageRef = storageRef.child("outfits/$userId-${System.currentTimeMillis()}.jpg")

        postImageRef.putFile(imageUri)
            .addOnSuccessListener {
                postImageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    saveImageUrlToFirestore(downloadUri.toString(), description, weather)
                }
                Toast.makeText(requireContext(), "Post Uploaded!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_uploadFragment_to_homeFragment)

            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Upload Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun saveImageUrlToFirestore(imageUrl: String, description: String, weather: Float) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        val post = hashMapOf(
            "user_id" to userId,
            "image_url" to imageUrl,
            "description" to description,
            "timestamp" to System.currentTimeMillis(),
            "rating_count" to 0,
            "rating_sum" to 0.0,
            "rating_users_ids" to emptyList<String>(),
            "weather" to weather
        )

        db.collection("posts").add(post)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Post Shared!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to share post: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    fun handleUpload(currentTemp: Float) {

        val etDescription = view?.findViewById<TextInputLayout>(R.id.description)
        val etUploadButton = view?.findViewById<Button>(R.id.upload_button)
        val shareButton = view?.findViewById<Button>(R.id.share)

        etUploadButton?.setOnClickListener {
            openGallery()  // Only selects an image, does NOT upload
        }

        shareButton?.setOnClickListener {
            val description = etDescription?.editText?.text.toString()
            if (selectedImageUri != null) {
                uploadPostPicture(selectedImageUri!!, description, currentTemp)  // Upload image only when sharing
            } else {
                Toast.makeText(requireContext(), "Please select an image first", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_upload, container, false)

        val childFragment = BottomNavFragment()
        val bundle = Bundle()
        bundle.putString("current_page", "upload")

        childFragment.arguments = bundle

        // Add the child fragment to the container
        childFragmentManager.beginTransaction()
            .replace(R.id.navbar_container, childFragment)
            .commit()

        db.collection("users")
            .document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { user ->
                val city = user.getString("city")
                val country = user.getString("country")
                val cityAndCountry = "$city, $country"

                val apiKey = "7f21c93531c74c408f893537253101"
                val weatherText = view.findViewById<TextView>(R.id.weather)

                viewLifecycleOwner.lifecycleScope.launch {
                    val weather = weatherViewModel.fetchWeather(apiKey, cityAndCountry)
                    val currentTemp = weather?.current?.temp_c?.toFloat() ?: 0f // Get the temperature and convert to float

                    weatherText.text = "Temperature: $currentTemp°C"

                    handleUpload(currentTemp)

                }

            }


        return view
    }

}