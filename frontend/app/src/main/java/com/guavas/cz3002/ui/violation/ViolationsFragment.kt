package com.guavas.cz3002.ui.violation

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.guavas.cz3002.R
import com.guavas.cz3002.databinding.FragmentViolationsBinding
import com.guavas.cz3002.extension.android.navigate
import com.guavas.cz3002.ui.base.BindingFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class ViolationsFragment : BindingFragment<FragmentViolationsBinding>() {
    override val layoutId = R.layout.fragment_violations

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            mainViewModel.currentUser.collect {
                if (it == null) {
                    Timber.d("User not logged in! Navigating to login fragment")
                    view.navigate(R.id.loginFragment)
                }
            }
        }
    }
}