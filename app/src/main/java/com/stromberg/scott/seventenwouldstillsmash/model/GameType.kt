package com.stromberg.scott.seventenwouldstillsmash.model

enum class GameType {
    ROYALE {
        override fun toString(): String {
            return "royale"
        }
    },
    SUDDEN_DEATH {
        override fun toString(): String {
            return "sudden_death"
        }
    };

    fun prettyName(): String {
        return when (this) {
            ROYALE -> {
                "Royale"
            }
            SUDDEN_DEATH -> {
                "Sudden Death"
            }
        }
    }
}