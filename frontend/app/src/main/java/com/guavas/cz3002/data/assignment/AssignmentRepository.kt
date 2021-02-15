package com.guavas.cz3002.data.assignment

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import javax.inject.Inject

class AssignmentRepository @Inject constructor(private val database: FirebaseDatabase) {
    /**
     * Retrieves assignment for the user.
     *
     * @param uid The signed-in user's uid.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAssignment(uid: String) = callbackFlow<Assignment?> {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.firstOrNull()
                    ?.getValue<Assignment>()
                    ?.let { data ->
                        sendBlocking(data)
                    } ?: sendBlocking(null)
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.w("RTD error on assignment retrieval: $error")
            }
        }

        val query = database.reference.child(ASSIGNMENT_PATH)
            .orderByChild(USER_KEY)
            .equalTo(uid)
            .apply {
                addValueEventListener(listener)
            }

        awaitClose { query.removeEventListener(listener) }
    }


    companion object {
        const val ASSIGNMENT_PATH = "assignments"
        const val USER_KEY = "user"
    }
}