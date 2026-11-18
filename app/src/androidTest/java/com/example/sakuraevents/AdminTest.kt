
/**
 * File: AdminTest.kt
 * Purpose: Tests the Admin dashboard, campsite browsing and removal,
 * profile browsing and removal, image removal, and admin roles.
 *
 * Target Classes: Admin.kt, AdminScreen.kt
 *
 * User Stories Tested:
 * - US 03.01.01: As an admin, I want to remove campsites.
 * - US 03.02.01: As an admin, I want to remove profiles.
 * - US 03.03.01: As an admin, I want to remove images.
 * - US 03.04.01: As an admin, I want to browse campsites.
 * - US 03.05.01: As an admin, I want to browse profiles.
 * - US 03.11.01: As an admin, I want to also be an entrant or campsite owner.
 *
 * Outstanding Issues:
 * - Image removal is tested with sample data, not Firebase.
 * - Firebase deletion and actual role switching are not tested here.
 */

package com.example.sakuraevents

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests the Admin screens and basic admin functions.
 */
@RunWith(AndroidJUnit4::class) // needed as run emulator to test the buttos etc
class AdminTest {

    @get:Rule
    val compose_rule = createComposeRule()

    private lateinit var campsite: Campsite
    private lateinit var entrant: Entrant
    private lateinit var admin: Admin

    /**
     * Creates sample objects before each test.
     */
    @Before
    fun setUp() {
        campsite = Campsite(
            UID = "0",
            name = "Pine Ridge",
            location = "Banff, Alberta",
            description = "Campsite near hiking trails",
            max_occupancy = 4,
            price = 50.0f
        )

        entrant = Entrant(
            UID = "1",
            role = Role.ENTRANT,
            name = "Bob",
            email = "b@example.com",
            phone_number = "7805550105"
        )

        admin = Admin(
            UID = "2",
            role = Role.ADMIN,
            name = "Sponge",
            email = "sponge@example.com",
            phone_number = "7805550105"
        )
    }

    /**
     * Checks that campsites are displayed and the correct campsite
     * is selected when the Remove button is clicked.
     *
     * US 03.01.01, US 03.04.01
     */
    @Test
    fun testCampsite() {
        var removed = false  //check if click button calls removal func

        compose_rule.setContent {
            AdminCampsiteScreen(
                campsites = listOf(campsite),
                on_remove = { removed = it.UID == campsite.UID },
                on_back = {}
            )
        }

        // Check the campsite is displayed.
        compose_rule.onNodeWithText("Pine Ridge").assertExists()
        compose_rule.onNodeWithText("Banff, Alberta").assertExists()

        // Click Remove and check the selected campsite.
        compose_rule.onNodeWithText("Remove").performClick()
        assertTrue(removed)
    }

    /**
     * Checks that user profiles are displayed and the correct
     * profile is selected for removal.
     *
     * US 03.02.01, US 03.05.01
     */
    @Test
    fun testProfile() {
        var removed = false

        compose_rule.setContent {
            AdminProfileScreen(
                profiles = listOf(entrant),
                on_remove = { removed = it.UID == entrant.UID },
                on_back = {}
            )
        }

        // Check the entrant's information.
        compose_rule.onNodeWithText("Alex Chen").assertExists()
        compose_rule.onNodeWithText("alex@example.com").assertExists()
        compose_rule.onNodeWithText("Role: ENTRANT").assertExists()

        // Check the Remove button.
        compose_rule.onNodeWithText("Remove").performClick()
        assertTrue(removed)
    }

    /**
     * Checks that an image is displayed and its Remove button
     * passes the correct image identifier.
     *
     * US 03.03.01
     */
    @Test
    fun testImage() {
        var removed_image = ""

        compose_rule.setContent {
            AdminImageScreen(
                images = listOf("Pine Ridge photo"),
                on_remove = { removed_image = it },
                on_back = {}
            )
        }

        // Check the image is listed.
        compose_rule.onNodeWithText("Pine Ridge photo").assertExists()

        // Check the correct image was selected.
        compose_rule.onNodeWithText("Remove").performClick()
        assertEquals("Pine Ridge photo", removed_image)
    }

    /**
     * Checks that the Admin dashboard displays the available
     * management options and role buttons.
     */
    @Test
    fun testDashboard() {
        compose_rule.setContent {
            AdminScreen(
                on_back = {},
                on_campsites = {},
                on_profiles = {},
                on_images = {},
                on_entrant = {},
                on_owner = {}
            )
        }

        // Check all the dashboard options.
        compose_rule.onNodeWithText("Admin Dashboard").assertExists()
        compose_rule.onNodeWithText("Manage Campsites").assertExists()
        compose_rule.onNodeWithText("Manage Profiles").assertExists()
        compose_rule.onNodeWithText("Manage Images").assertExists()
        compose_rule.onNodeWithText("Continue as Entrant").assertExists()
        compose_rule.onNodeWithText("Continue as Campsite Owner").assertExists()
    }

    /**
     * Checks that Admin keeps its UID and role and can use
     * the campsite owner functions through inheritance.
     *
     * US 03.11.01
     */
    @Test
    fun testAdminRoles() {

        // Admin inherits addCampsite and removeCampsite from CampsiteOwner.
        admin.addCampsite(campsite)

        assertEquals("2", admin.UID)
        assertEquals("ADMIN", admin.getRole())
        assertTrue(admin.owned_campsites.contains(campsite))

        // Remove the campsite from the owner's list.
        admin.removeCampsite(campsite)
        assertTrue(admin.owned_campsites.isEmpty())
    }
}
