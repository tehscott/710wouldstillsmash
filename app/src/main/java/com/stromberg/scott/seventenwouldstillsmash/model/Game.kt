package com.stromberg.scott.seventenwouldstillsmash.model

import android.os.Parcel
import android.os.Parcelable
import java.util.*

class Game() : Parcelable {
    var id: String? = ""
    var date: Long = 0
    var players: ArrayList<GamePlayer> = ArrayList()

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        date = parcel.readLong()
        players = parcel.readArrayList(GamePlayer::class.java.classLoader) as ArrayList<GamePlayer>
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeLong(date)
        parcel.writeList(players.toList())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Game> {
        override fun createFromParcel(parcel: Parcel): Game {
            return Game(parcel)
        }

        override fun newArray(size: Int): Array<Game?> {
            return arrayOfNulls(size)
        }
    }
}