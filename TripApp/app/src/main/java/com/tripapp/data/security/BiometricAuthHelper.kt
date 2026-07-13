package com.tripapp.data.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import androidx.core.content.ContextCompat

/** 選配：App 啟動時要求指紋/臉部辨識，防止手機遺失後資料被直接翻閱 */
object BiometricAuthHelper {

    fun isAvailable(activity: FragmentActivity): Boolean {
        val manager = BiometricManager.from(activity)
        return manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onFail: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }
                override fun onAuthenticationFailed() { onFail() }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) { onFail() }
            }
        )
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("解鎖旅遊行程")
            .setSubtitle("驗證身分以檢視你的行程資料")
            .setNegativeButtonText("取消")
            .build()
        prompt.authenticate(promptInfo)
    }
}
