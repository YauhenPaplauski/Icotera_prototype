package by.softteco.icotera.di.moduls.data

import by.softteco.icotera.db.dao.SpeedTestHistoryDao
import by.softteco.icotera.db.databases.IcoteraDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DaoModule {
    @Provides
    @Singleton
    fun provideSpeedTestHistoryDao(icoteraDatabase: IcoteraDatabase): SpeedTestHistoryDao {
        return icoteraDatabase.speedTestHistory()
    }
}