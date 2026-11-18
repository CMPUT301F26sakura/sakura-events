/**
 * File: Entrant.kt
 * Purpose: Defines the Role enumeration and the base Entrant user model for joining
 * campsite lottery waitlists, receiving lottery notifications, and managing profile details.
 *
 * User Stories Addressed:
 * - US 01.02.01: As an entrant, I want to join the waiting list for a campsite lottery.
 * - US 01.02.02: As an entrant, I want to leave the waiting list for a campsite lottery.
 * - US 01.03.01: As an entrant, I want to receive a notification when I win the campsite lottery.
 * - US 01.03.02: As an entrant, I want to receive a notification when I lose the campsite lottery.
 * - US 01.07.03: As an entrant, I want to update my personal information, such as name, email, and phone number, on my profile.
 * - US 01.09.01: As an entrant, I want to be identified by my device, so that I do not have to use a username and password.
 */

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
 * An entrant can join and leave campsite waiting lists, receive lottery notifications,
 * make reservations, and rate campsites after completing a visit.
 *
 * @property UID The unique ID of the entrant (can be tied to device/anonymous auth ID for US 01.09.01).
 * @property role The [Role] assigned to the user.
 * @property name The full name of the entrant.
 * @property email The email address of the entrant.
 * @property phone_number The phone number of the entrant.
 * @property _reservations Backing mutable list of reservations belonging to the entrant.
 * @property _notifications Backing mutable list of notifications delivered to the entrant.
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

    /**
     * Adds a [Reservation] to the entrant's reservation list if not already present.
     * @param reservation The [Reservation] object to add.
     */
    fun addReservation(reservation: Reservation) { // Add a Reservation object to campsite reservation list
        if (!_reservations.any { it === reservation }) {
            _reservations.add(reservation)
        }
    }
    /**
     * Removes a [Reservation] from the entrant's reservation list.
     * @param reservation The [Reservation] object to remove.
     */
    fun removeReservation(reservation: Reservation) { // Remove a reservation object from campsite reserv. list
        _reservations.removeIf { it === reservation }
    }
    // Deletes all reservations associated with this entrant prior to profile deletion.
    fun clearReservations() {
        _reservations.forEach { it.deleteReservation() }
    }
    /**
     * Returns the string representation of the user's [Role].
     * @return Role name string ("ENTRANT", "OWNER", or "ADMIN").
     */
    fun getRole(): String {
        return role.toString()
    }
    /**
     * Returns the entrant's full name.
     * @return Name string.
     */
    fun getName(): String {
        return name
    }
    /**
     * Returns the entrant's phone number.
     * @return Phone number string.
     */
    fun getPhoneNumber(): String {
        return phone_number
    }
    /**
     * Returns the entrant's email address.
     * @return Email string.
     */
    fun getEmail(): String {
        return email
    }
    /**
     * Updates the entrant's name (US 01.07.03).
     * @param newName The new name to assign.
     */
    fun setName(newName: String) {
        name = newName
    }
    /**
     * Updates the entrant's email address (US 01.07.03).
     * @param newEmail The new email address to assign.
     */
    fun setEmail(newEmail: String) {
        email = newEmail
    }
    /**
     * Updates the entrant's phone number (US 01.07.03).
     * @param newPhoneNumber The new phone number to assign.
     */
    fun setPhoneNumber(newPhoneNumber: String) {
        phone_number = newPhoneNumber
    }
    /**
     * Delivers a [Notification] to this entrant's notification list if not already present
     * (US 01.03.01, US 01.03.02).
     * @param notification The [Notification] to add.
     */
    fun addNotification(notification: Notification) {
        if (!_notifications.any { it === notification })
            _notifications.add(notification)
    }

}