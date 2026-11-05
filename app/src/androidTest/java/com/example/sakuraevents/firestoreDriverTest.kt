package com.example.sakuraevents

import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import com.google.android.gms.tasks.Tasks
import kotlin.text.set
import com.google.firebase.auth.FirebaseAuth

class FirestoreDriverTest {

    private lateinit var evilFirestore: FirestoreDriver

    @Before
    fun setUp() {
        evilFirestore = FirestoreDriver()
        Thread.sleep(6_000)
    }

    @Test
    fun insertCampsite() {
        val campsite: Campsite = Campsite(name = "Blorp", location = "Sheep", description = "maximus", max_occupancy = 15, price = 1.15f)
        evilFirestore.addCampsite(campsite)
        assertEquals("0", (evilFirestore.campsites["0"] as Campsite).UID)
        assert(evilFirestore.campsites.isNotEmpty())

    }
}