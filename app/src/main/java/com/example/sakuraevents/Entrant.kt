package com.example.sakuraevents

/**
 * Represents an entrant in the Sakura Events app.
 *
 * An entrant can join campsite waiting lists,
 * and make reservations, and rate campsites,
 * after completing a visit.
 *
 * @property userId  the unique Id of the entrant.
 * @property userName  the name of the entrant.
 * @property userEmail  the email of the entrant.
 * @property phoneNumber  the phone number of the entrant.
 */

class Entrant (
    userId: String,
    userName: String,
    userEmail: String,
    phoneNumber: String,
   var campsiteRating: Int? = null
) : User(userId, userName, userEmail, phoneNumber){
    // Store the entrant reservations.

    /**
     * Add the reservation to the entrant list
     *
     *
     *
     */
    fun addReservation(reservationId : String, ){
        // TODO : connect to reservation class when ready
    }

    /**
     * Cancle a reservation
     *
     * @return True if found and cancelled, false otherwise.
     */
    fun deleteReservation(reservationId: String): Boolean {
        // TODO : connect to reservation class when ready
        return false
    }



    fun canRate(): Boolean {
        return false

    }
    fun rateCampsite(stars: Int){
        if (stars !in 1..5){
            return

        }

    }

}

