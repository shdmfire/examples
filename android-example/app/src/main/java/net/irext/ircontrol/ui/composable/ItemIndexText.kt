package net.irext.ircontrol.ui.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.irext.ircontrol.ui.theme.IRControlTheme

/**
 * Composable replacement for item_index.xml.
 *
 * Original XML: Vertical LinearLayout (match_parent x match_parent, padding=4dp)
 *   + TextView (match_parent x 32dp, weight=4, marginStart/End=10dp, center_vertical, bold, 20sp)
 *   + TextView (match_parent x 32dp, weight=4, marginStart/End=10dp, center_vertical, normal, 14sp)
 */
@Composable
fun ItemIndexText(
    nameText: String,
    mapText: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
    ) {
        Text(
            text = nameText,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 32.dp)
                .padding(horizontal = 10.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
        )
        Text(
            text = mapText,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 32.dp)
                .padding(horizontal = 10.dp),
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ItemIndexTextPreview() {
    IRControlTheme {
        ItemIndexText(
            nameText = "Index Name",
            mapText = "Index Map",
        )
    }
}
