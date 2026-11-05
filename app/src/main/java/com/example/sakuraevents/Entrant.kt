package com.example.sakuraevents

class Entrant (val UID: String = "",
               private val name: String,
               private val email: String,
               private val phone_number: String,
               private val _reservations: MutableList<Reservation>,
               private val _notifications: MutableList<Notification>) {
    val reservations: List<Reservation> get() = _reservations
    val notifications: List<Notification> get() = _notifications
    fun addReservation(reservation: Reservation){}
    fun removeReservation(reservation: Reservation){}
    fun clearReservations(){}
    fun getUserName(): String{return name}
    fun getUserEmail(): String{return email}
    fun getPhoneNumber(): String{return phone_number}
}