package com.stromberg.scott.seventenwouldstillsmash.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stromberg.scott.seventenwouldstillsmash.MainActivity
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.adapter.GamesListAdapter
import com.stromberg.scott.seventenwouldstillsmash.model.Game
import com.stromberg.scott.seventenwouldstillsmash.model.Player
import java.util.*

class CreatePlayerFragment : BaseFragment() {
    private var db = FirebaseDatabase.getInstance()
    private var games = ArrayList<Game>()
    private var gamesAdapter: GamesListAdapter? = null

    private var contentView: View? = null
    private var recyclerView: RecyclerView? = null
    private var progressBar: ProgressBar? = null
    private var deleteButton: Button? = null
    private var cancelButton: Button? = null
    private var saveButton: Button? = null
    private var nameEditText: EditText? = null

    private var editingPlayer: Player? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        contentView = View.inflate(activity, R.layout.create_player, null)

        if(arguments != null && arguments.containsKey("player")) {
            editingPlayer = arguments.getSerializable("player") as Player?
        }

        recyclerView = contentView!!.findViewById(R.id.create_player_recyclerview)
        recyclerView!!.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        progressBar = contentView!!.findViewById(R.id.progress)

        setupGamesAdapter(games)
        setupButtons()

        nameEditText = contentView!!.findViewById(R.id.create_player_name)
        nameEditText?.setText(editingPlayer?.name ?: "")

        return contentView
    }

    override fun onResume() {
        super.onResume()

        if(editingPlayer != null) {
            getGames()
        }
    }

    private fun setupGamesAdapter(games: List<Game>) {
        gamesAdapter = GamesListAdapter(games)

        gamesAdapter!!.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            (activity as MainActivity).editGame(games[position])
        }

        recyclerView!!.adapter = gamesAdapter
    }

    private fun setupButtons() {
        cancelButton = contentView!!.findViewById(R.id.create_player_cancel_button)
        saveButton = contentView!!.findViewById(R.id.create_player_create_button)
        deleteButton = contentView!!.findViewById(R.id.create_player_delete_button)

        cancelButton?.setOnClickListener({ activity.onBackPressed() })

        if(editingPlayer == null) {
            deleteButton?.visibility = View.GONE

            saveButton?.text = "Create Player"
            saveButton?.setOnClickListener({ createPlayer() })

            contentView?.findViewById<View>(R.id.create_player_navigation)?.visibility = View.INVISIBLE
        }
        else {
            deleteButton?.visibility = View.VISIBLE
            deleteButton?.setOnClickListener({ deletePlayer() })

            saveButton?.text = "Save Player"
            saveButton?.setOnClickListener({ updatePlayer() })
        }
    }

    private fun getGames() {
        setContentShown(false)

        db.reference
            .child("games")
            .orderByChild("date")
            .addListenerForSingleValueEvent( object : ValueEventListener {
                override fun onCancelled(error: DatabaseError?) { }

                override fun onDataChange(snapshot: DataSnapshot?) {
                    snapshot?.children?.reversed()?.forEach {
                        var game: Game = it.getValue(Game::class.java)!!

                        if(game.players.any({ it.player!!.id == editingPlayer!!.id })) {
                            game.id = it.key
                            games.add(game)
                        }
                    }

                    gamesAdapter!!.loadMoreComplete()

                    recyclerView?.adapter?.notifyDataSetChanged()

                    setContentShown(true)
                }
            })
    }

    private fun createPlayer() {
        var playerName = nameEditText!!.text.toString()
        if (playerName.isNotEmpty()) {
            var player = Player()
            player.name = playerName
            player.id = Calendar.getInstance().timeInMillis.toString()

            db.reference
                .child("players")
                .child(player.id)
                .setValue(player)
                .addOnCompleteListener( {
                    activity.onBackPressed()
                })
        }
        else {
            showDialog("Set a player name.")
        }
    }
    private fun updatePlayer() {
        var playerName = nameEditText!!.text.toString()
        if (playerName.isNotEmpty()) {
            editingPlayer!!.name = playerName

            db.reference
                .child("players")
                .child(editingPlayer!!.id)
                .setValue(editingPlayer)
                .addOnCompleteListener( {
                    activity.onBackPressed()
                })
        }
    }

    private fun deletePlayer() {
        db.reference
            .child("players")
            .child(editingPlayer!!.id)
            .removeValue()
            .addOnCompleteListener( {
                activity.onBackPressed()
            })
    }

    override fun setContentShown(shown: Boolean) {
        progressBar!!.visibility = if(shown) View.GONE else View.VISIBLE
        recyclerView!!.visibility = if(shown) View.VISIBLE else View.GONE
    }

    private fun showDialog(message: String) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(null)
        builder.setMessage(message)
        builder.setPositiveButton(android.R.string.ok, { dialog, _ -> dialog.dismiss() })
        builder.show()
    }
}