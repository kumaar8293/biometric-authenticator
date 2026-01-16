package com.example.biometricauth.biometric

import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Manages biometric authentication prompts using Android's BiometricPrompt API.
 * 
 * Requires FragmentActivity because BiometricPrompt internally uses FragmentManager
 * to display the authentication dialog. ComponentActivity alone cannot provide
 * the necessary fragment support.
 */
class BioMetricPromptManager @Inject constructor() {
    /**
     * StateFlow for UI state management.
     * StateFlow ensures that collectors always get the latest state.
     */
    private val _biometricResult = MutableStateFlow<BioMetricResult?>(null)
    val biometricResult: StateFlow<BioMetricResult?> = _biometricResult

    /**
     * Displays a biometric authentication prompt to the user.
     * 
     * Flow:
     * 1. Check hardware availability and enrollment status
     * 2. Configure authenticator types based on Android version
     * 3. Create and show BiometricPrompt if available
     * 4. Update StateFlow with results for UI consumption
     * 
     * @param activity FragmentActivity required for BiometricPrompt
     * @param title The title displayed in the authentication dialog
     * @param description The description text shown to the user
     */
    fun showBioMetricPrompt(activity: FragmentActivity, title: String, description: String) {
        val manager = BiometricManager.from(activity)
        
        // On Android 11+ (API 30+), allow both biometric and device credential (PIN/pattern/password)
        // On older versions, only biometric authentication is supported
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
        
        // Negative button is required on Android 10 and below
        // On Android 11+, device credential fallback handles cancellation
        if (Build.VERSION.SDK_INT < 30) {
            promptInfo.setNegativeButtonText("Cancel")
        }

        /**
         * Check biometric availability before showing prompt.
         * 
         * canAuthenticate() verifies:
         * - Hardware exists and is functional
         * - User has enrolled at least one biometric or credential
         * 
         * This is different from authentication - availability check happens
         * before the prompt, while authentication occurs during user interaction.
         */
        when (manager.canAuthenticate(authenticator)) {
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                _biometricResult.value = BioMetricResult.HardwareUnAvailable
                return
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                _biometricResult.value = BioMetricResult.FeatureUnAvailable
                return
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                _biometricResult.value = BioMetricResult.AuthenticationNotSet
                return
            }

            else -> Unit
        }

        /**
         * Create BiometricPrompt with callback handlers.
         * Results are sent through StateFlow to be consumed in the ViewModel and UI layer,
         * enabling reactive state management in Compose.
         */
        val prompt = BiometricPrompt(
            activity,
            object : BiometricPrompt.AuthenticationCallback() {
                /**
                 * Called when a non-recoverable error occurs (e.g., user cancels, too many failures).
                 * This is different from onAuthenticationFailed, which occurs on a single failed attempt.
                 */
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    _biometricResult.value = BioMetricResult.AuthenticationError(errString.toString())
                }

                /**
                 * Called when authentication fails but user can retry.
                 * The prompt remains visible for additional attempts.
                 */
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    _biometricResult.value = BioMetricResult.AuthenticationFailed
                }

                /**
                 * Called when authentication succeeds.
                 * User has been verified and can proceed with the protected action.
                 */
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    _biometricResult.value = BioMetricResult.AuthenticationSuccess
                }
            }
        )
        prompt.authenticate(promptInfo.build())
    }
}

/**
 * Sealed interface representing all possible biometric authentication outcomes.
 * Used with StateFlow to communicate results from BioMetricPromptManager to ViewModel and UI.
 */
sealed interface BioMetricResult {
    data object HardwareUnAvailable : BioMetricResult
    data object FeatureUnAvailable : BioMetricResult
    data class AuthenticationError(val error: String) : BioMetricResult
    data object AuthenticationFailed : BioMetricResult
    data object AuthenticationSuccess : BioMetricResult
    data object AuthenticationNotSet : BioMetricResult
}