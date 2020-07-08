package com.stromberg.scott.seventenwouldstillsmash.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.stromberg.scott.seventenwouldstillsmash.activity.CharactersListFragment
import com.stromberg.scott.seventenwouldstillsmash.activity.GamesListFragment
import com.stromberg.scott.seventenwouldstillsmash.activity.PlayersListFragment

class ListPagerAdapter(framentActivity: FragmentActivity) : FragmentStateAdapter(framentActivity) {
    private val gamesListFragment = GamesListFragment()
    private val playersListFragment = PlayersListFragment()
    private val charactersListFragment = CharactersListFragment()

    override fun getItemCount(): Int {
        return NUM_ITEMS
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            1 -> playersListFragment
            2 -> charactersListFragment
            else -> gamesListFragment
        }
    }

    fun getFragmentAt(position: Int): Fragment {
        return when (position) {
            1 -> playersListFragment
            2 -> charactersListFragment
            else -> gamesListFragment
        }
    }

    companion object {
        private const val NUM_ITEMS = 3
    }
}