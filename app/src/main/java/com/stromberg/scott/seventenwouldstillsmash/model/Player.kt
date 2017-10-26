package com.stromberg.scott.seventenwouldstillsmash.model

import com.google.firebase.database.Exclude

class Player {
    @Exclude
    var id: String? = null
    var name: String? = null
}