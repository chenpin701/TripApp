package com.tripapp.ui.screens.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripapp.data.local.entity.*
import com.tripapp.data.repository.SplitCalculator
import com.tripapp.data.repository.TripRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// 與原本 HTML 版一致的固定匯率換算表
val RATES = mapOf("JPY" to 0.22, "USD" to 31.5, "EUR" to 34.2, "TWD" to 1.0, "KRW" to 0.024, "GBP" to 40.1, "HKD" to 4.05, "SGD" to 23.4)
fun toTwd(amount: Double, currency: String): Int = Math.round(amount * (RATES[currency] ?: 1.0)).toInt()

class TripDetailViewModel(
    private val repository: TripRepository,
    private val tripId: String
) : ViewModel() {

    val members: StateFlow<List<MemberEntity>> = repository.observeMembers(tripId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val itinerary: StateFlow<List<ItineraryItemEntity>> = repository.observeItinerary(tripId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseEntity>> = repository.observeExpenses(tripId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val balances: StateFlow<List<SplitCalculator.Balance>> = expenses
        .combine(members) { exps, _ -> SplitCalculator.calculate(exps, ::toTwd) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val checklist: StateFlow<List<ChecklistItemEntity>> = repository.observeChecklist(tripId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val votes: StateFlow<List<VoteProposalEntity>> = repository.observeVotes(tripId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val photos: StateFlow<List<AlbumPhotoEntity>> = repository.observePhotos(tripId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val documents: StateFlow<List<DocumentEntity>> = repository.observeDocuments(tripId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun voteItinerary(itemId: String) = viewModelScope.launch { repository.voteItineraryItem(itemId) }

    fun addExpense(name: String, amount: Double, currency: String, paidBy: String, splitIds: List<String>, category: String, date: String) {
        viewModelScope.launch {
            repository.addExpense(
                ExpenseEntity(
                    id = "e${System.currentTimeMillis()}", tripId = tripId, name = name, amount = amount,
                    currency = currency, paidByMemberId = paidBy, splitMemberIds = splitIds.joinToString(","),
                    category = category, date = date
                )
            )
        }
    }

    /** 真正結算：把這趟旅程所有分帳標記為已結清（取代原本的 placeholder） */
    fun settleAll() = viewModelScope.launch { repository.settleAllExpenses(tripId) }

    fun toggleChecklist(itemId: String) = viewModelScope.launch { repository.toggleChecklistItem(itemId) }

    fun toggleVote(proposal: VoteProposalEntity, memberId: String) =
        viewModelScope.launch { repository.toggleVote(proposal, memberId) }

    fun toggleLike(photoId: String) = viewModelScope.launch { repository.togglePhotoLike(photoId) }

    fun uploadDocument(name: String, mime: String, bytes: ByteArray, date: String) {
        viewModelScope.launch {
            repository.addDocument(tripId, "d${System.currentTimeMillis()}", name, mime, bytes, date)
        }
    }
}
