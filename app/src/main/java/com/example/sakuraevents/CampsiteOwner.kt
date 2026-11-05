// Purpose: Defines the CampsiteOwner model, which stores an owner's contact
// information and the UIDs of the campsites they own.

package com.example.sakuraevents

/**
 * Represents a campsite owner who can register and manage campsites.
 *
 * A campsite owner has a unique ID, a name, an email, a phone number, and a list of
 * the UIDs of the campsites they own. Campsite UIDs are stored instead of Campsite objects
 * so that the owner can be saved to and loaded from the database easily.
 *
 * @property UID the unique ID of this owner, used to find them in the database.
 * @property name the full name of the owner.
 * @property email the email address of the owner.
 * @property phone_number the phone number of the owner, stored as a string (eg: '780-555-1234').
 * @property _campsite_uids the private, editable list of UIDs of every campsite this owner has registered.
 *      Only this class can change it, through setCampsiteUids, addCampsiteUid and removeCampsiteUid.
 * @property campsite_uids a public, read-only view of _campsite_uids. Other code can read it but not change it,
 *      and it always shows the owner's current list.
 */
class CampsiteOwner(
    val UID: String = "",
    private var name: String = "",
    private var email: String = "",
    private var phone_number: String = "",
    private var _campsite_uids: MutableList<String> = mutableListOf()
) {

    val campsite_uids: List<String> get() = _campsite_uids

    /**
     * This getter simply returns the name of the owner.
     *
     * there are no parameters for this method. It acts directly on the class properties.
     * @return the owner's name as a String.
     */
    fun getName(): String {
        return name
    }

    /**
     * This setter replaces the name of the owner.
     *
     * @param name the new name of the owner.
     * @return Nothing
     */
    fun setName(name: String) {
        this.name = name
    }

    /**
     * This getter simply returns the email address of the owner.
     *
     * there are no parameters for this method. It acts directly on the class properties.
     * @return the owner's email address as a String.
     */
    fun getEmail(): String {
        return email
    }

    /**
     * This setter replaces the email address of the owner.
     *
     * @param email the new email address of the owner.
     * @return Nothing
     */
    fun setEmail(email: String) {
        this.email = email
    }

    /**
     * This getter simply returns the phone number of the owner.
     *
     * there are no parameters for this method. It acts directly on the class properties.
     * @return the owner's phone number as a String.
     */
    fun getPhoneNumber(): String {
        return phone_number
    }

    /**
     * This setter replaces the phone number of the owner.
     *
     * @param phone_number the new phone number of the owner.
     * @return Nothing
     */
    fun setPhoneNumber(phone_number: String) {
        this.phone_number = phone_number
    }

    /**
     * This getter returns a copy of the UIDs of the campsites this owner has registered.
     * A copy is returned so that code outside this class cannot change the owner's list directly.
     *
     * there are no parameters for this method. It acts directly on the class properties.
     * @return a read-only List of campsite UIDs.
     */
    fun getCampsiteUids(): List<String> {
        return _campsite_uids.toList()
    }

    /**
     * This setter replaces the whole list of campsite UIDs for this owner.
     * The given list is copied, so later changes to it do not affect this owner.
     *
     * @param campsite_uids the new list of campsite UIDs.
     * @return Nothing
     */
    fun setCampsiteUids(campsite_uids: List<String>) {
        this._campsite_uids = campsite_uids.toMutableList()
    }

    /**
     * This method adds a campsite UID to this owner's list when they register a new campsite.
     * Blank UIDs and UIDs already in the list are ignored.
     *
     * @param campsite_uid the UID of the campsite to add.
     * @return Nothing
     */
    fun addCampsiteUid(campsite_uid: String) {
        if (campsite_uid.isNotBlank() && !_campsite_uids.contains(campsite_uid)) {
            _campsite_uids.add(campsite_uid)
        }
    }

    /**
     * This method removes a campsite UID from this owner's list.
     * If the UID is not in the list, nothing happens.
     *
     * @param campsite_uid the UID of the campsite to remove.
     * @return Nothing
     */
    fun removeCampsiteUid(campsite_uid: String) {
        _campsite_uids.remove(campsite_uid)
    }
}
