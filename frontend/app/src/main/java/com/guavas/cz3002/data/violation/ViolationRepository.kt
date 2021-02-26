package com.guavas.cz3002.data.violation

import android.widget.ImageView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage
import com.guavas.cz3002.utils.GlideApp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import javax.inject.Inject

class ViolationRepository @Inject constructor(
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getViolations(location: String) = callbackFlow<List<Violation>> {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val violations = snapshot.children
                    .mapNotNull {
                        it.getValue<Violation>()?.apply {
                            it.key?.let { key -> id = key }
                        }
                    }
                sendBlocking(violations)
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.w("RTD error on violation retrieval: $error")
            }
        }

        val dataPath = database.reference.child(VIOLATION_PATH)
            .orderByChild(LOCATION_KEY)

        dataPath.keepSynced(true)

        val query = dataPath
            .equalTo(location)
            .apply {
                addValueEventListener(listener)
            }

        awaitClose {
            query.removeEventListener(listener)
        }
    }

    fun loadViolationImage(view: ImageView, violation: Violation) {
        val ref = storage.reference.child(violation.imageId)

        GlideApp.with(view.context)
            .load(ref)
            .error(android.R.color.darker_gray)
            .into(view)
    }

    companion object {
        const val VIOLATION_PATH = "violations"
        const val LOCATION_KEY = "location"
    }
}