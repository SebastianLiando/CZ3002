package com.guavas.cz3002.extension.android

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.os.Build
import androidx.fragment.app.Fragment

/** Closes the activity the fragment is attached to. */
fun Fragment.closeActivity() = requireActivity().finish()

/** Programmatically presses the back button. */
fun Fragment.pressBackButton() = requireActivity().onBackPressed()

/**
 * Creates a notification channel. If the channel is already created, this will update the
 * notification channel.
 *
 * @param id The channel ID.
 * @param name The channel name.
 * @param importance The importance level.
 * @param customization Any other customization for the channel's behavior.
 */
fun Fragment.createNotificationChannel(
    id: String,
    name: String,
    importance: Int,
    customization: NotificationChannel.() -> Unit = {}
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(id, name, importance).apply(customization)

        val manager = requireActivity()
            .getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager

        manager.createNotificationChannel(channel)
    }
}