
package com.example.sakuraevents

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Handles the database operations for admins.
 *
 * Admins can browse and remove campsites, profiles, and images.
 * They can also remove campsite owners and view notification
 * and message logs.
 *
 *
 */

//google gemini our school account was used to explain the logic for lab 5 to help me implenet this, Thinking "explain this lab 5 description"
class AdminRepository {

    private val db = FirebaseFirestore.getInstance()
    val campsites = mutableStateListOf<AdminCampsite>()

    init {
        // Listen for changes to the campsite collection
        db.collection("campsites").addSnapshotListener { snapshot, error ->
            if (error == null && snapshot != null) {
                campsites.clear() // Remove old campsite data

                for (doc in snapshot.documents) {
                    // Convert each document into a campsite object
                    val campsite = doc.toObject(AdminCampsite::class.java)

                    if (campsite != null) {
                        // Use the Firestore document ID for the campsite
                        campsites.add(campsite.copy(id = doc.id))
                    }
                }
            }
        }
    }

    /**
     * Removes a campsite from Firestore.
     *
     * @param id The ID of the campsite to remove.
     */
    fun deleteCampsite(id: String) {
        db.collection("campsites").document(id).delete()
    }


    // TODO: Profile management
    // Browse user profiles (US 03.05.01)
    // Remove user profiles (US 03.02.01)

    // TODO: Image management
    // Browse uploaded images (US 03.06.01)
    // Remove uploaded images (US 03.03.01)

    // TODO: Campsite owner management
    // Remove owners who violate app policy (US 03.07.01)

    // TODO: Notification logs
    // View all notification logs (US 03.08.01)

    // TODO: Message logs
    // View owner-to-entrant message logs (US 03.12.01)

    // TODO: Admin roles
    // Allow admins to also act as owners and entrants (US 03.11.01)
}
