package com.guavas.cz3002.ui.violation

import android.app.NotificationManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import com.guavas.cz3002.R
import com.guavas.cz3002.data.violation.Violation
import com.guavas.cz3002.databinding.FragmentViolationsBinding
import com.guavas.cz3002.extension.android.createNotificationChannel
import com.guavas.cz3002.extension.android.doOnGlobalLayout
import com.guavas.cz3002.extension.android.navigate
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

    private val violationAdapter by lazy {
        ViolationAdapter(
            onClickViolation = this::navigateToDetails,
            onVerifyViolation = this::verifyViolation,
            onLoadImage = viewModel::loadImage
        )
    }

    private lateinit var manager: GridLayoutManager

    override fun createLoginState() = mainViewModel.currentUser.map { it != null }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitTransition = MaterialFadeThrough()

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

        createNotificationChannel()

        lifecycleScope.launch {
            mainViewModel.assignment.collect { assignment ->
                if (assignment?.location == null) {
                    viewModel.updateLoading(false)
                } else {
                    assignment.location?.let(viewModel::setAssignedLocation)
                }
            }
        }

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

    override fun onBinding(binding: FragmentViolationsBinding) {
        super.onBinding(binding)

        binding.root.doOnGlobalLayout { root ->
            val recyclerView = binding.recyclerViewViolations

            val parentWidth = root.width
            Timber.d("Parent width: $parentWidth")
            Timber.d("Recycler item width: $RECYCLER_VIEW_ITEM_WIDTH")

            val spanCount = parentWidth / RECYCLER_VIEW_ITEM_WIDTH

            Timber.d("Suitable span count: $spanCount")
            manager = GridLayoutManager(requireContext(), spanCount)
            viewModel.layoutManagerState?.let(manager::onRestoreInstanceState)

            recyclerView.layoutManager = manager
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        binding.recyclerViewViolations.adapter = violationAdapter
    }

    private fun navigateToDetails(violation: Violation, view: View) {
        Timber.d("Navigating to details of violation ${violation.id}")

        exitTransition = MaterialElevationScale(false)
        reenterTransition = MaterialElevationScale(true)

        val dir =
            ViolationsFragmentDirections.actionViolationsFragmentToViolationDetailsFragment(
                violation.id
            )

        val extras = FragmentNavigatorExtras(view to getString(R.string.transition_name_details))

        view.navigate(dir, extras)
    }

    private fun verifyViolation(violation: Violation, isTrue: Boolean) {
        Timber.d("Violation $violation $isTrue")

        viewModel.verifyViolation(
            violation = violation,
            isTrue = isTrue,
            uid = mainViewModel.currentUser.value?.uid
                ?: throw IllegalStateException("Unable to verify without logging in!")
        )
    }

    override fun onNavigateToGuardDestination(destId: Int) {
        super.onNavigateToGuardDestination(destId)

        viewModel.updateLoading(true)
        viewModel.setAssignedLocation("")
    }

    override fun onStop() {
        super.onStop()

        if (::manager.isInitialized) {
            viewModel.layoutManagerState = manager.onSaveInstanceState()
        }
    }

    companion object {
        const val RECYCLER_VIEW_ITEM_WIDTH = 1000
    }
}