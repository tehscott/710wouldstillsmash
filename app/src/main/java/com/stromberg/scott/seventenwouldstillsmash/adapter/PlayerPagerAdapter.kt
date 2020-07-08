package com.stromberg.scott.seventenwouldstillsmash.adapter

import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.stromberg.scott.seventenwouldstillsmash.model.Player
import com.stromberg.scott.seventenwouldstillsmash.util.toPx

class PlayerPagerAdapter(val context: Context, val players: ArrayList<Player>) : PagerAdapter() {
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val textview = TextView(context)
        val lp = LinearLayout.LayoutParams(80.toPx, 80.toPx)
        textview.layoutParams = lp
        textview.text = players[position].name
        textview.textSize = 18f
        textview.gravity = Gravity.CENTER
        textview.setLines(1)
        textview.setSingleLine()
        textview.ellipsize = TextUtils.TruncateAt.END

        container.addView(textview)

        return textview
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
        container.removeView(view as View)
    }

    override fun getCount(): Int {
        return players.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }
}