package se.isakdahls.ikeafinder.map.components

import androidx.compose.material3.Text
import se.isakdahls.ikeafinder.R
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.Snackbar
import androidx.compose.material3.TextButton
import se.isakdahls.ikeafinder.map.MapUiState
import se.isakdahls.ikeafinder.map.MapViewModel

/**
 * snackbar för felmeddelanden på kartskärmen
 */
@Composable
fun MapErrorSnackbar(
    uiState: MapUiState,
    viewModel: MapViewModel,
    showErrorSnackbar: Boolean,
    modifier: Modifier = Modifier
) {
    if (showErrorSnackbar && !uiState.errorMessage.isNullOrEmpty()) {
        Snackbar(
            modifier = modifier,
            action = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text(stringResource(R.string.ok))
                }
            }
        ) {
            Text(uiState.errorMessage)
        }
    }
}
