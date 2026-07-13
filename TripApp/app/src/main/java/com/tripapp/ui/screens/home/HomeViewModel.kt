package com.tripapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripapp.data.local.entity.MemberEntity
import com.tripapp.data.local.entity.TripEntity
import com.tripapp.data.repository.TripRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: TripRepository) : ViewModel() {

    val trips: StateFlow<List<TripEntity>> = repository.observeTrips()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createTrip(name: String, dest: String, dates: String, cover: String, ownerUid: String) {
        viewModelScope.launch {
            val tripId = "t${System.currentTimeMillis()}"
            val trip = TripEntity(
                id = tripId, name = name, cover = cover, dest = dest,
                startDate = dates, endDate = dates, status = "upcoming", ownerUid = ownerUid
            )
            val owner = MemberEntity(
                id = "u${System.currentTimeMillis()}", tripId = tripId, uid = ownerUid,
                name = "我", avatar = "我", colorHex = "#1A6B8A"
            )
            repository.createTrip(trip, owner)
        }
    }
}
