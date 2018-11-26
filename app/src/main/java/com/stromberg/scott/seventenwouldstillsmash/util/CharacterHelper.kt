package com.stromberg.scott.seventenwouldstillsmash.util

import android.content.Context
import com.google.gson.Gson
import com.stromberg.scott.seventenwouldstillsmash.App
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.model.Game
import com.stromberg.scott.seventenwouldstillsmash.model.Group
import com.stromberg.scott.seventenwouldstillsmash.model.GroupType
import com.stromberg.scott.seventenwouldstillsmash.model.Player

class CharacterHelper {
    companion object {
        fun getTopCharacters(players: List<Player>, games: List<Game>): HashMap<String, ArrayList<Int>> {
            val topFiveCharacters = HashMap<String, ArrayList<Int>>()

            players.forEach {
                val gamesWithCharacters = HashMap<Int, Int>()
                val player = it
                (0..CharacterHelper.getNumberOfCharacters()).forEachIndexed { _, characterId ->
                    val numGamesWithThisCharacter = games.count { it.players.any { it.characterId == characterId && it.player!!.id == player.id } }
                    gamesWithCharacters[characterId] = numGamesWithThisCharacter
                }

                val characterIds = ArrayList<Int>()

                gamesWithCharacters.entries.sortedByDescending { it.value }.take(5).forEach {
                    characterIds.add(it.key)
                }

                topFiveCharacters[player.id!!] = characterIds
            }
            return topFiveCharacters
        }

        fun getNumberOfCharacters() : Int {
            if(isSSB4()) {
                return 57
            }
            else {
                return 74
            }
        }

        private fun isSSB4() : Boolean {
            val prefs = App.getContext().getSharedPreferences(App.getContext().getString(R.string.shared_prefs_key), Context.MODE_PRIVATE)
            val groups = Gson().fromJson<Array<Group>>(prefs.getString(App.getContext().getString(R.string.shared_prefs_group_codes), ""), Array<Group>::class.java)?.toCollection(ArrayList())
            val selectedGroup = groups?.firstOrNull { it.isSelected }

            return selectedGroup?.type == GroupType.SMASH4
        }

        fun getImage(characterId: Int): Int {
            return if(isSSB4()) {
                getSSB4Image(characterId)
            } else {
                getSSBUImage(characterId)
            }
        }

        private fun getSSB4Image(characterId: Int): Int {
            when (characterId) {
                0 -> return R.drawable.ssb4_character_bayonetta
                1 -> return R.drawable.ssb4_character_bowser
                2 -> return R.drawable.ssb4_character_bowserjr
                3 -> return R.drawable.ssb4_character_captain
                4 -> return R.drawable.ssb4_character_charizard
                5 -> return R.drawable.ssb4_character_cloud
                6 -> return R.drawable.ssb4_character_corrin
                7 -> return R.drawable.ssb4_character_darkpit
                8 -> return R.drawable.ssb4_character_dedede
                9 -> return R.drawable.ssb4_character_diddy
                10 -> return R.drawable.ssb4_character_donkey
                11 -> return R.drawable.ssb4_character_drmario
                12 -> return R.drawable.ssb4_character_duckhunt
                13 -> return R.drawable.ssb4_character_falco
                14 -> return R.drawable.ssb4_character_fox
                15 -> return R.drawable.ssb4_character_gamewatch
                16 -> return R.drawable.ssb4_character_ganon
                17 -> return R.drawable.ssb4_character_greninja
                18 -> return R.drawable.ssb4_character_ike
                19 -> return R.drawable.ssb4_character_jigglypuff
                20 -> return R.drawable.ssb4_character_kirby
                21 -> return R.drawable.ssb4_character_link
                22 -> return R.drawable.ssb4_character_littlemac
                23 -> return R.drawable.ssb4_character_lucario
                24 -> return R.drawable.ssb4_character_lucas
                25 -> return R.drawable.ssb4_character_lucina
                26 -> return R.drawable.ssb4_character_luigi
                27 -> return R.drawable.ssb4_character_mario
                28 -> return R.drawable.ssb4_character_marth
                29 -> return R.drawable.ssb4_character_megaman
                30 -> return R.drawable.ssb4_character_metaknight
                31 -> return R.drawable.ssb4_character_mewtwo
                32 -> return R.drawable.ssb4_character_miifighter
                33 -> return R.drawable.ssb4_character_miigunner
                34 -> return R.drawable.ssb4_character_miiswordsman
                35 -> return R.drawable.ssb4_character_ness
                36 -> return R.drawable.ssb4_character_olimar
                37 -> return R.drawable.ssb4_character_pacman
                38 -> return R.drawable.ssb4_character_palutena
                39 -> return R.drawable.ssb4_character_peach
                40 -> return R.drawable.ssb4_character_pikachu
                41 -> return R.drawable.ssb4_character_pit
                42 -> return R.drawable.ssb4_character_rob
                43 -> return R.drawable.ssb4_character_robin
                44 -> return R.drawable.ssb4_character_rosalina
                45 -> return R.drawable.ssb4_character_roy
                46 -> return R.drawable.ssb4_character_ryu
                47 -> return R.drawable.ssb4_character_samus
                48 -> return R.drawable.ssb4_character_sheik
                49 -> return R.drawable.ssb4_character_shulk
                50 -> return R.drawable.ssb4_character_sonic
                51 -> return R.drawable.ssb4_character_toonlink
                52 -> return R.drawable.ssb4_character_villager
                53 -> return R.drawable.ssb4_character_wario
                54 -> return R.drawable.ssb4_character_wiifit
                55 -> return R.drawable.ssb4_character_yoshi
                56 -> return R.drawable.ssb4_character_zelda
                57 -> return R.drawable.ssb4_character_zerosuitsamus
            }

            return 0
        }

        private fun getSSBUImage(characterId: Int): Int {
            when (characterId) {
                0 -> return R.drawable.ssbu_character_bayonetta
                1 -> return R.drawable.ssbu_character_bowser
                2 -> return R.drawable.ssbu_character_bowser_jr
                3 -> return R.drawable.ssbu_character_captain_falcon
                4 -> return R.drawable.ssbu_character_chrom
                5 -> return R.drawable.ssbu_character_cloud
                6 -> return R.drawable.ssbu_character_corrin
                7 -> return R.drawable.ssbu_character_daisy
                8 -> return R.drawable.ssbu_character_dark_pit
                9 -> return R.drawable.ssbu_character_dark_samus
                10 -> return R.drawable.ssbu_character_diddy_kong
                11 -> return R.drawable.ssbu_character_donkey_kong
                12 -> return R.drawable.ssbu_character_dr_mario
                13 -> return R.drawable.ssbu_character_duck_hunt
                14 -> return R.drawable.ssbu_character_falco
                15 -> return R.drawable.ssbu_character_fox
                16 -> return R.drawable.ssbu_character_ganondorf
                17 -> return R.drawable.ssbu_character_greninja
                18 -> return R.drawable.ssbu_character_ice_climbers
                19 -> return R.drawable.ssbu_character_ike
                20 -> return R.drawable.ssbu_character_incineroar
                21 -> return R.drawable.ssbu_character_inkling
                22 -> return R.drawable.ssbu_character_isabelle
                23 -> return R.drawable.ssbu_character_jiggly_puff
                24 -> return R.drawable.ssbu_character_ken
                25 -> return R.drawable.ssbu_character_king_dedede
                26 -> return R.drawable.ssbu_character_king_k_rool
                27 -> return R.drawable.ssbu_character_kirby
                28 -> return R.drawable.ssbu_character_little_mac
                29 -> return R.drawable.ssbu_character_link
                30 -> return R.drawable.ssbu_character_lucario
                31 -> return R.drawable.ssbu_character_lucas
                32 -> return R.drawable.ssbu_character_lucina
                33 -> return R.drawable.ssbu_character_luigi
                34 -> return R.drawable.ssbu_character_mario
                35 -> return R.drawable.ssbu_character_marth
                36 -> return R.drawable.ssbu_character_mega_man
                37 -> return R.drawable.ssbu_character_meta_knight
                38 -> return R.drawable.ssbu_character_mewtwo
                39 -> return R.drawable.ssbu_character_mii_brawler
                40 -> return R.drawable.ssbu_character_mii_gunner
                41 -> return R.drawable.ssbu_character_mii_swordfighter
                42 -> return R.drawable.ssbu_character_mr_game_and_watch
                43 -> return R.drawable.ssbu_character_ness
                44 -> return R.drawable.ssbu_character_olimar
                45 -> return R.drawable.ssbu_character_pacman
                46 -> return R.drawable.ssbu_character_palutena
                47 -> return R.drawable.ssbu_character_peach
                48 -> return R.drawable.ssbu_character_pichu
                49 -> return R.drawable.ssbu_character_pikachu
                50 -> return R.drawable.ssbu_character_piranha_plant
                51 -> return R.drawable.ssbu_character_pit
                52 -> return R.drawable.ssbu_character_pokemon_trainer
                53 -> return R.drawable.ssbu_character_richter
                54 -> return R.drawable.ssbu_character_ridley
                55 -> return R.drawable.ssbu_character_rob
                56 -> return R.drawable.ssbu_character_robin
                57 -> return R.drawable.ssbu_character_rosalina_and_luma
                58 -> return R.drawable.ssbu_character_roy
                59 -> return R.drawable.ssbu_character_ryu
                60 -> return R.drawable.ssbu_character_samus_aran
                61 -> return R.drawable.ssbu_character_sheik
                62 -> return R.drawable.ssbu_character_shulk
                63 -> return R.drawable.ssbu_character_simon
                64 -> return R.drawable.ssbu_character_snake
                65 -> return R.drawable.ssbu_character_sonic
                66 -> return R.drawable.ssbu_character_toon_link
                67 -> return R.drawable.ssbu_character_villager
                68 -> return R.drawable.ssbu_character_wario
                69 -> return R.drawable.ssbu_character_wii_fit_trainer
                70 -> return R.drawable.ssbu_character_wolf
                71 -> return R.drawable.ssbu_character_yoshi
                72 -> return R.drawable.ssbu_character_young_link
                73 -> return R.drawable.ssbu_character_zelda
                74 -> return R.drawable.ssbu_character_zero_suit_samus
            }

            return 0
        }

        fun getName(characterId: Int): String {
            return if(isSSB4()) {
                getSSB4Name(characterId)
            } else {
                getSSBUName(characterId)
            }
        }

        private fun getSSB4Name(characterId: Int): String {
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

        private fun getSSBUName(characterId: Int): String {
            when (characterId) {
                0 -> return "Bayonetta"
                1 -> return "Bowser"
                2 -> return "Bowser Jr."
                3 -> return "Captain Falcon"
                4 -> return "Chrom"
                5 -> return "Cloud"
                6 -> return "Corrin"
                7 -> return "Daisy"
                8 -> return "Dark Pit"
                9 -> return "Dark Samus"
                10 -> return "Diddy Kong"
                11 -> return "Donkey Kong"
                12 -> return "Dr. Mario"
                13 -> return "Duck Hunt"
                14 -> return "Falco"
                15 -> return "Fox"
                16 -> return "Ganondorf"
                17 -> return "Greninja"
                18 -> return "Ice Climbers"
                19 -> return "Ike"
                20 -> return "Incineroar"
                21 -> return "Inkling"
                22 -> return "Isabelle"
                23 -> return "Jigglypuff"
                24 -> return "Ken"
                25 -> return "King Dedede"
                26 -> return "King K. Rool"
                27 -> return "Kirby"
                28 -> return "Little Mac"
                29 -> return "Link"
                30 -> return "Lucario"
                31 -> return "Lucas"
                32 -> return "Lucina"
                33 -> return "Luigi"
                34 -> return "Mario"
                35 -> return "Marth"
                36 -> return "Mega Man"
                37 -> return "Meta Knight"
                38 -> return "Mewtwo"
                39 -> return "Mii Brawler"
                40 -> return "Mii Gunner"
                41 -> return "Mii Swordfighter"
                42 -> return "Mr. Game & Watch"
                43 -> return "Ness"
                44 -> return "Olimar"
                45 -> return "Pac-Man"
                46 -> return "Palutena"
                47 -> return "Peach"
                48 -> return "Pichu"
                49 -> return "Pikachu"
                50 -> return "Piranha Plant"
                51 -> return "Pit"
                52 -> return "Pokémon Trainer"
                53 -> return "Richter"
                54 -> return "Ridley"
                55 -> return "Rob"
                56 -> return "Robin"
                57 -> return "Rosalina & Luma"
                58 -> return "Roy"
                59 -> return "Ryu"
                60 -> return "Samus"
                61 -> return "Sheik"
                62 -> return "Shulk"
                63 -> return "Simon"
                64 -> return "Snake"
                65 -> return "Sonic"
                66 -> return "Toon Link"
                67 -> return "Villager"
                68 -> return "Wario"
                69 -> return "Wii Fit Trainer"
                70 -> return "Wolf"
                71 -> return "Yoshi"
                72 -> return "Young Link"
                73 -> return "Zelda"
                74 -> return "Zero Suit Samus"
            }

            return ""
        }

        fun getId(characterName: String): Int {
            return if(isSSB4()) {
                getSSB4Id(characterName)
            } else {
                getSSBUId(characterName)
            }
        }

        private fun getSSB4Id(characterName: String): Int {
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

        private fun getSSBUId(characterName: String): Int {
            when (characterName.toLowerCase()) {
                "Bayonetta".toLowerCase() -> return 0
                "Bowser".toLowerCase() -> return 1
                "Bowser Jr.".toLowerCase() -> return 2
                "Captain Falcon".toLowerCase() -> return 3
                "Chrom".toLowerCase() -> return 4
                "Cloud".toLowerCase() -> return 5
                "Corrin".toLowerCase() -> return 6
                "Daisy".toLowerCase() -> return 7
                "Dark Pit".toLowerCase() -> return 8
                "Dark Samus".toLowerCase() -> return 9
                "Diddy Kong".toLowerCase() -> return 10
                "Donkey Kong".toLowerCase() -> return 11
                "Dr. Mario".toLowerCase() -> return 12
                "Duck Hunt".toLowerCase() -> return 13
                "Falco".toLowerCase() -> return 14
                "Fox".toLowerCase() -> return 15
                "Ganondorf".toLowerCase() -> return 16
                "Greninja".toLowerCase() -> return 17
                "Ice Climbers".toLowerCase() -> return 18
                "Ike".toLowerCase() -> return 19
                "Incineroar".toLowerCase() -> return 20
                "Inkling".toLowerCase() -> return 21
                "Isabelle".toLowerCase() -> return 22
                "Jigglypuff".toLowerCase() -> return 23
                "Ken".toLowerCase() -> return 24
                "King Dedede".toLowerCase() -> return 25
                "King K. Rool".toLowerCase() -> return 26
                "Kirby".toLowerCase() -> return 27
                "Little Mac".toLowerCase() -> return 28
                "Link".toLowerCase() -> return 29
                "Lucario".toLowerCase() -> return 30
                "Lucas".toLowerCase() -> return 31
                "Lucina".toLowerCase() -> return 32
                "Luigi".toLowerCase() -> return 33
                "Mario".toLowerCase() -> return 34
                "Marth".toLowerCase() -> return 35
                "Mega Man".toLowerCase() -> return 36
                "Meta Knight".toLowerCase() -> return 37
                "Mewtwo".toLowerCase() -> return 38
                "Mii Brawler".toLowerCase() -> return 39
                "Mii Gunner".toLowerCase() -> return 40
                "Mii Swordfighter".toLowerCase() -> return 41
                "Mr. Game & Watch".toLowerCase() -> return 42
                "Ness".toLowerCase() -> return 43
                "Olimar".toLowerCase() -> return 44
                "Pac-Man".toLowerCase() -> return 45
                "Palutena".toLowerCase() -> return 46
                "Peach".toLowerCase() -> return 47
                "Pichu".toLowerCase() -> return 48
                "Pikachu".toLowerCase() -> return 49
                "Piranha Plant".toLowerCase() -> return 50
                "Pit".toLowerCase() -> return 51
                "Pokémon Trainer".toLowerCase() -> return 52
                "Richter".toLowerCase() -> return 53
                "Ridley".toLowerCase() -> return 54
                "Rob".toLowerCase() -> return 55
                "Robin".toLowerCase() -> return 56
                "Rosalina & Luma".toLowerCase() -> return 57
                "Roy".toLowerCase() -> return 58
                "Ryu".toLowerCase() -> return 59
                "Samus".toLowerCase() -> return 60
                "Sheik".toLowerCase() -> return 61
                "Shulk".toLowerCase() -> return 62
                "Simon".toLowerCase() -> return 63
                "Snake".toLowerCase() -> return 64
                "Sonic".toLowerCase() -> return 65
                "Toon Link".toLowerCase() -> return 66
                "Villager".toLowerCase() -> return 67
                "Wario".toLowerCase() -> return 68
                "Wii Fit Trainer".toLowerCase() -> return 69
                "Wolf".toLowerCase() -> return 70
                "Yoshi".toLowerCase() -> return 71
                "Young Link".toLowerCase() -> return 72
                "Zelda".toLowerCase() -> return 73
                "Zero Suit Samus".toLowerCase() -> return 74
            }

            return -1
        }
    }
}