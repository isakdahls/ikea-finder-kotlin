package se.isakdahls.ikeafinder.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity för IKEA butiksinformation
 */
@Entity(tableName = "store")
data class StoreEntity(
    @PrimaryKey
    val id: Int? = null,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "address") 
    val address: String,
    
    @ColumnInfo(name = "phone")
    val phone: String,
    
    @ColumnInfo(name = "website")
    val website: String,
    
    @ColumnInfo(name = "openingHours")
    val openingHours: String,
    
    @ColumnInfo(name = "latitude")
    val latitude: Double,
    
    @ColumnInfo(name = "longitude")
    val longitude: Double,
    
    @ColumnInfo(name = "city")
    val city: String
)