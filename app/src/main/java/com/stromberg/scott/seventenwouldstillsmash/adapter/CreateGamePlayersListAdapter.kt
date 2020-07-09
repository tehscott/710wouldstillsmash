package com.stromberg.scott.seventenwouldstillsmash.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.model.Characters
import com.stromberg.scott.seventenwouldstillsmash.model.GamePlayer
import com.stromberg.scott.seventenwouldstillsmash.util.CharacterHelper

class CreateGamePlayersListAdapter(private var players: List<GamePlayer>, private var itemClicked: (Int) -> (Unit)) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var context: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context

        val v = LayoutInflater.from(context).inflate(R.layout.create_game_players_list_item, parent, false)
        return PlayerViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val h = holder as PlayerViewHolder
        val item = players[position]

        h.image.setImageResource(Characters.byId(item.characterId)?.imageRes ?: 0)
        h.name.text = item.player?.name
        h.trophy.visibility = if(item.winner) View.VISIBLE else View.GONE
        h.parent.setOnClickListener({ itemClicked(position) })
    }

    override fun getItemCount(): Int {
        return players.size
    }

    private inner class PlayerViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var parent = v
        var image = v.findViewById<ImageView>(R.id.create_game_players_list_item_image)
        var name = v.findViewById<TextView>(R.id.create_game_players_list_item_name)
        var trophy = v.findViewById<ImageView>(R.id.create_game_players_list_item_trophy)
    }
}