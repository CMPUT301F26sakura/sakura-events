package com.example.sakuraevents

import kotlin.to
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


/**
 * This class handles the process of converting the various classes to and from their firestore
 * forms.
 */
class FirestoreDriver {

    private val _reservations = mutableMapOf<String, Reservation>()
    private val _campsites = mutableMapOf<String, Campsite>()
    //private val _entrants = mutableMapOf<String, Entrant>()
    //private val _owners = mutableMapOf<String, Owner>()
    //private val _admins = mutableMapOf<String, Admin>()
    //private val _notifications = mutableMapOf<String, Notification>()

    val reservations: Map<String, Reservation> get() = _reservations
    val campsites: Map<String, Campsite> get() = _campsites
    //val entrants: Map<String, Entrant> get() = _entrants
    //val owners: Map<String, Owner> get() = _owners
    //val admins: Map<String, Admin> get() = _admins
    //val notifications: Map<String, Notification> get() = _notifications

    private val db = Firebase.firestore
    private val reservations_ref = db.collection("reservations")
    private val campsites_ref = db.collection("campsites")

    init {
        reservations_ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            _reservations.clear()

            snapshot?.documents?.forEach() { document ->
                val reservation = document.id
                if (reservation != null) {
                    _reservations.
                }
            }
        }

    }

    fun addReservation(reservation: Reservation) {
        val firestore_format = mapOf<String, Any>( // Any type is a little dangerous, requires us to be disciplined in our typing.
            "campsite" to reservation.getCampsite().UID,
            "entrant" to reservation.getEntrant().UID,
            "guest_count" to reservation.getGuestCount(),
            "beginning_date" to reservation.getBegginingDate(),
            "end_date" to reservation.getEndDate(),
            "reservation_state" to reservation.getState())
        reservations_ref.document(reservation.UID).set(firestore_format)
    }
    fun deleteReservation(reservation: Reservation) {
        // I think I need to add something here that also removes the object references.
        // This will likely involve invoking reservation.deleteReservation
        reservations_ref.document(reservation.UID).delete()
    }

    fun addCampsite(campsite: Campsite) {

        val firestore_format = mapOf<String, Any>(
            "name" to campsite.getName(),
            "location" to campsite.getLocation(),
            "description" to campsite.getDescription(),
            "max_occupancy" to campsite.getMaxOccupancy(),
            "qr_code" to campsite.getQrCode(),
            "price" to campsite.getPrice(),
            "tags" to campsite.tags,
            "booking_period_dates" to campsite.booking_period_dates,
            "reservations" to campsite.reservations.map { it.UID }, // This returns a list of strings.
            "ratings" to campsite.ratings.map { (entrant, score) -> mapOf("UID" to entrant.UID, "score" to score) },
            "average_rating" to campsite.getAvgRating())
        campsites_ref.document(campsite.UID).set(firestore_format)
    }
    fun deleteCampsite(campsite: Campsite) {
        campsites_ref.document(campsite.UID).delete()
    }
}
