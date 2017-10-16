package com.stromberg.scott.seventenwouldstillsmash.fragment

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.adapter.CreateGamePlayersListAdapter
import com.stromberg.scott.seventenwouldstillsmash.model.Game
import com.stromberg.scott.seventenwouldstillsmash.model.GamePlayer
import com.stromberg.scott.seventenwouldstillsmash.model.Player
import com.stromberg.scott.seventenwouldstillsmash.util.CharacterHelper
import java.text.SimpleDateFormat
import java.util.*


class CreateGameFragment : Fragment() {
    private var db = FirebaseFirestore.getInstance()
    var dateFormatter = SimpleDateFormat("EEE, MMM d yyyy")

    private var game: Game = Game()
    private var royaleToggleWidth: Int = 0
    private var suddenDeathToggleWidth: Int = 0
    private var players = ArrayList<Player>()
    private var playersAdapter: CreateGamePlayersListAdapter? = null

    private var contentView: View? = null
    private var dateTextView: TextView? = null
    private var royaleToggle: Button? = null
    private var suddenDeathToggle: Button? = null
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

        dateTextView!!.text = dateFormatter.format(Calendar.getInstance().time)
        dateTextView!!.setOnClickListener({
            var datePicker = DatePickerDialog(activity)
            datePicker.setOnDateSetListener { view, year, month, day ->
                val cal = Calendar.getInstance()
                cal.set(year, month, day)
                game.date = cal.time

                dateTextView!!.text = dateFormatter.format(game.date)
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

        setContentShown(false)
        getPlayers()

        return contentView
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
        playerNameText.onItemClickListener = AdapterView.OnItemClickListener( { _: AdapterView<*>, view: View, position: Int, _: Long -> gamePlayer.player = playerList.find { it.name.equals((view as TextView).text.toString()) } } )

        val characterNames = (0..57).map { CharacterHelper.getName(it) }
        val characterAdapter = ArrayAdapter<String>(activity, android.R.layout.select_dialog_item, characterNames)
        characterNameText.threshold = 1
        characterNameText.setAdapter<ArrayAdapter<String>>(characterAdapter)
        characterNameText.onItemClickListener = AdapterView.OnItemClickListener( { _: AdapterView<*>, view: View, position: Int, _: Long -> gamePlayer.characterId = CharacterHelper.getId((view as TextView).text.toString()) } )

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
        builder.setPositiveButton(if(editingPlayer != null) "Save" else "Add Player", { dialog, _ -> if(editingPlayer == null) addPlayerToGame(gamePlayer); else playersList!!.adapter.notifyDataSetChanged(); dialog.dismiss() })

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
        db.collection("players")
        .orderBy("name")
        .get()
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result) {
                    var player = Player()
                    player.id = document.id
                    player.name = document.get("name").toString()
                    players.add(player)
                }
            } else {
                Snackbar.make(contentView!!, "Failed to load player data", Snackbar.LENGTH_SHORT).show()
            }

            setContentShown(true)
        }
    }

    private fun setContentShown(show: Boolean) {
        contentView!!.findViewById<View>(R.id.progress).visibility = if(show) View.GONE else View.VISIBLE
        contentView!!.findViewById<View>(R.id.content).visibility = if(show) View.VISIBLE else View.GONE
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