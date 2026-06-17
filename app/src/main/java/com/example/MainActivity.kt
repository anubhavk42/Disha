package com.example

import android.os.Bundle
import android.os.Build
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.datastore.UserPreferences
import com.example.ui.DishaViewModel
import com.example.ui.screens.MainNavigationContainer
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Request Notification permission for Android 13+ (Tiramisu)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
      }
    }

    setContent {
      val context = LocalContext.current
      val userPreferences = remember { UserPreferences(context) }
      val prefTheme by userPreferences.themeFlow.collectAsState(initial = "System")
      val prefFontSize by userPreferences.fontSizeFlow.collectAsState(initial = true)

      val systemDark = isSystemInDarkTheme()
      val isDarkTheme = when (prefTheme) {
        "Dark" -> true
        "Light" -> false
        else -> systemDark
      }

      val dishaViewModel: DishaViewModel = viewModel()
      // Synchronize core state flow with selected preference
      LaunchedEffect(isDarkTheme) {
        dishaViewModel.isDarkMode.value = isDarkTheme
      }
      val isHighContrast by dishaViewModel.isHighContrastMode.collectAsState()

      // Handle deep links from notifications when intent updates
      LaunchedEffect(intent) {
        intent?.let { handleIntent(it, dishaViewModel) }
      }

      MyApplicationTheme(darkTheme = isDarkTheme, highContrast = isHighContrast) {
        val localDensity = LocalDensity.current
        val customDensity = if (prefFontSize) {
          localDensity
        } else {
          Density(density = localDensity.density, fontScale = 1.0f)
        }

        CompositionLocalProvider(LocalDensity provides customDensity) {
          Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
          ) {
            MainNavigationContainer(viewModel = dishaViewModel)
          }
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent) // Triggers LaunchedEffect(intent) in compose content
  }

  private fun handleIntent(intent: Intent, viewModel: DishaViewModel) {
    val selectedId = intent.getStringExtra("selected_job_id")
    val navigateScreen = intent.getStringExtra("navigate_to_screen")
    if (navigateScreen == "JobDetail" && selectedId != null) {
      viewModel.handleDeepLink(selectedId, com.example.ui.screens.DishaScreen.JobDetail)
    }
  }
}
