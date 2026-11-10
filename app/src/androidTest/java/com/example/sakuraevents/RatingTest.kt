package com.example.sakuraevents

import org.junit.Test
import org.junit.Assert.assertEquals

class RatingTest{
    @Test
    fun completedReservationState() {
        val state = ReservationState.COMPLETE

        assertEquals(ReservationState.COMPLETE, state)
    }
    @Test
    fun testEntrantCreation(){
        val entrant = Entrant(
            uid = "test123",
            name = "Test User",
            email = "test@example.com",
            phoneNumber = "7801234567"
        )
        assertEquals(0, entrant.reservations.size)
    }
    @Test
    fun testCampsiteCreation(){
        val campsite = Campsite()

        assertEquals(0, campsite.ratings.size)
    }
    @Test
    fun testAddRating(){
        val entrant = Entrant(
            uid = "test123",
            name = "Test User",
            email = "test@example.com",
            phoneNumber = "7801234567"
        )

    }
    @Test
    fun testAverageRating() {
        val entrant = Entrant(
            uid = "test123",
            name = "Test User",
            email = "test@example.com",
            phoneNumber = "7801234567"
        )

        val campsite = Campsite()

        campsite.addRating(entrant, 4.0f)

        assertEquals("4.0", campsite.getAvgRating())
    }
    @Test
    fun testActiveReservationCannotRate() {
        val state = ReservationState.ACTIVE

        val canRate = state == ReservationState.COMPLETE

        assertEquals(false, canRate)
    }
    @Test
    fun testCompletedReservationCanRate() {
        val state = ReservationState.COMPLETE

        val canRate = state == ReservationState.COMPLETE

        assertEquals(true, canRate)
    }



}