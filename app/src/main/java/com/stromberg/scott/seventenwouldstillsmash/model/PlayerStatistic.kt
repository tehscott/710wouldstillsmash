package com.stromberg.scott.seventenwouldstillsmash.model

import java.io.Serializable

class PlayerStatistic : Serializable {
    var player: Player? = null
    var gameType: GameType2? = null
    var gamesPlayed = 0
    var gamesWon = 0
    var is30GameStat = false
}