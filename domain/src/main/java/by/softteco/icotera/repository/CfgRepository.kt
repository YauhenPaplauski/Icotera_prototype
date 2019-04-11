package by.softteco.icotera.repository

import by.softteco.icotera.net.entity.CfgInfoUnath
import io.reactivex.Observable

interface CfgRepository {
    fun getCfgInfoUnauth(url:String): Observable<CfgInfoUnath>
}