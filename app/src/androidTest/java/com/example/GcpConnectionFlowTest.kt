package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import org.junit.Rule
import org.junit.Test

/** Exercises the real GCP connection setup without authorizing or making cloud API requests. */
class GcpConnectionFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun emptyStateShowsNoInventedInventoryAndOffersLiveGcpConnection() {
        composeRule.onNodeWithText("NO CLOUD DATA YET").assertIsDisplayed()
        composeRule.onNodeWithText("Connect your Google Cloud project", substring = true).assertIsDisplayed()
        composeRule.onNodeWithTag("live_gcp_scan_button").performScrollTo().performClick()
        composeRule.onNodeWithText("Connect Google Cloud").assertIsDisplayed()
        composeRule.onNodeWithText("Billing data is not requested.", substring = true).assertIsDisplayed()
    }

    @Test
    fun connectionDialogExplainsReadOnlyScopeAndValidatesProjectId() {
        composeRule.onNodeWithTag("live_gcp_scan_button").performScrollTo().performClick()
        composeRule.onNodeWithText("Connect Google Cloud").assertIsDisplayed()
        composeRule.onNodeWithText("No resources are modified.", substring = true).assertIsDisplayed()

        val authorizeButton = composeRule.onNodeWithTag("start_custom_project_scan_button")
        composeRule.onNodeWithTag("project_id_input_field").performTextReplacement("bad id")
        authorizeButton.assertIsNotEnabled()

        composeRule.onNodeWithTag("project_id_input_field").performTextReplacement("guardian-project-123")
        authorizeButton.assertIsEnabled()
        composeRule.onNodeWithText("Cancel").performClick()
    }
}
