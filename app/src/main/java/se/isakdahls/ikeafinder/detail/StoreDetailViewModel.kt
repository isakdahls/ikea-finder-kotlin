package se.isakdahls.ikeafinder.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import se.isakdahls.ikeafinder.R
import se.isakdahls.ikeafinder.data.database.AppDatabase
import se.isakdahls.ikeafinder.data.models.Store
import se.isakdahls.ikeafinder.data.repository.StoreRepository
import se.isakdahls.ikeafinder.utils.AppLogger
import se.isakdahls.ikeafinder.utils.DistanceCalculator

/**
 * ViewModel för butikdetaljvy
 */
class StoreDetailViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val storeId: Int
) : AndroidViewModel(application) {
    
    private val repository = StoreRepository(AppDatabase.getDatabase(application))

    private val _uiState = MutableStateFlow(StoreDetailUiState())
    val uiState: StateFlow<StoreDetailUiState> = _uiState.asStateFlow()

    private val _store = MutableStateFlow<Store?>(null)
    val store: StateFlow<Store?> = _store.asStateFlow()
    

     //körs när ViewModel skapas

    init {
        AppLogger.debug("DETAIL", "Startar detaljvy för butik ID: $storeId")
        

        restoreSavedState()
        loadStoreDetails()
        loadUserLocation()
    }
    
    /**
     * Hämtar information om butik
     */
    private fun loadStoreDetails() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val storeData = repository.getStoreById(storeId)

                if (storeData != null) {
                    _store.value = storeData
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = null
                    )

                    AppLogger.debug("DETAIL", "Butikdata laddad: ${storeData.name}")
                    
                    //beräkna avstånd om användarposition finns
                    calculateDistanceIfPossible()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = getApplication<Application>().getString(R.string.store_with_id_not_found, storeId)
                    )

                    AppLogger.error("DETAIL", "Butik med ID $storeId hittades inte", null)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = getApplication<Application>().getString(R.string.could_not_load_store_info)
                )

                AppLogger.error("DETAIL", "Fel vid laddning av butik $storeId", e)
            }
        }
    }
    
    /**
     * Hämta användarens sist kända position
     */
    private fun loadUserLocation() {
        viewModelScope.launch {

            val lastKnownLocation = repository.getLastKnownLocation()

            if (lastKnownLocation != null) {

                _uiState.value = _uiState.value.copy(
                    userLocation = Pair(lastKnownLocation.latitude, lastKnownLocation.longitude)
                )

                AppLogger.debug("DETAIL", "Användarpos hämtad")

                calculateDistanceIfPossible() //beräkna avstånd
            } else {
                AppLogger.debug("DETAIL", "Ingen användarpos tillgänglig")
            }
        }
    }
    
    
    /**
     * beräknar avstånd till butiken om både butik och användarposition finns
     */
    private fun calculateDistanceIfPossible() {

        val currentStore = _store.value
        val currentUserLocation = _uiState.value.userLocation
        
        if (currentStore != null && currentUserLocation != null) {
            val distance = DistanceCalculator.calculateDistanceToStore(
                currentUserLocation.first,
                currentUserLocation.second,
                currentStore
            )
            
            _uiState.value = _uiState.value.copy(distanceToStore = distance)
            AppLogger.debug("DETAIL", "Avstånd beräknat: $distance km till ${currentStore.name}")
        }
    }
    
    /**
     * skapar Google Maps URI för navigation
     */
    fun getGoogleMapsUri(): String {
        val currentStore = _store.value

        return if (currentStore != null) {

            "geo:${currentStore.latitude},${currentStore.longitude}?q=${currentStore.name}"
        } else {
            ""
        }
    }
    
    /**
     * Genererar telefon resource identifier
     */
    fun getPhoneUri(): String {
        val currentStore = _store.value
        return if (currentStore != null && currentStore.phone.isNotBlank()) {

            "tel:${currentStore.phone}"
        } else {
            ""
        }
    }
    
    /**
     * Genererar webbläsare URI för webbplats
     */
    fun getWebsiteUri(): String {
        val currentStore = _store.value
        return if (currentStore != null && currentStore.website.isNotBlank()) {

            if (currentStore.website.startsWith("http")) {
                currentStore.website
            } else {
                "https://${currentStore.website}"
            }
        } else {
            ""
        }
    }
    
    /**
     * Tar bort felmeddelanden
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * Sparar viktig state för process death/rotation
     */
    private fun saveState() {
        val currentState = _uiState.value
        savedStateHandle[KEY_USER_LOCATION] = currentState.userLocation
        savedStateHandle[KEY_DISTANCE] = currentState.distanceToStore
        //sparar endast storeId, undvik serialiseringsfel
        savedStateHandle[KEY_STORE_ID] = _store.value?.id
    }
    
    /**
     * återställer state efter process death/rotation
     */
    private fun restoreSavedState() {

        val userLocation: Pair<Double, Double>? = savedStateHandle[KEY_USER_LOCATION]
        val distance: Int? = savedStateHandle[KEY_DISTANCE]
        val savedStoreId: Int? = savedStateHandle[KEY_STORE_ID]
        
        if (userLocation != null || distance != null) {

            _uiState.value = _uiState.value.copy(
                userLocation = userLocation,
                distanceToStore = distance
            )
            AppLogger.debug("DETAIL", "State återställd från SavedStateHandle")
        }
        
        // sparat store-id men inte laddat butiken än, ladda den
        if (savedStoreId != null && savedStoreId == storeId && _store.value == null) {
            AppLogger.debug("DETAIL", "Store-data kommer att laddas från databas med ID: $savedStoreId")
        }
    }
    
    /**
     * anropas när ViewModel inte längre används och rensas
     */
    override fun onCleared() {
        super.onCleared()
        saveState()
    }
    
    companion object {
        private const val KEY_USER_LOCATION = "user_location"
        private const val KEY_DISTANCE = "distance_to_store"
        private const val KEY_STORE_ID = "store_id"
    }
}

/**
 * UI State för butikdetaljvy
 */

data class StoreDetailUiState(
    val isLoading: Boolean = false,
    val userLocation: Pair<Double, Double>? = null,
    val distanceToStore: Int? = null,
    val errorMessage: String? = null
)