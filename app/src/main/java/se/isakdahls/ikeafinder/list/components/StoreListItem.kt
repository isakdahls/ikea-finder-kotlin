package se.isakdahls.ikeafinder.list.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import se.isakdahls.ikeafinder.R
import se.isakdahls.ikeafinder.data.models.Store

@Composable
fun StoreListItem( //listelement
    modifier: Modifier = Modifier,
    store: Store,
    distance: Int? = null,
    onClick: () -> Unit
) {

    val currentScheme = MaterialTheme.colorScheme
    val currentTypography = MaterialTheme.typography

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = currentScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Butiksnamn
            Text(
                text = store.name,
                style = currentTypography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = currentScheme.onSurface
            )
            
            // Adress
            Text(
                text = store.address,
                style = currentTypography.bodyMedium,
                color = currentScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            // Visa avstånd
            if (distance != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = currentScheme.primary
                    )
                    
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = currentScheme.primaryContainer
                    ) {
                        Text(
                            text = if (distance == 0) {
                                stringResource(R.string.distance_nearby)
                            } else {
                                stringResource(R.string.distance_km, distance) //ta ej bort, avstånd visas som %
                            },
                            style = currentTypography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = currentScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}