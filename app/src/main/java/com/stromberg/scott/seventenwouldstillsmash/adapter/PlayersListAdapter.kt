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

        if(!item!!.isHidden) {
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
        else {
            layoutParams?.width = ViewGroup.LayoutParams.WRAP_CONTENT
            playerName?.layoutParams = layoutParams

            playerName?.setTextColor(mContext.getColor(R.color.text_secondary))
            playerName?.setCompoundDrawablesWithIntrinsicBounds(null, null, mContext.getDrawable(R.drawable.ic_visibility_off), null)
            viewHolder?.getView<View>(R.id.stats_container)!!.visibility = View.GONE
        }
    }

    private fun createStatTextView(stat: PlayerStatistic, container: LinearLayout?) {
        val tv = TextView(mContext)
        tv.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        tv.textSize = 16f

        var iconResId = mContext.resources.getIdentifier(stat.gameType?.iconName
                ?: "", "drawable", App.getContext().packageName)

        if (iconResId == -1) {
            iconResId = R.drawable.ic_royale
        }

        tv.setCompoundDrawablesWithIntrinsicBounds(iconResId, 0, 0, 0)
        tv.compoundDrawablePadding = mContext.resources.getDimensionPixelSize(R.dimen.space_8dp)

        val winRate = Math.round((stat.gamesWon.toFloat() / stat.gamesPlayed.toFloat()) * 100f)

        tv.text = "$winRate% (${stat.gamesWon}/${stat.gamesPlayed})"

        container?.addView(tv)
    }

    fun setPlayerStats(playerStats: HashMap<Player, List<PlayerStatistic>>) {
        this.playerStats = playerStats
    }
}