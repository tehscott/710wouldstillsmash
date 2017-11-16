package com.stromberg.scott.seventenwouldstillsmash.model

import java.io.Serializable

class CharacterStats : Serializable {
    var playerId: String = ""
    var characterId: Int = 0
    var wins: Int = 0
    var losses: Int = 0
}