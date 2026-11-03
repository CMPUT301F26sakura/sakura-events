
package com.example.sakuraevents

/**
 * Stores campsite information used by the admin.
 *
 * @property id The campsite ID.
 * @property name The name of the campsite.
 * @property location The campsite location.
 */
data class AdminCampsite(
    val id: String = "",
    val name: String = "",
    val location: String = ""
)

/**
 * Stores the information for a user profile.
 *
 * @property id The user's ID.
 * @property name The user's name.
 * @property email The user's email.
 * @property roles The roles assigned to the user.
 * @property status Whether the profile is active or disabled.
 */
data class AdminProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val roles: List<String> = emptyList(),
    val status: String = "active"
) {
    /** Checks if the user has a certain role. */
    fun hasRole(role: String): Boolean {
        return roles.contains(role)
    }
}

/**
 * Stores information about an uploaded image.
 *
 * @property id The image ID.
 * @property campsiteId The campsite the image belongs to.
 * @property storagePath The location of the image in Firebase Storage.
 */
data class AdminImage(
    val id: String = "",
    val campsiteId: String = "",
    val storagePath: String = ""
)

/**
 * Stores information about a message or notification.
 *
 * @property id The log ID.
 * @property senderId The sender's ID.
 * @property recipientId The recipient's ID.
 * @property message The message content.
 */
data class AdminLog(
    val id: String = "",
    val senderId: String = "",
    val recipientId: String = "",
    val message: String = ""
)
