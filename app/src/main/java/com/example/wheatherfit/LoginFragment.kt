package com.example.wheatherfit

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.navigation.fragment.findNavController

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
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = inflater.inflate(R.layout.fragment_login, container, false)

        // when the button is clicked get info from email and password
        val button: Button = binding.findViewById<Button>(R.id.submit);
        val emailInput = binding.findViewById<EditText>(R.id.email);
        val passwordInput = binding.findViewById<EditText>(R.id.password);

        button.setOnClickListener{

            val email = emailInput.text.toString();
            val password = passwordInput.text.toString();
            handleLogin(email, password);

        }
        return binding;
    }

    fun handleLogin(email: String, password: String) {

        Log.d("Login", "Email entered: $email\nPassword entered: $password")
        findNavController().navigate(R.id.action_loginFragment_to_homeFragment) // Din do this only if auth succeeds

    }
}