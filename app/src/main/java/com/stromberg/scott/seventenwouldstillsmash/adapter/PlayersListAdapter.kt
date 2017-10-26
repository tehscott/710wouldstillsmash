package com.stromberg.scott.seventenwouldstillsmash.adapter

import android.content.Context
import android.support.v4.widget.Space
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.model.Game
import com.stromberg.scott.seventenwouldstillsmash.model.GameType
import com.stromberg.scott.seventenwouldstillsmash.model.Player
import com.stromberg.scott.seventenwouldstillsmash.util.CharacterHelper
import java.text.SimpleDateFormat
import java.util.*

//class PlayersListAdapter(private var players: List<Player>, private var playerNameWidth: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//    private var context: Context? = null
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        context = parent.context
//
//        val v = LayoutInflater.from(context).inflate(R.layout.player_list_item, parent, false)
//        return PlayerViewHolder(v)
//    }
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        val h = holder as PlayerViewHolder
//        val item = players[position]
//
//        h.name.text = item.name
//    }
//
//    override fun getItemCount(): Int {
//        return players.size
//    }
//
//    private inner class PlayerViewHolder(v: View) : RecyclerView.ViewHolder(v) {
//        var parent = v
//        var name: TextView
//
//        init {
//            name = v.findViewById<TextView>(R.id.player_list_item_player_name)
//            var layoutParams = name.layoutParams
//            layoutParams.width = playerNameWidth
//        }
//    }
//}

class PlayersListAdapter(players: List<Player>, private var playerNameWidth: Int) : BaseQuickAdapter<Player, BaseViewHolder>(R.layout.player_list_item, players) {
    private var dateFormatter = SimpleDateFormat("MM/dd/yy")

    override fun convert(viewHolder: BaseViewHolder?, item: Player?) {
        val playerName = viewHolder?.getView<TextView>(R.id.player_list_item_player_name)
        playerName?.text = item?.name

        val layoutParams = playerName?.layoutParams
        layoutParams?.width = playerNameWidth
        playerName?.layoutParams = layoutParams
    }
}