package com.stromberg.scott.seventenwouldstillsmash.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
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
import com.stromberg.scott.seventenwouldstillsmash.adapter.StatisticsListAdapter
import com.stromberg.scott.seventenwouldstillsmash.model.Game
import com.stromberg.scott.seventenwouldstillsmash.model.GameType
import com.stromberg.scott.seventenwouldstillsmash.model.Player
import com.stromberg.scott.seventenwouldstillsmash.model.Statistic
import java.util.*

class CreatePlayerFragment : BaseFragment() {
    private var db = FirebaseDatabase.getInstance()
    private var games = ArrayList<Game>()
    private var gamesAdapter: GamesListAdapter? = null
    private var statisticsAdapter: StatisticsListAdapter? = null

    private var contentView: View? = null
    private var recyclerView: RecyclerView? = null
    private var progressBar: ProgressBar? = null
    private var deleteButton: Button? = null
    private var cancelButton: Button? = null
    private var saveButton: Button? = null
    private var nameEditText: EditText? = null
    private var tabs: BottomNavigationView? = null

    private var editingPlayer: Player? = null
    private var isFirstLoad = true;

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
            if(games.size == 0) {
                getGames()
            }
            else {
                getStatistics()
            }
        }
    }

    private fun setupGamesAdapter(games: List<Game>) {
        gamesAdapter = GamesListAdapter(games)

        gamesAdapter!!.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            (activity as MainActivity).editGame(games[position])
        }

        recyclerView!!.adapter = gamesAdapter
        recyclerView!!.adapter.notifyDataSetChanged()
    }

    private fun setupStatisticsAdapter(statistics: List<Statistic>) {
        statisticsAdapter = StatisticsListAdapter(statistics)

        recyclerView!!.adapter = statisticsAdapter
        recyclerView!!.adapter.notifyDataSetChanged()
    }

    private fun setupButtons() {
        cancelButton = contentView!!.findViewById(R.id.create_player_cancel_button)
        saveButton = contentView!!.findViewById(R.id.create_player_create_button)
        deleteButton = contentView!!.findViewById(R.id.create_player_delete_button)
        tabs = contentView!!.findViewById(R.id.create_player_navigation)

        tabs?.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_statistics -> {
                    getStatistics()
                }
                R.id.navigation_games -> {
                    getGames()
                }
            }

            return@OnNavigationItemSelectedListener true
        })

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
                    games.clear()

                    snapshot?.children?.reversed()?.forEach {
                        var game: Game = it.getValue(Game::class.java)!!

                        if(game.players.any({ it.player!!.id == editingPlayer!!.id })) {
                            game.id = it.key
                            games.add(game)
                        }
                    }

                    if(isFirstLoad) {
                        isFirstLoad = false
                        getStatistics()
                    }
                    else {
                        gamesAdapter!!.loadMoreComplete()

                        setupGamesAdapter(games)
                    }

                    setContentShown(true)
                }
            })
    }

    private fun getStatistics() {
        val statistics = ArrayList<Statistic>()

        val royaleGamesWon: Float = (games.count { it.players.any { it.player!!.id == editingPlayer!!.id && it.winner } && it.gameType!!.equals(GameType.ROYALE.toString(), true) }).toFloat()
        val royaleGamesLost: Float = games.size - royaleGamesWon
        val suddenDeathGamesWon: Float = (games.count { it.players.any { it.player!!.id == editingPlayer!!.id && it.winner } && it.gameType!!.equals(GameType.SUDDEN_DEATH.toString(), true) }).toFloat()
        val suddenDeathGamesLost: Float = games.size - suddenDeathGamesWon

        // Overall win rate
        val overallWinRate = Statistic()
        overallWinRate.playerId = editingPlayer!!.id!!
        overallWinRate.playerValue = " Overall win rate: " + Math.round(((royaleGamesWon + suddenDeathGamesWon) / (royaleGamesLost + suddenDeathGamesLost)) * 100).toString() + "% (" + (royaleGamesWon + suddenDeathGamesWon).toInt() + "/" + (royaleGamesLost + suddenDeathGamesLost).toInt() + ")"
        statistics.add(overallWinRate)

        // Royale win rate
        val royaleWinRate = Statistic()
        royaleWinRate.playerId = editingPlayer!!.id!!
        royaleWinRate.playerValue = " Royale win rate: " + Math.round((royaleGamesWon / royaleGamesLost) * 100).toString() + "% (" + royaleGamesWon.toInt() + "/" + royaleGamesLost.toInt() + ")"
        statistics.add(royaleWinRate)

        // Sudden Death win rate
        val suddenDeathWinRate = Statistic()
        suddenDeathWinRate.playerId = editingPlayer!!.id!!
        suddenDeathWinRate.playerValue = " Sudden Death win rate: " + Math.round((suddenDeathGamesWon / suddenDeathGamesLost) * 100).toString() + "% (" + suddenDeathGamesWon.toInt() + "/" + suddenDeathGamesLost.toInt() + ")"
        statistics.add(suddenDeathWinRate)

        // Longest win streak (all games)
        val longestWinStreakAllGames = Statistic()
        longestWinStreakAllGames.playerId = editingPlayer!!.id!!
        longestWinStreakAllGames.playerValue = " Longest win streak (all games): " + getLongestWinStreak(null)
        statistics.add(longestWinStreakAllGames)

        // Longest lose streak (all games)
        val longestLosingStreakAllGames = Statistic()
        longestLosingStreakAllGames.playerId = editingPlayer!!.id!!
        longestLosingStreakAllGames.playerValue = " Longest losing streak (all games): " + getLongestLosingStreak(null)
        statistics.add(longestLosingStreakAllGames)

        // Longest win streak (royale)
        val longestRoyaleWinStreak = Statistic()
        longestRoyaleWinStreak.playerId = editingPlayer!!.id!!
        longestRoyaleWinStreak.playerValue = " Longest Royale win streak: " + getLongestWinStreak(GameType.ROYALE)
        statistics.add(longestRoyaleWinStreak)

        // Longest lose streak (royale)
        val longestRoyaleLosingStreak = Statistic()
        longestRoyaleLosingStreak.playerId = editingPlayer!!.id!!
        longestRoyaleLosingStreak.playerValue = " Longest Royale losing streak: " + getLongestLosingStreak(GameType.ROYALE)
        statistics.add(longestRoyaleLosingStreak)

        // Longest win streak (sudden death)
        val longestSuddenDeathWinStreak = Statistic()
        longestSuddenDeathWinStreak.playerId = editingPlayer!!.id!!
        longestSuddenDeathWinStreak.playerValue = " Longest Sudden Death win streak: " + getLongestWinStreak(GameType.SUDDEN_DEATH)
        statistics.add(longestSuddenDeathWinStreak)

        // Longest lose streak (sudden death)
        val longestSuddenDeathLosingStreak = Statistic()
        longestSuddenDeathLosingStreak.playerId = editingPlayer!!.id!!
        longestSuddenDeathLosingStreak.playerValue = " Longest Sudden Death losing streak: " + getLongestLosingStreak(GameType.SUDDEN_DEATH)
        statistics.add(longestSuddenDeathLosingStreak)

        // Most played characters (top 3)
        // Least played character (bottom 3)
        // Best character
//        games.sortBy { it. }
//
//        val bestCharacter = Statistic()
//        bestCharacter.playerId = editingPlayer!!.id!!
//        bestCharacter.playerValue = " Best character: " +
//        statistics.add(bestCharacter)

        // Worst character
        // Best vs character
        // Worst vs character
        // Best vs player
        // Worst vs player

        setupStatisticsAdapter(statistics)
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

    private fun getLongestWinStreak(gameType: GameType?): Int {
        var winCount = 0
        var longestWinStreak = 0

        val sortedGames: List<Game>

        if(gameType != null) {
            sortedGames = games.filter { it.gameType.equals(gameType.toString(), true) }.sortedBy { it.date }
        }
        else {
            sortedGames = games.sortedBy { it.date }
        }

        sortedGames.forEach {
            if (it.players.first { it.player!!.id!! == editingPlayer!!.id }.winner) {
                winCount++
            } else {
                winCount = 0
            }

            if (winCount > longestWinStreak) {
                longestWinStreak = winCount
            }
        }

        return longestWinStreak
    }

    private fun getLongestLosingStreak(gameType: GameType?): Int {
        var lossCount = 0
        var longestLossStreak = 0

        val sortedGames: List<Game>

        if(gameType != null) {
            sortedGames = games.filter { it.gameType.equals(gameType.toString(), true) }.sortedBy { it.date }
        }
        else {
            sortedGames = games.sortedBy { it.date }
        }

        sortedGames.forEach {
            if (it.players.first { it.player!!.id!! == editingPlayer!!.id }.winner) {
                lossCount = 0
            } else {
                lossCount++
            }

            if (lossCount > longestLossStreak) {
                longestLossStreak = lossCount
            }
        }

        return longestLossStreak
    }

//    private fun addLongestStreak(gamesForPlayers: HashMap<Player, List<Game>>, gamesForCharacters: HashMap<Int, List<Game>>, gameType: GameType) {
//        var mostWinsPlayer: Player? = null
//        var mostPlayerWins = 0
//        var mostLossesPlayer: Player? = null
//        var mostPlayerLosses = 0
//
//        gamesForPlayers.forEach {
//            val player = it.key
//            var winCount = 0
//            var longestWinStreak = 0
//            var lossCount = 0
//            var longestLossStreak = 0
//
//            Log.d("streak", "starting " + player.name)
//
//            var sortedGames = it.value.filter { it.gameType.equals(gameType.toString(), true) }.sortedBy { it.date }
//            sortedGames.forEach {
//                if (it.players.first { it.player!!.id!! == player.id }.winner) {
//                    winCount++
//                    lossCount = 0
//                } else {
//                    lossCount++
//                    winCount = 0
//                }
//
//                if (winCount > longestWinStreak) {
//                    longestWinStreak = winCount
//                }
//
//                if (longestWinStreak > mostPlayerWins) {
//                    mostWinsPlayer = player
//                    mostPlayerWins = winCount
////                    Log.d("streak", "mostPlayerWins: $mostPlayerWins by " + mostWinsPlayer!!.name)
//                }
//
//                if (lossCount > longestLossStreak) {
//                    longestLossStreak = lossCount
////                    Log.d("streak", "longestLossStreak: $longestLossStreak by " + player.name)
//                }
//
//                if (longestLossStreak > mostPlayerLosses) {
//                    mostLossesPlayer = player
//                    mostPlayerLosses = lossCount
////                    Log.d("streak", "mostPlayerLosses: $mostPlayerLosses by " + mostLossesPlayer!!.name)
////                    Log.d("streak", "last game " + it.date + " as " + CharacterHelper.getName(it.players.first { it.player!!.id!! == player.id }.characterId))
//                }
//            }
//        }
//
//        var mostWinsCharacterId: Int? = null
//        var mostCharacterWins = 0
//        var mostLossesCharacterId: Int? = null
//        var mostCharacterLosses = 0
//
//        gamesForCharacters.forEach {
//            val characterId = it.key
//            var winCount = 0
//            var longestWinStreak = 0
//            var lossCount = 0
//            var longestLossStreak = 0
//
//            var sortedGames = it.value.filter { it.gameType.equals(gameType.toString(), true) }.sortedBy { it.date }
//            sortedGames.forEach {
//                if (it.players.first { it.characterId == characterId }.winner) {
//                    winCount++
//                    lossCount = 0
//                } else {
//                    lossCount++
//                    winCount = 0
//                }
//
//                if (winCount > longestWinStreak) {
//                    longestWinStreak = winCount
//                }
//
//                if (longestWinStreak > mostCharacterWins) {
//                    mostWinsCharacterId = characterId
//                    mostCharacterWins = winCount
////                    Log.d("streak", "mostCharacterWins: $mostCharacterWins by " + CharacterHelper.getName(mostWinsCharacterId!!))
//                }
//
//                if (lossCount > longestLossStreak) {
//                    longestLossStreak = lossCount
//                }
//
//                if (longestLossStreak > mostCharacterLosses) {
//                    mostLossesCharacterId = characterId
//                    mostCharacterLosses = lossCount
////                    Log.d("streak", "mostCharacterLosses: $mostCharacterLosses by " + CharacterHelper.getName(mostLossesCharacterId!!))
//                }
//            }
//        }
//
//        val mostWinsParent: LinearLayout = layoutInflater.inflate(R.layout.statistics_parent_list_item, null) as LinearLayout
//        mostWinsParent.layoutParams = createLinearLayoutParams()
//        mostWinsParent.findViewById<TextView>(R.id.statistics_parent_title).text = "Longest " + gameType.prettyName() + " Win Streak"
//
//        val mostWinsChild = layoutInflater.inflate(R.layout.statistics_child_list_item, mostWinsParent.findViewById(R.id.statistics_parent_list))
//        mostWinsChild.findViewById<TextView>(R.id.statistics_child_player_stat).text = mostWinsPlayer!!.name + " ($mostPlayerWins)"
//        mostWinsChild.findViewById<ImageView>(R.id.statistics_child_character_image).setImageResource(CharacterHelper.getImage(mostWinsCharacterId!!))
//        mostWinsChild.findViewById<TextView>(R.id.statistics_child_character_stat).text = CharacterHelper.getName(mostWinsCharacterId!!) + " ($mostPlayerWins)"
//        list!!.addView(mostWinsParent)
//
//        list!!.addView(createSpace())
//
//        val mostLossesParent: LinearLayout = layoutInflater.inflate(R.layout.statistics_parent_list_item, null) as LinearLayout
//        mostLossesParent.layoutParams = createLinearLayoutParams()
//        mostLossesParent.findViewById<TextView>(R.id.statistics_parent_title).text = "Longest " + gameType.prettyName() + " Loss Streak"
//
//        val mostLossesChild = layoutInflater.inflate(R.layout.statistics_child_list_item, mostLossesParent.findViewById(R.id.statistics_parent_list))
//        mostLossesChild.findViewById<TextView>(R.id.statistics_child_player_stat).text = mostLossesPlayer!!.name + " ($mostPlayerLosses)"
//        mostLossesChild.findViewById<ImageView>(R.id.statistics_child_character_image).setImageResource(CharacterHelper.getImage(mostLossesCharacterId!!))
//        mostLossesChild.findViewById<TextView>(R.id.statistics_child_character_stat).text = CharacterHelper.getName(mostLossesCharacterId!!) + " ($mostCharacterLosses)"
//        list!!.addView(mostLossesParent)
//    }

}