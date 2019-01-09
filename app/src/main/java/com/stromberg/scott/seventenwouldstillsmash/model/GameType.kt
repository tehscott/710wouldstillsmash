package com.stromberg.scott.seventenwouldstillsmash.model

import com.google.firebase.database.Exclude

data class GameType2 (
    val id: String = "",
    var name: String = "",
    var iconName: String = "",
    var isDeleted: Boolean = false,
    @Exclude @set:Exclude @get:Exclude var needsEdit: Boolean = false
)

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