package com.example.biometricauth

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import com.example.biometricauth.biometric.BioMetricPromptManager
import com.example.biometricauth.biometric.BioMetricResult
import com.example.biometricauth.ui.theme.BiometricAuthTheme

/**
 * Main activity for the biometric authentication demo.
 * 
 * Extends FragmentActivity (not ComponentActivity) because BiometricPrompt
 * requires FragmentManager support, which FragmentActivity provides.
 */
class MainActivity : FragmentActivity() {

    /**
     * Lazy initialization of BioMetricPromptManager to avoid creating it
     * before the activity is fully initialized.
     */
    private val promptManager by lazy { BioMetricPromptManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BiometricAuthTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    /**
                     * Collect authentication results from the Flow.
                     * collectAsState converts the Flow into a Compose state that
                     * triggers recomposition when new results arrive.
                     */
                    val bioMetricResult by promptManager.resultChannel.collectAsState(initial = null)
                    
                    /**
                     * Launcher for opening device settings when biometric enrollment is needed.
                     * Only available on Android 11+ (API 30+).
                     */
                    val enrollLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult(),
                        onResult = {
                            println("ZACT Activity result $it")
                        }
                    )
                    
                    /**
                     * Automatically navigate to enrollment settings if user hasn't set up
                     * biometric authentication. This provides a better UX by guiding users
                     * to complete the setup process.
                     */
                    LaunchedEffect(bioMetricResult) {
                        if (bioMetricResult is BioMetricResult.AuthenticationNotSet) {
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
                        Button(onClick = {
                            promptManager.showBioMetricPrompt(
                                "Sample Prompt",
                                "Sample Prompt description"
                            )
                        }
                        ) {
                            Text("Authenticate")
                        }

                        bioMetricResult?.let { result ->
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
            }
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