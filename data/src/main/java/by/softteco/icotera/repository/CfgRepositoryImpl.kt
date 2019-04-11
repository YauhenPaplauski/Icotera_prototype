package by.softteco.icotera.repository

import by.softteco.icotera.net.entity.CfgInfoUnath
import by.softteco.icotera.net.rest.service.RestService
import io.reactivex.Observable
import javax.inject.Inject

class CfgRepositoryImpl @Inject constructor(private val restService: RestService) : CfgRepository {
    override fun getCfgInfoUnauth(url: String): Observable<CfgInfoUnath> {
        return restService.getCfgInfoUnauth(url)
    }

}