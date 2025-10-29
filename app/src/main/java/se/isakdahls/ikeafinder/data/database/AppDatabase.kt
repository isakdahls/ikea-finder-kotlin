package se.isakdahls.ikeafinder.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [StoreEntity::class, UserLocationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * DAO för butiksinfo
     */
    abstract fun storeDao(): StoreDao
    
    /**
     * DAO för användarpos
     */
    abstract fun userLocationDao(): UserLocationDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val DATABASE_NAME = "ikea_finder.db"
        private const val ASSET_DATABASE_NAME = "ikea.db"
        
        /**
         * Skapar/returnerar databas
         */

        fun getDatabase(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .createFromAsset(ASSET_DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .enableMultiInstanceInvalidation()
                .build()
                
                INSTANCE = instance
                instance
            }
        }

    }
}