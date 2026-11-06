// Purpose: Unit tests for validateCampsiteForm, the checks used by the campsite form.

package com.example.sakuraevents

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [validateCampsiteForm]. These run on the computer (no emulator needed).
 */
class CampsiteFormTest {

    /** Valid details should give no error. */
    @Test
    fun validDetailsGiveNoError() {
        assertNull(validateCampsiteForm("Pine Valley", "Jasper, AB", "6", "30.50"))
    }

    /** A price of 0 (free entry) should be allowed. */
    @Test
    fun zeroPriceIsAllowed() {
        assertNull(validateCampsiteForm("Pine Valley", "Jasper, AB", "6", "0"))
    }

    /** Extra spaces around the numbers should be ignored. */
    @Test
    fun spacesAroundNumbersAreIgnored() {
        assertNull(validateCampsiteForm("Pine Valley", "Jasper, AB", " 6 ", " 30 "))
    }

    /** A blank name should be rejected. */
    @Test
    fun blankNameIsRejected() {
        assertEquals("Please enter a name.", validateCampsiteForm("  ", "Jasper, AB", "6", "30"))
    }

    /** A blank location should be rejected. */
    @Test
    fun blankLocationIsRejected() {
        assertEquals("Please enter a location.", validateCampsiteForm("Pine Valley", "", "6", "30"))
    }

    /** Max occupancy must be a whole number above 0. */
    @Test
    fun badMaxOccupancyIsRejected() {
        val message = "Max occupancy must be a whole number above 0."
        assertEquals(message, validateCampsiteForm("Pine Valley", "Jasper, AB", "0", "30"))
        assertEquals(message, validateCampsiteForm("Pine Valley", "Jasper, AB", "-2", "30"))
        assertEquals(message, validateCampsiteForm("Pine Valley", "Jasper, AB", "2.5", "30"))
        assertEquals(message, validateCampsiteForm("Pine Valley", "Jasper, AB", "abc", "30"))
    }

    /** Price must be a number that is 0 or more. */
    @Test
    fun badPriceIsRejected() {
        val message = "Price must be a number that is 0 or more."
        assertEquals(message, validateCampsiteForm("Pine Valley", "Jasper, AB", "6", "-1"))
        assertEquals(message, validateCampsiteForm("Pine Valley", "Jasper, AB", "6", ""))
        assertEquals(message, validateCampsiteForm("Pine Valley", "Jasper, AB", "6", "free"))
    }
}