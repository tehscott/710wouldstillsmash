package com.stromberg.scott.seventenwouldstillsmash.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
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

        if(!item!!.isHidden) {
            layoutParams?.width = playerNameWidth
            playerName?.layoutParams = layoutParams

            val prefs = mContext.getSharedPreferences(mContext.getString(R.string.shared_prefs_key), Context.MODE_PRIVATE)

            // 30 GAMES
            val thirtyGamesRoyaleGamesWon = prefs.getFloat(item!!.id + GameType.ROYALE.toString() + "30_games_won", 0f)
            val thirtyGamesRoyaleGamesLost = prefs.getFloat(item!!.id + GameType.ROYALE.toString() + "30_games_lost", 0f)
            val thirtyGamesRoyaleGamesPlayed = thirtyGamesRoyaleGamesWon + thirtyGamesRoyaleGamesLost;
            val thirtyGamesSuddenDeathGamesWon = prefs.getFloat(item!!.id + GameType.SUDDEN_DEATH.toString() + "30_games_won", 0f)
            val thirtyGamesSuddenDeathGamesLost = prefs.getFloat(item!!.id + GameType.SUDDEN_DEATH.toString() + "30_games_lost", 0f)
            val thirtyGamesSuddenDeathGamesPlayed = thirtyGamesSuddenDeathGamesWon + thirtyGamesSuddenDeathGamesLost;
            val thirtyGamesRoyaleWinPercentage = if(thirtyGamesRoyaleGamesPlayed > 0) thirtyGamesRoyaleGamesWon / thirtyGamesRoyaleGamesPlayed else 0f
            val thirtyGamesSuddenDeathWinPercentage = if(thirtyGamesSuddenDeathGamesPlayed > 0) thirtyGamesSuddenDeathGamesWon / thirtyGamesSuddenDeathGamesPlayed else 0f

            viewHolder?.setText(R.id.player_list_item_30_day_royale_wins_text, (Math.round(thirtyGamesRoyaleWinPercentage * 100).toString() + "%") + " (" + thirtyGamesRoyaleGamesWon.toInt() + "/" + thirtyGamesRoyaleGamesPlayed.toInt() + ")")
            viewHolder?.setText(R.id.player_list_item_30_day_sudden_death_wins_text, (Math.round(thirtyGamesSuddenDeathWinPercentage * 100).toString() + "%") + " (" + thirtyGamesSuddenDeathGamesWon.toInt() + "/" + thirtyGamesSuddenDeathGamesPlayed.toInt() + ")")

            // ALL TIME
            val allTimeRoyaleGamesWon = prefs.getFloat(item.id + GameType.ROYALE.toString() + "all_time_games_won", 0f)
            val allTimeRoyaleGamesLost = prefs.getFloat(item.id + GameType.ROYALE.toString() + "all_time_games_lost", 0f)
            val allTimeRoyaleGamesPlayed = allTimeRoyaleGamesWon + allTimeRoyaleGamesLost;
            val allTimeSuddenDeathGamesWon = prefs.getFloat(item.id + GameType.SUDDEN_DEATH.toString() + "all_time_games_won", 0f)
            val allTimeSuddenDeathGamesLost = prefs.getFloat(item.id + GameType.SUDDEN_DEATH.toString() + "all_time_games_lost", 0f)
            val allTimeSuddenDeathGamesPlayed = allTimeSuddenDeathGamesWon + allTimeSuddenDeathGamesLost;

            val allTimeRoyaleWinPercentage = if(allTimeRoyaleGamesPlayed > 0) allTimeRoyaleGamesWon / allTimeRoyaleGamesPlayed else 0f
            val allTimeSuddenDeathWinPercentage = if(allTimeSuddenDeathGamesPlayed > 0) allTimeSuddenDeathGamesWon / allTimeSuddenDeathGamesPlayed else 0f

            viewHolder?.setText(R.id.player_list_item_all_time_royale_wins_text, (Math.round(allTimeRoyaleWinPercentage * 100).toString() + "%") + " (" + allTimeRoyaleGamesWon.toInt() + "/" + allTimeRoyaleGamesPlayed.toInt() + ")")
            viewHolder?.setText(R.id.player_list_item_all_time_sudden_death_wins_text, (Math.round(allTimeSuddenDeathWinPercentage * 100).toString() + "%") + " (" + allTimeSuddenDeathGamesWon.toInt() + "/" + allTimeSuddenDeathGamesPlayed.toInt() + ")")

            playerName?.setTextColor(mContext.getColor(R.color.primary))
            playerName?.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            viewHolder?.getView<View>(R.id.stats_container)!!.visibility = View.VISIBLE
        }
        else {
            layoutParams?.width = ViewGroup.LayoutParams.WRAP_CONTENT
            playerName?.layoutParams = layoutParams

            playerName?.setTextColor(mContext.getColor(R.color.text_secondary))
            playerName?.setCompoundDrawablesWithIntrinsicBounds(null, null, mContext.getDrawable(R.drawable.ic_visibility_off), null)
            viewHolder?.getView<View>(R.id.stats_container)!!.visibility = View.GONE
        }
    }
}