package com.stromberg.scott.seventenwouldstillsmash.fragment

import android.content.Context
import androidx.fragment.app.Fragment
import com.github.clans.fab.FloatingActionButton
import java.util.ArrayList

abstract class BaseFragment : Fragment() {
    abstract fun setContentShown(shown: Boolean)

    open fun addFabClicked() {}

    open fun hasFab(): Boolean {
        return false
    }
}