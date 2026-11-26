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

    private val _reservations = mutableMapOf<String, Reservation>()
    // The following _campsites and _owners lines were written with help from Anthropic, Claude (Claude Code), "Add owner campsite support", 2026-11-05
    // mutableStateMapOf lets Compose screens redraw automatically when these maps change.
    private val _campsites = mutableStateMapOf<String, Campsite>()
    private val _entrants = mutableStateMapOf<String, Entrant>()
    private val _owners = mutableStateMapOf<String, CampsiteOwner>()
    private val _admins = mutableStateMapOf<String, Admin>()
    //private val _notifications = mutableMapOf<String, Notification>()
    //private val _lotteries = mutableMapOf<String, Lottery>()

    val reservations: Map<String, Reservation> get() = _reservations
    val campsites: Map<String, Campsite> get() = _campsites
    val entrants: Map<String, Entrant> get() = _entrants
    val owners: Map<String, CampsiteOwner> get() = _owners
    val admins: Map<String, Admin> get() = _admins
    //val notifications: Map<String, Notification> get() = _notifications
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
                        "notifications" to emptyList<String>()
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

                _all_users[document.id] = user
                if (user.has_entrant_access) _entrants[document.id] = user
                if (user.has_owner_access) _owners[document.id] = user
                if (user.has_admin_access) _admins[document.id] = user
            }

            current_user = current_user_uid?.let { _all_users[it] }
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
                        ratings = (campsite["ratings"] as List<*>).mapNotNull { map ->
                            val ratingMap = map as Map<*, *>
                            val entrantUID = ratingMap["UID"] as? String
                                ?: return@mapNotNull null
                            val entrant = _entrants[entrantUID]
                                ?: return@mapNotNull null
                            val score = ratingMap["score"] as? Number
                                ?: return@mapNotNull null

                            entrant to score.toFloat()
                        }.toMutableList()
                    }
                    val reviews = mutableMapOf<String, String>()

                    (campsite["ratings"] as? List<*>)?.forEach { item ->
                        val ratingMap = item as? Map<*, *>
                        val uid = ratingMap?.get("UID") as? String
                        val review = ratingMap?.get("review") as? String

                        if (uid != null && !review.isNullOrBlank()) {
                            reviews[uid] = review
                        }
                    }

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
                        _reviews = reviews,
                        average_rating = (campsite["average_rating"] as String).toFloat(),
                        _waiting_list = (campsite["waiting_list"] as List<*>).mapNotNull { (it as? String)?.let { uid -> _all_users[uid] } }.toMutableList(),
                        waitlist_capacity = (campsite["waitlist_capacity"] as Long).toInt()
                    )
                    _all_users.forEach { (uid, user) ->
                        _user_owned_campsite_uids[uid]?.mapNotNull { _campsites[it] }?.forEach { user.addCampsite(it) }
                    }
                }
            }
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
                        beginning_date = reservation["beginning_date"] as String,
                        end_date = reservation["end_date"] as String,
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
            "ratings" to campsite.ratings.map { (entrant, rating) -> mapOf("UID" to entrant.UID, "score" to rating, "review" to (campsite.reviews[entrant.UID] ?: "")) },
            "average_rating" to campsite.getAvgRating(),
            "waiting_list" to campsite.waiting_list.map { it.UID },
            "waitlist_capacity" to campsite.getWaitlistCapacity())
        return campsites_ref.document(UID).set(firestore_format)
    }

    /**
     * Reads the latest ratings directly from Firebase.
     * Checks whether the entrant has already rated.
     * Preserves other entrants' ratings and reviews.
     * Recalculates the average rating.
     * Updates only the rating related fields instead of the entire campsite.
     */
    fun submitRating(
        campsiteUID: String, entrantUID: String, score: Float, review: String
    ): Task<Void> {
        val campsiteRef = campsites_ref.document(campsiteUID)
        return db.runTransaction<Void> { transaction ->
            val snapshot = transaction.get(campsiteRef)
            val ratings = (snapshot.get("ratings") as? List<*>)
                .orEmpty().map { it as Map<*, *> }.toMutableList()
            if (ratings.any { it["UID"] == entrantUID }) {
                throw IllegalStateException("You have already rated this campsite.")
            }
            require(score in 1.0f..5.0f)
            ratings.add(
                mapOf("UID" to entrantUID, "score" to score, "review" to review.trim()))
            val average = ratings.map { (it["score"] as Number).toDouble() }.average()
            transaction.update(
                campsiteRef, mapOf("ratings" to ratings,
                    "average_rating" to average.toString()
                )
            )

            null
        }
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
            "ratings" to campsite.ratings.map { (entrant, rating) -> mapOf("UID" to entrant.UID, "score" to rating,"review" to(campsite.reviews[entrant.UID] ?: "")) },
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
            "owned_campsites" to owned_uids
        )
        return users_ref.document(uid).set(firestore_format)
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
