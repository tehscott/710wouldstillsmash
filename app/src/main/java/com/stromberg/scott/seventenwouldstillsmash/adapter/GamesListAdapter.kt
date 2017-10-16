package com.stromberg.scott.seventenwouldstillsmash.adapter

import android.content.Context
import android.support.v4.widget.Space
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.model.Game
import com.stromberg.scott.seventenwouldstillsmash.model.GameType
import com.stromberg.scott.seventenwouldstillsmash.util.CharacterHelper
import java.text.SimpleDateFormat

class GamesListAdapter(private var games: List<Game>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var context: Context? = null
    var dateFormatter = SimpleDateFormat("MM/dd/yy")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context

        val v = LayoutInflater.from(context).inflate(R.layout.game_list_item, parent, false)
        return GameViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        //todo: get complementing color and set it as the bg for each image

        val h = holder as GameViewHolder
        val item = games[position]

//        h.parent.setBackgroundResource(if (position % 2 == 0) R.color.primary_light else android.R.color.white)

        h.game = item
        h.date.text = dateFormatter.format(item.date)
//        h.date.setTextColor(context!!.resources.getColor(if (position % 2 == 0) android.R.color.white else R.color.primary_dark))
//        h.date.setTextColor(context!!.resources.getColor(R.color.primary_dark))

        when (item.gameType) {
            GameType.ROYALE.toString() -> {
                h.image.setImageResource(R.drawable.ic_royale)
            }

            GameType.SUDDEN_DEATH.toString() -> {
                h.image.setImageResource(R.drawable.ic_sudden_death)
            }
        }

//        h.image.drawable.setTint(context!!.resources.getColor(if (position % 2 == 0) android.R.color.white else R.color.primary_dark))
//        h.image.drawable.setTint(context!!.resources.getColor(R.color.primary_dark))

        h.list.removeAllViews()
        item.players!!.forEach({ player ->
            run {
                val playerLayout: FrameLayout = LayoutInflater.from(context).inflate(R.layout.game_list_item_image, null) as FrameLayout
                playerLayout.findViewById<ImageView>(R.id.game_list_item_character_image).setImageResource(CharacterHelper.getImage(player.characterId))
                playerLayout.findViewById<TextView>(R.id.game_list_item_player_name).text = player.player!!.name
                playerLayout.findViewById<TextView>(R.id.game_list_item_player_name).setBackgroundResource(if (player.winner) R.color.player_winner else R.color.player_loser)

                var layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)
                layoutParams.weight = 1f
                playerLayout.layoutParams = layoutParams

                h.list.addView(playerLayout)

                var space = Space(context)
                layoutParams = LinearLayout.LayoutParams(context!!.resources.getDimensionPixelSize(R.dimen.space_8dp), LinearLayout.LayoutParams.WRAP_CONTENT)
                space.layoutParams = layoutParams

                h.list.addView(space)
            }
        })
    }

    override fun getItemCount(): Int {
        return games.size
    }

    private inner class GameViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var parent = v
        var game: Game? = null
        var list: LinearLayout = v.findViewById<LinearLayout>(R.id.game_list)
        var image: ImageView = v.findViewById<ImageView>(R.id.game_list_item_game_type_image)
        var date: TextView = v.findViewById<TextView>(R.id.game_list_item_date)
    }
}