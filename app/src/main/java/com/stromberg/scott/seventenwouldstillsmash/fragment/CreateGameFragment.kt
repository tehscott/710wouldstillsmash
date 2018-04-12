package com.stromberg.scott.seventenwouldstillsmash.fragment

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.view.ViewPager
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.adapter.CreateGamePlayersListAdapter
import com.stromberg.scott.seventenwouldstillsmash.model.*
import com.stromberg.scott.seventenwouldstillsmash.util.CharacterHelper
import com.stromberg.scott.seventenwouldstillsmash.util.getReference
import com.stromberg.scott.seventenwouldstillsmash.util.showDialog
import java.text.SimpleDateFormat
import java.util.*
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener



class CreateGameFragment : BaseFragment() {
    private var db = FirebaseDatabase.getInstance()
    var dateFormatter = SimpleDateFormat("EEE, MMM d yyyy")

    private var game: Game = Game()
    private var royaleToggleWidth: Int = 0
    private var suddenDeathToggleWidth: Int = 0
    private var isEdit: Boolean = false
    private var players = ArrayList<Player>()
    private var playersAdapter: CreateGamePlayersListAdapter? = null
    private lateinit var topFiveCharacters: HashMap<String, ArrayList<Int>>

    private var contentView: View? = null
    private var dateTextView: TextView? = null
    private var royaleToggle: Button? = null
    private var suddenDeathToggle: Button? = null
    private var createButton: Button? = null
    private var deleteButton: Button? = null
    private var playersList: RecyclerView? = null
    private var addPlayerButton: TextView? = null
    private var addPlayerDialog: AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        contentView = View.inflate(activity, R.layout.create_game, null)

        dateTextView = contentView!!.findViewById(R.id.create_game_date)
        royaleToggle = contentView!!.findViewById(R.id.create_game_royale_toggle)
        suddenDeathToggle = contentView!!.findViewById(R.id.create_game_sudden_death_royale_toggle)
        playersList = contentView!!.findViewById(R.id.create_game_players_list)
        addPlayerButton = contentView!!.findViewById(R.id.create_game_players_title)
        createButton = contentView!!.findViewById(R.id.create_game_create_button)
        deleteButton = contentView!!.findViewById(R.id.create_game_delete_button)

        isEdit = arguments?.containsKey("Game") ?: false

        if(isEdit) {
            game = arguments.getParcelable("Game")
        }
        else {
            game.gameType = GameType.ROYALE.toString()
            game.date = Calendar.getInstance().time.time
        }

        topFiveCharacters = arguments.getSerializable("TopCharacters") as HashMap<String, ArrayList<Int>>
        topFiveCharacters.forEach { it.value.sort() }

        dateTextView!!.text = dateFormatter.format(Date(game.date))
        dateTextView!!.setOnClickListener({
            var datePicker = DatePickerDialog(activity)
            datePicker.setOnDateSetListener { view, year, month, day ->
                val cal = Calendar.getInstance()
                cal.set(year, month, day)
                game.date = cal.time.time

                dateTextView!!.text = dateFormatter.format(Date(game.date))
            }
            datePicker.show()
        })

        setupToggle()

        addPlayerButton?.setOnClickListener({ addPlayer(null) })

        playersAdapter = CreateGamePlayersListAdapter(game.players, fun(position: Int) { addPlayer(game.players[position]) })
        playersList!!.adapter = playersAdapter
        playersList!!.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val dividerItemDecoration = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        playersList!!.addItemDecoration(dividerItemDecoration)

        contentView!!.findViewById<View>(R.id.create_game_cancel_button).setOnClickListener({ activity.onBackPressed() })

        if(isEdit) {
            deleteButton?.setOnClickListener({ deleteGame(true,false) })
            deleteButton?.visibility = View.VISIBLE

            createButton!!.setOnClickListener({ updateGame() })
            createButton!!.text = "Save Game"
        }
        else {
            createButton!!.setOnClickListener({ createGame() })
        }

        setContentShown(false)
        getPlayers()

        return contentView
    }

    private fun updateGame() {
        if(game.players.size > 1) {
            deleteGame(false, true)
            createGame()
        }
        else {
            showDialog("There must be more than one player.")
        }
    }

    private fun deleteGame(goBack: Boolean, force: Boolean) {
        if(force) {
            doDeleteGame(goBack)
        }
        else {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete game")
            builder.setMessage("You can't undo this. Are you sure?")
            builder.setPositiveButton(android.R.string.ok, { dialog, _ ->
                doDeleteGame(goBack)
            })
            builder.setNegativeButton(android.R.string.cancel, { dialog, _ -> dialog.dismiss() })
            builder.show()
        }
    }

    private fun doDeleteGame(goBack: Boolean) {
        setContentShown(false)

        db.getReference(activity)
                .child("games")
                .child(game.id)
                .removeValue()
                .addOnCompleteListener({
                    if (goBack) {
                        activity.onBackPressed()
                    }
                })
    }

    private fun createGame() {
        if(game.players.size > 1) {
            setContentShown(false)

            db.getReference(activity)
                .child("games")
                .child(Calendar.getInstance().timeInMillis.toString())
                .setValue(game)
                .addOnCompleteListener({
                    activity.onBackPressed()
                })
                .addOnFailureListener({
                    Snackbar.make(contentView!!, "Failed to create game", Snackbar.LENGTH_SHORT).show()
                })
        }
        else {
            showDialog("There must be more than one player.")
        }
    }

    private fun addPlayer(editingPlayer: GamePlayer?) {
        val prefs = activity.getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE)
        val gamePlayer = editingPlayer ?: GamePlayer()
        val layout = layoutInflater.inflate(R.layout.create_game_players_dialog, null)
        val playerSpinner = layout.findViewById<Spinner>(R.id.create_game_players_dialog_player_spinner)
        val characterSpinner = layout.findViewById<Spinner>(R.id.create_game_players_dialog_character_spinner)
        val isWinnerCheckbox = layout.findViewById<CheckBox>(R.id.create_game_players_dialog_winner)
        val editingCharacterId = gamePlayer.characterId

        val playerList = getUnusedPlayers(gamePlayer)
        val playerNames = ArrayList<String>(playerList.map { it.name })
        playerNames.add(0, "")
        playerNames.add(1, "Add Player")

        val playerAdapter = ArrayAdapter<String>(activity, android.R.layout.select_dialog_item, playerNames)

        playerSpinner.adapter = playerAdapter

        if(editingPlayer != null) {
            val player = players.find { it.id.equals(editingPlayer.player!!.id) }
            playerSpinner.setSelection(playerList.indexOf(player) + 2, true)

            setupCharacterDropdown(characterSpinner, gamePlayer, prefs)
            val topCharactersForThisPlayer = getTopCharactersForPlayer(editingPlayer)
            characterSpinner.setSelection(editingPlayer.characterId + topCharactersForThisPlayer.size, true)

            isWinnerCheckbox.isChecked = editingPlayer.winner
        }

        playerSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> characterSpinner.adapter = null
                    1 -> showNameEntryDialog(gamePlayer)
                    else -> {
                        gamePlayer.player = playerList[position - 2]
                        setupCharacterDropdown(characterSpinner, gamePlayer, prefs)
                    }
                }
            }
            override fun onNothingSelected(arg0: AdapterView<*>) {}
        }

        isWinnerCheckbox.setOnCheckedChangeListener { view, isChecked -> gamePlayer.winner = isChecked }

        val builder = AlertDialog.Builder(activity)
        builder.setTitle(if(editingPlayer != null) "Edit Player" else "Add Player")
        builder.setView(layout)

        builder.setNegativeButton(android.R.string.cancel, { dialog, _ ->
            gamePlayer.characterId = editingCharacterId
            dialog.dismiss()
        })
        builder.setPositiveButton(if(editingPlayer != null) "Save" else "Add Player", { dialog, _ ->
            run {
                if(gamePlayer.player!!.id == null) {
                    gamePlayer.player!!.id = Calendar.getInstance().timeInMillis.toString()

                db.getReference(activity)
                    .child("players")
                    .child(gamePlayer.player!!.id)
                    .setValue(gamePlayer.player)
                    .addOnCompleteListener({
                        if(editingPlayer == null) {
                            addPlayerToGame(gamePlayer)

                            if(!players.any { it.id.equals(gamePlayer.player!!.id) }) {
                                players.add(gamePlayer.player!!)
                            }
                        }
                        else {
                            playersList!!.adapter.notifyDataSetChanged()
                        }

                        dialog.dismiss()
                    })
                    .addOnFailureListener({
                        showDialog("Failed to add player.")
                    })
                }
                else {
                    if(editingPlayer == null) {
                        addPlayerToGame(gamePlayer)
                    }
                    else {
                        playersList!!.adapter.notifyDataSetChanged()
                    }

                    dialog.dismiss()
                }
            }
        })

        if(editingPlayer != null) {
            builder.setNeutralButton("Delete", { dialog, _ ->
                if(gamePlayer.player!!.id == null) {
                    players.remove(gamePlayer.player!!)
                    game.players.remove(gamePlayer)
                }

                game.players.remove(editingPlayer)
                playersList!!.adapter.notifyDataSetChanged(); dialog.dismiss()
            })
        }

        addPlayerDialog = builder.create()
        addPlayerDialog!!.show()
    }

    private fun showNameEntryDialog(gamePlayer: GamePlayer) {
        val eightDp = resources.getDimensionPixelSize(R.dimen.space_8dp)
        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val linearLayout = LinearLayout(context)
        linearLayout.layoutParams = layoutParams
        linearLayout.setPadding(eightDp, eightDp, eightDp, eightDp)
        val editText = EditText(context)
        editText.layoutParams = layoutParams
        linearLayout.addView(editText)

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Player name")
        builder.setView(linearLayout)
        builder.setPositiveButton(android.R.string.ok, { dialog, _ ->
            if(gamePlayer.player == null) {
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

            val playerAdapter = ArrayAdapter<String>(activity, android.R.layout.select_dialog_item, playerNames)
            playerSpinner.adapter = playerAdapter
            playerSpinner.setSelection(playerList.indexOf(gamePlayer.player!!) + 2, true)

            dialog.dismiss()
        })
        builder.setNegativeButton(android.R.string.cancel, { dialog, _ ->
            val playerSpinner = addPlayerDialog!!.findViewById<Spinner>(R.id.create_game_players_dialog_player_spinner)
            playerSpinner.setSelection(0)
            dialog.dismiss()
        })
        builder.show()
    }

    private fun setupCharacterDropdown(characterSpinner: Spinner, gamePlayer: GamePlayer, prefs: SharedPreferences) {
        val characterNames = (0..57).mapNotNull { CharacterHelper.getName(it) } as ArrayList<String>
        val topCharactersForThisPlayer = getTopCharactersForPlayer(gamePlayer)

        val characterAdapter = CharacterNameAdapter(context, topCharactersForThisPlayer, characterNames)
        characterSpinner.adapter = characterAdapter
        characterSpinner.onItemSelectedListener = object : OnItemSelectedListener {
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

        royaleToggle!!.setOnClickListener({
            game.gameType = "royale"
            royaleToggle!!.setBackgroundResource(R.drawable.toggle_left_selected_ripple)
            royaleToggle!!.setTextColor(resources.getColor(R.color.text_primary))
            suddenDeathToggle!!.setBackgroundResource(R.drawable.toggle_right_deselected_ripple)
            suddenDeathToggle!!.setTextColor(resources.getColor(R.color.text_secondary))
            fixTogglePadding()
        })

        suddenDeathToggle!!.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                suddenDeathToggleWidth = suddenDeathToggle!!.measuredWidth
                suddenDeathToggle!!.getViewTreeObserver().removeOnPreDrawListener(this)
                toggleMeasured()
                return false
            }
        })

        suddenDeathToggle!!.setOnClickListener({
            game.gameType = "sudden_death"
            royaleToggle!!.setBackgroundResource(R.drawable.toggle_left_deselected_ripple)
            royaleToggle!!.setTextColor(resources.getColor(R.color.text_secondary))
            suddenDeathToggle!!.setBackgroundResource(R.drawable.toggle_right_selected_ripple)
            suddenDeathToggle!!.setTextColor(resources.getColor(R.color.text_primary))
            fixTogglePadding()
        })

        if(isEdit) {
            if(game.gameType.equals(GameType.ROYALE.toString())) {
                royaleToggle?.performClick()
            }
            else {
                suddenDeathToggle?.performClick()
            }
        }
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
        db.getReference(activity)
            .child("players")
            .orderByKey()
            .addListenerForSingleValueEvent( object : ValueEventListener {
                override fun onCancelled(error: DatabaseError?) { }

                override fun onDataChange(snapshot: DataSnapshot?) {
                    snapshot?.children?.reversed()?.forEach {
                        var player: Player = it.getValue(Player::class.java)!!
                        player.id = it.key
                        players.add(player)
                    }

                    players.sortBy { it.name }

                    setContentShown(true)
                }
            })
    }

    override fun setContentShown(show: Boolean) {
        contentView!!.findViewById<View>(R.id.progress).visibility = if(show) View.GONE else View.VISIBLE
        contentView!!.findViewById<View>(R.id.content).visibility = if(show) View.VISIBLE else View.GONE
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
}