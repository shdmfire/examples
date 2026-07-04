package net.irext.ircontrol.compose.ui.composable

import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import net.irext.ircontrol.compose.ui.theme.IRControlTheme


/**
 * Filename:       ComposeItemViews.kt
 * Created:        Date: 2026-07-14
 *
 * Description:    Provides the ComposeItemViews source for the IRControl Android Compose sample.
 *
 * Revision log:
 * 2026-07-14: created by shdmfire and strawmanbobi
 */
/**
 * Bridge helper for Java adapters to use Compose-based item views.
 * Provides factory and bind methods callable from Java via @JvmStatic.
 */
object ComposeItemViews {

    @JvmStatic
    fun createSingleTextItem(parent: ViewGroup): ComposeView {
        return ComposeView(parent.context).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
            )
        }
    }

    @JvmStatic
    fun bindSingleText(composeView: ComposeView, text: String) {
        composeView.setContent {
            IRControlTheme {
                ItemSingleText(text = text)
            }
        }
    }

    @JvmStatic
    fun createIndexItem(parent: ViewGroup): ComposeView {
        return ComposeView(parent.context).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
            )
        }
    }

    @JvmStatic
    fun bindIndexText(composeView: ComposeView, nameText: String, mapText: String) {
        composeView.setContent {
            IRControlTheme {
                ItemIndexText(nameText = nameText, mapText = mapText)
            }
        }
    }
}
