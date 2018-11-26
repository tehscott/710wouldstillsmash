package com.stromberg.scott.seventenwouldstillsmash.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.ProgressBar
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
import com.stromberg.scott.seventenwouldstillsmash.model.GameType
import com.stromberg.scott.seventenwouldstillsmash.model.Player
import com.stromberg.scott.seventenwouldstillsmash.model.Statistic
import com.stromberg.scott.seventenwouldstillsmash.util.CharacterHelper
import com.stromberg.scott.seventenwouldstillsmash.util.PlayerHelper
import com.stromberg.scott.seventenwouldstillsmash.util.getReference
import kotlinx.android.synthetic.main.activity_character.*
import java.util.*

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

    enum class GameResult {
        UNKNOWN,
        WIN,
        LOSS;

        fun toString(gameCount: Int): String {
            return when (this) {
                UNKNOWN -> {
                    "unknown"
                }
                WIN -> {
                    if(gameCount != 1) "wins" else "win"
                }
                LOSS -> {
                    if(gameCount != 1) "losses" else "loss"
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character)

        if(intent?.extras?.containsKey("characterId") == true) {
            mCharacterId = intent!!.extras!!.getInt("characterId")
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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

        recyclerView!!.adapter = gamesAdapter
        recyclerView?.adapter?.notifyDataSetChanged()
    }

    private fun setupStatisticsAdapter(statistics: List<Statistic>) {
        statisticsAdapter = StatisticsListAdapter(statistics)

        recyclerView!!.adapter = statisticsAdapter
        recyclerView!!.adapter?.notifyDataSetChanged()
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

        val royaleGamesCount = games.count { it.gameType.equals(GameType.ROYALE.toString()) }
        val suddenDeathGamesCount = games.count { it.gameType.equals(GameType.SUDDEN_DEATH.toString()) }
        val royaleGamesWon: Float = (games.count { it.players.any { it.characterId == mCharacterId && it.winner } && it.gameType!!.equals(GameType.ROYALE.toString(), true) }).toFloat()
        val suddenDeathGamesWon: Float = (games.count { it.players.any { it.characterId == mCharacterId && it.winner } && it.gameType!!.equals(GameType.SUDDEN_DEATH.toString(), true) }).toFloat()

        val overallWinRate = Math.round(((royaleGamesWon + suddenDeathGamesWon) / (games.size)) * 100).toString() + "% (" + (royaleGamesWon + suddenDeathGamesWon).toInt() + "/" + games.size + ")"
        val royaleWinRate = Math.round((royaleGamesWon / royaleGamesCount) * 100).toString() + "% (" + royaleGamesWon.toInt() + "/" + royaleGamesCount + ")"
        val suddenDeathWinRate = Math.round((suddenDeathGamesWon / suddenDeathGamesCount) * 100).toString() + "% (" + suddenDeathGamesWon.toInt() + "/" + suddenDeathGamesCount + ")"

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val thirtyDaysAgo = calendar.timeInMillis

        val games30Days = games.filter { it.date >= thirtyDaysAgo }
        val royaleGames30DaysCount = games30Days.count { it.gameType.equals(GameType.ROYALE.toString()) }
        val suddenDeathGames30DaysCount = games30Days.count { it.gameType.equals(GameType.SUDDEN_DEATH.toString()) }
        val royaleGames30DaysWon: Float = (games30Days.count { it.players.any { it.characterId == mCharacterId && it.winner } && it.gameType!!.equals(GameType.ROYALE.toString(), true) }).toFloat()
        val suddenDeathGames30DaysWon: Float = (games30Days.count { it.players.any { it.characterId == mCharacterId && it.winner } && it.gameType!!.equals(GameType.SUDDEN_DEATH.toString(), true) }).toFloat()

        val thirtyDaysOverallWinRate = Math.round(((royaleGames30DaysWon + suddenDeathGames30DaysWon) / (games30Days.size)) * 100).toString() + "% (" + (royaleGames30DaysWon + suddenDeathGames30DaysWon).toInt() + "/" + games30Days.size + ")"
        val thirtyDaysRoyaleWinRate = Math.round((royaleGames30DaysWon / royaleGames30DaysCount) * 100).toString() + "% (" + royaleGames30DaysWon.toInt() + "/" + royaleGames30DaysCount + ")"
        val thirtyDaysSuddenDeathWinRate = Math.round((suddenDeathGames30DaysWon / suddenDeathGames30DaysCount) * 100).toString() + "% (" + suddenDeathGames30DaysWon.toInt() + "/" + suddenDeathGames30DaysCount + ")"

        // Win rates
        val allTimeWinRates = Statistic()
        allTimeWinRates.characterId = mCharacterId
        allTimeWinRates.playerValue = " Win rates (all time):\n\t " +
                "Overall: " + overallWinRate + "\n\t " +
                "Royale: " + royaleWinRate + "\n\t " +
                "Sudden Death: " + suddenDeathWinRate
        statistics.add(allTimeWinRates)

        val thirtyDayWinRates = Statistic()
        thirtyDayWinRates.characterId = mCharacterId
        thirtyDayWinRates.playerValue = " Win rates (30 days):\n\t " +
                "Overall: " + thirtyDaysOverallWinRate + "\n\t " +
                "Royale: " + thirtyDaysRoyaleWinRate + "\n\t " +
                "Sudden Death: " + thirtyDaysSuddenDeathWinRate
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
        val allGamesStreak = getCurrentStreak(null)
        val royaleStreak = getCurrentStreak(GameType.ROYALE)
        val suddenDeathStreak = getCurrentStreak(GameType.SUDDEN_DEATH)

        val streaks = Statistic()
        streaks.characterId = mCharacterId
        streaks.playerValue = " Streaks:\n\t " +
                "Current streak (all games): " + allGamesStreak.first + " " + allGamesStreak.second.toString(allGamesStreak.first) + "\n\t " +
                "Win streak (all games): " + getLongestWinStreak(null) + "\n\t " +
                "Losing streak (all games): " + getLongestLosingStreak(null) + "\n\t " +
                "Current Royale streak: " + royaleStreak.first + " " + royaleStreak.second.toString(royaleStreak.first) + "\n\t " +
                "Royale win streak: " + getLongestWinStreak(GameType.ROYALE) + "\n\t " +
                "Royale losing streak: " + getLongestLosingStreak(GameType.ROYALE) + "\n\t " +
                "Current Sudden Death streak: " + suddenDeathStreak.first + " " + suddenDeathStreak.second.toString(suddenDeathStreak.first) + "\n\t " +
                "Sudden Death win streak: " + getLongestWinStreak(GameType.SUDDEN_DEATH) + "\n\t " +
                "Sudden Death losing streak: " + getLongestLosingStreak(GameType.SUDDEN_DEATH)
        statistics.add(streaks)

        setupStatisticsAdapter(statistics)
    }

    private fun getCurrentStreak(gameType: GameType?): Pair<Int, GameResult> {
        var gameCount = 0
        var lastGameResult = GameResult.UNKNOWN
        val sortedGames: List<Game>

        if(gameType != null) {
            sortedGames = games.filter { it.gameType.equals(gameType.toString(), true) }.sortedByDescending { it.date }
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
