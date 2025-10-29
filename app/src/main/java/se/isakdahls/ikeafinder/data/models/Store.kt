package se.isakdahls.ikeafinder.data.models

import se.isakdahls.ikeafinder.data.database.StoreEntity

/**
 * Domain model för butik
 */
data class Store(
    val id: Int,
    val name: String,
    val address: String,
    val phone: String,
    val website: String,
    val openingHours: String,
    val latitude: Double,
    val longitude: Double,
    val city: String
)

/**
 * StoreEntity till Store domain model
 */
fun StoreEntity.toStore(): Store {
    return Store(
        id = this.id ?: 0,
        name = this.name,
        address = this.address,
        phone = this.phone,
        website = this.website,
        openingHours = this.openingHours,
        latitude = this.latitude,
        longitude = this.longitude,
        city = this.city
    )
}

/**
 * lista av StoreEntity till lista av Store
 */
fun List<StoreEntity>.toStores(): List<Store> {
    return this.map { it.toStore() }
}