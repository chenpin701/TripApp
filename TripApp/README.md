# 旅遊行程 App — Android 原生版（Kotlin + Jetpack Compose）

從原本單一 HTML Demo 轉換成的原生 Android 專案骨架，架構為 **MVVM + Repository**，
本機資料庫全程加密，並加入 Firebase 雲端同步，讓親友多人共用同一份行程資料。

## 專案結構

```
app/src/main/java/com/tripapp/
├─ TravelApplication.kt          App 入口，初始化資料庫/加密/同步單例
├─ MainActivity.kt               生物辨識解鎖 + Compose Navigation 掛載點
├─ data/
│  ├─ local/
│  │  ├─ entity/Entities.kt      Room 資料表（對應原本 INIT_TRIPS/ITIN/EXPS…）
│  │  ├─ dao/                    Room DAO
│  │  └─ AppDatabase.kt          SQLCipher 加密資料庫
│  ├─ security/
│  │  ├─ CryptoManager.kt        Android Keystore 保護的密碼/檔案加密
│  │  └─ BiometricAuthHelper.kt  生物辨識解鎖
│  ├─ remote/FirebaseSyncManager.kt   Firebase Auth / Firestore / Storage
│  └─ repository/TripRepository.kt    本機優先、雲端同步、分帳結算邏輯
├─ ui/
│  ├─ theme/Theme.kt
│  ├─ navigation/NavGraph.kt
│  └─ screens/
│     ├─ home/                   行程列表 + 新增行程（完整實作）
│     └─ trip/                   行程詳情六分頁（完整實作）
└─ firebase/firestore.rules, storage.rules   伺服器端權限規則
```

## 資料安全設計（對應「注重資料安全」的需求）

| 風險 | 對策 |
|---|---|
| 手機遺失，資料被直接翻閱 | App 啟動要求生物辨識（`BiometricAuthHelper`） |
| App 資料夾被讀出（root/備份） | Room 資料庫用 SQLCipher AES-256 全庫加密；`allowBackup="false"` 避免明文備份 |
| 資料庫密碼寫死在程式碼被反編譯看到 | 密碼隨機產生，透過 Android Keystore 保護的 `EncryptedSharedPreferences` 存放，金鑰不離開硬體安全區 |
| 護照/證件掃描檔外洩 | 上傳雲端前用每趟行程專屬的 AES-256-GCM 金鑰加密，雲端只存密文 |
| 別團行程被偷看 | Firestore/Storage 安全規則在**伺服器端**強制比對 `memberUids`，不是只靠 App 端隱藏 UI |
| 傳輸過程被竊聽 | `network_security_config.xml` 禁止明文 HTTP，全程 TLS |

## 已補完的功能缺口（相較原本 HTML 版）

- **分帳結算**：原本 `markPaid()` 只是 placeholder。現在 `SplitCalculator` 會依「誰付了多少、該由誰分攤」算出每人淨額（應收/應付），`settleAll()` 真的會把紀錄標記已結清。
- **多人即時同步**：原本 localStorage 各手機各自一份，現在 Firestore 讓行程成員看到同一份資料（含權限控管）。
- **文件/相簿雲端化**：原本 base64 塞進 localStorage 容量很快爆掉；現在改存 Firebase Storage，本機只留 metadata 與縮圖快取。

## 開發環境設定步驟

1. 用 Android Studio 開啟本專案資料夾
2. 到 [Firebase Console](https://console.firebase.google.com) 建立專案，開啟 Authentication（建議用 Email 連結或 Google 登入）、Firestore、Storage
3. 下載 `google-services.json`，放到 `app/` 資料夾下（此檔含金鑰，不要上傳公開 repo）
4. 把 `firebase/firestore.rules`、`firebase/storage.rules` 部署到 Firebase（Firebase Console → Firestore/Storage → 規則，貼上即可）
5. Sync Gradle，直接 Run

## 待你依實際需求延伸（TODO）

- **邀請機制**：目前新增成員只存名字，尚未串接「邀請連結 → 對方登入 → 綁定 uid」的流程
- **離線重試同步**：`TripRepository` 目前雲端寫入失敗只是靜默略過，建議接 `WorkManager` 做背景重試
- **相片本機快取管理**：`AlbumPhotoEntity.localCachePath` 目前只是欄位保留，需加上下載/清快取邏輯
- 投票、清單、文件分頁的新增/刪除 UI 目前只做了顯示與核心互動（投票、打勾、按讚），新增表單可仿照「分帳」「新增行程」的 Dialog 模式補上
