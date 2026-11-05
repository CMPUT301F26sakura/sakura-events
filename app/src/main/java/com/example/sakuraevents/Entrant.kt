package com.example.sakuraevents

import kotlin.collections.mutableListOf


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

class Entrant (UID: String = "",
   name: String,
   email: String,
   phone_number: String,
   private val _reservations: MutableList<Reservation> = mutableListOf<Reservation>(),
   private val _notifications: MutableList<Notification> = mutableListOf<Notification>()
): User(UID, name, email, phone_number) {
    val reservations: List<Reservation> get() = _reservations
    val notifications: List<Notification> get() = _notifications

    fun addReservation(reservation: Reservation) { // Add a Reservation object to campsite reservation list
        if (!_reservations.any { it === reservation }) {
            _reservations.add(reservation)
        }
    }
    fun removeReservation(reservation: Reservation) { // Remove a reservation object from campsite reserv. list
        _reservations.removeIf { it === reservation }
    }
    fun clearReservations() {
        _reservations.forEach { it.deleteReservation() }
    }
}