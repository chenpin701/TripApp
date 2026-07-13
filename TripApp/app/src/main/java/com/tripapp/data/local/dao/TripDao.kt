package com.tripapp.data.local.dao

import androidx.room.*
import com.tripapp.data.local.entity.MemberEntity
import com.tripapp.data.local.entity.TripEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trips ORDER BY updatedAt DESC")
    fun observeTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getTrip(tripId: String): TripEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(trip: TripEntity)

    @Delete
    suspend fun delete(trip: TripEntity)

    @Query("SELECT * FROM members WHERE tripId = :tripId")
    fun observeMembers(tripId: String): Flow<List<MemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMember(member: MemberEntity)

    @Query("DELETE FROM members WHERE id = :memberId")
    suspend fun removeMember(memberId: String)
}
