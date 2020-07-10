package com.stromberg.scott.seventenwouldstillsmash.adapter

import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.stromberg.scott.seventenwouldstillsmash.App
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.model.Characters
import com.stromberg.scott.seventenwouldstillsmash.model.Game
import java.util.*

class CharactersListAdapter(private var gamesForAllPlayers: SortedMap<Characters, List<Game>>) : BaseQuickAdapter<Characters, BaseViewHolder>(R.layout.character_list_item, gamesForAllPlayers.keys.toList()) {
    override fun convert(viewHolder: BaseViewHolder?, character: Characters) {
        val characterImage = viewHolder?.getView<ImageView>(R.id.character_list_item_character_image)
        val characterName = viewHolder?.getView<TextView>(R.id.character_list_item_character_name)

        characterImage?.setImageResource(character.imageRes ?: 0)
        characterName?.text = character.characterName

        val gamesForThisPlayer = gamesForAllPlayers[character]
        viewHolder?.setVisible(R.id.character_list_game_type_1_wins_text, false)
        viewHolder?.setVisible(R.id.character_list_win_rate_text, false)

        val gamesPlayed: Float = (gamesForThisPlayer!!.count { it.players.any { it.characterId == character.id } }).toFloat()

        if(gamesPlayed > 0) {
            val gamesWon: Float = (gamesForThisPlayer.count { it.players.any { it.characterId == character.id && it.winner } }).toFloat()
            val winPercentage = if (gamesPlayed > 0) gamesWon / gamesPlayed else 0f

            viewHolder?.setVisible(R.id.character_list_game_type_1_wins_text, true)
            viewHolder?.setVisible(R.id.character_list_win_rate_text, true)
            viewHolder?.setText(R.id.character_list_game_type_1_wins_text, (Math.round(winPercentage * 100).toString() + "%") + " (" + gamesWon.toInt() + "/" + gamesPlayed.toInt() + ")")
        }
    }
}