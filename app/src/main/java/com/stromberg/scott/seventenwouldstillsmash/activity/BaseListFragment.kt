package com.stromberg.scott.seventenwouldstillsmash.activity

import androidx.fragment.app.Fragment

abstract class BaseListFragment : Fragment() {
    var readyToShowTooltips = false
    var hasFragmentBeenShown = false

    abstract fun setContentShown(shown: Boolean)
    abstract fun fabClicked()
    abstract fun showTooltips()
}