package com.stromberg.scott.seventenwouldstillsmash.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.widget.Space
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.ajguan.library.EasyRefreshLayout
import com.ajguan.library.LoadModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.model.CharacterStats
import com.stromberg.scott.seventenwouldstillsmash.model.Game
import com.stromberg.scott.seventenwouldstillsmash.model.GameType
import com.stromberg.scott.seventenwouldstillsmash.model.Player
import com.stromberg.scott.seventenwouldstillsmash.util.CharacterHelper
import java.util.*

class StatisticsFragment : BaseFragment() {
    private var db = FirebaseDatabase.getInstance()
    private var games = ArrayList<Game>()
    private var players = ArrayList<Player>()

    private var contentView: View? = null
    private var pullToRefreshView: EasyRefreshLayout? = null
    private var list: LinearLayout? = null
    private var progressBar: ProgressBar? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        contentView = View.inflate(activity, R.layout.fragment_statistics, null)

        pullToRefreshView = contentView!!.findViewById(R.id.statistics_pull_to_refresh)
        list = contentView!!.findViewById(R.id.statistics_list)

        progressBar = contentView!!.findViewById(R.id.progress)

        pullToRefreshView!!.loadMoreModel = LoadModel.NONE
        pullToRefreshView!!.addEasyEvent(object: EasyRefreshLayout.EasyEvent {
            override fun onRefreshing() {
                getGames()
            }

            override fun onLoadMore() {}
        })

        getGames()

        return contentView
    }

    private fun getStatistics() {
        val prefs = activity.getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE)
        val hashMapType = object : TypeToken<HashMap<String, CharacterStats>>() {}.type
        val playerStats: HashMap<String, CharacterStats> = Gson().fromJson<HashMap<String, CharacterStats>>(prefs.getString("PlayerStatsJson", null), hashMapType)
        val gamesForPlayers = HashMap<Player, List<Game>>()
        val gamesForCharacters = HashMap<Int, List<Game>>()

        players.forEach {
            val playerId = it.id

            val gamesForPlayer = games.filter {
                it.players.any { it.player!!.id == playerId }
            }

            gamesForPlayers.put(it, gamesForPlayer)
        }

        for(id in 0..63) {
            val gamesForCharacter = games.filter {
                it.players.any { it.characterId == id }
            }

            if(gamesForCharacter.isNotEmpty()) {
                gamesForCharacters.put(id, gamesForCharacter)
            }
        }

        addMostWinsAndLosses(playerStats, GameType.ROYALE)
        list!!.addView(createSpace())
        addMostWinsAndLosses(playerStats, GameType.SUDDEN_DEATH)
        list!!.addView(createSpace())
        addLongestStreak(gamesForPlayers, gamesForCharacters, GameType.ROYALE)
        list!!.addView(createSpace())
        addLongestStreak(gamesForPlayers, gamesForCharacters, GameType.SUDDEN_DEATH)
        list!!.addView(createSpace())
        addMostAndLeastGames(gamesForPlayers, gamesForCharacters, GameType.ROYALE)
        list!!.addView(createSpace())

        // best win rate
        // worst win rate

        setContentShown(true)
    }

    private fun addMostAndLeastGames(gamesForPlayers: HashMap<Player, List<Game>>, gamesForCharacters: HashMap<Int, List<Game>>, gameType: GameType) {
        var mostGamesPlayer: Player? = null
        var mostPlayerGames = 0
        var mostGamesCharacterId: Int? = null
        var mostCharacterGames = 0

        gamesForPlayers.forEach {
            var player = it.key
            var filteredGames = it.value.filter { it.gameType.equals(gameType.toString()) }

            if(filteredGames.size > mostPlayerGames) {
                mostPlayerGames = filteredGames.size
                mostGamesPlayer = player
            }
        }

        gamesForCharacters.forEach {
            var characterId = it.key
            var filteredGames = it.value.filter { it.gameType.equals(gameType.toString()) }

            if(filteredGames.size > mostCharacterGames) {
                mostCharacterGames = filteredGames.size
                mostGamesCharacterId = characterId
            }
        }

        var leastGamesPlayer: Player? = null
        var leastPlayerGames = Int.MAX_VALUE
        var leastGamesCharacterId: Int? = null
        var leastCharacterGames = Int.MAX_VALUE

        gamesForPlayers.forEach {
            var player = it.key
            var filteredGames = it.value.filter { it.gameType.equals(gameType.toString()) }

            if(filteredGames.size < leastPlayerGames) {
                leastPlayerGames = filteredGames.size
                leastGamesPlayer = player
            }
        }

        gamesForCharacters.forEach {
            var characterId = it.key
            var filteredGames = it.value.filter { it.gameType.equals(gameType.toString()) }

            if(filteredGames.size > 0 && filteredGames.size < leastCharacterGames) {
                leastCharacterGames = filteredGames.size
                leastGamesCharacterId = characterId
            }
        }

        val mostGamesParent: LinearLayout = layoutInflater.inflate(R.layout.statistics_parent_list_item, null) as LinearLayout
        mostGamesParent.layoutParams = createLinearLayoutParams()
        mostGamesParent.findViewById<TextView>(R.id.statistics_parent_title).text = "Most " + gameType.prettyName() + " Games"

        val mostGamesChild = layoutInflater.inflate(R.layout.statistics_child_list_item, mostGamesParent.findViewById(R.id.statistics_parent_list))
        mostGamesChild.findViewById<TextView>(R.id.statistics_child_player_stat).text = mostGamesPlayer!!.name + " (${mostPlayerGames})"
        mostGamesChild.findViewById<ImageView>(R.id.statistics_child_character_image).setImageResource(CharacterHelper.getImage(mostGamesCharacterId!!))
        mostGamesChild.findViewById<TextView>(R.id.statistics_child_character_stat).text = CharacterHelper.getName(mostGamesCharacterId!!) + " (${mostCharacterGames})"
        list!!.addView(mostGamesParent)

        list!!.addView(createSpace())

        val leastGamesParent: LinearLayout = layoutInflater.inflate(R.layout.statistics_parent_list_item, null) as LinearLayout
        leastGamesParent.layoutParams = createLinearLayoutParams()
        leastGamesParent.findViewById<TextView>(R.id.statistics_parent_title).text = "Least " + gameType.prettyName() + " Games"

        val leastGamesChild = layoutInflater.inflate(R.layout.statistics_child_list_item, leastGamesParent.findViewById(R.id.statistics_parent_list))
        leastGamesChild.findViewById<TextView>(R.id.statistics_child_player_stat).text = leastGamesPlayer!!.name + " (${leastPlayerGames})"
        leastGamesChild.findViewById<ImageView>(R.id.statistics_child_character_image).setImageResource(CharacterHelper.getImage(leastGamesCharacterId!!))
        leastGamesChild.findViewById<TextView>(R.id.statistics_child_character_stat).text = CharacterHelper.getName(leastGamesCharacterId!!) + " (${leastCharacterGames})"
        list!!.addView(leastGamesParent)
    }

    private fun addLongestStreak(gamesForPlayers: HashMap<Player, List<Game>>, gamesForCharacters: HashMap<Int, List<Game>>, gameType: GameType) {
        var mostWinsPlayer: Player? = null
        var mostPlayerWins = 0
        var mostLossesPlayer: Player? = null
        var mostPlayerLosses = 0

        gamesForPlayers.forEach {
            val player = it.key
            var winCount = 0
            var longestWinStreak = 0
            var lossCount = 0
            var longestLossStreak = 0

            Log.d("streak", "starting " + player.name)

            var sortedGames = it.value.filter { it.gameType.equals(gameType.toString(), true) }.sortedBy { it.date }
            sortedGames.forEach {
                if (it.players.first { it.player!!.id!! == player.id }.winner) {
                    winCount++
                    lossCount = 0
                } else {
                    lossCount++
                    winCount = 0
                }

                if (winCount > longestWinStreak) {
                    longestWinStreak = winCount
                }

                if (longestWinStreak > mostPlayerWins) {
                    mostWinsPlayer = player
                    mostPlayerWins = winCount
//                    Log.d("streak", "mostPlayerWins: $mostPlayerWins by " + mostWinsPlayer!!.name)
                }

                if (lossCount > longestLossStreak) {
                    longestLossStreak = lossCount
//                    Log.d("streak", "longestLossStreak: $longestLossStreak by " + player.name)
                }

                if (longestLossStreak > mostPlayerLosses) {
                    mostLossesPlayer = player
                    mostPlayerLosses = lossCount
//                    Log.d("streak", "mostPlayerLosses: $mostPlayerLosses by " + mostLossesPlayer!!.name)
//                    Log.d("streak", "last game " + it.date + " as " + CharacterHelper.getName(it.players.first { it.player!!.id!! == player.id }.characterId))
                }
            }
        }

        var mostWinsCharacterId: Int? = null
        var mostCharacterWins = 0
        var mostLossesCharacterId: Int? = null
        var mostCharacterLosses = 0

        gamesForCharacters.forEach {
            val characterId = it.key
            var winCount = 0
            var longestWinStreak = 0
            var lossCount = 0
            var longestLossStreak = 0

            var sortedGames = it.value.filter { it.gameType.equals(gameType.toString(), true) }.sortedBy { it.date }
            sortedGames.forEach {
                if (it.players.first { it.characterId == characterId }.winner) {
                    winCount++
                    lossCount = 0
                } else {
                    lossCount++
                    winCount = 0
                }

                if (winCount > longestWinStreak) {
                    longestWinStreak = winCount
                }

                if (longestWinStreak > mostCharacterWins) {
                    mostWinsCharacterId = characterId
                    mostCharacterWins = winCount
//                    Log.d("streak", "mostCharacterWins: $mostCharacterWins by " + CharacterHelper.getName(mostWinsCharacterId!!))
                }

                if (lossCount > longestLossStreak) {
                    longestLossStreak = lossCount
                }

                if (longestLossStreak > mostCharacterLosses) {
                    mostLossesCharacterId = characterId
                    mostCharacterLosses = lossCount
//                    Log.d("streak", "mostCharacterLosses: $mostCharacterLosses by " + CharacterHelper.getName(mostLossesCharacterId!!))
                }
            }
        }

        val mostWinsParent: LinearLayout = layoutInflater.inflate(R.layout.statistics_parent_list_item, null) as LinearLayout
        mostWinsParent.layoutParams = createLinearLayoutParams()
        mostWinsParent.findViewById<TextView>(R.id.statistics_parent_title).text = "Longest " + gameType.prettyName() + " Win Streak"

        val mostWinsChild = layoutInflater.inflate(R.layout.statistics_child_list_item, mostWinsParent.findViewById(R.id.statistics_parent_list))
        mostWinsChild.findViewById<TextView>(R.id.statistics_child_player_stat).text = mostWinsPlayer!!.name + " ($mostPlayerWins)"
        mostWinsChild.findViewById<ImageView>(R.id.statistics_child_character_image).setImageResource(CharacterHelper.getImage(mostWinsCharacterId!!))
        mostWinsChild.findViewById<TextView>(R.id.statistics_child_character_stat).text = CharacterHelper.getName(mostWinsCharacterId!!) + " ($mostPlayerWins)"
        list!!.addView(mostWinsParent)

        list!!.addView(createSpace())

        val mostLossesParent: LinearLayout = layoutInflater.inflate(R.layout.statistics_parent_list_item, null) as LinearLayout
        mostLossesParent.layoutParams = createLinearLayoutParams()
        mostLossesParent.findViewById<TextView>(R.id.statistics_parent_title).text = "Longest " + gameType.prettyName() + " Loss Streak"

        val mostLossesChild = layoutInflater.inflate(R.layout.statistics_child_list_item, mostLossesParent.findViewById(R.id.statistics_parent_list))
        mostLossesChild.findViewById<TextView>(R.id.statistics_child_player_stat).text = mostLossesPlayer!!.name + " ($mostPlayerLosses)"
        mostLossesChild.findViewById<ImageView>(R.id.statistics_child_character_image).setImageResource(CharacterHelper.getImage(mostLossesCharacterId!!))
        mostLossesChild.findViewById<TextView>(R.id.statistics_child_character_stat).text = CharacterHelper.getName(mostLossesCharacterId!!) + " ($mostCharacterLosses)"
        list!!.addView(mostLossesParent)
    }

    private fun addMostWinsAndLosses(playerStats: HashMap<String, CharacterStats>, gameType: GameType) {
        var mostPlayerWins = 0
        var mostCharacterWins = 0
        var mostWinsPlayer: Player? = null
        var mostWinsCharacterId = ""

        var mostPlayerLosses = 0
        var mostCharacterLosses = 0
        var mostLossesPlayer: Player? = null
        var mostLossesCharacterId = ""


        for (player in players) {
            val thisPlayerStats = playerStats.filter { it.key.contains(player.id!!, true) }

            var wins = 0
            var losses = 0

            if(gameType == GameType.ROYALE) {
                wins = thisPlayerStats.values.sumBy { it.royaleWins }
                losses = thisPlayerStats.values.sumBy { it.royaleLosses }
            }
            else {
                wins = thisPlayerStats.values.sumBy { it.suddenDeathWins }
                losses = thisPlayerStats.values.sumBy { it.suddenDeathLosses }
            }

            if (wins > mostPlayerWins) {
                mostPlayerWins = wins
                mostWinsPlayer = player
            }

            if (losses > mostPlayerLosses) {
                mostPlayerLosses = losses
                mostLossesPlayer = player
            }
        }

        for (characterId in (0..63)) {
            var characterStats = playerStats.filter { it.key.split("_")[1] == characterId.toString() }

            var wins = 0
            var losses = 0

            if(gameType == GameType.ROYALE) {
                wins = characterStats.values.sumBy { it.royaleWins }
                losses = characterStats.values.sumBy { it.royaleLosses }
            }
            else {
                wins = characterStats.values.sumBy { it.suddenDeathWins }
                losses = characterStats.values.sumBy { it.suddenDeathLosses }
            }

            if (wins > mostCharacterWins) {
                mostCharacterWins = wins
                mostWinsCharacterId = characterId.toString()
            }

            if (losses > mostCharacterLosses) {
                mostCharacterLosses = losses
                mostLossesCharacterId = characterId.toString()
            }
        }

        val mostWinsParentView: LinearLayout = layoutInflater.inflate(R.layout.statistics_parent_list_item, null) as LinearLayout
        mostWinsParentView.layoutParams = createLinearLayoutParams()
        mostWinsParentView.findViewById<TextView>(R.id.statistics_parent_title).text = "Most " + gameType.prettyName() + " Wins"

        val mostWinsChildView = layoutInflater.inflate(R.layout.statistics_child_list_item, mostWinsParentView.findViewById(R.id.statistics_parent_list))
        mostWinsChildView.findViewById<TextView>(R.id.statistics_child_player_stat).text = mostWinsPlayer!!.name + " ($mostPlayerWins)"
        mostWinsChildView.findViewById<ImageView>(R.id.statistics_child_character_image).setImageResource(CharacterHelper.getImage(mostWinsCharacterId.toInt()))
        mostWinsChildView.findViewById<TextView>(R.id.statistics_child_character_stat).text = CharacterHelper.getName(mostWinsCharacterId.toInt()) + " ($mostCharacterWins)"
        list!!.addView(mostWinsParentView)

        list!!.addView(createSpace())

        val mostLossesParentView: LinearLayout = layoutInflater.inflate(R.layout.statistics_parent_list_item, null) as LinearLayout
        mostLossesParentView.layoutParams = createLinearLayoutParams()
        mostLossesParentView.findViewById<TextView>(R.id.statistics_parent_title).text = "Most " + gameType.prettyName() + " Losses"

        val mostLossesChildView = layoutInflater.inflate(R.layout.statistics_child_list_item, mostLossesParentView.findViewById(R.id.statistics_parent_list))
        mostLossesChildView.findViewById<TextView>(R.id.statistics_child_player_stat).text = mostLossesPlayer!!.name + " ($mostPlayerLosses)"
        mostLossesChildView.findViewById<ImageView>(R.id.statistics_child_character_image).setImageResource(CharacterHelper.getImage(mostLossesCharacterId.toInt()))
        mostLossesChildView.findViewById<TextView>(R.id.statistics_child_character_stat).text = CharacterHelper.getName(mostLossesCharacterId.toInt()) + " ($mostCharacterLosses)"
        list!!.addView(mostLossesParentView)
    }

    private fun createLinearLayoutParams(): LinearLayout.LayoutParams {
        var lp: LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(activity.resources.getDimensionPixelSize(R.dimen.space_8dp), activity.resources.getDimensionPixelSize(R.dimen.space_4dp), activity.resources.getDimensionPixelSize(R.dimen.space_8dp), 0)

        return lp
    }

    private fun createSpace(): Space {
        var space = Space(activity)
        space.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, activity.resources.getDimensionPixelSize(R.dimen.space_8dp))

        return space
    }

    private fun getGames() {
        setContentShown(false)

        db.reference
            .child("games")
            .orderByKey()
            .addListenerForSingleValueEvent( object : ValueEventListener {
                override fun onCancelled(error: DatabaseError?) { }

                override fun onDataChange(snapshot: DataSnapshot?) {
                    games.clear()

                    snapshot?.children?.reversed()?.forEach {
                        var game: Game = it.getValue(Game::class.java)!!
                        game.id = it.key
                        games.add(game)
                    }

                    getPlayers()
                }
            })
    }

    private fun getPlayers() {
        db.reference
            .child("players")
            .orderByKey()
            .addListenerForSingleValueEvent( object : ValueEventListener {
                override fun onCancelled(error: DatabaseError?) { }

                override fun onDataChange(snapshot: DataSnapshot?) {
                    players.clear()

                    snapshot?.children?.reversed()?.forEach {
                        val player: Player = it.getValue(Player::class.java)!!
                        player.id = it.key
                        players.add(player)
                    }

                    players.sortBy { it.name }

                    getStatistics()
                }
            })
    }

    override fun setContentShown(shown: Boolean) {
        progressBar!!.visibility = if(shown) View.GONE else View.VISIBLE
        pullToRefreshView!!.visibility = if(shown) View.VISIBLE else View.GONE
    }
}