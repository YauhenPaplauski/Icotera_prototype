package by.softteco.icotera.di.moduls.data

import android.content.Context
import by.softteco.icotera.db.databases.IcoteraDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DataModule {
    @Provides
    @Singleton
    fun provideDatabase(context: Context): IcoteraDatabase {
        return IcoteraDatabase.getInstance(context)
    }
}