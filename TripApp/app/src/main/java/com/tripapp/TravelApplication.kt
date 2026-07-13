package com.tripapp

import android.app.Application
import com.tripapp.data.local.AppDatabase
import com.tripapp.data.remote.FirebaseSyncManager
import com.tripapp.data.repository.TripRepository
import com.tripapp.data.security.CryptoManager

class TravelApplication : Application() {

    lateinit var repository: TripRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val crypto = CryptoManager(this)
        val db = AppDatabase.getInstance(this)
        val sync = FirebaseSyncManager(crypto)
        repository = TripRepository(db, sync)
    }
}
