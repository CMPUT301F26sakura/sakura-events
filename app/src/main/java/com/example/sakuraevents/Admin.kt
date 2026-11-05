

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
    _reservations: MutableList<Reservation> = mutableListOf<Reservation>(),
    _notifications: MutableList<Notification> = mutableListOf<Notification>(),
    _owned_campsites: MutableList<Campsite> = mutableListOf()
): CampsiteOwner(UID, name, email, phone_number, _reservations, _notifications, _owned_campsites){

}
