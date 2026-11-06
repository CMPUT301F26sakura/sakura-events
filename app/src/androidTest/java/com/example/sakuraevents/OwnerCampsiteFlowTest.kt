/**
 * File: OwnerCampsiteFlowTest.kt
 * Purpose: Intent (UI) tests for the campsite owner screens, moving between the campsite list,
 * the campsite form, and the campsite details the way an owner would.
 *
 * Target Classes: OwnerCampsiteListScreen.kt, CampsiteFormScreen.kt, CampsiteDetailsScreen.kt
 *
 * User Stories Tested:
 * - US 02.01.01: As a campsite owner, I want to register a campsite.
 * - US 02.01.02: As a campsite owner, I want to provide campsite details.
 * - US 02.01.03: As a campsite owner, I want to update campsite details.
 * - US 02.01.04: As a campsite owner, I want to view the details of my campsite.
 * - US 02.03.01: As a campsite owner, I want to view a list of my campsites.
 *
 * These tests use fake CampsiteOwner and Campsite objects instead of Firestore, so they are fast,
 * need no internet, and never change the team's database. The small ownerScreensHost below stands
 * in for the app's navigation and FirestoreDriver.
 *
 * Outstanding Issues:
 * - Saving to Firestore and the real page navigation are not covered here.
 */
// Written with help from Anthropic, Claude (Claude Code), "Write intent tests for the owner campsite screens", 2026-11-05

package com.example.sakuraevents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Stands in for the app's navigation during tests. It shows one owner screen at a time and
 * switches between them, saving changes to the fake owner and campsites instead of Firestore.
 *
 * @param owner the fake CampsiteOwner whose campsites are shown.
 */
@Composable
private fun ownerScreensHost(owner: CampsiteOwner) {
    var page by remember { mutableStateOf("list") }
    var selected_campsite by remember { mutableStateOf<Campsite?>(null) }

    when (page) {
        "list" -> ownerCampsiteListContent(
            owner_found = true,
            campsites = owner.owned_campsites.toList(),
            show_debug_button = false,
            on_back = {},
            on_create_test_owner = {},
            on_register = { page = "register" },
            on_campsite_click = { campsite ->
                selected_campsite = campsite
                page = "details"
            }
        )
        "register" -> campsiteFormContent(
            campsite = null,
            is_editing = false,
            on_back = { page = "list" },
            on_save = { values ->
                owner.addCampsite(buildCampsiteFromForm("new_${owner.owned_campsites.size}", values))
                page = "list"
                null
            }
        )
        "details" -> campsiteDetailsContent(
            campsite = selected_campsite,
            on_back = { page = "list" },
            on_edit = { page = "edit" }
        )
        "edit" -> campsiteFormContent(
            campsite = selected_campsite,
            is_editing = true,
            on_back = { page = "details" },
            on_save = { values ->
                applyFormToCampsite(selected_campsite!!, values)
                page = "details"
                null
            }
        )
    }
}

/**
 * Intent tests for the owner campsite screens.
 */
@RunWith(AndroidJUnit4::class)
class OwnerCampsiteFlowTest {

    @get:Rule
    val compose_rule = createComposeRule()

    /**
     * Makes a fake owner with no campsites.
     *
     * @return a new CampsiteOwner.
     */
    private fun makeOwner(): CampsiteOwner {
        return CampsiteOwner(
            UID = "owner_test",
            role = Role.OWNER,
            name = "Test Owner",
            email = "test.owner@example.com",
            phone_number = "780-555-0000"
        )
    }

    /**
     * Makes a fake campsite with one tag.
     *
     * @param uid the campsite's UID.
     * @param name the campsite's name.
     * @return a new Campsite.
     */
    private fun makeCampsite(uid: String, name: String): Campsite {
        val campsite = Campsite(
            UID = uid,
            name = name,
            location = "Jasper, AB",
            description = "Fire pits and showers",
            max_occupancy = 6,
            price = 30.0f
        )
        campsite.addTag("shaded")
        return campsite
    }

    /** The list should show every campsite the owner has (US 02.03.01). */
    @Test
    fun listShowsOwnersCampsites() {
        val owner = makeOwner()
        owner.addCampsite(makeCampsite("camp1", "Pine Valley"))
        owner.addCampsite(makeCampsite("camp2", "Lakeside"))

        compose_rule.setContent { ownerScreensHost(owner) }

        compose_rule.onNodeWithText("Pine Valley").assertIsDisplayed()
        compose_rule.onNodeWithText("Lakeside").assertIsDisplayed()
    }

    /** An owner with no campsites should see a message instead of an empty list (US 02.03.01). */
    @Test
    fun emptyListShowsMessage() {
        compose_rule.setContent { ownerScreensHost(makeOwner()) }

        compose_rule.onNodeWithText("You have no campsites yet.").assertIsDisplayed()
    }

    /**
     * Registering a campsite with every detail should add it to the owner and show it in the
     * list (US 02.01.01, US 02.01.02).
     */
    @Test
    fun registerCampsiteWithAllDetailsShowsInList() {
        val owner = makeOwner()
        compose_rule.setContent { ownerScreensHost(owner) }

        compose_rule.onNodeWithTag("registerCampsiteButton").performClick()
        compose_rule.onNodeWithTag("campsiteNameInput").performTextInput("Riverbend")
        compose_rule.onNodeWithTag("campsiteLocationInput").performTextInput("Canmore, AB")
        compose_rule.onNodeWithTag("campsiteDescriptionInput").performTextInput("By the river, has a boat launch")
        compose_rule.onNodeWithTag("maxOccupancyInput").performScrollTo().performTextInput("8")
        compose_rule.onNodeWithTag("priceInput").performScrollTo().performTextInput("25.50")
        compose_rule.onNodeWithTag("tagInput").performScrollTo().performTextInput("pet friendly")
        compose_rule.onNodeWithTag("addTagButton").performClick()
        compose_rule.onNodeWithTag("tagInput").performTextInput("lakeside")
        compose_rule.onNodeWithTag("addTagButton").performClick()
        compose_rule.onNodeWithTag("saveCampsiteButton").performScrollTo().performClick()

        // Back on the list, the new campsite is shown.
        compose_rule.onNodeWithText("Riverbend").assertIsDisplayed()

        // The owner now holds the campsite, with every detail that was entered.
        assertEquals(1, owner.owned_campsites.size)
        val campsite = owner.owned_campsites[0]
        assertEquals("Riverbend", campsite.getName())
        assertEquals("Canmore, AB", campsite.getLocation())
        assertEquals("By the river, has a boat launch", campsite.getDescription())
        assertEquals(8, campsite.getMaxOccupancy())
        assertEquals(25.5f, campsite.getPrice())
        assertEquals(listOf("pet friendly", "lakeside"), campsite.tags)
    }

    /** Tapping a campsite in the list should open its details (US 02.01.04). */
    @Test
    fun openingCampsiteShowsItsDetails() {
        val owner = makeOwner()
        owner.addCampsite(makeCampsite("camp1", "Pine Valley"))
        compose_rule.setContent { ownerScreensHost(owner) }

        compose_rule.onNodeWithTag("campsiteRow_camp1").performClick()

        compose_rule.onNodeWithText("Campsite Details").assertIsDisplayed()
        compose_rule.onNodeWithText("Pine Valley").assertIsDisplayed()
        compose_rule.onNodeWithText("Jasper, AB").assertIsDisplayed()
        compose_rule.onNodeWithText("Fire pits and showers").assertIsDisplayed()
        compose_rule.onNodeWithText("6").assertIsDisplayed()
        compose_rule.onNodeWithText("$%.2f".format(30.0f)).assertIsDisplayed()
        compose_rule.onNodeWithText("shaded").assertIsDisplayed()
    }

    /** Editing a campsite should save the changes and show them in its details (US 02.01.03). */
    @Test
    fun editingCampsiteSavesChange() {
        val owner = makeOwner()
        val campsite = makeCampsite("camp1", "Pine Valley")
        owner.addCampsite(campsite)
        compose_rule.setContent { ownerScreensHost(owner) }

        compose_rule.onNodeWithTag("campsiteRow_camp1").performClick()
        compose_rule.onNodeWithTag("editCampsiteButton").performScrollTo().performClick()

        compose_rule.onNodeWithTag("campsiteNameInput").performTextClearance()
        compose_rule.onNodeWithTag("campsiteNameInput").performTextInput("Pine Valley North")
        compose_rule.onNodeWithTag("priceInput").performScrollTo().performTextClearance()
        compose_rule.onNodeWithTag("priceInput").performTextInput("40")
        compose_rule.onNodeWithTag("tagChip_shaded").performScrollTo().performClick()
        compose_rule.onNodeWithTag("saveCampsiteButton").performScrollTo().performClick()

        // Back on the details page, the new values are shown.
        compose_rule.onNodeWithText("Pine Valley North").assertIsDisplayed()
        compose_rule.onNodeWithText("$%.2f".format(40.0f)).assertIsDisplayed()

        // The Campsite object itself was changed.
        assertEquals("Pine Valley North", campsite.getName())
        assertEquals(40.0f, campsite.getPrice())
        assertEquals(emptyList<String>(), campsite.tags)
    }
}
