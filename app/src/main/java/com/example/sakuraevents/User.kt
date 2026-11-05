package com.example.sakuraevents



/**
 * Represents a user of the Sakura Events app.
 *
 * A user has an Id, name, email, and phone number.
 * The user can update their personal information.
 *
 * @property UID the unique Id for the entrant.
 * @property userName  the name of the user.
 * @property userEmail  the email of the user.
 * @property phoneNumber  the phone number of the user.
 */

open class User (
    val UID: String= "",
    private var userName: String,
    private var userEmail: String,
    private var phoneNumber: String
){
    fun getUserName(): String {
        return userName
    }
    fun getPhoneNumber(): String {
        return phoneNumber
    }
    fun getEmail(): String {
        return userEmail
    }
    fun updateUserName(newName: String) {
        userName = newName
    }
    fun updateEmail(newEmail: String) {
        userEmail = newEmail
    }
    fun updatePhoneNumber(newPhoneNumber: String) {
        phoneNumber = newPhoneNumber
    }
}
