package se.isakdahls.ikeafinder.detail.components

import se.isakdahls.ikeafinder.data.models.Store
import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import se.isakdahls.ikeafinder.map.components.StoreDetailMapView

/**
 * detaljerad information om en butik, inklusive en karta och en informationsruta
 */
@Composable
fun StoreDetailContent(
    store: Store,
    distance: Int?,
    onPhoneClick: (String) -> Unit,
    onWebsiteClick: (String) -> Unit,
    onGoogleMapsClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    Box(modifier = modifier.fillMaxSize()) {
        StoreDetailMapView(store = store)

        StoreInfoCard(
            store = store,
            distance = distance,
            onPhoneClick = onPhoneClick,
            onWebsiteClick = onWebsiteClick,
            onGoogleMapsClick = onGoogleMapsClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .systemBarsPadding()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp,
                    top = if (isPortrait) 72.dp else 16.dp
                )
        )

        BackButton(
            onClick = onNavigateBack,
            modifier = Modifier.align(Alignment.TopStart)
        )
    }
}