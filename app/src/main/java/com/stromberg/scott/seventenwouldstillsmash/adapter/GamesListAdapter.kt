package com.stromberg.scott.seventenwouldstillsmash.adapter

import android.widget.LinearLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import com.stromberg.scott.seventenwouldstillsmash.App
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.model.Game
import com.stromberg.scott.seventenwouldstillsmash.model.GamePlayer
import com.stromberg.scott.seventenwouldstillsmash.model.GameType
import com.stromberg.scott.seventenwouldstillsmash.util.CharacterHelper
import java.text.SimpleDateFormat
import java.util.*

class GamesListAdapter(val games: List<Game>, val sortBy: SortBy, private var loserContainerWidth: Int) : BaseQuickAdapter<Game, BaseViewHolder>(R.layout.game_list_item, games), FastScrollRecyclerView.SectionedAdapter {
    private var dateFormatter = SimpleDateFormat("MM/dd/yy", Locale.getDefault())
    private var sectionDateFormatter = SimpleDateFormat("MMM yy", Locale.getDefault())

    enum class SortBy {
        WINNER,
        PLAYER;
    }

    override fun convert(viewHolder: BaseViewHolder, item: Game?) {
        val players: List<GamePlayer>?

        when(sortBy) {
            SortBy.WINNER -> players = item?.players?.sortedWith(compareBy({ !it.winner }, { it.player?.name }))
            SortBy.PLAYER -> players = item?.players?.sortedWith(compareBy { it.player?.name })
        }

        val winner = players?.firstOrNull { it.winner }
        val otherPlayers = players?.filter { !it.winner }?.take(3)

        if(winner != null) {
            viewHolder.setVisible(R.id.game_list_item_winner_image, true)
            viewHolder.setVisible(R.id.game_list_item_winner_name, true)
            viewHolder.setImageResource(R.id.game_list_item_winner_image, CharacterHelper.getImage(winner.characterId))
            viewHolder.setText(R.id.game_list_item_winner_name, winner.player?.name)
        }
        else {
            viewHolder.setVisible(R.id.game_list_item_winner_image, false)
            viewHolder.setVisible(R.id.game_list_item_winner_name, false)
        }

        val resources = App.getContext().resources
        val packageName = App.getContext().packageName

        for (i in 1..3) {
            val imageId = resources.getIdentifier("game_list_item_loser${i}_image", "id", packageName)
            val nameId = resources.getIdentifier("game_list_item_loser${i}_name", "id", packageName)

            viewHolder.setVisible(imageId, false)
            viewHolder.setVisible(nameId, false)
        }

        otherPlayers?.forEachIndexed { index, gamePlayer ->
            val containerId = resources.getIdentifier("game_list_item_loser${index + 1}_container", "id", packageName)
            val imageId = resources.getIdentifier("game_list_item_loser${index + 1}_image", "id", packageName)
            val nameId = resources.getIdentifier("game_list_item_loser${index + 1}_name", "id", packageName)

            viewHolder.setVisible(imageId, true)
            viewHolder.setVisible(nameId, true)

            viewHolder.setImageResource(imageId, CharacterHelper.getImage(gamePlayer.characterId))
            viewHolder.setText(nameId, gamePlayer.player?.name)

            val container = viewHolder.getView<LinearLayout>(containerId)

            val playerNameLayoutParams = container?.layoutParams
            playerNameLayoutParams?.width = loserContainerWidth
            container?.layoutParams = playerNameLayoutParams
        }

        when (item!!.gameType) {
            GameType.ROYALE.toString() -> {
                viewHolder.setImageResource(R.id.game_list_item_game_type_image, R.drawable.ic_royale)
            }

            GameType.SUDDEN_DEATH.toString() -> {
                viewHolder.setImageResource(R.id.game_list_item_game_type_image, R.drawable.ic_sudden_death)
            }
        }

        viewHolder.setText(R.id.game_list_item_date, dateFormatter.format(Date(item.date)))
    }

    override fun getSectionName(position: Int): String {
        return sectionDateFormatter.format(Date(games[position].date))
    }
}