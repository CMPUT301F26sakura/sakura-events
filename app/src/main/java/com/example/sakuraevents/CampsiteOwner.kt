/**
 * File: CampsiteOwner.kt
 * Purpose: Defines the CampsiteOwner model, which stores an owner's contact
 * information and the campsites they own and manage.
 *
 * User Stories Addressed:
 * - US 02.01.01: As a campsite owner, I want to register my campsite with the app.
 * - US 02.01.02: As a campsite owner, I want to provide campsite details including name, location, description (amenities), maximum occupancy, map location, optional image, and tags.
 * - US 02.01.03: As a campsite owner, I want to update details about my campsite.
 * - US 02.01.04: As a campsite owner, I want to view details of my campsite.
 * - US 02.03.01: As a campsite owner, I want to view a list of my campsites.
 */
package com.example.sakuraevents

/**
 * Represents a campsite owner who can register and manage campsites.
 *
 * A campsite owner has a unique ID, a name, an email, a phone number, and a list of
 * the campsites they own. The campsite references allow the owner to add and remove
 * campsites as they register or delete them.
 *
 * @property UID the unique ID of this owner, used to find them in the database.
 * @property name the full name of the owner.
 * @property email the email address of the owner.
 * @property phone_number the phone number of the owner, stored as a string (eg: '780-555-1234').
 * @property _owned_campsites the private, editable list of references to the Campsites this owner has registered.
 *      Only this class can change it, through addCampsite and removeCampsite.
 * @property owned_campsites a public, read-only view of _owned_campsites. Other code can read it but not change it,
 *      and it always shows the owner's current list.
 */
open class CampsiteOwner(
    UID: String = "",
    role: Role,
    name: String,
    email: String,
    phone_number: String,
    _reservations: MutableList<Reservation> = mutableListOf<Reservation>(),
    _notifications: MutableList<Notification> = mutableListOf<Notification>(),
    private var _owned_campsites: MutableList<Campsite> = mutableListOf()
): Entrant(UID, role, name, email, phone_number, _reservations, _notifications) {

    val owned_campsites: List<Campsite> get() = _owned_campsites

    /**
     * This method adds a campsite to this owner's list when they register a new campsite.
     * If this exact Campsite object is already in the list, it is not added again.
     *
     * @param campsite a reference to the Campsite to add.
     * @return Nothing
     */
    fun addCampsite(campsite: Campsite) {
        if (!_owned_campsites.any { it === campsite }) {
            _owned_campsites.add(campsite)
        }
    }

    /**
     * This method removes a campsite from this owner's list.
     * If the Campsite is not in the list, nothing happens.
     *
     * @param campsite a reference to the Campsite to remove.
     * @return Nothing
     */
    fun removeCampsite(campsite: Campsite) {
        _owned_campsites.removeIf { it === campsite }
    }
}