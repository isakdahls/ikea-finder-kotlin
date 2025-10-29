package se.isakdahls.ikeafinder.detail

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import se.isakdahls.ikeafinder.R
import se.isakdahls.ikeafinder.data.models.Store
import se.isakdahls.ikeafinder.detail.components.StoreDetailContent
import se.isakdahls.ikeafinder.detail.components.StoreDetailError
import se.isakdahls.ikeafinder.detail.components.StoreDetailLoading


/**
 * hämtar och visar information om butik när man klickar på marker eller från listan
 */
@Composable
fun StoreDetailScreen(
    storeId: Int,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: StoreDetailViewModel = viewModel(
        factory = StoreDetailViewModelFactory(context as androidx.activity.ComponentActivity, storeId)
    )
    val uiState by viewModel.uiState.collectAsState()
    val store by viewModel.store.collectAsState()
    var showErrorSnackbar by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.errorMessage) {
        showErrorSnackbar = uiState.errorMessage != null
    }

    StoreDetailScaffold( //ny privat hjälp funktion
        modifier = modifier,
        uiState = uiState,
        store = store,
        showErrorSnackbar = showErrorSnackbar,
        onDismissSnackbar = {
            showErrorSnackbar = false
            viewModel.clearError()
        },
        viewModel = viewModel,
        onNavigateBack = onNavigateBack
    )
}

/**
 * bygger upp skärmen för butiksdetaljer
 */
@Composable
private fun StoreDetailScaffold(
    modifier: Modifier,
    uiState: StoreDetailUiState,
    store: Store?,
    showErrorSnackbar: Boolean,
    onDismissSnackbar: () -> Unit,
    viewModel: StoreDetailViewModel,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            StoreDetailSnackbar(
                show = showErrorSnackbar,
                errorMessage = uiState.errorMessage,
                onDismiss = onDismissSnackbar
            )
        }
    ) { paddingValues ->
        StoreDetailContent(
            uiState = uiState,
            store = store,
            viewModel = viewModel,
            onNavigateBack = onNavigateBack,
            paddingValues = paddingValues
        )
    }
}

/**
 * innehållet för butiksdetaljer
 */
@Composable
private fun StoreDetailContent(
    uiState: StoreDetailUiState,
    store: Store?,
    viewModel: StoreDetailViewModel,
    onNavigateBack: () -> Unit,
    paddingValues: PaddingValues
) {
    //här läggs alla UI-element
    ScreenContent(
        uiState = uiState,
        store = store,
        viewModel = viewModel,
        onNavigateBack = onNavigateBack,
        paddingValues = paddingValues
    )
}



/**
 * denna funktion väljer vilket innehåll som ska visas på skärmen (laddar, visar butik eller fel)
 */
@Composable
private fun ScreenContent(
    uiState: StoreDetailUiState,
    store: Store?,
    viewModel: StoreDetailViewModel,
    onNavigateBack: () -> Unit,
    paddingValues: PaddingValues
) {
    val context = LocalContext.current
    val modifier = Modifier.padding(paddingValues)

    when {
        uiState.isLoading -> StoreDetailLoading(modifier)

        store != null -> StoreDetailContent(
            store = store,
            distance = uiState.distanceToStore,
            onPhoneClick = { context.launchUri(viewModel.getPhoneUri(), Intent.ACTION_DIAL) },
            onWebsiteClick = { context.launchUri(viewModel.getWebsiteUri()) },
            onGoogleMapsClick = { context.launchUri(viewModel.getGoogleMapsUri()) },
            onNavigateBack = onNavigateBack,
            modifier = modifier
        )

        else -> StoreDetailError(
            onNavigateBack = onNavigateBack,
            modifier = modifier
        )
    }
}

/**
 * Error Snackbar
 */
@Composable
private fun StoreDetailSnackbar(
    show: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit
) {
    if (show && errorMessage != null) {
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.ok))
                }
            }
        ) {
            Text(errorMessage)
        }
    }
}

/**
 * denna funktion startar en aktivitet för att öppna en URI (t.ex. webbsida, telefonnummer)
 */
private fun Context.launchUri(uriString: String, action: String = Intent.ACTION_VIEW) {
    if (uriString.isNotBlank()) {
        val intent = Intent(action, uriString.toUri())
        startActivity(intent)
    }
}


/**
 * StoreDetailViewModel Factory, storeId inparameter
 */
class StoreDetailViewModelFactory(
    private val activity: androidx.activity.ComponentActivity,
    private val storeId: Int
) : androidx.lifecycle.ViewModelProvider.Factory {

    /**
     * denna funktion skapar en ny instans av StoreDetailViewModel
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoreDetailViewModel::class.java)) {
            val savedStateHandle = androidx.lifecycle.SavedStateHandle()
            savedStateHandle["storeId"] = storeId

            val viewModel = StoreDetailViewModel(
                application = activity.application,
                savedStateHandle = savedStateHandle,
                storeId = storeId
            )

            return viewModel as T
        }
        throw IllegalArgumentException("okänd ViewModel")
    }
}