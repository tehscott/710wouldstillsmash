package com.stromberg.scott.seventenwouldstillsmash.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.widget.Space
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
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

        var mostPlayerWins = 0
        var mostCharacterWins = 0
        var mostWinsPlayer: Player? = null
        var mostWinsCharacterId = ""

        var mostPlayerLosses = 0
        var mostCharacterLosses = 0
        var mostLossesPlayer: Player? = null
        var mostLossesCharacterId = ""

        val hashMapType = object : TypeToken<HashMap<String, CharacterStats>>() {}.type
        val playerStats: HashMap<String, CharacterStats> = Gson().fromJson<HashMap<String, CharacterStats>>(prefs.getString("PlayerStatsJson", null), hashMapType)

        for (player in players) {
            val thisPlayerStats = playerStats.filter { it.key.contains(player.id!!, true) }

            val wins = thisPlayerStats.values.sumBy { it.wins }
            val losses = thisPlayerStats.values.sumBy { it.losses }

            if(wins > mostPlayerWins) {
                mostPlayerWins = wins
                mostWinsPlayer = player
            }

            if(losses > mostPlayerLosses) {
                mostPlayerLosses = losses
                mostLossesPlayer = player
            }
        }

//        val key = playerId + "_" + characterId
        for (characterId in (0..63)) {
            var characterStats = playerStats.filter { it.key.split("_")[1] == characterId.toString() }

            val wins = characterStats.values.sumBy { it.wins }
            val losses = characterStats.values.sumBy { it.losses }

            if(wins > mostCharacterWins) {
                mostCharacterWins = wins
                mostWinsCharacterId = characterId.toString()
            }

            if(losses > mostCharacterLosses) {
                mostCharacterLosses = losses
                mostLossesCharacterId = characterId.toString()
            }
        }

        Log.d("stats", "wins: " + mostPlayerWins + " by " + mostWinsPlayer!!.name)
        Log.d("stats", "losses: " + mostPlayerLosses + " by " + mostLossesPlayer!!.name)
        Log.d("stats", "wins: $mostCharacterWins by $mostWinsCharacterId")
        Log.d("stats", "losses: $mostCharacterLosses by $mostLossesCharacterId")

        val mostWinsParentView: LinearLayout = layoutInflater.inflate(R.layout.statistics_parent_list_item, null) as LinearLayout
        mostWinsParentView.layoutParams = createLinearLayoutParams()
        mostWinsParentView.findViewById<TextView>(R.id.statistics_parent_title).text = "Most Wins"

        val mostWinsChildView = layoutInflater.inflate(R.layout.statistics_child_list_item, mostWinsParentView.findViewById(R.id.statistics_parent_list))
        mostWinsChildView.findViewById<TextView>(R.id.statistics_child_player_stat).text = mostWinsPlayer.name + " ($mostPlayerWins)"
        mostWinsChildView.findViewById<ImageView>(R.id.statistics_child_character_image).setImageResource(CharacterHelper.getImage(mostWinsCharacterId.toInt()))
        mostWinsChildView.findViewById<TextView>(R.id.statistics_child_character_stat).text = CharacterHelper.getName(mostWinsCharacterId.toInt()) + " ($mostCharacterWins)"
        list!!.addView(mostWinsParentView)

        var space = createSpace()

        list!!.addView(space)

        val mostLossesParentView: LinearLayout = layoutInflater.inflate(R.layout.statistics_parent_list_item, null) as LinearLayout
        mostLossesParentView.layoutParams = createLinearLayoutParams()
        mostLossesParentView.findViewById<TextView>(R.id.statistics_parent_title).text = "Most Losses"

        val mostLossesChildView = layoutInflater.inflate(R.layout.statistics_child_list_item, mostLossesParentView.findViewById(R.id.statistics_parent_list))
        mostLossesChildView.findViewById<TextView>(R.id.statistics_child_player_stat).text = mostLossesPlayer.name + " ($mostPlayerLosses)"
        mostLossesChildView.findViewById<ImageView>(R.id.statistics_child_character_image).setImageResource(CharacterHelper.getImage(mostLossesCharacterId.toInt()))
        mostLossesChildView.findViewById<TextView>(R.id.statistics_child_character_stat).text = CharacterHelper.getName(mostLossesCharacterId.toInt()) + " ($mostCharacterLosses)"
        list!!.addView(mostLossesParentView)

        // most wins

        // most losses
        // longest streak
        // most games
        // least games
        // best win rate
        // worst win rate

        setContentShown(true)
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