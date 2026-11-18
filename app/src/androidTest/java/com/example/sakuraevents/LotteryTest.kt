/**
 * File: LotteryTest.kt
 * Purpose: Unit tests for the Lottery pooling class, testing single and multi-campsite
 * lotteries, registration periods, winner drawing from member campsites' waitlists,
 * and automatic win/loss notification dispatch.
 *
 * Target Class: Lottery.kt
 *
 * User Stories Tested:
 * - US 01.03.01: As an entrant, I want to receive a notification when I win the campsite lottery.
 * - US 01.03.02: As an entrant, I want to receive a notification when I lose the campsite lottery.
 * - US 02.04.01: As a campsite owner, I want to list my campsite in a lottery for a certain period.
 * - US 02.04.02: As a campsite owner, I want be able to list multiple campsites in a lottery.
 * - US 02.04.03: As a campsite owner, I want to set a registration period.
 * - US 02.04.05: As a campsite owner, I want to set the number of lottery winners.
 * - US 02.05.01: As a campsite owner, I want the system to automatically draw winners after the registration period ends.
 *
 * Outstanding Issues:
 * - None for Part 3 checkpoint for now.
 */

package com.example.sakuraevents

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit test suite for [Lottery].
 */
class LotteryTest {
    private lateinit var lottery: Lottery
    private lateinit var camp1: Campsite
    private lateinit var camp2: Campsite

    /**
     * Initializes a [Lottery] and two [Campsite]s before each test.
     */
    @Before
    fun setUp() {
        lottery = Lottery(
            capacity = 2,
            start_time = "2027-05-01",
            end_time = "2027-05-07"
        )
        camp1 = Campsite(UID = "", name = "Moraine Creek", location = "Banff", description = "Alpine site", max_occupancy = 4, price = 45f)
        camp2 = Campsite(UID = "", name = "Maligne Canyon", location = "Jasper", description = "Canyon site", max_occupancy = 6, price = 50f)
    }

    /**
     * Verifies configuring multiple campsites, registration period dates, and winner capacity
     * (US 02.04.01, US 02.04.02, US 02.04.03, US 02.04.05).
     */
    // The following @Test functions below were generated / modified using Google Gemini Model 3.1 Pro
    // "Given my Lottery.kt, I want you to write some @Test functions for my Lottery.kt" 2026-Nov-5
    @Test
    fun testLotteryConfigurationAndMultiCampsiteListing() {
        lottery.addCampsite(camp1)
        lottery.addCampsite(camp2)
        assertEquals(2, lottery.campsites_list.size)

        lottery.receiveCapacity(3)
        lottery.receiveStartTime("2027-06-01")
        lottery.receiveEndTime("2027-06-10")

        assertEquals(3, lottery.getCapacity())
        assertEquals("2027-06-01", lottery.getStartTime())
        assertEquals("2027-06-10", lottery.getEndTime())
    }
    /**
     * Verifies that the lottery dynamically pulls from member campsites' waitlists
     * and filters out duplicates if an entrant joins multiple member campsites (just to be on the safe side here).
     */
    @Test
    fun testDynamicWaitlistPoolingAndDuplicateFiltering() {
        lottery.addCampsite(camp1)
        lottery.addCampsite(camp2)
        lottery.receiveCapacity(2)

        // Added role = Role.ENTRANT to satisfy the Entrant constructor
        val e1 = Entrant(UID = "", role = Role.ENTRANT, name = "One", email = "1@uab.ca", phone_number = "111")
        val e2 = Entrant(UID = "", role = Role.ENTRANT, name = "Two", email = "2@uab.ca", phone_number = "222")

        // e1 joins camp1, e2 joins both camp1 and camp2
        // Changed joinWaitlist to addToWaitingList
        camp1.addToWaitingList(e1)
        camp1.addToWaitingList(e2)
        camp2.addToWaitingList(e2)

        val winners = lottery.drawWinners()

        // Even though e2 joined twice, the distinct() pool size was exactly 2,
        // and the capacity is 2, so both should win exactly once without crashing HOPEFULLY.
        assertEquals(2, winners.size)
        assertTrue(winners.contains(e1))
        assertTrue(winners.contains(e2))
    }
    /**
     * Verifies drawing winners up to capacity and sending win/loss notifications
     * to all waiting list entrants across member campsites (US 01.03.01, US 01.03.02, US 02.05.01).
     */
    @Test
    fun testDrawWinnersAndSendWinLoseNotifications() {
        lottery.addCampsite(camp1)
        lottery.receiveCapacity(1)

        // Added role = Role.ENTRANT here as well
        val e1 = Entrant(UID = "", role = Role.ENTRANT, name = "Entrant A", email = "a@uab.ca", phone_number = "111")
        val e2 = Entrant(UID = "", role = Role.ENTRANT, name = "Entrant B", email = "b@uab.ca", phone_number = "222")
        val e3 = Entrant(UID = "", role = Role.ENTRANT, name = "Entrant C", email = "c@uab.ca", phone_number = "333")

        camp1.addToWaitingList(e1)
        camp1.addToWaitingList(e2)
        camp1.addToWaitingList(e3)

        val winners = lottery.drawWinners()

        // Capacity is 1, so only 1 winner should be drawn
        assertEquals(1, winners.size)

        // All 3 entrants must have received exactly 1 notification (1 winner + 2 losers)
        assertEquals(1, e1.notifications.size)
        assertEquals(1, e2.notifications.size)
        assertEquals(1, e3.notifications.size)

        val winnerEntrant = winners.first()

        // Changed getTitle() to .title since Kotlin uses property access
        assertEquals("Lottery Winner!", winnerEntrant.notifications.first().getTitle())
    }
}