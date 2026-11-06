

package com.example.sakuraevents

import com.google.android.gms.tasks.Task

/**
 * File: Admin.kt
 * Purpose: Model class representing an admin user who can manage
 * campsites and user profiles. Admin also inherits entrant and
 * campsite owner functionality.
 *
 * User Stories Addressed:
 * - US 03.01.01: As an admin, I want to remove campsites.
 * - US 03.02.01: As an admin, I want to remove profiles.
 * - US 03.11.01: As an admin, I want to also be an entrant or campsite owner.
 *
 *
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
