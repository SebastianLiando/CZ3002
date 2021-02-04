package com.guavas.cz3002.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.guavas.cz3002.ui.activity.MainActivityViewModel
import com.guavas.cz3002.utils.tryAssignBinding

/**
 * The base class for all [Fragment] that uses data binding. This class also tries to automatically
 * assigns this fragment and the activity's view model to the binding object.
 *
 * To auto assign fragment, name the variable as "fragment".
 *
 * To auto assign activity view model, name the variable as "activityModel".
 *
 * To set data to the fragment at the beginning, call [onBinding].
 *
 *
 * @param T The generated binding class.
 */
abstract class BindingFragment<T : ViewDataBinding> : Fragment() {
    val mainViewModel by activityViewModels<MainActivityViewModel>()

    protected lateinit var binding: T

    abstract val layoutId: Int

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        binding.run {
            // Binding using reflection
            onBindingReflection(this)

            // Manual binding
            onBinding(this)

            lifecycleOwner = viewLifecycleOwner

            invalidateAll()
        }

        return binding.root
    }

    /**
     * Assigns data to the binding object.
     * After assignment, [ViewDataBinding.invalidateAll] will be called.
     *
     * @param binding The binding object.
     */
    protected open fun onBinding(binding: T) {}

    /**
     * Assigns data to the binding object via reflection mechanism. This method should **NOT** be
     * exposed to a non-abstract class.
     *
     * @param binding The binding object.
     */
    private fun onBindingReflection(binding: T) {
        // Set fragment
        tryAssignBinding("setFragment", this::class.java, binding, this)

        // Set shared view model
        tryAssignBinding("setActivityModel", mainViewModel::class.java, binding, mainViewModel)
    }
}