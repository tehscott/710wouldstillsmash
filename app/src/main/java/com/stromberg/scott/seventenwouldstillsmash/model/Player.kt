package com.stromberg.scott.seventenwouldstillsmash.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Player(
    var id: String = "",
    var name: String = ""
) : Parcelable