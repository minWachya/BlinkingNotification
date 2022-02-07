package com.example.blinkingnotification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.work.*
import com.google.firebase.messaging.RemoteMessage
import java.util.concurrent.TimeUnit

class AlarmWorker : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        intent?.let {
            val title = it.getStringExtra("title").toString()
            val message = it.getStringExtra("message").toString()
            // Create Notification Data
            val notificationData = Data.Builder()
                .putString("title", title)
                .putString("message", message)
                .build()

            // WorkManager 사용
            val workRequest =
                OneTimeWorkRequestBuilder<ScheduledWorker>()
                    .setInputData(notificationData)
                    .setBackoffCriteria(BackoffPolicy.LINEAR, 30000, TimeUnit.MILLISECONDS)
                    .build()

            val workManager = WorkManager.getInstance(context)
            workManager.enqueue(workRequest)
        }
    }
}

class ScheduledWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {

        // Get Notification Data
        val title = inputData.getString("title").toString()
        val message = inputData.getString("message").toString()
        // FCM 전송
//        NotificationUtil(applicationContext).showNotification(
//            title,
//            message
//        )
        val bundle = Bundle()
        bundle.putString("title", title)
        bundle.putString("message", message)
        bundle.putString("type", NotificationType.NORMAL.toString())
        val remoteMessage = RemoteMessage(bundle)
        val fcm = MyFirebaseMessagingService(applicationContext)
        fcm.onMessageReceived(remoteMessage)

        return Result.success()

    }
}