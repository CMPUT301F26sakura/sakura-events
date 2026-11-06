// Purpose: Unit tests for the CampsiteOwner model class, checking its constructor,
// getters, setters, and owned campsite list behaviour.
// Written with help from Anthropic, Claude (Claude Code), "Write unit tests for CampsiteOwner", 2026-11-04

package com.example.sakuraevents

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [CampsiteOwner]. These run on the computer (no emulator needed).
 */
class CampsiteOwnerTest {

    private val camp1 = Campsite(UID = "camp1", name = "Pine Valley", location = "Jasper, AB",
        description = "Forest site", max_occupancy = 6, price = 30.0f)
    private val camp2 = Campsite(UID = "camp2", name = "Lakeside", location = "Banff, AB",
        description = "Lake view", max_occupancy = 4, price = 45.0f)
    private val camp3 = Campsite(UID = "camp3", name = "Riverbend", location = "Canmore, AB",
        description = "By the river", max_occupancy = 8, price = 25.0f)

    /**
     * Creates an owner with sample data so each test starts from the same state.
     *
     * @return a CampsiteOwner who owns camp1.
     */
    private fun makeOwner(): CampsiteOwner {
        return CampsiteOwner(
            UID = "owner1",
            role = Role.OWNER,
            name = "Jane Doe",
            email = "jane@example.com",
            phone_number = "780-555-1234",
            _owned_campsites = mutableListOf(camp1)
        )
    }

    /** An owner made with only the required values should have an empty UID and no campsites. */
    @Test
    fun optionalValuesDefaultToEmpty() {
        val owner = CampsiteOwner(role = Role.OWNER, name = "", email = "", phone_number = "")
        assertEquals("", owner.UID)
        assertEquals("OWNER", owner.getRole())
        assertTrue(owner.owned_campsites.isEmpty())
    }

    /** Values given to the constructor should be returned by the getters and the view. */
    @Test
    fun constructorStoresValues() {
        val owner = makeOwner()
        assertEquals("owner1", owner.UID)
        assertEquals("Jane Doe", owner.getName())
        assertEquals("jane@example.com", owner.getEmail())
        assertEquals("780-555-1234", owner.getPhoneNumber())
        assertEquals(1, owner.owned_campsites.size)
        assertSame(camp1, owner.owned_campsites[0])
    }

    /** setName should replace the owner's name. */
    @Test
    fun setNameUpdatesName() {
        val owner = makeOwner()
        owner.setName("John Smith")
        assertEquals("John Smith", owner.getName())
    }

    /** setEmail should replace the owner's email. */
    @Test
    fun setEmailUpdatesEmail() {
        val owner = makeOwner()
        owner.setEmail("john@example.com")
        assertEquals("john@example.com", owner.getEmail())
    }

    /** setPhoneNumber should replace the owner's phone number. */
    @Test
    fun setPhoneNumberUpdatesPhoneNumber() {
        val owner = makeOwner()
        owner.setPhoneNumber("587-555-9876")
        assertEquals("587-555-9876", owner.getPhoneNumber())
    }

    /** addCampsite should add a new campsite to the end of the list. */
    @Test
    fun addCampsiteAddsNewCampsite() {
        val owner = makeOwner()
        owner.addCampsite(camp2)
        assertEquals(listOf(camp1, camp2), owner.owned_campsites)
    }

    /** Adding a campsite that is already in the list should not create a duplicate. */
    @Test
    fun addCampsiteIgnoresDuplicate() {
        val owner = makeOwner()
        owner.addCampsite(camp1)
        assertEquals(listOf(camp1), owner.owned_campsites)
    }

    /** removeCampsite should remove a campsite that is in the list. */
    @Test
    fun removeCampsiteRemovesCampsite() {
        val owner = makeOwner()
        owner.removeCampsite(camp1)
        assertTrue(owner.owned_campsites.isEmpty())
    }

    /** Removing a campsite that is not in the list should leave the list unchanged. */
    @Test
    fun removeCampsiteIgnoresMissingCampsite() {
        val owner = makeOwner()
        owner.removeCampsite(camp3)
        assertEquals(listOf(camp1), owner.owned_campsites)
    }

    /** A view read before changes should show the owner's current list afterwards. */
    @Test
    fun ownedCampsitesViewShowsLaterChanges() {
        val owner = makeOwner()
        val view = owner.owned_campsites
        owner.addCampsite(camp2)
        assertEquals(listOf(camp1, camp2), view)
        owner.removeCampsite(camp1)
        assertEquals(listOf(camp2), view)
    }
}