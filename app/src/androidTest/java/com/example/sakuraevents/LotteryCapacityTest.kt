/**
 * Purpose: UI tests for the [LotteryCapacityField] component to verify valid and invalid input handling.
 * 
 * Target Class: LotteryComponents.kt (LotteryCapacityField)
 * 
 * User Stories Tested:
 * - US 02.04.05: As a campsite owner, I want to set the number of lottery winners.
 * 
 * Outstanding Issues:
 * - None.
 */

package com.example.sakuraevents

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.callbackFlow
import org.junit.Rule
import org.junit.Test

class LotteryCapacityTest{

    @get:Rule
    val capacityTestRule = createComposeRule()

    @Test
    fun lotteryCapacity_validInput_updateTest(){
        var callback = false
        val lottery = Lottery()


        capacityTestRule.setContent {
            LotteryCapacityField(
                lottery = lottery,
                onLotteryChanged = { callback = true }
            )
        }

        //clear old value and input a valid capacity
        capacityTestRule.onNodeWithTag("winner_count_input").performTextClearance()
        capacityTestRule.onNodeWithTag("winner_count_input").performTextInput("100")

        //clik the confirm button
        capacityTestRule.onNodeWithTag("lottery_winner_count_saved").performClick()

        //checkt the call back and the capacity was saved or not
        assertTrue(callback)
        assertEquals(100, lottery.getCapacity())

        //check no error message printed
        capacityTestRule.onNodeWithTag("settting_winner_count_error").assertDoesNotExist()
    }



    @Test
    fun lotteryCapacity_showErrorMessage(){
        var callback = false
        val lottery = Lottery()

        capacityTestRule.setContent {
            LotteryCapacityField(
                lottery = lottery,
                onLotteryChanged = { callback = true }
            )
        }

        //clear old value and input a invalid capacity
        capacityTestRule.onNodeWithTag("winner_count_input").performTextClearance()
        capacityTestRule.onNodeWithTag("winner_count_input").performTextInput("abcd")

        //click confirm button
        capacityTestRule.onNodeWithTag("lottery_winner_count_saved").performClick()

        //check it does not call back
        assertFalse(callback)

        //check it prints the error message
        capacityTestRule.onNodeWithTag("setting_winner_count_error").assertIsDisplayed()
    }

}
