package com.example.sakuraevents

// WaitlistComponentsTest.kt: UI tests for WaitlistButton and WaitlistCapacityField.

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WaitlistComponentsTest {

    @get:Rule
    val compose_rule = createComposeRule()

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

    @Test
    fun testJoinWaitlist() {
        val campsite = mockCampsite()
        compose_rule.setContent { WaitlistButton(campsite = campsite, entrant = aisha) }

        compose_rule.onNodeWithText("Join Waitlist").performClick()

        compose_rule.onNodeWithText("Leave Waitlist").assertExists()
        compose_rule.onNodeWithTag("waitlist_count").assertTextEquals("1 on waitlist")
        assertTrue(campsite.waiting_list.contains(aisha))
    }

    @Test
    fun testLeaveWaitlist() {
        val campsite = mockCampsite()
        campsite.addToWaitingList(aisha)
        compose_rule.setContent { WaitlistButton(campsite = campsite, entrant = aisha) }

        compose_rule.onNodeWithText("Leave Waitlist").performClick()

        compose_rule.onNodeWithText("Join Waitlist").assertExists()
        compose_rule.onNodeWithTag("waitlist_count").assertTextEquals("0 on waitlist")
        assertFalse(campsite.waiting_list.contains(aisha))
    }

    @Test
    fun testWaitlistFull() {
        val campsite = mockCampsite()
        campsite.setWaitlistCapacity(1)
        campsite.addToWaitingList(ben)
        compose_rule.setContent { WaitlistButton(campsite = campsite, entrant = aisha) }

        compose_rule.onNodeWithText("Waitlist Full").assertIsNotEnabled()
        compose_rule.onNodeWithTag("waitlist_count").assertTextEquals("1 / 1 on waitlist")
    }

    @Test
    fun testCanStillLeaveWhenFull() {
        val campsite = mockCampsite()
        campsite.setWaitlistCapacity(1)
        campsite.addToWaitingList(aisha)
        compose_rule.setContent { WaitlistButton(campsite = campsite, entrant = aisha) }

        compose_rule.onNodeWithText("Leave Waitlist").assertIsEnabled().performClick()

        assertFalse(campsite.waiting_list.contains(aisha))
    }

    @Test
    fun testJoinCallsSaveCallback() {
        val campsite = mockCampsite()
        var saved_campsite: Campsite? = null
        compose_rule.setContent {
            WaitlistButton(campsite = campsite, entrant = aisha, onWaitlistChanged = { saved_campsite = it })
        }

        compose_rule.onNodeWithText("Join Waitlist").performClick()

        assertTrue(saved_campsite === campsite)
    }

    @Test
    fun testSetCapacity() {
        val campsite = mockCampsite()
        compose_rule.setContent { WaitlistCapacityField(campsite = campsite) }

        compose_rule.onNodeWithTag("capacity_input").performTextReplacement("3")
        compose_rule.onNodeWithTag("capacity_save").performClick()

        assertEquals(3, campsite.getWaitlistCapacity())
        compose_rule.onNodeWithTag("capacity_error").assertDoesNotExist()
    }

    @Test
    fun testInvalidCapacityShowsError() {
        val campsite = mockCampsite()
        compose_rule.setContent { WaitlistCapacityField(campsite = campsite) }

        compose_rule.onNodeWithTag("capacity_input").performTextReplacement("-2")
        compose_rule.onNodeWithTag("capacity_save").performClick()

        compose_rule.onNodeWithTag("capacity_error").assertExists()
        assertEquals(0, campsite.getWaitlistCapacity())
    }
}