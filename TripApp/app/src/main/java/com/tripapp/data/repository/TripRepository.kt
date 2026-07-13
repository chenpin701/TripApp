package com.tripapp.data.repository

import com.tripapp.data.local.AppDatabase
import com.tripapp.data.local.entity.*
import com.tripapp.data.remote.FirebaseSyncManager
import kotlinx.coroutines.flow.Flow

/**
 * Repository 模式：畫面永遠先讀寫本機加密資料庫（快、離線可用），
 * 寫入成功後再非同步推播到 Firebase；離線時先落地本機，
 * 之後可加入 WorkManager 做背景重試同步（TODO，見 README 待辦）。
 */
class TripRepository(
    private val db: AppDatabase,
    private val sync: FirebaseSyncManager
) {
    fun observeTrips(): Flow<List<TripEntity>> = db.tripDao().observeTrips()
    fun observeMembers(tripId: String): Flow<List<MemberEntity>> = db.tripDao().observeMembers(tripId)
    fun observeItinerary(tripId: String): Flow<List<ItineraryItemEntity>> = db.itineraryDao().observeItems(tripId)
    fun observeExpenses(tripId: String): Flow<List<ExpenseEntity>> = db.expenseDao().observeExpenses(tripId)
    fun observeChecklist(tripId: String): Flow<List<ChecklistItemEntity>> = db.checklistDao().observeItems(tripId)
    fun observeVotes(tripId: String): Flow<List<VoteProposalEntity>> = db.voteDao().observeProposals(tripId)
    fun observePhotos(tripId: String): Flow<List<AlbumPhotoEntity>> = db.mediaDao().observePhotos(tripId)
    fun observeDocuments(tripId: String): Flow<List<DocumentEntity>> = db.mediaDao().observeDocuments(tripId)

    suspend fun createTrip(trip: TripEntity, owner: MemberEntity) {
        db.tripDao().upsert(trip)
        db.tripDao().upsertMember(owner)
        runCatching { sync.pushTrip(trip, listOf(owner)) } // 失敗不擋本機操作，之後可重試
    }

    suspend fun addItineraryItem(item: ItineraryItemEntity) = db.itineraryDao().upsert(item)
    suspend fun voteItineraryItem(itemId: String) = db.itineraryDao().incrementVote(itemId)

    suspend fun addExpense(expense: ExpenseEntity) = db.expenseDao().upsert(expense)

    /** 補齊原本 markPaid() 只是 placeholder 的問題：真正把該行程所有分帳標記為已結算 */
    suspend fun settleAllExpenses(tripId: String) = db.expenseDao().settleAll(tripId)

    suspend fun toggleChecklistItem(itemId: String) = db.checklistDao().toggle(itemId)
    suspend fun toggleVote(proposal: VoteProposalEntity, memberId: String) {
        val voters = proposal.voterMemberIds.split(",").filter { it.isNotBlank() }.toMutableSet()
        if (!voters.remove(memberId)) voters.add(memberId)
        db.voteDao().upsert(proposal.copy(voterMemberIds = voters.joinToString(",")))
    }

    suspend fun togglePhotoLike(photoId: String) = db.mediaDao().toggleLike(photoId)

    /** 上傳文件：先加密再上雲，本機只留 metadata，不落地明文檔案 */
    suspend fun addDocument(tripId: String, docId: String, name: String, mime: String, plainBytes: ByteArray, date: String) {
        val path = sync.uploadEncryptedDocument(tripId, docId, plainBytes, mime)
        db.mediaDao().upsertDocument(DocumentEntity(docId, tripId, name, path, mime, date))
    }
}

/** 計算分帳結果：每個成員該收/該付多少，取代原本只有加總沒有結算邏輯的版本 */
object SplitCalculator {
    data class Balance(val memberId: String, val netTwd: Int) // 正數＝該收錢，負數＝該付錢

    fun calculate(expenses: List<ExpenseEntity>, toTwd: (Double, String) -> Int): List<Balance> {
        val paid = mutableMapOf<String, Int>()
        val owed = mutableMapOf<String, Int>()
        expenses.forEach { exp ->
            val amountTwd = toTwd(exp.amount, exp.currency)
            paid[exp.paidByMemberId] = (paid[exp.paidByMemberId] ?: 0) + amountTwd
            val splitIds = exp.splitMemberIds.split(",").filter { it.isNotBlank() }
            if (splitIds.isEmpty()) return@forEach
            val share = amountTwd / splitIds.size
            splitIds.forEach { id -> owed[id] = (owed[id] ?: 0) + share }
        }
        val allIds = (paid.keys + owed.keys).toSet()
        return allIds.map { id -> Balance(id, (paid[id] ?: 0) - (owed[id] ?: 0)) }
    }
}
