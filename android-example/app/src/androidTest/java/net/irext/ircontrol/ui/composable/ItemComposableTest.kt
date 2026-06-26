package net.irext.ircontrol.ui.composable

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class ItemComposableTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun itemSingleText_displaysCorrectText() {
        composeTestRule.setContent {
            ItemSingleText(text = "Test Brand")
        }
        composeTestRule.onNodeWithText("Test Brand").assertExists()
    }

    @Test
    fun itemIndexText_displaysBothTexts() {
        composeTestRule.setContent {
            ItemIndexText(nameText = "Index 1", mapText = "Map Data")
        }
        composeTestRule.onNodeWithText("Index 1").assertExists()
        composeTestRule.onNodeWithText("Map Data").assertExists()
    }
}
