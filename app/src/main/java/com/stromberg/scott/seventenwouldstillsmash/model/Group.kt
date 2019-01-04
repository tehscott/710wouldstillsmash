package com.stromberg.scott.seventenwouldstillsmash.model

import com.google.firebase.database.Exclude

class Group {
    var code: String? = null
    var type: GroupType = GroupType.SMASH4

    @Exclude @set:Exclude @get:Exclude
    var isSelected: Boolean = false
}