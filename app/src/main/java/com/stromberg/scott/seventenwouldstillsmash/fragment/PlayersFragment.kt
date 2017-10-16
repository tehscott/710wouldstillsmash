package com.stromberg.scott.seventenwouldstillsmash.fragment

import android.content.Context
import android.content.res.Resources
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
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
import com.google.firebase.firestore.FirebaseFirestore
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.adapter.PlayersListAdapter
import com.stromberg.scott.seventenwouldstillsmash.model.Player
import com.stromberg.scott.seventenwouldstillsmash.util.hideKeyboard
import uk.co.chrisjenx.calligraphy.TypefaceUtils
import java.util.*

class PlayersFragment : BaseFragment() {
    private var db = FirebaseFirestore.getInstance()

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

        db.collection("players")
                .orderBy("name")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result) {
                            var player = Player()
                            player.id = document.id
                            player.name = document.get("name").toString()
                            players.add(player)
                        }

                        val playerNameWidth = getLongestNameLength(players)

                        recyclerView!!.adapter = PlayersListAdapter(players, playerNameWidth)
                        recyclerView?.adapter?.notifyDataSetChanged()
                    } else {
                        Snackbar.make(contentView!!, "Failed to load player data", Snackbar.LENGTH_SHORT).show()
                    }

                    setContentShown(true)
                }
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
        val inputLayout = LayoutInflater.from(activity).inflate(R.layout.add_player_snackbar, null)

        inputLayout.findViewById<ImageButton>(R.id.cancel).setOnClickListener({ snackbar?.dismiss() })
        inputLayout.findViewById<ImageButton>(R.id.add).setOnClickListener({
            run {
                var playerName = inputLayout.findViewById<EditText>(R.id.player_name_text).text.toString()

                if(playerName.isNotEmpty()) {
                    addPlayer(inputLayout, playerName)
                }
            }
        })

        snackbar = Snackbar.make(contentView!!, "", Snackbar.LENGTH_INDEFINITE)
        val group = snackbar!!.view as ViewGroup
        group.setBackgroundColor(ContextCompat.getColor(activity, android.R.color.white))

        val layout = snackbar!!.view as Snackbar.SnackbarLayout
        layout.addView(inputLayout, 0);
        snackbar!!.show();
    }

    private fun addPlayer(contentView: View, playerName: String) {
        this.contentView?.hideKeyboard()

        contentView.findViewById<View>(R.id.content).visibility = View.GONE
        contentView.findViewById<View>(R.id.progress).visibility = View.VISIBLE

        var player = HashMap<String, Any>()
        player.put("name", playerName)

        db.collection("players")
            .add(player)
            .addOnSuccessListener { documentReference ->
                run {
                    snackbar?.dismiss()

                    getPlayers()
                }
            }
            .addOnFailureListener({
                run {
                    snackbar?.dismiss()
                    Snackbar.make(contentView!!, "Failed to add player", Snackbar.LENGTH_LONG).show()
                }
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