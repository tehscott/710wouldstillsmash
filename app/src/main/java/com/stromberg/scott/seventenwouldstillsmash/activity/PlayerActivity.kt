package com.stromberg.scott.seventenwouldstillsmash.activity

import androidx.appcompat.app.AlertDialog
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
    private var isPlayerHidden = false
    private var isPlayerLowPriority = false
    private var isFirstLoad = true

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

    //todo: why is this not used?
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

        recyclerView!!.adapter = gamesAdapter as RecyclerView.Adapter<*>
        recyclerView!!.adapter!!.notifyDataSetChanged()

        recyclerView!!.visibility = if (games.isEmpty()) View.GONE else View.VISIBLE
        emptyStateTextView.visibility = if (games.isEmpty() && editingPlayer != null) View.VISIBLE else View.GONE
    }

    private fun setupStatisticsAdapter(statistics: List<Statistic>) {
        statisticsAdapter = StatisticsListAdapter(statistics)

        recyclerView!!.adapter = statisticsAdapter as RecyclerView.Adapter<*>
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
                            val player: Player = it.getValue(Player::class.java)!!
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
                            val game: Game = it.getValue(Game::class.java)!!

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

        val gameTypes = HashMap<String?, Int>()

        games.map { it.gameType }.forEach { gameTypeId ->
            val gameType = GameTypeHelper.getGameType(gameTypeId)

            if(gameType != null && !gameType.isDeleted) {
                gameTypes[gameTypeId] = games.count { game -> game.gameType == gameTypeId }
            }
        }

        val gameTypesSorted = gameTypes.toList().sortedByDescending { (_, count) -> count}.take(2).map { it.first }

        val gamesThisPlayerPlayedAllTime = games.filter { it.players.any { player -> player.player?.id == editingPlayer!!.id } }
        val gamesThisPlayerWonAllTime: Float = (gamesThisPlayerPlayedAllTime.count { it.players.any { player -> player.player?.id == editingPlayer!!.id && player.winner } }).toFloat()
        val last30GamesThisPlayerPlayed = gamesThisPlayerPlayedAllTime.sortedByDescending { it.date }.take(30)
        val gamesThisPlayerWonOutOfLast30Games: Float = (last30GamesThisPlayerPlayed.count { it.players.any { player -> player.player?.id == editingPlayer!!.id && player.winner } }).toFloat()

        val allGameTypesOverallWinRate = Math.round(((gamesThisPlayerWonAllTime) / (gamesThisPlayerPlayedAllTime.size)) * 100).toString() + "% (" + (gamesThisPlayerWonAllTime).toInt() + "/" + gamesThisPlayerPlayedAllTime.size + ")"
        val allGameTypesLast30GamesWinRate = Math.round(((gamesThisPlayerWonOutOfLast30Games) / (last30GamesThisPlayerPlayed.size)) * 100).toString() + "% (" + (gamesThisPlayerWonOutOfLast30Games).toInt() + "/" + last30GamesThisPlayerPlayed.size + ")"

        val top2GameTypeAllTimeStats = HashMap<String?, String>()
        val top2GameTypeLast30GamesStats = HashMap<String?, String>()
        gameTypesSorted.forEach { gameTypeId ->
            val gamesThisCharacterPlayedForThisGameTypeAllTime = gamesThisPlayerPlayedAllTime.filter { game -> game.gameType == gameTypeId }
            val gamesThisCharacterWonForThisGameTypeAllTimeCount = gamesThisCharacterPlayedForThisGameTypeAllTime.count { it.players.any { player -> player.player?.id == editingPlayer!!.id && player.winner } }
            val thisGameTypeAllTimeWinRateDouble  = Math.round((gamesThisCharacterWonForThisGameTypeAllTimeCount / gamesThisCharacterPlayedForThisGameTypeAllTime.size.toDouble()) * 100)
            val thisGameTypeAllTimeWinRate = thisGameTypeAllTimeWinRateDouble.toString() + "% (" + gamesThisCharacterWonForThisGameTypeAllTimeCount + "/" + gamesThisCharacterPlayedForThisGameTypeAllTime.size + ")"

            val last30GamesThisPlayerPlayedForThisGameType = gamesThisPlayerPlayedAllTime.filter{ game -> game.gameType == gameTypeId }.sortedByDescending { it.date }.take(30)
            val gamesThisCharacterWonForThisGameType30GamesCount = last30GamesThisPlayerPlayedForThisGameType.count { it.players.any { player -> player.player?.id == editingPlayer!!.id && player.winner } }
            val thisGameTypeLast30GamesWinRateDouble = Math.round((gamesThisCharacterWonForThisGameType30GamesCount / last30GamesThisPlayerPlayedForThisGameType.size.toDouble()) * 100)
            val thisGameTypeLast30GamesWinRate = thisGameTypeLast30GamesWinRateDouble.toString() + "% (" + gamesThisCharacterWonForThisGameType30GamesCount + "/" + last30GamesThisPlayerPlayedForThisGameType.size + ")"

            if(gamesThisCharacterPlayedForThisGameTypeAllTime.isNotEmpty()) {
                top2GameTypeAllTimeStats[gameTypeId] = thisGameTypeAllTimeWinRate
            }

            if(last30GamesThisPlayerPlayedForThisGameType.isNotEmpty()) {
                top2GameTypeLast30GamesStats[gameTypeId] = thisGameTypeLast30GamesWinRate
            }
        }

        val allTimeWinRates = Statistic()
        allTimeWinRates.playerId = editingPlayer!!.id!!
        allTimeWinRates.playerValue = " Win rates (all games):\n\t " +
                "Overall: " + allGameTypesOverallWinRate + "\n\t "

        top2GameTypeAllTimeStats.forEach { gameTypeId, winRateText ->
            val gameType = GameTypeHelper.getGameType(gameTypeId)

            gameType?.let {
                allTimeWinRates.playerValue += "${it.name}: $winRateText\n\t "
            }
        }
        allTimeWinRates.playerValue = allTimeWinRates.playerValue.removeSuffix("\n\t ")
        statistics.add(allTimeWinRates)

        val thirtyGameWinRates = Statistic()
        thirtyGameWinRates.playerId = editingPlayer!!.id!!
        thirtyGameWinRates.playerValue = " Win rates (last 30 games):\n\t " +
                "Overall: " + allGameTypesLast30GamesWinRate + "\n\t "

        top2GameTypeLast30GamesStats.forEach { gameTypeId, winRateText ->
            val gameType = GameTypeHelper.getGameType(gameTypeId)

            gameType?.let {
                thirtyGameWinRates.playerValue += "${it.name}: $winRateText\n\t "
            }
        }
        thirtyGameWinRates.playerValue = thirtyGameWinRates.playerValue.removeSuffix("\n\t ")

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

            if(bestVsPlayerWinRate > 0) {
                bestVsPlayerStat.playerValue = " Best vs " + bestVsPlayer?.name + " (won " + Math.round(bestVsPlayerWinRate * 100) + "% of " + bestVsPlayerNumGames + " games)"
            }
            else {
                bestVsPlayerStat.playerValue = " Best vs no players...git gud nerd"
            }

            statistics.add(bestVsPlayerStat)
        }

        if(worstVsPlayer != null) {
            val worstVsPlayerStat = Statistic()
            worstVsPlayerStat.playerId = editingPlayer!!.id!!

            if(worstVsPlayerWinRate > 0) {
                worstVsPlayerStat.playerValue = " Worst vs " + worstVsPlayer?.name + " (lost " + Math.round(worstVsPlayerWinRate * 100) + "% of " + worstVsPlayerNumGames + " games)"
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

        val averageGamesPlayed = (0..CharacterHelper.getNumberOfCharacters()).sumBy {
            val characterId = it
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

            if(bestVsCharacterWinRate > 0) {
                bestVsCharacterStat.playerValue = " Best vs " + CharacterHelper.getName(bestVsCharacterId!!) + " (won " + Math.round(bestVsCharacterWinRate * 100) + "% of " + bestVsCharacterNumGames + " games)"
            }
            else {
                bestVsCharacterStat.playerValue = " Best vs no characters...git gud nerd"
            }

            statistics.add(bestVsCharacterStat)
        }

        if(worstVsCharacterId != null) {
            val worstVsCharacterStat = Statistic()
            worstVsCharacterStat.playerId = editingPlayer!!.id!!

            if(worstVsCharacterWinRate > 0) {
                worstVsCharacterStat.playerValue = " Worst vs " + CharacterHelper.getName(worstVsCharacterId!!) + " (lost " + Math.round(worstVsCharacterWinRate * 100) + "% of " + worstVsCharacterNumGames + " games)"
            }
            else {
                worstVsCharacterStat.playerValue = " Worst vs no characters...you are supreme"
            }

            statistics.add(worstVsCharacterStat)
        }

        // Streaks
        if(gamesThisPlayerPlayedAllTime.isNotEmpty()) {
            val allGamesStreak = getCurrentStreak(null)

            val streaks = Statistic()
            streaks.playerId = editingPlayer!!.id!!
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


        if(gamesThisPlayerPlayedAllTime.isNotEmpty()) {
            getCharacterStats(statistics)
        }

        setupStatisticsAdapter(statistics)
    }

    private fun createPlayer() {
        val playerName = nameEditText!!.text.toString()
        if (playerName.isNotEmpty()) {
            val player = Player()
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
        val playerName = nameEditText!!.text.toString()
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

    private fun getCharacterStats(statistics: ArrayList<Statistic>) {
        var statsString = " Character stats:\n"

        for(characterId in 0..CharacterHelper.getNumberOfCharacters()) {
            val gamesForCharacter = games.filter {
                it.players.any {player -> player.characterId == characterId && player.player!!.id == editingPlayer!!.id }
            }

            val gamesWonCount = gamesForCharacter.count { it.players.any { player -> player.characterId == characterId && player.winner } }
            val gamesWinPercentage = Math.round(gamesWonCount.toDouble() / gamesForCharacter.size.toDouble()) * 100

            statsString += "  ${CharacterHelper.getName(characterId)}: $gamesWonCount/${gamesForCharacter.size} ($gamesWinPercentage%)\n"
        }

        val stat = Statistic()
        stat.playerId = editingPlayer!!.id!!
        stat.playerValue = statsString
        statistics.add(stat)
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
            val didIWin = it.players.any { it.player!!.id == editingPlayer!!.id!! && it.winner }

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

        val sortedGames: List<Game> = if(gameTypeId?.isNotEmpty() == true) {
            games.filter { it.gameType.equals(gameTypeId, true) }.sortedBy { it.date }
        }
        else {
            games.sortedBy { it.date }
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

    private fun getLongestLosingStreak(gameTypeId: String?): Int {
        var lossCount = 0
        var longestLossStreak = 0

        val sortedGames: List<Game> = if(gameTypeId?.isNotEmpty() == true) {
            games.filter { it.gameType.equals(gameTypeId, true) }.sortedBy { it.date }
        }
        else {
            games.sortedBy { it.date }
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
