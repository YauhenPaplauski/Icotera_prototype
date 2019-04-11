package by.softteco.icotera.di.moduls.data

import by.softteco.icotera.net.rest.service.RestService
import dagger.Module
import dagger.Provides

@Module
class RestServiceModule {
    @Provides
    fun provideRestService(): RestService {
        return RestService()
    }
}