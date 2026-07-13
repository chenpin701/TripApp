package com.tripapp.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.tripapp.data.local.entity.*
import com.tripapp.data.security.CryptoManager
import kotlinx.coroutines.tasks.await

/**
 * 雲端同步 + 存取權限：
 * - Firestore 規則（見 firestore.rules）限制「只有 members 陣列裡含自己 uid 的行程」能被讀寫，
 *   不是靠 App 端隱藏 UI，而是伺服器端強制驗證，就算有心人繞過 App 直接呼叫 API 也拿不到別團資料。
 * - 敏感文件先用 CryptoManager 加密成密文，再上傳 Storage，Storage 規則同樣鎖定行程成員。
 */
class FirebaseSyncManager(private val crypto: CryptoManager) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    val currentUid: String? get() = auth.currentUser?.uid

    suspend fun signInWithEmailLink(email: String, link: String) {
        auth.signInWithEmailLink(email, link).await()
    }

    /** 推送單一行程資料到雲端（其餘 sub-collection 同理，這裡示範 trip + members） */
    suspend fun pushTrip(trip: TripEntity, members: List<MemberEntity>) {
        val uid = currentUid ?: error("尚未登入")
        val tripRef = db.collection("trips").document(trip.id)
        tripRef.set(
            mapOf(
                "name" to trip.name,
                "dest" to trip.dest,
                "startDate" to trip.startDate,
                "endDate" to trip.endDate,
                "status" to trip.status,
                "ownerUid" to trip.ownerUid,
                "memberUids" to members.mapNotNull { it.uid }, // Firestore 規則靠這欄位比對權限
                "updatedAt" to trip.updatedAt
            )
        ).await()
        require(members.any { it.uid == uid } || trip.ownerUid == uid) { "非行程成員" }
    }

    /** 上傳前先加密，雲端只存密文 */
    suspend fun uploadEncryptedDocument(tripId: String, docId: String, plainBytes: ByteArray, mime: String): String {
        val encrypted = crypto.encryptFile(tripId, plainBytes)
        val path = "trips/$tripId/documents/$docId.enc"
        storage.reference.child(path).putBytes(encrypted).await()
        return path
    }

    suspend fun downloadAndDecryptDocument(tripId: String, storagePath: String): ByteArray {
        val bytes = storage.reference.child(storagePath).getBytes(20L * 1024 * 1024).await()
        return crypto.decryptFile(tripId, bytes)
    }

    fun signOut() = auth.signOut()
}
