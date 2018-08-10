package com.ping.android.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.text.TextUtils
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.bzzzchat.cleanarchitecture.DefaultObserver
import com.bzzzchat.configuration.GlideApp
import com.google.firebase.storage.FirebaseStorage
import com.ping.android.App
import com.ping.android.R
import com.ping.android.domain.usecase.GetCurrentUserUseCase
import com.ping.android.domain.usecase.notification.ShowIncomingMessageNotificationUseCase
import com.ping.android.domain.usecase.notification.ShowMissedCallNotificationUseCase
import com.ping.android.model.Callback
import com.ping.android.model.User
import com.ping.android.presentation.view.activity.ChatActivity
import com.ping.android.presentation.view.activity.SplashActivity
import com.ping.android.utils.ActivityLifecycle
import com.ping.android.utils.BadgeHelper
import com.ping.android.utils.Log
import com.ping.android.utils.SharedPrefsHelper
import com.quickblox.messages.services.fcm.QBFcmPushListenerService
import org.json.JSONException
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class FbMessagingService: QBFcmPushListenerService() {
    private lateinit var badgeHelper: BadgeHelper
    private var mNotificationId: Int = 0
    private var mConversationId: String? = null
    @Inject
    internal lateinit var getCurrentUserUseCase: GetCurrentUserUseCase
    @Inject
    internal lateinit var showMissedCallNotificationUseCase: ShowMissedCallNotificationUseCase
    @Inject
    internal lateinit var showIncomingMessageNotificationUseCase: ShowIncomingMessageNotificationUseCase


    override fun onCreate() {
        super.onCreate()
        badgeHelper = BadgeHelper(this)
        (applicationContext as App).component.inject(this)
    }

    override fun sendPushMessage(data: MutableMap<Any?, Any?>?, from: String?, message: String?) {
        if (data != null) {
            val notificationType = data["notificationType"] as String
            if (TextUtils.equals(notificationType, "incoming_call")) {
                val qbId = SharedPrefsHelper.getInstance().get<Int>("quickbloxId")
                val pingId = SharedPrefsHelper.getInstance().get<String>("pingId")
                CallService.start(this, qbId, pingId)
                return
            }

            val conversationId = data["conversationId"] as? String ?: ""
            val senderProfile = data["senderProfile"] as? String ?: ""
            Log.d("new message: $message$conversationId$notificationType")
            if (TextUtils.equals(notificationType, "missed_call")) {
                var isVideo = 0
                try {
                    isVideo = Integer.parseInt(data["isVideo"] as String)
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }

                val senderId = data["senderId"] as String
                this.badgeHelper.increaseMissedCall()
                showMissedCallNotificationUseCase
                        .execute(DefaultObserver(),
                                ShowMissedCallNotificationUseCase.Params(
                                        senderId,
                                        senderProfile,
                                        message,
                                        isVideo == 1
                                ))
            } else if (TextUtils.equals(notificationType, "incoming_message")) {
                if (!needDisplayNotification(conversationId)) {
                    return
                }
                this.badgeHelper.increaseBadgeCount(conversationId)
                showIncomingMessageNotificationUseCase.execute(DefaultObserver(),
                        ShowIncomingMessageNotificationUseCase.Params(message, conversationId, senderProfile))
            } else if (TextUtils.equals(notificationType, "game_status")) {
                if (!needDisplayNotification(conversationId)) {
                    return
                }
                getCurrentUserUseCase.execute(object : DefaultObserver<User>() {
                    override fun onNext(user: User) {
                        try {
                            postNotification(this@FbMessagingService, user, message!!, conversationId, senderProfile)
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    }
                }, null)
            }
        }
    }

    @Throws(JSONException::class)
    private fun postNotification(context: Context, currentUser: User?, message: String,
                                 conversationId: String, profileImage: String) {
        val soundNotification = currentUser == null || currentUser.settings.notification
        mNotificationId = getID()
        mConversationId = conversationId

        // 3. Build notification
        val notificationBuilder = NotificationCompat.Builder(context)
        // Create pending intent, mention the Activity which needs to be
        val intent = Intent(context, SplashActivity::class.java)
        intent.putExtra(ChatActivity.CONVERSATION_ID, conversationId)
        intent.addFlags(Intent.FLAG_FROM_BACKGROUND)
        val contentIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)

        notificationBuilder.setContentText(message).setContentIntent(contentIntent).setAutoCancel(true)//setShowWhen(true).
        //setWhen(0).
        if (ActivityLifecycle.getInstance().isForeground && !soundNotification) {
            notificationBuilder.setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE)
        } else {
            notificationBuilder.setDefaults(Notification.DEFAULT_ALL)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.priority = NotificationManager.IMPORTANCE_HIGH
        } else {
            notificationBuilder.priority = Notification.PRIORITY_HIGH
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            notificationBuilder
                    .setContentTitle(context.resources.getString(R.string.app_name))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            prepareProfileImage(context, profileImage, Callback { error, data ->
                if (error != null) {
                    notificationBuilder.setSmallIcon(R.drawable.ic_notification).setColor(context.resources.getColor(R.color.colorAccent)).setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
                } else {
                    notificationBuilder.setSmallIcon(R.drawable.ic_notification).setColor(context.resources.getColor(R.color.colorAccent)).setLargeIcon(data[0] as Bitmap)
                }
                sendNotification(notificationBuilder)
            })
        } else {
            notificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
            sendNotification(notificationBuilder)
        }

    }

    private fun sendNotification(builder: NotificationCompat.Builder) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var channel: NotificationChannel? = notificationManager.getNotificationChannel("channel0")
            if (channel == null) {
                channel = NotificationChannel("channel0", "channel0", NotificationManager.IMPORTANCE_HIGH)
                channel.enableLights(true)
                channel.lightColor = Color.GREEN
                channel.enableVibration(true)
                channel.setShowBadge(true)
                notificationManager.createNotificationChannel(channel)
            }
            builder.setChannelId("channel0")
        }

        notificationManager.notify(mNotificationId, builder.build())
    }

    private fun prepareProfileImage(context: Context, profileImage: String, callback: Callback) {
        if (!TextUtils.isEmpty(profileImage) && profileImage.startsWith("gs://")) {
            val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(profileImage)
            val target = object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    callback.complete(null, resource)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    callback.complete(Error())
                }
            }
            GlideApp.with(context)
                    .asBitmap()
                    .override(100)
                    .apply(RequestOptions.circleCropTransform())
                    .load(gsReference)
                    .into<SimpleTarget<Bitmap>>(target)
        } else {
            callback.complete(Error())
        }
    }

    private fun needDisplayNotification(conversationId: String): Boolean {
        val activeActivity = ActivityLifecycle.getInstance().foregroundActivity
        val isForeground = ActivityLifecycle.getInstance().isForeground
        // do not display notification if opponentUser already logged out
        // Note: use this params to detect in case of user reinstall app
        if (!SharedPrefsHelper.getInstance().get("isLoggedIn", false)) {
            Log.d("opponentUser not logged-in")
            return false
        }
        //do not display notification if opponentUser is opening same conversation
        return if (activeActivity != null && activeActivity is ChatActivity && isForeground) {
            conversationId != activeActivity.conversationId
        } else true
    }

    companion object {
        @JvmStatic
        private val c = AtomicInteger(0)

        fun getID(): Int {
            return c.incrementAndGet()
        }
    }
}