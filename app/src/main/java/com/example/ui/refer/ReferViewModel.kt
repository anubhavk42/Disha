package com.example.ui.refer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.JobRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ReferUiState(
    val referralCode: String = "DISHA-AN82",
    val invitedCount: Int = 8,
    val joinedCount: Int = 5,
    val proDaysEarned: Int = 150
)

class ReferViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = JobRepository(application)

    private val _uiState = MutableStateFlow(ReferUiState())
    val uiState: StateFlow<ReferUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.activeSessionFlow.collectLatest { session ->
                if (session != null) {
                    val code = generateReferralCode(session.name, session.uid)
                    // Map or mock realistic stats relative to session referral count
                    val invited = if (session.referralsCount > 0) session.referralsCount * 2 + 1 else 8
                    val joined = if (session.referralsCount > 0) session.referralsCount else 5
                    val proDays = joined * 30

                    _uiState.value = ReferUiState(
                        referralCode = code,
                        invitedCount = invited,
                        joinedCount = joined,
                        proDaysEarned = proDays
                    )
                } else {
                    // Fallback mock values
                    _uiState.value = ReferUiState(
                        referralCode = "DISHA-JD4281",
                        invitedCount = 12,
                        joinedCount = 8,
                        proDaysEarned = 240
                    )
                }
            }
        }
    }

    private fun generateReferralCode(name: String, uid: String): String {
        val cleanName = name.trim().filter { it.isLetter() }.uppercase()
        val prefix = if (cleanName.length >= 2) cleanName.substring(0, 2) else "DI"
        val suffix = if (uid.length >= 4) {
            uid.substring(uid.length - 4)
        } else {
            val hash = uid.hashCode()
            val absHash = if (hash == Int.MIN_VALUE) 0 else java.lang.Math.abs(hash)
            String.format("%04d", absHash % 10000)
        }
        return "DISHA-$prefix$suffix"
    }
}
