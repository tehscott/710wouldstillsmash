package com.stromberg.scott.seventenwouldstillsmash.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.stromberg.scott.seventenwouldstillsmash.activity.CharactersListFragment
import com.stromberg.scott.seventenwouldstillsmash.activity.GamesListFragment
import com.stromberg.scott.seventenwouldstillsmash.activity.PlayersListFragment

class ListPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {
    override fun getCount(): Int {
        return NUM_ITEMS
    }

    override fun getItem(position: Int): Fragment? {
        return when (position) {
            0 -> GamesListFragment()
            1 -> PlayersListFragment()
            2 -> CharactersListFragment()
            else -> null
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return "Page $position"
    }

    companion object {
        private const val NUM_ITEMS = 3
    }

}