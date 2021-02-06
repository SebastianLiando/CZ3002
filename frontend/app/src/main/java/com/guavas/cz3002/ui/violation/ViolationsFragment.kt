package com.guavas.cz3002.ui.violation

import androidx.fragment.app.activityViewModels
import com.guavas.cz3002.R
import com.guavas.cz3002.databinding.FragmentViolationsBinding
import com.guavas.cz3002.ui.activity.MainActivityViewModel
import com.guavas.cz3002.ui.base.GuardedFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map

@AndroidEntryPoint
class ViolationsFragment : GuardedFragment<FragmentViolationsBinding>() {
    private val mainViewModel by activityViewModels<MainActivityViewModel>()
    override val layoutId = R.layout.fragment_violations

    override val loginDestinationId = R.id.loginFragment

    override fun createLoginState() = mainViewModel.currentUser.map { it != null }

}