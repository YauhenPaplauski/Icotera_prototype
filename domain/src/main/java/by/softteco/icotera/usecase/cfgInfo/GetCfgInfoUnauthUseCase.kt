package by.softteco.icotera.usecase.cfgInfo

import by.softteco.icotera.executor.PostExecutorThread
import by.softteco.icotera.net.entity.CfgInfoUnath
import by.softteco.icotera.repository.CfgRepository
import by.softteco.icotera.usecase.BaseUseCase
import io.reactivex.Observable
import javax.inject.Inject

class GetCfgInfoUnauthUseCase @Inject constructor(
    postExecutorThread: PostExecutorThread,
    private val repository: CfgRepository
) : BaseUseCase(postExecutorThread) {
    fun getCfgInfoUnauth(url: String): Observable<CfgInfoUnath> {
        return repository.getCfgInfoUnauth(url)
    }
}