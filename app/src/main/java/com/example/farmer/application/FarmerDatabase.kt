package com.example.farmer.application
import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.farmer.Dao.FarmerDao
import com.example.farmer.Model.Farmer


@Database(entities = [Farmer::class], version = 1, exportSchema = false)
abstract class FarmerDatabase: RoomDatabase() {
    abstract fun farmerDao(): FarmerDao

    companion object {
        private var INSTANCE:FarmerDatabase? = null

        fun getDatabase(context: Context): FarmerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    FarmerDatabase::class.java,
                    "farmer_database_10"
                )
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}