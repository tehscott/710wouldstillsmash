package com.stromberg.scott.seventenwouldstillsmash

import android.app.Application
import android.content.Context
import uk.co.chrisjenx.calligraphy.CalligraphyConfig

class App : Application() {
    companion object {
        private lateinit var instance: App

        fun getContext(): Context {
            return instance.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
            .setDefaultFontPath("Quicksand-Light.ttf")
            .setFontAttrId(R.attr.fontPath)
            .build()
        )
    }
}