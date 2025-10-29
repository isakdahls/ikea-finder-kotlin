package se.isakdahls.ikeafinder.map.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * namn på närmaste butiken
 */
@Composable
fun NearestStoreHeader(name: String, distanceText: String, modifier: Modifier = Modifier) {
    Text(
        text = "$name: $distanceText",
        modifier = modifier,
        color = MaterialTheme.colorScheme.inverseSurface,
        maxLines = 1
    )
}
