/**
 * File: CampsiteFormScreenTest.kt
 * Purpose: Intent (UI) tests for the campsite form on its own: error messages for invalid input
 * and filling in the form when editing.
 *
 * Target Class: CampsiteFormScreen.kt
 *
 * User Stories Tested:
 * - US 02.01.02: As a campsite owner, I want to provide campsite details.
 * - US 02.01.03: As a campsite owner, I want to update campsite details.
 *
 * These tests use fake Campsite objects instead of Firestore.
 *
 * Outstanding Issues:
 * - None for now.
 */
// Written with help from Anthropic, Claude (Claude Code), "Write intent tests for the owner campsite screens", 2026-11-05

package com.example.sakuraevents

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Intent tests for [campsiteFormContent].
 */
@RunWith(AndroidJUnit4::class)
class CampsiteFormScreenTest {

    @get:Rule
    val compose_rule = createComposeRule()

    /** Set to true if the form tries to save, so tests can check that invalid input is not saved. */
    private var save_called = false

    /** Shows an empty form for registering a new campsite. */
    private fun showEmptyForm() {
        compose_rule.setContent {
            campsiteFormContent(
                campsite = null,
                is_editing = false,
                on_back = {},
                on_save = { save_called = true; null }
            )
        }
    }

    /**
     * Types the given details into the form and taps Save.
     *
     * @param name the name to type.
     * @param location the location to type.
     * @param max_occupancy the max occupancy to type.
     * @param price the price to type.
     */
    private fun fillAndSave(name: String, location: String, max_occupancy: String, price: String) {
        if (name.isNotEmpty()) compose_rule.onNodeWithTag("campsiteNameInput").performTextInput(name)
        if (location.isNotEmpty()) compose_rule.onNodeWithTag("campsiteLocationInput").performTextInput(location)
        compose_rule.onNodeWithTag("maxOccupancyInput").performScrollTo().performTextInput(max_occupancy)
        compose_rule.onNodeWithTag("priceInput").performScrollTo().performTextInput(price)
        compose_rule.onNodeWithTag("saveCampsiteButton").performScrollTo().performClick()
    }

    /** A max occupancy of 0 should show an error and not save. */
    @Test
    fun zeroOccupancyShowsError() {
        showEmptyForm()

        fillAndSave(name = "Pine Valley", location = "Jasper, AB", max_occupancy = "0", price = "30")

        compose_rule.onNodeWithTag("formErrorText").performScrollTo().assertIsDisplayed()
            .assertTextEquals("Max occupancy must be a whole number above 0.")
        assertFalse(save_called)
    }

    /** A blank name should show an error and not save. */
    @Test
    fun blankNameShowsError() {
        showEmptyForm()

        fillAndSave(name = "", location = "Jasper, AB", max_occupancy = "6", price = "30")

        compose_rule.onNodeWithTag("formErrorText").performScrollTo().assertIsDisplayed()
            .assertTextEquals("Please enter a name.")
        assertFalse(save_called)
    }

    /** When editing, the form should start filled in with the campsite's current values. */
    @Test
    fun editFormIsPrefilled() {
        val campsite = Campsite(
            UID = "camp1",
            name = "Pine Valley",
            location = "Jasper, AB",
            description = "Fire pits and showers",
            max_occupancy = 6,
            price = 30.0f
        )
        campsite.addTag("shaded")

        compose_rule.setContent {
            campsiteFormContent(campsite = campsite, is_editing = true, on_back = {}, on_save = { null })
        }

        compose_rule.onNodeWithTag("campsiteNameInput").assertTextContains("Pine Valley")
        compose_rule.onNodeWithTag("campsiteLocationInput").assertTextContains("Jasper, AB")
        compose_rule.onNodeWithTag("campsiteDescriptionInput").assertTextContains("Fire pits and showers")
        compose_rule.onNodeWithTag("maxOccupancyInput").performScrollTo().assertTextContains("6")
        compose_rule.onNodeWithTag("priceInput").performScrollTo().assertTextContains("30.0")
        compose_rule.onNodeWithTag("tagChip_shaded").performScrollTo().assertIsDisplayed()
    }
}
