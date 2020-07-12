package com.stromberg.scott.seventenwouldstillsmash.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.adapter.GamesListAdapter
import com.stromberg.scott.seventenwouldstillsmash.adapter.StatisticsListAdapter
import com.stromberg.scott.seventenwouldstillsmash.model.*
import com.stromberg.scott.seventenwouldstillsmash.util.CharacterHelper
import com.stromberg.scott.seventenwouldstillsmash.util.PlayerHelper
import com.stromberg.scott.seventenwouldstillsmash.util.getReference
import kotlinx.android.synthetic.main.activity_player.*
import java.util.*
import kotlin.math.roundToInt

class PlayerActivity : BaseActivity() {
    private var db = FirebaseDatabase.getInstance()
    private var games = ArrayList<Game>()
    private var players = ArrayList<Player>()
    private var gamesAdapter: GamesListAdapter? = null
    private var statisticsAdapter: StatisticsListAdapter? = null
    private var editingPlayer: Player? = null
    private var isFirstLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        intent?.extras?.let {
            if (it.containsKey("player")) {
                editingPlayer = it.getParcelable("player") as Player?
            }
        }

        create_player_recyclerview.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        setupGamesAdapter(games)
        setupButtons()

        if (!editingPlayer?.name.isNullOrEmpty()) {
            create_player_name.setText(editingPlayer?.name)
        } else {
            create_player_name.apply {
                setText(R.string.new_player)
                requestFocus()
                selectAll()
            }

            Handler().postDelayed({
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
            }, 250)
        }

        back_button.setOnClickListener { onBackPressed() }
    }

    override fun onResume() {
        super.onResume()

        if(editingPlayer != null) {
            if(games.size == 0) {
                getPlayers()
            } else {
                getStatistics()
            }
        }
    }

    private fun setupGamesAdapter(games: List<Game>) {
        val allNames = HashSet<String>()
        games.forEach { game -> allNames.addAll(game.players.map { player -> player.player.name }) }
        val loserContainerWidth = PlayerHelper.getLongestNameLength(resources.getDimension(R.dimen.loser_name_text_size), allNames.toList()) + (resources.getDimensionPixelSize(R.dimen.loser_image_margin_size) * 2) + resources.getDimensionPixelSize(R.dimen.loser_image_size)

        gamesAdapter = GamesListAdapter(games, GamesListAdapter.SortBy.PLAYER, loserContainerWidth)
        gamesAdapter?.onItemClickListener = BaseQuickAdapter.OnItemClickListener { _, _, position ->
            editGame(games[position], games)
        }

        create_player_recyclerview.adapter = gamesAdapter
        create_player_recyclerview.adapter?.notifyDataSetChanged()

        create_player_recyclerview.visibility = if (games.isEmpty()) View.GONE else View.VISIBLE
        empty_state_text_view.visibility = if (games.isEmpty() && editingPlayer != null) View.VISIBLE else View.GONE
    }

    private fun setupStatisticsAdapter(statistics: List<Statistic>) {
        statisticsAdapter = StatisticsListAdapter(statistics)

        create_player_recyclerview.adapter = statisticsAdapter
        create_player_recyclerview.adapter?.notifyDataSetChanged()

        create_player_recyclerview.visibility = View.VISIBLE
        empty_state_text_view.visibility = View.GONE
    }

    private fun setupButtons() {
        create_player_navigation.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
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

        if(editingPlayer == null) {
            delete_button?.visibility = View.GONE

            save_button?.setOnClickListener { createPlayer() }

            findViewById<View>(R.id.create_player_navigation)?.visibility = View.INVISIBLE
        }
        else {
            delete_button?.visibility = View.VISIBLE
            delete_button?.setOnClickListener { deletePlayer() }

            save_button?.setOnClickListener { updatePlayer() }
        }
    }

    private fun editGame(game: Game, games: List<Game>) {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("Game", game)
        intent.putExtra("TopCharacters", CharacterHelper.getTopCharacters(game.players.map { it.player }, games))
        startActivity(intent)
    }

    private fun getPlayers() {
        db.getReference(context = this)
                .child("players")
                .orderByKey()
                .addListenerForSingleValueEvent( object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) { }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.reversed().forEach {
                            val player: Player = it.getValue(Player::class.java)!!
                            player.id = it.key.orEmpty()
                            players.add(player)
                        }

                        players.sortBy { it.name }

                        getGames()
                    }
                })
    }

    private fun getGames() {
        setContentShown(false)

        db.getReference(context = this)
                .child("games")
                .orderByChild("date")
                .addListenerForSingleValueEvent( object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) { }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        games.clear()

                        snapshot.children.reversed().forEach {
                            val game: Game = it.getValue(Game::class.java)!!

                            if(game.players.any { player -> player.player.id == editingPlayer?.id }) {
                                game.id = it.key!!
                                games.add(game)
                            }
                        }

                        if(isFirstLoad) {
                            isFirstLoad = false
                            getStatistics()
                        }
                        else {
                            gamesAdapter?.loadMoreComplete()

                            setupGamesAdapter(games)
                        }

                        setContentShown(true)
                    }
                })
    }

    private fun getStatistics() {
        val statistics = ArrayList<Statistic>()

        val gamesThisPlayerPlayedAllTime = games.filter { it.players.any { player -> player.player.id == editingPlayer?.id } }
        val gamesThisPlayerWonAllTime: Float = (gamesThisPlayerPlayedAllTime.count { it.players.any { player -> player.player.id == editingPlayer!!.id && player.winner } }).toFloat()
        val last30GamesThisPlayerPlayed = gamesThisPlayerPlayedAllTime.sortedByDescending { it.date }.take(30)
        val gamesThisPlayerWonOutOfLast30Games: Float = (last30GamesThisPlayerPlayed.count { it.players.any { player -> player.player.id == editingPlayer!!.id && player.winner } }).toFloat()

        val allGameTypesOverallWinRate = (((gamesThisPlayerWonAllTime) / (gamesThisPlayerPlayedAllTime.size)) * 100).roundToInt().toString() + "% (" + (gamesThisPlayerWonAllTime).toInt() + "/" + gamesThisPlayerPlayedAllTime.size + ")"
        val allGameTypesLast30GamesWinRate = (((gamesThisPlayerWonOutOfLast30Games) / (last30GamesThisPlayerPlayed.size)) * 100).roundToInt().toString() + "% (" + (gamesThisPlayerWonOutOfLast30Games).toInt() + "/" + last30GamesThisPlayerPlayed.size + ")"

        val allTimeWinRates = Statistic()
        allTimeWinRates.playerId = editingPlayer!!.id
        allTimeWinRates.playerValue = " Win rates (all games):\n\t Overall: $allGameTypesOverallWinRate"
        statistics.add(allTimeWinRates)

        val thirtyGameWinRates = Statistic()
        thirtyGameWinRates.playerId = editingPlayer!!.id
        thirtyGameWinRates.playerValue = " Win rates (last 30 games):\n\t Overall: $allGameTypesLast30GamesWinRate"

        statistics.add(thirtyGameWinRates)

        // Best/Worst vs player
        var bestVsPlayer: Player? = null
        var bestVsPlayerWinRate = 0f
        var bestVsPlayerNumGames = 0
        var worstVsPlayer: Player? = null
        var worstVsPlayerWinRate = 0f
        var worstVsPlayerNumGames = 0

        val averageGamesPlayedByPlayers = games.size / players.size

        players.forEachIndexed { _, player ->
            if(player.id != editingPlayer!!.id) {
                val numGamesWithThisPlayer = games.count { it.players.any { gamePlayer -> gamePlayer.player.id == player.id } }
                val numGamesThisPlayerWon: Float = games.count { it.players.any { gamePlayer -> gamePlayer.player.id == player.id && gamePlayer.winner } }.toFloat()
                val numGamesIWonVsThisPlayer: Float = games.count { it.players.any { gamePlayer -> gamePlayer.player.id == player.id } && it.players.any { gamePlayer -> gamePlayer.player.id == editingPlayer?.id && gamePlayer.winner } }.toFloat()

                if(numGamesWithThisPlayer > 0 && numGamesWithThisPlayer >= averageGamesPlayedByPlayers) {
                    val thisPlayerWinRate = numGamesThisPlayerWon / numGamesWithThisPlayer.toFloat()
                    val winRateVsThisPlayer = numGamesIWonVsThisPlayer / numGamesWithThisPlayer.toFloat()

                    if (bestVsPlayer == null || winRateVsThisPlayer > bestVsPlayerWinRate) {
                        bestVsPlayerNumGames = numGamesWithThisPlayer
                        bestVsPlayerWinRate = winRateVsThisPlayer
                        bestVsPlayer = player
                    }

                    if (worstVsPlayer == null || worstVsPlayerWinRate < thisPlayerWinRate) {
                        worstVsPlayerNumGames = numGamesWithThisPlayer
                        worstVsPlayerWinRate = thisPlayerWinRate
                        worstVsPlayer = player
                    }
                }
            }
        }

        if(bestVsPlayer != null) {
            val bestVsPlayerStat = Statistic()
            bestVsPlayerStat.playerId = editingPlayer!!.id

            if(bestVsPlayerWinRate > 0) {
                bestVsPlayerStat.playerValue = " Best vs " + bestVsPlayer?.name + " (won " + (bestVsPlayerWinRate * 100).roundToInt() + "% of " + bestVsPlayerNumGames + " games)"
            }
            else {
                bestVsPlayerStat.playerValue = " Best vs no players...git gud nerd"
            }

            statistics.add(bestVsPlayerStat)
        }

        if(worstVsPlayer != null) {
            val worstVsPlayerStat = Statistic()
            worstVsPlayerStat.playerId = editingPlayer!!.id

            if(worstVsPlayerWinRate > 0) {
                worstVsPlayerStat.playerValue = " Worst vs " + worstVsPlayer?.name + " (lost " + (worstVsPlayerWinRate * 100).roundToInt() + "% of " + worstVsPlayerNumGames + " games)"
            }
            else {
                worstVsPlayerStat.playerValue = " Worst vs no players...you are supreme"
            }

            statistics.add(worstVsPlayerStat)
        }

        // Best vs character
        // Worst vs character
        var bestVsCharacterId: Int? = null
        var bestVsCharacterWinRate = 0f
        var bestVsCharacterNumGames = 0
        var worstVsCharacterId: Int? = null
        var worstVsCharacterWinRate = 0f
        var worstVsCharacterNumGames = 0

        val averageGamesPlayed = Characters.values().sumBy { character ->
            games.count { it.players.any { gamePlayer -> gamePlayer.characterId == character.id && gamePlayer.player.id != editingPlayer?.id } } } / Characters.values().size

        Characters.values().forEach { character ->
            val numGamesWithThisCharacter = games.count { it.players.any { gamePlayer -> gamePlayer.characterId == character.id && gamePlayer.player.id != editingPlayer?.id } }
            val numGamesThisCharacterWon: Float = games.count { it.players.any { gamePlayer -> gamePlayer.characterId == character.id && gamePlayer.player.id != editingPlayer?.id && gamePlayer.winner } }.toFloat()
            val numGamesIWonVsThisCharacter: Float = games.count { it.players.any { gamePlayer -> gamePlayer.characterId == character.id && gamePlayer.player.id != editingPlayer?.id } && it.players.any { gamePlayer -> gamePlayer.player.id == editingPlayer?.id && gamePlayer.winner } }.toFloat()

            val thisCharacterWinRate = numGamesThisCharacterWon / numGamesWithThisCharacter.toFloat()
            val winRateVsThisCharacter = numGamesIWonVsThisCharacter / numGamesWithThisCharacter.toFloat()

            if(numGamesWithThisCharacter > 0 && numGamesWithThisCharacter >= averageGamesPlayed) {
                if (bestVsCharacterId == null || winRateVsThisCharacter > bestVsCharacterWinRate) {
                    bestVsCharacterNumGames = numGamesWithThisCharacter
                    bestVsCharacterWinRate = winRateVsThisCharacter
                    bestVsCharacterId = character.id
                }

                if (worstVsCharacterId == null || worstVsCharacterWinRate < thisCharacterWinRate) {
                    worstVsCharacterNumGames = numGamesWithThisCharacter
                    worstVsCharacterWinRate = thisCharacterWinRate
                    worstVsCharacterId = character.id
                }
            }
        }

        if(bestVsCharacterId != null) {
            val bestVsCharacterStat = Statistic()
            bestVsCharacterStat.playerId = editingPlayer!!.id

            if(bestVsCharacterWinRate > 0) {
                bestVsCharacterStat.playerValue = " Best vs " + Characters.byId(bestVsCharacterId!!)?.characterName + " (won " + (bestVsCharacterWinRate * 100).roundToInt() + "% of " + bestVsCharacterNumGames + " games)"
            }
            else {
                bestVsCharacterStat.playerValue = " Best vs no characters...git gud nerd"
            }

            statistics.add(bestVsCharacterStat)
        }

        if(worstVsCharacterId != null) {
            val worstVsCharacterStat = Statistic()
            worstVsCharacterStat.playerId = editingPlayer!!.id

            if(worstVsCharacterWinRate > 0) {
                worstVsCharacterStat.playerValue = " Worst vs " + Characters.byId(worstVsCharacterId!!)?.characterName + " (lost " + (worstVsCharacterWinRate * 100).roundToInt() + "% of " + worstVsCharacterNumGames + " games)"
            }
            else {
                worstVsCharacterStat.playerValue = " Worst vs no characters...you are supreme"
            }

            statistics.add(worstVsCharacterStat)
        }

        // Streaks
        if(gamesThisPlayerPlayedAllTime.isNotEmpty()) {
            val allGamesStreak = getCurrentStreak()

            val streaks = Statistic()
            streaks.playerId = editingPlayer!!.id
            streaks.playerValue = " Streaks:\n\t " +
                    "Current streak (all games): " + allGamesStreak.first + " " + allGamesStreak.second.toString(allGamesStreak.first) + "\n\t " +
                    "Longest win streak (all games): " + getLongestWinStreak() + "\n\t " +
                    "Longest losing streak (all games): " + getLongestLosingStreak() + "\n\t "
            statistics.add(streaks)
        }


        if(gamesThisPlayerPlayedAllTime.isNotEmpty()) {
            getCharacterStats(statistics)
        }

        setupStatisticsAdapter(statistics)
    }

    private fun createPlayer() {
        val playerName = create_player_name.text.toString()
        if (playerName.isNotEmpty()) {
            val player = Player(Calendar.getInstance().timeInMillis.toString(), playerName)

            db.getReference(context = this)
                    .child("players")
                    .child(player.id)
                    .setValue(player)
                    .addOnCompleteListener {
                        onBackPressed()
                    }
        }
        else {
            showDialog("Set a player name.")
        }
    }

    private fun updatePlayer() {
        val playerName = create_player_name.text.toString()
        if (playerName.isNotEmpty()) {
            editingPlayer!!.name = playerName

            db.getReference(context = this)
                .child("players")
                .child(editingPlayer!!.id)
                .setValue(editingPlayer)
                .addOnCompleteListener {
                    onBackPressed()
                }
        }
    }

    private fun deletePlayer() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete " + editingPlayer?.name)
        builder.setMessage("You can't undo this. Are you sure?")
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            db.getReference(context = this)
                    .child("players")
                    .child(editingPlayer!!.id)
                    .removeValue()
                    .addOnCompleteListener {
                        onBackPressed()
                    }
        }
        builder.setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    override fun setContentShown(shown: Boolean) {
        progress.visibility = if(shown) View.GONE else View.VISIBLE
        create_player_recyclerview.visibility = if(shown) View.VISIBLE else View.GONE
    }

    private fun getCharacterStats(statistics: ArrayList<Statistic>) {
        var statsString = " Character stats:\n"

        Characters.values().sortedBy { it.characterName }.forEach { character ->
            val gamesForCharacter = games.filter {
                it.players.any {player -> player.characterId == character.id && player.player.id == editingPlayer?.id }
            }

            val gamesWonCount = gamesForCharacter.count { it.players.any { player -> player.characterId == character.id && player.winner } }
            if (gamesForCharacter.isNotEmpty()) {
                val gamesWinPercentage = (gamesWonCount.toDouble() / gamesForCharacter.size.toDouble() * 100).roundToInt()
                statsString += "   ${Characters.byId(character.id)?.characterName}: $gamesWonCount/${gamesForCharacter.size} ($gamesWinPercentage%)\n"
            }
        }

        val stat = Statistic()
        stat.playerId = editingPlayer!!.id
        stat.playerValue = statsString
        statistics.add(stat)
    }

    private fun getCurrentStreak(): Pair<Int, GameResult> {
        var gameCount = 0
        var lastGameResult = GameResult.UNKNOWN
        val sortedGames = games.sortedByDescending { it.date }

        sortedGames.forEach {
            val didIWin = it.players.any { gamePlayer -> gamePlayer.player.id == editingPlayer?.id && gamePlayer.winner }

            if(lastGameResult == GameResult.UNKNOWN || (lastGameResult == GameResult.WIN && didIWin) || (lastGameResult == GameResult.LOSS && !didIWin)) {
                gameCount++
                lastGameResult = if(didIWin) GameResult.WIN else GameResult.LOSS
            }
            else {
                return Pair(gameCount, lastGameResult)
            }
        }

        return Pair(gameCount, lastGameResult)
    }

    private fun getLongestWinStreak(): Int {
        var winCount = 0
        var longestWinStreak = 0

        val sortedGames = games.sortedBy { it.date }

        sortedGames.forEach {
            if (it.players.first { gamePlayer -> gamePlayer.player.id == editingPlayer?.id }.winner) {
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

    private fun getLongestLosingStreak(): Int {
        var lossCount = 0
        var longestLossStreak = 0

        val sortedGames = games.sortedBy { it.date }

        sortedGames.forEach {
            if (it.players.first { gamePlayer -> gamePlayer.player.id == editingPlayer?.id }.winner) {
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
}
