package com.stromberg.scott.seventenwouldstillsmash.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.database.Exclude
import java.util.*

class Game() : Parcelable {
    @Exclude
    var id: String = ""

    var date: Long = 0
    var players: ArrayList<GamePlayer> = ArrayList()
    var gameType: String? = null

    constructor(parcel: Parcel) : this() {
        date = parcel.readLong()
        gameType = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(date)
        parcel.writeString(gameType)
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