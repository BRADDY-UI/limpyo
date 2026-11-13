package com.limpyo.app.core.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

/**
 * Receives pushes from Firebase Cloud Messaging.
 * Phase 0: only logs. Phase 3 adds token upload, notification channels, dedup and deep links.
 */
class LimpyoMessagingService : FirebaseMessagingService() {
    /** Called when this device gets (or refreshes) its push token. Only the first 8 characters are logged. */
    override fun onRegistered(token: String) {
        Timber.tag("LimpyoPush").i("FCM ready, token starts with %s", token.take(8))
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Timber.tag("LimpyoPush").i("Push received, id=%s", message.messageId)
    }
}
