package com.example.biometricauth.biometric

import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class BioMetricPromptManager(val context: FragmentActivity) {
    private val _resultChannel = Channel<BioMetricResult>()
    val resultChannel = _resultChannel.receiveAsFlow()

    fun showBioMetricPrompt(title: String, description: String) {
        val manager = BiometricManager.from(context)
        val authenticator =
            if (Build.VERSION.SDK_INT >= 30) {
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            } else {
                BiometricManager.Authenticators.BIOMETRIC_STRONG
            }
        val promptInfo = BiometricPrompt
            .PromptInfo.Builder()
            .setTitle(title)
            .setDescription(description)
            .setAllowedAuthenticators(authenticator)
          // .setConfirmationRequired(false)
        if (Build.VERSION.SDK_INT < 30) {
            promptInfo.setNegativeButtonText("Cancel")
        }

        when (manager.canAuthenticate(authenticator)) {
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                _resultChannel.trySend(BioMetricResult.HardwareUnAvailable)
                return
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                _resultChannel.trySend(BioMetricResult.FeatureUnAvailable)
                return
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                _resultChannel.trySend(BioMetricResult.AuthenticationNotSet)
                return
            }

            else -> Unit
        }

        val prompt = BiometricPrompt(
            context,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    _resultChannel.trySend(BioMetricResult.AuthenticationError(errString.toString()))

                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    _resultChannel.trySend(BioMetricResult.AuthenticationFailed)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    _resultChannel.trySend(BioMetricResult.AuthenticationSuccess)
                }

            }

        )
        prompt.authenticate(promptInfo.build())
    }
}

sealed interface BioMetricResult {
    data object HardwareUnAvailable : BioMetricResult
    data object FeatureUnAvailable : BioMetricResult
    data class AuthenticationError(val error: String) : BioMetricResult
    data object AuthenticationFailed : BioMetricResult
    data object AuthenticationSuccess : BioMetricResult
    data object AuthenticationNotSet : BioMetricResult
}