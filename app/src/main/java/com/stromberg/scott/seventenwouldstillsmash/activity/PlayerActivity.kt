package com.stromberg.scott.seventenwouldstillsmash.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.adapter.GamesListAdapter
import com.stromberg.scott.seventenwouldstillsmash.adapter.StatisticsListAdapter
import com.stromberg.scott.seventenwouldstillsmash.model.*
import com.stromberg.scott.seventenwouldstillsmash.util.*
import kotlinx.android.synthetic.main.activity_player.*
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig
import java.util.*

class PlayerActivity : BaseActivity() {
    private var db = FirebaseDatabase.getInstance()
    private var games = ArrayList<Game>()
    private var players = ArrayList<Player>()
    private var gamesAdapter: GamesListAdapter? = null
    private var statisticsAdapter: StatisticsListAdapter? = null

    private var recyclerView: RecyclerView? = null
    private var progressBar: ProgressBar? = null
    private var nameEditText: EditText? = null
    private var tabs: BottomNavigationView? = null
    private lateinit var visibilityToggle: ImageView
    private lateinit var priorityToggle: ImageView
    private lateinit var emptyStateTextView: TextView

    private var editingPlayer: Player? = null
    private var hasMadeEdit: Boolean = false
    private var isPlayerHidden = true
    private var isPlayerLowPriority = false
    private var isFirstLoad = true
    private val characterStats = ArrayList<CharacterStats>()
    private var bestRoyaleCharacters = ArrayList<CharacterStats>()
    private var bestSuddenDeathCharacters = ArrayList<CharacterStats>()
    private var worstRoyaleCharacters = ArrayList<CharacterStats>()
    private var worstSuddenDeathCharacters = ArrayList<CharacterStats>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if(intent?.extras != null) {
            if(intent.extras!!.containsKey("player")) {
                editingPlayer = intent.extras!!.getSerializable("player") as Player?
                isPlayerHidden = editingPlayer?.isHidden ?: false
                isPlayerLowPriority = editingPlayer?.isLowPriority ?: false
            }
        }

        emptyStateTextView = findViewById(R.id.empty_state_text_view)
        recyclerView = findViewById(R.id.create_player_recyclerview)
        recyclerView!!.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        progressBar = findViewById(R.id.progress)

        setupGamesAdapter(games)
        setupButtons()

        player_title_text.text = editingPlayer?.name ?: "New Player"

        nameEditText = findViewById(R.id.create_player_name)
        nameEditText?.setText(editingPlayer?.name ?: "")

        visibilityToggle = findViewById(R.id.create_player_visibility_button)
        visibilityToggle.setOnClickListener {
            if(isPlayerHidden) {
                visibilityToggle.setImageResource(R.drawable.ic_visibility_on)
                showSnackbar(player_title_text.text.toString() + " set to visible")
            }
            else {
                visibilityToggle.setImageResource(R.drawable.ic_visibility_off)
                showSnackbar(player_title_text.text.toString() + " set to hidden")
            }

            isPlayerHidden = !isPlayerHidden

            if(editingPlayer != null) {
                updatePlayer(false)
            }
        }

        if(isPlayerHidden) {
            visibilityToggle.setImageResource(R.drawable.ic_visibility_off)
        }
        else {
            visibilityToggle.setImageResource(R.drawable.ic_visibility_on)
        }

        priorityToggle = findViewById(R.id.create_player_priority_button)
        priorityToggle.setOnClickListener {
            if(isPlayerLowPriority) {
                priorityToggle.setImageResource(R.drawable.ic_priority_high)
                showSnackbar(player_title_text.text.toString() + " set to normal priority")
            }
            else {
                priorityToggle.setImageResource(R.drawable.ic_priority_low)
                showSnackbar(player_title_text.text.toString() + " set to low priority")
            }

            isPlayerLowPriority = !isPlayerLowPriority

            if(editingPlayer != null) {
                updatePlayer(false)
            }
        }

        if(isPlayerLowPriority) {
            priorityToggle.setImageResource(R.drawable.ic_priority_low)
        }
        else {
            priorityToggle.setImageResource(R.drawable.ic_priority_high)
        }
    }

    override fun onResume() {
        super.onResume()

        if(editingPlayer != null) {
            if(games.size == 0) {
                getPlayers()
            }
            else {
                getStatistics()
            }
        }

        showTooltips()
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

    private fun hasMadeEdits() : Boolean {
        if(editingPlayer != null && editingPlayer!!.name != create_player_name.text.toString()) {
            hasMadeEdit = true
        }

        return hasMadeEdit
    }

    private fun setupGamesAdapter(games: List<Game>) {
        val allNames = HashSet<String>()
        games.forEach { allNames.addAll(it.players.map { it.player!!.name!! }) }
        var loserContainerWidth = PlayerHelper.getLongestNameLength(resources, "Quicksand-Light.ttf", resources.getDimension(R.dimen.loser_name_text_size), allNames.toList())
        loserContainerWidth += (resources.getDimensionPixelSize(R.dimen.loser_image_margin_size) * 2) + resources.getDimensionPixelSize(R.dimen.loser_image_size)

        gamesAdapter = GamesListAdapter(games, GamesListAdapter.SortBy.PLAYER, loserContainerWidth)

        gamesAdapter!!.onItemClickListener = BaseQuickAdapter.OnItemClickListener { _, _, position ->
            editGame(games[position], games)
        }

        recyclerView!!.adapter = gamesAdapter
        recyclerView!!.adapter!!.notifyDataSetChanged()

        recyclerView!!.visibility = if (games.isEmpty()) View.GONE else View.VISIBLE
        emptyStateTextView.visibility = if (games.isEmpty() && editingPlayer != null) View.VISIBLE else View.GONE
    }

    private fun setupStatisticsAdapter(statistics: List<Statistic>) {
        statisticsAdapter = StatisticsListAdapter(statistics)

        recyclerView!!.adapter = statisticsAdapter
        recyclerView!!.adapter!!.notifyDataSetChanged()

        recyclerView!!.visibility = View.VISIBLE
        emptyStateTextView.visibility = View.GONE
    }

    private fun setupButtons() {
        tabs = findViewById(R.id.create_player_navigation)

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

        if(editingPlayer == null) {
            delete_button?.visibility = View.GONE

            save_button?.setOnClickListener { createPlayer() }

            findViewById<View>(R.id.create_player_navigation)?.visibility = View.INVISIBLE
        }
        else {
            delete_button?.visibility = View.VISIBLE
            delete_button?.setOnClickListener { deletePlayer() }

            save_button?.setOnClickListener { updatePlayer(true) }
        }
    }

    private fun editGame(game: Game, games: List<Game>) {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("Game", game)
        intent.putExtra("TopCharacters", CharacterHelper.getTopCharacters(game.players.map { it.player!! }, games))
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

                            if(game.players.any({ it.player!!.id == editingPlayer!!.id })) {
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

    private fun getStatistics() {
        val statistics = ArrayList<Statistic>()

        val royaleGamesCount = games.count { it.gameType.equals(GameType.ROYALE.toString()) }
        val suddenDeathGamesCount = games.count { it.gameType.equals(GameType.SUDDEN_DEATH.toString()) }
        val royaleGamesWon: Float = (games.count { it.players.any { it.player!!.id == editingPlayer!!.id && it.winner } && it.gameType!!.equals(GameType.ROYALE.toString(), true) }).toFloat()
        val suddenDeathGamesWon: Float = (games.count { it.players.any { it.player!!.id == editingPlayer!!.id && it.winner } && it.gameType!!.equals(GameType.SUDDEN_DEATH.toString(), true) }).toFloat()

        val overallWinRate = Math.round(((royaleGamesWon + suddenDeathGamesWon) / (games.size)) * 100).toString() + "% (" + (royaleGamesWon + suddenDeathGamesWon).toInt() + "/" + games.size + ")"
        val royaleWinRate = Math.round((royaleGamesWon / royaleGamesCount) * 100).toString() + "% (" + royaleGamesWon.toInt() + "/" + royaleGamesCount + ")"
        val suddenDeathWinRate = Math.round((suddenDeathGamesWon / suddenDeathGamesCount) * 100).toString() + "% (" + suddenDeathGamesWon.toInt() + "/" + suddenDeathGamesCount + ")"

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val thirtyDaysAgo = calendar.timeInMillis

        val games30Days = games.filter { it.date >= thirtyDaysAgo }
        val royaleGames30DaysCount = games30Days.count { it.gameType.equals(GameType.ROYALE.toString()) }
        val suddenDeathGames30DaysCount = games30Days.count { it.gameType.equals(GameType.SUDDEN_DEATH.toString()) }
        val royaleGames30DaysWon: Float = (games30Days.count { it.players.any { it.player!!.id == editingPlayer!!.id && it.winner } && it.gameType!!.equals(GameType.ROYALE.toString(), true) }).toFloat()
        val suddenDeathGames30DaysWon: Float = (games30Days.count { it.players.any { it.player!!.id == editingPlayer!!.id && it.winner } && it.gameType!!.equals(GameType.SUDDEN_DEATH.toString(), true) }).toFloat()

        val thirtyDaysOverallWinRate = Math.round(((royaleGames30DaysWon + suddenDeathGames30DaysWon) / (games30Days.size)) * 100).toString() + "% (" + (royaleGames30DaysWon + suddenDeathGames30DaysWon).toInt() + "/" + games30Days.size + ")"
        val thirtyDaysRoyaleWinRate = Math.round((royaleGames30DaysWon / royaleGames30DaysCount) * 100).toString() + "% (" + royaleGames30DaysWon.toInt() + "/" + royaleGames30DaysCount + ")"
        val thirtyDaysSuddenDeathWinRate = Math.round((suddenDeathGames30DaysWon / suddenDeathGames30DaysCount) * 100).toString() + "% (" + suddenDeathGames30DaysWon.toInt() + "/" + suddenDeathGames30DaysCount + ")"

        // Win rates
        val allTimeWinRates = Statistic()
        allTimeWinRates.playerId = editingPlayer!!.id!!
        allTimeWinRates.playerValue = " Win rates (all time):\n\t " +
                "Overall: " + overallWinRate + "\n\t " +
                "Royale: " + royaleWinRate + "\n\t " +
                "Sudden Death: " + suddenDeathWinRate
        statistics.add(allTimeWinRates)

        val thirtyDayWinRates = Statistic()
        thirtyDayWinRates.playerId = editingPlayer!!.id!!
        thirtyDayWinRates.playerValue = " Win rates (30 days):\n\t " +
                "Overall: " + thirtyDaysOverallWinRate + "\n\t " +
                "Royale: " + thirtyDaysRoyaleWinRate + "\n\t " +
                "Sudden Death: " + thirtyDaysSuddenDeathWinRate
        statistics.add(thirtyDayWinRates)

        // Best/Worst vs player
        var bestVsPlayer: Player? = null
        var bestVsPlayerWinRate = 0f
        var bestVsPlayerNumGames = 0
        var worstVsPlayer: Player? = null
        var worstVsPlayerWinRate = 0f
        var worstVsPlayerNumGames = 0

        val averageGamesPlayedByPlayers = games.size / players.size

        players.forEachIndexed { _, player ->
            if(player.id != editingPlayer!!.id && !player.isHidden) {
                val numGamesWithThisPlayer = games.count { it.players.any { it.player?.id == player.id } }
                val numGamesThisPlayerWon: Float = games.count { it.players.any { it.player?.id == player.id && it.winner } }.toFloat()
                val numGamesIWonVsThisPlayer: Float = games.count { it.players.any { it.player?.id == player.id } && it.players.any { it.player?.id == editingPlayer!!.id && it.winner } }.toFloat()

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
            bestVsPlayerStat.playerId = editingPlayer!!.id!!
            bestVsPlayerStat.playerValue = " Best vs " + bestVsPlayer?.name + " (won " + Math.round(bestVsPlayerWinRate * 100) + "% of " + bestVsPlayerNumGames + " games)"
            statistics.add(bestVsPlayerStat)
        }

        if(worstVsPlayer != null) {
            val worstVsPlayerStat = Statistic()
            worstVsPlayerStat.playerId = editingPlayer!!.id!!
            worstVsPlayerStat.playerValue = " Worst vs " + worstVsPlayer?.name + " (lost " + Math.round(worstVsPlayerWinRate * 100) + "% of " + worstVsPlayerNumGames + " games)"
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

        val averageGamesPlayed = (0..CharacterHelper.getNumberOfCharacters()).sumBy {
            var characterId = it
            games.count { it.players.any { it.characterId == characterId && it.player!!.id != editingPlayer!!.id } } } / 58

        (0..CharacterHelper.getNumberOfCharacters()).forEachIndexed { _, characterId ->
            val numGamesWithThisCharacter = games.count { it.players.any { it.characterId == characterId && it.player!!.id != editingPlayer!!.id } }
            val numGamesThisCharacterWon: Float = games.count { it.players.any { it.characterId == characterId && it.player!!.id != editingPlayer!!.id && it.winner } }.toFloat()
            val numGamesIWonVsThisCharacter: Float = games.count { it.players.any { it.characterId == characterId && it.player!!.id != editingPlayer!!.id } && it.players.any { it.player?.id == editingPlayer?.id && it.winner } }.toFloat()

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
            bestVsCharacterStat.playerId = editingPlayer!!.id!!
            bestVsCharacterStat.playerValue = " Best vs " + CharacterHelper.getName(bestVsCharacterId!!) + " (won " + Math.round(bestVsCharacterWinRate * 100) + "% of " + bestVsCharacterNumGames + " games)"
            statistics.add(bestVsCharacterStat)
        }

        if(worstVsCharacterId != null) {
            val worstVsCharacterStat = Statistic()
            worstVsCharacterStat.playerId = editingPlayer!!.id!!
            worstVsCharacterStat.playerValue = " Worst vs " + CharacterHelper.getName(worstVsCharacterId!!) + " (lost " + Math.round(worstVsCharacterWinRate * 100) + "% of " + worstVsCharacterNumGames + " games)"
            statistics.add(worstVsCharacterStat)
        }

        // Streaks
        val streaks = Statistic()
        streaks.playerId = editingPlayer!!.id!!
        streaks.playerValue = " Streaks:\n\t " +
                "Win streak (all games): " + getLongestWinStreak(null) + "\n\t " +
                "Losing streak (all games): " + getLongestLosingStreak(null) + "\n\t " +
                "Royale win streak: " + getLongestWinStreak(GameType.ROYALE) + "\n\t " +
                "Royale losing streak: " + getLongestLosingStreak(GameType.ROYALE) + "\n\t " +
                "Sudden Death win streak: " + getLongestWinStreak(GameType.SUDDEN_DEATH) + "\n\t " +
                "Sudden Death losing streak: " + getLongestLosingStreak(GameType.SUDDEN_DEATH)
        statistics.add(streaks)

        getCharacterStatistics()

        val bestRoyaleCharacters = Statistic()
        bestRoyaleCharacters.playerId = editingPlayer!!.id!!
        bestRoyaleCharacters.playerValue = " Best Royale characters:\n\t " + this.bestRoyaleCharacters.take(5).joinToString("\n\t ") { CharacterHelper.getName(it.characterId) + " (" + Math.round(it.getRoyaleWinRate() * 100) + "%) (" + it.royaleWins.toInt() + "/" + it.getTotalRoyaleGames().toInt() + ")" }
        statistics.add(bestRoyaleCharacters)

        val bestSuddenDeathCharacters = Statistic()
        bestSuddenDeathCharacters.playerId = editingPlayer!!.id!!
        bestSuddenDeathCharacters.playerValue = " Best Sudden Death characters:\n\t " + this.bestSuddenDeathCharacters.take(5).joinToString("\n\t ") { CharacterHelper.getName(it.characterId) + " (" + Math.round(it.getSuddenDeathWinRate() * 100) + "%) (" + it.suddenDeathWins.toInt() + "/" + it.getTotalSuddenDeathGames().toInt() + ")" }
        statistics.add(bestSuddenDeathCharacters)

        val allRoyaleCharacters = Statistic()
        allRoyaleCharacters.playerId = editingPlayer!!.id!!
        allRoyaleCharacters.playerValue = " All Royale characters:\n\t " + ArrayList(characterStats.filter { it.hasRoyaleGames() }).joinToString("\n\t ") { CharacterHelper.getName(it.characterId) + " (" + Math.round(it.getRoyaleWinRate() * 100) + "%) (" + it.royaleWins.toInt() + "/" + it.getTotalRoyaleGames().toInt() + ")" }
        statistics.add(allRoyaleCharacters)

        val allSuddenDeathCharacters = Statistic()
        allSuddenDeathCharacters.playerId = editingPlayer!!.id!!
        allSuddenDeathCharacters.playerValue = " All Sudden Death characters:\n\t " + ArrayList(characterStats.filter { it.hasSuddenDeathGames() }).joinToString("\n\t ") { CharacterHelper.getName(it.characterId) + " (" + Math.round(it.getSuddenDeathWinRate() * 100) + "%) (" + it.suddenDeathWins.toInt() + "/" + it.getTotalSuddenDeathGames().toInt() + ")" }
        statistics.add(allSuddenDeathCharacters)

        setupStatisticsAdapter(statistics)
    }

    private fun createPlayer() {
        var playerName = nameEditText!!.text.toString()
        if (playerName.isNotEmpty()) {
            var player = Player()
            player.name = playerName
            player.id = Calendar.getInstance().timeInMillis.toString()
            player.isHidden = isPlayerHidden
            player.isLowPriority = isPlayerLowPriority

            db.getReference(context = this)
                    .child("players")
                    .child(player.id!!)
                    .setValue(player)
                    .addOnCompleteListener {
                        onBackPressed()
                    }
        }
        else {
            showDialog("Set a player name.")
        }
    }

    private fun updatePlayer(goBack: Boolean) {
        var playerName = nameEditText!!.text.toString()
        if (playerName.isNotEmpty()) {
            editingPlayer!!.name = playerName
            editingPlayer!!.isHidden = isPlayerHidden
            editingPlayer!!.isLowPriority = isPlayerLowPriority

            db.getReference(context = this)
                .child("players")
                .child(editingPlayer!!.id!!)
                .setValue(editingPlayer)
                .addOnCompleteListener {
                    if(goBack) {
                        onBackPressed()
                    }
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
                    .child(editingPlayer!!.id!!)
                    .removeValue()
                    .addOnCompleteListener {
                        onBackPressed()
                    }
        }
        builder.setNegativeButton(android.R.string.cancel, { dialog, _ -> dialog.dismiss() })
        builder.show()
    }

    override fun setContentShown(shown: Boolean) {
        progressBar!!.visibility = if(shown) View.GONE else View.VISIBLE
        recyclerView!!.visibility = if(shown) View.VISIBLE else View.GONE
    }

    private fun getCharacterStatistics() {
        val gamesForCharacters = HashMap<Int, List<Game>>()
        characterStats.clear()

        for(id in 0..CharacterHelper.getNumberOfCharacters()) {
            val gamesForCharacter = games.filter {
                it.players.any { it.characterId == id && it.player!!.id == editingPlayer!!.id }
            }

            gamesForCharacters.put(id, gamesForCharacter)
        }

        gamesForCharacters.forEach { id, games ->
            if(games.isNotEmpty()) {
                val stat = CharacterStats()
                stat.playerId = editingPlayer!!.id!!
                stat.characterId = id

                val royaleGames = games.filter { it.gameType == GameType.ROYALE.toString() }
                stat.royaleWins = royaleGames.count { it.players.any { it.characterId == id && it.winner } }.toDouble()
                stat.royaleLosses = royaleGames.size - stat.royaleWins

                val suddenDeathGames = games.filter { it.gameType == GameType.SUDDEN_DEATH.toString() }
                stat.suddenDeathWins = suddenDeathGames.count { it.players.any { it.characterId == id && it.winner } }.toDouble()
                stat.suddenDeathLosses = suddenDeathGames.size - stat.suddenDeathWins

                characterStats.add(stat)
            }
        }

        val allRoyaleGames = characterStats.count { it.hasRoyaleGames() }.toDouble()
        val allSuddenDeathGames = characterStats.count { it.hasSuddenDeathGames() }.toDouble()
        val royaleAverageWinRate = characterStats.sumByDouble { it.getRoyaleWinRate() } / allRoyaleGames
        val royaleAverageGamesPlayed = characterStats.sumByDouble { it.getTotalRoyaleGames() } / allRoyaleGames
        val suddenDeathAverageWinRate = characterStats.sumByDouble { it.getSuddenDeathWinRate() } / allSuddenDeathGames
        val suddenDeathAverageGamesPlayed = characterStats.sumByDouble { it.getTotalSuddenDeathGames() } / allSuddenDeathGames

        bestRoyaleCharacters = ArrayList(characterStats.filter { it.hasRoyaleGames() }.filter { ((it.getTotalRoyaleGames()) >= royaleAverageGamesPlayed) && (it.getRoyaleWinRate() >= royaleAverageWinRate) }.sortedByDescending { it.getRoyaleWinRate() })
        if(bestRoyaleCharacters.size == 0) {
            bestRoyaleCharacters = ArrayList(characterStats.filter { it.hasRoyaleGames() }.filter { it.getRoyaleWinRate() > 0 }.sortedByDescending { it.getRoyaleWinRate() })
        }
        if(bestRoyaleCharacters.size == 0) {
            bestRoyaleCharacters = ArrayList(characterStats.filter { it.hasRoyaleGames() }.sortedBy { it.getTotalRoyaleGames() })
        }

        bestSuddenDeathCharacters = ArrayList(characterStats.filter { it.hasSuddenDeathGames() }.filter { ((it.getTotalSuddenDeathGames()) >= suddenDeathAverageGamesPlayed) && (it.getSuddenDeathWinRate() >= suddenDeathAverageWinRate) }.sortedByDescending { it.getSuddenDeathWinRate() })
        if(bestSuddenDeathCharacters.size == 0) {
            bestSuddenDeathCharacters = ArrayList(characterStats.filter { it.hasSuddenDeathGames() }.filter { it.getSuddenDeathWinRate() > 0 }.sortedByDescending { it.getSuddenDeathWinRate() })
        }
        if(bestSuddenDeathCharacters.size == 0) {
            bestSuddenDeathCharacters = ArrayList(characterStats.filter { it.hasSuddenDeathGames() }.sortedBy { it.getTotalSuddenDeathGames() })
        }

        worstRoyaleCharacters = ArrayList(characterStats.filter { it.hasRoyaleGames() }.filter { ((it.getTotalRoyaleGames()) >= royaleAverageGamesPlayed) && (it.getRoyaleWinRate() >= royaleAverageWinRate) }.sortedByDescending { it.getRoyaleWinRate() })
        if(worstRoyaleCharacters.size == 0) {
            worstRoyaleCharacters = ArrayList(characterStats.filter { it.hasRoyaleGames() }.filter { it.getRoyaleWinRate() > 0 }.sortedByDescending { it.getRoyaleWinRate() })
        }
        if(worstRoyaleCharacters.size == 0) {
            worstRoyaleCharacters = ArrayList(characterStats.filter { it.hasRoyaleGames() }.sortedBy { it.getTotalRoyaleGames() })
        }

        worstSuddenDeathCharacters = ArrayList(characterStats.filter { it.hasSuddenDeathGames() }.filter { ((it.getTotalSuddenDeathGames()) >= suddenDeathAverageGamesPlayed) && (it.getSuddenDeathWinRate() >= suddenDeathAverageWinRate) }.sortedByDescending { it.getSuddenDeathWinRate() })
        if(worstSuddenDeathCharacters.size == 0) {
            worstSuddenDeathCharacters = ArrayList(characterStats.filter { it.hasSuddenDeathGames() }.filter { it.getSuddenDeathWinRate() > 0 }.sortedByDescending { it.getSuddenDeathWinRate() })
        }
        if(worstSuddenDeathCharacters.size == 0) {
            worstSuddenDeathCharacters = ArrayList(characterStats.filter { it.hasSuddenDeathGames() }.sortedBy { it.getTotalSuddenDeathGames() })
        }
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

    private fun showTooltips() {
        val config = ShowcaseConfig()
        config.fadeDuration = 50L

        val sequence = MaterialShowcaseSequence(this, "PlayerTooltip")
        sequence.setConfig(config)

        sequence.addSequenceItem(MaterialShowcaseView.Builder(this)
                .setTarget(create_player_visibility_button)
                .setDismissText(getString(R.string.tooltip_next))
                .setContentText(R.string.player_visibility_tooltip)
                .setDismissOnTouch(true)
                .build())

        sequence.addSequenceItem(MaterialShowcaseView.Builder(this)
                .setTarget(create_player_priority_button)
                .setDismissText(getString(R.string.tooltip_next))
                .setContentText(R.string.player_priority_tooltip)
                .setDismissOnTouch(true)
                .build())

        sequence.start()
    }

    private fun showSnackbar(text: String) {
        Snackbar.make(create_player_navigation, text, Snackbar.LENGTH_LONG)
                .setBackgroundColor(R.color.primary)
                .setTextAttributes(resources.getColor(R.color.text_primary, null), 20f)
                .show()
    }
}
