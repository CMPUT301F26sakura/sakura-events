/**
 * File: OwnerLotteryScreenTest.kt
 * Purpose: Intent (UI) tests for the owner lottery screens: listing lotteries, creating one,
 * editing one, deleting one, and checking that invalid fields are highlighted on their own.
 *
 * Target Classes: OwnerLotteryScreen.kt, OwnerCampsiteListScreen.kt ("My Lotteries" button)
 *
 * User Stories Tested:
 * - US 02.04.01: As a campsite owner, I want to list my campsite in a lottery for a certain period.
 * - US 02.04.02: As a campsite owner, I want be able to list multiple campsites in a lottery.
 * - US 02.04.03: As a campsite owner, I want to set a registration period.
 * - US 02.04.06: As a campsite owner, I want to update my campsite lottery listing.
 *
 * These tests use fake Lottery and Campsite objects instead of Firestore, so they are fast, need no
 * internet, and never change the team's database. lotteryScreensHost below stands in for FirestoreDriver.
 *
 * Outstanding Issues:
 * - Picking a date through the Material calendar dialog is not driven here; tests start from a
 *   lottery that already has dates. Date reading/writing is covered by LotteryFormTest.
 * - Saving to Firestore is not covered here.
 */
// Name: Arham Ahmed
// Written with help from Anthropic, Claude (Claude Code), "Write intent tests for the owner lottery screens", 2026-11-26

package com.example.sakuraevents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Stands in for FirestoreDriver during tests. Saved lotteries go into the given list instead of
 * Firestore, and every save is also recorded in saves so tests can check what was sent.
 *
 * @param lotteries the fake lotteries shown in the list.
 * @param campsites the fake owner's campsites.
 * @param saves records each save as (lottery UID or null for new, form values).
 */
@Composable
private fun lotteryScreensHost(
    lotteries: SnapshotStateList<Lottery>,
    campsites: List<Campsite>,
    saves: MutableList<Pair<String?, LotteryFormValues>>
) {
    ownerLotteryContent(
        lotteries = lotteries.toList(),
        owned_campsites = campsites,
        on_back = {},
        on_save = { uid, values ->
            saves.add(uid to values)
            if (uid == null) {
                lotteries.add(buildLotteryFromForm("new${lotteries.size}", values))
            } else {
                val index = lotteries.indexOfFirst { it.UID == uid }
                lotteries[index] = buildLotteryFromForm(uid, values)
            }
        },
        on_delete = { lottery -> lotteries.remove(lottery) }
    )
}

/** Matches a node that is shown in its error state (for example, an OutlinedTextField with isError). */
private val is_error = SemanticsMatcher.keyIsDefined(SemanticsProperties.Error)

/** Matches a node that is not in its error state. */
private val is_not_error = SemanticsMatcher.keyNotDefined(SemanticsProperties.Error)

/**
 * Intent tests for the owner lottery screens.
 */
@RunWith(AndroidJUnit4::class)
class OwnerLotteryScreenTest {

    @get:Rule
    val compose_rule = createComposeRule()

    private val camp1 = Campsite(UID = "camp1", name = "Pine Valley", location = "Jasper, AB",
        description = "", max_occupancy = 6, price = 30.0f)
    private val camp2 = Campsite(UID = "camp2", name = "Lakeside", location = "Banff, AB",
        description = "", max_occupancy = 4, price = 45.0f)

    /**
     * Makes a fake lottery with one campsite.
     *
     * @param uid the lottery's UID.
     * @param start the registration start date.
     * @param end the registration end date.
     * @return a new Lottery with camp1 and 2 winners.
     */
    private fun makeLottery(uid: String, start: String, end: String): Lottery {
        return buildLotteryFromForm(uid, LotteryFormValues(listOf(camp1), start, end, 2))
    }

    /** The list should show each lottery's campsites, registration period, and winner count. */
    @Test
    fun listShowsOwnersLotteries() {
        val lotteries = mutableStateListOf(makeLottery("1", "2027-05-01", "2027-05-07"))
        compose_rule.setContent { lotteryScreensHost(lotteries, listOf(camp1, camp2), mutableListOf()) }

        compose_rule.onNodeWithTag("lotteryRow_1").assertIsDisplayed()
        compose_rule.onNodeWithText("Pine Valley").assertIsDisplayed()
        compose_rule.onNodeWithText("Registration: 2027-05-01 to 2027-05-07").assertIsDisplayed()
        compose_rule.onNodeWithText("Winners: 2").assertIsDisplayed()
    }

    /** An owner with no lotteries should see a message instead of an empty list. */
    @Test
    fun emptyListShowsMessage() {
        compose_rule.setContent { lotteryScreensHost(mutableStateListOf(), listOf(camp1), mutableListOf()) }

        compose_rule.onNodeWithTag("noLotteriesText").assertIsDisplayed()
    }

    /**
     * Saving an empty form should highlight every field that needs fixing, each with its own
     * message, and should not save anything (US 02.04.01, US 02.04.03).
     */
    @Test
    fun emptyFormHighlightsEachInvalidField() {
        val saves = mutableListOf<Pair<String?, LotteryFormValues>>()
        compose_rule.setContent { lotteryScreensHost(mutableStateListOf(), listOf(camp1, camp2), saves) }

        compose_rule.onNodeWithTag("createLotteryButton").performClick()
        compose_rule.onNodeWithTag("saveLotteryButton").performScrollTo().performClick()

        compose_rule.onNodeWithTag("lotteryCampsitesError", useUnmergedTree = true).assertExists()
        compose_rule.onNodeWithTag("lotteryStartDateField").assert(is_error)
        compose_rule.onNodeWithTag("lotteryStartDateError", useUnmergedTree = true).assertExists()
        compose_rule.onNodeWithTag("lotteryEndDateField").assert(is_error)
        compose_rule.onNodeWithTag("lotteryEndDateError", useUnmergedTree = true).assertExists()
        compose_rule.onNodeWithTag("lotteryCapacityError", useUnmergedTree = true).assertExists()
        compose_rule.onNodeWithTag("lotteryFormErrorText").assertExists()
        assertTrue(saves.isEmpty())
    }

    /** Ticking a campsite after a failed save should clear only the campsite highlight. */
    @Test
    fun fixingAFieldClearsOnlyItsHighlight() {
        compose_rule.setContent { lotteryScreensHost(mutableStateListOf(), listOf(camp1, camp2), mutableListOf()) }

        compose_rule.onNodeWithTag("createLotteryButton").performClick()
        compose_rule.onNodeWithTag("saveLotteryButton").performScrollTo().performClick()
        compose_rule.onNodeWithTag("lotteryCampsiteCheckbox_camp1").performScrollTo().performClick()

        compose_rule.onNodeWithTag("lotteryCampsitesError", useUnmergedTree = true).assertDoesNotExist()
        compose_rule.onNodeWithTag("lotteryStartDateField").assert(is_error)
    }

    /**
     * Editing a lottery so its end date is before its start date should highlight only the end
     * date and not save (US 02.04.03, US 02.04.06).
     */
    @Test
    fun endBeforeStartHighlightsEndDateOnly() {
        val saves = mutableListOf<Pair<String?, LotteryFormValues>>()
        val lotteries = mutableStateListOf(makeLottery("1", "2027-05-07", "2027-05-01"))
        compose_rule.setContent { lotteryScreensHost(lotteries, listOf(camp1, camp2), saves) }

        compose_rule.onNodeWithTag("editLotteryButton_1").performClick()
        compose_rule.onNodeWithTag("saveLotteryButton").performScrollTo().performClick()

        compose_rule.onNodeWithTag("lotteryEndDateField").assert(is_error)
        compose_rule.onNodeWithText("End date must be after the start date.", useUnmergedTree = true).assertExists()
        compose_rule.onNodeWithTag("lotteryStartDateField").assert(is_not_error)
        compose_rule.onNodeWithTag("lotteryCampsitesError", useUnmergedTree = true).assertDoesNotExist()
        assertTrue(saves.isEmpty())
    }

    /**
     * Editing a lottery to use several campsites and a new winner count should save those changes
     * under the same UID and show them in the list (US 02.04.02, US 02.04.06).
     */
    @Test
    fun editingLotterySavesMultipleCampsitesAndWinners() {
        val saves = mutableListOf<Pair<String?, LotteryFormValues>>()
        val lotteries = mutableStateListOf(makeLottery("1", "2027-05-01", "2027-05-07"))
        compose_rule.setContent { lotteryScreensHost(lotteries, listOf(camp1, camp2), saves) }

        compose_rule.onNodeWithTag("editLotteryButton_1").performClick()

        // The form starts from the lottery's current values.
        compose_rule.onNodeWithTag("lotteryCampsiteCheckbox_camp1").assertIsOn()
        compose_rule.onNodeWithTag("lotteryCampsiteCheckbox_camp2").assertIsOff()

        compose_rule.onNodeWithTag("lotteryCampsiteCheckbox_camp2").performScrollTo().performClick()
        compose_rule.onNodeWithTag("winner_count_input").performScrollTo().performTextClearance()
        compose_rule.onNodeWithTag("winner_count_input").performTextInput("5")
        compose_rule.onNodeWithTag("lottery_winner_count_saved").performClick()
        compose_rule.onNodeWithTag("saveLotteryButton").performScrollTo().performClick()

        // Saved once, under the same UID, with both campsites and the new winner count.
        assertEquals(1, saves.size)
        val (uid, values) = saves[0]
        assertEquals("1", uid)
        assertEquals(listOf("camp1", "camp2"), values.campsites.map { it.UID })
        assertEquals(5, values.capacity)
        assertEquals("2027-05-01", values.start_date)
        assertEquals("2027-05-07", values.end_date)

        // Back on the list, the change is shown.
        compose_rule.onNodeWithText("Pine Valley, Lakeside").assertIsDisplayed()
        compose_rule.onNodeWithText("Winners: 5").assertIsDisplayed()
    }

    /** Deleting a lottery should ask first, then remove it from the list. */
    @Test
    fun deletingLotteryAsksThenRemovesIt() {
        val lotteries = mutableStateListOf(makeLottery("1", "2027-05-01", "2027-05-07"))
        compose_rule.setContent { lotteryScreensHost(lotteries, listOf(camp1), mutableListOf()) }

        compose_rule.onNodeWithTag("deleteLotteryButton_1").performClick()
        compose_rule.onNodeWithTag("confirmDeleteLotteryButton").performClick()

        compose_rule.onNodeWithTag("lotteryRow_1").assertDoesNotExist()
        assertTrue(lotteries.isEmpty())
    }

    /** The "My Lotteries" button on the campsite list should open the lottery page. */
    @Test
    fun myLotteriesButtonOpensLotteries() {
        var opened = false
        compose_rule.setContent {
            ownerCampsiteListContent(
                owner_found = true,
                campsites = listOf(camp1),
                show_debug_button = false,
                on_back = {},
                on_create_test_owner = {},
                on_register = {},
                on_campsite_click = {},
                on_my_lotteries = { opened = true }
            )
        }

        compose_rule.onNodeWithTag("myLotteriesButton").performClick()

        assertTrue(opened)
    }
}
