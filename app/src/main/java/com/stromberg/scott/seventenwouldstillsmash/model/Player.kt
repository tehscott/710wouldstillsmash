package com.stromberg.scott.seventenwouldstillsmash.model

import android.content.Context
import com.google.firebase.database.Exclude
import com.stromberg.scott.seventenwouldstillsmash.App
import com.stromberg.scott.seventenwouldstillsmash.R
import java.io.Serializable

class Player : Serializable {
    @Exclude
    var id: String? = null

    var isHidden: Boolean
        @Exclude get() {
            val prefs = App.getContext().getSharedPreferences(App.getContext().getString(R.string.shared_prefs_key), Context.MODE_PRIVATE)
            return prefs.getBoolean(id + "_isHidden", false)
        }
        @Exclude set(value) {
            App.getContext().getSharedPreferences(App.getContext().getString(R.string.shared_prefs_key), Context.MODE_PRIVATE).edit().putBoolean(id + "_isHidden", value).apply()
        }

    var isLowPriority: Boolean
        @Exclude get() {
            val prefs = App.getContext().getSharedPreferences(App.getContext().getString(R.string.shared_prefs_key), Context.MODE_PRIVATE)
            return prefs.getBoolean(id + "_isLowPriority", false)
        }
        @Exclude set(value) {
            App.getContext().getSharedPreferences(App.getContext().getString(R.string.shared_prefs_key), Context.MODE_PRIVATE).edit().putBoolean(id + "_isLowPriority", value).apply()
        }

    var name: String? = null
}