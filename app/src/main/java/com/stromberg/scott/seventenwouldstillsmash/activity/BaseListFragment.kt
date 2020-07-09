package com.stromberg.scott.seventenwouldstillsmash.activity

import androidx.fragment.app.Fragment

abstract class BaseListFragment : Fragment() {
    var hasFragmentBeenShown = false

    abstract fun setContentShown(shown: Boolean)
    abstract fun fabClicked()
}