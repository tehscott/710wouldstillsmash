package com.stromberg.scott.seventenwouldstillsmash.fragment

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.*
import com.github.clans.fab.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.adapter.CreateGamePlayersListAdapter
import com.stromberg.scott.seventenwouldstillsmash.adapter.PlayersListAdapter
import com.stromberg.scott.seventenwouldstillsmash.model.Game
import com.stromberg.scott.seventenwouldstillsmash.model.GamePlayer
import com.stromberg.scott.seventenwouldstillsmash.model.GameType
import com.stromberg.scott.seventenwouldstillsmash.model.Player
import com.stromberg.scott.seventenwouldstillsmash.util.CharacterHelper
import java.text.SimpleDateFormat
import java.util.*

class CreateGameFragment : BaseFragment() {
    private var db = FirebaseDatabase.getInstance()
    var dateFormatter = SimpleDateFormat("EEE, MMM d yyyy")

    private var game: Game = Game()
    private var royaleToggleWidth: Int = 0
    private var suddenDeathToggleWidth: Int = 0
    private var isEdit: Boolean = false
    private var players = ArrayList<Player>()
    private var playersAdapter: CreateGamePlayersListAdapter? = null

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
            deleteButton?.setOnClickListener({ deleteGame(true) })
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
        deleteGame(false)
        createGame()
    }

    private fun deleteGame(goBack: Boolean) {
        setContentShown(false)

        db.reference
            .child("games")
            .child(game.id)
            .removeValue()
            .addOnCompleteListener( { if(goBack) { activity.onBackPressed() } } )
    }

    private fun createMissingPlayers() {
        game.players.forEach({  })
    }

    private fun createGame() {
        if(game.players.size > 1) {
            setContentShown(false)

            db.reference
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
            Snackbar.make(contentView!!, "Add some players", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun addPlayer(editingPlayer: GamePlayer?) {
        val gamePlayer = editingPlayer ?: GamePlayer()
        val layout = layoutInflater.inflate(R.layout.create_game_players_dialog, null)
        val playerNameText = layout.findViewById<AutoCompleteTextView>(R.id.create_game_players_dialog_player_name)
        val characterNameText = layout.findViewById<AutoCompleteTextView>(R.id.create_game_players_dialog_character_name)
        val isWinnerCheckbox = layout.findViewById<CheckBox>(R.id.create_game_players_dialog_winner)

        val playerList = ArrayList<Player>()
        players.forEach { player ->
            val playerInUse = game.players.any { it.player!! == player }

            if(!playerInUse) {
                playerList.add(player)
            }
        }

        val playerAdapter = ArrayAdapter<String>(activity, android.R.layout.select_dialog_item, playerList.map { it.name })
        playerNameText.threshold = 1
        playerNameText.setAdapter<ArrayAdapter<String>>(playerAdapter)
        playerNameText.onItemClickListener = AdapterView.OnItemClickListener( { _: AdapterView<*>, view: View, position: Int, _: Long ->
            run {
                gamePlayer.player = playerList.find { it.name.equals((view as TextView).text.toString()) }
                characterNameText.requestFocus()
            }
        })

        playerNameText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if(hasFocus) {
                playerNameText.showDropDown()
            }
        }

        playerNameText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(text: Editable?) {
                playerNameText.showDropDown()
            }
            override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) { }
        })

        val characterNames = (0..57).map { CharacterHelper.getName(it) }
        val characterAdapter = ArrayAdapter<String>(activity, android.R.layout.select_dialog_item, characterNames)
        characterNameText.threshold = 1
        characterNameText.setAdapter<ArrayAdapter<String>>(characterAdapter)
        characterNameText.onItemClickListener = AdapterView.OnItemClickListener( { _: AdapterView<*>, view: View, position: Int, _: Long -> gamePlayer.characterId = CharacterHelper.getId((view as TextView).text.toString()) } )

        characterNameText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if(hasFocus) {
                characterNameText.showDropDown()
            }
        }

        characterNameText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(text: Editable?) {
                characterNameText.showDropDown()
            }
            override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) { }
        })

        isWinnerCheckbox.setOnCheckedChangeListener { view, isChecked -> gamePlayer.winner = isChecked }

        if(editingPlayer != null) {
            playerNameText.setText(editingPlayer.player!!.name!!)
            characterNameText.setText(CharacterHelper.getName(editingPlayer.characterId))
            isWinnerCheckbox.isChecked = editingPlayer.winner
        }

        val builder = AlertDialog.Builder(activity)
        builder.setTitle(if(editingPlayer != null) "Edit Player" else "Add Player")
        builder.setView(layout)

        builder.setNegativeButton(android.R.string.cancel, { dialog, _ -> dialog.dismiss() })
        builder.setPositiveButton(if(editingPlayer != null) "Save" else "Add Player", { dialog, _ ->
            run {
                if(gamePlayer.player == null) {
                    var player = Player()
                    player.name = playerNameText.text.toString()
                    gamePlayer.player = player

                    db.reference
                        .child("games")
                        .child(Calendar.getInstance().timeInMillis.toString())
                        .setValue(player)
                        .addOnCompleteListener({
                            if(editingPlayer == null) {
                                addPlayerToGame(gamePlayer)
                            }
                            else {
                                playersList!!.adapter.notifyDataSetChanged()
                            }

                            dialog.dismiss()
                        })
                        .addOnFailureListener({
                            Snackbar.make(contentView!!, "Failed to add player", Snackbar.LENGTH_LONG).show()
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
            builder.setNeutralButton("Delete", { dialog, _ -> game.players.remove(editingPlayer); playersList!!.adapter.notifyDataSetChanged(); dialog.dismiss() })
        }

        addPlayerDialog = builder.create()
        addPlayerDialog!!.show()
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
        db.reference
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

                    setContentShown(true)
                }
            })
    }

    override fun setContentShown(show: Boolean) {
        contentView!!.findViewById<View>(R.id.progress).visibility = if(show) View.GONE else View.VISIBLE
        contentView!!.findViewById<View>(R.id.content).visibility = if(show) View.VISIBLE else View.GONE
    }

    override fun addFabClicked() {}

    override fun hasFab(): Boolean {
        return false
    }

    override fun getFabButtons(context: Context): List<FloatingActionButton> {
        return ArrayList<FloatingActionButton>()
    }

//    private fun isEdit(): Boolean {
//        return arguments != null && arguments.containsKey("isEdit") && arguments.getBoolean("isEdit")
//    }
//
//    private fun getGamePlayer(): GamePlayer {
//        if(arguments != null && arguments.containsKey("gamePlayer")) {
//            return arguments.get("gamePlayer") as GamePlayer
//        }
//
//        return GamePlayer()
//    }
}