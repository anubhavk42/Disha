package com.example.ui.privacy

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.datastore.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AccessLog(
    val recruiterName: String,
    val timeAgo: String,
    val actionType: String
)

class PrivacyViewModel(application: Application) : AndroidViewModel(application) {
    private val userPreferences = UserPreferences(application)
    private val database = AppDatabase.getDatabase(application)

    private val _profileVisibility = MutableStateFlow("Recruiters only")
    val profileVisibility: StateFlow<String> = _profileVisibility.asStateFlow()

    private val _dataConsentGiven = MutableStateFlow(true)
    val dataConsentGiven: StateFlow<Boolean> = _dataConsentGiven.asStateFlow()

    private val _accessLogs = MutableStateFlow<List<AccessLog>>(emptyList())
    val accessLogs: StateFlow<List<AccessLog>> = _accessLogs.asStateFlow()

    private val _cookiePreferences = MutableStateFlow(true)
    val cookiePreferences: StateFlow<Boolean> = _cookiePreferences.asStateFlow()

    init {
        // Load realistic CV view/download access logs as required
        _accessLogs.value = listOf(
            AccessLog("Reliance Games Recruiter", "2 hours ago", "Downloaded CV"),
            AccessLog("Tata Consultancy Services Lead", "1 day ago", "Viewed Profile"),
            AccessLog("Swiggy Talent Hunter", "3 days ago", "Downloaded CV"),
            AccessLog("Zomato Recruitment Partner", "5 days ago", "Viewed Profile")
        )
    }

    fun setProfileVisibility(visibility: String) {
        _profileVisibility.value = visibility
    }

    fun setConsentGiven(consent: Boolean) {
        _dataConsentGiven.value = consent
    }

    fun setCookiePreferences(enabled: Boolean) {
        _cookiePreferences.value = enabled
    }

    fun deleteAccountAndAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            // 1. Reset all preferences in DataStore
            userPreferences.clear()

            // 2. Erase Room SQLite records completely
            database.userSessionDao().clearSession()
            database.preferenceRoleDao().clearAll()
            database.jobMatchDao().clearAll()

            // 3. Callback to trigger UI navigation transition
            onComplete()
        }
    }
}
