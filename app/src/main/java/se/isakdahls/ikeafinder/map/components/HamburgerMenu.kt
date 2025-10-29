package se.isakdahls.ikeafinder.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.Icons.Default
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import se.isakdahls.ikeafinder.R

/**
 * skapar en hamburgermeny-knapp
 */
@Composable
fun HamburgerMenu(onClick: () -> Unit, modifier: Modifier = Modifier) {

    val currentScheme = MaterialTheme.colorScheme

    IconButton(
        onClick = onClick,
        modifier = modifier
            .clip(CircleShape)
            .background(currentScheme.surface)
    ) {
        Icon(
            imageVector = Default.Menu,
            contentDescription = stringResource(R.string.menu_show_store_list_description),
            tint = currentScheme.onSurface
        )
    }
}
