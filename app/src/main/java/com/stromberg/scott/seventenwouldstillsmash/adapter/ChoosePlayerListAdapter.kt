package com.stromberg.scott.seventenwouldstillsmash.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.model.Player

class ChoosePlayerListAdapter(private var players: List<Player>, var itemClicked: (Int) -> (Unit)) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var context: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context

        val v = LayoutInflater.from(context).inflate(R.layout.choose_player_character_list_item, parent, false)
        return PlayerViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val h = holder as PlayerViewHolder
        val item = players[position]

        h.image.visibility = View.GONE
        h.name.text = item.name

        h.parent.setOnClickListener({ itemClicked(position) })
    }

    override fun getItemCount(): Int {
        return players.size
    }

    private inner class PlayerViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var parent = v
        var image: ImageView = v.findViewById<ImageView>(R.id.choose_player_image)
        var name: TextView = v.findViewById<TextView>(R.id.choose_player_name)
    }
}