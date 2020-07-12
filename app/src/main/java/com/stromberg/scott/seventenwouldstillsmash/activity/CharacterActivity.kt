package com.stromberg.scott.seventenwouldstillsmash.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
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
import kotlinx.android.synthetic.main.activity_character.*
import java.util.*
import kotlin.math.roundToInt

class CharacterActivity : BaseActivity() {
    private var db = FirebaseDatabase.getInstance()
    private var games = ArrayList<Game>()
    private var players = ArrayList<Player>()
    private var gamesAdapter: GamesListAdapter? = null
    private var statisticsAdapter: StatisticsListAdapter? = null
    private var isFirstLoad = true
    private var characterId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character)

        if(intent?.extras?.containsKey("characterId") == true) {
            characterId = intent!!.extras!!.getInt("characterId")
        }

        character_recyclerview.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        setupGamesAdapter(games)

        val character = Characters.byId(characterId)
        character_name.text = character?.characterName
        character_image.setImageResource(character?.imageRes ?: 0)
        character_image.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                character_image.viewTreeObserver.removeOnPreDrawListener(this)

                val layoutParams = character_image.layoutParams
                layoutParams.width = character_image.height
                character_image.layoutParams = layoutParams

                return false
            }
        })

        character_navigation.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
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

        back_button.setOnClickListener { onBackPressed() }
    }

    override fun onResume() {
        super.onResume()

        if(games.size == 0) {
            getPlayers()
        }
    }

    private fun setupGamesAdapter(games: List<Game>) {
        val allNames = HashSet<String>()
        games.forEach { allNames.addAll(it.players.map { gamePlayer -> gamePlayer.player.name }) }
        var loserContainerWidth = PlayerHelper.getLongestNameLength(resources.getDimension(R.dimen.loser_name_text_size), allNames.toList())
        loserContainerWidth += (resources.getDimensionPixelSize(R.dimen.loser_image_margin_size) * 2) + resources.getDimensionPixelSize(R.dimen.loser_image_size)

        gamesAdapter = GamesListAdapter(games, GamesListAdapter.SortBy.WINNER, loserContainerWidth)
        gamesAdapter?.onItemClickListener = BaseQuickAdapter.OnItemClickListener { _, _, position ->
            editGame(games[position], games)
        }

        character_recyclerview.adapter = gamesAdapter as RecyclerView.Adapter<*>
        character_recyclerview?.adapter?.notifyDataSetChanged()

        character_recyclerview.visibility = if (games.isEmpty()) View.GONE else View.VISIBLE
        empty_state_text_view.visibility = if (games.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun setupStatisticsAdapter(statistics: List<Statistic>) {
        statisticsAdapter = StatisticsListAdapter(statistics)

        character_recyclerview.adapter = statisticsAdapter as RecyclerView.Adapter<*>
        character_recyclerview.adapter?.notifyDataSetChanged()

        character_recyclerview.visibility = View.VISIBLE
        empty_state_text_view.visibility = View.GONE
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

                            if(game.players.any { player -> player.characterId == characterId }) {
                                game.id = it.key.orEmpty()
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

    private fun editGame(game: Game, games: List<Game>) {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("Game", game)
        intent.putExtra("TopCharacters", CharacterHelper.getTopCharacters(game.players.map { it.player }, games))
        startActivity(intent)
    }

    private fun getStatistics() {
        val statistics = ArrayList<Statistic>()

        val gamesThisCharacterPlayedAllTime = games.filter { it.players.any { player -> player.characterId == characterId } }
        val gamesThisCharacterWonAllTime: Float = (gamesThisCharacterPlayedAllTime.count { it.players.any { player -> player.characterId == characterId && player.winner } }).toFloat()
        val last30GamesThisCharacterPlayed = gamesThisCharacterPlayedAllTime.sortedByDescending { it.date }.take(30)
        val gamesThisCharacterWonLast30Games: Float = (last30GamesThisCharacterPlayed.count { it.players.any { player -> player.characterId == characterId && player.winner } }).toFloat()

        val allGameTypesOverallWinRate = (((gamesThisCharacterWonAllTime) / (gamesThisCharacterPlayedAllTime.size)) * 100).roundToInt().toString() + "% (" + (gamesThisCharacterWonAllTime).toInt() + "/" + gamesThisCharacterPlayedAllTime.size + ")"
        val allGameTypesLast30GamesWinRate = (((gamesThisCharacterWonLast30Games) / (last30GamesThisCharacterPlayed.size)) * 100).roundToInt().toString() + "% (" + (gamesThisCharacterWonLast30Games).toInt() + "/" + last30GamesThisCharacterPlayed.size + ")"

        val allTimeWinRates = Statistic()
        allTimeWinRates.characterId = characterId
        allTimeWinRates.playerValue = " Win rates (all games):\n\t Overall: $allGameTypesOverallWinRate"
        statistics.add(allTimeWinRates)

        val thirtyDayWinRates = Statistic()
        thirtyDayWinRates.characterId = characterId
        thirtyDayWinRates.playerValue = " Win rates (last 30 games):\n\t Overall: $allGameTypesLast30GamesWinRate"
        statistics.add(thirtyDayWinRates)

        // Best vs character
        // Worst vs character
        var bestVsCharacterId: Int? = null
        var bestVsCharacterWinRate = 0f
        var bestVsCharacterNumGames = 0
        var worstVsCharacterId: Int? = null
        var worstVsCharacterWinRate = 0f
        var worstVsCharacterNumGames = 0

        val averageGamesPlayed = Characters.values().sumByDouble { character ->
            games.count { it.players.any { gamePlayer -> gamePlayer.characterId == character.id } }.toDouble() / games.size.toDouble()
        }

        Characters.values().forEach { character ->
            val numGamesWithThisCharacter: Int
            val numGamesThisCharacterWon: Float
            val numGamesIWonVsThisCharacter: Float

            if(character.id == characterId) {
                val gamesVsSameCharacter = games.filter { it.players.count { gamePlayer -> gamePlayer.characterId == character.id } > 1 }
                numGamesWithThisCharacter = gamesVsSameCharacter.size
                numGamesThisCharacterWon = gamesVsSameCharacter.count { it.players.any { gamePlayer -> gamePlayer.characterId == character.id && gamePlayer.winner } }.toFloat()
                numGamesIWonVsThisCharacter = gamesVsSameCharacter.count { it.players.any { gamePlayer -> gamePlayer.characterId == characterId && gamePlayer.winner } && it.players.any { gamePlayer -> gamePlayer.characterId == character.id } }.toFloat()
            }
            else {
                numGamesWithThisCharacter = games.count { it.players.any { gamePlayer -> gamePlayer.characterId == character.id } }
                numGamesThisCharacterWon = games.count { it.players.any { gamePlayer -> gamePlayer.characterId == character.id && gamePlayer.winner } }.toFloat()
                numGamesIWonVsThisCharacter = games.count { it.players.any { gamePlayer -> gamePlayer.characterId == characterId && gamePlayer.winner } && it.players.any { gamePlayer -> gamePlayer.characterId == character.id } }.toFloat()
            }

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
            bestVsCharacterStat.characterId = characterId
            bestVsCharacterStat.playerValue = " Best vs " + Characters.byId(bestVsCharacterId!!)?.characterName + " (won " + (bestVsCharacterWinRate * 100).roundToInt() + "% of " + bestVsCharacterNumGames + " games)"
            statistics.add(bestVsCharacterStat)
        }

        if(worstVsCharacterId != null) {
            val worstVsCharacterStat = Statistic()
            worstVsCharacterStat.characterId = characterId
            worstVsCharacterStat.playerValue = " Worst vs " + Characters.byId(worstVsCharacterId!!)?.characterName + " (lost " + (worstVsCharacterWinRate * 100).roundToInt() + "% of " + worstVsCharacterNumGames + " games)"
            statistics.add(worstVsCharacterStat)
        }

        // Streaks
        if(gamesThisCharacterPlayedAllTime.isNotEmpty()) {
            val allGamesStreak = getCurrentStreak()

            val streaks = Statistic()
            streaks.characterId = characterId
            streaks.playerValue = " Streaks:\n\t " +
                    "Current streak (all games): " + allGamesStreak.first + " " + allGamesStreak.second.toString(allGamesStreak.first) + "\n\t " +
                    "Longest win streak (all games): " + getLongestWinStreak() + "\n\t " +
                    "Longest losing streak (all games): " + getLongestLosingStreak() + "\n\t "
            statistics.add(streaks)
        }

        setupStatisticsAdapter(statistics)
    }

    private fun getCurrentStreak(): Pair<Int, GameResult> {
        var gameCount = 0
        var lastGameResult = GameResult.UNKNOWN
        val sortedGames = games.sortedByDescending { it.date }

        sortedGames.forEach {
            val didIWin = it.players.any { gamePlayer -> gamePlayer.characterId == characterId && gamePlayer.winner }

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
            if (it.players.first { gamePlayer -> gamePlayer.characterId == characterId }.winner) {
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
            if (it.players.first { gamePlayer -> gamePlayer.characterId == characterId }.winner) {
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
        progress.visibility = if(shown) View.GONE else View.VISIBLE
        character_recyclerview.visibility = if(shown) View.VISIBLE else View.GONE
    }
}
