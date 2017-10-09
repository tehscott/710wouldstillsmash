package com.stromberg.scott.seventenwouldstillsmash.fragment

import android.content.Context
import android.support.v4.app.Fragment
import com.github.clans.fab.FloatingActionButton

abstract class BaseFragment : Fragment() {
    abstract fun addFabClicked()
    abstract fun hasFab(): Boolean
    abstract fun getFabButtons(context: Context): List<FloatingActionButton>
    abstract fun setContentShown(shown: Boolean)
}