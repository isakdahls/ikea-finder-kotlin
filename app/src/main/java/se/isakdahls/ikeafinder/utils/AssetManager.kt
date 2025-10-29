package se.isakdahls.ikeafinder.utils

import android.content.Context
import java.io.*

object AssetManager {
    
    private const val DATABASE_NAME = "ikea.db"

    /**
     * kopierar ikea.db från assets till internminne
     */
    fun copyDatabaseIfNeeded(context: Context): Boolean {
        return try {
            val dbFile = context.getDatabasePath(DATABASE_NAME)

            if (dbFile.exists()) {
                AppLogger.debug("ASSET", "Databas finns redan: ${dbFile.absolutePath}")
                return true
            }

            dbFile.parentFile?.mkdirs() //skapa mappar
            
            // Kopiera databas från assets
            copyAssetToFile(context, DATABASE_NAME, dbFile)
            
            AppLogger.debug("ASSET", "Databas kopierad till: ${dbFile.absolutePath}")
            true
            
        } catch (e: Exception) {
            AppLogger.error("ASSET", "Fel vid kopiering av databas", e)
            false
        }
    }

    /**
     * kopierar asset fil till destination
     */
    private fun copyAssetToFile(context: Context, assetName: String, destinationFile: File) {
        val assetManager = context.assets
        
        assetManager.open(assetName).use { inputStream ->
            FileOutputStream(destinationFile).use { outputStream ->
                val buffer = ByteArray(1024)
                var length: Int
                
                while (inputStream.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }
                
                outputStream.flush()
            }
        }
    }

}