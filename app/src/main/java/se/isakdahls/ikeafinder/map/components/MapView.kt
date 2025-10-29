package se.isakdahls.ikeafinder.map.components

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import se.isakdahls.ikeafinder.data.models.Store
import se.isakdahls.ikeafinder.utils.AppLogger
import se.isakdahls.ikeafinder.R

/**
 * karta med butiksmarkörer
 */
@Composable
fun MapView(
    modifier: Modifier = Modifier,
    stores: List<Store>,
    centerOnStore: Store? = null,
    nearestStoreId: Int? = null,
    onStoreMarkerClick: (Store) -> Unit = {}
) {
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle()

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { mapView.apply { initializeMap() } },
        update = { map ->
            map.overlays.clear()
            stores.forEach { store ->
                map.overlays.add(createStoreMarker(context, map, store, nearestStoreId, onStoreMarkerClick))
            }
            centerOnStore?.let { map.centerOnStore(it) }
            map.invalidate()
        }
    )
}

/**
 * initierar kartvyn med grundläggande inställningar
 */
private fun MapView.initializeMap() {
    setTileSource(TileSourceFactory.MAPNIK)
    setMultiTouchControls(true)
    setBuiltInZoomControls(false) // tar bort vita zoom knappar
    minZoomLevel = 6.0
    maxZoomLevel = 13.0
    controller.setZoom(6.0)
    controller.setCenter(GeoPoint(61.0, 18.0)) // zoomat på sverige
    AppLogger.map("MapView started with Sweden as center")
}

/**
 * skapar en markör för en butik på kartan
 */
private fun createStoreMarker(
    context: Context,
    mapView: MapView,
    store: Store,
    nearestStoreId: Int?,
    onStoreMarkerClick: (Store) -> Unit
): Marker {
    return Marker(mapView).apply {
        position = GeoPoint(store.latitude, store.longitude)
        title = store.name
        snippet = "${store.city} - ${store.address}"

        icon = context.getDrawable(
            if (store.id == nearestStoreId) R.drawable.marker_ikea_store_nearest
            else R.drawable.marker_ikea_store
        )

        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        setOnMarkerClickListener { _, _ ->
            AppLogger.map("Marker clicked: ${store.name} (ID: ${store.id})")
            if (store.id > 0) onStoreMarkerClick(store)
            else AppLogger.map("Invalid store ID: ${store.id}")
            true
        }
    }
}

/**
 * centrerar kartan på en specifik butik
 */
private fun MapView.centerOnStore(store: Store, zoom: Double = 12.0) {
    controller.animateTo(GeoPoint(store.latitude, store.longitude))
    controller.setZoom(zoom)
    AppLogger.map("Map centered on ${store.name}")
}


/**
 * "Bakgrundkartan" när man väljer en butik
 */
@Composable
fun StoreDetailMapView(
    store: Store,
    modifier: Modifier = Modifier
) {
    val mapView = rememberMapViewWithLifecycle()

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            mapView.apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                setBuiltInZoomControls(false) //döljer vita zoom knappar
                minZoomLevel = 6.0
                maxZoomLevel = 13.0

                // Centrera direkt på butiken
                val storeLocation = GeoPoint(store.latitude, store.longitude)
                controller.setCenter(storeLocation)
                controller.setZoom(14.0) // fixad zoom för detaljvy

                AppLogger.map("DetailMapView centrerad på ${store.name}")
            }
        },
        update = { mapView ->
            mapView.overlays.clear()

            val storeMarker = Marker(mapView)

            storeMarker.position = GeoPoint(store.latitude, store.longitude)
            storeMarker.title = store.name
            storeMarker.snippet = store.address
            storeMarker.icon = mapView.context.getDrawable(R.drawable.marker_ikea_store)
            storeMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            mapView.overlays.add(storeMarker)
            mapView.invalidate()
        }
    )
}

/**
 * skapar och hanterar livscykeln för MapView
 */
@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context)
    }

    val lifecycleObserver = remember(mapView) {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_DESTROY -> mapView.onDetach()
                else -> {}
            }
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return mapView
}