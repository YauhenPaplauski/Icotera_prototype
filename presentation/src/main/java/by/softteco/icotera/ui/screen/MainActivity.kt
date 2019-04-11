package by.softteco.icotera.ui.screen

import android.os.Bundle
import android.os.PersistableBundle
import by.softteco.icotera.ui.base.BaseActivity
import by.softteco.icotera.usecase.cfgInfo.GetCfgInfoUnauthUseCase
import by.softteco.icotera.R
import javax.inject.Inject

class MainActivity : BaseActivity() {
    @Inject
    lateinit var getCfgInfoUseCase: GetCfgInfoUnauthUseCase

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_main)
//        startScanBtn.setOnClickListener { onStartScanClicked() }
//        initRecycler()
    }
}