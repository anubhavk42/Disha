package com.example.ui.appearance

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.datastore.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppearanceViewModel(application: Application) : AndroidViewModel(application) {
    private val userPreferences = UserPreferences(application)

    val themeState: StateFlow<String> = userPreferences.themeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "System")

    val reduceMotionState: StateFlow<Boolean> = userPreferences.reduceMotionFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val fontSizeState: StateFlow<Boolean> = userPreferences.fontSizeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setTheme(theme: String) {
        viewModelScope.launch {
            userPreferences.setTheme(theme)
        }
    }

    fun setReduceMotion(reduce: Boolean) {
        viewModelScope.launch {
            userPreferences.setReduceMotion(reduce)
        }
    }

    fun setUseSystemFontSize(useSystem: Boolean) {
        viewModelScope.launch {
            userPreferences.setUseSystemFontSize(useSystem)
        }
    }
}
