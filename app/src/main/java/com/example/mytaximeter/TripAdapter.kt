package com.example.mytaximeter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class TripAdapter(private val trips: List<Trip>) : RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trip, parent, false)
        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val trip = trips[position]

        // Format the date with time (Hour:Minute:Second)
        val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(trip.date))

        holder.distanceText.text = "Distance: ${trip.distance.roundToInt()} meters"
        holder.durationText.text = "Duration: ${trip.duration} minutes"
        holder.fareText.text = "Fare: ${trip.fare.roundToInt()} MAD"
        holder.dateText.text = "Date: $formattedDate" // Adding date with time
    }

    override fun getItemCount(): Int {
        return trips.size
    }

    // Internal ViewHolder to define the elements
    inner class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val distanceText: TextView = itemView.findViewById(R.id.distanceText)
        val durationText: TextView = itemView.findViewById(R.id.durationText)
        val fareText: TextView = itemView.findViewById(R.id.fareText)
        val dateText: TextView = itemView.findViewById(R.id.dateText)
    }
}
