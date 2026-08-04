package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.data.AppDatabase
import com.example.data.DatabaseSeeder
import com.example.data.repository.AppRepository
import com.example.ui.screens.navigation.AppNavigation
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.theme.ShaghafTheme
import com.example.util.SessionManager
import com.example.util.createLocalizedContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(this)
        val repository = AppRepository(database)
        val sessionManager = SessionManager(this)

        setContent {
            LaunchedEffect(Unit) {
                DatabaseSeeder.seedIfNeeded(database)
            }

            val currentLang by sessionManager.appLanguage.collectAsState()
            val baseContext = LocalContext.current
            val localizedContext = remember(currentLang, baseContext) {
                baseContext.createLocalizedContext(currentLang)
            }
            val layoutDirection = if (currentLang == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

            var showSplash by remember { mutableStateOf(true) }

            ShaghafTheme {
                CompositionLocalProvider(
                    LocalContext provides localizedContext,
                    LocalActivityResultRegistryOwner provides this@MainActivity,
                    LocalLayoutDirection provides layoutDirection
                ) {
                    if (showSplash) {
                        SplashScreen(
                            onSplashFinished = { showSplash = false },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        AppNavigation(
                            repository = repository,
                            sessionManager = sessionManager,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
