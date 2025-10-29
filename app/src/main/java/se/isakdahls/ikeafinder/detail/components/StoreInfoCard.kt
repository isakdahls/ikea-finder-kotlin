package se.isakdahls.ikeafinder.detail.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import se.isakdahls.ikeafinder.R
import se.isakdahls.ikeafinder.data.models.Store

/**
 * Detalijvy om butik
 */
@Composable
fun StoreInfoCard(
    modifier: Modifier = Modifier,
    store: Store,
    distance: Int? = null,
    onPhoneClick: (String) -> Unit,
    onWebsiteClick: (String) -> Unit,
    onGoogleMapsClick: (String) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    Card(
        modifier = modifier.then(
            if (isLandscape) {
                Modifier.fillMaxWidth(0.6f)
            } else {
                Modifier.fillMaxWidth()
            }
        ),

        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),

        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) { //card slut

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .verticalScroll(scrollState) // la till scrollbart
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) { //column start butiksnamn och avstånd


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) { //rad slut
                Column(modifier = Modifier.weight(1f)) {

                    Text(
                        text = store.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(

                        text = store.city,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                // avståndsinformation
                distance?.let { dist ->
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small

                    ) {
                        Text(
                            text = if (dist == 0) {
                                stringResource(R.string.distance_nearby)
                            } else {
                                stringResource(R.string.distance_from_you, dist)
                            },

                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            
            //adress
            StoreInfoRow(
                icon = Icons.Default.LocationOn,
                label = stringResource(R.string.address_label),
                value = store.address,
                contentDescription = stringResource(R.string.address_description, store.address)
            )
            
            // öppettider
            StoreInfoRow(
                icon = Icons.Filled.Info,
                label = stringResource(R.string.opening_hours_label),
                value = store.openingHours,
                contentDescription = stringResource(R.string.opening_hours_description, store.openingHours)
            )
            
            // telefon
            if (store.phone.isNotBlank()) {

                StoreInfoRow(
                    icon = Icons.Default.Phone,
                    label = stringResource(R.string.phone_label),
                    value = store.phone,
                    isClickable = true,
                    onClick = { onPhoneClick(store.phone) },
                    contentDescription = stringResource(R.string.call_phone_description, store.phone)
                )
            }
            
            // webbplats
            if (store.website.isNotBlank()) {
                StoreInfoRow(
                    icon = Icons.Filled.Info,
                    label = stringResource(R.string.website_label),
                    value = store.website,
                    isClickable = true,
                    onClick = { onWebsiteClick(store.website) },
                    contentDescription = stringResource(R.string.visit_website_description, store.website)
                )
            }
            
            // google maps knapp
            Button(
                onClick = { onGoogleMapsClick("${store.latitude},${store.longitude}") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.open_in_maps),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        } // column end
    }
}

/**
 * Inforad
 */
@Composable
private fun StoreInfoRow(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    isClickable: Boolean = false,
    onClick: (() -> Unit)? = null,
    contentDescription: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isClickable && onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = /* if (isClickable) { */
                MaterialTheme.colorScheme.primary,
            /*} else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },*/
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(

                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isClickable) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}