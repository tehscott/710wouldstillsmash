package com.stromberg.scott.seventenwouldstillsmash.adapter

import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.model.Game
import com.stromberg.scott.seventenwouldstillsmash.model.GameType
import com.stromberg.scott.seventenwouldstillsmash.util.CharacterHelper
import java.util.*

class CharactersListAdapter(characterIds: List<Int>, private var gamesForPlayers: HashMap<Int, List<Game>>) : BaseQuickAdapter<Int, BaseViewHolder>(R.layout.character_list_item, characterIds) {
    override fun convert(viewHolder: BaseViewHolder?, characterId: Int) {
        var characterImage = viewHolder?.getView<ImageView>(R.id.character_list_item_character_image);
        val characterName = viewHolder?.getView<TextView>(R.id.character_list_item_character_name)

        characterImage?.setImageResource(CharacterHelper.getImage(characterId))
        characterName?.text = CharacterHelper.getName(characterId)

        val games = gamesForPlayers[characterId];

        val royaleGamesPlayed: Float = (games!!.count { it.players.any { it.characterId == characterId } && it.gameType!!.equals(GameType.ROYALE.toString(), true) }).toFloat()
        val royaleGamesWon: Float = (games.count { it.players.any { it.characterId == characterId && it.winner } && it.gameType!!.equals(GameType.ROYALE.toString(), true) }).toFloat()
        val royaleGamesLost: Float = games.size - royaleGamesWon
        val suddenDeathGamesPlayed: Float = (games.count { it.players.any { it.characterId == characterId } && it.gameType!!.equals(GameType.SUDDEN_DEATH.toString(), true) }).toFloat()
        val suddenDeathGamesWon: Float = (games.count { it.players.any { it.characterId == characterId && it.winner } && it.gameType!!.equals(GameType.SUDDEN_DEATH.toString(), true) }).toFloat()
        val suddenDeathGamesLost: Float = games.size - suddenDeathGamesWon

        val royaleWinPercentage = if(royaleGamesPlayed > 0) royaleGamesWon / royaleGamesPlayed else 0f
        val suddenDeathWinPercentage = if(suddenDeathGamesPlayed > 0) suddenDeathGamesWon / suddenDeathGamesPlayed else 0f

        viewHolder?.setText(R.id.character_list_item_royale_wins_text, (Math.round(royaleWinPercentage * 100).toString() + "%") + " (" + royaleGamesWon.toInt() + "/" + royaleGamesPlayed.toInt() + ")")
        viewHolder?.setText(R.id.character_list_item_sudden_death_wins_text, (Math.round(suddenDeathWinPercentage * 100).toString() + "%") + " (" + suddenDeathGamesWon.toInt() + "/" + suddenDeathGamesPlayed.toInt() + ")")
    }
}