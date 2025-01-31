package com.example.wheatherfit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class UploadFragment : Fragment() {

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
        return view
    }

}