package by.softteco.icotera.db.databases

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
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