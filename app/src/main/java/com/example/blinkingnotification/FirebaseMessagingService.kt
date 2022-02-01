package com.example.blinkingnotification

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

// 알림 유형
enum class NotificationType(val title: String, val id: Int) {
    NORMAL("일반 알림", 0),
    EXPANDABLE("확장형 알림", 1),
    CUSTOM("커스텀 알림", 3),
}

class FirebaseMessagingService(val context: Context) : FirebaseMessagingService() {
    companion object {
        const val TAG = "mmmMessagingService"
        private const val CHANNEL_NAME = "Push Notification"
        private const val CHANNEL_DESCRIPTION = "Push Notification 을 위한 채널"
        private const val CHANNEL_ID = "Channel Id"
        private const val FLAG_UPDATE_CURRENT = 125
    }

    // 토큰 생성 메서드
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    // 메세지 수신 메서드
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "onMessageReceived() - remoteMessage : $remoteMessage")
        Log.d(TAG, "onMessageReceived() - from : ${remoteMessage.from}")
        Log.d(TAG, "onMessageReceived() - notification : ${remoteMessage.notification?.body}")

        val type = remoteMessage.data["type"]?.let { NotificationType.valueOf(it) } ?: kotlin.run {
            NotificationType.NORMAL  //type 이 null 이면 NORMAL type 으로 처리
        }
        val title = remoteMessage.data["title"]
        val message = remoteMessage.data["message"]

        Log.d(TAG, "onMessageReceived() - type : $type")
        Log.d(TAG, "onMessageReceived() - title : $title")
        Log.d(TAG, "onMessageReceived() - message : $message")

        sendNotification(type, title, message)
    }


    // 알림 발송 메서드
    private fun sendNotification(
        type: NotificationType,
        title: String?,
        message: String?
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Oreo(26) 이상 버전에는 channel 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = CHANNEL_DESCRIPTION
            notificationManager.createNotificationChannel(channel)
        }

        // 알림 생성
        NotificationManagerCompat.from(context)
            .notify((System.currentTimeMillis()/100).toInt(), createNotification(type, title, message))  //알림이 여러개 표시되도록 requestCode 를 추가
    }


    // 알림 생성 메서드
    @SuppressLint("WrongConstant", "UnspecifiedImmutableFlag")
    private fun createNotification(
        type: NotificationType,
        title: String?,
        message: String?
    ): Notification {

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("notificationType", " ${type.title} 타입 ")
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        // 알림이 여러개 표시되도록 requestCode 를 추가
        val pendingIntent = PendingIntent.getActivity(context, (System.currentTimeMillis()/100).toInt(), intent, FLAG_UPDATE_CURRENT)

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_eyes)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)  //알림 눌렀을 때 실행할 Intent 설정
            .setAutoCancel(true)  //클릭 시 자동으로 삭제되도록 설정

        //type 에 따라 style 설정
        when (type) {
            NotificationType.NORMAL -> Unit
            NotificationType.EXPANDABLE -> {
                notificationBuilder.setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("$message \n 😀 😃 😄 😁 😆 😅 😂 🤣 🥲  😐 😑 😬 🙄 😯 😦 😧 😮 😲 🥱 😴 🤤 😪 😵 🤐 🥴 🤢 🤮 🤧 😷 🤒 🤕")
                )
            }
//            NotificationType.CUSTOM -> {
//                notificationBuilder.setStyle(
//                    NotificationCompat.DecoratedCustomViewStyle()
//                )
//                    .setCustomContentView(
//                        RemoteViews(
//                            packageName,
//                            R.layout.view_custom_notification
//                        ).apply {
//                            setTextViewText(R.id.tv_custom_title, title)
//                            setTextViewText(R.id.tv_custom_message, message)
//                        }
//                    )
//            }
        }
        return notificationBuilder.build()
    }


}