package com.stromberg.scott.seventenwouldstillsmash.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GamePlayer(
    var player: Player = Player(),
    var characterId: Int = -1,
    var winner: Boolean = false
) : Parcelable