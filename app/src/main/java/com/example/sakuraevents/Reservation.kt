package com.example.sakuraevents

enum class ReservationState {
    NEW,
    ACTIVE,
    COMPLETE,
    CANCELLED
}

/**
 * Represents a reservation for a campsite at a specific set of dates.
 * This occurs when a user wins a lottery and selects from the available dates for a given campsite.
 *
 * A reservation has a campsite, reserving entrant, guest count, beginning, and end dates.
 * The campsite and entrant references allow for adding/removing self from its campsite and entrant.
 * guest count is used to confirm that the reservation doesn't overflow the
 *
 * @property campsite a reference to the Campsite for which this reservation is associated.
 * @property entrant a reference to the Entrant who established this reservation, they are included in the guest_count.
 * @property guest_count the total number of guests who will be using this reservation.
 * @property beginning_date the date on which the reservation begins.
 * @property end_date the date on which the reservation ends.
 *      both of the dates are stored as a string in ISO 8601 format, eg: 'YYYY-MM-DD'.
 */
class Reservation (val UID: String = "",
                   private val campsite: Campsite,
                   private val entrant: Entrant,
                   private val guest_count: Int,
                   private val beginning_date: String,
                   private val end_date: String,
                   private var reservation_state: ReservationState = ReservationState.NEW ){

    /**
     * The init block here handles adding this reservation to its Campsite and Entrant.
     */
    init {
        entrant.addReservation(this)
        campsite.addReservation(this)
    }

    /**
     * This getter simply returns the reference to the campsite object.
     *
     * there are no parameters for this method. It acts directly on the class properties.
     * @return a reference to the campsite object with the Campsite type.
     */
    fun getCampsite(): Campsite {
        return campsite
    }

    /**
     * This getter simply returns the reference to the entrant object.
     *
     * there are no parameters for this method. It acts directly on the class properties.
     * @return a reference to the entrant object with the Entrant type.
     */
    fun getEntrant(): Entrant {
        return entrant
    }

    /**
     * This getter simply returns the guest count for this reservation.
     *
     * there are no parameters for this method. It acts directly on the class properties.
     * @return the integer representation of the guest count.
     */
    fun getGuestCount(): Int {
        return guest_count
    }

    /**
     * This getter simply returns the date range of this reservation.
     *
     * there are no parameters for this method. It acts directly on the class properties.
     * @return a formatted string containing both the beginning and end dates of the reservation.
     */
    fun getBeginningDate(): String {
        return beginning_date
    }
    fun getEndDate(): String {
        return end_date
    }

    /**
     *
     * @param new_state can be any valid ReservationState enum, which are: NEW, ACTIVE, COMPLETE, CANCELLED
     * @return Nothing.
     */
    fun setState(new_state: ReservationState) {
        reservation_state = new_state
    }

    /**
     * Provides a string representation of the current state.
     * @return formatted state string.
     */
    fun getState(): String {
        return reservation_state.name
    }

    /**
     * This method cancels the existing reservation and notifies the Entrant and Campsite objects
     *      so that they can update their own references.
     * Then, the reservation is removed from the database and cleaned up.
     *
     * This method requires no parameters as it directly references the properties of the parent class.
     */
    fun deleteReservation(): Unit {
        entrant.removeReservation(this)
        campsite.removeReservation(this)
    }
}
