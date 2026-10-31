package com.example.sakuraevents

class User (
    val userId: String,
    var userName: String,
    var userEmail: String,
    var phoneNumber: String
){
    fun updateUserName(newName: String) {
        userName = newName
    }
    fun updatePhoneNumber(newPhoneNumber: String) {
        phoneNumber = newPhoneNumber
    }
    fun updateEmail(newEmail: String) {
        userEmail = newEmail
    }
}
