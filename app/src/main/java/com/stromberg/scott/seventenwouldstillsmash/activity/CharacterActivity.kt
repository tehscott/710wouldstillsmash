package com.stromberg.scott.seventenwouldstillsmash.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ProgressBar
import android.widget.TextView
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
import com.stromberg.scott.seventenwouldstillsmash.model.Game
import com.stromberg.scott.seventenwouldstillsmash.model.GameResult
import com.stromberg.scott.seventenwouldstillsmash.model.Player
import com.stromberg.scott.seventenwouldstillsmash.model.Statistic
import com.stromberg.scott.seventenwouldstillsmash.util.CharacterHelper
import com.stromberg.scott.seventenwouldstillsmash.util.GameTypeHelper
import com.stromberg.scott.seventenwouldstillsmash.util.PlayerHelper
import com.stromberg.scott.seventenwouldstillsmash.util.getReference
import kotlinx.android.synthetic.main.activity_character.*
import java.util.*
import kotlin.collections.HashMap

class CharacterActivity : BaseActivity() {
    private var db = FirebaseDatabase.getInstance()
    private var games = ArrayList<Game>()
    private var players = ArrayList<Player>()
    private var gamesAdapter: GamesListAdapter? = null
    private var statisticsAdapter: StatisticsListAdapter? = null
    private var isFirstLoad = true;
    private var mCharacterId: Int = -1

    private var recyclerView: RecyclerView? = null
    private var progressBar: ProgressBar? = null
    private var tabs: BottomNavigationView? = null
    private lateinit var emptyStateTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character)

        if(intent?.extras?.containsKey("characterId") == true) {
            mCharacterId = intent!!.extras!!.getInt("characterId")
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        emptyStateTextView = findViewById(R.id.empty_state_text_view)
        recyclerView = findViewById(R.id.character_recyclerview)
        recyclerView!!.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        progressBar = findViewById(R.id.progress)

        setupGamesAdapter(games)

        character_name.text = CharacterHelper.getName(mCharacterId)
        character_image.setImageResource(CharacterHelper.getImage(mCharacterId))
        character_image.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                character_image.viewTreeObserver.removeOnPreDrawListener(this)

                var layoutParams = character_image.layoutParams
                layoutParams.width = character_image.height
                character_image.layoutParams = layoutParams

                return false
            }
        })

        tabs = findViewById(R.id.character_navigation)

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
    }

    override fun onResume() {
        super.onResume()

        if(games.size == 0) {
            getPlayers()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun setupGamesAdapter(games: List<Game>) {
        val allNames = HashSet<String>()
        games.forEach { allNames.addAll(it.players.map { it.player!!.name!! }) }
        var loserContainerWidth = PlayerHelper.getLongestNameLength(resources, "Quicksand-Light.ttf", resources.getDimension(R.dimen.loser_name_text_size), allNames.toList())
        loserContainerWidth += (resources.getDimensionPixelSize(R.dimen.loser_image_margin_size) * 2) + resources.getDimensionPixelSize(R.dimen.loser_image_size)

        gamesAdapter = GamesListAdapter(games, GamesListAdapter.SortBy.WINNER, loserContainerWidth)

        gamesAdapter!!.onItemClickListener = BaseQuickAdapter.OnItemClickListener { _, _, position ->
            editGame(games[position], games)
        }

        recyclerView!!.adapter = gamesAdapter as RecyclerView.Adapter<*>
        recyclerView?.adapter?.notifyDataSetChanged()

        recyclerView!!.visibility = if (games.isEmpty()) View.GONE else View.VISIBLE
        emptyStateTextView.visibility = if (games.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun setupStatisticsAdapter(statistics: List<Statistic>) {
        statisticsAdapter = StatisticsListAdapter(statistics)

        recyclerView!!.adapter = statisticsAdapter as RecyclerView.Adapter<*>
        recyclerView!!.adapter?.notifyDataSetChanged()

        recyclerView!!.visibility = View.VISIBLE
        emptyStateTextView.visibility = View.GONE
    }

    private fun getPlayers() {
        db.getReference(context = this)
                .child("players")
                .orderByKey()
                .addListenerForSingleValueEvent( object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) { }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        players.clear()

                        snapshot.children.reversed().forEach {
                            var player: Player = it.getValue(Player::class.java)!!
                            player.id = it.key
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
                            var game: Game = it.getValue(Game::class.java)!!

                            if(game.players.any({ it.characterId == mCharacterId })) {
                                game.id = it.key!!
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

    private fun editGame(game: Game, games: List<Game>) {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("Game", game)
        intent.putExtra("TopCharacters", CharacterHelper.getTopCharacters(game.players.map { it.player!! }, games))
        startActivity(intent)
    }

    private fun getStatistics() {
        val statistics = ArrayList<Statistic>()

        val gameTypes = HashMap<String?, Int>()

        games.map { it.gameType }.forEach { gameTypeId ->
            val gameType = GameTypeHelper.getGameType(gameTypeId)

            if(gameType != null && !gameType.isDeleted) {
                gameTypes[gameTypeId] = games.count { game -> game.gameType == gameTypeId }
            }
        }

        val top2GameTypes = gameTypes.toList().sortedByDescending { (_, count) -> count}.take(2).map { it.first }
        Log.d("gametypes", top2GameTypes.toString())

        val gamesThisCharacterPlayedAllTime = games.filter { it.players.any { player -> player.characterId == mCharacterId } }
        val gamesThisCharacterWonAllTime: Float = (gamesThisCharacterPlayedAllTime.count { it.players.any { player -> player.characterId == mCharacterId && player.winner } }).toFloat()
        val last30GamesThisCharacterPlayed = gamesThisCharacterPlayedAllTime.sortedByDescending { it.date }.take(30)
        val gamesThisCharacterWonLast30Games: Float = (last30GamesThisCharacterPlayed.count { it.players.any { player -> player.characterId == mCharacterId && player.winner } }).toFloat()

        val allGameTypesOverallWinRate = Math.round(((gamesThisCharacterWonAllTime) / (gamesThisCharacterPlayedAllTime.size)) * 100).toString() + "% (" + (gamesThisCharacterWonAllTime).toInt() + "/" + gamesThisCharacterPlayedAllTime.size + ")"
        val allGameTypesLast30GamesWinRate = Math.round(((gamesThisCharacterWonLast30Games) / (last30GamesThisCharacterPlayed.size)) * 100).toString() + "% (" + (gamesThisCharacterWonLast30Games).toInt() + "/" + last30GamesThisCharacterPlayed.size + ")"

        val top2GameTypeAllTimeStats = HashMap<String?, String>()
        val top2GameTypeLast30GamesStats = HashMap<String?, String>()
        top2GameTypes.forEach { gameTypeId ->
            val gamesThisCharacterPlayedForThisGameTypeAllTime = gamesThisCharacterPlayedAllTime.filter { game -> game.gameType == gameTypeId }
            val gamesThisCharacterWonForThisGameTypeAllTimeCount = gamesThisCharacterPlayedForThisGameTypeAllTime.count { it.players.any { player -> player.characterId == mCharacterId && player.winner } }
            val thisGameTypeAllTimeWinRateDouble  = Math.round((gamesThisCharacterWonForThisGameTypeAllTimeCount / gamesThisCharacterPlayedForThisGameTypeAllTime.size.toDouble()) * 100)
            val thisGameTypeAllTimeWinRate = thisGameTypeAllTimeWinRateDouble.toString() + "% (" + gamesThisCharacterWonForThisGameTypeAllTimeCount + "/" + gamesThisCharacterPlayedForThisGameTypeAllTime.size + ")"

            val last30GamesThisCharacterPlayedForThisGameType = gamesThisCharacterPlayedAllTime.filter{ game -> game.gameType == gameTypeId }.sortedByDescending { it.date }.take(30)
            val gamesThisCharacterWonForThisGameTypeLast30GamesCount = last30GamesThisCharacterPlayedForThisGameType.count { it.players.any { player -> player.characterId == mCharacterId && player.winner } }
            val thisGameTypeLast30GamesWinRateDouble = Math.round((gamesThisCharacterWonForThisGameTypeLast30GamesCount / last30GamesThisCharacterPlayedForThisGameType.size.toDouble()) * 100)
            val thisGameTypeLast30GamesWinRate = thisGameTypeLast30GamesWinRateDouble.toString() + "% (" + gamesThisCharacterWonForThisGameTypeLast30GamesCount + "/" + last30GamesThisCharacterPlayedForThisGameType.size + ")"

            if(gamesThisCharacterPlayedForThisGameTypeAllTime.isNotEmpty()) {
                top2GameTypeAllTimeStats[gameTypeId] = thisGameTypeAllTimeWinRate
            }

            if(last30GamesThisCharacterPlayedForThisGameType.isNotEmpty()) {
                top2GameTypeLast30GamesStats[gameTypeId] = thisGameTypeLast30GamesWinRate
            }
        }

        val allTimeWinRates = Statistic()
        allTimeWinRates.characterId = mCharacterId
        allTimeWinRates.playerValue = " Win rates (all games):\n\t " +
                "Overall: " + allGameTypesOverallWinRate + "\n\t "

        top2GameTypeAllTimeStats.forEach { gameTypeId, winRateText ->
            val gameType = GameTypeHelper.getGameType(gameTypeId)

            if(gameType != null) {
                allTimeWinRates.playerValue += "${gameType.name}: $winRateText\n\t "
            }
        }
        allTimeWinRates.playerValue = allTimeWinRates.playerValue.removeSuffix("\n\t ")
        statistics.add(allTimeWinRates)

        val thirtyDayWinRates = Statistic()
        thirtyDayWinRates.characterId = mCharacterId
        thirtyDayWinRates.playerValue = " Win rates (last 30 games):\n\t " +
                "Overall: " + allGameTypesLast30GamesWinRate + "\n\t "

        top2GameTypeLast30GamesStats.forEach { gameTypeId, winRateText ->
            val gameType = GameTypeHelper.getGameType(gameTypeId)

            if(gameType != null) {
                thirtyDayWinRates.playerValue += "${gameType.name}: $winRateText\n\t "
            }
        }
        thirtyDayWinRates.playerValue = thirtyDayWinRates.playerValue.removeSuffix("\n\t ")
        statistics.add(thirtyDayWinRates)

        // Best vs character
        // Worst vs character
        var bestVsCharacterId: Int? = null
        var bestVsCharacterWinRate = 0f
        var bestVsCharacterNumGames = 0
        var worstVsCharacterId: Int? = null
        var worstVsCharacterWinRate = 0f
        var worstVsCharacterNumGames = 0

        val averageGamesPlayed = (0..CharacterHelper.getNumberOfCharacters()).sumByDouble {
            var characterId = it
            games.count { it.players.any { it.characterId == characterId } }.toDouble() / games.size.toDouble() }

        (0..CharacterHelper.getNumberOfCharacters()).forEachIndexed { _, characterId ->
            var numGamesWithThisCharacter: Int
            var numGamesThisCharacterWon: Float
            var numGamesIWonVsThisCharacter: Float

            if(characterId == mCharacterId) {
                val gamesVsSameCharacter = games.filter { it.players.count { it.characterId == characterId } > 1 }
                numGamesWithThisCharacter = gamesVsSameCharacter.size
                numGamesThisCharacterWon = gamesVsSameCharacter.count { it.players.any { it.characterId == characterId && it.winner } }.toFloat()
                numGamesIWonVsThisCharacter = gamesVsSameCharacter.count { it.players.any { it.characterId == mCharacterId && it.winner } && it.players.any { it.characterId == characterId } }.toFloat()
            }
            else {
                numGamesWithThisCharacter = games.count { it.players.any { it.characterId == characterId } }
                numGamesThisCharacterWon = games.count { it.players.any { it.characterId == characterId && it.winner } }.toFloat()
                numGamesIWonVsThisCharacter = games.count { it.players.any { it.characterId == mCharacterId && it.winner } && it.players.any { it.characterId == characterId } }.toFloat()
            }

            val thisCharacterWinRate = numGamesThisCharacterWon / numGamesWithThisCharacter.toFloat()
            val winRateVsThisCharacter = numGamesIWonVsThisCharacter / numGamesWithThisCharacter.toFloat()

            if(numGamesWithThisCharacter > 0 && numGamesWithThisCharacter >= averageGamesPlayed) {
                if (bestVsCharacterId == null || winRateVsThisCharacter > bestVsCharacterWinRate) {
                    bestVsCharacterNumGames = numGamesWithThisCharacter
                    bestVsCharacterWinRate = winRateVsThisCharacter
                    bestVsCharacterId = characterId
                }

                if (worstVsCharacterId == null || worstVsCharacterWinRate < thisCharacterWinRate) {
                    worstVsCharacterNumGames = numGamesWithThisCharacter
                    worstVsCharacterWinRate = thisCharacterWinRate
                    worstVsCharacterId = characterId
                }
            }
        }

        if(bestVsCharacterId != null) {
            val bestVsCharacterStat = Statistic()
            bestVsCharacterStat.characterId = mCharacterId
            bestVsCharacterStat.playerValue = " Best vs " + CharacterHelper.getName(bestVsCharacterId!!) + " (won " + Math.round(bestVsCharacterWinRate * 100) + "% of " + bestVsCharacterNumGames + " games)"
            statistics.add(bestVsCharacterStat)
        }

        if(worstVsCharacterId != null) {
            val worstVsCharacterStat = Statistic()
            worstVsCharacterStat.characterId = mCharacterId
            worstVsCharacterStat.playerValue = " Worst vs " + CharacterHelper.getName(worstVsCharacterId!!) + " (lost " + Math.round(worstVsCharacterWinRate * 100) + "% of " + worstVsCharacterNumGames + " games)"
            statistics.add(worstVsCharacterStat)
        }

        // Streaks
        if(gamesThisCharacterPlayedAllTime.size > 0) {
            val allGamesStreak = getCurrentStreak(null)

            val streaks = Statistic()
            streaks.characterId = mCharacterId
            streaks.playerValue = " Streaks:\n\t " +
                    "Current streak (all games): " + allGamesStreak.first + " " + allGamesStreak.second.toString(allGamesStreak.first) + "\n\t " +
                    "Longest win streak (all games): " + getLongestWinStreak(null) + "\n\t " +
                    "Longest losing streak (all games): " + getLongestLosingStreak(null) + "\n\t "

            gameTypes.forEach { gameType ->
                val gt = GameTypeHelper.getGameType(gameType.key)
                val streak = getCurrentStreak(gt?.id)

                streaks.playerValue += "\n\t Current ${gt?.name} streak: " + streak.first + " " + streak.second.toString(streak.first) + "\n\t " +
                        "Longest ${gt?.name} win streak: " + getLongestWinStreak(gt?.id) + "\n\t " +
                        "Longest ${gt?.name} losing streak: " + getLongestLosingStreak(gt?.id)
            }

            statistics.add(streaks)
        }

        setupStatisticsAdapter(statistics)
    }

    private fun getCurrentStreak(gameTypeId: String?): Pair<Int, GameResult> {
        var gameCount = 0
        var lastGameResult = GameResult.UNKNOWN
        val sortedGames: List<Game>

        if(gameTypeId?.isNotEmpty() == true) {
            sortedGames = games.filter { it.gameType.equals(gameTypeId, true) }.sortedByDescending { it.date }
        }
        else {
            sortedGames = games.sortedByDescending { it.date }
        }

        sortedGames.forEach {
            val didIWin = it.players.any { it.characterId == mCharacterId && it.winner }

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

    private fun getLongestWinStreak(gameTypeId: String?): Int {
        var winCount = 0
        var longestWinStreak = 0

        val sortedGames: List<Game>

        if(gameTypeId?.isNotEmpty() == true) {
            sortedGames = games.filter { it.gameType.equals(gameTypeId, true) }.sortedBy { it.date }
        }
        else {
            sortedGames = games.sortedBy { it.date }
        }

        sortedGames.forEach {
            if (it.players.first { it.characterId == mCharacterId }.winner) {
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

    private fun getLongestLosingStreak(gameTypeId: String?): Int {
        var lossCount = 0
        var longestLossStreak = 0

        val sortedGames: List<Game>

        if(gameTypeId?.isNotEmpty() == true) {
            sortedGames = games.filter { it.gameType.equals(gameTypeId, true) }.sortedBy { it.date }
        }
        else {
            sortedGames = games.sortedBy { it.date }
        }

        sortedGames.forEach {
            if (it.players.first { it.characterId == mCharacterId }.winner) {
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

    override fun setContentShown(shown: Boolean) {
        progressBar!!.visibility = if(shown) View.GONE else View.VISIBLE
        recyclerView!!.visibility = if(shown) View.VISIBLE else View.GONE
    }
}
