package com.stromberg.scott.seventenwouldstillsmash.model

import java.io.Serializable

class CharacterStats : Serializable {
    var playerId: String = ""
    var characterId: Int = 0
    var royaleWins: Int = 0
    var royaleLosses: Int = 0
    var suddenDeathWins: Int = 0
    var suddenDeathLosses: Int = 0
}