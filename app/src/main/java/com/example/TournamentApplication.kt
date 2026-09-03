package com.example

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class TournamentApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        initFirebase(this)
    }

    companion object {
        var instance: TournamentApplication? = null
            private set

        fun initFirebase(context: Context) {
            try {
                if (FirebaseApp.getApps(context).isEmpty()) {
                    val apiKey = BuildConfig.VITE_FIREBASE_API_KEY
                    val appId = BuildConfig.VITE_FIREBASE_APP_ID
                    val projectId = BuildConfig.VITE_FIREBASE_PROJECT_ID
                    val storageBucket = BuildConfig.VITE_FIREBASE_STORAGE_BUCKET
                    val gcmSenderId = BuildConfig.VITE_FIREBASE_MESSAGING_SENDER_ID

                    if (apiKey.isNotBlank() && appId.isNotBlank() && projectId.isNotBlank()) {
                        try {
                            val options = FirebaseOptions.Builder()
                                .setApiKey(apiKey)
                                .setApplicationId(appId)
                                .setProjectId(projectId)
                                .apply {
                                    if (storageBucket.isNotBlank()) {
                                        setStorageBucket(storageBucket)
                                    }
                                    if (gcmSenderId.isNotBlank()) {
                                        setGcmSenderId(gcmSenderId)
                                    }
                                }
                                .build()
                            FirebaseApp.initializeApp(context.applicationContext, options)
                            Log.i("TournamentApplication", "FirebaseApp initialized with project: $projectId")
                        } catch (e: Throwable) {
                            Log.w("TournamentApplication", "Options initialization failed, trying default: ${e.message}")
                            FirebaseApp.initializeApp(context.applicationContext)
                        }
                    } else {
                        FirebaseApp.initializeApp(context.applicationContext)
                        Log.i("TournamentApplication", "FirebaseApp initialized with default resources")
                    }
                }
            } catch (e: Throwable) {
                Log.w("TournamentApplication", "FirebaseApp initialization notice: ${e.message}")
            }
        }
    }
}
