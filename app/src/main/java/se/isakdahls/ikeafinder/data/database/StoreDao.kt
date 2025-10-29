package se.isakdahls.ikeafinder.data.database

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object för IKEA butiker
 */
@Dao
interface StoreDao {
    
    /**
     * Hämtar alla IKEA butiker (Flow)
     */
    @Query("SELECT * FROM store ORDER BY name")
    fun getAllStores(): Flow<List<StoreEntity>>
    
    /**
     * Hämtar alla IKEA butiker som lista (inte Flow)
     */
    @Query("SELECT * FROM store ORDER BY name")
    suspend fun getAllStoresList(): List<StoreEntity>
    
    /**
     * Hämtar en specifik butik baserat på ID
     */
    @Query("SELECT * FROM store WHERE id = :storeId")
    suspend fun getStoreById(storeId: Int): StoreEntity?
}