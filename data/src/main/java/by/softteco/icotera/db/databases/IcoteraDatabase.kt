package by.softteco.icotera.db.databases

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import by.softteco.icotera.db.dao.SpeedTestHistoryDao
import by.softteco.icotera.db.entity.SpeedTestHistoryItem

@Database(
    entities = [
        SpeedTestHistoryItem::class
    ], version = 1, exportSchema = false
)
abstract class IcoteraDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_NAME: String = "IcoteraDatabase"

        fun getInstance(context: Context): IcoteraDatabase {
            return Room
                .databaseBuilder(
                    context, IcoteraDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
        }
    }

    abstract fun speedTestHistory(): SpeedTestHistoryDao
}