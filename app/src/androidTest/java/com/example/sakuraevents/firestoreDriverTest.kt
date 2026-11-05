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
    }

    @Test
    fun insertCampsite() {
        val campsite: Campsite = Campsite(name = "Beep", location = "Sheep", description = "maximus", max_occupancy = 15, price = 1.15f)
        val myTask = evilFirestore.addCampsite(campsite)
        assertEquals("0", (evilFirestore.campsites["0"] as Campsite).UID)
        //assert(evilFirestore.campsites.isEmpty())


        //val task = evilFirestore.db.collection("TEST").document("TSET").set(mapOf("Hello" to "World"))

        //Tasks.await(task) // Wait for Firestore; throws if the write fails

        /*evilFirestore.db
            .collection("TEST")
            .document("TSET")
            .set(mapOf("Hello" to "World"))
            .addOnSuccessListener {
                println("Write succeeded")
            }
            .addOnFailureListener { error ->
                error.printStackTrace() // Look for PERMISSION_DENIED, etc.
            }
        */
    }
}