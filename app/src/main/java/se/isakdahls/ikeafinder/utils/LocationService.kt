package se.isakdahls.ikeafinder.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull


class LocationService(private val context: Context) {

    /*
     konstruktor med attribut
     en rad i kotlin
     exempel på hur det skulle vara i java
     public class LocationService {
         private final Context context;

         public LocationService(Context context) {
            this.context = context;
         }
     }
    */

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)


    /**
     * Hämtar plats, om plats har ändrats, för coarse måste man rensa cache för Google Play Services
     */

    @SuppressLint("MissingPermission")
    suspend fun requestSingleLocationUpdate(
        timeoutMillis: Long = 20_000L
    ): LocationResult {

        AppLogger.location("Begär GPS-pos med timeout ${timeoutMillis}ms")

        if (!hasLocationPermission()) { //hämtar permission
            AppLogger.location("GPS-behörigheter saknas")
            return LocationResult.PermissionDenied
        }

        val request = createRequest()
        val cancellationToken = CancellationTokenSource()

        return try {

            fusedLocationClient.flushLocations().await() // vet ej om denna hjälper

            val location = withTimeoutOrNull(timeoutMillis) {
                fusedLocationClient.getCurrentLocation(request, cancellationToken.token).await()
            }

            if (location != null) {
                LocationResult.Success(location)
            } else {
                AppLogger.location("GPS-timeout efter ${timeoutMillis}ms")
                LocationResult.Timeout
            }

        } catch (e: SecurityException) {
            AppLogger.error("LOCATION", "Behörighetsfel vid platsförfrågan", e)
            LocationResult.PermissionDenied
        } catch (e: Exception) {
            AppLogger.error("LOCATION", "Oväntat fel vid platsförfrågan", e)
            LocationResult.Error(e)
        } finally {
            cancellationToken.cancel()
        }
    }

    /**
     * skapar en begäran om aktuell plats
     */
    private fun createRequest(): CurrentLocationRequest {
        return CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .setMaxUpdateAgeMillis(2_000L) // chache tid max 2 sek gammal
            .setGranularity(Granularity.GRANULARITY_COARSE)
            .build()
    }

    /**
     * kontrollerar om appen har fine eller coarse platstillstånd
     */
    fun hasLocationPermission(context: Context): Boolean {
        return hasCoarseLocationPermission(context) || hasFineCoarseLocationPermission(context)
    }

    /**
     * kontrollerar om appen har tillstånd för grov platsdata
     */
    private fun hasCoarseLocationPermission(context: Context): Boolean {
        AppLogger.location("COARSE location permission fetched")
        val coarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return coarseLocation == PackageManager.PERMISSION_GRANTED
    }

    /**
     * kontrollerar om appen har tillstånd för exakt platsdata
     */
    private fun hasFineCoarseLocationPermission(context: Context): Boolean {
        AppLogger.location("FINE location permission fetched")
        val fineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        return fineLocation == PackageManager.PERMISSION_GRANTED
    }

    //ghetto
    /**
     * kontrollerar om appen har tillstånd för platsdata
     */
    fun hasLocationPermission(): Boolean {
        return hasLocationPermission(context);
    }

    sealed class LocationResult {
        data class Success(val location: Location) : LocationResult()
        object PermissionDenied : LocationResult()
        object Timeout : LocationResult()
        data class Error(val exception: Exception) : LocationResult()
    }
}
