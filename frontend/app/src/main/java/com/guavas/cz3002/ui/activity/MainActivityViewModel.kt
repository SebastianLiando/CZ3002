package com.guavas.cz3002.ui.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging
import com.guavas.cz3002.data.assignment.AssignmentRepository
import com.guavas.cz3002.data.preference.PreferencesRepository
import com.guavas.cz3002.data.violation.ViolationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class MainActivityViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseMessaging: FirebaseMessaging,
    private val assignmentRepo: AssignmentRepository,
    private val violationRepo: ViolationRepository,
    private val preferencesRepo: PreferencesRepository,
) : ViewModel() {
    private val _currentUser = MutableStateFlow(firebaseAuth.currentUser)

    /** Currently signed-in user. If signed out, this returns null. */
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    /** Holds the toilet location that the current user is assigned to. Assignment is null means that
     * the security guard is not assigned to any location. */
    private val assignment = currentUser
        .map { user -> user?.uid }
        .flatMapLatest { uid -> uid?.let { assignmentRepo.getAssignment(it) } ?: flowOf(null) }

    /** Contains violations of the assigned location. Violation is null means that the security guard
     * is not assigned to any location. Empty list means that there is no violation. */
    val violations = assignment.flatMapConcat { assignment ->
        assignment?.location?.let { location ->
            violationRepo.getViolations(location)
        } ?: flowOf(null)
    }

    /** Listens to user sign in and sign out. */
    private val authListener = FirebaseAuth.AuthStateListener { auth ->
        _currentUser.value = auth.currentUser
    }

    init {
        firebaseAuth.addAuthStateListener(authListener)

        viewModelScope.launch {
            currentUser.collect { Timber.d("Current user ${it?.uid}") }
        }

        viewModelScope.launch {
            assignment.collect { assigned ->
                Timber.d("Assigned: $assigned")
                val currentTopic = preferencesRepo.subscribedTopic.take(1).single()

                if (currentTopic != assigned?.location) {
                    if (currentTopic.isNotEmpty()) {
                        firebaseMessaging.unsubscribeFromTopic(currentTopic)
                        Timber.d("Stop subscribing from topic '$currentTopic'")
                    }

                    assigned?.location?.let { assignedLocation ->
                        firebaseMessaging.subscribeToTopic(assignedLocation)
                        Timber.d("Subscribed to topic '$assignedLocation'")

                        viewModelScope.launch {
                            preferencesRepo.updateSubscribedTopic(assignedLocation)
                        }
                    } ?: preferencesRepo.clearSubscribedTopic()
                }
            }
        }
    }


    override fun onCleared() {
        super.onCleared()

        firebaseAuth.removeAuthStateListener(authListener)
    }
}