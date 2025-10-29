package se.isakdahls.ikeafinder.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import se.isakdahls.ikeafinder.data.database.AppDatabase
import se.isakdahls.ikeafinder.data.database.UserLocationEntity
import se.isakdahls.ikeafinder.data.models.Store
import se.isakdahls.ikeafinder.data.models.toStore
import se.isakdahls.ikeafinder.data.models.toStores

class StoreRepository(
    database: AppDatabase
) {
    
    private val storeDao = database.storeDao()
    private val userLocationDao = database.userLocationDao()
    
    /**
     * Hämtar butiker som Flow
     */
    fun getAllStores(): Flow<List<Store>> {
        return storeDao.getAllStores().map { entities ->
            entities.toStores()
        }
    }
    
    /**
     * Hämtar butiker som lista för avståndsberäkningar
     */
    suspend fun getAllStoresList(): List<Store> {
        return storeDao.getAllStoresList().toStores()
    }
    
    /**
     * Hämtar butik med ID
     */
    suspend fun getStoreById(storeId: Int): Store? {
        return storeDao.getStoreById(storeId)?.toStore()
    }
    
    /**
     * Sparar senaste GPS pos
     */
    suspend fun saveUserLocation(latitude: Double, longitude: Double) {
        val userLocation = UserLocationEntity(
            latitude = latitude,
            longitude = longitude,
            timestamp = System.currentTimeMillis()
        )
        userLocationDao.saveUserLocation(userLocation)
    }
    
    /**
     * Hämtar senaste GPS pos
     */
    suspend fun getLastKnownLocation(): UserLocationEntity? {
        return userLocationDao.getLastKnownLocation()
    }

}