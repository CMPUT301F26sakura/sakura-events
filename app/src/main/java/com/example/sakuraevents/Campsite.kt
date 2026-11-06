/**
 * File: Campsite.kt
 * Purpose: Model class representing a campsite listing, including summary and full details,
 * tags, booking dates, ratings, waiting list management, QR code generation, and optional images.
 *
 * User Stories Addressed:
 * - US 01.01.04: As an entrant, I want to see campsite summary information including name, location, maximum occupancy, and rating.
 * - US 01.01.05: As an entrant, I want to see details of a campsite.
 * - US 01.01.06: As an entrant, I want to join an active lottery from the campsite details screen.
 * - US 01.02.01: As an entrant, I want to join the waiting list for a campsite lottery.
 * - US 01.02.02: As an entrant, I want to leave the waiting list for a campsite lottery.
 * - US 02.01.02: As a campsite owner, I want to provide campsite details including name, location, description (amenities), maximum occupancy, map location, optional image, and tags.
 * - US 02.01.03: As a campsite owner, I want to update details about my campsite.
 * - US 02.01.04: As a campsite owner, I want to view details of my campsite.
 * - US 02.02.01: As a campsite owner, I want the system to generate a unique QR code that links to the campsite details.
 * - US 02.04.04: As a campsite owner, I want to set a booking period.
 * - US 02.04.07: As a campsite owner, I want to optionally set a capacity for my lottery waitlist.
 * - US 03.03.01: As an admin, I want to remove images.
 */
package com.example.sakuraevents
/**
 * Represents a campsite in the Sakura Events application.
 *
 * Encapsulates mutable list properties via backing fields (`_tags`, `_booking_period_dates`,
 * `_reservations`, `_ratings`, `_waiting_list`) and exposes read-only [List] views publicly.
 *
 * @property UID Unique Firestore document identifier for this campsite.
 * @property name Display name of the campsite.
 * @property location Region or park location of the campsite.
 * @property description Amenities, rules, and general description of the campsite.
 * @property max_occupancy Maximum number of guests permitted on the campsite.
 * @property qr_code Unique QR code payload linking to this campsite's details screen.
 * @property price Nightly rate for the campsite.
 * @property _tags Backing mutable list of descriptive tags/amenities.
 * @property _booking_period_dates Backing mutable list of ISO date strings ("YYYY-MM-DD") available to book.
 * @property _reservations Backing mutable list of reservations associated with this campsite.
 * @property _ratings Backing mutable list of entrant rating pairs.
 * @property average_rating Current average star rating of the campsite.
 * @property _waiting_list Backing mutable list of entrants on the lottery waiting list.
 * @property waitlist_capacity Maximum capacity of the waiting list (0 indicates unlimited capacity).
 */

class Campsite(
    val UID: String = "",
    private var name: String,
    private var location: String,
    private var description: String,
    private var max_occupancy: Int,
    private var qr_code: String = "",
    private var price: Float,
    private var _tags: MutableList<String> = mutableListOf(),
    private var _booking_period_dates: MutableList<String> = mutableListOf(),
    private var _reservations: MutableList<Reservation> = mutableListOf(),
    private var _ratings: MutableList<Pair<Entrant, Float>> = mutableListOf(),
    private var average_rating: Float = 0.0f,
    private var _waiting_list: MutableList<Entrant> = mutableListOf(),
    private var waitlist_capacity: Int = 0 // 0 here indicates there is no limit.
) {

    val tags: List<String> get() = _tags
    val booking_period_dates: List<String> get() = _booking_period_dates
    val reservations: List<Reservation> get() = _reservations
    val ratings: List<Pair<Entrant, Float>> get() = _ratings
    val waiting_list: List<Entrant> get() = _waiting_list

    init {
        if (qr_code.isBlank() && (UID.isNotBlank() || name.isNotBlank())) {
            qr_code = generateQrCode()
        }
    }
    /**
     * Generates and assigns a unique QR code string linking to this campsite's details (US 02.02.01).
     * @return The generated QR code link string.
     */
    fun generateQrCode(): String {
        val identifier = UID.ifBlank { name.trim().lowercase().replace(" ", "_") }
        qr_code = "sakuraevents://campsite/$identifier"
        return qr_code
    }
    /**
     * Updates the name of the campsite (US 02.01.03).
     * @param name New campsite name.
     */
    fun setName(name: String){ // Set the name of the campsite
        this.name = name // Keyword "this" refers to the specific object instance in the code
        // Basically "this" keyword points to the current instance of that class
    }
    /**
     * Returns the current waiting list capacity (0 means unlimited) (US 02.04.07).
     * @return Waitlist capacity as an [Int].
     */
    fun getWaitlistCapacity(): Int {
        return waitlist_capacity
    }
    /**
     * Sets an optional capacity limit for the lottery waiting list (US 02.04.07).
     * @param new_capacity New capacity (0 for unlimited, or a positive integer).
     * @return `true` if the capacity was valid and updated, `false` otherwise.
     */
    fun setWaitlistCapacity(new_capacity: Int): Boolean {
        if (new_capacity >= 0) {
            waitlist_capacity = new_capacity
            return true
        }
        return false
    }
    /**
     * Adds an [Entrant] to the campsite lottery waiting list if capacity allows and the entrant
     * is not already on the list (US 01.01.06, US 01.02.01).
     * @param entrant The [Entrant] joining the waiting list.
     * @return `true` if the entrant was added, `false` if full or already registered.
     */
    fun addToWaitingList(entrant: Entrant): Boolean {
        if ((waitlist_capacity == 0 || _waiting_list.count() < waitlist_capacity) && !_waiting_list.any { it === entrant }) {
            _waiting_list.add(entrant)
            return true
        }
        return false
    }
    /**
     * Removes an [Entrant] from the campsite lottery waiting list (US 01.02.02).
     * @param entrant The [Entrant] leaving the waiting list.
     */
    fun removeFromWaitingList(entrant: Entrant) {
        _waiting_list.removeIf { it === entrant }
    }
    /**
     * Returns the campsite name (US 01.01.04).
     * @return The campsite name string.
     */
    fun getName(): String { // Returns campsite name
        return this.name
    }
    /**
     * Updates the campsite location (US 02.01.03).
     * @param location New location string.
     */
    fun setLocation(location: String) { // Set the campsite location
        this.location = location
    }
    /**
     * Returns the campsite location (US 01.01.04).
     * @return The campsite location string.
     */
    fun getLocation(): String { // Return the campsite location
        return this.location
    }
    /**
     * Updates the campsite description and amenities (US 02.01.03).
     * @param description New description string.
     */
    fun setDescription(description: String) { // Set the campsite description
        this.description = description
    }
    /**
     * Returns the campsite description and amenities (US 01.01.05).
     * @return The campsite description string.
     */
    fun getDescription(): String {
        return this.description // Return the description of the campsite
    }
    /**
     * Sets the maximum guest occupancy for the campsite (must be > 0) (US 02.01.02, US 02.01.03).
     * @param max_occupancy Maximum number of guests.
     */
    fun setMaxOccupancy(max_occupancy: Int) { // Set the max occupancy for campsite
        if (max_occupancy > 0) { // Max occupancy should be greater than 0
            this.max_occupancy = max_occupancy
        }
    }
    /**
     * Returns the maximum occupancy of the campsite (US 01.01.04).
     * @return Maximum occupancy as an [Int].
     */
    fun getMaxOccupancy(): Int { // Return the max occupancy
        return this.max_occupancy
    }
    /**
     * Sets the nightly price for the campsite (must be >= 0.0f) (US 02.01.02, US 02.01.03).
     * @param price Nightly price as a [Float].
     */
    fun setPrice(price: Float) { // Set the price for the campsite entry
        if (price >= 0.0f) { // Price can be zero if the campsite owner wishes free entry lol
            this.price = price
        }
    }
    /**
     * Returns the nightly price for the campsite (US 01.01.05).
     * @return Price as a [Float].
     */
    fun getPrice(): Float { // Return the price
        return this.price
    }
    /**
     * Sets the QR code payload string for this campsite (US 02.02.01).
     * @param qr_code The QR code payload string.
     */
    fun setQrCode(qr_code: String) {
        this.qr_code = qr_code
    }
    /**
     * Returns the QR code payload string for this campsite (US 02.02.01).
     * @return QR code string.
     */
    fun getQrCode(): String {
        return this.qr_code
    }
    /**
     * Adds a descriptive tag to the campsite if not already present (US 02.01.02, US 02.01.03).
     * @param tag Tag string to add.
     */
    fun addTag(tag: String) { // Add a tag to the campsite's tags if not already exists
        if (!_tags.contains(tag)) {
            _tags.add(tag)
        }
    }
    //fun getTags(): String { // This will return the campsite's tags as a comma-separated string
    //    return tags.joinToString(", ")
    //}
    /**
     * Removes a tag from the campsite's tag list (US 02.01.03).
     * @param tag Tag string to remove.
     */
    fun removeTag(tag: String) {
        _tags.remove(tag)
    }
    /**
     * Adds an available booking date to the campsite's booking period (US 02.04.04).
     * @param date Date string (e.g. "YYYY-MM-DD").
     */
    fun addBookingPeriod(date: String) { // This adds available date to booking_period_dates if not already exists
        if (!_booking_period_dates.contains(date)) {
            _booking_period_dates.add(date)
        }
    }
    /**
     * Removes a date from the campsite's booking period dates (US 02.04.04).
     * @param date Date string to remove.
     */
    fun removeBookingPeriodDate(date: String) { // Removes a date from booking period dates
        _booking_period_dates.remove(date)
    }
    /**
     * Associates a [Reservation] with this campsite if not already present.
     * @param reservation The [Reservation] to add.
     */
    fun addReservation(reservation: Reservation) { // Add a Reservation object to campsite reservation list
        if (!_reservations.any { it === reservation }) {
            _reservations.add(reservation)

        }
    }
    /**
     * Removes a [Reservation] from this campsite's reservation list.
     * @param reservation The [Reservation] to remove.
     */
    fun removeReservation(reservation: Reservation) { // Remove a reservation object from campsite reserv. list
        _reservations.removeIf { it === reservation }
    }
    /**
     * Returns the average rating of the campsite formatted as a string (US 01.01.04).
     * @return Average rating string.
     */
    fun getAvgRating(): String { // This will return the average rating of the campsite as a string
        return average_rating.toString()
    }
    /**
     * Adds a rating from an [Entrant] between 1.0 and 5.0 and recalculates [average_rating].
     * @param entrant The [Entrant] submitting the rating.
     * @param score Rating score in `1.0f..5.0f`.
     */
    fun addRating(entrant: Entrant, score: Float) {
        // PLEASE NOTE that this function code is just an idea and may be updated in the future!
        // This function will be responsible of adding a new (Entrant, Float) rating pair and update average_rating
        if (score in 1.0f..5.0f) {
            _ratings.add(Pair(entrant, score)) // This will pack entrant and their score into a single Pair

            var sum = 0.0f
            for ((reviewer, ratingScore) in _ratings) { // Split the pair into reviewer and ratingScore in ratings
                sum += ratingScore // Add the rating score to the sum
            }
            average_rating = sum / _ratings.size
        }
    }

    /**
     * In order to safely delete this campsite we need to remove every reservation that refers to it.
     * Under normal circumstances this should only be invoked by the firestoreDriver.
     */
    fun clearReservations() {
        _reservations.forEach { it.deleteReservation() }
    }
}