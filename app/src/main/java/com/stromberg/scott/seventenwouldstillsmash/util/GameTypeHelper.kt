package com.stromberg.scott.seventenwouldstillsmash.util

import android.content.Context
import com.google.gson.Gson
import com.stromberg.scott.seventenwouldstillsmash.App
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.model.GameType2
import java.util.ArrayList

class GameTypeHelper {
    companion object {
        fun saveGameTypes(gameTypes: ArrayList<GameType2>) {
            val prefs = App.getContext().getSharedPreferences(App.getContext().getString(R.string.shared_prefs_key), Context.MODE_PRIVATE)
            prefs.edit().putString(App.getContext().getString(R.string.shared_prefs_game_types), Gson().toJson(gameTypes)).apply()
        }

        fun getGameTypes(): ArrayList<GameType2>? {
            val prefs = App.getContext().getSharedPreferences(App.getContext().getString(R.string.shared_prefs_key), Context.MODE_PRIVATE)
            return Gson().fromJson<Array<GameType2>>(prefs.getString(App.getContext().getString(R.string.shared_prefs_game_types), ""), Array<GameType2>::class.java)?.toCollection(ArrayList())
        }

        fun getGameType(gameTypeId: String?): GameType2? {
            return getGameTypes()?.find { it.id == gameTypeId }
        }

        fun addGameType(gameType: GameType2) {
            var gameTypes = getGameTypes()

            if(gameTypes == null) {
                gameTypes = ArrayList()
            }

            gameTypes.add(gameType)

            saveGameTypes(gameTypes)
        }

        fun updateGameType(gameType: GameType2) {
            val gameTypes = getGameTypes()

            if(gameTypes != null) {
                gameTypes.forEach {
                    if(it.id == gameType.id) {
                        it.iconName = gameType.iconName
                        it.isDeleted = gameType.isDeleted
                        it.name = gameType.name
                    }
                }

                saveGameTypes(gameTypes)
            }
            else {
                addGameType(gameType)
            }
        }

        fun deleteGameType(gameType: GameType2) {
            val gameTypes = getGameTypes()

            if(gameTypes != null) {
                var gameTypeToRemove: GameType2? = null

                gameTypes.forEach {
                    if(it.id == gameType.id) {
                        gameTypeToRemove = it
                    }
                }

                if(gameTypeToRemove != null) {
                    gameTypes.remove(gameTypeToRemove!!)
                }

                saveGameTypes(gameTypes)
            }
        }
    }
}