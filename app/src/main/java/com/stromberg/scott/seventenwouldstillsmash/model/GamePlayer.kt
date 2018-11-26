package com.stromberg.scott.seventenwouldstillsmash.model

import java.io.Serializable

class GamePlayer : Serializable {
    var player: Player? = null
    var characterId: Int = -1
    var winner: Boolean = false
}