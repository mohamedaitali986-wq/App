package com.example.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Creates a localized Context wrapping the specified language code ("ar" or "en").
 */
fun Context.createLocalizedContext(languageCode: String): Context {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    config.setLayoutDirection(locale)
    return createConfigurationContext(config)
}
