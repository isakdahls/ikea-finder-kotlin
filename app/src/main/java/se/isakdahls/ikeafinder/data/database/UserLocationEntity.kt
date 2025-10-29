package se.isakdahls.ikeafinder.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity last known GPS-pos
 */
@Entity(tableName = "user_location")
data class UserLocationEntity(
    @PrimaryKey
    val id: Int = 1,
    
    @ColumnInfo(name = "latitude")
    val latitude: Double,
    
    @ColumnInfo(name = "longitude") 
    val longitude: Double,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long
)