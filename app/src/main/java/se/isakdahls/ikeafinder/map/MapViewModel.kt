package se.isakdahls.ikeafinder.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import se.isakdahls.ikeafinder.R
import se.isakdahls.ikeafinder.data.database.AppDatabase
import se.isakdahls.ikeafinder.data.models.Store
import se.isakdahls.ikeafinder.data.repository.StoreRepository
import se.isakdahls.ikeafinder.utils.AppLogger
import se.isakdahls.ikeafinder.utils.DistanceCalculator
import se.isakdahls.ikeafinder.utils.LocationService

/**
 * ViewModel för kartvy
 */
class MapViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val repository = StoreRepository(AppDatabase.getDatabase(application))
    private val locationService = LocationService(application)
    private val _uiState = MutableStateFlow(MapUiState())

    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    val stores: StateFlow<List<Store>> = repository.getAllStores()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        restoreSavedState()
        loadLastKnownLocation()
    }

    /**
     * Hanterar "Hitta närmaste IKEA"-knappen
     */
    fun findNearestStore(onNearestStoreFound: (Int) -> Unit) {
        AppLogger.map("Börjar söka efter närmaste IKEA butik")

        if (isLocationRequestInProgress()) return

        viewModelScope.launch {
            prepareForLocationRequest()

            when (val result = locationService.requestSingleLocationUpdate()) {
                is LocationService.LocationResult.Success -> handleLocationSuccess(result, onNearestStoreFound)
                is LocationService.LocationResult.PermissionDenied -> showPermissionDeniedError()
                is LocationService.LocationResult.Timeout,
                is LocationService.LocationResult.Error -> showError(
                    getApplication<Application>().getString(R.string.could_not_get_location_retry)
                )
            }
        }
    }

    /**
     * kontrollerar om en platsbegäran redan pågår
     */
    private fun isLocationRequestInProgress(): Boolean {
        if (_uiState.value.isLoadingLocation) {
            AppLogger.map("GPS-begäran pågår redan, ignorerar ny begäran")
            return true
        }
        return false
    }

    /**
     * förbereder UI-tillståndet för en platsbegäran
     */
    private fun prepareForLocationRequest() {
        _uiState.value = _uiState.value.copy(
            isLoadingLocation = true,
            errorMessage = null
        )
    }

    /**
     * hanterar en lyckad platsbegäran
     */
    private suspend fun handleLocationSuccess(
        result: LocationService.LocationResult.Success,
        onNearestStoreFound: (Int) -> Unit
    ) {
        val location = result.location
        AppLogger.map("GPS-position erhållen: ${location.latitude}, ${location.longitude}")

        repository.saveUserLocation(location.latitude, location.longitude)

        val nearestStoreData = withContext(Dispatchers.Default) {
            findNearestStoreData(location.latitude, location.longitude)
        }

        withContext(Dispatchers.Main) {
            updateUiWithNearestStore(nearestStoreData, location, onNearestStoreFound)
        }
    }

    /**
     * hittar den närmaste butiken baserat på latitud och longitud
     */
    private suspend fun findNearestStoreData(
        lat: Double,
        lon: Double
    ): Pair<Store?, Int?> {
        val storesList = repository.getAllStoresList()
        val nearestStore = DistanceCalculator.calculateNearestStore(lat, lon, storesList)
        val distance = nearestStore?.let {
            DistanceCalculator.calculateDistanceToStore(lat, lon, it)
        }
        return nearestStore to distance
    }

    /**
     * uppdaterar UI med information om den närmaste butiken
     */
    private fun updateUiWithNearestStore(
        nearestStoreData: Pair<Store?, Int?>,
        location: android.location.Location,
        onNearestStoreFound: (Int) -> Unit
    ) {
        val (nearestStore, distance) = nearestStoreData

        if (nearestStore == null) {
            AppLogger.map("Ingen butik hittades")
            showError(getApplication<Application>().getString(R.string.no_stores_found_search))
            return
        }

        val previousNearestId = _uiState.value.nearestStore?.id
        val shouldNavigate = previousNearestId != nearestStore.id

        _uiState.value = _uiState.value.copy(
            isLoadingLocation = false,
            userLocation = Pair(location.latitude, location.longitude),
            nearestStore = nearestStore,
            distanceToNearest = distance
        )

        saveState()
        AppLogger.map("State sparad, GPS hämtad")

        if (shouldNavigate) {
            AppLogger.map("Navigerar till ny närmaste butik: ${nearestStore.name}")
            onNearestStoreFound(nearestStore.id)
        } else {
            AppLogger.map("Samma närmaste butik hittad, hoppar över navigation")
        }
    }

    /**
     * laddar den senast kända platsen för användaren
     */
    private fun loadLastKnownLocation() {
        viewModelScope.launch {
            val lastKnownLocation = repository.getLastKnownLocation()
            if (lastKnownLocation != null) {
                _uiState.value = _uiState.value.copy(
                    userLocation = Pair(lastKnownLocation.latitude, lastKnownLocation.longitude)
                )

                if (_uiState.value.nearestStore == null) {
                    val lat = lastKnownLocation.latitude
                    val lon = lastKnownLocation.longitude

                    withContext(Dispatchers.Default) {
                        val storesList = repository.getAllStoresList()
                        val nearestStore = DistanceCalculator.calculateNearestStore(lat, lon, storesList)

                        if (nearestStore != null) {
                            val distance = DistanceCalculator.calculateDistanceToStore(lat, lon, nearestStore)
                            withContext(Dispatchers.Main) {
                                _uiState.value = _uiState.value.copy(
                                    nearestStore = nearestStore,
                                    distanceToNearest = distance
                                )
                                saveState()
                                AppLogger.map("Beräknar närmaste butik från sparad pos: ${nearestStore.name}")
                            }
                        }
                    }
                }

                AppLogger.map("Laddar senaste pos vid start: ${lastKnownLocation.latitude}, ${lastKnownLocation.longitude}")
            }
        }
    }

    /**
     * rensar eventuella felmeddelanden
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * kontrollerar om appen har behörighet att använda platsdata
     */
    fun hasLocationPermission(): Boolean = locationService.hasLocationPermission()

    /**
     * visar ett felmeddelande om platstillstånd nekas
     */
    fun showPermissionDeniedError() {
        showError(
            getApplication<Application>().getString(R.string.location_permission_required_nearest)
        )
    }

    /**
     * visar ett generellt felmeddelande
     */
    fun showError(message: String) {
        _uiState.value = _uiState.value.copy(
            isLoadingLocation = false,
            errorMessage = message
        )
        AppLogger.map("Fel: $message")
    }

    /**
     * sparar det aktuella UI-tillståndet
     */
    private fun saveState() {
        val currentState = _uiState.value
        savedStateHandle[KEY_USER_LOCATION] = currentState.userLocation
        savedStateHandle[KEY_NEAREST_STORE_ID] = currentState.nearestStore?.id
        savedStateHandle[KEY_DISTANCE] = currentState.distanceToNearest
    }

    /**
     * återställer det sparade UI-tillståndet
     */
    private fun restoreSavedState() {
        val userLocation: Pair<Double, Double>? = savedStateHandle[KEY_USER_LOCATION]
        val nearestStoreId: Int? = savedStateHandle[KEY_NEAREST_STORE_ID]
        val distance: Int? = savedStateHandle[KEY_DISTANCE]

        if (userLocation != null || distance != null) {
            _uiState.value = _uiState.value.copy(
                userLocation = userLocation,
                distanceToNearest = distance
            )
            AppLogger.map("Återställd grundläggande state från SavedStateHandle")
        }

        if (nearestStoreId != null) {
            viewModelScope.launch {
                val nearestStore = repository.getStoreById(nearestStoreId)
                if (nearestStore != null) {
                    _uiState.value = _uiState.value.copy(nearestStore = nearestStore)
                    AppLogger.map("Närmaste butik återställd: ${nearestStore.name}")
                }
            }
        }
    }

    companion object {
        private const val KEY_USER_LOCATION = "user_location"
        private const val KEY_NEAREST_STORE_ID = "nearest_store_id"
        private const val KEY_DISTANCE = "distance_to_nearest"
    }
}

/**
 * UI State för kartvy
 */
data class MapUiState(
    val isLoadingLocation: Boolean = false,
    val userLocation: Pair<Double, Double>? = null,
    val nearestStore: Store? = null,
    val distanceToNearest: Int? = null,
    val errorMessage: String? = null
)
