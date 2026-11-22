

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
    role: Role = Role.ADMIN,
    name: String,
    email: String,
    phone_number: String,
    _reservations: MutableList<Reservation> = mutableListOf<Reservation>(),
    _notifications: MutableList<Notification> = mutableListOf<Notification>(),
    _owned_campsites: MutableList<Campsite> = mutableListOf(),
    is_entrant: Boolean = (role == Role.ENTRANT),
    is_owner: Boolean = (role == Role.OWNER),
    is_admin: Boolean = (role == Role.ADMIN)
): CampsiteOwner(UID, role, name, email, phone_number, _reservations, _notifications, _owned_campsites, is_entrant, is_owner, is_admin) {

}
