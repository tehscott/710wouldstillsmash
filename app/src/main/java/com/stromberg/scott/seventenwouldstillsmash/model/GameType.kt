package com.stromberg.scott.seventenwouldstillsmash.model

import com.google.firebase.database.Exclude

data class GameType (
    val id: String = "",
    var name: String = "",
    var iconName: String = "",
    var isDeleted: Boolean = false,
    @Exclude @set:Exclude @get:Exclude var needsEdit: Boolean = false
)