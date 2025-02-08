package com.example.wheatherfit

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.wheatherfit.adapters.PostAdapter
import com.example.wheatherfit.data.local.AppDatabase
import com.example.wheatherfit.data.local.User
import com.example.wheatherfit.data.models.Post
import com.example.wheatherfit.data.repository.PostRepository
import com.example.wheatherfit.data.repository.UserRepository
import com.example.wheatherfit.viewmodel.PostViewModel
import com.example.wheatherfit.viewmodel.UserViewModel
import com.example.wheatherfit.viewmodel.UserViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeFragment : Fragment() {
    private lateinit var userViewModel: UserViewModel
    private lateinit var recyclerView: RecyclerView
    private var postList = mutableListOf<Post>()
    private lateinit var postAdapter: PostAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    val postViewModel = PostViewModel()

    fun renderNav(user: User) {

        val childFragment = BottomNavFragment()
        val bundle = Bundle()
        bundle.putString("current_page", "home")
        bundle.putString("firstname", user.firstname)

        childFragment.arguments = bundle

        // Add the child fragment to the container
        childFragmentManager.beginTransaction()
            .replace(R.id.navbar_container, childFragment)
            .commit()

    }

    private fun fetchPosts() {
        // Show the loading spinner while fetching posts
        swipeRefreshLayout.isRefreshing = true

        FirebaseFirestore.getInstance().collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                postList.clear()
                for (document in documents) {
                    val post = document.toObject(Post::class.java)
                    postList.add(post)
                }
                postAdapter.notifyDataSetChanged()
                swipeRefreshLayout.isRefreshing = false  // Hide the loading spinner after the data is loaded
            }
            .addOnFailureListener { exception ->
                Log.e("HomeFragment", "Error fetching posts", exception)
                Toast.makeText(requireContext(), "Failed to load posts", Toast.LENGTH_SHORT).show()
                swipeRefreshLayout.isRefreshing = false  // Hide the loading spinner if there is an error
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Set up SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            // Fetch posts when the user swipes to refresh
            fetchPosts()
        }

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        postAdapter = PostAdapter(postList, postViewModel, requireContext())
        recyclerView.adapter = postAdapter

        val userDao = AppDatabase.getDatabase(requireContext()).userDao()
        val repository = UserRepository(userDao)
        val factory = UserViewModelFactory(repository)
        userViewModel = ViewModelProvider(this, factory).get(UserViewModel::class.java)

        val users = userViewModel.getUsers(
            callback = {
                users ->
                    run {
                        Log.d("Home", users.toString())
                        if (users.isEmpty()) {
                            Toast.makeText(requireActivity(), "Not logged in", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_homeFragment_to_logoutFragment)
                        }
                        else {

                            renderNav(users[0])

                        }
                    }
            }
        );

        // Fetch posts from Firebase
        fetchPosts()

        return view
    }

}