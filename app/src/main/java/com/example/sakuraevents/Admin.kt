

package com.example.sakuraevents

/**
 * Represents an admin user in the app.
 *
 * Admins can view and remove campsites, profiles, and images.
 * They can also use the app as an entrant or campsite owner.
 *
 *
 * Reworked the admin code to use the team's shared classes instead of separate
 * admin models. FirestoreDriver will handle loading and saving objects to Firebase, so
 * Admin.kt will only contain admin-related functions. The database methods still need
 *to be connected once the driver is finished.
 */
class Admin (
    UID: String = "",
    name: String,
    email: String,
    phone_number: String,
    private val _reservations: MutableList<Reservation> = mutableListOf<Reservation>(),
    private val _notifications: MutableList<Notification> = mutableListOf<Notification>(),
    private var _owned_campsites: MutableList<Campsite> = mutableListOf()
): User(UID, name, email, phone_number){
    val reservations: List<Reservation> get() = _reservations
    val notifications: List<Notification> get() = _notifications
    val owned_campsites: List<Campsite> get() = _owned_campsites

}
