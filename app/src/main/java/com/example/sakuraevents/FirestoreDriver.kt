/**
 * File: FirestoreDriver.kt
 * Purpose: Manages Firebase Firestore persistence, real-time snapshot listeners,
 * device-based anonymous authentication, and object serialization/deserialization
 * for campsites, entrants, owners, admins, reservations, lotteries, and notifications.
 *
 * User Stories Addressed:
 * - US 01.01.03, US 01.01.04, US 01.01.05, US 01.01.06: Load and query campsites and details.
 * - US 01.02.01, US 01.02.02: Persist joining and leaving campsite lottery waiting lists.
 * - US 01.03.01, US 01.03.02: Persist lottery win and loss notifications.
 * - US 01.09.01: Identify entrant by device via Firebase Anonymous Authentication / device ID.
 * - US 02.01.01, US 02.01.02, US 02.01.03, US 02.01.04, US 02.02.01, US 02.03.01: Create, view, and update campsites.
 * - US 02.04.01, US 02.04.02, US 02.04.03, US 02.04.04, US 02.04.05, US 02.04.06, US 02.04.07, US 02.05.01: Manage lottery listings and draw winners.
 * - US 03.01.01, US 03.02.01, US 03.03.01, US 03.04.01, US 03.05.01, US 03.11.01: Admin browsing and removal operations.
 */

package com.example.sakuraevents

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import kotlin.to
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue


/**
 * This class handles the process of converting the various classes to and from their firestore
 * forms.
 */
class FirestoreDriver {

    private val _reservations = mutableStateMapOf<String, Reservation>()
    // The following _campsites and _owners lines were written with help from Anthropic, Claude (Claude Code), "Add owner campsite support", 2026-11-05
    // mutableStateMapOf lets Compose screens redraw automatically when these maps change.
    private val _campsites = mutableStateMapOf<String, Campsite>()
    private val _entrants = mutableStateMapOf<String, Entrant>()
    private val _owners = mutableStateMapOf<String, CampsiteOwner>()
    private val _admins = mutableStateMapOf<String, Admin>()
    // The notifications lines below were written with help from Anthropic, Claude, "Save and load notifications in Firestore", 2026-11-25
    private val _notifications = mutableStateMapOf<String, Notification>()
    // Raw notification documents, kept so notifications can be rebuilt once users and campsites have loaded.
    private val _notification_docs = mutableMapOf<String, Map<String, Any>>()
    //private val _lotteries = mutableMapOf<String, Lottery>()

    val reservations: Map<String, Reservation> get() = _reservations
    val campsites: Map<String, Campsite> get() = _campsites
    val entrants: Map<String, Entrant> get() = _entrants
    val owners: Map<String, CampsiteOwner> get() = _owners
    val admins: Map<String, Admin> get() = _admins
    val notifications: Map<String, Notification> get() = _notifications
    // val lotteries: Map<String, Lottery> get() = _lotteries

    val db = Firebase.firestore
    // private val auth = FirebaseAuth.getInstance().signInAnonymously()
    private val reservations_ref = db.collection("reservations")
    private val campsites_ref = db.collection("campsites")
    private val entrants_ref = db.collection("entrants")
    private val owners_ref = db.collection("owners")
    private val admins_ref = db.collection("admins")
    private val notifications_ref = db.collection("notifications")
    private val lotteries_ref = db.collection("lotteries")

    // Name: Devesh Darji
    // The following function / lines of code were generated / modified by Google Gemini Neon Model.
    // Prompt: "How do I store the logged-in user and owned campsites as Compose state in FirestoreDriver so the UI updates live when roles change?" 2026-11-22
    // Added Compose-observable maps/state (_all_users, _user_owned_campsite_uids, current_user_uid, current_user, users_ref) and getOwnedCampsites().
    private val _all_users = mutableStateMapOf<String, Admin>()
    private val _user_owned_campsite_uids = mutableStateMapOf<String, List<String>>()

    val all_users: Map<String, Admin> get() = _all_users
    fun getOwnedCampsites(uid: String?): List<Campsite> {
        if (uid == null) return emptyList()
        return _user_owned_campsite_uids[uid]?.mapNotNull { _campsites[it] } ?: emptyList()
    }

    var current_user_uid by mutableStateOf<String?>(null)
        private set
    var current_user by mutableStateOf<Admin?>(null)
        private set

    private val users_ref = db.collection("users")


    /**
     *
     */
    init {
        //Tasks.await(auth.signInAnonymously())


        // Each of these ref.addSnapshotListener are what handle both assembling the arrays and updating them.
        // This code is adapted from the lab05 listyCity code. It is mostly modified in how it retrieves
        // the info from the document file because parts of our classes don't serialize cleanly.
        //assert(false)

        // Name: Devesh Darji
        // The following function / lines of code were generated / modified by Google Gemini Neon Model.
        // Prompt: "How do I use Firebase Anonymous Auth with a single users collection and boolean role flags, and prevent snapshot listener crashes?" 2026-11-22
        // Implemented anonymous sign-in with automatic default entrant creation, a unified users_ref listener populating role maps via has_*_access, and null-safe lookups in campsites_ref and reservations_ref.
        FirebaseAuth.getInstance().signInAnonymously().addOnSuccessListener { result ->
            val uid = result.user?.uid ?: return@addOnSuccessListener
            current_user_uid = uid
            current_user = _all_users[uid]

            val user_doc = users_ref.document(uid)
            user_doc.get().addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    user_doc.set(mapOf(
                        "name" to "New User",
                        "email" to "",
                        "phone_number" to "",
                        "role" to Role.ENTRANT.name,
                        "owned_campsites" to emptyList<String>(),
                        "notifications" to emptyList<String>(),
                        "notifications_enabled" to true
                    ))
                }
            }
        }

        users_ref.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            _all_users.clear()
            _entrants.clear()
            _owners.clear()
            _admins.clear()
            _user_owned_campsite_uids.clear()

            snapshot?.documents?.forEach { document ->
                val data = document.data ?: return@forEach
                val role_string = data["role"] as? String ?: Role.ENTRANT.name
                val parsed_role = try {
                    Role.valueOf(role_string.uppercase())
                } catch (e: IllegalArgumentException) {
                    Role.ENTRANT
                }
                val owned_uids = (data["owned_campsites"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                _user_owned_campsite_uids[document.id] = owned_uids

                val user = Admin(
                    UID = document.id,
                    role = parsed_role,
                    name = data["name"] as? String ?: "",
                    email = data["email"] as? String ?: "",
                    phone_number = data["phone_number"] as? String ?: "",
                    _owned_campsites = owned_uids.mapNotNull { _campsites[it] }.toMutableList()
                )

                user.setNotificationsEnabled(data["notifications_enabled"] as? Boolean ?: true)

                _all_users[document.id] = user
                if (user.has_entrant_access) _entrants[document.id] = user
                if (user.has_owner_access) _owners[document.id] = user
                if (user.has_admin_access) _admins[document.id] = user
            }

            current_user = current_user_uid?.let { _all_users[it] }
            rebuildNotifications()
        }

        campsites_ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            _campsites.clear()
            snapshot?.documents?.forEach() { document ->
                val campsite: Map<String, Any>? = document.data
                if (campsite != null) {
                    val ratings: MutableList<Pair<Entrant, Float>>
                    // I would prefer to do every property inline, but this one was too complex.
                    if ((campsite["ratings"] as List<*>).isEmpty()) {
                        ratings = mutableListOf<Pair<Entrant, Float>>()
                    }
                    else {
                        ratings = (campsite["ratings"] as List<*>).flatMap { map ->
                            // Read all ratings from the map.
                            (map as Map<*, *>).entries.mapNotNull { entry ->
                                // Find the entrant who submitted the rating.
                                val entrant = _entrants[entry.key as? String]
                                val rating = (entry.value as? Number)?.toFloat()
                                // Only use valid ratings.
                                if (entrant != null && rating != null) { entrant to rating } else { null } } }.toMutableList() }
                    _campsites[document.id] = Campsite(
                        UID = document.id,
                        name = campsite["name"] as String,
                        location = campsite["location"] as String,
                        description = campsite["description"] as String,
                        max_occupancy = (campsite["max_occupancy"] as Long).toInt(),
                        qr_code = campsite["qr_code"] as String,
                        price = (campsite["price"] as Double).toFloat(),
                        _tags = ((campsite["tags"] as List<*>).map { it as String }).toMutableList(),
                        _booking_period_dates = ((campsite["booking_period_dates"] as List<*>).map { it as String }).toMutableList(),
                        _reservations = mutableListOf<Reservation>(),
                        // The above is created without values because the reservations themselves handle adding themselves to this list.
                        _ratings = ratings,
                        average_rating = (campsite["average_rating"] as String).toFloat(),
                        _waiting_list = (campsite["waiting_list"] as List<*>).mapNotNull { (it as? String)?.let { uid -> _all_users[uid] } }.toMutableList(),
                        waitlist_capacity = (campsite["waitlist_capacity"] as Long).toInt()
                    )
                    _all_users.forEach { (uid, user) ->
                        _user_owned_campsite_uids[uid]?.mapNotNull { _campsites[it] }?.forEach { user.addCampsite(it) }
                    }
                }
            }
            rebuildNotifications()
        }

        // Loads notifications (US 01.03.01, US 01.03.02, US 02.10.01, US 02.10.02).
        notifications_ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            _notification_docs.clear()
            snapshot?.documents?.forEach { document ->
                val data = document.data ?: return@forEach
                _notification_docs[document.id] = data
            }
            rebuildNotifications()
        }

        reservations_ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            _reservations.clear()
            snapshot?.documents?.forEach() { document ->
                val reservation: Map<String, Any>? = document.data
                if (reservation != null) {
                    // Safely read the IDs as Strings (if missing, skip this document)
                    val campsite_uid = reservation["campsite"] as? String ?: return@forEach
                    val entrant_uid = reservation["entrant"] as? String ?: return@forEach
                    // Look up the actual objects (if not loaded yet, skip this document)
                    val campsite = _campsites[campsite_uid] ?: return@forEach
                    val entrant = _all_users[entrant_uid] ?: return@forEach
                    // Pass the safe, non-null objects into Reservation(...)
                    _reservations[document.id] = Reservation(
                        UID =            document.id,
                        campsite =       campsite,
                        entrant =        entrant,
                        guest_count =    (reservation["guest_count"] as Long).toInt(),
                        // Skip reservations with missing dates.
                        beginning_date = reservation["beginning_date"] as? String ?: return@forEach,
                        end_date = reservation["end_date"] as? String ?: return@forEach,
                          reservation_state = ReservationState.entries.find {
                              it.name == reservation["reservation_state"]
                          } ?: ReservationState.NEW
                    )
                }
            }
        }
    }

    fun generateNextUid(source_map: Set<String>): String {
        val numeric_ids = source_map.mapNotNull { it.toIntOrNull() }
        return if (numeric_ids.isNotEmpty()) (numeric_ids.max() + 1).toString() else "0"
    }

    fun addReservation(reservation: Reservation): Task<Void> {
        val UID: String = if (reservation.UID.isNotBlank()) reservation.UID else generateNextUid(_reservations.keys)
        val firestore_format = mapOf<String, Any>( // Any type is a little dangerous, requires us to be disciplined in our typing.
            "campsite" to reservation.getCampsite().UID,
            "entrant" to reservation.getEntrant().UID,
            "guest_count" to reservation.getGuestCount(),
            "beginning_date" to reservation.getBeginningDate(),
            "end_date" to reservation.getEndDate(),
            "reservation_state" to reservation.getState())
        return reservations_ref.document(UID).set(firestore_format)
    }

    /**
     * US 01.04.03: Decline an unconfirmed reservation.
     * Only the entrant who owns the reservation can decline it.
     */
    fun declineReservation(reservationUID: String): Task<Void> {
        val entrantUID = current_user_uid
            ?: return Tasks.forException(
                IllegalStateException("User is not signed in")
            )

        val ref = reservations_ref.document(reservationUID)

        return db.runTransaction<Void> { transaction ->
            val document = transaction.get(ref)

            require(document.exists()) {
                "Reservation not found"
            }

            require(document.getString("entrant") == entrantUID) {
                "This reservation does not belong to you"
            }

            require(document.getString("reservation_state") == "NEW") {
                "This reservation cannot be declined"
            }

            transaction.update(
                ref,
                "reservation_state",
                ReservationState.CANCELLED.name
            )

            null
        }
    }

    fun deleteReservation(reservation: Reservation): Task<Void> {
        // I think I need to add something here that also removes the object references.
        // This will likely involve invoking reservation.deleteReservation
        reservation.deleteReservation()
        return reservations_ref.document(reservation.UID).delete()
    }

    // The following was written with help from Anthropic, Claude (Claude Code), "Add owner campsite support", 2026-11-05
    /**
     * Picks the UID that the next new campsite should use: one more than the largest
     * campsite UID currently loaded, or "0" if there are none.
     *
     * Call this before creating a new Campsite when you need to know its UID before it is
     * saved (for example, to add it to an owner's list first).
     *
     * @return the next free campsite UID as a String.
     */


    fun addCampsite(campsite: Campsite): Task<Void> {

        // The following was written with help from Anthropic, Claude (Claude Code), "Add owner campsite support", 2026-11-05
        // Use the campsite's own UID if it already has one, otherwise pick the next free UID.
        val UID: String = if (campsite.UID.isNotBlank()) campsite.UID else generateNextUid(_campsites.keys)
        val firestore_format = mapOf<String, Any>(
            "name" to campsite.getName(),
            "location" to campsite.getLocation(),
            "description" to campsite.getDescription(),
            "max_occupancy" to campsite.getMaxOccupancy(),
            "qr_code" to campsite.getQrCode(),
            "price" to campsite.getPrice(),
            "tags" to campsite.tags,
            "booking_period_dates" to campsite.booking_period_dates,
            "ratings" to campsite.ratings.map { (entrant, rating) -> mapOf("UID" to entrant.UID, "score" to rating) },
            "average_rating" to campsite.getAvgRating(),
            "waiting_list" to campsite.waiting_list.map { it.UID },
            "waitlist_capacity" to campsite.getWaitlistCapacity())
        return campsites_ref.document(UID).set(firestore_format)
    }

    // The following was written with help from Anthropic, Claude (Claude Code), "Add owner campsite support", 2026-11-05
    /**
     * Saves changes to a campsite that already exists in Firestore, replacing its stored
     * document with the campsite's current values. The campsite keeps its UID.
     *
     * @param campsite the edited Campsite. Its UID must not be blank.
     * @return the Firestore Task for the save.
     */
    fun updateCampsite(campsite: Campsite): Task<Void> {
        val firestore_format = mapOf<String, Any>(
            "name" to campsite.getName(),
            "location" to campsite.getLocation(),
            "description" to campsite.getDescription(),
            "max_occupancy" to campsite.getMaxOccupancy(),
            "qr_code" to campsite.getQrCode(),
            "price" to campsite.getPrice(),
            "tags" to campsite.tags,
            "booking_period_dates" to campsite.booking_period_dates,
            "ratings" to campsite.ratings.map { (entrant, rating) -> mapOf("UID" to entrant.UID, "score" to rating) },
            "average_rating" to campsite.getAvgRating(),
            "waiting_list" to campsite.waiting_list.map { it.UID },
            "waitlist_capacity" to campsite.getWaitlistCapacity())
        return campsites_ref.document(campsite.UID).set(firestore_format)
    }

    // Name: Devesh Darji
    // The following function / lines of code were generated / modified by Google Gemini Neon Model.
    // Prompt: "How do I clean up owner references when deleting a campsite and route all user save/delete methods to the unified users collection?" 2026-11-22
    // Updated deleteCampsite to remove deleted UIDs from owners, added saveUser() to persist all role flags to users_ref, and made generateNextUid() ignore non-numeric Auth UIDs.
    fun deleteCampsite(campsite: Campsite): Task<Void> {
        campsite.clearReservations()
        _all_users.values.forEach { user ->
            if (user.owned_campsites.any { it.UID == campsite.UID }) {
                user.removeCampsite(campsite)
                saveUser(user)
            }
        }
        return campsites_ref.document(campsite.UID).delete()
    }

    private fun saveUser(user: Entrant): Task<Void> {
        val uid = if (user.UID.isNotBlank()) user.UID else generateNextUid(_all_users.keys)
        val owned_uids = if (user is CampsiteOwner) user.owned_campsites.map { it.UID } else emptyList()
        val firestore_format = mapOf<String, Any>(
            "name" to user.getName(),
            "email" to user.getEmail(),
            "phone_number" to user.getPhoneNumber(),
            "role" to user.getRole(),
            "notifications" to user.notifications.map { it.UID },
            "owned_campsites" to owned_uids,
            "notifications_enabled" to user.getNotificationsEnabled()
        )
        return users_ref.document(uid).set(firestore_format)
    }

    // The functions below were written with help from Anthropic, Claude, "Save and load notifications in Firestore", 2026-11-25
    /**
     * Turns the stored notification documents into Notification objects. Called whenever users,
     * campsites or notifications change, because a notification needs its sender, campsite and
     * recipients to be loaded first. Notifications whose sender or campsite no longer exists are skipped.
     */
    private fun rebuildNotifications() {
        _notifications.clear()
        _notification_docs.forEach { (uid, data) ->
            val sender = _all_users[data["sender"] as? String ?: ""] ?: return@forEach
            val campsite = _campsites[data["campsite"] as? String ?: ""] ?: return@forEach
            val recipients = (data["recipients"] as? List<*>)
                ?.mapNotNull { (it as? String)?.let { recipient_uid -> _all_users[recipient_uid] } } ?: emptyList()
            val type = try {
                NotificationType.valueOf(data["type"] as? String ?: "")
            } catch (e: IllegalArgumentException) {
                NotificationType.OWNER_MESSAGE
            }
            _notifications[uid] = Notification(
                UID = uid,
                notification_type = type,
                sender = sender,
                campsite = campsite,
                title = data["title"] as? String ?: "",
                message = data["message"] as? String ?: "",
                _recipients = recipients,
                date_sent = data["date_sent"] as? String ?: ""
            )
        }
    }

    /**
     * Saves a notification to Firestore (US 02.10.01). Recipients, sender and campsite are stored by UID.
     *
     * @param notification the Notification to save.
     * @return the Firestore Task for the save.
     */
    fun addNotification(notification: Notification): Task<Void> {
        val UID: String = if (notification.UID.isNotBlank()) notification.UID else generateNextUid(_notification_docs.keys)
        val firestore_format = mapOf<String, Any>(
            "type" to notification.getNotificationType().name,
            "sender" to notification.getSender().UID,
            "campsite" to notification.getCampsite().UID,
            "title" to notification.getTitle(),
            "message" to notification.getMessage(),
            "recipients" to notification.recipients.map { it.UID },
            "date_sent" to notification.getDateSent())
        return notifications_ref.document(UID).set(firestore_format)
    }

    /**
     * Returns the notifications a user received, newest first (US 01.03.01, US 01.03.02).
     *
     * @param uid the user's UID.
     * @return the notifications that list this user as a recipient.
     */
    fun getNotificationsFor(uid: String): List<Notification> {
        return _notifications.values
            .filter { notification -> notification.recipients.any { it.UID == uid } }
            .sortedByDescending { it.UID.toIntOrNull() ?: 0 }
    }

    /**
     * Returns the notifications a campsite owner sent, newest first (US 02.10.02).
     *
     * @param uid the owner's UID.
     * @return the notifications this user sent.
     */
    fun getNotificationsSentBy(uid: String): List<Notification> {
        return _notifications.values
            .filter { it.getSender().UID == uid }
            .sortedByDescending { it.UID.toIntOrNull() ?: 0 }
    }

    fun updateUserRole(user: Entrant, new_role: Role): Task<Void> {
        user.role = new_role
        return saveUser(user)
    }

    fun addEntrant(entrant: Entrant): Task<Void> {
        return saveUser(entrant)
    }

    fun deleteEntrant(entrant: Entrant): Task<Void> {
        entrant.clearReservations()
        return users_ref.document(entrant.UID).delete()
    }

    fun addOwner(owner: CampsiteOwner): Task<Void> {
        return saveUser(owner)
    }

    fun updateOwner(owner: CampsiteOwner): Task<Void> {
        return saveUser(owner)
    }

    fun deleteOwner(owner: CampsiteOwner): Task<Void> {
        owner.clearReservations()
        return users_ref.document(owner.UID).delete()
    }

    fun addAdmin(admin: Admin): Task<Void> {
        return saveUser(admin)
    }

    fun deleteAdmin(admin: Admin): Task<Void> {
        admin.clearReservations()
        return users_ref.document(admin.UID).delete()
    }
}
