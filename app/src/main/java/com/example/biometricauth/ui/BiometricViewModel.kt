package com.example.biometricauth.ui

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.biometricauth.biometric.BioMetricPromptManager
import com.example.biometricauth.biometric.BioMetricResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel that manages biometric authentication state and triggers authentication.
 * 
 * Observes BioMetricPromptManager's state and exposes it to the UI.
 * UI should only trigger authentication through ViewModel methods.
 */
@HiltViewModel
class BiometricViewModel @Inject constructor(
    private val biometricPromptManager: BioMetricPromptManager
) : ViewModel() {

    /**
     * Exposes biometric result state to the UI.
     * StateFlow ensures UI always receives the latest state.
     */
    val biometricResult: StateFlow<BioMetricResult?> = biometricPromptManager.biometricResult
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    /**
     * Triggers biometric authentication prompt.
     * 
     * @param activity FragmentActivity required for BiometricPrompt
     * @param title Title for the authentication dialog
     * @param description Description for the authentication dialog
     */
    fun authenticate(activity: FragmentActivity, title: String, description: String) {
        viewModelScope.launch {
            biometricPromptManager.showBioMetricPrompt(activity, title, description)
        }
    }
}
