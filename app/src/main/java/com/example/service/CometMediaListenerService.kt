package com.example.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class CometMediaListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        ActiveMediaManager.notifyListenerConnected(this)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        ActiveMediaManager.notifyListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        ActiveMediaManager.checkActiveSessions()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        ActiveMediaManager.checkActiveSessions()
    }
}
