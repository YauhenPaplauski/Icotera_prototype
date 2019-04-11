package by.softteco.icotera.di.moduls.repository

import by.softteco.icotera.net.rest.service.RestService
import by.softteco.icotera.repository.CfgRepository
import by.softteco.icotera.repository.CfgRepositoryImpl
import dagger.Module
import dagger.Provides

@Module
class RepositoryModule {
    @Provides
    fun provideCfgRepository(restService: RestService): CfgRepository {
        return CfgRepositoryImpl(restService)
    }
}