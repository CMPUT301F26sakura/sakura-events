

package com.example.sakuraevents

/**
 * Represents an admin user in the app.
 *
 * Admins can view and remove campsites, profiles, and images.
 * They can also use the app as an entrant or campsite owner.
 *
 *
 * Reworked the admin code to use the team's shared classes instead of separate
 * admin models. FirestoreDriver will handle loading and saving objects to Firebase, so
 * Admin.kt will only contain admin-related functions. The database methods still need
 *to be connected once the driver is finished.
 */
class Admin {

    //TODO: Link Admin to the shared User class so admins
    // can also use entrant and owner features with the same account.

    /**
     * Gets all registered campsites.
     *
     * @return A list of campsites.
     */
    //fun getCampsites(): List<Campsite> {
        //TODO("Get campsites from FirestoreDriver")
    //}

    /**
     * Removes a registered campsite.
     *
     * @param campsite The campsite to remove.
     */
    //fun removeCampsite(campsite: Campsite) {
        //TODO("Connect campsite removal to FirestoreDriver")
    //}

    /**
     * Gets all user profiles.
     *
     * @return A list of user profiles.
     */
    fun getProfiles(): List<String> {
        TODO("Get profiles from FirestoreDriver")
    }

    /**
     * Removes a user profile.
     *
     * @param profile_uid UID of the profile to remove.
     */
    fun removeProfile(profile_uid: String) {
        TODO("Connect profile removal to FirestoreDriver")
    }

    /**
     * Removes an uploaded image.
     *
     * @param image_uid UID of the image to remove.
     */
    fun removeImage(image_uid: String) {
        TODO("Connect image removal to FirestoreDriver")
    }

    /**
     * Checks if the admin can use entrant features.
     *
     * @return True if entrant features are available.
     */
    fun canUseEntrantFeatures(): Boolean {
        TODO("Connect to the shared user role system")
    }

    /**
     * Checks if the admin can use campsite owner features.
     *
     * @return True if owner features are available.
     */
    fun canUseOwnerFeatures(): Boolean {
        TODO("Connect to the shared user role system")
    }
}
