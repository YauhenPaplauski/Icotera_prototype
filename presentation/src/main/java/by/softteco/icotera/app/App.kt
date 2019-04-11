package by.softteco.icotera.app

import android.app.Application
import by.softteco.icotera.di.components.AppComponent
import by.softteco.icotera.di.components.DaggerAppComponent
import by.softteco.icotera.di.moduls.AppModule

class App : Application() {
    companion object {
        lateinit var instance: App
        @JvmStatic
        lateinit var appComponent: AppComponent
    }

    init {
        instance = this
        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()
    }

    override fun onCreate() {
        appComponent.inject(this)
        super.onCreate()
    }
}