package com.stromberg.scott.seventenwouldstillsmash.model

import com.google.firebase.database.Exclude

class Group {
    var code: String? = null

    @Exclude @set:Exclude @get:Exclude
    var isSelected: Boolean = false
}