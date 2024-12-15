package com.example.mytaximeter

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*
import kotlin.math.roundToInt

class TrackingService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null
    private var totalDistance = 0.0
    private val baseFare = 5.0
    private val farePerKilometer = 2.0
    private val farePerMinute = 0.5
    private var startTime: Long = 0L

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startTime = SystemClock.elapsedRealtime()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            try {
                if (!hasLocationPermission()) {
                    Log.e("TrackingService", "Permissions are missing. Stopping service.")
                    stopSelf()
                    return@launch
                }

                if (ActivityCompat.checkSelfPermission(
                        this@TrackingService,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this@TrackingService,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.e("TrackingService", "Permissions are missing. Requesting permissions.")
                    stopSelf() // Stop service as permissions are not granted
                    return@launch
                }

                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    lastLocation = location
                }

                while (isActive) {
                    delay(1000)

                    if (!hasLocationPermission()) {
                        Log.e("TrackingService", "Permissions lost. Stopping service.")
                        stopSelf()
                        return@launch
                    }

                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            if (lastLocation != null) {
                                val distance = lastLocation!!.distanceTo(location)
                                if (distance >= 10) {
                                    totalDistance += distance / 1000.0
                                }
                            }
                            lastLocation = location
                        }
                    }

                    val elapsedMinutes = (SystemClock.elapsedRealtime() - startTime) / 60000
                    val totalFare = baseFare + (totalDistance * farePerKilometer) + (elapsedMinutes * farePerMinute)

                    updateNotification(elapsedMinutes, totalFare)
                }
            } catch (e: Exception) {
                Log.e("TrackingService", "Error: ${e.message}")
                stopSelf()
            }
        }

        return START_STICKY
    }


    private fun updateNotification(elapsedMinutes: Long, totalFare: Double) {
        val notification = NotificationCompat.Builder(this, "tracking_channel")
            .setContentTitle("Trip in Progress")
            .setContentText(
                "Distance: ${totalDistance.roundToInt()} km\n" +
                        "Time: $elapsedMinutes min\n" +
                        "Fare: ${totalFare.roundToInt()} currency"
            )
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()

        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(1, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "tracking_channel",
            "Tracking Service",
            NotificationManager.IMPORTANCE_LOW
        )
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}