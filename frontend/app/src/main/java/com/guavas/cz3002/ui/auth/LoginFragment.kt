package com.guavas.cz3002.ui.auth

import android.content.Context
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.guavas.cz3002.R
import com.guavas.cz3002.databinding.FragmentLoginBinding
import com.guavas.cz3002.extension.android.closeActivity
import com.guavas.cz3002.extension.android.hideKeyboard
import com.guavas.cz3002.extension.android.pressBackButton
import com.guavas.cz3002.extension.android.textString
import com.guavas.cz3002.ui.activity.MainActivityViewModel
import com.guavas.cz3002.ui.base.BindingFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class LoginFragment : BindingFragment<FragmentLoginBinding>() {
    override val layoutId = R.layout.fragment_login

    private val mainViewModel by activityViewModels<MainActivityViewModel>()
    val viewModel by viewModels<LoginFragmentViewModel>()

    private val backButtonHandler = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (mainViewModel.currentUser.value == null) {
                // Quit application if not signed in
                Timber.d("Quitting application")
                closeActivity()
            } else {
                // Prevent this callback from being called again when the back button is pressed
                Timber.d("Entered login although already logged in?!")
                isEnabled = false

                pressBackButton()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        requireActivity().onBackPressedDispatcher.addCallback(this, backButtonHandler)
    }

    fun onClickSignIn(v: View) {
        requireContext().hideKeyboard(v)

        lifecycleScope.launch {
            val email = binding.editTextEmail.textString
            val password = binding.editTextPassword.textString

            if (email.isEmpty()) {
                binding.layoutEmail.error = getString(R.string.error_empty)
            }

            if (password.isEmpty()) {
                binding.layoutPassword.error = getString(R.string.error_empty)
            }

            if (email.isEmpty() || password.isEmpty()) return@launch

            if (viewModel.signIn(email, password)) {
                pressBackButton()
            } else {
                Snackbar.make(requireView(), R.string.snack_bar_login_failed, Snackbar.LENGTH_LONG)
                    .setAction(android.R.string.ok) { /* Dismiss */ }
                    .show()
            }
        }
    }
}