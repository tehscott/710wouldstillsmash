package com.stromberg.scott.seventenwouldstillsmash.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.ajguan.library.EasyRefreshLayout
import com.ajguan.library.LoadModel
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stromberg.scott.seventenwouldstillsmash.activity.MainActivity
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.adapter.GamesListAdapter
import com.stromberg.scott.seventenwouldstillsmash.model.Game
import com.stromberg.scott.seventenwouldstillsmash.util.CharacterHelper
import com.stromberg.scott.seventenwouldstillsmash.util.getReference
import java.text.DateFormatSymbols
import java.util.*


class GamesFragment : BaseFragment() {
    private var db = FirebaseDatabase.getInstance()
    private var games = ArrayList<Game>()
    private var adapter: GamesListAdapter? = null
    private var lastGameLoaded: String? = null
    private val quantityToLoad = 20

    private var contentView: View? = null
    private var pullToRefreshView: EasyRefreshLayout? = null
    private var recyclerView: RecyclerView? = null
    private var progressBar: ProgressBar? = null
    private var searchView: SearchView? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        contentView = View.inflate(activity, R.layout.fragment_games, null)

        pullToRefreshView = contentView!!.findViewById(R.id.games_pull_to_refresh)
        recyclerView = contentView!!.findViewById(R.id.games_recyclerview)
        recyclerView!!.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        progressBar = contentView!!.findViewById(R.id.progress)
        searchView = contentView!!.findViewById(R.id.games_search_view)

        setupAdapter(games)
        setupSearchView()

        pullToRefreshView!!.loadMoreModel = LoadModel.NONE
        pullToRefreshView!!.addEasyEvent(object: EasyRefreshLayout.EasyEvent {
            override fun onRefreshing() {
                getGames()
            }

            override fun onLoadMore() {}
        })

        return contentView
    }

    override fun onResume() {
        super.onResume()

        getGames()
//        importGames()
    }

    private fun setupAdapter(games: List<Game>) {
        adapter = GamesListAdapter(games, GamesListAdapter.SortBy.WINNER)

        adapter!!.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            (activity as MainActivity).editGame(games[position], games)
        }

//        adapter!!.setEnableLoadMore(true)
//        adapter!!.setOnLoadMoreListener(BaseQuickAdapter.RequestLoadMoreListener { getMoreGames(lastGameLoaded, quantityToLoad) })

        recyclerView!!.adapter = adapter
    }

    private fun setupSearchView() {
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if(newText != null) {
                    var searchCriteria = newText.split(",")

                    var filteredGames = findGames(searchCriteria[0].trim(), games)

                    searchCriteria.forEachIndexed { index, criteria ->
                        if(index > 0) {
                            filteredGames = ArrayList(findGames(criteria.trim(), games).intersect(filteredGames))
                        }
                    }

                    setupAdapter(filteredGames)
                }

                return true
            }

        })
    }

    private fun findGames(queryText: String, gamesToSearchWithin: ArrayList<Game>): ArrayList<Game> {
        return ArrayList(gamesToSearchWithin.filter {
            try {
                it.gameType!!.contains(queryText, true)
                    || hasDateMatch(it.date, queryText)
                    || it.players.any {
                        CharacterHelper.getName(it.characterId).contains(queryText, true)
                        || it.player!!.name!!.contains(queryText, true)
                    }
            } catch(_: Exception) {
                false
            }
        })
    }

    private fun hasDateMatch(date1: Long, queryText: String): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.timeInMillis = date1

        var dayEquals = cal1[Calendar.DAY_OF_MONTH].toString().equals(queryText, true) // number
        dayEquals = dayEquals || cal1[Calendar.DAY_OF_WEEK].toString().contains(queryText, true) // name

        val monthName = DateFormatSymbols().months[cal1[Calendar.MONTH]]

        var monthEquals = cal1[Calendar.MONTH].toString().equals(queryText, true) // number
        monthEquals = monthEquals || monthName.contains(queryText, true) // name

        var yearEquals = cal1[Calendar.YEAR].toString().contains(queryText, true)

        return dayEquals || monthEquals || yearEquals
    }

    private fun getGames() {
        setContentShown(false)

        db.getReference(activity)
            .child("games")
            .orderByChild("date")
//            .limitToLast(queryLimit)
            .addListenerForSingleValueEvent( object : ValueEventListener {
                override fun onCancelled(error: DatabaseError?) { }

                override fun onDataChange(snapshot: DataSnapshot?) {
                    handleSnapshot(snapshot)
                }
            })
    }

//    private fun getMoreGames(startAfter: String?, queryLimit: Int) {
//        db.getReference("games")
//                .orderByKey()
//                .endAt(startAfter)
//                .limitToLast(queryLimit)
//                .addListenerForSingleValueEvent( object : ValueEventListener {
//                    override fun onCancelled(error: DatabaseError?) { }
//
//                    override fun onDataChange(snapshot: DataSnapshot?) {
//                        handleSnapshot(snapshot, snapshot!!.children.count() == queryLimit)
//                    }
//                })
//    }

    fun handleSnapshot(snapshot: DataSnapshot?) {
        games.clear()

        snapshot?.children?.reversed()?.forEach {
            var game: Game = it.getValue(Game::class.java)!!
            game.id = it.key
            games.add(game)
        }

//        if(shouldShowMore) {
//            lastGameLoaded = games.last().id
//        }
//        else {
//            adapter!!.setEnableLoadMore(false)
//        }

        setupAdapter(games)
        recyclerView?.adapter?.notifyDataSetChanged()

        pullToRefreshView!!.refreshComplete()

        adapter!!.loadMoreComplete()

//        if(toggleContentShown) {
            setContentShown(true)
//        }
    }

    override fun setContentShown(shown: Boolean) {
        progressBar!!.visibility = if(shown) View.GONE else View.VISIBLE
        pullToRefreshView!!.visibility = if(shown) View.VISIBLE else View.GONE
        searchView!!.visibility = if(shown) View.VISIBLE else View.GONE
    }

    override fun addFabClicked() {
        (activity as MainActivity).createGame(games)
    }

    override fun hasFab(): Boolean {
        return true
    }
}