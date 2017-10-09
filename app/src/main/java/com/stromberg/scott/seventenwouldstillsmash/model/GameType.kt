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
    }
}