package com.example.data

import android.app.Activity
import android.util.Log
import com.example.BuildConfig
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.abs

data class FirebaseUserResponse(
    val localId: String,
    val email: String,
    val idToken: String?,
    val refreshToken: String?,
    val error: String? = null
)

/**
 * Thin wrapper around the Firebase Auth SDK. When no real Firebase project is configured
 * (i.e. [BuildConfig.FIREBASE_API_KEY] is blank), email sign-up/login and phone OTP sign-in all
 * skip the live network call entirely and return a mock success via [getMockSuccess] (or a mock
 * verification id, for [sendOtp]) so the sandbox app remains usable without real credentials.
 * When a real key IS configured, failures are surfaced as a generic, user-friendly message - the
 * raw Firebase/network error is only logged, never returned to callers.
 */
class FirebaseService {

    sealed class PhoneAuthResult {
        data class CodeSent(val verificationId: String) : PhoneAuthResult()
        data class AutoVerified(val credential: PhoneAuthCredential) : PhoneAuthResult()
        data class Failed(val message: String) : PhoneAuthResult()
    }

    private val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    private fun isRealFirebaseConfigured(): Boolean = BuildConfig.FIREBASE_API_KEY.isNotBlank()

    /** Fabricates a stable, deterministic "logged in" response without touching the network. */
    private fun getMockSuccess(email: String): FirebaseUserResponse =
        FirebaseUserResponse(
            localId = "mock-${abs(email.hashCode())}",
            email = email,
            idToken = null,
            refreshToken = null
        )

    suspend fun signUpWithEmail(email: String, password: String): FirebaseUserResponse = withContext(Dispatchers.IO) {
        if (!isRealFirebaseConfigured()) return@withContext getMockSuccess(email)
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                FirebaseUserResponse(localId = user.uid, email = user.email ?: email, idToken = null, refreshToken = null)
            } else {
                FirebaseUserResponse("", "", null, null, "Couldn't sign you in, please try again")
            }
        } catch (e: Exception) {
            Log.e("FirebaseService", "Sign up failed", e)
            FirebaseUserResponse("", "", null, null, "Couldn't create your account, please try again")
        }
    }

    suspend fun loginWithEmail(email: String, password: String): FirebaseUserResponse = withContext(Dispatchers.IO) {
        if (!isRealFirebaseConfigured()) return@withContext getMockSuccess(email)
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                FirebaseUserResponse(localId = user.uid, email = user.email ?: email, idToken = null, refreshToken = null)
            } else {
                FirebaseUserResponse("", "", null, null, "Couldn't sign you in, please try again")
            }
        } catch (e: Exception) {
            Log.e("FirebaseService", "Login failed", e)
            FirebaseUserResponse("", "", null, null, "Couldn't sign you in, please try again")
        }
    }

    /**
     * Kicks off real Firebase phone-number verification. Requires an [Activity] because Firebase
     * may need to display a reCAPTCHA fallback if silent (Play Integrity) verification is unavailable.
     * Skips the live call entirely (no network round-trip, no reCAPTCHA) when no real Firebase
     * project is configured, immediately returning a mock verification id instead.
     */
    suspend fun sendOtp(phoneNumber: String, activity: Activity): PhoneAuthResult {
        if (!isRealFirebaseConfigured()) return PhoneAuthResult.CodeSent(MOCK_VERIFICATION_ID)
        return suspendCancellableCoroutine { cont ->
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    if (cont.isActive) cont.resume(PhoneAuthResult.AutoVerified(credential))
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.e("FirebaseService", "Phone verification failed", e)
                    if (cont.isActive) cont.resume(PhoneAuthResult.Failed("Couldn't send the verification code, please try again"))
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    if (cont.isActive) cont.resume(PhoneAuthResult.CodeSent(verificationId))
                }
            }

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    /**
     * Completes sign-in with a credential obtained either from [sendOtp]'s auto-verification or
     * from a user-entered OTP code. Skips the live call and returns a mock success when no real
     * Firebase project is configured - this also covers the [MOCK_VERIFICATION_ID] credential
     * built locally by [PhoneAuthProvider.getCredential], which Firebase would otherwise reject.
     */
    suspend fun signInWithPhoneCredential(credential: PhoneAuthCredential): FirebaseUserResponse = withContext(Dispatchers.IO) {
        if (!isRealFirebaseConfigured()) return@withContext getMockSuccess(MOCK_PHONE_EMAIL)
        try {
            val result = auth.signInWithCredential(credential).await()
            val user = result.user
            if (user != null) {
                FirebaseUserResponse(localId = user.uid, email = user.email ?: "", idToken = null, refreshToken = null)
            } else {
                FirebaseUserResponse("", "", null, null, "Couldn't verify your code, please try again")
            }
        } catch (e: Exception) {
            Log.e("FirebaseService", "Phone sign-in failed", e)
            FirebaseUserResponse("", "", null, null, "Couldn't verify your code, please try again")
        }
    }

    companion object {
        private const val MOCK_VERIFICATION_ID = "mock-verification-id"
        private const val MOCK_PHONE_EMAIL = "mock-phone-user@disha.ai"
    }

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                cont.resume(task.result)
            } else {
                cont.resumeWithException(task.exception ?: Exception("Unknown Firebase error"))
            }
        }
    }
}
