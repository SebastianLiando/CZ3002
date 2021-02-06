package com.guavas.cz3002.ui.base

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.guavas.cz3002.R
import com.guavas.cz3002.utils.tryAssignBinding

/**
 * The base class for all [Fragment] that uses data binding. This class also tries to automatically
 * assigns this fragment to the binding object using reflection.
 *
 * To auto assign fragment, name the variable as "fragment".
 *
 * To set data to the fragment at the beginning, call [onBinding].
 *
 *
 * @param T The generated binding class.
 */
abstract class BindingFragment<T : ViewDataBinding> : Fragment() {
    protected lateinit var binding: T

    abstract val layoutId: Int

    open val menuResId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(menuResId != null)
    }

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
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item, requireView().findNavController()) ||
                super.onOptionsItemSelected(item)
    }
}