package com.guavas.cz3002.ui.violation

import android.app.NotificationManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import com.guavas.cz3002.R
import com.guavas.cz3002.databinding.FragmentViolationsBinding
import com.guavas.cz3002.extension.android.createNotificationChannel
import com.guavas.cz3002.ui.activity.MainActivityViewModel
import com.guavas.cz3002.ui.adapter.ViolationAdapter
import com.guavas.cz3002.ui.base.GuardedFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class ViolationsFragment : GuardedFragment<FragmentViolationsBinding>() {
    private val mainViewModel by activityViewModels<MainActivityViewModel>()
    val viewModel by viewModels<ViolationsFragmentViewModel>()
    override val layoutId = R.layout.fragment_violations

    override val menuResId = R.menu.main_menu

    override val loginDestinationId = R.id.loginFragment

    override fun createLoginState() = mainViewModel.currentUser.map { it != null }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitTransition = MaterialFadeThrough()

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            createNotificationChannel(
                id = getString(R.string.violation_notification_channel_id),
                name = getString(R.string.violation_notification_channel_name),
                importance = NotificationManager.IMPORTANCE_DEFAULT
            ) {
                enableLights(true)
                lightColor = Color.RED

                enableVibration(true)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            mainViewModel.assignment.collect { assignment ->
                if (assignment?.location == null) {
                    viewModel.updateLoading(false)
                } else {
                    assignment.location?.let(viewModel::setAssignedLocation)
                }
            }
        }

        val violationAdapter = ViolationAdapter(owner = viewLifecycleOwner,
            onClickViolation = {
                Timber.d("Navigating to details of violation ${it.id}")
            }, onVerifyViolation = { violation, isTrue ->
                Timber.d("Violation $violation $isTrue")
            })

        binding.recyclerViewViolations.adapter = violationAdapter

        lifecycleScope.launch {
            viewModel.violations.collect {
                it?.let { violations ->
                    Timber.d("Found ${violations.size} violations!")
                    it.forEach { v -> Timber.d("$v") }
                    viewModel.updateLoading(false)

                    violationAdapter.submitList(violations)
                }
            }
        }
    }

    override fun onNavigateToGuardDestination(destId: Int) {
        super.onNavigateToGuardDestination(destId)

        viewModel.updateLoading(true)
        viewModel.setAssignedLocation("")
    }
}