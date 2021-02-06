package com.guavas.cz3002.ui.violation

import android.app.NotificationManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import com.guavas.cz3002.R
import com.guavas.cz3002.databinding.FragmentViolationsBinding
import com.guavas.cz3002.extension.android.createNotificationChannel
import com.guavas.cz3002.ui.activity.MainActivityViewModel
import com.guavas.cz3002.ui.base.GuardedFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map

@AndroidEntryPoint
class ViolationsFragment : GuardedFragment<FragmentViolationsBinding>() {
    private val mainViewModel by activityViewModels<MainActivityViewModel>()
    private val viewModel by viewModels<ViolationsFragmentViewModel>()
    override val layoutId = R.layout.fragment_violations

    override val menuResId = R.menu.main_menu

    override val loginDestinationId = R.id.loginFragment

    override fun createLoginState() = mainViewModel.currentUser.map { it != null }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitTransition = MaterialFadeThrough()

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) createNotificationChannel()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() = createNotificationChannel(
        id = getString(R.string.violation_notification_channel_id),
        name = getString(R.string.violation_notification_channel_name),
        importance = NotificationManager.IMPORTANCE_DEFAULT
    ) {
        enableLights(true)
        lightColor = Color.RED

        enableVibration(true)
    }
}