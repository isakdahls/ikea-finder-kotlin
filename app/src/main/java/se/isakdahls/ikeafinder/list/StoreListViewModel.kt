package se.isakdahls.ikeafinder.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.isakdahls.ikeafinder.data.database.AppDatabase
import se.isakdahls.ikeafinder.data.models.Store
import se.isakdahls.ikeafinder.data.repository.StoreRepository
import se.isakdahls.ikeafinder.utils.AppLogger

/**
 * ViewModel för enkel butikslista
 */

class StoreListViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    
    private val repository = StoreRepository(AppDatabase.getDatabase(application))
    
    // UI State
    private val _uiState = MutableStateFlow(StoreListUiState())
    val uiState: StateFlow<StoreListUiState> = _uiState.asStateFlow()
    
    // Butiker från databasen
    val stores: StateFlow<List<Store>> = repository.getAllStores()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    
    init {
        // Återställ state vid rotation

        restoreSavedState()
        
        // ladda användarposition för distance-beräkningar
        loadUserLocation()
    }
    
    
    /**
     * Laddar användarens sist kända position
     */
    private fun loadUserLocation() {
        viewModelScope.launch {
            val lastKnownLocation = repository.getLastKnownLocation()
            if (lastKnownLocation != null) {
                _uiState.value = _uiState.value.copy(
                    userLocation = Pair(lastKnownLocation.latitude, lastKnownLocation.longitude)
                )
                AppLogger.debug("LIST", "Hämtade användarpos för avståndsberäkning")
            } else {
                AppLogger.debug("LIST", "No användarpos found")
            }
        }
    }
    
    /**
     * Sparar scroll
     */

    fun saveScrollPosition(position: Int) {
        savedStateHandle[KEY_SCROLL_POSITION] = position
        AppLogger.debug("LIST", "Scroll sparad: $position")
    }
    
    /**
     * Hämtar scroll
     */

    fun getSavedScrollPosition(): Int {
        return savedStateHandle[KEY_SCROLL_POSITION] ?: 0
    }
    
    /**
     * Sparar state
     */
    private fun saveState() {
        val currentState = _uiState.value
        savedStateHandle[KEY_USER_LOCATION] = currentState.userLocation
    }
    
    /**
     * Återställer state
     */
    private fun restoreSavedState() {
        val userLocation: Pair<Double, Double>? = savedStateHandle[KEY_USER_LOCATION]
        
        if (userLocation != null) {

            _uiState.value = _uiState.value.copy(
                userLocation = userLocation
            )

            AppLogger.debug("LIST", "State återställd")
        }
    }
    
    /**
     * anropas när viewmodel inte längre används och rensas
     */
    override fun onCleared() {
        super.onCleared()
        saveState()
    }
    
    companion object {
        private const val KEY_USER_LOCATION = "user_location"
        private const val KEY_SCROLL_POSITION = "scroll_position"
    }
}

/**
 * UI State för butikslista
 */
data class StoreListUiState(
    val userLocation: Pair<Double, Double>? = null
)