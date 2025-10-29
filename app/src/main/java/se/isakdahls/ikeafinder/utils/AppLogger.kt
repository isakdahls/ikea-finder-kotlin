package se.isakdahls.ikeafinder.utils

import android.util.Log
import se.isakdahls.ikeafinder.BuildConfig

/**
 * loggklass för att underlätta utveckling
 */
object AppLogger {
    
    private val DEBUG_MODE = BuildConfig.DEBUG
    private const val LOG_PREFIX = "IKEA_"
    
    /**
     * Loggar i debug build
     */
    fun debug(tag: String, message: String) {
        if (DEBUG_MODE) {
            Log.d("$LOG_PREFIX$tag", message)
        }
    }

    /**
     * Loggar fel i debug build
     */
    fun error(tag: String, message: String, exception: Throwable? = null) {
        if (DEBUG_MODE) {
            if (exception != null) { //skickar till logcat
                Log.e("$LOG_PREFIX$tag", message, exception)
            } else {
                Log.e("$LOG_PREFIX$tag", message)
            }
        }
    }

    /**
     * Loggar mapfel
     */
    fun map(message: String) {
        debug("MAP", message)
    }

    /**
     * Loggar platsfel
     */
    fun location(message: String) {
        debug("LOCATION", message)
    }
}