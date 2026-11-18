package com.example.sakuraevents

// CampsiteWaitlistTest.kt: unit tests for joining, leaving, and capping a campsite's waiting list.

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CampsiteWaitlistTest {

    private fun mockCampsite(): Campsite {
        return Campsite(
            name = "Tunnel Mountain Village",
            location = "Banff, AB",
            description = "Sites with mountain views, close to town.",
            max_occupancy = 4,
            price = 41.5f
        )
    }

    private val aisha = Entrant("entrant_001", Role.ENTRANT, "Aisha Khan", "aisha@example.com", "780-555-0101")
    private val ben = Entrant("entrant_002", Role.ENTRANT, "Ben Carter", "ben@example.com", "780-555-0102")
    private val chen = Entrant("entrant_003", Role.ENTRANT, "Chen Wei", "chen@example.com", "780-555-0103")

    @Test
    fun testJoinWaitingList() {
        val campsite = mockCampsite()
        assertTrue(campsite.addToWaitingList(aisha))
        assertEquals(1, campsite.waiting_list.size)
        assertTrue(campsite.waiting_list.contains(aisha))
    }

    @Test
    fun testCannotJoinTwice() {
        val campsite = mockCampsite()
        campsite.addToWaitingList(aisha)
        assertFalse(campsite.addToWaitingList(aisha))
        assertEquals(1, campsite.waiting_list.size)
    }

    @Test
    fun testLeaveWaitingList() {
        val campsite = mockCampsite()
        campsite.addToWaitingList(aisha)
        campsite.addToWaitingList(ben)
        campsite.removeFromWaitingList(aisha)
        assertFalse(campsite.waiting_list.contains(aisha))
        assertEquals(1, campsite.waiting_list.size)
    }

    @Test
    fun testLeaveWhenNotOnList() {
        val campsite = mockCampsite()
        campsite.addToWaitingList(ben)
        campsite.removeFromWaitingList(aisha)
        assertEquals(1, campsite.waiting_list.size)
    }

    @Test
    fun testNoLimitByDefault() {
        val campsite = mockCampsite()
        assertEquals(0, campsite.getWaitlistCapacity())
        assertTrue(campsite.addToWaitingList(aisha))
        assertTrue(campsite.addToWaitingList(ben))
        assertTrue(campsite.addToWaitingList(chen))
    }

    @Test
    fun testSetCapacity() {
        val campsite = mockCampsite()
        assertTrue(campsite.setWaitlistCapacity(2))
        assertEquals(2, campsite.getWaitlistCapacity())
    }

    @Test
    fun testNegativeCapacityRejected() {
        val campsite = mockCampsite()
        assertFalse(campsite.setWaitlistCapacity(-1))
        assertEquals(0, campsite.getWaitlistCapacity())
    }

    @Test
    fun testCannotJoinWhenFull() {
        val campsite = mockCampsite()
        campsite.setWaitlistCapacity(2)
        assertTrue(campsite.addToWaitingList(aisha))
        assertTrue(campsite.addToWaitingList(ben))
        assertFalse(campsite.addToWaitingList(chen))
        assertEquals(2, campsite.waiting_list.size)
    }

    @Test
    fun testCanJoinAfterSomeoneLeaves() {
        val campsite = mockCampsite()
        campsite.setWaitlistCapacity(1)
        campsite.addToWaitingList(aisha)
        campsite.removeFromWaitingList(aisha)
        assertTrue(campsite.addToWaitingList(ben))
    }
}