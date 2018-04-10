package com.stromberg.scott.seventenwouldstillsmash.model

import com.google.firebase.database.Exclude
import java.io.Serializable

class Player : Serializable {
    @Exclude
    var id: String? = null
    var name: String? = null
    var isHidden: Boolean = false
}