package com.stromberg.scott.seventenwouldstillsmash

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import com.stromberg.scott.seventenwouldstillsmash.fragment.BaseFragment
import com.stromberg.scott.seventenwouldstillsmash.fragment.GamesFragment
import com.stromberg.scott.seventenwouldstillsmash.fragment.PlayersFragment
import kotlinx.android.synthetic.main.activity_main.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

class MainActivity : AppCompatActivity() {
    private var mCurrentFragment: BaseFragment? = null
    private var mAddFabMenu: FloatingActionMenu? = null
    private var mAddFabButton: FloatingActionButton? = null

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_games -> {
                mCurrentFragment = GamesFragment()
            }
            R.id.navigation_players -> {
                mCurrentFragment = PlayersFragment()
            }
            R.id.navigation_statistics -> {
                mCurrentFragment = null
            }
            R.id.navigation_settings -> {
                mCurrentFragment = null
            }
        }

        if(mCurrentFragment != null) {
            supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, mCurrentFragment).commit()

            if(mCurrentFragment?.hasFab()!!) {
                val fabMenuButtons = mCurrentFragment?.getFabButtons(this)

                if(fabMenuButtons?.size!! > 0) {
                    mAddFabMenu?.visibility = View.VISIBLE
                    mAddFabButton?.visibility = View.GONE

                    mAddFabMenu?.removeAllMenuButtons()
                    fabMenuButtons.forEach( {fab -> run { mAddFabMenu?.addMenuButton(fab) } })
                }
                else {
                    mAddFabMenu?.visibility = View.GONE
                    mAddFabButton?.visibility = View.VISIBLE
                    mAddFabButton?.setOnClickListener { mCurrentFragment?.addFabClicked() }
                }
            }
            else {
                hideFabs()
            }
        }
        else {
            hideFabs()
        }

        return@OnNavigationItemSelectedListener true
    }

    private fun hideFabs() {
        mAddFabMenu?.visibility = View.GONE
        mAddFabButton?.visibility = View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAddFabMenu = findViewById(R.id.add_fab_menu)
        mAddFabButton = findViewById(R.id.add_fab_button)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation.selectedItemId = R.id.navigation_games
        navigation.itemTextColor = ColorStateList.valueOf(resources.getColor(R.color.text_secondary))
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    override fun dispatchTouchEvent(motionEvent: MotionEvent?): Boolean {
        if (motionEvent?.action == MotionEvent.ACTION_DOWN) {
            if(mCurrentFragment is PlayersFragment) {
                (mCurrentFragment as PlayersFragment).dismissSnackbar(motionEvent)
            }
        }

        return super.dispatchTouchEvent(motionEvent)
    }
}
