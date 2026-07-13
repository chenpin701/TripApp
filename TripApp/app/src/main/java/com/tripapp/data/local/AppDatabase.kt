package com.tripapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tripapp.data.local.dao.*
import com.tripapp.data.local.entity.*
import com.tripapp.data.security.CryptoManager
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        TripEntity::class, MemberEntity::class, ItineraryItemEntity::class,
        ExpenseEntity::class, VoteProposalEntity::class, ChecklistItemEntity::class,
        AlbumPhotoEntity::class, DocumentEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun itineraryDao(): ItineraryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun voteDao(): VoteDao
    abstract fun checklistDao(): ChecklistDao
    abstract fun mediaDao(): MediaDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                SQLiteDatabase.loadLibs(context) // 初始化 SQLCipher native library

                val crypto = CryptoManager(context.applicationContext)
                val passphrase = crypto.getOrCreateDbPassphrase()
                val factory = SupportFactory(passphrase)

                Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "tripapp_encrypted.db")
                    .openHelperFactory(factory) // 關鍵：資料庫檔案本身用 AES-256 加密
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
