package com.stromberg.scott.seventenwouldstillsmash.fragment

import android.content.Context
import android.support.v4.app.Fragment
import com.github.clans.fab.FloatingActionButton
import java.util.ArrayList

abstract class BaseFragment : Fragment() {
    abstract fun setContentShown(shown: Boolean)

    open fun getFabButtons(context: Context): List<FloatingActionButton> {
        return ArrayList<FloatingActionButton>()
    }

    open fun addFabClicked() {}

    open fun hasFab(): Boolean {
        return false
    }
}