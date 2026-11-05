package com.example.sakuraevents

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import kotlin.to
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.auth.FirebaseAuth


/**
 * This class handles the process of converting the various classes to and from their firestore
 * forms.
 */
class FirestoreDriver {

    private val _reservations = mutableMapOf<String, Reservation>()
    private val _campsites = mutableMapOf<String, Campsite>()
    private val _entrants = mutableMapOf<String, Entrant>()
    private val _owners = mutableMapOf<String, CampsiteOwner>()
    //private val _admins = mutableMapOf<String, Admin>()
    //private val _notifications = mutableMapOf<String, Notification>()

    val reservations: Map<String, Reservation> get() = _reservations
    val campsites: Map<String, Campsite> get() = _campsites
    val entrants: Map<String, Entrant> get() = _entrants
    val owners: Map<String, CampsiteOwner> get() = _owners
    //val admins: Map<String, Admin> get() = _admins
    //val notifications: Map<String, Notification> get() = _notifications

    val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance().signInAnonymously()
    private val reservations_ref = db.collection("reservations")
    private val campsites_ref = db.collection("campsites")
    private val entrants_ref = db.collection("entrants")
    private val owners_ref = db.collection("owners")
    private val admins_ref = db.collection("admins")
    private val notifications_ref = db.collection("notifications")


    /**
     *
     */
    init {
        //Tasks.await(auth.signInAnonymously())


        // Each of these ref.addSnapshotListener are what handle both assembling the arrays and updating them.
        // This code is adapted from the lab05 listyCity code. It is mostly modified in how it retrieves
        // the info from the document file because parts of our classes don't serialize cleanly.
        //assert(false)
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
                        ratings = (campsite["ratings"] as List<*>).map { map ->
                            val (entrantUID, double) = (map as Map<*, *>).entries.single()
                            val entrant = _entrants[entrantUID as String] as Entrant
                            val float = (double as Double).toFloat()
                            entrant to float
                        }.toMutableList()
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
                        average_rating = (campsite["average_rating"] as String).toFloat()
                    )
                }
            }
        }

        entrants_ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            _entrants.clear()
            snapshot?.documents?.forEach() { document ->
                val entrant: Map<String, Any>? = document.data
                if (entrant != null) {
                    _entrants[document.id] = Entrant(
                        UID = document.id,
                        name = entrant["name"] as String,
                        email = entrant["email"] as String,
                        phone_number = entrant["phone_number"] as String,
                        _notifications = mutableListOf<Notification>()
                    )
                }
            }
        }

        owners_ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            _owners.clear()
            snapshot?.documents?.forEach { document ->
                val owner: Map<String, Any>? = document.data
                if (owner != null) {
                    _owners[document.id] = CampsiteOwner(
                        UID = document.id,
                        name = owner["name"] as String,
                        email = owner["email"] as String,
                        phone_number = owner["phone_number"] as String,
                        _owned_campsites = (owner["owned_campsites"] as List<*>).map { uid -> _campsites[uid as String] as Campsite }.toMutableList()
                    )
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
                    _reservations[document.id] = Reservation(
                        UID =            document.id,
                        campsite =       _campsites[reservation["campsite"] as String] as Campsite,
                        entrant =        _entrants[reservation["entrant"] as String] as Entrant,
                        guest_count =    (reservation["guest_count"] as Long).toInt(),
                        beginning_date = reservation["beginning_date"] as String,
                        end_date =       reservation["end_date"] as String
                    )
                }
            }
        }
    }

    fun addReservation(reservation: Reservation): Task<Void> {
        val firestore_format = mapOf<String, Any>( // Any type is a little dangerous, requires us to be disciplined in our typing.
            "campsite" to reservation.getCampsite().UID,
            "entrant" to reservation.getEntrant().UID,
            "guest_count" to reservation.getGuestCount(),
            "beginning_date" to reservation.getBeginningDate(),
            "end_date" to reservation.getEndDate(),
            "reservation_state" to reservation.getState())
        return reservations_ref.document(reservation.UID).set(firestore_format)
    }
    fun deleteReservation(reservation: Reservation): Task<Void> {
        // I think I need to add something here that also removes the object references.
        // This will likely involve invoking reservation.deleteReservation
        reservation.deleteReservation()
        return reservations_ref.document(reservation.UID).delete()
    }

    fun addCampsite(campsite: Campsite): Task<Void> {
        val UID: String
        if (_campsites.isNotEmpty()) {
            UID = ((_campsites.keys.map { it.toInt() }).sortedDescending()[0] + 1).toString()
        }
        else {
            UID = "0"
        }
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
            "average_rating" to campsite.getAvgRating())
        return campsites_ref.document(UID).set(firestore_format)
    }
    fun deleteCampsite(campsite: Campsite): Task<Void> {
        campsite.clearReservations()
        return campsites_ref.document(campsite.UID).delete()
    }

    fun addEntrant(entrant: Entrant): Task<Void> {
        val UID: String
        if (_entrants.isNotEmpty()) {
            UID = ((_entrants.keys.map { it.toInt() }).sortedDescending()[0] + 1).toString()
        }
        else {
            UID = "0"
        }
        val firestore_format = mapOf<String, Any>(
            "name" to entrant.getName(),
            "email" to entrant.getEmail(),
            "phone_number" to entrant.getPhoneNumber(),
            "notifications" to entrant.notifications.map { it.UID })
        return entrants_ref.document(UID).set(firestore_format)
    }
    fun deleteEntrant(entrant: Entrant): Task<Void> {
        entrant.clearReservations()
        return entrants_ref.document(entrant.UID).delete()
    }

    fun addOwner(owner: CampsiteOwner): Task<Void> {
        val UID: String
        if (_owners.isNotEmpty()) {
            UID = ((_owners.keys.map { it.toInt() }).sortedDescending()[0] + 1).toString()
        }
        else {
            UID = "0"
        }
        val firestore_format = mapOf<String, Any>(
            "name" to owner.getName(),
            "email" to owner.getEmail(),
            "phone_number" to owner.getPhoneNumber(),
            "owned_campsites" to owner.owned_campsites.map { it.UID })
        return owners_ref.document(UID).set(firestore_format)
    }
    fun deleteOwner(owner: CampsiteOwner): Task<Void> {
        // If we delete an owner should we also remove all of their campsites? Question for later.
        return owners_ref.document(owner.UID).delete()
    }
}
