package com.stromberg.scott.seventenwouldstillsmash.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.model.Statistic
import com.stromberg.scott.seventenwouldstillsmash.util.CharacterHelper

class StatisticsListAdapter(statistics: List<Statistic>) : BaseQuickAdapter<Statistic, BaseViewHolder>(R.layout.statistics_child_list_item, statistics) {
    override fun convert(viewHolder: BaseViewHolder?, item: Statistic?) {
        if(item?.playerId != null && item.playerId != "") {
            viewHolder?.setVisible(R.id.statistics_child_player_stat, true)
            viewHolder?.setVisible(R.id.statistics_child_character_stat, false)
            viewHolder?.setVisible(R.id.statistics_child_character_image, false)
            viewHolder?.setText(R.id.statistics_child_player_stat, item.playerValue)
        }
        else {
            viewHolder?.setVisible(R.id.statistics_child_player_stat, false)
            viewHolder?.setVisible(R.id.statistics_child_character_stat, true)
            viewHolder?.setVisible(R.id.statistics_child_character_image, true)
            viewHolder?.setText(R.id.statistics_child_character_stat, item?.characterValue)
            viewHolder?.setImageResource(R.id.statistics_child_character_image, CharacterHelper.getImage(item!!.characterId))
        }
    }
}