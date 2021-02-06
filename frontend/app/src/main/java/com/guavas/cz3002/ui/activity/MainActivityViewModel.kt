package com.guavas.cz3002.ui.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging
import com.guavas.cz3002.utils.ViolationSubscription
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseMessaging: FirebaseMessaging
) : ViewModel() {
    private val _currentUser = MutableStateFlow(firebaseAuth.currentUser)

    /** Currently signed-in user. If signed out, this returns null. */
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    private val authListener = FirebaseAuth.AuthStateListener { auth ->
        _currentUser.value = auth.currentUser
    }

    init {
        firebaseAuth.addAuthStateListener(authListener)

        viewModelScope.launch {
            currentUser.collect {
                if (it == null) {
                    firebaseMessaging.unsubscribeFromTopic(ViolationSubscription.TOPIC)
                } else {
                    firebaseMessaging.subscribeToTopic(ViolationSubscription.TOPIC)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        firebaseAuth.removeAuthStateListener(authListener)
    }
}