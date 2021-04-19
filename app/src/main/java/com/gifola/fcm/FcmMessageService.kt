package com.gifola.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import com.gifola.MainActivity
import com.gifola.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FcmMessageService: FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        // If required send token to your app server.
    }
    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        //Use this condition to validation login
         if (remoteMessage.data.isNotEmpty()){
            val extras = Bundle()
            for ((key, value) in remoteMessage.data) {
                extras.putString(key, value)
            }
            if(extras.containsKey("message") && !extras.getString("message").isNullOrBlank()) {
                sendNotification(extras.getString("message")!!)
            }
        }else {
             sendNotification(remoteMessage.notification?.body ?: "")
         }
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private fun sendNotification(messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        var notificationBuilder: NotificationCompat.Builder? = null
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                packageName,
                packageName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = packageName
            notificationManager.createNotificationChannel(channel)
            if (notificationBuilder == null) {
                notificationBuilder = NotificationCompat.Builder(application, packageName)
            }
        } else {
            if (notificationBuilder == null) {
                notificationBuilder = NotificationCompat.Builder(application,packageName)
            }
        }
        notificationBuilder.setSmallIcon(R.drawable.bell)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "FcmMessageService"
    }
}