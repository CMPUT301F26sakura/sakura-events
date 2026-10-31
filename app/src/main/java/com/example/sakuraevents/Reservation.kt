package com.example.sakuraevents

/**
 * Represents a reservation for a campsite at a specific set of dates.
 * This occurs when a user wins a lottery and selects from the available dates for a given campsite.
 *
 * A reservation has a name, current mana, and spell power stat. The
 * wizard needs sufficient mana to cast spells, and spell power
 * is used to calculate how much damage is dealt when a valid
 * spell is cast.
 *
 * @property campsite a reference to the Campsite for which this reservation is associated.
 * @property entrant a reference to the Entrant who established this reservation, they are included in the guest_count.
 * @property guest_count the total number of guests who will be using this reservation.
 * @property beginning_date the date on which the reservation begins.
 * @property end_date the date on which the reservation ends.
 *      both of the dates are stored as a string in ISO 8601 format, eg: 'YYYY-MM-DD'.
 */
class Reservation (val campsite: Campsite, val entrant: Entrant, val guest_count: Int, val beginning_date: String, val end_date: String) {

    /**
     * This method cancels the existing reservation and notifies the Entrant and Campsite objects
     *      so that they can update their own references.
     * Then, the reservation is removed from the database and cleaned up.
     *
     * This method requires no parameters as it directly references the properties of the parent class.
     */
    fun cancelReservation(): {
        /*
        call campsite method to remove this from the campsite list.
        call entrant method to remove this from the entrant list.
        then handle cleaning self up from database.
        kotlin handles its own memory management, it should clean this up once nothing else references it.
         */
    }

}


/*
Reservation
Responsibilities 	Collaborators
Associate a winning entrant with a specific campsite and selected dates. 	Entrant
Track and update the lifecycle status of a reservation (active, past, cancelled, declined, or rejected). 	Campsite
Enforce entrant confirmation of campsite rules and policies prior to finalizing a reservation. 	Campsite Owner
Provide reservation records and cancellation via notifications. 	Notification
Facilitate direct messaging between a campsite owner and the reserving entrant. 	Admin
Trigger notifications for reservation confirmations, cancellations, and owner rejections. 	Lottery
Present valid, available dates to entrants who have won a campsite lottery.
Prevent scheduling conflicts by tracking which dates are successfully converted into active reservations.
 */