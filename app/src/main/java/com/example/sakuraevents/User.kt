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
    private var name: String,
    private var email: String,
    private var phone_number: String
){
    fun getName(): String {
        return name
    }
    fun getPhoneNumber(): String {
        return phone_number
    }
    fun getEmail(): String {
        return email
    }
    fun setName(newName: String) {
        name = newName
    }
    fun setEmail(newEmail: String) {
        email = newEmail
    }
    fun setPhoneNumber(newPhoneNumber: String) {
        phone_number = newPhoneNumber
    }
}
