package com.guavas.cz3002.ui.violation

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialContainerTransform
import com.guavas.cz3002.R
import com.guavas.cz3002.databinding.FragmentViolationDetailsBinding
import com.guavas.cz3002.extension.android.themeColor
import com.guavas.cz3002.ui.activity.MainActivityViewModel
import com.guavas.cz3002.ui.base.BindingFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ViolationDetailsFragment : BindingFragment<FragmentViolationDetailsBinding>() {
    override val layoutId = R.layout.fragment_violation_details

    private val args by navArgs<ViolationDetailsFragmentArgs>()

    private val mainViewModel by activityViewModels<MainActivityViewModel>()
    val viewModel by viewModels<ViolationDetailsFragmentViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.navHostFragment
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(requireContext().themeColor(R.attr.colorSurface))
        }

        lifecycleScope.launch {
            val assignedLocation = mainViewModel.assignment
                .take(1)
                .single()
                ?.location
                ?: throw IllegalStateException("Unable to access details without logging in!")

            viewModel.setLocation(assignedLocation)
            viewModel.setViolationId(args.violationId)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.violation.observe(viewLifecycleOwner) {
            viewModel.loadImage(binding.imageViolation, it)
        }
    }

    fun verifyViolation(isTrue: Boolean) = lifecycleScope.launch {
        val uid = mainViewModel.currentUser.value?.uid
            ?: throw IllegalStateException("Unable to verify without logging in!")

        val violation = viewModel.violation.asFlow().take(1).single()

        viewModel.verifyViolation(violation, isTrue, uid)
    }
}