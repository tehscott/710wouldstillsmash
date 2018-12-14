package com.stromberg.scott.seventenwouldstillsmash.activity

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.adapter.CreateGamePlayersListAdapter
import com.stromberg.scott.seventenwouldstillsmash.model.Game
import com.stromberg.scott.seventenwouldstillsmash.model.GamePlayer
import com.stromberg.scott.seventenwouldstillsmash.model.GameType
import com.stromberg.scott.seventenwouldstillsmash.model.Player
import com.stromberg.scott.seventenwouldstillsmash.util.CharacterHelper
import com.stromberg.scott.seventenwouldstillsmash.util.getReference
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.android.synthetic.main.activity_game.*
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig

class GameActivity : BaseActivity() {
    private var db = FirebaseDatabase.getInstance()
    var dateFormatter = SimpleDateFormat("EEE, MMM d yyyy")

    private var game: Game = Game()
    private var royaleToggleWidth: Int = 0
    private var suddenDeathToggleWidth: Int = 0
    private var isEdit: Boolean = false
    private var hasMadeEdit: Boolean = false
    private var players = ArrayList<Player>()
    private var playersAdapter: CreateGamePlayersListAdapter? = null
    private lateinit var topFiveCharacters: HashMap<String, ArrayList<Int>>

    private var dateTextView: TextView? = null
    private var royaleToggle: Button? = null
    private var suddenDeathToggle: Button? = null
    private var playersList: RecyclerView? = null
    private var addPlayerButton: TextView? = null
    private var addPlayerDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        dateTextView = findViewById(R.id.create_game_date)
        royaleToggle = findViewById(R.id.create_game_royale_toggle)
        suddenDeathToggle = findViewById(R.id.create_game_sudden_death_royale_toggle)
        playersList = findViewById(R.id.create_game_players_list)
        addPlayerButton = findViewById(R.id.create_game_players_title)

        isEdit = intent?.extras?.containsKey("Game") ?: false

        if(isEdit && intent?.extras != null) {
            game = intent.extras!!.getParcelable("Game")!!
        }
        else {
            game.gameType = GameType.ROYALE.toString()
            game.date = Calendar.getInstance().time.time
        }

        if(intent?.extras != null) {
            topFiveCharacters = intent.extras!!.getSerializable("TopCharacters") as HashMap<String, ArrayList<Int>>
            topFiveCharacters.forEach { it.value.sort() }
        }

        dateTextView!!.text = dateFormatter.format(Date(game.date))
        dateTextView!!.setOnClickListener {
            var datePicker = DatePickerDialog(this)
            datePicker.setOnDateSetListener { _, year, month, day ->
                val cal = Calendar.getInstance()
                cal.set(year, month, day)
                game.date = cal.time.time

                dateTextView!!.text = dateFormatter.format(Date(game.date))
                hasMadeEdit = true
            }
            datePicker.show()
        }

        game_title_text.text = if (isEdit) "Edit Game" else "New Game"

        setupToggle()

        addPlayerButton?.setOnClickListener { addPlayer(null) }

        playersAdapter = CreateGamePlayersListAdapter(game.players, fun(position: Int) { addPlayer(game.players[position]) })
        playersList!!.adapter = playersAdapter
        playersList!!.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val dividerItemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        playersList!!.addItemDecoration(dividerItemDecoration)

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

    override fun onResume() {
        super.onResume()

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

    private fun updateGame() {
        if(!game.id.isNullOrBlank()) {
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
            .child(game.id!!)
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
                        Snackbar.make(content, "Failed to create game", Snackbar.LENGTH_SHORT).show()
                    }
        }
        else {
            showDialog("There must be more than one player.")
        }
    }

    private fun addPlayer(editingPlayer: GamePlayer?) {
        val gamePlayer = editingPlayer ?: GamePlayer()
        val layout = layoutInflater.inflate(R.layout.create_game_players_dialog, null)
        val playerSpinner = layout.findViewById<Spinner>(R.id.create_game_players_dialog_player_spinner)
        val characterSpinner = layout.findViewById<Spinner>(R.id.create_game_players_dialog_character_spinner)
        val isWinnerCheckbox = layout.findViewById<CheckBox>(R.id.create_game_players_dialog_winner)
        val editingCharacterId = gamePlayer.characterId

        val playerNames = ArrayList<String>(getUnusedPlayers(gamePlayer).map { it.name })
        playerNames.add(0, "")
        playerNames.add(1, "Add Player")

        val playerAdapter = ArrayAdapter<String>(this, android.R.layout.select_dialog_item, playerNames)

        playerSpinner.adapter = playerAdapter

        if(editingPlayer != null) {
            val player = players.find { it.id.equals(editingPlayer.player!!.id) }
            playerSpinner.setSelection(getUnusedPlayers(gamePlayer).indexOf(player) + 2, true)

            setupCharacterDropdown(characterSpinner, gamePlayer)
            val topCharactersForThisPlayer = getTopCharactersForPlayer(editingPlayer)
            characterSpinner.setSelection(editingPlayer.characterId + topCharactersForThisPlayer.size, true)

            isWinnerCheckbox.isChecked = editingPlayer.winner
        }

        playerSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> characterSpinner.adapter = null
                    1 -> showNameEntryDialog(gamePlayer)
                    else -> {
                        gamePlayer.player = getUnusedPlayers(gamePlayer)[position - 2]
                        setupCharacterDropdown(characterSpinner, gamePlayer)
                    }
                }
            }
            override fun onNothingSelected(arg0: AdapterView<*>) {}
        }

        isWinnerCheckbox.setOnCheckedChangeListener { _, isChecked -> gamePlayer.winner = isChecked }

        val builder = AlertDialog.Builder(this)
        builder.setTitle(if(editingPlayer != null) "Edit Player" else "Add Player")
        builder.setView(layout)

        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            gamePlayer.characterId = editingCharacterId
            dialog.dismiss()
        }

        builder.setPositiveButton(if(editingPlayer != null) "Save" else "Add Player") { dialog, _ ->
            run {
                if(gamePlayer.player!!.id == null) {
                    gamePlayer.player!!.id = Calendar.getInstance().timeInMillis.toString()

                    db.getReference(context = this)
                            .child("players")
                            .child(gamePlayer.player!!.id!!)
                            .setValue(gamePlayer.player)
                            .addOnCompleteListener {
                                if(editingPlayer == null) {
                                    addPlayerToGame(gamePlayer)

                                    if(!players.any { it.id.equals(gamePlayer.player!!.id) }) {
                                        players.add(gamePlayer.player!!)
                                    }
                                } else {
                                    playersList!!.adapter!!.notifyDataSetChanged()
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
                        playersList!!.adapter!!.notifyDataSetChanged()
                    }

                    hasMadeEdit = true

                    dialog.dismiss()
                }
            }
        }

        if(editingPlayer != null) {
            builder.setNeutralButton("Delete") { dialog, _ ->
                if(gamePlayer.player!!.id == null) {
                    players.remove(gamePlayer.player!!)
                    game.players.remove(gamePlayer)
                }

                game.players.remove(editingPlayer)
                playersList!!.adapter!!.notifyDataSetChanged()
                hasMadeEdit = true

                dialog.dismiss()
            }
        }

        addPlayerDialog = builder.create()
        addPlayerDialog!!.show()
    }

    private fun showNameEntryDialog(gamePlayer: GamePlayer) {
        val eightDp = resources.getDimensionPixelSize(R.dimen.space_8dp)
        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val linearLayout = LinearLayout(this)
        linearLayout.layoutParams = layoutParams
        linearLayout.setPadding(eightDp, eightDp, eightDp, eightDp)
        val editText = EditText(this)
        editText.layoutParams = layoutParams
        linearLayout.addView(editText)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Player name")
        builder.setView(linearLayout)
        builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
            if(editText.text.toString().trim().isNotEmpty()) {
                if (gamePlayer.player == null) {
                    gamePlayer.player = Player()
                }

                gamePlayer.player!!.name = editText.text.toString()

                players.add(gamePlayer.player!!)
                players.sortByDescending { it.name }

                val playerSpinner = addPlayerDialog!!.findViewById<Spinner>(R.id.create_game_players_dialog_player_spinner)
                val playerList = getUnusedPlayers(gamePlayer)
                val playerNames = ArrayList<String>(playerList.map { it.name })
                playerNames.add(0, "")
                playerNames.add(1, "Add Player")

                val playerAdapter = ArrayAdapter<String>(this, android.R.layout.select_dialog_item, playerNames)
                playerSpinner.adapter = playerAdapter
                playerSpinner.setSelection(playerList.indexOf(gamePlayer.player!!) + 2, true)

                dialog.dismiss()
            }
            else {
                val playerSpinner = addPlayerDialog!!.findViewById<Spinner>(R.id.create_game_players_dialog_player_spinner)
                playerSpinner.setSelection(0)
                dialog.dismiss()
            }
        }
        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            val playerSpinner = addPlayerDialog!!.findViewById<Spinner>(R.id.create_game_players_dialog_player_spinner)
            playerSpinner.setSelection(0)
            dialog.dismiss()
        }
        builder.show()
    }

    private fun setupCharacterDropdown(characterSpinner: Spinner, gamePlayer: GamePlayer) {
        val characterNames = (0..CharacterHelper.getNumberOfCharacters()).mapNotNull { CharacterHelper.getName(it) } as ArrayList<String>
        val topCharactersForThisPlayer = getTopCharactersForPlayer(gamePlayer)

        val characterAdapter = CharacterNameAdapter(this, topCharactersForThisPlayer, characterNames)
        characterSpinner.adapter = characterAdapter
        characterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val characters = topFiveCharacters[gamePlayer.player?.id]

                if(characters != null) {
                    if (position < topCharactersForThisPlayer.size) {
                        gamePlayer.characterId = characters[position]
                    } else {
                        gamePlayer.characterId = position - characters.size
                    }
                }
                else {
                    gamePlayer.characterId = position
                }
            }
            override fun onNothingSelected(arg0: AdapterView<*>) {}
        }
    }

    private fun getTopCharactersForPlayer(gamePlayer: GamePlayer): ArrayList<String> {
        val topCharactersForThisPlayer = ArrayList<String>()

        if (topFiveCharacters.containsKey(gamePlayer.player?.id)) {
            topFiveCharacters[gamePlayer.player?.id]!!.forEach {
                topCharactersForThisPlayer.add(CharacterHelper.getName(it))
            }
        }

        return topCharactersForThisPlayer
    }

    private fun getUnusedPlayers(gamePlayer: GamePlayer): ArrayList<Player> {
        val playerList = ArrayList<Player>()
        players.forEach { player ->
            val isCurrentlySelectedPlayer = gamePlayer.player != null && gamePlayer.player!!.id.equals(player.id)
            val playerInUse = !isCurrentlySelectedPlayer && game.players.any { it.player!!.id.equals(player.id) }

            if (!playerInUse && !player.isHidden) {
                playerList.add(player)
            }
        }
        return playerList
    }

    private fun addPlayerToGame(player: GamePlayer) {
        game.players.add(player)
        playersAdapter!!.notifyDataSetChanged()
        playersList?.adapter?.notifyDataSetChanged()

        addPlayerDialog!!.dismiss()
    }

    private fun setupToggle() {
        royaleToggle!!.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                royaleToggleWidth = royaleToggle!!.measuredWidth
                royaleToggle!!.getViewTreeObserver().removeOnPreDrawListener(this)
                toggleMeasured()
                return false
            }
        })

        royaleToggle!!.setOnClickListener {
            royaleToggleClicked()
            hasMadeEdit = true
        }

        suddenDeathToggle!!.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                suddenDeathToggleWidth = suddenDeathToggle!!.measuredWidth
                suddenDeathToggle!!.getViewTreeObserver().removeOnPreDrawListener(this)
                toggleMeasured()
                return false
            }
        })

        suddenDeathToggle!!.setOnClickListener {
            suddenDeathToggleClicked()
            hasMadeEdit = true
        }

        if(isEdit) {
            if(game.gameType.equals(GameType.ROYALE.toString())) {
                royaleToggle?.performClick()
            }
            else {
                suddenDeathToggle?.performClick()
            }
        }
    }

    private fun royaleToggleClicked() {
        game.gameType = "royale"
        royaleToggle!!.setBackgroundResource(R.drawable.toggle_left_selected_ripple)
        royaleToggle!!.setTextColor(resources.getColor(R.color.text_primary, null))
        suddenDeathToggle!!.setBackgroundResource(R.drawable.toggle_right_deselected_ripple)
        suddenDeathToggle!!.setTextColor(resources.getColor(R.color.text_secondary, null))
        fixTogglePadding()
    }

    private fun suddenDeathToggleClicked() {
        game.gameType = "sudden_death"
        royaleToggle!!.setBackgroundResource(R.drawable.toggle_left_deselected_ripple)
        royaleToggle!!.setTextColor(resources.getColor(R.color.text_secondary, null))
        suddenDeathToggle!!.setBackgroundResource(R.drawable.toggle_right_selected_ripple)
        suddenDeathToggle!!.setTextColor(resources.getColor(R.color.text_primary, null))
        fixTogglePadding()
    }

    private fun fixTogglePadding() {
        royaleToggle!!.setPadding(resources.getDimensionPixelSize(R.dimen.space_16dp),resources.getDimensionPixelSize(R.dimen.space_8dp),resources.getDimensionPixelSize(R.dimen.space_16dp),resources.getDimensionPixelSize(R.dimen.space_8dp))
        suddenDeathToggle!!.setPadding(resources.getDimensionPixelSize(R.dimen.space_16dp),resources.getDimensionPixelSize(R.dimen.space_8dp),resources.getDimensionPixelSize(R.dimen.space_16dp),resources.getDimensionPixelSize(R.dimen.space_8dp))
    }

    private fun toggleMeasured() {
        if(royaleToggleWidth > 0 && suddenDeathToggleWidth > 0) {
            var largestWidth = Math.max(royaleToggleWidth, suddenDeathToggleWidth)

            var royaleLP = royaleToggle!!.layoutParams
            royaleLP.width = largestWidth
            royaleToggle!!.layoutParams = royaleLP

            var suddenDeathLP = suddenDeathToggle!!.layoutParams
            suddenDeathLP.width = largestWidth
            suddenDeathToggle!!.layoutParams = suddenDeathLP
        }
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
                        hasMadeEdit = false

                        setContentShown(true)
                    }
                })
    }

    override fun setContentShown(shown: Boolean) {
        findViewById<View>(R.id.progress).visibility = if(shown) View.GONE else View.VISIBLE
        findViewById<View>(R.id.content).visibility = if(shown) View.VISIBLE else View.GONE
    }

    private class CharacterNameAdapter(context: Context, var recentCharacters: ArrayList<String>, var allCharacters: ArrayList<String>) : ArrayAdapter<String>(context, android.R.layout.select_dialog_item) {
        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var listItem = convertView
            if(listItem == null) {
                listItem = LayoutInflater.from(context).inflate(R.layout.character_name_list_item, null) as LinearLayout
            }

            listItem.findViewById<View>(R.id.divider).visibility = View.INVISIBLE

            if(position < recentCharacters.size) {
                listItem.findViewById<TextView>(R.id.name).text = recentCharacters[position]

                if(position == recentCharacters.size - 1) {
                    listItem.findViewById<View>(R.id.divider).visibility = View.VISIBLE
                }
            }
            else {
                listItem.findViewById<TextView>(R.id.name).text = allCharacters[position - recentCharacters.size]
            }

            return listItem
        }

        override fun getItem(position: Int): String {
            if(position < recentCharacters.size) {
                return recentCharacters[position]
            }
            else {
                return allCharacters[position - recentCharacters.size]
            }
        }

        override fun getCount(): Int {
            return recentCharacters.size + allCharacters.size
        }
    }

    private fun showTooltips() {
        val config = ShowcaseConfig()
        config.fadeDuration = 50L

        val sequence = MaterialShowcaseSequence(this, "GameTooltip")
        sequence.setConfig(config)

        sequence.addSequenceItem(MaterialShowcaseView.Builder(this)
                .setTarget(dateTextView)
                .setDismissText(getString(R.string.tooltip_next))
                .setContentText(R.string.date_tooltip)
                .setDismissOnTouch(true)
                .withRectangleShape()
                .build())

        sequence.addSequenceItem(MaterialShowcaseView.Builder(this)
                .setTarget(findViewById(R.id.toggle_container))
                .setDismissText(getString(R.string.tooltip_next))
                .setContentText(R.string.type_tooltip)
                .setDismissOnTouch(true)
                .withRectangleShape()
                .build())

        sequence.addSequenceItem(MaterialShowcaseView.Builder(this)
                .setTarget(addPlayerButton)
                .setDismissText(getString(R.string.tooltip_next))
                .setContentText(R.string.add_player_to_game_tooltip)
                .setDismissOnTouch(true)
                .withRectangleShape()
                .build())

        sequence.start()
    }
}
