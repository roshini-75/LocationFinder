package com.example.createfile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //val intent = Intent(ACTION_PROCESS_UPDATES)
// Add any additional data if needed
        //sendBroadcast(intent)

        // Start the service to request location updates
        val serviceIntent = Intent(this, LocationUpdatesService::class.java)
        serviceIntent.action = LocationUpdatesService.ACTION_PROCESS_UPDATES
        startService(serviceIntent)
    }
}