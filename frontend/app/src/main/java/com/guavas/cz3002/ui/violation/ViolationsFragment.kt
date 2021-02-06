package com.guavas.cz3002.ui.violation

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
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

    override val menuResId = R.menu.main_menu

    override val loginDestinationId = R.id.loginFragment

    override fun createLoginState() = mainViewModel.currentUser.map { it != null }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitTransition = MaterialFadeThrough()

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }
}