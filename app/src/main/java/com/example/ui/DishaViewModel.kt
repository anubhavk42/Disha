package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.JobMatch
import com.example.data.JobRepository
import com.example.data.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DishaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = JobRepository(application)

    // Room Persistent state flows
    val activeSession: StateFlow<UserSession?> = repository.activeSessionFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val preferenceRoles = repository.allPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val jobMatches = repository.allMatchesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Local State Transitions
    private val _emailInput = MutableStateFlow("anubhavk42@gmail.com")
    val emailInput = _emailInput.asStateFlow()

    private val _phoneInput = MutableStateFlow("9988776655")
    val phoneInput = _phoneInput.asStateFlow()

    private val _userNameInput = MutableStateFlow("Anubhav Kapoor")
    val userNameInput = _userNameInput.asStateFlow()

    private val _otpInput = MutableStateFlow("")
    val otpInput = _otpInput.asStateFlow()

    private val _isAuthenticating = MutableStateFlow(false)
    val isAuthenticating = _isAuthenticating.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Temporary values for profile/preferences flow
    val selectedSalaryMin = MutableStateFlow(80000)
    val selectedSalaryMax = MutableStateFlow(150000)
    val selectedLocation = MutableStateFlow("Delhi NCR, Bangalore")

    // Personal Job Note Temp State
    val personalNoteText = MutableStateFlow("")

        // Dark Mode and Referral State
    val isDarkMode = MutableStateFlow(true)
    val isHighContrastMode = MutableStateFlow(false)

    // Job Filters State (INR Salary ranges in LPA)
    val filterLocation = MutableStateFlow("All")
    val filterIndustry = MutableStateFlow("All")
    val filterSalaryMin = MutableStateFlow(5)
    val filterSalaryMax = MutableStateFlow(100)
    val sortOption = MutableStateFlow("Match Score")

    init {
        // Run seed initial jobs in coroutines context
        viewModelScope.launch {
            repository.seedInitialData()
        }
        // Collect session to keep theme state flow in sync
        viewModelScope.launch {
            repository.activeSessionFlow.collect { session ->
                if (session != null) {
                    isDarkMode.value = session.isDarkMode
                    isHighContrastMode.value = session.isHighContrastMode
                }
            }
        }
    }

    fun setFilterLocation(value: String) {
        filterLocation.value = value
    }

    fun setFilterIndustry(value: String) {
        filterIndustry.value = value
    }

    fun setFilterSalaryRange(min: Int, max: Int) {
        filterSalaryMin.value = min
        filterSalaryMax.value = max
    }

    fun setSortOption(value: String) {
        sortOption.value = value
    }

    fun resetFilters() {
        filterLocation.value = "All"
        filterIndustry.value = "All"
        filterSalaryMin.value = 5
        filterSalaryMax.value = 100
        sortOption.value = "Match Score"
    }

    fun updateProfile(name: String, designation: String, email: String, mobile: String) {
        viewModelScope.launch {
            val session = repository.getActiveSession()
            if (session != null) {
                repository.updateSession(session.copy(
                    name = name,
                    designation = designation,
                    email = email,
                    mobile = mobile
                ))
            }
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            val session = repository.getActiveSession()
            val newDark = !isDarkMode.value
            isDarkMode.value = newDark

            // Sync with DataStore UserPreferences as well!
            val userPrefs = com.example.data.datastore.UserPreferences(getApplication())
            userPrefs.setTheme(if (newDark) "Dark" else "Light")

            if (session != null) {
                repository.updateSession(session.copy(
                    isDarkMode = newDark
                ))
            }
        }
    }

    fun toggleHighContrastMode() {
        viewModelScope.launch {
            val session = repository.getActiveSession()
            val newHC = !isHighContrastMode.value
            isHighContrastMode.value = newHC
            if (session != null) {
                repository.updateSession(session.copy(
                    isHighContrastMode = newHC
                ))
            }
        }
    }

    fun inviteReferral(emailOrPhone: String) {
        viewModelScope.launch {
            val session = repository.getActiveSession()
            if (session != null) {
                repository.updateSession(session.copy(
                    referralsCount = session.referralsCount + 1
                ))
            }
        }
    }

    fun onEmailChange(newValue: String) {
        _emailInput.value = newValue
    }

    fun onPhoneChange(newValue: String) {
        _phoneInput.value = newValue
    }

    fun onUserNameChange(newValue: String) {
        _userNameInput.value = newValue
    }

    fun onOtpChange(newValue: String) {
        if (newValue.length <= 6) {
            _otpInput.value = newValue
        }
    }

    fun onSearchQueryChange(newValue: String) {
        _searchQuery.value = newValue
    }

    fun requestOtp() {
        _authError.value = null
        if (_phoneInput.value.trim().isEmpty()) {
            _authError.value = "Please enter a valid mobile number"
            return
        }
    }

    fun performFirebaseAndLocalLogin() {
        viewModelScope.launch {
            _isAuthenticating.value = true
            _authError.value = null

            val signupEmail = if (_emailInput.value.contains("@")) _emailInput.value else "${_phoneInput.value}@disha.ai"
            val result = repository.signUp(
                email = signupEmail,
                name = _userNameInput.value,
                mobile = "+91 " + _phoneInput.value
            )

            result.fold(
                onSuccess = { session ->
                    _isAuthenticating.value = false
                },
                onFailure = { error ->
                    _authError.value = error.message ?: "Authentication failed. Defaulting to local space registration."
                    // Failover gracefully by saving local Session anyway
                    val fallbackSession = UserSession(
                        uid = "local_${System.currentTimeMillis()}",
                        email = signupEmail,
                        name = _userNameInput.value,
                        mobile = "+91 " + _phoneInput.value,
                        isSetupComplete = false
                    )
                    repository.updateSession(fallbackSession)
                    _isAuthenticating.value = false
                }
            )
        }
    }

    fun completeOnboardingAndSave() {
        viewModelScope.launch {
            val session = repository.getActiveSession()
            if (session != null) {
                repository.updateSession(session.copy(isSetupComplete = true))
            }
        }
    }

    fun addPreferenceRole(roleName: String) {
        viewModelScope.launch {
            repository.addPreferenceRole(roleName)
        }
    }

    fun toggleJobBookmark(id: String, isBookmarked: Boolean) {
        viewModelScope.launch {
            repository.toggleJobBookmark(id, isBookmarked)
        }
    }

    fun updateJobApplicationStatus(id: String, status: String?) {
        viewModelScope.launch {
            repository.updateJobApplicationStatus(id, status)
        }
    }

    fun removePreferenceRole(roleName: String) {
        viewModelScope.launch {
            repository.removePreferenceRole(roleName)
        }
    }

    fun uploadResumeAndProgress(fileName: String) {
        viewModelScope.launch {
            val session = repository.getActiveSession()
            if (session != null) {
                repository.updateSession(session.copy(
                    hasUploadedResume = true,
                    resumeFileName = fileName
                ))
            }
        }
    }

    fun updatePlatformConnection(platform: String, connected: Boolean) {
        viewModelScope.launch {
            val session = repository.getActiveSession()
            if (session != null) {
                val updated = when (platform.lowercase()) {
                    "linkedin" -> session.copy(linkedinConnected = connected)
                    "indeed" -> session.copy(indeedConnected = connected)
                    "naukri" -> session.copy(naukriConnected = connected)
                    else -> session
                }
                repository.updateSession(updated)
            }
        }
    }

    fun updateToggleDigest(enabled: Boolean) {
        viewModelScope.launch {
            val session = repository.getActiveSession()
            if (session != null) {
                repository.updateSession(session.copy(dailyDigest = enabled))
            }
        }
    }

    fun updateToggleBiometrics(enabled: Boolean) {
        viewModelScope.launch {
            val session = repository.getActiveSession()
            if (session != null) {
                repository.updateSession(session.copy(secureBiometrics = enabled))
            }
        }
    }

    fun updateToggleAutoApply(enabled: Boolean) {
        viewModelScope.launch {
            val session = repository.getActiveSession()
            if (session != null) {
                repository.updateSession(session.copy(autoApplyAlerts = enabled))
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            // Reset input fields
            _otpInput.value = ""
            _phoneInput.value = "9988776655"
        }
    }

    // deep link notification properties
    val deepLinkJobId = MutableStateFlow<String?>(null)
    val deepLinkScreen = MutableStateFlow<com.example.ui.screens.DishaScreen?>(null)

    fun handleDeepLink(jobId: String, screen: com.example.ui.screens.DishaScreen) {
        deepLinkJobId.value = jobId
        deepLinkScreen.value = screen
    }

    fun clearDeepLink() {
        deepLinkScreen.value = null
        deepLinkJobId.value = null
    }

    fun simulateIncomingMatch(context: android.content.Context) {
        viewModelScope.launch {
            // Retrieve current preference roles to ensure we match user criteria
            val activeRoles = preferenceRoles.value.map { it.roleName }
            val preferredTitle = activeRoles.firstOrNull() ?: "Senior Product Designer"

            val simulatedJob = com.example.data.JobMatch(
                id = "sim_${System.currentTimeMillis()}",
                title = preferredTitle,
                company = "Google India",
                logoUrl = "https://logo.clearbit.com/google.com",
                location = "Bangalore, India (Hybrid)",
                isRemote = false,
                salaryRange = "₹48L - ₹62L",
                matchScore = 97,
                sourcePlatform = "LinkedIn",
                isFullTime = true,
                experienceLevel = "Mid-Senior Level",
                responsibilitiesJson = """[
                    "Build next-generation inclusive user experiences powered by Google Gemini model intelligence.",
                    "Collaborate with diverse user research and developer teams globally across locations.",
                    "Advocate for visual craftsmanship, robust system typography, and gorgeous design languages."
                ]""",
                recruiterName = "Sarah Jenkins",
                recruiterRole = "Director of UX Talent"
            )

            // Trigger real evaluation and mobile push notification alert
            com.example.data.DishaFirebaseMessagingService.evaluateAndNotify(context, simulatedJob)
        }
    }
}
