package com.stromberg.scott.seventenwouldstillsmash.util

import com.stromberg.scott.seventenwouldstillsmash.model.Game
import com.stromberg.scott.seventenwouldstillsmash.model.Player

class CharacterHelper {
    companion object {
        fun getTopCharacters(players: List<Player>, games: List<Game>): HashMap<String, ArrayList<Int>> {
            val topFiveCharacters = HashMap<String, ArrayList<Int>>()

            players.forEach { player ->
                val gamesWithCharacters = HashMap<Int, Int>()
                (0..Character.SIZE).forEachIndexed { _, characterId ->
                    val numGamesWithThisCharacter = games.count { it.players.any { gamePlayer -> gamePlayer.characterId == characterId && gamePlayer.player!!.id == player.id } }
                    gamesWithCharacters[characterId] = numGamesWithThisCharacter
                }

                val characterIds = ArrayList<Int>()

                gamesWithCharacters.entries.sortedByDescending { it.value }.take(5).forEach { game ->
                    characterIds.add(game.key)
                }

                topFiveCharacters[player.id!!] = characterIds
            }
            return topFiveCharacters
        }
    }
}