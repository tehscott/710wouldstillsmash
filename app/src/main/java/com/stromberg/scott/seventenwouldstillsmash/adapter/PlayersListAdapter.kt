package com.stromberg.scott.seventenwouldstillsmash.adapter

import android.content.Context
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.model.GameType
import com.stromberg.scott.seventenwouldstillsmash.model.Player

class PlayersListAdapter(players: List<Player>, private var playerNameWidth: Int) : BaseQuickAdapter<Player, BaseViewHolder>(R.layout.player_list_item, players) {
    override fun convert(viewHolder: BaseViewHolder?, item: Player?) {
        val playerName = viewHolder?.getView<TextView>(R.id.player_list_item_player_name)
        playerName?.text = item?.name

        val layoutParams = playerName?.layoutParams
        layoutParams?.width = playerNameWidth
        playerName?.layoutParams = layoutParams

        val prefs = mContext.getSharedPreferences(mContext.getString(R.string.shared_prefs_key), Context.MODE_PRIVATE)
        val royaleGamesPlayed = prefs.getFloat(item!!.id + GameType.ROYALE.toString() + "_games_played", 0f)
        val royaleGamesWon = prefs.getFloat(item!!.id + GameType.ROYALE.toString() + "_games_won", 0f)
        val royaleGamesLost = prefs.getFloat(item!!.id + GameType.ROYALE.toString() + "_games_lost", 0f)
        val suddenDeathGamesPlayed = prefs.getFloat(item!!.id + GameType.SUDDEN_DEATH.toString() + "_games_played", 0f)
        val suddenDeathGamesWon = prefs.getFloat(item!!.id + GameType.SUDDEN_DEATH.toString() + "_games_won", 0f)
        val suddenDeathGamesLost = prefs.getFloat(item!!.id + GameType.SUDDEN_DEATH.toString() + "_games_lost", 0f)

        val royaleWinPercentage = if(royaleGamesPlayed > 0) royaleGamesWon / royaleGamesPlayed else 0f
        val suddenDeathWinPercentage = if(suddenDeathGamesPlayed > 0) suddenDeathGamesWon / suddenDeathGamesPlayed else 0f

        viewHolder?.setText(R.id.player_list_item_royale_wins_text, Math.round(royaleWinPercentage * 100).toString() + "%")
        viewHolder?.setText(R.id.player_list_item_sudden_death_wins_text, Math.round(suddenDeathWinPercentage * 100).toString() + "%")
    }
}