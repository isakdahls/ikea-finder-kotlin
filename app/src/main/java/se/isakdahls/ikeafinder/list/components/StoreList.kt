package se.isakdahls.ikeafinder.list.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import se.isakdahls.ikeafinder.data.models.Store
import se.isakdahls.ikeafinder.utils.DistanceCalculator

/**
 * rullningsbar lista över butiker
 */
@Composable
fun StoreList(
    stores: List<Store>,
    userLocation: Pair<Double, Double>?,
    onStoreNavigate: (Int) -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
    ) {
        items(
            items = stores,
            key = { it.id }
        ) { store ->
            val distance = userLocation?.let { location ->
                DistanceCalculator.calculateDistanceToStore(
                    location.first, location.second, store
                )
            }

            StoreListItem(
                store = store,
                distance = distance,
                onClick = { onStoreNavigate(store.id) }
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) } // extra bottom spacing
    }
}
