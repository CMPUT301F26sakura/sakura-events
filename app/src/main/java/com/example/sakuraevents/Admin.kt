

package com.example.sakuraevents

import com.google.android.gms.tasks.Task

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
    role: Role,
    name: String,
    email: String,
    phone_number: String,
    _reservations: MutableList<Reservation> = mutableListOf<Reservation>(),
    _notifications: MutableList<Notification> = mutableListOf<Notification>(),
    _owned_campsites: MutableList<Campsite> = mutableListOf()
): CampsiteOwner(UID, role, name, email, phone_number, _reservations, _notifications, _owned_campsites) {


    /**
     * Removes a registered campsite from Firebase.
     *
     * @param campsite The campsite to remove.
     * @param firestore_driver The shared database controller.
     * @return The Firebase task for the deletion.
     */
    fun removeCampsite(
        campsite: Campsite,
        firestore_driver: FirestoreDriver
    ): Task<Void> {

        // The driver uses the campsite's UID to delete its document.
        return firestore_driver.deleteCampsite(campsite)
    }


    /**
     * Removes a user profile from Firebase.
     *
     * @param profile The profile to remove.
     * @param firestore_driver The shared database controller.
     * @return The Firebase task for the deletion.
     */
    fun removeProfile(
        profile: Entrant,
        firestore_driver: FirestoreDriver
    ): Task<Void> {
        // Check the user's type to find the correct database collection.
        // Admin is checked first because it inherits from CampsiteOwner
        // and Entrant, so it also counts as both of those types.
        return when (profile) {
            is Admin -> firestore_driver.deleteAdmin(profile)
            is CampsiteOwner -> firestore_driver.deleteOwner(profile)
            else -> firestore_driver.deleteEntrant(profile)
        }
    }
}