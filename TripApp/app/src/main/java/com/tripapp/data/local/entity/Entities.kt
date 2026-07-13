package com.tripapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 行程（對應原 INIT_TRIPS） */
@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey val id: String,
    val name: String,
    val cover: String,
    val dest: String,
    val startDate: String,
    val endDate: String,
    val status: String, // upcoming / past
    val ownerUid: String, // Firebase Auth uid，用來做雲端權限比對
    val updatedAt: Long = System.currentTimeMillis()
)

/** 行程成員 */
@Entity(tableName = "members")
data class MemberEntity(
    @PrimaryKey val id: String,
    val tripId: String,
    val uid: String?, // 對應 Firebase 帳號；null 代表尚未綁定帳號的臨時成員
    val name: String,
    val avatar: String,
    val colorHex: String
)

/** 每日行程項目（對應 INIT_ITIN） */
@Entity(tableName = "itinerary_items")
data class ItineraryItemEntity(
    @PrimaryKey val id: String,
    val tripId: String,
    val day: Int,
    val date: String,
    val dayTitle: String,
    val time: String,
    val name: String,
    val type: String, // transport / spot / food / activity
    val note: String,
    val votes: Int
)

/** 分帳紀錄（對應 INIT_EXPS） */
@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val tripId: String,
    val name: String,
    val amount: Double,
    val currency: String,
    val paidByMemberId: String,
    val splitMemberIds: String, // 以逗號分隔存 member id，讀取時再 split
    val category: String,
    val date: String,
    val isSettled: Boolean = false // 補齊原本只是 placeholder 的結算功能
)

/** 景點提案投票（對應 INIT_VOTES） */
@Entity(tableName = "vote_proposals")
data class VoteProposalEntity(
    @PrimaryKey val id: String,
    val tripId: String,
    val name: String,
    val type: String,
    val emoji: String,
    val note: String,
    val voterMemberIds: String // 逗號分隔
)

/** 打包/待辦清單（對應 INIT_CHECKS） */
@Entity(tableName = "checklist_items")
data class ChecklistItemEntity(
    @PrimaryKey val id: String,
    val tripId: String,
    val name: String,
    val isDone: Boolean,
    val owner: String // "p" 個人 / "g" 團體
)

/** 相簿照片（對應 INIT_ALBUMS）— storagePath 存 Firebase Storage 路徑，本機只快取縮圖 */
@Entity(tableName = "album_photos")
data class AlbumPhotoEntity(
    @PrimaryKey val id: String,
    val tripId: String,
    val storagePath: String,
    val localCachePath: String?,
    val caption: String,
    val day: Int,
    val liked: Boolean
)

/** 文件（護照、訂房確認信等，對應 INIT_DOCS）— 上傳前會經過加密，見 CryptoManager */
@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val tripId: String,
    val name: String,
    val storagePath: String, // Firebase Storage 上的密文路徑
    val mimeType: String,
    val date: String
)
