/**
 * File: LotteryFormTest.kt
 * Purpose: Unit tests for the lottery form logic in OwnerLotteryScreen.kt: date reading/writing,
 * per-field validation, and building a Lottery from the form's values.
 *
 * Target Class: OwnerLotteryScreen.kt (parseLotteryDate, formatLotteryDate, validateLotteryForm,
 * buildLotteryFromForm)
 *
 * User Stories Tested:
 * - US 02.04.01: As a campsite owner, I want to list my campsite in a lottery for a certain period.
 * - US 02.04.02: As a campsite owner, I want be able to list multiple campsites in a lottery.
 * - US 02.04.03: As a campsite owner, I want to set a registration period.
 * - US 02.04.06: As a campsite owner, I want to update my campsite lottery listing.
 *
 * Outstanding Issues:
 * - None.
 */
// Name: Arham Ahmed
// Written with help from Anthropic, Claude (Claude Code), "Write unit tests for the owner lottery form", 2026-11-26

package com.example.sakuraevents

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the lottery form logic. These run on the computer (no emulator needed).
 */
class LotteryFormTest {

    private val camp1 = Campsite(UID = "camp1", name = "Pine Valley", location = "Jasper, AB",
        description = "", max_occupancy = 6, price = 30.0f)
    private val camp2 = Campsite(UID = "camp2", name = "Lakeside", location = "Banff, AB",
        description = "", max_occupancy = 4, price = 45.0f)

    /** A real "yyyy-MM-dd" date should be read. */
    @Test
    fun validDateIsParsed() {
        assertNotNull(parseLotteryDate("2027-05-01"))
    }

    /** Blank text, other formats, and impossible dates should not be read as dates. */
    @Test
    fun badDatesAreNotParsed() {
        assertNull(parseLotteryDate(""))
        assertNull(parseLotteryDate("05/01/2027"))
        assertNull(parseLotteryDate("2027-5-1"))
        assertNull(parseLotteryDate("2027-02-30"))
        assertNull(parseLotteryDate("2027-13-01"))
    }

    /** A date picked in the date picker (UTC millis) should be written back as the same day. */
    @Test
    fun formatThenParseGivesSameDay() {
        val millis = parseLotteryDate("2027-05-01")!!.time
        assertEquals("2027-05-01", formatLotteryDate(millis))
    }

    /** A form with a campsite, an end date after the start date, and a winner count has no errors. */
    @Test
    fun validFormHasNoErrors() {
        val errors = validateLotteryForm(listOf("camp1"), "2027-05-01", "2027-05-07", 2)
        assertFalse(errors.has_errors)
        assertEquals(LotteryFormErrors(), errors)
    }

    /** Picking no campsites should only flag the campsites field (US 02.04.01). */
    @Test
    fun noCampsitesIsFlagged() {
        val errors = validateLotteryForm(emptyList(), "2027-05-01", "2027-05-07", 2)
        assertTrue(errors.has_errors)
        assertEquals("Pick at least one campsite.", errors.campsites)
        assertNull(errors.start_date)
        assertNull(errors.end_date)
        assertNull(errors.capacity)
    }

    /** An end date before the start date should flag the end date, not the start date (US 02.04.03). */
    @Test
    fun endBeforeStartIsFlagged() {
        val errors = validateLotteryForm(listOf("camp1"), "2027-05-07", "2027-05-01", 2)
        assertNull(errors.start_date)
        assertEquals("End date must be after the start date.", errors.end_date)
    }

    /** An end date on the same day as the start date is not after it, so it is flagged. */
    @Test
    fun endSameAsStartIsFlagged() {
        val errors = validateLotteryForm(listOf("camp1"), "2027-05-01", "2027-05-01", 2)
        assertEquals("End date must be after the start date.", errors.end_date)
    }

    /** Missing dates should each be flagged on their own field. */
    @Test
    fun missingDatesAreFlagged() {
        val errors = validateLotteryForm(listOf("camp1"), "", "", 2)
        assertEquals("Pick a registration start date.", errors.start_date)
        assertEquals("Pick a registration end date.", errors.end_date)
    }

    /** A winner count below 1 should be flagged. */
    @Test
    fun zeroWinnersIsFlagged() {
        val errors = validateLotteryForm(listOf("camp1"), "2027-05-01", "2027-05-07", 0)
        assertEquals("Set a winner count of at least 1 and press Confirm.", errors.capacity)
    }

    /** Every bad field should be reported at once, so the screen can highlight them all. */
    @Test
    fun allProblemsAreReportedTogether() {
        val errors = validateLotteryForm(emptyList(), "2027-05-07", "2027-05-01", 0)
        assertNotNull(errors.campsites)
        assertNull(errors.start_date)
        assertNotNull(errors.end_date)
        assertNotNull(errors.capacity)
    }

    /** Building a lottery should copy every value and include every picked campsite (US 02.04.02). */
    @Test
    fun buildLotteryCopiesValuesAndCampsites() {
        val values = LotteryFormValues(listOf(camp1, camp2), "2027-05-01", "2027-05-07", 3)
        val lottery = buildLotteryFromForm("7", values)

        assertEquals("7", lottery.UID)
        assertEquals(3, lottery.getCapacity())
        assertEquals("2027-05-01", lottery.getStartTime())
        assertEquals("2027-05-07", lottery.getEndTime())
        assertEquals(listOf(camp1, camp2), lottery.campsites_list)
    }

    /** Editing reuses the existing UID so the same Firestore document is replaced (US 02.04.06). */
    @Test
    fun editedLotteryKeepsItsUid() {
        val original = buildLotteryFromForm("4", LotteryFormValues(listOf(camp1), "2027-05-01", "2027-05-07", 1))
        val edited = buildLotteryFromForm(original.UID, LotteryFormValues(listOf(camp2), "2027-06-01", "2027-06-10", 5))

        assertEquals("4", edited.UID)
        assertEquals(listOf(camp2), edited.campsites_list)
        assertEquals("2027-06-10", edited.getEndTime())
        assertEquals(5, edited.getCapacity())
    }
}
