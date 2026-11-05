package com.example.sakuraevents

import android.app.Notification

/**
 * Represents an entrant in the Sakura Events app.
 *
 * An entrant can join campsite waiting lists,
 * and make reservations, and rate campsites,
 * after completing a visit.
 *
 * @property UID the unique Id of the entrant.
 * @property userName  the name of the entrant.
 * @property userEmail  the email of the entrant.
 * @property phoneNumber  the phone number of the entrant.
 */

class Entrant (
    UID: String,
    userName: String,
    userEmail: String,
    phoneNumber: String,
    //val UID: String = ""
) : User(UID, userName, userEmail, phoneNumber){
    // Store the entrant reservations.
    val reservations = mutableListOf<Reservation>()
    val notifications = mutableListOf<Notification>()
    //TODO: Enable when the team message class is ready
    //val messages = mutableListOf<Message>()

    /**
     * Add the reservation to the entrant list
     */
    fun addReservation(reservation: Reservation ){
        reservations.add(reservation)
        // TODO : connect to reservation class when ready
    }

    /**
     * Remove a reservation from the entrant list
     *
     * @return reservation the reservation to remove
     */
    fun removeReservation(reservation: Reservation) {
        reservations.remove(reservation)
    }

    fun addNotification(notification: Notification) {
        notifications.add(notification)
    }


}

