package com.tripapp.data.local.dao

import androidx.room.*
import com.tripapp.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ItineraryDao {
    @Query("SELECT * FROM itinerary_items WHERE tripId = :tripId ORDER BY day, time")
    fun observeItems(tripId: String): Flow<List<ItineraryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ItineraryItemEntity)

    @Query("UPDATE itinerary_items SET votes = votes + 1 WHERE id = :itemId")
    suspend fun incrementVote(itemId: String)

    @Delete
    suspend fun delete(item: ItineraryItemEntity)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE tripId = :tripId ORDER BY date DESC")
    fun observeExpenses(tripId: String): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(expense: ExpenseEntity)

    @Query("UPDATE expenses SET isSettled = 1 WHERE tripId = :tripId")
    suspend fun settleAll(tripId: String)

    @Delete
    suspend fun delete(expense: ExpenseEntity)
}

@Dao
interface VoteDao {
    @Query("SELECT * FROM vote_proposals WHERE tripId = :tripId")
    fun observeProposals(tripId: String): Flow<List<VoteProposalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(proposal: VoteProposalEntity)
}

@Dao
interface ChecklistDao {
    @Query("SELECT * FROM checklist_items WHERE tripId = :tripId")
    fun observeItems(tripId: String): Flow<List<ChecklistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ChecklistItemEntity)

    @Query("UPDATE checklist_items SET isDone = NOT isDone WHERE id = :itemId")
    suspend fun toggle(itemId: String)
}

@Dao
interface MediaDao {
    @Query("SELECT * FROM album_photos WHERE tripId = :tripId ORDER BY day")
    fun observePhotos(tripId: String): Flow<List<AlbumPhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPhoto(photo: AlbumPhotoEntity)

    @Query("UPDATE album_photos SET liked = NOT liked WHERE id = :photoId")
    suspend fun toggleLike(photoId: String)

    @Query("SELECT * FROM documents WHERE tripId = :tripId ORDER BY date DESC")
    fun observeDocuments(tripId: String): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDocument(doc: DocumentEntity)

    @Delete
    suspend fun deleteDocument(doc: DocumentEntity)
}
