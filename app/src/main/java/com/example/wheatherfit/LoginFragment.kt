package com.example.wheatherfit

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.wheatherfit.data.local.AppDatabase
import com.example.wheatherfit.data.local.User
import com.example.wheatherfit.data.repository.UserRepository
import com.example.wheatherfit.viewmodel.UserViewModel
import com.example.wheatherfit.viewmodel.UserViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {
    lateinit var auth: FirebaseAuth
    private lateinit var userViewModel: UserViewModel
    val db = FirebaseFirestore.getInstance()

    suspend fun downloadImageAndConvertToByteArray(imageUrl: String): ByteArray? {
        return suspendCancellableCoroutine { continuation ->
            try {
                // Asynchronously load the image using Picasso
                Picasso.get()
                    .load(imageUrl)
                    .into(object : com.squareup.picasso.Target {
                        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                            if (bitmap != null) {
                                // Convert the Bitmap into ByteArray
                                val byteArrayOutputStream = ByteArrayOutputStream()
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                                continuation.resume(byteArrayOutputStream.toByteArray())  // Resume the coroutine with the byte array
                            } else {
                                continuation.resumeWithException(Exception("Bitmap is null"))
                            }
                        }

                        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                            continuation.resumeWithException(e ?: Exception("Failed to load image"))
                        }

                        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                            // You can use this to show a placeholder if needed
                        }
                    })
            } catch (e: Exception) {
                continuation.resumeWithException(e) // Handle the exception if something goes wrong
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = FirebaseAuth.getInstance()

        checkIfLoggedIn()

        val binding = inflater.inflate(R.layout.fragment_login, container, false)

        // when the button is clicked get info from email and password
        val submitButton: Button = binding.findViewById<Button>(R.id.submit);
        val emailInput = binding.findViewById<EditText>(R.id.email);
        val passwordInput = binding.findViewById<EditText>(R.id.password);
        val registerButton = binding.findViewById<Button>(R.id.register_button);

        submitButton.setOnClickListener{

            val email = emailInput.text.toString();
            val password = passwordInput.text.toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Email and Password cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            handleLogin(email, password);

        }

        registerButton.setOnClickListener{
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        return binding;
    }

    private fun handleLogin(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(requireActivity())
        {
            if (it.isSuccessful) {
                Log.d("Login", "signInWithEmail:success")
                Toast.makeText(requireActivity(), "Successfully Logged In", Toast.LENGTH_SHORT).show()

                val userDao = AppDatabase.getDatabase(requireContext()).userDao()
                val repository = UserRepository(userDao)
                val factory = UserViewModelFactory(repository)
                userViewModel = ViewModelProvider(this, factory).get(UserViewModel::class.java)

                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        db.collection("users")
                            .whereEqualTo("email", email)
                            .get()
                            .addOnSuccessListener { documents: QuerySnapshot ->
                                if (!documents.isEmpty) {
                                    val user = documents.documents[0].toObject(User::class.java)

                                    // Check if the user has a profile image URL
                                    val profileImageUrl = user?.profileImageUrl
                                    Log.d("Login", "Image: $profileImageUrl")

                                    if (profileImageUrl != "") {
                                        // Download image as ByteArray in a coroutine
                                        viewLifecycleOwner.lifecycleScope.launch {
                                            Log.d("Login", "Downloading image...")
                                            val imageByteArray = downloadImageAndConvertToByteArray(
                                                profileImageUrl.toString()
                                            )

                                            Log.d("Login", "Image ByteArray: $imageByteArray")
                                            // Proceed to store the user with the image in the Room DB
                                            userViewModel.addUser(
                                                User(
                                                    id = auth.currentUser!!.uid!!,
                                                    email = user!!.email,
                                                    firstname = user!!.firstname,
                                                    lastname = user!!.lastname,
                                                    city = user!!.city,
                                                    country = user!!.country,
                                                    profileImageUrl = user!!.profileImageUrl,
                                                    imageBlob = imageByteArray // Save image as ByteArray
                                                )
                                            )
                                            // Fetch the user from Room and navigate to the home screen
                                            userViewModel.getUser(auth.currentUser!!.uid!!) { currentuser ->
                                                Log.d("Login", currentuser.toString())
                                                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                                            }
                                        }
                                    } else {
                                        // If no profile image URL, just save user without an image
                                        userViewModel.addUser(
                                            User(
                                                id = auth.currentUser!!.uid!!,
                                                email = user!!.email,
                                                firstname = user!!.firstname,
                                                lastname = user!!.lastname,
                                                city = user!!.city,
                                                country = user!!.country,
                                                profileImageUrl = user!!.profileImageUrl,
                                                imageBlob = null // No image
                                            )
                                        )
                                        // Fetch the user from Room and navigate to the home screen
                                        userViewModel.getUser(auth.currentUser!!.uid!!) { currentuser ->
                                            Log.d("Login", currentuser.toString())
                                            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                                        }
                                    }


                                } else {
                                    Log.d("Login", "No user found with email: $email")
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Login", "Error getting user", exception)
                            }
                    } catch (e: Exception) {
                        Log.e("Login", "Error downloading image", e)
                    }
                }

            }
            else {
                Log.w("Login", "signInWithEmail:failure", it.exception)
                Toast.makeText(requireActivity(), "Log In failed ", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkIfLoggedIn() {
        if (auth.currentUser != null) {
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
        }

    }
}