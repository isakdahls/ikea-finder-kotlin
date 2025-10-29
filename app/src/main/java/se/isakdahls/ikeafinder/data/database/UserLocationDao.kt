package se.isakdahls.ikeafinder.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserLocationDao {
    
    /**
     * Sparar eller uppdaterar användarens pos
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserLocation(location: UserLocationEntity)
    
    /**
     * Hämtar användarens senaste position
     */
    @Query("SELECT * FROM user_location WHERE id = 1")
    suspend fun getLastKnownLocation(): UserLocationEntity?

}