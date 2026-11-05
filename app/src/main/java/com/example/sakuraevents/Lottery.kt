/**
 * File: Lottery.kt
 * Purpose: Manages the pooling/lottery system for one or more campsites, including
 * registration periods, drawing winners from member campsites' waitlists,
 * and dispatching win/loss notifications to entrants.
 *
 * User Stories Worked On:
 * - US 01.03.01: As an entrant, I want to receive a notification when I win the campsite lottery.
 * - US 01.03.02: As an entrant, I want to receive a notification when I lose the campsite lottery.
 * - US 02.04.01: As a campsite owner, I want to list my campsite in a lottery for a certain period.
 * - US 02.04.02: As a campsite owner, I want be able to list multiple campsites in a lottery.
 * - US 02.04.03: As a campsite owner, I want to set a registration period.
 * - US 02.04.05: As a campsite owner, I want to set the number of lottery winners.
 * - US 02.05.01: As a campsite owner, I want the system to automatically draw winners after the registration period ends.
 *
 * Outstanding Issues:
 * hmm... not sure
 */

package com.example.sakuraevents

/**
 * Represents a lottery that draws a list of winners dynamically from the pooled
 * waiting lists of its member campsites based on the configured winner capacity.
 *
 * @property capacity Number of winners to draw in the lottery.
 * @property start_time Registration start date/time string.
 * @property end_time Registration end date/time string.
 */
class Lottery(
    val UID: String = "",
    private var capacity: Int = 0,
    private var start_time: String = "",
    private var end_time: String = ""
) {
    private var winners_list: MutableList<Entrant> = mutableListOf()
    private var _campsites_list: MutableList<Campsite> = mutableListOf()

    /** Read-only list of campsites included in this lottery. */
    val campsites_list: List<Campsite> get() = _campsites_list

    /** Read-only list of entrants selected as lottery winners. */
    val winners: List<Entrant> get() = winners_list

    /**
     * Adds a [Campsite] to this lottery event.
     *
     * @param new_campsite The campsite to include.
     */
    fun addCampsite(new_campsite: Campsite) {
        if (!_campsites_list.any { it === new_campsite }) {
            _campsites_list.add(new_campsite)
        }
    }
    /**
     * Removes a [Campsite] from this lottery event.
     *
     * @param campsite The campsite to remove.
     */
    fun removeCampsite(campsite: Campsite) {
        _campsites_list.removeIf { it === campsite }
    }
    /**
     * Sets the number of winners to draw.
     *
     * @param new_cap Number of winners.
     */
    fun receiveCapacity(new_cap: Int) {
        if (new_cap >= 0) {
            capacity = new_cap
        }
    }
    /**
     * Gets the configured winner capacity.
     *
     * @return The number of lottery winners to draw.
     */
    fun getCapacity(): Int = capacity
    /**
     * Sets the registration start time.
     *
     * @param new_start_time Start date/time string.
     */
    fun receiveStartTime(new_start_time: String) {
        start_time = new_start_time
    }
    /**
     * Gets the registration start time.
     *
     * @return Start date/time string.
     */
    fun getStartTime(): String = start_time
    /**
     * Sets the registration end time.
     *
     * @param new_end_time End date/time string.
     */
    fun receiveEndTime(new_end_time: String) {
        end_time = new_end_time
    }
    /**
     * Gets the registration end time.
     *
     * @return End date/time string.
     */
    fun getEndTime(): String = end_time
    /**
     * Generates a list of winners by pooling the waitlists of all member campsites.
     *
     * @return A list of Entrants that have won the lottery.
     */
    // The following function drawWinners() was generated / modified using Google Gemini 3.1 Pro
    // "Can you tell me the logic / boilerplate behind drawWinners() from List<Entrant> please?" 2026-Nov-05
    fun drawWinners(): List<Entrant> {
        winners_list.clear()
        // 1. Create a master pool by combining the waiting_list from ALL member campsites.
        // .distinct() ensures an entrant isn't drawn twice if they joined multiple member campsites.
        val pooledWaitlist = _campsites_list.flatMap { it.waiting_list }.distinct()
        // BASICALLY flatMap iterates through the array of member campsites, extracts the waiting_list
        // from each, and flattens them into one master list.
        // If no one is in the pool, or capacity is 0, return empty
        if (pooledWaitlist.isEmpty() || capacity <= 0) return winners_list
        // 2. Shuffle the pooled waitlist and pick winners up to the capacity limit
        val shuffled = pooledWaitlist.shuffled()
        val selectedCount = minOf(capacity, shuffled.size)
        winners_list.addAll(shuffled.take(selectedCount))
        // 3. Send out notifications to everyone in the pool

        val campsiteNames = _campsites_list.joinToString(", ") { it.getName() }.ifBlank { "Campsite Lottery" }

        for (entrant in pooledWaitlist) {
            val won = winners_list.contains(entrant)

            // Only create a notification if we actually have a campsite
            if (_campsites_list.isNotEmpty()) {
                // Create a dummy sender (CampsiteOwner) since Notification requires one.
                // Must satisfy the Entrant parameters (email, phone_number, role) it inherits.
                val dummySender = CampsiteOwner(
                    name = "System Lottery",
                    email = "lottery@system.com",
                    phone_number = "000-000-0000",
                    role = Role.ADMIN // enum is named 'Role' and has 'ADMIN'
                )

                val notification = Notification(
                    UID = "notif_${System.currentTimeMillis()}_${entrant.UID}",
                    title = if (won) "Lottery Winner!" else "Lottery Result",
                    message = if (won) {
                        "Congratulations! You won the lottery for $campsiteNames. Please select your dates to reserve."
                    } else {
                        "Unfortunately, you were not selected in the lottery for $campsiteNames."
                    },
                    _recipients = mutableListOf(entrant),
                    campsite = _campsites_list.first(), // Pass the actual first campsite object
                    sender = dummySender // Pass the dummy owner object
                )
                entrant.addNotification(notification)
            }
        }

        return winners_list.toList()
    }
    /**
     * Populates the provided mutable list with the drawn lottery winners.
     *
     * @param out_winners_list Mutable list to populate with winners.
     */
    fun getWinnerList(out_winners_list: MutableList<Entrant>) {
        if (winners_list.isEmpty()) {
            drawWinners()
        }
        out_winners_list.clear()
        out_winners_list.addAll(winners_list)
    }
}