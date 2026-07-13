package com.tripapp

import android.os.Bundle
import androidx.activity.compose.setContent
import com.google.firebase.auth.FirebaseAuth
import com.tripapp.data.security.BiometricAuthHelper
import com.tripapp.ui.navigation.TripAppNavGraph
import com.tripapp.ui.theme.TripAppTheme

class MainActivity : androidx.fragment.app.FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as TravelApplication
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "local-demo-uid"

        setContent {
            TripAppTheme {
                var unlocked by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(!BiometricAuthHelper.isAvailable(this)) }

                if (unlocked) {
                    TripAppNavGraph(repository = app.repository, currentUid = uid)
                } else {
                    BiometricAuthHelper.authenticate(
                        activity = this,
                        onSuccess = { unlocked = true },
                        onFail = { /* 停留在鎖定畫面，可加上重試按鈕 */ }
                    )
                }
            }
        }
    }
}
