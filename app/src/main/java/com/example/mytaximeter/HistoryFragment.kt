package com.example.mytaximeter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tripsList: MutableList<Trip> // List of trips
    private lateinit var adapter: TripAdapter

    // Firestore instance
    private val db = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_history, container, false)

        recyclerView = rootView.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        tripsList = mutableListOf() // Initially, the list is empty
        adapter = TripAdapter(tripsList)
        recyclerView.adapter = adapter

        // Fetch trips from Firestore
        loadTrips()

        return rootView
    }

    private fun loadTrips() {
        user?.let {
            db.collection("users")
                .document(it.uid)
                .collection("trips")
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val trip = document.toObject(Trip::class.java)
                        tripsList.add(trip) // Add trip to the list
                    }
                    adapter.notifyDataSetChanged() // Update the adapter to display the new data
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Failed to fetch data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
