package se.isakdahls.ikeafinder.map.components

import android.Manifest
import se.isakdahls.ikeafinder.R
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import se.isakdahls.ikeafinder.map.MapUiState
import se.isakdahls.ikeafinder.map.MapViewModel

/**
 * Extended FAB
 * Begär GPS location, hämtar direkt om tillstånd
 * */
@Composable
fun NearestStoreButton(
    uiState: MapUiState,
    viewModel: MapViewModel,
    onNavigateToDetail: (Int) -> Unit,
    modifier: Modifier = Modifier
) {

    val fineLauncher = permissionLauncher(
        onGranted = { viewModel.findNearestStore { onNavigateToDetail(it) } },
        onDenied = { viewModel.showPermissionDeniedError() }
    )

    ExtendedFloatingActionButton(

        onClick = {
            if (viewModel.hasLocationPermission()) { // 1. om permissions redan finns
                viewModel.findNearestStore { onNavigateToDetail(it) }
            } else {
                fineLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) // 2. dubbelskärm för fine / coarse
            }
        },

        modifier = modifier,

        icon = {
            if (uiState.isLoadingLocation) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                Icon(Icons.Default.LocationOn, contentDescription = stringResource(R.string.find_nearest_button))
            }
        },

        text = {
            Text(
                text = if (uiState.isLoadingLocation)
                    stringResource(R.string.searching_location)
                else
                    stringResource(R.string.find_nearest_button)
            )
        }
    )
}

/**
 * aktivitetsstartare för att begära behörigheter, mer lästbart ovnaför
 */
@Composable
private fun permissionLauncher(
    onGranted: () -> Unit,
    onDenied: () -> Unit = {}
) = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
) { isGranted ->
    if (isGranted) onGranted() else onDenied()
}
