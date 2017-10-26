package com.stromberg.scott.seventenwouldstillsmash.adapter

import android.support.v4.widget.Space
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.model.Game
import com.stromberg.scott.seventenwouldstillsmash.model.GameType
import com.stromberg.scott.seventenwouldstillsmash.util.CharacterHelper
import java.text.SimpleDateFormat
import java.util.*

class GamesListAdapter(games: List<Game>) : BaseQuickAdapter<Game, BaseViewHolder>(R.layout.game_list_item, games) {
    private var dateFormatter = SimpleDateFormat("MM/dd/yy")

    override fun convert(viewHolder: BaseViewHolder?, item: Game?) {
        val playersList = viewHolder!!.getView<LinearLayout>(R.id.player_list)
        val gameTypeImage = viewHolder.getView<ImageView>(R.id.game_list_item_game_type_image)

        playersList.removeAllViews()
        item?.players?.forEach({ player ->
            run {
                val playerLayout: FrameLayout = LayoutInflater.from(mContext).inflate(R.layout.game_list_item_image, null) as FrameLayout
                playerLayout.findViewById<ImageView>(R.id.game_list_item_character_image).setImageResource(CharacterHelper.getImage(player.characterId))
                playerLayout.findViewById<TextView>(R.id.game_list_item_player_name).text = player.player!!.name
                playerLayout.findViewById<TextView>(R.id.game_list_item_player_name).setBackgroundResource(if (player.winner) R.color.player_winner else R.color.player_loser)

                var layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)
                layoutParams.weight = 1f
                playerLayout.layoutParams = layoutParams

                playersList.addView(playerLayout)

                val space = Space(mContext)
                layoutParams = LinearLayout.LayoutParams(mContext.resources.getDimensionPixelSize(R.dimen.space_8dp), LinearLayout.LayoutParams.WRAP_CONTENT)
                space.layoutParams = layoutParams

                playersList.addView(space)
            }
        })

        when (item!!.gameType) {
            GameType.ROYALE.toString() -> {
                gameTypeImage.setImageResource(R.drawable.ic_royale)
            }

            GameType.SUDDEN_DEATH.toString() -> {
                gameTypeImage.setImageResource(R.drawable.ic_sudden_death)
            }
        }

        viewHolder.setText(R.id.game_list_item_date, dateFormatter.format(Date(item.date)))
    }
}