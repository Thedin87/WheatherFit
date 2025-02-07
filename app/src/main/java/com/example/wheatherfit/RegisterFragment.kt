package com.example.wheatherfit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RegisterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegisterFragment : Fragment() {
    lateinit var etEmail: EditText
    lateinit var etFirstNmae: EditText
    lateinit var etLastName: EditText
    lateinit var etCity: EditText
    lateinit var etCountry: EditText
    private lateinit var etPass: EditText
    private lateinit var btnSignUp: Button
    lateinit var tvRedirectLogin: TextView

    // create Firebase authentication object
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = inflater.inflate(R.layout.fragment_register, container, false);

        // View Bindings
        etFirstNmae = binding.findViewById(R.id.firstname_field)
        etLastName = binding.findViewById(R.id.lastname_field)
        etEmail = binding.findViewById(R.id.email_field)
        etPass = binding.findViewById(R.id.password_field)
        etCity = binding.findViewById(R.id.city_field)
        etCountry = binding.findViewById(R.id.country_field)
        btnSignUp = binding.findViewById(R.id.register_submit_button)
        tvRedirectLogin = binding.findViewById(R.id.register_login_button)

        // Initialising auth object
        auth = Firebase.auth

        btnSignUp.setOnClickListener {
            signUpUser()
        }

        // switching from signUp Activity to Login Activity
        tvRedirectLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
        return binding
    }

    private fun signUpUser() {
        val firstName = etFirstNmae.text.toString()
        val lastName = etLastName.text.toString()
        val email = etEmail.text.toString()
        val pass = etPass.text.toString()
        val city = etCity.text.toString()
        val country = etCountry.text.toString()
        val db = FirebaseFirestore.getInstance()

        // check pass
        if (firstName.isBlank() || lastName.isBlank() ||email.isBlank() || pass.isBlank() || city.isBlank() || country.isBlank()) {
            Toast.makeText(requireContext(), "Email and Password can't be blank", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener() {
            if (it.isSuccessful) {
                val user = auth.currentUser
                Log.d("Register", user?.uid.toString())
                if (user != null) {
                    saveUserToFirestore(user)
                    db.collection("users").document(user?.uid!!)
                        .update("firstname", firstName)
                    db.collection("users").document(user?.uid!!)
                        .update("lastname", lastName)
                    db.collection("users").document(user?.uid!!)
                        .update("city", city)
                    db.collection("users").document(user?.uid!!)
                        .update("country", country)
                }
                Toast.makeText(requireContext(), "Successfully Singed Up", Toast.LENGTH_SHORT).show()
                requireActivity().finish()
            } else {
                Log.w("Register", "createUserWithEmail:failure", it.exception)
                Toast.makeText(requireContext(), "Sing Up Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun saveUserToFirestore(user: FirebaseUser) {
        val db = FirebaseFirestore.getInstance()
        val userData = hashMapOf(
            "name" to (user.displayName ?: "New User"),
            "email" to user.email,
            "profileImageUrl" to "", // Empty until user uploads a profile picture
            "phone" to "",
            "role" to "user" // Can be "admin" if needed
        )

        db.collection("users")
            .document(user.uid) // Use UID as document ID
            .set(userData)
            .addOnSuccessListener {
                Log.d("Firestore", "User data saved successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error saving user data", e)
            }
    }

}