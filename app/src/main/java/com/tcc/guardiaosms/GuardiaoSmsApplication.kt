package com.tcc.guardiaosms

import android.app.Application
import com.tcc.guardiaosms.di.AppContainer

class GuardiaoSmsApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
