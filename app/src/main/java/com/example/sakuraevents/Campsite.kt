package com.example.sakuraevents

class Campsite(
    val uid: String = "",
    private var name: String = "",
    private var location: String = "",
    private var description: String = "",
    private var max_occupancy: Int = 1,
    private var qr_code: String = "",
    private var price: Float = 0.0f,
    private var tags: MutableList<String> = mutableListOf(),
    private var booking_period_dates: MutableList<String> = mutableListOf(),
    private var reservations: MutableList<Reservation> = mutableListOf(),
    private var ratings: MutableList<Pair<Entrant, Float>> = mutableListOf<>(),
    private var average_rating: Float = 0.0f
) {
    fun setName(name: String){ // Set the name of the campsite
        this.name = name // Keyword "this" refers to the specific object instance in the code
        // Basically "this" keyword points to the current instance of that class
    }
    fun getName(): String { // Returns campsite name
        return this.name
    }
    fun setLocation(location: String) { // Set the campsite location
        this.location = location
    }
    fun getLocation(): String { // Return the campsite location
        return this.location
    }
    fun setDescription(description: String) { // Set the campsite description
        this.description = description
    }
    fun getDescription(): String {
        return this.description // Return the description of the campsite
    }
    fun setMaxOccupancy(max_occupancy: Int) { // Set the max occupancy for campsite
        if (max_occupancy > 0) { // Max occupancy should be greater than 0
            this.max_occupancy = max_occupancy
        }
    }
    fun getMaxOccupancy(): Int { // Return the max occupancy
        return this.max_occupancy
    }
    fun setPrice(price: Float) { // Set the price for the campsite entry
        if (price >= 0.0f) { // Price can be zero if the campsite owner wishes free entry lol
            this.price = price
        }
    }
    fun getPrice(): Float { // Return the price
        return this.price
    }
    fun setQrCode(qr_code: String) {
        this.qr_code = qr_code
    }
    fun getQrCode(): String {
        return this.qr_code
    }
    fun addTag(tag: String) { // Add a tag to the campsite's tags if not already exists
        if (!tags.contains(tag)) {
            tags.add(tag)
        }
    }
    fun getTags(): String { // This will return the campsite's tags as a comma-separated string
        return tags.joinToString(", ")
    }
    fun removeTag(tag: String) {
        tags.remove(tag)
    }
    fun addBookingPeriod(date: String) { // This adds available date to booking_period_dates if not already exists
        if (!booking_period_dates.contains(date)) {
            booking_period_dates.add(date)
        }
    }
    fun removeBookingPeriodDate(date: String) { // Removes a date from booking period dates
        booking_period_dates.remove(date)
    }
    fun addReservation(reservation: Reservation) { // Add a Reservation object to campsite reservation list
        if (!reservations.contains(reservation)) {
            reservations.add(reservation)
        }
    }
    fun removeReservation(reservation: Reservation) { // Remove a reservation object from campsite reserv. list
        reservations.removeIf { it === reservation }
    }
    fun getAvgRating(): String { // This will return the average rating of the campsite as a string
        return average_rating.toString()
    }
    fun addRating(entrant: Entrant, score: Float) {
        // PLEASE NOTE that this function code is just an idea and may be updated in the future!
        // This function will be responsible of adding a new (Entrant, Float) rating pair and update average_rating
        if (score in 1.0f..5.0f) {
            ratings.add(Pair(entrant, score)) // This will pack entrant and their score into a single Pair

            var sum = 0.0f
            for ((reviewer, ratingScore) in ratings) { // Split the pair into reviewer and ratingScore in ratings
                sum += ratingScore // Add the rating score to the sum
            }
            average_rating = sum / ratings.size
        }
    }
}