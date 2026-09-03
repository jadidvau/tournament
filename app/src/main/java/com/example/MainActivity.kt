package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    try {
      if (com.google.firebase.FirebaseApp.getApps(this).isEmpty()) {
        try {
          com.google.firebase.FirebaseApp.initializeApp(this)
        } catch (ignored: Throwable) {
          TournamentApplication.initFirebase(this)
        }
      }
    } catch (e: Throwable) {
      android.util.Log.w("MainActivity", "Firebase initialization check in MainActivity: ${e.message}")
    }
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        AppNavigation()
      }
    }
  }
}

