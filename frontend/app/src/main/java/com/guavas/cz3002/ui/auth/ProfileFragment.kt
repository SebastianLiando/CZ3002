package com.guavas.cz3002.ui.auth

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialFadeThrough
import com.guavas.cz3002.R
import com.guavas.cz3002.databinding.FragmentProfileBinding
import com.guavas.cz3002.extension.android.pressBackButton
import com.guavas.cz3002.ui.activity.MainActivityViewModel
import com.guavas.cz3002.ui.base.BindingFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : BindingFragment<FragmentProfileBinding>() {
    override val layoutId = R.layout.fragment_profile

    private val viewModel by viewModels<ProfileFragmentViewModel>()
    private val mainViewModel by activityViewModels<MainActivityViewModel>()
    val currentUser by lazy { mainViewModel.currentUser.value }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialFadeThrough()
    }

    fun onClickSignOut() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.label_dialog_log_out_title)
            .setMessage(R.string.label_dialog_log_out_message)
            .setPositiveButton(R.string.label_yes) { _, _ ->
                viewModel.signOut()
                pressBackButton()
            }.setNegativeButton(R.string.label_no) { _, _ -> /* dismiss */ }
            .show()
    }
}