package com.stromberg.scott.seventenwouldstillsmash.adapter

import android.content.Context
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.stromberg.scott.seventenwouldstillsmash.App
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.model.GameType2

abstract class GameTypeListAdapter(val context: Context, val gameTypes: ArrayList<GameType2>) : BaseQuickAdapter<GameType2, BaseViewHolder>(R.layout.game_type_list_item, gameTypes) {
    override fun convert(viewHolder: BaseViewHolder?, gameType: GameType2) {
        var iconResId = context.resources.getIdentifier(gameType.iconName, "drawable", context.packageName)

        if(iconResId == -1) {
            iconResId = R.drawable.ic_royale
        }

        viewHolder?.setImageResource(R.id.game_type_image, iconResId)
        viewHolder?.setText(R.id.game_type_name, gameType.name)

        viewHolder?.addOnClickListener(R.id.game_type_image)
        viewHolder?.addOnClickListener(R.id.game_type_delete)

        val nameEditText = viewHolder?.getView<EditText>(R.id.game_type_name)
        nameEditText?.setOnEditorActionListener { view, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE && gameType.name != view.text.toString()) {
                gameType.name = view.text.toString()
                onNameChange(gameType)
                nameEditText.clearFocus()

                true
            }

            false
        }

        nameEditText?.clearFocus()

        if(gameType.needsEdit) {
            gameType.needsEdit = false
            Toast.makeText(App.getContext(), gameType.name, Toast.LENGTH_SHORT).show()
            editGameTypeName(nameEditText!!)
        }
    }

    abstract fun onNameChange(gameType: GameType2)
    abstract fun editGameTypeName(textView: EditText)
}