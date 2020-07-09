package com.stromberg.scott.seventenwouldstillsmash.adapter

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.model.Characters
import com.stromberg.scott.seventenwouldstillsmash.util.CharacterHelper
import com.stromberg.scott.seventenwouldstillsmash.util.toPx
import de.hdodenhof.circleimageview.CircleImageView

class CharacterPagerAdapter(val context: Context) : PagerAdapter() {
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val character = Characters.byId(position)

        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.character_pager_item, container, false)

        val image = layout.findViewById<CircleImageView>(R.id.character_pager_image)
        image.setImageResource(character?.imageRes ?: 0)
//        val lp = LinearLayout.LayoutParams(80.toPx, 80.toPx)
//        lp.gravity = Gravity.CENTER
//        image.layoutParams = lp

        layout.findViewById<TextView>(R.id.character_pager_name).setText(character?.characterName)

        container.addView(layout)

        return layout
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
        container.removeView(view as View)
    }

    override fun getCount(): Int {
        return Characters.count()
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }
}