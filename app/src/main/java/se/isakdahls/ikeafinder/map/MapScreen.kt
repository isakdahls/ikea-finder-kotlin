package se.isakdahls.ikeafinder.map

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import se.isakdahls.ikeafinder.map.components.*
import se.isakdahls.ikeafinder.R
import se.isakdahls.ikeafinder.data.models.Store

/**
 * kartskärmen med butiker och användarens position
 */
@Composable
fun MapScreen(
    onNavigateToList: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: MapViewModel = viewModel()

    val uiState by viewModel.uiState.collectAsState()
    val stores by viewModel.stores.collectAsState()
    var showErrorSnackbar by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.errorMessage) {
        showErrorSnackbar = !uiState.errorMessage.isNullOrEmpty()
    }

    Box(modifier = modifier.fillMaxSize()) {

        // Map with markers
        MapView(
            modifier = Modifier.fillMaxSize(),
            stores = stores,
            nearestStoreId = uiState.nearestStore?.id,
            onStoreMarkerClick = { handleStoreClick(it, viewModel, onNavigateToDetail, context) }
        )

        HamburgerMenu(
            onClick = onNavigateToList,
            modifier = Modifier
                .align(Alignment.TopStart)
                .systemBarsPadding()
                .padding(8.dp)
        )

        uiState.nearestStore?.let {
            val distanceText = uiState.distanceToNearest?.let { d -> context.getString(R.string.distance_km, d) } ?: ""
            NearestStoreHeader(
                name = it.name,
                distanceText = distanceText,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .systemBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        //extended fab som triggar gps pos
        NearestStoreButton(
            uiState = uiState,
            viewModel = viewModel,
            onNavigateToDetail = onNavigateToDetail,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(16.dp)
        )

        MapErrorSnackbar(
            uiState = uiState,
            viewModel = viewModel,
            showErrorSnackbar = showErrorSnackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp)
        )

    }
}

/**
 * hanterar klick på butiksmarkörer på kartan
 */
private fun handleStoreClick(
    store: Store,
    viewModel: MapViewModel,
    onNavigateToDetail: (Int) -> Unit,
    context: Context
) {
    if (store.id > 0) onNavigateToDetail(store.id)
    else viewModel.showError(context.getString(R.string.could_not_open_store_info))
}

