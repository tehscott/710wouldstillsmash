package com.stromberg.scott.seventenwouldstillsmash.activity

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.adapter.CharacterPagerAdapter
import com.stromberg.scott.seventenwouldstillsmash.adapter.CreateGamePlayersListAdapter
import com.stromberg.scott.seventenwouldstillsmash.adapter.PlayerPagerAdapter
import com.stromberg.scott.seventenwouldstillsmash.model.*
import com.stromberg.scott.seventenwouldstillsmash.util.*
import com.stromberg.scott.seventenwouldstillsmash.view.VelocityViewPager
import kotlinx.android.synthetic.main.activity_game.*
import java.text.SimpleDateFormat
import java.util.*

class GameActivity : BaseActivity() {
    private var db = FirebaseDatabase.getInstance()
    private var dateFormatter = SimpleDateFormat("EEE, MMM d yyyy", Locale.getDefault())

    private var game: Game = Game()
    private var lastGame: Game? = null
    private var isEdit: Boolean = false
    private var hasMadeEdit: Boolean = false
    private var players = ArrayList<Player>()
    private lateinit var playersAdapter: CreateGamePlayersListAdapter
    private var topFiveCharacters = HashMap<String, ArrayList<Int>>()

    private var addPlayerDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        isEdit = intent?.extras?.containsKey("Game") ?: false

        if(isEdit && intent?.extras != null) {
            game = intent.extras!!.getParcelable("Game")!!
        }

        if(intent?.extras != null) {
            topFiveCharacters = intent.extras!!.getSerializable("TopCharacters") as HashMap<String, ArrayList<Int>>
            topFiveCharacters.forEach { it.value.sort() }

            if(intent?.hasExtra("LastGame") == true) {
                lastGame = intent.extras!!.getParcelable("LastGame")
            }
        }

        create_game_date.text = dateFormatter.format(Date(game.date))
        create_game_date.setOnClickListener {
            val datePicker = DatePickerDialog(this)
            datePicker.setOnDateSetListener { _, year, month, day ->
                val cal = Calendar.getInstance()
                cal.set(year, month, day)
                game.date = cal.time.time

                create_game_date.text = dateFormatter.format(Date(game.date))
                hasMadeEdit = true
            }
            datePicker.show()
        }

        game_title_text.text = if (isEdit) "Edit Game" else "New Game"

        create_game_players_title.setOnClickListener { addPlayer(null) }

        updateAdapter()
        create_game_players_list.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val dividerItemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        create_game_players_list.addItemDecoration(dividerItemDecoration)

        if(isEdit) {
            delete_button.setOnClickListener { deleteGame(true,false) }
            save_button.setOnClickListener { updateGame() }
        }
        else {
            delete_button.visibility = View.GONE
            save_button.setOnClickListener { createGame() }
        }

        setContentShown(false)
        getPlayers()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if(hasMadeEdit) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Save Changes?")
            builder.setMessage("Do you want to save your changes?")
            builder.setPositiveButton("Save") { dialog, _ -> save_button.performClick(); dialog.dismiss() }
            builder.setNegativeButton("No") { dialog, _ -> super.onBackPressed(); dialog.dismiss() }
            builder.setNeutralButton("Cancel") { dialog, _ -> dialog.dismiss() }
            builder.show()
        }
        else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 6969) {
            val gameTypeId = data?.extras?.getString("gameTypeId", null)

            if(gameTypeId != null) {
                hasMadeEdit = true
            }
        }
    }

    private fun updateAdapter() {
        val sortedPlayers = game.players.sortedBy { it.player.name }.sortedByDescending { it.winner }
        playersAdapter = CreateGamePlayersListAdapter(sortedPlayers, fun(position: Int) { addPlayer(sortedPlayers[position]) })
        create_game_players_list.adapter = playersAdapter
        create_game_players_list.adapter?.notifyDataSetChanged()
    }

    private fun updateGame() {
        if(!game.id.isBlank()) {
            if(game.players.size > 1) {
                deleteGame(false, true)
                createGame()
            }
            else {
                showDialog("There must be more than one player.")
            }
        }
        else {
            showDialog("Something went wrong (you almost deleted all the games, you fool), try again.")
        }
    }

    private fun deleteGame(goBack: Boolean, force: Boolean) {
        if(force) {
            doDeleteGame(goBack)
        }
        else {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete game")
            builder.setMessage("You can't undo this. Are you sure?")
            builder.setPositiveButton(android.R.string.ok) { _, _ ->
                doDeleteGame(goBack)
            }
            builder.setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
            builder.show()
        }
    }

    private fun doDeleteGame(goBack: Boolean) {
        setContentShown(false)

        db.getReference(context = this)
            .child("games")
            .child(game.id)
            .removeValue()
            .addOnCompleteListener {
                if (goBack) {
                    onBackPressed()
                }
            }
    }

    private fun createGame() {
        if(game.players.size > 1) {
            setContentShown(false)

            db.getReference(context = this)
                    .child("games")
                    .child(Calendar.getInstance().timeInMillis.toString())
                    .setValue(game)
                    .addOnCompleteListener {
                        hasMadeEdit = false
                        onBackPressed()
                    }
                    .addOnFailureListener {
                        Snackbar.make(content, "Failed to create game", Snackbar.LENGTH_LONG)
                                .setBackgroundColor(R.color.primary)
                                .setTextAttributes(resources.getColor(R.color.text_primary, null), 20f)
                                .show()
                    }
        }
        else {
            showDialog("There must be more than one player.")
        }
    }

    private fun addPlayer(editingPlayer: GamePlayer?) {
        val characters = Characters.values().sortedBy { it.characterName }
        val gamePlayer = editingPlayer ?: GamePlayer()
        val layout = layoutInflater.inflate(R.layout.create_game_players_dialog, null)
        val isWinnerCheckbox = layout.findViewById<CheckBox>(R.id.create_game_players_dialog_winner)
        val editingCharacterId = gamePlayer.characterId

        val unusedPlayers = getUnusedPlayers(gamePlayer)
        val playerPager = layout.findViewById<VelocityViewPager>(R.id.player_view_pager)
        playerPager.offscreenPageLimit = 10
        playerPager.pageMargin = 2
        playerPager.adapter = PlayerPagerAdapter(this@GameActivity, unusedPlayers)
        playerPager.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                val width = playerPager.measuredWidth
                val desiredWidth = 96.toPx
                val padding = (width - desiredWidth) / 2
                playerPager.setPadding(padding, 0, padding, 0)

                playerPager.viewTreeObserver.removeOnPreDrawListener(this)

                return true
            }
        })
        editingPlayer?.player?.id?.let { playerToSelectId ->
            playerPager.currentItem = players.indexOfFirst { it.id == playerToSelectId }
        }

        val characterPager = layout.findViewById<VelocityViewPager>(R.id.character_view_pager)
        characterPager.offscreenPageLimit = 10
        characterPager.pageMargin = 2
        characterPager.adapter = CharacterPagerAdapter(this@GameActivity)
        characterPager.viewTreeObserver.addOnPreDrawListener(object: ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                val width = characterPager.measuredWidth
                val desiredWidth = 96.toPx
                val padding = (width - desiredWidth) / 2
                characterPager.setPadding(padding, 8.toPx, padding, 8.toPx)

                characterPager.viewTreeObserver.removeOnPreDrawListener(this)

                return true
            }
        })
        editingPlayer?.characterId?.let { characterToSelectId ->
            characterPager.currentItem = characters.indexOfFirst { it.id == characterToSelectId }
        }

        isWinnerCheckbox.isChecked = gamePlayer.winner

        val builder = AlertDialog.Builder(this)
        builder.setTitle(if(editingPlayer != null) "Edit Player" else "Add Player")
        builder.setView(layout)

        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            gamePlayer.characterId = editingCharacterId
            dialog.dismiss()
        }

        builder.setPositiveButton(if(editingPlayer != null) "Save" else "Add Player") { dialog, _ ->
            run {
                gamePlayer.player = unusedPlayers[playerPager.currentItem]
                gamePlayer.characterId = characters[characterPager.currentItem].id
                gamePlayer.winner = isWinnerCheckbox.isChecked

                if(gamePlayer.player.id.isEmpty()) {
                    gamePlayer.player.id = Calendar.getInstance().timeInMillis.toString()

                    // Save new player to firebase
                    db.getReference(context = this)
                            .child("players")
                            .child(gamePlayer.player.id)
                            .setValue(gamePlayer.player)
                            .addOnCompleteListener {
                                if(editingPlayer == null) {
                                    addPlayerToGame(gamePlayer)

                                    if(!players.any { it.id == gamePlayer.player.id }) {
                                        players.add(gamePlayer.player)
                                    }
                                } else {
                                    updateAdapter()
                                }

                                hasMadeEdit = true

                                dialog.dismiss()
                            }
                            .addOnFailureListener {
                                showDialog("Failed to add player.")
                            }
                }
                else {
                    if(editingPlayer == null) {
                        addPlayerToGame(gamePlayer)
                    }
                    else {
                        updateAdapter()
                    }

                    hasMadeEdit = true

                    dialog.dismiss()
                }
            }
        }

        if(editingPlayer != null) {
            builder.setNeutralButton("Delete") { dialog, _ ->
                if(gamePlayer.player.id.isEmpty()) {
                    players.remove(gamePlayer.player)
                    game.players.remove(gamePlayer)
                }

                game.players.remove(editingPlayer)
                updateAdapter()
                hasMadeEdit = true

                dialog.dismiss()
            }
        }

        addPlayerDialog = builder.create().also { it.show() }
    }

    // TODO use this still
    private fun getTopCharactersForPlayer(gamePlayer: GamePlayer): ArrayList<String> {
        val topCharactersForThisPlayer = ArrayList<String>()

        if (topFiveCharacters.containsKey(gamePlayer.player.id)) {
            topFiveCharacters[gamePlayer.player.id]!!.forEach {
                topCharactersForThisPlayer.add(Characters.byId(it)?.characterName.orEmpty())
            }
        }

        return topCharactersForThisPlayer
    }

    private fun getUnusedPlayers(gamePlayer: GamePlayer): ArrayList<Player> {
        val playerList = ArrayList<Player>()
        players.forEach { player ->
            val isCurrentlySelectedPlayer = gamePlayer.player.id == player.id
            val playerInUse = !isCurrentlySelectedPlayer && game.players.any { it.player.id == player.id }

            if (!playerInUse) {
                playerList.add(player)
            }
        }
        return playerList
    }

    private fun addPlayerToGame(player: GamePlayer) {
        game.players.add(player)
        updateAdapter()

        addPlayerDialog?.dismiss()
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
                        hasMadeEdit = false

                        addPlayersFromLastGame()

                        setContentShown(true)
                    }
                })
    }

    override fun setContentShown(shown: Boolean) {
        findViewById<View>(R.id.progress).visibility = if(shown) View.GONE else View.VISIBLE
        findViewById<View>(R.id.content).visibility = if(shown) View.VISIBLE else View.GONE
    }

    private fun addPlayersFromLastGame() {
        val prefs = getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE)
        val showPrompt = prefs.getBoolean(getString(R.string.shared_prefs_show_copy_last_game_prompt), true)

        if(!isEdit && lastGame != null && showPrompt) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Copy last game?")
            builder.setMessage("Do you want to copy the players from last game?")
            builder.setPositiveButton("Yes") { dialog, _ ->
                lastGame!!.players.sortedBy { it.player.name }.forEach { player ->
                    player.winner = false
                    game.players.add(player)
                }

                playersAdapter = CreateGamePlayersListAdapter(game.players, fun(position: Int) { addPlayer(game.players[position]) })
                create_game_players_list.adapter = playersAdapter
                playersAdapter.notifyDataSetChanged()

                dialog.dismiss()
            }
            builder.setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            builder.setNeutralButton("Stop Asking") { dialog, _ ->
                prefs.edit().putBoolean(getString(R.string.shared_prefs_show_copy_last_game_prompt), false).apply()

                dialog.dismiss()
            }
            builder.show()
        }
    }
}
