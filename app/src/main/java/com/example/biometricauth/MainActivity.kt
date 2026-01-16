package com.example.biometricauth

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.biometricauth.biometric.BioMetricResult
import com.example.biometricauth.ui.BiometricViewModel
import com.example.biometricauth.ui.theme.BiometricAuthTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for the biometric authentication demo.
 * 
 * Extends FragmentActivity (not ComponentActivity) because BiometricPrompt
 * requires FragmentManager support, which FragmentActivity provides.
 * 
 * Uses @AndroidEntryPoint for Hilt dependency injection.
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BiometricAuthTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BiometricAuthScreen(activity = this@MainActivity)
                }
            }
        }
    }
}

@Composable
fun BiometricAuthScreen(
    activity: FragmentActivity,
    viewModel: BiometricViewModel = viewModel()
) {
    /**
     * Collect authentication results from the ViewModel's StateFlow.
     * collectAsStateWithLifecycle automatically handles lifecycle-aware collection.
     */
    val biometricResult by viewModel.biometricResult.collectAsStateWithLifecycle()
    
    /**
     * Launcher for opening device settings when biometric enrollment is needed.
     * Only available on Android 11+ (API 30+).
     */
    val enrollLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            println("Activity result $it")
        }
    )
    
    /**
     * Automatically navigate to enrollment settings if user hasn't set up
     * biometric authentication. This provides a better UX by guiding users
     * to complete the setup process.
     */
    LaunchedEffect(biometricResult) {
        if (biometricResult is BioMetricResult.AuthenticationNotSet) {
            if (Build.VERSION.SDK_INT >= 30) {
                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(
                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BiometricManager.Authenticators.BIOMETRIC_STRONG or
                                BiometricManager.Authenticators.DEVICE_CREDENTIAL
                    )
                }
                enrollLauncher.launch(enrollIntent)
            }
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {
                // Trigger authentication through ViewModel
                viewModel.authenticate(
                    activity = activity,
                    title = "Sample Prompt",
                    description = "Sample Prompt description"
                )
            }
        ) {
            Text("Authenticate")
        }

        biometricResult?.let { result ->
            Text(
                text = when (result) {
                    is BioMetricResult.AuthenticationError -> {
                        result.error
                    }

                    BioMetricResult.AuthenticationFailed -> {
                        "Authentication Failed"
                    }

                    BioMetricResult.AuthenticationNotSet -> {
                        "Authentication Not set"
                    }

                    BioMetricResult.AuthenticationSuccess -> {
                        "Authentication Success"
                    }

                    BioMetricResult.FeatureUnAvailable -> {
                        "Feature Not Available"
                    }

                    BioMetricResult.HardwareUnAvailable -> {
                        "Hardware unavailable"
                    }
                }
            )
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BiometricAuthTheme {
        Greeting("Android")
    }
}