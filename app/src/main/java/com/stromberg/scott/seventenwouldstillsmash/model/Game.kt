package com.stromberg.scott.seventenwouldstillsmash.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Game(
    var id: String = "",
    var date: Long = Calendar.getInstance().time.time,
    var players: ArrayList<GamePlayer> = arrayListOf()
) : Parcelable