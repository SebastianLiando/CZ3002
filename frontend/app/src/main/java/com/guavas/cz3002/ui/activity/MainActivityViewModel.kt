package com.guavas.cz3002.ui.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging
import com.guavas.cz3002.data.assignment.AssignmentRepository
import com.guavas.cz3002.data.violation.ViolationRepository
import com.guavas.cz3002.utils.ViolationSubscription
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(FlowPreview::class)
class MainActivityViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseMessaging: FirebaseMessaging,
    private val assignmentRepo: AssignmentRepository,
    private val violationRepo: ViolationRepository,
) : ViewModel() {
    private val _currentUser = MutableStateFlow(firebaseAuth.currentUser)

    /** Currently signed-in user. If signed out, this returns null. */
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    /** Holds the toilet location that the current user is assigned to. */
    private val assignment = currentUser
        .filter { user -> user != null }
        .map { user -> user!!.uid }
        .flatMapConcat { uid -> assignmentRepo.getAssignment(uid) }

    /** Contains violations of the assigned location. */
    val violations = assignment.flatMapConcat { violationRepo.getViolations(it.location) }

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