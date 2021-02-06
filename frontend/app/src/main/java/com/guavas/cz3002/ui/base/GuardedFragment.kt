package com.guavas.cz3002.ui.base

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.lifecycleScope
import com.guavas.cz3002.extension.android.navigate
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import timber.log.Timber

/**
 * The base class for all [BindingFragment] that requires user to be logged in.
 * The login state is determined by [loginState]. If the user is not logged in when this fragment is
 * accessed, the user will be redirected to [loginDestinationId].
 *
 * @param T The generated binding class.
 *
 */
abstract class GuardedFragment<T : ViewDataBinding> : BindingFragment<T>() {
    /** The destination ID which is used to log the user in. */
    abstract val loginDestinationId: Int

    /** The login state. The value `true` means the user is logged in. */
    private val loginState: Flow<Boolean> by lazy { createLoginState() }

    /** This function should create the [Flow] for the login state. */
    abstract fun createLoginState(): Flow<Boolean>

    private lateinit var guard: Job

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        guard = lifecycleScope.launchWhenResumed {
            loginState.collect { loggedIn ->
                if (!loggedIn) {
                    Timber.d("User not logged in! Navigating to login fragment")
                    view.navigate(loginDestinationId)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (::guard.isInitialized) {
            guard.cancel()
        }
    }
}