package com.stromberg.scott.seventenwouldstillsmash.model

import java.io.Serializable

class CharacterStats : Serializable {
    var playerId: String = ""
    var characterId: Int = 0
    var royaleWins: Double = 0.0
    var royaleLosses: Double = 0.0
    var suddenDeathWins: Double = 0.0
    var suddenDeathLosses: Double = 0.0

    fun getTotalRoyaleGames(): Double {
        return royaleWins + royaleLosses
    }

    fun getTotalSuddenDeathGames(): Double {
        return suddenDeathWins + suddenDeathLosses
    }

    fun hasRoyaleGames(): Boolean {
        return royaleWins + royaleLosses > 0
    }

    fun hasSuddenDeathGames(): Boolean {
        return suddenDeathWins + suddenDeathLosses > 0
    }

    fun getRoyaleWinRate(): Double {
        if(hasRoyaleGames()) {
            return royaleWins / (royaleWins + royaleLosses)
        }

        return 0.0
    }

    fun getSuddenDeathWinRate(): Double {
        if(hasSuddenDeathGames()) {
            return suddenDeathWins / (suddenDeathWins + suddenDeathLosses)
        }

        return 0.0
    }
}