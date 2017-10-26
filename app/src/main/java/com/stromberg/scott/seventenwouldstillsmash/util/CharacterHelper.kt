package com.stromberg.scott.seventenwouldstillsmash.util

import com.stromberg.scott.seventenwouldstillsmash.R

class CharacterHelper {
    companion object {
        fun getImage(characterId: Int): Int {
            when (characterId) {
                0 -> return R.drawable.ic_character_bayonetta
                1 -> return R.drawable.ic_character_bowser
                2 -> return R.drawable.ic_character_bowserjr
                3 -> return R.drawable.ic_character_captain
                4 -> return R.drawable.ic_character_charizard
                5 -> return R.drawable.ic_character_cloud
                6 -> return R.drawable.ic_character_corrin
                7 -> return R.drawable.ic_character_darkpit
                8 -> return R.drawable.ic_character_dedede
                9 -> return R.drawable.ic_character_diddy
                10 -> return R.drawable.ic_character_donkey
                11 -> return R.drawable.ic_character_drmario
                12 -> return R.drawable.ic_character_duckhunt
                13 -> return R.drawable.ic_character_falco
                14 -> return R.drawable.ic_character_fox
                15 -> return R.drawable.ic_character_gamewatch
                16 -> return R.drawable.ic_character_ganon
                17 -> return R.drawable.ic_character_greninja
                18 -> return R.drawable.ic_character_ike
                19 -> return R.drawable.ic_character_jigglypuff
                20 -> return R.drawable.ic_character_kirby
                21 -> return R.drawable.ic_character_link
                22 -> return R.drawable.ic_character_littlemac
                23 -> return R.drawable.ic_character_lucario
                24 -> return R.drawable.ic_character_lucas
                25 -> return R.drawable.ic_character_lucina
                26 -> return R.drawable.ic_character_luigi
                27 -> return R.drawable.ic_character_mario
                28 -> return R.drawable.ic_character_marth
                29 -> return R.drawable.ic_character_megaman
                30 -> return R.drawable.ic_character_metaknight
                31 -> return R.drawable.ic_character_mewtwo
                32 -> return R.drawable.ic_character_miifighter
                33 -> return R.drawable.ic_character_miigunner
                34 -> return R.drawable.ic_character_miiswordsman
                35 -> return R.drawable.ic_character_ness
                36 -> return R.drawable.ic_character_olimar
                37 -> return R.drawable.ic_character_pacman
                38 -> return R.drawable.ic_character_palutena
                39 -> return R.drawable.ic_character_peach
                40 -> return R.drawable.ic_character_pikachu
                41 -> return R.drawable.ic_character_pit
                42 -> return R.drawable.ic_character_rob
                43 -> return R.drawable.ic_character_robin
                44 -> return R.drawable.ic_character_rosalina
                45 -> return R.drawable.ic_character_roy
                46 -> return R.drawable.ic_character_ryu
                47 -> return R.drawable.ic_character_samus
                48 -> return R.drawable.ic_character_sheik
                49 -> return R.drawable.ic_character_shulk
                50 -> return R.drawable.ic_character_sonic
                51 -> return R.drawable.ic_character_toonlink
                52 -> return R.drawable.ic_character_villager
                53 -> return R.drawable.ic_character_wario
                54 -> return R.drawable.ic_character_wiifit
                55 -> return R.drawable.ic_character_yoshi
                56 -> return R.drawable.ic_character_zelda
                57 -> return R.drawable.ic_character_zerosuitsamus
            }

            return 0
        }

        fun getName(characterId: Int): String {
            when (characterId) {
                0 -> return "Bayonetta"
                1 -> return "Bowser"
                2 -> return "Bowser Jr."
                3 -> return "Captain Falcon"
                4 -> return "Charizard"
                5 -> return "Cloud"
                6 -> return "Corrin"
                7 -> return "Dark Pit"
                8 -> return "King DeDeDe"
                9 -> return "Diddy Kong"
                10 -> return "Donkey Kong"
                11 -> return "Dr. Mario"
                12 -> return "Duck Hunt"
                13 -> return "Falco"
                14 -> return "Fox"
                15 -> return "Mr. Game & Watch"
                16 -> return "Ganondorf"
                17 -> return "Greninja"
                18 -> return "Ike"
                19 -> return "Jigglypuff"
                20 -> return "Kirby"
                21 -> return "Link"
                22 -> return "Little Mac"
                23 -> return "Lucario"
                24 -> return "Lucas"
                25 -> return "Lucina"
                26 -> return "Luigi"
                27 -> return "Mario"
                28 -> return "Marth"
                29 -> return "Mega Man"
                30 -> return "Meta Knight"
                31 -> return "Mewtwo"
                32 -> return "Mii Fighter"
                33 -> return "Mii Gunner"
                34 -> return "Mii Swordsman"
                35 -> return "Ness"
                36 -> return "Olimar"
                37 -> return "Pacman"
                38 -> return "Palutena"
                39 -> return "Peach"
                40 -> return "Pikachu"
                41 -> return "Pit"
                42 -> return "Rob"
                43 -> return "Robin"
                44 -> return "Rosalina & Luma"
                45 -> return "Roy"
                46 -> return "Ryu"
                47 -> return "Samus"
                48 -> return "Sheik"
                49 -> return "Shulk"
                50 -> return "Sonic"
                51 -> return "Toon Link"
                52 -> return "Villager"
                53 -> return "Wario"
                54 -> return "Wii Fit Trainer"
                55 -> return "Yoshi"
                56 -> return "Zelda"
                57 -> return "Zero Suit Samus"
            }

            return ""
        }

        fun getId(characterName: String): Int {
            when (characterName.toLowerCase()) {
                "Bayonetta".toLowerCase() -> return 0
                "Bowser".toLowerCase() -> return 1
                "Bowser Jr.".toLowerCase() -> return 2
                "Captain Falcon".toLowerCase() -> return 3
                "Charizard".toLowerCase() -> return 4
                "Cloud".toLowerCase() -> return 5
                "Corrin".toLowerCase() -> return 6
                "Dark Pit".toLowerCase() -> return 7
                "King DeDeDe".toLowerCase() -> return 8
                "Diddy Kong".toLowerCase() -> return 9
                "Donkey Kong".toLowerCase() -> return 10
                "Dr. Mario".toLowerCase() -> return 11
                "Duck Hunt".toLowerCase() -> return 12
                "Falco".toLowerCase() -> return 13
                "Fox".toLowerCase() -> return 14
                "Mr. Game & Watch".toLowerCase() -> return 15
                "Ganondorf".toLowerCase() -> return 16
                "Greninja".toLowerCase() -> return 17
                "Ike".toLowerCase() -> return 18
                "Jigglypuff".toLowerCase() -> return 19
                "Kirby".toLowerCase() -> return 20
                "Link".toLowerCase() -> return 21
                "Little Mac".toLowerCase() -> return 22
                "Lucario".toLowerCase() -> return 23
                "Lucas".toLowerCase() -> return 24
                "Lucina".toLowerCase() -> return 25
                "Luigi".toLowerCase() -> return 26
                "Mario".toLowerCase() -> return 27
                "Marth".toLowerCase() -> return 28
                "Mega Man".toLowerCase() -> return 29
                "Meta Knight".toLowerCase() -> return 30
                "Mewtwo".toLowerCase() -> return 31
                "Mii Fighter".toLowerCase() -> return 32
                "Mii Gunner".toLowerCase() -> return 33
                "Mii Swordsman".toLowerCase() -> return 34
                "Ness".toLowerCase() -> return 35
                "Olimar".toLowerCase() -> return 36
                "Pacman".toLowerCase() -> return 37
                "Palutena".toLowerCase() -> return 38
                "Peach".toLowerCase() -> return 39
                "Pikachu".toLowerCase() -> return 40
                "Pit".toLowerCase() -> return 41
                "Rob".toLowerCase() -> return 42
                "Robin".toLowerCase() -> return 43
                "Rosalina & Luma".toLowerCase() -> return 44
                "Roy".toLowerCase() -> return 45
                "Ryu".toLowerCase() -> return 46
                "Samus".toLowerCase() -> return 47
                "Sheik".toLowerCase() -> return 48
                "Shulk".toLowerCase() -> return 49
                "Sonic".toLowerCase() -> return 50
                "Toon Link".toLowerCase() -> return 51
                "Villager".toLowerCase() -> return 52
                "Wario".toLowerCase() -> return 53
                "Wii Fit Trainer".toLowerCase() -> return 54
                "Yoshi".toLowerCase() -> return 55
                "Zelda".toLowerCase() -> return 56
                "Zero Suit Samus".toLowerCase() -> return 57
            }

            return -1
        }
    }
}