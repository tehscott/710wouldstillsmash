package com.stromberg.scott.seventenwouldstillsmash.model

enum class GameResult {
    UNKNOWN,
    WIN,
    LOSS;

    fun toString(gameCount: Int): String {
        return when (this) {
            UNKNOWN -> {
                "unknown"
            }
            WIN -> {
                if(gameCount != 1) "wins" else "win"
            }
            LOSS -> {
                if(gameCount != 1) "losses" else "loss"
            }
        }
    }
}