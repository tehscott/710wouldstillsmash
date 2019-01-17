package com.stromberg.scott.seventenwouldstillsmash.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ajguan.library.EasyRefreshLayout
import com.ajguan.library.LoadModel
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase
import com.stromberg.scott.seventenwouldstillsmash.App
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.adapter.GameTypeListAdapter
import com.stromberg.scott.seventenwouldstillsmash.model.GameType2
import kotlinx.android.synthetic.main.activity_game_type_list.*
import java.util.*
import android.content.res.ColorStateList
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.stromberg.scott.seventenwouldstillsmash.util.*

class GameTypeListActivity: BaseActivity() {
    private var db = FirebaseDatabase.getInstance()
    private var recyclerView: RecyclerView? = null
    private var lastGameTypeEdited: GameType2? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_type_list)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerView = findViewById(R.id.recycler_view)

        recyclerView!!.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerView!!.addOnChildAttachStateChangeListener(object: RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                showTooltips()
            }

            override fun onChildViewDetachedFromWindow(view: View) {}
        })

        refresh_layout.loadMoreModel = LoadModel.NONE
        refresh_layout.addEasyEvent(object : EasyRefreshLayout.EasyEvent {
            override fun onRefreshing() {
                getGameTypes()
            }

            override fun onLoadMore() {}
        })

        empty_state_text_view.visibility = View.GONE

        fab.setOnClickListener {
            createGameType()
        }
    }

    override fun onResume() {
        super.onResume()

        getGameTypes()
    }

    override fun onBackPressed() {
        if(lastGameTypeEdited != null) {
            val data = Intent()
            data.putExtra("gameTypeId", lastGameTypeEdited!!.id)
            setResult(6969, data)
        }

        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getGameTypes() {
        setContentShown(false)

        var gameTypes = GameTypeHelper.getGameTypes()

        if(gameTypes != null) {
            gameTypes = ArrayList(gameTypes.filter { !it.isDeleted })

            val adapter = object : GameTypeListAdapter(this, gameTypes) {
                override fun onNameChange(gameType: GameType2) {
                    lastGameTypeEdited = gameType
                    updateGameType(gameType)
                }

                override fun editGameTypeName(textView: EditText) {
                    textView.postDelayed({
                        textView.requestFocus()
                        textView.selectAll()
                        val imm = App.getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showSoftInput(textView, InputMethodManager.SHOW_IMPLICIT)

                        this@GameTypeListActivity.recyclerView!!.smoothScrollToPosition(gameTypes.size - 1)
                    }, 250)
                }
            }
            adapter.setEnableLoadMore(false)

            adapter.onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->
                val gameType = (recyclerView!!.adapter!! as GameTypeListAdapter).gameTypes[position]

                when (view.tag) {
                    "game_type_image" -> {
                        showImageDialog(view as ImageView, gameType)
                    }
                    "game_type_delete" -> {
                        deleteGameType(gameType)
                    }
                }
            }

            recyclerView!!.adapter = adapter as RecyclerView.Adapter<*>

            datasetChanged(gameTypes.size > 0)
        }
        else {
            setContentShown(true)
        }
    }

    private fun datasetChanged(hasGameTypes: Boolean) {
        recyclerView?.adapter?.notifyDataSetChanged()
        refresh_layout.refreshComplete()
        empty_state_text_view.visibility = if (hasGameTypes) View.GONE else View.VISIBLE
        setContentShown(true)
    }

    override fun setContentShown(shown: Boolean) {
        game_type_progress.visibility = if(shown) View.GONE else View.VISIBLE
        refresh_layout.visibility = if(shown) View.VISIBLE else View.GONE
    }

    private fun showTooltips() {

    }

    private fun showImageDialog(imageView: ImageView, gameType: GameType2) {
        val gameTypeImages = intArrayOf(
                R.drawable.ic_delete,
                R.drawable.ic_games,
                R.drawable.ic_pencil,
                R.drawable.ic_pikachu,
                R.drawable.ic_players,
                R.drawable.ic_trophy
        )

        val builderSingle = AlertDialog.Builder(this)

        builderSingle.setTitle("Select Item")
        val arrayAdapter = object: ArrayAdapter<Int>(this, android.R.layout.simple_spinner_item, gameTypeImages.toTypedArray()) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                return getImageForPosition(position)
            }

            fun getImageForPosition(position: Int): ImageView {
                val imageView = ImageView(context)
                imageView.setImageResource(gameTypeImages[position])
                imageView.layoutParams = AbsListView.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT)
                ImageViewCompat.setImageTintList(imageView, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.text_secondary)))
                return imageView
            }
        }

        builderSingle.setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }

        builderSingle.setAdapter(arrayAdapter) { _, which ->
            val resId = arrayAdapter.getItem(which)

            imageView.setImageResource(resId)
            val imageName = resources.getResourceEntryName(resId)
            gameType.iconName = imageName

            lastGameTypeEdited = gameType
            updateGameType(gameType)
        }
        builderSingle.show()
    }

    private fun createGameType() {
        val gameType = GameType2(Calendar.getInstance().timeInMillis.toString(), "testing", "ic_royale", true)

        db.getReference(context = this)
                .child("gameTypes")
                .child(gameType.id)
                .setValue(gameType)
                .addOnCompleteListener {
                    lastGameTypeEdited = gameType
                    GameTypeHelper.addGameType(gameType)

                    (recyclerView!!.adapter!! as GameTypeListAdapter).gameTypes.add(gameType)
                    datasetChanged(true)
                }
                .addOnFailureListener {
                    Snackbar.make(recyclerView!!, "Failed to create game type", Snackbar.LENGTH_LONG)
                            .setBackgroundColor(R.color.primary)
                            .setTextAttributes(resources.getColor(R.color.text_primary, null), 20f)
                            .show()
                }
    }

    private fun deleteGameType(gameType: GameType2) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete " + gameType.name)
        builder.setMessage("You can't undo this. Are you sure?")
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            gameType.isDeleted = true
            (recyclerView!!.adapter!! as GameTypeListAdapter).gameTypes.remove(gameType)

            updateGameType(gameType, true)
        }
        builder.setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun updateGameType(gameType: GameType2, isDelete: Boolean = false) {
        db.getReference(context = this)
                .child("gameTypes")
                .child(gameType.id)
                .setValue(gameType)
                .addOnCompleteListener {
                    GameTypeHelper.updateGameType(gameType)

                    datasetChanged(true)

                    if(isDelete) {
                        Snackbar.make(recyclerView!!, "Game type deleted!", Snackbar.LENGTH_SHORT)
                                .setBackgroundColor(R.color.primary)
                                .setTextAttributes(resources.getColor(R.color.text_primary, null), 20f)
                                .show()

                    }
                    else {
                        Snackbar.make(recyclerView!!, "Game type updated!", Snackbar.LENGTH_SHORT)
                                .setBackgroundColor(R.color.primary)
                                .setTextAttributes(resources.getColor(R.color.text_primary, null), 20f)
                                .show()
                    }
                }
                .addOnFailureListener {
                    if(isDelete) {
                        Snackbar.make(recyclerView!!, "Failed to delete game type", Snackbar.LENGTH_LONG)
                                .setBackgroundColor(R.color.primary)
                                .setTextAttributes(resources.getColor(R.color.text_primary, null), 20f)
                                .show()
                    }
                    else {
                        Snackbar.make(recyclerView!!, "Failed to update game type", Snackbar.LENGTH_LONG)
                                .setBackgroundColor(R.color.primary)
                                .setTextAttributes(resources.getColor(R.color.text_primary, null), 20f)
                                .show()
                    }
                }
    }
}