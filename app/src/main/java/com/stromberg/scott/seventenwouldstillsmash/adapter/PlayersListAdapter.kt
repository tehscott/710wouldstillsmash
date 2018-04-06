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

        // 30 DAY
        val thirtyDayRoyaleGamesWon = prefs.getFloat(item!!.id + GameType.ROYALE.toString() + "30_day_games_won", 0f)
        val thirtyDayRoyaleGamesLost = prefs.getFloat(item!!.id + GameType.ROYALE.toString() + "30_day_games_lost", 0f)
        val thirtyDayRoyaleGamesPlayed = thirtyDayRoyaleGamesWon + thirtyDayRoyaleGamesLost;
        val thirtyDaySuddenDeathGamesWon = prefs.getFloat(item!!.id + GameType.SUDDEN_DEATH.toString() + "30_day_games_won", 0f)
        val thirtyDaySuddenDeathGamesLost = prefs.getFloat(item!!.id + GameType.SUDDEN_DEATH.toString() + "30_day_games_lost", 0f)
        val thirtyDaySuddenDeathGamesPlayed = thirtyDaySuddenDeathGamesWon + thirtyDaySuddenDeathGamesLost;

        val thirtyDayRoyaleWinPercentage = if(thirtyDayRoyaleGamesPlayed > 0) thirtyDayRoyaleGamesWon / thirtyDayRoyaleGamesPlayed else 0f
        val thirtyDaySuddenDeathWinPercentage = if(thirtyDaySuddenDeathGamesPlayed > 0) thirtyDaySuddenDeathGamesWon / thirtyDaySuddenDeathGamesPlayed else 0f

        viewHolder?.setText(R.id.player_list_item_30_day_royale_wins_text, (Math.round(thirtyDayRoyaleWinPercentage * 100).toString() + "%") + " (" + thirtyDayRoyaleGamesWon.toInt() + "/" + thirtyDayRoyaleGamesPlayed.toInt() + ")")
        viewHolder?.setText(R.id.player_list_item_30_day_sudden_death_wins_text, (Math.round(thirtyDaySuddenDeathWinPercentage * 100).toString() + "%") + " (" + thirtyDaySuddenDeathGamesWon.toInt() + "/" + thirtyDaySuddenDeathGamesPlayed.toInt() + ")")

        // ALL TIME
        val allTimeRoyaleGamesWon = prefs.getFloat(item!!.id + GameType.ROYALE.toString() + "all_time_games_won", 0f)
        val allTimeRoyaleGamesLost = prefs.getFloat(item!!.id + GameType.ROYALE.toString() + "all_time_games_lost", 0f)
        val allTimeRoyaleGamesPlayed = allTimeRoyaleGamesWon + allTimeRoyaleGamesLost;
        val allTimeSuddenDeathGamesWon = prefs.getFloat(item!!.id + GameType.SUDDEN_DEATH.toString() + "all_time_games_won", 0f)
        val allTimeSuddenDeathGamesLost = prefs.getFloat(item!!.id + GameType.SUDDEN_DEATH.toString() + "all_time_games_lost", 0f)
        val allTimeSuddenDeathGamesPlayed = allTimeSuddenDeathGamesWon + allTimeSuddenDeathGamesLost;

        val allTimeRoyaleWinPercentage = if(allTimeRoyaleGamesPlayed > 0) allTimeRoyaleGamesWon / allTimeRoyaleGamesPlayed else 0f
        val allTimeSuddenDeathWinPercentage = if(allTimeSuddenDeathGamesPlayed > 0) allTimeSuddenDeathGamesWon / allTimeSuddenDeathGamesPlayed else 0f

        viewHolder?.setText(R.id.player_list_item_all_time_royale_wins_text, (Math.round(allTimeRoyaleWinPercentage * 100).toString() + "%") + " (" + allTimeRoyaleGamesWon.toInt() + "/" + allTimeRoyaleGamesPlayed.toInt() + ")")
        viewHolder?.setText(R.id.player_list_item_all_time_sudden_death_wins_text, (Math.round(allTimeSuddenDeathWinPercentage * 100).toString() + "%") + " (" + allTimeSuddenDeathGamesWon.toInt() + "/" + allTimeSuddenDeathGamesPlayed.toInt() + ")")
    }
}