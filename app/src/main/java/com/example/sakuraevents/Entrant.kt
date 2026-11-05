package com.example.sakuraevents

import kotlin.collections.mutableListOf

enum class Role {
    ENTRANT,
    OWNER,
    ADMIN,
}

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

open class Entrant (val UID: String= "",
                    private val role: Role,
                    private var name: String,
                    private var email: String,
                    private var phone_number: String,
                    private val _reservations: MutableList<Reservation> = mutableListOf<Reservation>(),
                    private val _notifications: MutableList<Notification> = mutableListOf<Notification>()){

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

    fun getRole(): String {
        return role.toString()
    }
    fun getName(): String {
        return name
    }
    fun getPhoneNumber(): String {
        return phone_number
    }
    fun getEmail(): String {
        return email
    }

    fun setName(newName: String) {
        name = newName
    }
    fun setEmail(newEmail: String) {
        email = newEmail
    }
    fun setPhoneNumber(newPhoneNumber: String) {
        phone_number = newPhoneNumber
    }
    fun addNotification(notification: Notification) {
        if (!_notifications.any { it === notification })
            _notifications.add(notification)
    }

}