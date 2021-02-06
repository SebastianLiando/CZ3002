package com.guavas.cz3002.ui.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileFragmentViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {
    fun signOut() {
        firebaseAuth.signOut()
    }
}