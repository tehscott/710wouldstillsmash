package com.stromberg.scott.seventenwouldstillsmash.fragment

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.adapter.ChooseCharacterListAdapter
import com.stromberg.scott.seventenwouldstillsmash.adapter.ChoosePlayerListAdapter
import com.stromberg.scott.seventenwouldstillsmash.model.Game
import com.stromberg.scott.seventenwouldstillsmash.model.GamePlayer
import com.stromberg.scott.seventenwouldstillsmash.model.Player
import com.stromberg.scott.seventenwouldstillsmash.util.CharacterHelper
import com.stromberg.scott.seventenwouldstillsmash.view.SquareLinearLayout
import java.text.SimpleDateFormat
import java.util.*

class CreateGameFragment : Fragment() {
    private var db = FirebaseFirestore.getInstance()
    var dateFormatter = SimpleDateFormat("EEE, MMM d yyyy")

    private var game: Game = Game()
    private var royaleToggleWidth: Int = 0
    private var suddenDeathToggleWidth: Int = 0
    private var players = ArrayList<Player>()
    private var gamePlayers = HashMap<Int, GamePlayer>()

    private var contentView: View? = null
    private var dateTextView: TextView? = null
    private var royaleToggle: Button? = null
    private var suddenDeathToggle: Button? = null

    private var addPlayer1Button: SquareLinearLayout? = null
    private var addPlayer2Button: SquareLinearLayout? = null
    private var addPlayer3Button: SquareLinearLayout? = null
    private var addPlayer4Button: SquareLinearLayout? = null
    private var addPlayer5Button: SquareLinearLayout? = null
    private var addPlayer6Button: SquareLinearLayout? = null
    private var addPlayer7Button: SquareLinearLayout? = null
    private var addPlayer8Button: SquareLinearLayout? = null
    private var setWinnerButton: SquareLinearLayout? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        contentView = View.inflate(activity, R.layout.create_game, null)

        dateTextView = contentView!!.findViewById<TextView>(R.id.create_game_date)
        royaleToggle = contentView!!.findViewById<Button>(R.id.create_game_royale_toggle)
        suddenDeathToggle = contentView!!.findViewById<Button>(R.id.create_game_sudden_death_royale_toggle)

        dateTextView!!.text = dateFormatter.format(Calendar.getInstance().time)
        dateTextView!!.setOnClickListener({
            var datePicker = DatePickerDialog(activity)
            datePicker.setOnDateSetListener(object : DatePickerDialog.OnDateSetListener {
                override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
                    var cal = Calendar.getInstance()
                    cal.set(year, month, day)
                    game.date = cal.time

                    dateTextView!!.text = dateFormatter.format(game.date)
                }
            })
            datePicker.show()
        })

        addPlayer1Button = contentView!!.findViewById(R.id.create_game_player_1_button)
        addPlayer2Button = contentView!!.findViewById(R.id.create_game_player_2_button)
        addPlayer3Button = contentView!!.findViewById(R.id.create_game_player_3_button)
        addPlayer4Button = contentView!!.findViewById(R.id.create_game_player_4_button)
        addPlayer5Button = contentView!!.findViewById(R.id.create_game_player_5_button)
        addPlayer6Button = contentView!!.findViewById(R.id.create_game_player_6_button)
        addPlayer7Button = contentView!!.findViewById(R.id.create_game_player_7_button)
        addPlayer8Button = contentView!!.findViewById(R.id.create_game_player_8_button)
        setWinnerButton = contentView!!.findViewById(R.id.create_game_set_winner_button)

        addPlayer1Button!!.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                addPlayer1Button!!.viewTreeObserver.removeOnPreDrawListener(this)

                var lp = setWinnerButton!!.layoutParams
                lp.width = (addPlayer1Button!!.width * 1.5).toInt()
                lp.height = (addPlayer1Button!!.height * 1.5).toInt()
                setWinnerButton!!.layoutParams = lp

                return false
            }
        })

        addPlayer1Button!!.setOnClickListener({ addPlayer(1) })
        addPlayer2Button!!.setOnClickListener({ addPlayer(2) })
        addPlayer3Button!!.setOnClickListener({ addPlayer(3) })
        addPlayer4Button!!.setOnClickListener({ addPlayer(4) })
        addPlayer5Button!!.setOnClickListener({ addPlayer(5) })
        addPlayer6Button!!.setOnClickListener({ addPlayer(6) })
        addPlayer7Button!!.setOnClickListener({ addPlayer(7) })
        addPlayer8Button!!.setOnClickListener({ addPlayer(8) })

        setupToggle()

        return contentView
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

    private fun addPlayer(playerNumber: Int) {
        if(players.size > 0) {
            showPlayersDialog(playerNumber)
        }
        else {
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

                        showPlayersDialog(playerNumber)
                    } else {
                        Snackbar.make(contentView!!, "Failed to load player data", Snackbar.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun showPlayersDialog(playerNumber: Int) {
        val recyclerView = RecyclerView(activity)

        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Select Player")
        builder.setView(recyclerView)

        builder.setNegativeButton(android.R.string.cancel, DialogInterface.OnClickListener { dialog, _ -> dialog.dismiss() })
        var dialog = builder.create()

        recyclerView.adapter = ChoosePlayerListAdapter(players, fun(position: Int) {
            dialog.dismiss()

            showCharactersDialog(players[position], playerNumber)
        })
        recyclerView.layoutManager = LinearLayoutManager(context)

        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL)
        recyclerView.addItemDecoration(dividerItemDecoration)

        dialog.show()
    }

    private fun showCharactersDialog(player: Player, playerNumber: Int) {
        val recyclerView = RecyclerView(activity)

        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Select Character")
        builder.setView(recyclerView)

        builder.setNegativeButton(android.R.string.cancel, { dialog, _ -> dialog.dismiss() })
        var dialog = builder.create()

        recyclerView.adapter = ChooseCharacterListAdapter(fun(position: Int) {
            if(gamePlayers.containsKey(playerNumber)) {
                game.players!!.remove(gamePlayers[playerNumber])
                gamePlayers.remove(playerNumber)
            }

            val gamePlayer = GamePlayer()
            gamePlayer.player = player
            gamePlayer.characterId = position

            if(game.players == null) {
                game.players = ArrayList<GamePlayer>()
            }

            gamePlayers.put(playerNumber, gamePlayer)
            game.players!!.add(gamePlayer)

            val button = getSetPlayerButton(playerNumber)
            getSetPlayerButtonImage(button, playerNumber)?.visibility = View.VISIBLE
            getSetPlayerButtonImage(button, playerNumber)?.setImageResource(CharacterHelper.getImage(gamePlayer.characterId))
            getSetPlayerButtonText(button, playerNumber)?.text = player.name

            dialog.dismiss()
        })
        recyclerView.layoutManager = LinearLayoutManager(context)

        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL)
        recyclerView.addItemDecoration(dividerItemDecoration)

        dialog.show()
    }

    private fun getSetPlayerButton(playerNumber: Int): SquareLinearLayout? {
        when(playerNumber) {
            1 -> return addPlayer1Button
            2 -> return addPlayer2Button
            3 -> return addPlayer3Button
            4 -> return addPlayer4Button
            5 -> return addPlayer5Button
            6 -> return addPlayer6Button
            7 -> return addPlayer7Button
            8 -> return addPlayer8Button
        }

        return null
    }

    private fun getSetPlayerButtonImage(button: SquareLinearLayout?, playerNumber: Int): ImageView? {
        when(playerNumber) {
            1 -> return button?.findViewById(R.id.create_game_player_1_image)
            2 -> return button?.findViewById(R.id.create_game_player_2_image)
            3 -> return button?.findViewById(R.id.create_game_player_3_image)
            4 -> return button?.findViewById(R.id.create_game_player_4_image)
            5 -> return button?.findViewById(R.id.create_game_player_5_image)
            6 -> return button?.findViewById(R.id.create_game_player_6_image)
            7 -> return button?.findViewById(R.id.create_game_player_7_image)
            8 -> return button?.findViewById(R.id.create_game_player_8_image)
        }

        return null
    }

    private fun getSetPlayerButtonText(button: SquareLinearLayout?, playerNumber: Int): TextView? {
        when(playerNumber) {
            1 -> return button?.findViewById(R.id.create_game_player_1_name)
            2 -> return button?.findViewById(R.id.create_game_player_2_name)
            3 -> return button?.findViewById(R.id.create_game_player_3_name)
            4 -> return button?.findViewById(R.id.create_game_player_4_name)
            5 -> return button?.findViewById(R.id.create_game_player_5_name)
            6 -> return button?.findViewById(R.id.create_game_player_6_name)
            7 -> return button?.findViewById(R.id.create_game_player_7_name)
            8 -> return button?.findViewById(R.id.create_game_player_8_name)
        }

        return null
    }
}