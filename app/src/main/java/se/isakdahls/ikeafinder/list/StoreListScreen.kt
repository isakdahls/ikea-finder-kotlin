package se.isakdahls.ikeafinder.list

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import se.isakdahls.ikeafinder.data.models.Store
import se.isakdahls.ikeafinder.list.components.StoreList
import se.isakdahls.ikeafinder.list.components.StoreListLoading
import se.isakdahls.ikeafinder.list.components.StoreListTopBar


/**
 * lista över butiker
 */
@Composable
fun StoreListScreen(
    onNavigateToDetail: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = rememberStoreListViewModel() // ny privat
    val uiState by viewModel.uiState.collectAsState()
    val stores by viewModel.stores.collectAsState()
    val listState = rememberStoreListScrollState(viewModel) // ny privat

    Scaffold(topBar = { StoreListTopBar(onNavigateBack) }) { paddingValues ->
        StoreListContent(
            stores = stores,
            userLocation = uiState.userLocation,
            listState = listState,
            onStoreNavigate = onNavigateToDetail,
            modifier = modifier.padding(paddingValues)
        ) // ny privat
    }
}

/**
 * denna funktion skapar och kommer ihåg StoreListViewModel
 */
@Composable
private fun rememberStoreListViewModel(): StoreListViewModel {
    val context = LocalContext.current
    val viewModelStoreOwner = LocalViewModelStoreOwner.current!!

    return viewModel(
        viewModelStoreOwner = viewModelStoreOwner,
        factory = SavedStateViewModelFactory(
            (context as ComponentActivity).application, context
        )
    )
}

/**
 * denna funktion kommer ihåg rullningsläget för butikslistan
 */
@Composable
private fun rememberStoreListScrollState(
    viewModel: StoreListViewModel
): LazyListState {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = viewModel.getSavedScrollPosition())

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            viewModel.saveScrollPosition(listState.firstVisibleItemIndex)
        }
    }

    return listState
}

/**
 * denna funktion visar antingen en laddningsindikator eller butikslistan
 */
@Composable
private fun StoreListContent(
    stores: List<Store>,
    userLocation: Pair<Double, Double>?,
    listState: LazyListState,
    onStoreNavigate: (Int) -> Unit,
    modifier: Modifier
) {
    if (stores.isEmpty()) {
        StoreListLoading(modifier = modifier)
    } else {
        StoreList(
            stores = stores,
            userLocation = userLocation,
            onStoreNavigate = onStoreNavigate,
            listState = listState,
            modifier = modifier
        )
    }
}






