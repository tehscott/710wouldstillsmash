package com.stromberg.scott.seventenwouldstillsmash.fragment

import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import com.github.clans.fab.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.adapter.GamesListAdapter
import com.stromberg.scott.seventenwouldstillsmash.model.*
import com.stromberg.scott.seventenwouldstillsmash.util.CharacterHelper
import com.stromberg.scott.seventenwouldstillsmash.util.random
import java.util.*
import kotlin.collections.ArrayList

class GamesFragment : BaseFragment() {
    private var db = FirebaseFirestore.getInstance()

    private var contentView: View? = null
    private var recyclerView: RecyclerView? = null
    private var progressBar: ProgressBar? = null
    private var searchView: SearchView? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        contentView = View.inflate(activity, R.layout.fragment_games, null)

        recyclerView = contentView!!.findViewById<RecyclerView>(R.id.games_recyclerview)
        recyclerView!!.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        progressBar = contentView!!.findViewById<ProgressBar>(R.id.progress)
        searchView = contentView!!.findViewById<SearchView>(R.id.games_search_view)

        return contentView
    }

    override fun onResume() {
        super.onResume()

//        makeGames()
        getGames()
    }

    private fun getGames() {
        setContentShown(false)

        var gamesProcessed = 0
        var games = ArrayList<Game>()

        db.collection("games")
                .orderBy("date")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        var totalGames = task.result.size()

                        for (document in task.result) {
                            var game = Game()
                            game.id = document.id
                            game.date = Date(document.getLong("date"))
                            game.gameType = document.getString("gameType")
                            game.players = ArrayList<GamePlayer>()

                            var gamePlayers = ArrayList<GamePlayer>()
                            var gamePlayersList = document.get("gamePlayers") as ArrayList<HashMap<String, Any>>
                            for (gPlayer in gamePlayersList) {
                                var gamePlayer = GamePlayer()

                                var character = Character()
                                character.id = gPlayer.get("characterId").toString().toInt()
                                character.name = CharacterHelper.getName(character.id)

                                gamePlayer.character = character
                                gamePlayer.winner = gPlayer.get("isWinner").toString().toBoolean()

                                var player = Player()
                                player.id = gPlayer.get("playerId").toString()
                                player.name = gPlayer.get("playerName").toString()

                                gamePlayer.player = player

                                gamePlayers.add(gamePlayer)
                            }

                            game.players = gamePlayers
                            games.add(game)

                            gamesProcessed++

                            if (gamesProcessed == totalGames) {
                                recyclerView!!.adapter = GamesListAdapter(games)
                                recyclerView?.adapter?.notifyDataSetChanged()
                                setContentShown(true)
                            }
                        }
                    } else {
                        Snackbar.make(contentView!!, "Failed to load game data", Snackbar.LENGTH_SHORT).show()
                    }
                }
    }

    private fun makeGames() {
        setContentShown(false)

        var gamesProcessed = 0

        for (i in 0..19) {
            var cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)

            var game = HashMap<String, Any>()
            game.put("gameType", if ((0..1).random() == 0) GameType.ROYALE.toString() else GameType.SUDDEN_DEATH.toString())
            game.put("date", cal.timeInMillis)

            var winner = (0..3).random()

            var gamePlayers = ArrayList<Any>()

            var dustin = HashMap<String, Any>()
            dustin.put("playerId", "Afazw9wal1TEkOZ6jnxF")
            dustin.put("playerName", "Dustin")
            dustin.put("characterId", (0..57).random())
            dustin.put("isWinner", winner == 0)

            var chad = HashMap<String, Any>()
            chad.put("playerId", "e8F9BY1VHyrALelHu5ah")
            chad.put("playerName", "Chad")
            chad.put("characterId", (0..57).random())
            chad.put("isWinner", winner == 1)

            var scott = HashMap<String, Any>()
            scott.put("playerId", "eNBKVdu2j7H2p0iE2989")
            scott.put("playerName", "Scott")
            scott.put("characterId", (0..57).random())
            scott.put("isWinner", winner == 2)

            var josh = HashMap<String, Any>()
            josh.put("playerId", "icHPaoUTxVhQmrSDzleS")
            josh.put("playerName", "Josh")
            josh.put("characterId", (0..57).random())
            josh.put("isWinner", winner == 3)

            gamePlayers.add(dustin)
            gamePlayers.add(chad)
            gamePlayers.add(scott)
            gamePlayers.add(josh)

            game.put("gamePlayers", gamePlayers)

            db.collection("games")
                    .add(game)
                    .addOnSuccessListener { documentReference ->
                        run {
                            gamesProcessed++

                            if(gamesProcessed == 20) {
                                setContentShown(true)
                            }
                        }
                    }
        }
    }

    override fun getFabButtons(context: Context): List<FloatingActionButton> {
        var fabs = ArrayList<FloatingActionButton>()

        val royaleDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_royale)!!
        royaleDrawable.mutate()
        DrawableCompat.setTint(royaleDrawable, ContextCompat.getColor(context, android.R.color.white));

        val suddenDeathDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_sudden_death)!!
        suddenDeathDrawable.mutate()
        DrawableCompat.setTint(suddenDeathDrawable, ContextCompat.getColor(context, android.R.color.white));

        var royaleFab = FloatingActionButton(context)
        royaleFab.setImageDrawable(royaleDrawable)
        royaleFab.buttonSize = FloatingActionButton.SIZE_MINI
        royaleFab.labelText = "Royale"
        royaleFab.colorNormal = context.resources.getColor(R.color.primary)
        royaleFab.colorPressed = context.resources.getColor(R.color.primary_light)
        royaleFab.colorRipple = context.resources.getColor(R.color.text_primary)
        royaleFab.setOnClickListener({ run { Toast.makeText(context, "ROYALE", Toast.LENGTH_SHORT).show() } })

        var suddenDeathFab = FloatingActionButton(context)
        suddenDeathFab.setImageDrawable(suddenDeathDrawable)
        suddenDeathFab.buttonSize = FloatingActionButton.SIZE_MINI
        suddenDeathFab.labelText = "Sudden Death"
        suddenDeathFab.colorNormal = context.resources.getColor(R.color.primary)
        suddenDeathFab.colorPressed = context.resources.getColor(R.color.primary_light)
        suddenDeathFab.colorRipple = context.resources.getColor(R.color.text_primary)
        suddenDeathFab.setOnClickListener({ run { Toast.makeText(context, "SUDDEN DEATH", Toast.LENGTH_SHORT).show() } })

        fabs.add(royaleFab)
        fabs.add(suddenDeathFab)

        return fabs
    }

    override fun setContentShown(shown: Boolean) {
        progressBar!!.visibility = if(shown) View.GONE else View.VISIBLE
        recyclerView!!.visibility = if(shown) View.VISIBLE else View.GONE
        searchView!!.visibility = if(shown) View.VISIBLE else View.GONE
    }

    override fun addFabClicked() {

    }

    override fun hasFab(): Boolean {
        return true
    }
}