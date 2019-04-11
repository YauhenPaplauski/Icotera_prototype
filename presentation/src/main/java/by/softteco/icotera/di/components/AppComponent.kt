package by.softteco.icotera.di.components

import by.softteco.icotera.app.App
import by.softteco.icotera.di.moduls.AppModule
import by.softteco.icotera.di.moduls.data.DaoModule
import by.softteco.icotera.di.moduls.data.DataModule
import by.softteco.icotera.di.moduls.data.RestServiceModule
import by.softteco.icotera.di.moduls.repository.RepositoryModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        DaoModule::class,
        DataModule::class,
        RestServiceModule::class,
        RepositoryModule::class
    ]
)
interface AppComponent {
    fun inject(app: App)
}