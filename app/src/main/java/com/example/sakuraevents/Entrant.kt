package com.example.sakuraevents





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

class Entrant (val UID: String = "",
               private var name: String,
               private var email: String,
               private var phone_number: String,
               private val _reservations: MutableList<Reservation>,
               private val _notifications: MutableList<Notification>) {
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
    fun getName(): String{
        return name
    }
    fun setName(new_name: String): Unit{
        name = new_name
    }
    fun getEmail(): String{
        return email
    }
    fun setEmail(new_email: String): Unit{
        email = new_email
    }
    fun getPhoneNumber(): String{
        return phone_number
    }
    fun setPhoneNumber(new_phone_number: String): Unit{
        phone_number = new_phone_number
    }
}