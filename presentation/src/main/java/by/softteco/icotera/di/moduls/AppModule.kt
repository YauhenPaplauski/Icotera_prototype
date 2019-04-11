package by.softteco.icotera.di.moduls

import android.content.Context
import by.softteco.icotera.app.App
import by.softteco.icotera.executer.UIThread
import by.softteco.icotera.executor.PostExecutorThread
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(private val app: App) {
    @Provides
    @Singleton
    fun provideContext(): Context = app

    @Provides
    @Singleton
    fun providePostExecutorThread(): PostExecutorThread {
        return UIThread()
    }
}