package com.example.drivesafer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.drivesafer.model.DrivingTrip
import com.example.drivesafer.model.RoutePoint
import com.example.drivesafer.model.ViolationDetail
import com.example.drivesafer.util.DateConverter

/**
 * Database utama aplikasi yang mengelola entity dan DAO
 */
@Database(entities = [
    DrivingTrip::class,
    RoutePoint ::class,
    ViolationDetail :: class],
    version = 3, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun drivingTripDao(): DrivingTripDao
    abstract fun routePointDao(): RoutePointDao
    abstract fun violationDetailDao(): ViolationDetailDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE driving_trips ADD COLUMN noiseCount INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create route points table
                database.execSQL("""
                    CREATE TABLE trip_route_points (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        tripId INTEGER NOT NULL,
                        latitude REAL NOT NULL,
                        longitude REAL NOT NULL,
                        timestamp INTEGER NOT NULL,
                        speed REAL NOT NULL,
                        accuracy REAL NOT NULL DEFAULT 0
                    )
                """)

                // Create violation details table
                database.execSQL("""
                    CREATE TABLE violation_details (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        tripId INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        latitude REAL NOT NULL,
                        longitude REAL NOT NULL,
                        timestamp INTEGER NOT NULL,
                        severity REAL NOT NULL,
                        speed REAL NOT NULL DEFAULT 0
                    )
                """)
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "drivesafer_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration() // ADD THIS as safety net
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}