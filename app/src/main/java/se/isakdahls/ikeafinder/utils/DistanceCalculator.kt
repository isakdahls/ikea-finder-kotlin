package se.isakdahls.ikeafinder.utils

import se.isakdahls.ikeafinder.data.models.Store
import kotlin.math.*

object DistanceCalculator {
    
    private const val EARTH_RADIUS_KM = 6371.0
    
    /**
     * avstånd mellan två koordinater med Haversine
     */
    fun calculateDistance(
        lat1: Double, 
        lon1: Double,
        lat2: Double, 
        lon2: Double
    ): Int {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
                
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = EARTH_RADIUS_KM * c

        return if (distance < 1.0) 0 else distance.roundToInt() // 0km för avstånd under 1km, annars avrunda
    }
    
    /**
     * Hittar närmaste butik från användarens pos
     */

    fun calculateNearestStore(
        userLat: Double, 
        userLon: Double, 
        stores: List<Store>

    ): Store? {
        return stores.minByOrNull { store ->
            calculateDistance(userLat, userLon, store.latitude, store.longitude)
        }
    }
    
    /**
     * avstånd från användarposition till butik
     */
    fun calculateDistanceToStore(
        userLat: Double,
        userLon: Double,
        store: Store

    ): Int {
        return calculateDistance(userLat, userLon, store.latitude, store.longitude)
    }

}