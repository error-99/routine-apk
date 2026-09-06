package com.studentapp.isu14

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.studentapp.isu14.data.database.AppDatabase
import com.studentapp.isu14.data.repository.IsuRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class IsuRoutineApp : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { IsuRepository(database.appDao()) }
    val networkMonitor by lazy { com.studentapp.isu14.util.NetworkMonitor(this) }
    val syncManager by lazy { com.studentapp.isu14.data.firebase.FirebaseSyncManager(database.appDao()) }

    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
        } catch (e: Exception) {
            Log.w("IsuRoutineApp", "FirebaseApp init: ${e.message}")
        }
    }
}

