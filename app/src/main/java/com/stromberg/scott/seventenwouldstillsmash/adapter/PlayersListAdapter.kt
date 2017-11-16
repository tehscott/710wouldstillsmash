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

        // player win/loss
        val royaleGamesPlayed = prefs.getFloat(item!!.id + GameType.ROYALE.toString() + "_games_played", 0f)
        val royaleGamesWon = prefs.getFloat(item!!.id + GameType.ROYALE.toString() + "_games_won", 0f)
        val royaleGamesLost = prefs.getFloat(item!!.id + GameType.ROYALE.toString() + "_games_lost", 0f)
        val suddenDeathGamesPlayed = prefs.getFloat(item!!.id + GameType.SUDDEN_DEATH.toString() + "_games_played", 0f)
        val suddenDeathGamesWon = prefs.getFloat(item!!.id + GameType.SUDDEN_DEATH.toString() + "_games_won", 0f)
        val suddenDeathGamesLost = prefs.getFloat(item!!.id + GameType.SUDDEN_DEATH.toString() + "_games_lost", 0f)

        val royaleWinPercentage = if(royaleGamesPlayed > 0) royaleGamesWon / royaleGamesPlayed else 0f
        val suddenDeathWinPercentage = if(suddenDeathGamesPlayed > 0) suddenDeathGamesWon / suddenDeathGamesPlayed else 0f

        viewHolder?.setText(R.id.player_list_item_royale_wins_text, (Math.round(royaleWinPercentage * 100).toString() + "%") + " (" + royaleGamesWon.toInt() + "/" + royaleGamesPlayed.toInt() + ")")
        viewHolder?.setText(R.id.player_list_item_sudden_death_wins_text, (Math.round(suddenDeathWinPercentage * 100).toString() + "%") + " (" + suddenDeathGamesWon.toInt() + "/" + suddenDeathGamesPlayed.toInt() + ")")

//        // best character
//        val hashMapType = object : TypeToken<HashMap<String, CharacterStats>>() {}.type
//        val playerStats = Gson().fromJson<HashMap<String, CharacterStats>>(prefs.getString("PlayerStatsJson", null), hashMapType)
//
//        var thisPlayerStats = playerStats.filter { it.key.contains(item.id!!, true) }
//
//        var best = thisPlayerStats.values
//                .filter { it.wins + it.losses > 1 }
//                .maxBy { it.wins.toFloat() / (it.wins.toFloat() + it.losses.toFloat()) }
//
//        var worst = thisPlayerStats.values
//                .filter { it.wins + it.losses > 1 }
//                .maxBy { it.losses.toFloat() / (it.wins.toFloat() + it.losses.toFloat()) }
//
//        var bestOneWinRate = best!!.wins.toFloat() / (best!!.wins.toFloat() + best!!.losses.toFloat())
//        var bestTwoWinRate = best!!.wins.toFloat() / (best!!.wins.toFloat() + best!!.losses.toFloat())
//        var worstOneWinRate = worst!!.losses.toFloat() / (worst!!.wins.toFloat() + worst!!.losses.toFloat())
//        var worstTwoWinRate = worst!!.losses.toFloat() / (worst!!.wins.toFloat() + worst!!.losses.toFloat())
//        viewHolder?.setText(R.id.player_list_item_best_one_text, (bestOneWinRate * 100).toString() + "%")
//        viewHolder?.setImageResource(R.id.player_list_item_best_one_image, CharacterHelper.getImage(best!!.characterId))
//
//        viewHolder?.setText(R.id.player_list_item_worst_one_text, (worstOneWinRate * 100).toString() + "%")
//        viewHolder?.setImageResource(R.id.player_list_item_worst_one_image, CharacterHelper.getImage(worst!!.characterId))
    }
}