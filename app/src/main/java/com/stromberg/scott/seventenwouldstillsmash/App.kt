package com.stromberg.scott.seventenwouldstillsmash

import android.app.Application
import uk.co.chrisjenx.calligraphy.CalligraphyConfig

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
            .setDefaultFontPath("Quicksand-Light.ttf")
            .setFontAttrId(R.attr.fontPath)
            .build()
        )
    }
}