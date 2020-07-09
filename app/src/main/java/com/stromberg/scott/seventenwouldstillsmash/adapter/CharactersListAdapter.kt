package com.stromberg.scott.seventenwouldstillsmash.adapter

import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.stromberg.scott.seventenwouldstillsmash.App
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.model.Characters
import com.stromberg.scott.seventenwouldstillsmash.model.Game
import com.stromberg.scott.seventenwouldstillsmash.util.CharacterHelper
import com.stromberg.scott.seventenwouldstillsmash.util.GameTypeHelper
import java.util.*

class CharactersListAdapter(characterIds: List<Int>, private var gamesForAllPlayers: HashMap<Int, List<Game>>) : BaseQuickAdapter<Int, BaseViewHolder>(R.layout.character_list_item, characterIds) {
    override fun convert(viewHolder: BaseViewHolder?, characterId: Int) {
        val character = Characters.byId(characterId)

        val characterImage = viewHolder?.getView<ImageView>(R.id.character_list_item_character_image)
        val characterName = viewHolder?.getView<TextView>(R.id.character_list_item_character_name)

        characterImage?.setImageResource(character?.imageRes ?: 0)
        characterName?.text = character?.characterName

        val gamesForThisPlayer = gamesForAllPlayers[characterId]
        val gameTypesForThisPlayer = HashMap<String?, Int>()
        gamesForThisPlayer?.map { it.gameType }?.forEach { gameTypeId ->
            val gameType = GameTypeHelper.getGameType(gameTypeId)

            if(gameType != null && !gameType.isDeleted) {
                gameTypesForThisPlayer[gameTypeId] = gamesForThisPlayer.count { game -> game.gameType == gameTypeId }
            }
        }
        val top2GameTypes = gameTypesForThisPlayer.toList().sortedByDescending { (_, count) -> count}.take(2).map { it.first }

        viewHolder?.setVisible(R.id.character_list_game_type_1_wins_text, false)
        viewHolder?.setVisible(R.id.character_list_game_type_2_wins_text, false)
        viewHolder?.setVisible(R.id.character_list_win_rate_text, top2GameTypes.isNotEmpty())

        top2GameTypes.forEachIndexed { index, gameTypeId ->
            val gameType = GameTypeHelper.getGameType(gameTypeId)
            val gamesPlayed: Float = (gamesForThisPlayer!!.count { it.players.any { it.characterId == characterId } && it.gameType!!.equals(gameTypeId, true) }).toFloat()

            if(gamesPlayed > 0) {
                val gamesWon: Float = (gamesForThisPlayer.count { it.players.any { it.characterId == characterId && it.winner } && it.gameType!!.equals(gameTypeId, true) }).toFloat()
                val winPercentage = if (gamesPlayed > 0) gamesWon / gamesPlayed else 0f

                when (index) {
                    0 -> {
                        viewHolder?.setVisible(R.id.character_list_game_type_1_wins_text, true)
                        viewHolder?.setText(R.id.character_list_game_type_1_wins_text, (Math.round(winPercentage * 100).toString() + "%") + " (" + gamesWon.toInt() + "/" + gamesPlayed.toInt() + ")")

                        if(gameType != null) {
                            var iconResId = App.getContext().resources.getIdentifier(gameType.iconName, "drawable", App.getContext().packageName)

                            if(iconResId == -1) {
                                iconResId = R.drawable.ic_royale
                            }

                            viewHolder?.getView<TextView>(R.id.character_list_game_type_1_wins_text)?.setCompoundDrawablesWithIntrinsicBounds(iconResId, 0, 0, 0)
                        }
                    }
                    1 -> {
                        viewHolder?.setVisible(R.id.character_list_game_type_2_wins_text, true)
                        viewHolder?.setText(R.id.character_list_game_type_2_wins_text, (Math.round(winPercentage * 100).toString() + "%") + " (" + gamesWon.toInt() + "/" + gamesPlayed.toInt() + ")")
                    }
                }
            }
        }
    }
}