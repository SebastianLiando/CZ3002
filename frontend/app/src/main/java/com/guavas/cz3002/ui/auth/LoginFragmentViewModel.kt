package com.guavas.cz3002.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.guavas.cz3002.utils.ViolationSubscription
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

@HiltViewModel
class LoginFragmentViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asLiveData()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage = _errorMessage.asLiveData()

    /**
     * Attempts sign in to Firebase Authentication service.
     *
     * @param email The email address.
     * @param password The password.
     *
     * @return `true` if the user has successfully signed in.
     */
    suspend fun signIn(email: String, password: String) =
        suspendCancellableCoroutine<Boolean> { cont ->
            _isLoading.value = true
            _errorMessage.value = ""

            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    _isLoading.value = false

                    if (it.isSuccessful) {
                        cont.resume(true)
                    } else {
                        _errorMessage.value = it.exception?.localizedMessage ?: ""
                        cont.resume(false)
                    }
                }
        }
}