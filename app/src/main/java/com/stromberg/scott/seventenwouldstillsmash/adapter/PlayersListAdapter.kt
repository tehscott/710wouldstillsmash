package com.stromberg.scott.seventenwouldstillsmash.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.stromberg.scott.seventenwouldstillsmash.App
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.model.Player
import com.stromberg.scott.seventenwouldstillsmash.model.PlayerStatistic

class PlayersListAdapter(players: List<Player>, private var playerNameWidth: Int, private var playerStats: HashMap<Player, List<PlayerStatistic>>) : BaseQuickAdapter<Player, BaseViewHolder>(R.layout.player_list_item, players) {
    override fun convert(viewHolder: BaseViewHolder?, item: Player?) {
        val playerName = viewHolder?.getView<TextView>(R.id.player_list_item_player_name)
        playerName?.text = item?.name
        val layoutParams = playerName?.layoutParams

        layoutParams?.width = playerNameWidth
        playerName?.layoutParams = layoutParams

        val statsForPlayer = playerStats[item]
        statsForPlayer?.filter { !it.is30GameStat }?.forEach { stat ->
            createStatTextView(stat, viewHolder?.getView(R.id.all_time_stats_container))
        }

        statsForPlayer?.filter { it.is30GameStat }?.forEach { stat ->
            createStatTextView(stat, viewHolder?.getView(R.id.last_30_stats_container))
        }

        playerName?.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        viewHolder?.getView<View>(R.id.stats_container)!!.visibility = View.VISIBLE
    }

    private fun createStatTextView(stat: PlayerStatistic, container: LinearLayout?) {
        val tv = TextView(mContext)
        tv.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        tv.textSize = 16f

        val winRate = Math.round((stat.gamesWon.toFloat() / stat.gamesPlayed.toFloat()) * 100f)

        tv.text = "$winRate% (${stat.gamesWon}/${stat.gamesPlayed})"

        container?.addView(tv)
    }

    fun setPlayerStats(playerStats: HashMap<Player, List<PlayerStatistic>>) {
        this.playerStats = playerStats
    }
}