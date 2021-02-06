package com.guavas.cz3002.service

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.Service
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.guavas.cz3002.R
import timber.log.Timber

// Token is not used for this application
@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class ViolationService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        message.notification?.let {
            Timber.d("Message Notification Body: ${it.body}")
            sendNotification(it)
        }
    }

    private fun sendNotification(remote: RemoteMessage.Notification) {
        val notification =
            NotificationCompat.Builder(this, getString(R.string.violation_notification_channel_id))
//                .setStyle(
//                    NotificationCompat.BigPictureStyle()
//                        .bigLargeIcon(null)
//                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_warning)
                .setContentTitle(remote.title)
                .setContentText(remote.body)
                .build()

        val manager = getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val NOTIFICATION_ID = 1234
    }
}