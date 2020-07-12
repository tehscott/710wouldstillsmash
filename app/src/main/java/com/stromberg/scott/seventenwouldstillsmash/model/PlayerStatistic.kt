package com.stromberg.scott.seventenwouldstillsmash.model

data class PlayerStatistic(
    var player: Player? = null,
    var gamesPlayed: Int = 0,
    var gamesWon: Int = 0,
    var is30GameStat: Boolean = false
)