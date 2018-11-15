package com.stromberg.scott.seventenwouldstillsmash.util

import android.content.Context
import com.google.gson.Gson
import com.stromberg.scott.seventenwouldstillsmash.App
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.model.Group
import com.stromberg.scott.seventenwouldstillsmash.model.GroupType

class CharacterHelper {
    companion object {
        fun getNumberOfCharacters() : Int {
            if(isSSB4()) {
                return 57
            }
            else {
                return 65
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
                0	-> 	R.drawable.ssbu_character_bayonetta
                1	-> 	R.drawable.ssbu_character_bowser
                2	-> 	R.drawable.ssbu_character_bowser_jr
                3	-> 	R.drawable.ssbu_character_captain_falcon
                4	-> 	R.drawable.ssbu_character_cloud
                5	-> 	R.drawable.ssbu_character_corrin
                6	-> 	R.drawable.ssbu_character_daisy
                7	-> 	R.drawable.ssbu_character_dark_pit
                8	-> 	R.drawable.ssbu_character_diddy_kong
                9	-> 	R.drawable.ssbu_character_donkey_kong
                10	-> 	R.drawable.ssbu_character_dr_mario
                11	-> 	R.drawable.ssbu_character_duck_hunt
                12	-> 	R.drawable.ssbu_character_falco
                13	-> 	R.drawable.ssbu_character_fox
                14	-> 	R.drawable.ssbu_character_ganondorf
                15	-> 	R.drawable.ssbu_character_greninja
                16	-> 	R.drawable.ssbu_character_ice_climbers
                17	-> 	R.drawable.ssbu_character_ike
                18	-> 	R.drawable.ssbu_character_inkling
                19	-> 	R.drawable.ssbu_character_jiggly_puff
                20	-> 	R.drawable.ssbu_character_king_dedede
                21	-> 	R.drawable.ssbu_character_kirby
                22	-> 	R.drawable.ssbu_character_link
                23	-> 	R.drawable.ssbu_character_little_mac
                24	-> 	R.drawable.ssbu_character_lucario
                25	-> 	R.drawable.ssbu_character_lucas
                26	-> 	R.drawable.ssbu_character_lucina
                27	-> 	R.drawable.ssbu_character_luigi
                28	-> 	R.drawable.ssbu_character_marth
                29	-> 	R.drawable.ssbu_character_mario
                30	-> 	R.drawable.ssbu_character_mega_man
                31	-> 	R.drawable.ssbu_character_meta_knight
                32	-> 	R.drawable.ssbu_character_mewtwo
                33	-> 	R.drawable.ssbu_character_mii_brawler
                34	-> 	R.drawable.ssbu_character_mii_gunner
                35	-> 	R.drawable.ssbu_character_mii_swordsman
                36	-> 	R.drawable.ssbu_character_mr_game_and_watch
                37	-> 	R.drawable.ssbu_character_ness
                38	-> 	R.drawable.ssbu_character_olimar
                39	-> 	R.drawable.ssbu_character_palutena
                40	-> 	R.drawable.ssbu_character_pacman
                41	-> 	R.drawable.ssbu_character_peach
                42	-> 	R.drawable.ssbu_character_pichu
                43	-> 	R.drawable.ssbu_character_pikachu
                44	-> 	R.drawable.ssbu_character_pit
                45	-> 	R.drawable.ssbu_character_pokemon_trainer
                46	-> 	R.drawable.ssbu_character_ridley
                47	-> 	R.drawable.ssbu_character_rob
                48	-> 	R.drawable.ssbu_character_robin
                49	-> 	R.drawable.ssbu_character_rosalina_and_luma
                50	-> 	R.drawable.ssbu_character_roy
                51	-> 	R.drawable.ssbu_character_ryu
                52	-> 	R.drawable.ssbu_character_samus
                53	-> 	R.drawable.ssbu_character_sheik
                54	-> 	R.drawable.ssbu_character_shulk
                55	-> 	R.drawable.ssbu_character_snake
                56	-> 	R.drawable.ssbu_character_sonic
                57	-> 	R.drawable.ssbu_character_toon_link
                58	-> 	R.drawable.ssbu_character_villager
                59	-> 	R.drawable.ssbu_character_wario
                60	-> 	R.drawable.ssbu_character_wii_fit_trainer
                61	-> 	R.drawable.ssbu_character_wolf
                62	-> 	R.drawable.ssbu_character_yoshi
                63	-> 	R.drawable.ssbu_character_young_link
                64	-> 	R.drawable.ssbu_character_zelda
                65	-> 	R.drawable.ssbu_character_zero_suit_samus
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
                4 -> return "Cloud"
                5 -> return "Corrin"
                6 -> return "Daisy"
                7 -> return "Dark Pit"
                8 -> return "Diddy Kong"
                9 -> return "Donkey Kong"
                10 -> return "Dr. Mario"
                11 -> return "Duck Hunt"
                12 -> return "Falco"
                13 -> return "Fox"
                14 -> return "Ganondorf"
                15 -> return "Greninja"
                16 -> return "Ice Climbers"
                17 -> return "Ike"
                18 -> return "Inkling"
                19 -> return "Jigglypuff"
                20 -> return "King Dedede"
                21 -> return "Kirby"
                22 -> return "Link"
                23 -> return "Little Mac"
                24 -> return "Lucario"
                25 -> return "Lucas"
                26 -> return "Lucina"
                27 -> return "Luigi"
                28 -> return "Mario"
                29 -> return "Marth"
                30 -> return "Mega Man"
                31 -> return "Meta Knight"
                32 -> return "Mewtwo"
                33 -> return "Mii Brawler"
                34 -> return "Mii Gunner"
                35 -> return "Mii Swordfighter"
                36 -> return "Mr. Game & Watch"
                37 -> return "Ness"
                38 -> return "Olimar"
                39 -> return "Pac-Man"
                40 -> return "Palutena"
                41 -> return "Peach"
                42 -> return "Pichu"
                43 -> return "Pikachu"
                44 -> return "Pit"
                45 -> return "Pokémon Trainer"
                47 -> return "Ridley"
                46 -> return "Rob"
                48 -> return "Robin"
                49 -> return "Rosalina & Luma"
                50 -> return "Roy"
                51 -> return "Ryu"
                52 -> return "Samus"
                53 -> return "Sheik"
                54 -> return "Shulk"
                55 -> return "Snake"
                56 -> return "Sonic"
                57 -> return "Toon Link"
                58 -> return "Villager"
                59 -> return "Wario"
                60 -> return "Wii Fit Trainer"
                61 -> return "Wolf"
                62 -> return "Yoshi"
                63 -> return "Young Link"
                64 -> return "Zelda"
                65 -> return "Zero Suit Samus"
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
                "Cloud".toLowerCase() -> return 4
                "Corrin".toLowerCase() -> return 5
                "Daisy".toLowerCase() -> return 6
                "Dark Pit".toLowerCase() -> return 7
                "Diddy Kong".toLowerCase() -> return 8
                "Donkey Kong".toLowerCase() -> return 9
                "Dr. Mario".toLowerCase() -> return 10
                "Duck Hunt".toLowerCase() -> return 11
                "Falco".toLowerCase() -> return 12
                "Fox".toLowerCase() -> return 13
                "Ganondorf".toLowerCase() -> return 14
                "Greninja".toLowerCase() -> return 15
                "Ice Climbers".toLowerCase() -> return 16
                "Ike".toLowerCase() -> return 17
                "Inkling".toLowerCase() -> return 18
                "Jigglypuff".toLowerCase() -> return 19
                "King Dedede".toLowerCase() -> return 20
                "Kirby".toLowerCase() -> return 21
                "Link".toLowerCase() -> return 22
                "Little Mac".toLowerCase() -> return 23
                "Lucario".toLowerCase() -> return 24
                "Lucas".toLowerCase() -> return 25
                "Lucina".toLowerCase() -> return 26
                "Luigi".toLowerCase() -> return 27
                "Mario".toLowerCase() -> return 28
                "Marth".toLowerCase() -> return 29
                "Mega Man".toLowerCase() -> return 30
                "Meta Knight".toLowerCase() -> return 31
                "Mewtwo".toLowerCase() -> return 32
                "Mii Brawler".toLowerCase() -> return 33
                "Mii Gunner".toLowerCase() -> return 34
                "Mii Swordfighter".toLowerCase() -> return 35
                "Mr. Game & Watch".toLowerCase() -> return 36
                "Ness".toLowerCase() -> return 37
                "Olimar".toLowerCase() -> return 38
                "Pac-Man".toLowerCase() -> return 39
                "Palutena".toLowerCase() -> return 40
                "Peach".toLowerCase() -> return 41
                "Pichu".toLowerCase() -> return 42
                "Pikachu".toLowerCase() -> return 43
                "Pit".toLowerCase() -> return 44
                "Pokémon Trainer: Charizard".toLowerCase() -> return 45
                "Pokémon Trainer: Ivysaur".toLowerCase() -> return 46
                "Pokémon Trainer: Squirtle".toLowerCase() -> return 47
                "R.O.B.".toLowerCase() -> return 48
                "Ridley".toLowerCase() -> return 49
                "Robin".toLowerCase() -> return 50
                "Rosalina & Luma".toLowerCase() -> return 51
                "Roy".toLowerCase() -> return 52
                "Ryu".toLowerCase() -> return 53
                "Samus".toLowerCase() -> return 54
                "Sheik".toLowerCase() -> return 55
                "Shulk".toLowerCase() -> return 56
                "Snake".toLowerCase() -> return 57
                "Sonic".toLowerCase() -> return 58
                "Toon Link".toLowerCase() -> return 59
                "Villager".toLowerCase() -> return 60
                "Wario".toLowerCase() -> return 61
                "Wii Fit Trainer".toLowerCase() -> return 62
                "Wolf".toLowerCase() -> return 63
                "Yoshi".toLowerCase() -> return 64
                "Young Link".toLowerCase() -> return 65
                "Zelda".toLowerCase() -> return 66
                "Zero Suit Samus".toLowerCase() -> return 67
            }

            return -1
        }
    }
}