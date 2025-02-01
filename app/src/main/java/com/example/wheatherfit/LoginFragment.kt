package com.example.wheatherfit

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = inflater.inflate(R.layout.fragment_login, container, false)

        // when the button is clicked get info from email and password
        val submitButton: Button = binding.findViewById<Button>(R.id.submit);
        val emailInput = binding.findViewById<EditText>(R.id.email);
        val passwordInput = binding.findViewById<EditText>(R.id.password);
        val registerButton = binding.findViewById<Button>(R.id.register_button);

        auth = FirebaseAuth.getInstance()

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
                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            }
            else {
                Log.w("Login", "signInWithEmail:failure", it.exception)
                Toast.makeText(requireActivity(), "Log In failed ", Toast.LENGTH_SHORT).show()
            }
        }
    }
}