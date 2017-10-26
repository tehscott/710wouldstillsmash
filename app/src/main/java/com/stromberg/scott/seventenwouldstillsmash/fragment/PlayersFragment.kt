package com.stromberg.scott.seventenwouldstillsmash.fragment

import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
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

    private var snackbar: Snackbar? = null

    private var contentView: View? = null
    private var recyclerView: RecyclerView? = null
    private var progressBar: ProgressBar? = null
    private var searchView: SearchView? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        contentView = View.inflate(activity as Context?, R.layout.fragment_players, null)

        recyclerView = contentView!!.findViewById<RecyclerView>(R.id.players_recyclerview)
        recyclerView!!.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        progressBar = contentView!!.findViewById<ProgressBar>(R.id.progress)
        searchView = contentView!!.findViewById<SearchView>(R.id.players_search_view)

        return contentView
    }

    override fun onResume() {
        super.onResume()

        getPlayers()
    }

    private fun getPlayers() {
        var players = ArrayList<Player>()

        setContentShown(false)

        db.reference
            .child("players")
            .orderByKey()
            .addListenerForSingleValueEvent( object : ValueEventListener {
                override fun onCancelled(error: DatabaseError?) { }

                override fun onDataChange(snapshot: DataSnapshot?) {
                    snapshot?.children?.reversed()?.forEach {
                        var player: Player = it.getValue(Player::class.java)!!
                        player.id = it.key
                        players.add(player)
                    }

                    val playerNameWidth = getLongestNameLength(players)

                    recyclerView!!.adapter = PlayersListAdapter(players, playerNameWidth)
                    recyclerView?.adapter?.notifyDataSetChanged()

                    setContentShown(true)
                }
            })
    }

    override fun getFabButtons(context: Context): List<FloatingActionButton> {
        var fabs = ArrayList<FloatingActionButton>()

        return fabs
    }

    override fun setContentShown(shown: Boolean) {
        progressBar!!.visibility = if(shown) View.GONE else View.VISIBLE
        recyclerView!!.visibility = if(shown) View.VISIBLE else View.GONE
        searchView!!.visibility = if(shown) View.VISIBLE else View.GONE
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
//        val inputLayout = LayoutInflater.from(activity).inflate(R.layout.add_player_snackbar, null)
//
//        inputLayout.findViewById<ImageButton>(R.id.cancel).setOnClickListener({ snackbar?.dismiss() })
//        inputLayout.findViewById<ImageButton>(R.id.add).setOnClickListener({
//            run {
//                var playerName = inputLayout.findViewById<EditText>(R.id.player_name_text).text.toString()
//
//                if(playerName.isNotEmpty()) {
//                    addPlayer(inputLayout, playerName)
//                }
//            }
//        })
//
//        snackbar = Snackbar.make(contentView!!, "", Snackbar.LENGTH_INDEFINITE)
//        val group = snackbar!!.view as ViewGroup
//        group.setBackgroundColor(ContextCompat.getColor(activity, android.R.color.white))
//
//        val layout = snackbar!!.view as Snackbar.SnackbarLayout
//        layout.addView(inputLayout, 0);
//        snackbar!!.show();

        var builder = AlertDialog.Builder(activity)
        builder.setTitle("Add Player")

        val inputLayout = LayoutInflater.from(activity).inflate(R.layout.add_player_dialog, null)

        builder.setView(inputLayout)
        builder.setPositiveButton("Add") { dialog, _ -> run {
            var playerName = inputLayout.findViewById<EditText>(R.id.add_player_dialog_player_name).text.toString()
            if(playerName.isNotEmpty()) {
                dialog.dismiss()
                addPlayer(playerName)
            }
        }}
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.setCancelable(true)
        builder.show()
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
                snackbar?.dismiss()
                getPlayers()
            })
    }

    override fun hasFab(): Boolean {
        return true
    }

    fun dismissSnackbar(motionEvent: MotionEvent) {
        if (snackbar != null && snackbar!!.isShown) {
            val sRect = Rect()
            snackbar!!.view.getHitRect(sRect)

            if (!sRect.contains(motionEvent.x.toInt(), motionEvent.y.toInt())) {
                contentView?.hideKeyboard()
                snackbar!!.dismiss()
            }
        }
    }
}