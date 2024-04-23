package com.example.createfile

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import java.io.IOException
import java.io.OutputStreamWriter

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocationUpdatesBroadcastReceiver : BroadcastReceiver() {

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var lastLocation: Location? = null
    private val TAG = "LocationReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        if (LocationResult.hasResult(intent)) {
            val locationResult = LocationResult.extractResult(intent)
            if (locationResult != null) {
                lastLocation = locationResult.lastLocation
            } else {
                Log.e(TAG, "No location result found in intent")
            }
        } else {
            Log.e(TAG, "No location result found in intent")
        }

        if (intent != null) {
            Log.d(TAG, "Received broadcast intent with action: ${intent.action}")
        } else {
            Log.e(TAG, "Received null intent")
        }
    }

    fun scheduleLocationUpdates(context: Context) {
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date(System.currentTimeMillis()))

                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.e(TAG, "Location permissions not granted")
                    return
                }

                val fusedLocationProviderClient =
                    LocationServices.getFusedLocationProviderClient(context)
                val locationRequest = LocationRequest.create().apply {
                    interval = 0 // Request location updates immediately
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }

                fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult?) {
                            super.onLocationResult(locationResult)
                            if (locationResult != null) {
                                lastLocation = locationResult.lastLocation
                                val logMessage =
                                    "Timestamp: $timestamp, Latitude: ${lastLocation?.latitude}, Longitude: ${lastLocation?.longitude}"
                                Log.d(TAG, logMessage)

                                // Write log message to file here
                                writeFile(context, logMessage)
                            } else {
                                Log.e(TAG, "No location result found")
                            }
                        }
                    },
                    Looper.getMainLooper()
                )

                handler.postDelayed(this, 900000) // Schedule the next update after 15 minutes
            }
        }

        // Start scheduling updates immediately
        handler.post(runnable)
    }
    private fun writeFile(context: Context, logMessage: String) {
        try {
            val fileName = "location_log.txt"
            val fileOutputStream = context.openFileOutput(fileName, Context.MODE_APPEND)
            val outputStreamWriter = OutputStreamWriter(fileOutputStream)
            outputStreamWriter.write("$logMessage\n")
            outputStreamWriter.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error writing to file: ${e.message}")
        }
    }

}
