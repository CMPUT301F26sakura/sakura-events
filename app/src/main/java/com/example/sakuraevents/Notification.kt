package com.example.sakuraevents


// Notification.kt: model class for notifications sent to entrants.
// Written with help from Anthropic, Claude, "Kotlin Notification model class", 2026-11-04

/** The kinds of notification the app can send. */
enum class NotificationType {
    LOTTERY_WIN,
    LOTTERY_LOSS,
    RESERVATION_CONFIRMED,
    CANCELLATION_CONFIRMED,
    RESERVATION_REJECTED,
    WAITLIST_MESSAGE,
    OWNER_MESSAGE
}

/**
 * A notification created when a campsite owner hits send. It can't be changed after
 * it is created, so it also works as a log for owners and admins.
 *
 * @property UID unique ID, used by Firestore.
 * @property notification_type the kind of notification.
 * @property sender the campsite owner who sent it.
 * @property campsite the campsite it is about.
 * @property title short title shown to the entrant.
 * @property message full message shown to the entrant.
 * @property recipients the entrants who receive it.
 * @property date_sent send date ("YYYY-MM-DD"), set by the system.
 */
class Notification(
    val UID: String = "",
    private val notification_type: NotificationType = NotificationType.OWNER_MESSAGE,
    private val sender: CampsiteOwner = CampsiteOwner(),
    private val campsite: Campsite = Campsite(),
    private val title: String = "",
    private val message: String = "",
    private val _recipients: List<Entrant> = listOf(),
    private val date_sent: String = ""
) {
    val recipients: List<Entrant> get() = _recipients

    /** @return the notification type. */
    fun getNotificationType(): NotificationType {
        return notification_type
    }

    /** @return the campsite owner who sent it. */
    fun getSender(): CampsiteOwner {
        return sender
    }

    /** @return the campsite it is about. */
    fun getCampsite(): Campsite {
        return campsite
    }

    /** @return the title. */
    fun getTitle(): String {
        return title
    }

    /** @return the message. */
    fun getMessage(): String {
        return message
    }

    /** @return the send date. */
    fun getDateSent(): String {
        return date_sent
    }

    /**
     * Delivers this notification to every recipient who has not opted out.
     *
     * @param opted_out entrants who opted out of notifications.
     * @return the entrants it was delivered to.
     */
    fun send(opted_out: List<Entrant> = listOf()): List<Entrant> {
        val delivered = _recipients.filter { recipient -> opted_out.none { it === recipient } }
        for (entrant in delivered) {
            entrant.addNotification(this)
        }
        return delivered
    }
}