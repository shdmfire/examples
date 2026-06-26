package net.irext.ircontrol.ui.composable

import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import net.irext.ircontrol.ui.theme.IRControlTheme

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
