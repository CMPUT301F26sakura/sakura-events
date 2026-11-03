package com.example.sakuraevents

// Notification.kt: model class for notifications sent to entrants.

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
 * A notification sent to one or more entrants. It can't be edited after it is sent,
 * so it also works as a log for owners and admins.
 *
 * @property UID unique ID, set by the database controller.
 * @property notification_type the kind of notification.
 * @property sender_uid UID of the sending owner, or "system".
 * @property campsite_uid UID of the related campsite.
 * @property title short title shown to the entrant.
 * @property message full message shown to the entrant.
 * @property recipient_uids UIDs of the receiving entrants.
 * @property date_sent send date ("YYYY-MM-DD"), empty until sent.
 * @property is_sent whether it has been sent.
 */
class Notification(
    val UID: String = "",
    private var notification_type: NotificationType = NotificationType.OWNER_MESSAGE,
    private var sender_uid: String = "",
    private var campsite_uid: String = "",
    private var title: String = "",
    private var message: String = "",
    private val recipient_uids: MutableList<String> = mutableListOf(),
    private var date_sent: String = "",
    private var is_sent: Boolean = false
) {

    /** @return the notification type. */
    fun getNotificationType(): NotificationType {
        return notification_type
    }

    /** @return the sender's UID. */
    fun getSenderUid(): String {
        return sender_uid
    }

    /** @return the campsite's UID. */
    fun getCampsiteUid(): String {
        return campsite_uid
    }

    /** @return the title. */
    fun getTitle(): String {
        return title
    }

    /** @return the message. */
    fun getMessage(): String {
        return message
    }

    /** @return a copy of the recipient UIDs. */
    fun getRecipientUids(): List<String> {
        return recipient_uids.toList()
    }

    /** @return the send date, or "" if not sent. */
    fun getDateSent(): String {
        return date_sent
    }

    /** @return true if sent. */
    fun isSent(): Boolean {
        return is_sent
    }

    /** Sets the type. @return false if already sent. */
    fun setNotificationType(new_type: NotificationType): Boolean {
        if (is_sent) {
            return false
        }
        notification_type = new_type
        return true
    }

    /** Sets the title. @return false if blank or already sent. */
    fun setTitle(new_title: String): Boolean {
        if (is_sent || new_title.isBlank()) {
            return false
        }
        title = new_title
        return true
    }

    /** Sets the message. @return false if blank or already sent. */
    fun setMessage(new_message: String): Boolean {
        if (is_sent || new_message.isBlank()) {
            return false
        }
        message = new_message
        return true
    }

    // The following functions (addRecipient, removeRecipient, hasRecipient) are from Anthropic, Claude, "How do I add and remove recipients from a list in a Kotlin class?", 2026-11-02
    /** Adds a recipient. @return false if blank, duplicate, or already sent. */
    fun addRecipient(entrant_uid: String): Boolean {
        if (is_sent || entrant_uid.isBlank() || recipient_uids.contains(entrant_uid)) {
            return false
        }
        recipient_uids.add(entrant_uid)
        return true
    }

    /** Removes a recipient. @return false if not found or already sent. */
    fun removeRecipient(entrant_uid: String): Boolean {
        if (is_sent) {
            return false
        }
        return recipient_uids.remove(entrant_uid)
    }

    /** @return true if the entrant is a recipient. */
    fun hasRecipient(entrant_uid: String): Boolean {
        return recipient_uids.contains(entrant_uid)
    }

    /**
     * Sends the notification, skipping entrants who opted out.
     *
     * @param send_date date in "YYYY-MM-DD" format.
     * @param opted_out_uids UIDs of entrants who opted out.
     * @return false if already sent, bad date, blank title/message, or no recipients left.
     */
    fun send(send_date: String, opted_out_uids: List<String> = emptyList()): Boolean {
        if (is_sent || !isValidDate(send_date) || title.isBlank() || message.isBlank()) {
            return false
        }
        recipient_uids.removeAll(opted_out_uids)
        if (recipient_uids.isEmpty()) {
            return false
        }
        date_sent = send_date
        is_sent = true
        return true
    }

    // Checks the date is "YYYY-MM-DD" with a valid month and day.
    private fun isValidDate(date: String): Boolean {
        val date_pattern = Regex("""^\d{4}-(\d{2})-(\d{2})$""")
        val match = date_pattern.matchEntire(date) ?: return false
        val month = match.groupValues[1].toInt()
        val day = match.groupValues[2].toInt()
        return month in 1..12 && day in 1..31
    }
}