package com.stromberg.scott.seventenwouldstillsmash.model

enum class GroupType {
    SMASH4 {
        override fun toString(): String {
            return "smash4"
        }
    },
    ULTIMATE {
        override fun toString(): String {
            return "ultimate"
        }
    };

    fun prettyName(): String {
        return when (this) {
            SMASH4 -> {
                "SSB 4"
            }
            ULTIMATE -> {
                "SSB Ultimate"
            }
        }
    }

    fun shortName(): String {
        return when (this) {
            SMASH4 -> {
                "SSB4"
            }
            ULTIMATE -> {
                "SSBU"
            }
        }
    }
}