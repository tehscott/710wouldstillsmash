package com.stromberg.scott.seventenwouldstillsmash.fragment

import android.content.Context
import android.content.res.Resources
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.ajguan.library.EasyRefreshLayout
import com.ajguan.library.LoadModel
import com.chad.library.adapter.base.BaseQuickAdapter
import com.github.clans.fab.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.adapter.PlayersListAdapter
import com.stromberg.scott.seventenwouldstillsmash.model.Player
import com.stromberg.scott.seventenwouldstillsmash.util.hideKeyboard
import uk.co.chrisjenx.calligraphy.TypefaceUtils
import java.util.*

class PlayersFragment : BaseFragment() {
    private var db = FirebaseDatabase.getInstance()

    private var contentView: View? = null
    private var recyclerView: RecyclerView? = null
    private var pullToRefreshView: EasyRefreshLayout? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        contentView = View.inflate(activity as Context?, R.layout.fragment_players, null)

        pullToRefreshView = contentView!!.findViewById(R.id.players_pull_to_refresh)
        recyclerView = contentView!!.findViewById<RecyclerView>(R.id.players_recyclerview)
        recyclerView!!.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        pullToRefreshView!!.loadMoreModel = LoadModel.NONE
        pullToRefreshView!!.addEasyEvent(object: EasyRefreshLayout.EasyEvent {
            override fun onRefreshing() {
                getPlayers()
            }

            override fun onLoadMore() {}
        })

        return contentView
    }

    override fun onResume() {
        super.onResume()

        getPlayers()
    }

    private fun getPlayers() {
        setContentShown(false)

        db.reference
            .child("players")
            .orderByKey()
            .addListenerForSingleValueEvent( object : ValueEventListener {
                override fun onCancelled(error: DatabaseError?) { }

                override fun onDataChange(snapshot: DataSnapshot?) {
                    val players = ArrayList<Player>()

                    snapshot?.children?.reversed()?.forEach {
                        val player: Player = it.getValue(Player::class.java)!!
                        player.id = it.key
                        players.add(player)
                    }

                    players.sortBy { it.name }

                    val playerNameWidth = getLongestNameLength(players)

                    val adapter = PlayersListAdapter(players, playerNameWidth)
                    recyclerView!!.adapter = adapter

                    adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
                        editPlayer(players[position])
                    }

                    adapter.setEnableLoadMore(false)

                    recyclerView?.adapter?.notifyDataSetChanged()

                    pullToRefreshView!!.refreshComplete()
                    setContentShown(true)
                }
            })
    }

    override fun setContentShown(shown: Boolean) {
        pullToRefreshView?.isRefreshing = !shown
    }

    fun getLongestNameLength(players: ArrayList<Player>): Int {
        val paint = Paint()
        val bounds = Rect()

        var longestLength = 0

        paint.typeface = TypefaceUtils.load(resources.assets, "Quicksand-Bold.ttf")
        paint.textSize = resources.getDimension(R.dimen.player_list_player_name)

        players.forEach({player -> run {
            paint.getTextBounds(player.name, 0, player.name!!.length, bounds)

            if(bounds.width() > longestLength) {
                longestLength = bounds.width()
            }
        }})

        Log.d("name", longestLength.toString())

        val oneThirdDisplayWidth = (Resources.getSystem().displayMetrics.widthPixels / 3)
        if(longestLength > oneThirdDisplayWidth) {
            return oneThirdDisplayWidth
        }
        else {
            return (longestLength * 1.15).toInt()
        }
    }

    override fun addFabClicked() {
        var builder = AlertDialog.Builder(activity)
        builder.setTitle("Add Player")

        val inputLayout = LayoutInflater.from(activity).inflate(R.layout.add_player_dialog, null)

        builder.setView(inputLayout)
        builder.setPositiveButton("Add") { dialog, _ ->
            run {
                var playerName = inputLayout.findViewById<EditText>(R.id.add_player_dialog_player_name).text.toString()
                if (playerName.isNotEmpty()) {
                    dialog.dismiss()
                    addPlayer(playerName)
                }
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.setCancelable(true)
        builder.show()
    }

    private fun editPlayer(player: Player) {
        var builder = AlertDialog.Builder(activity)
        builder.setTitle("Edit " + player.name)

        val inputLayout = LayoutInflater.from(activity).inflate(R.layout.add_player_dialog, null)
        inputLayout.findViewById<EditText>(R.id.add_player_dialog_player_name).setText(player.name)

        builder.setView(inputLayout)
        builder.setPositiveButton("Save") { dialog, _ ->
            run {
                var playerName = inputLayout.findViewById<EditText>(R.id.add_player_dialog_player_name).text.toString()
                if (playerName.isNotEmpty()) {
                    dialog.dismiss()

                    player.name = playerName

                    db.reference
                        .child("players")
                        .child(player.id)
                        .setValue(player)
                        .addOnCompleteListener( {
                            getPlayers()
                        })
                }
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.setNeutralButton("Delete Player") { dialog, _ -> run {
            deletePlayer(player)
            dialog.cancel()
        } }
        builder.setCancelable(true)
        builder.show()
    }

    private fun deletePlayer(player: Player) {
        db.reference
                .child("players")
                .child(player.id)
                .removeValue()
                .addOnCompleteListener( {
                    getPlayers()
                })
    }

    private fun addPlayer(playerName: String) {
        this.contentView?.hideKeyboard()

        setContentShown(false)

        var player = Player()
        player.id = Calendar.getInstance().timeInMillis.toString()
        player.name = playerName
        db.reference
            .child("players")
            .child(player.id)
            .setValue(player)
            .addOnCompleteListener( {
                getPlayers()
            })
    }

    override fun hasFab(): Boolean {
        return true
    }
}