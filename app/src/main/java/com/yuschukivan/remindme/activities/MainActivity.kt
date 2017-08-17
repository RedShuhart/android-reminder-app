package com.yuschukivan.remindme.activities

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.yuschukivan.remindme.R
import com.yuschukivan.remindme.adapters.TabsAdapter
import com.yuschukivan.remindme.common.utils.RealmConfig
import com.yuschukivan.remindme.common.utils.find
import com.yuschukivan.remindme.models.Categoty
import com.yuschukivan.remindme.mvp.presenters.MainPresenter
import com.yuschukivan.remindme.mvp.views.MainView
import io.realm.Realm
import io.realm.RealmConfiguration
import java.text.FieldPosition

/**
 * Created by Ivan on 5/9/2017.
 */
class MainActivity: MvpAppCompatActivity(), MainView {



    @InjectPresenter
    lateinit var presenter: MainPresenter

    val toolbar: Toolbar by lazy {
        find<Toolbar>(R.id.toolbar).apply {
            setTitle(R.string.app_name)
            setOnMenuItemClickListener { false }
            inflateMenu( R.menu.menu)
        }
    }

    val viewPager: ViewPager by lazy { find<ViewPager>(R.id.view_pager) }
    val tabLayout: TabLayout by lazy { find<TabLayout>(R.id.tab_layout) }
    lateinit var drawerLayout: DrawerLayout
    lateinit var toggle: ActionBarDrawerToggle

    lateinit var actionButton: FloatingActionButton
    lateinit var tabsAdapter: TabsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppDefault)
//        Realm.init(applicationContext)
//        realm = Realm.getInstance(RealmConfig.realmConfig)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setShowHideAnimationEnabled(true)
        initNavigationView()
        initTabLayout(0)
    }

    override fun onResume() {
        super.onResume()
    }

    fun initNavigationView() {
        drawerLayout = find<DrawerLayout>(R.id.drawer_layout)
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.view_nacigation_open, R.string.view_nacigation_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        actionButton = findViewById(R.id.action_button) as FloatingActionButton

        val navigationView = findViewById(R.id.navigation) as NavigationView
        navigationView.setNavigationItemSelectedListener { item ->
            drawerLayout.closeDrawers()
            when (item.itemId) {
                R.id.notification_item -> showAllNotificationsTab()
                R.id.calendar_item -> presenter.onCalendar()
            }
            false
        }

        actionButton.setOnClickListener { presenter.onAddReminder() }
    }

    fun initTabLayout(position: Int) {
        tabsAdapter = TabsAdapter(supportFragmentManager)
        presenter.loadCategories()
        viewPager.adapter = tabsAdapter
        tabLayout.setupWithViewPager(viewPager)
        viewPager.setCurrentItem(position, true)
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {
            R.id.add_category -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Category")
                val editText = EditText(this)
                editText.hint = "Category Name"
                builder.setView(editText)
                builder.setPositiveButton("Save",
                        DialogInterface.OnClickListener { dialog, which ->
                            presenter.onAddCategory(editText.text.toString())
                            viewPager.setCurrentItem(viewPager.adapter.count)
                        })
                builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                    dialog.cancel()
                })
                builder.show()
            }
            R.id.delete_category -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Delete Category")
                val title = tabsAdapter[viewPager.currentItem - 1].title
                if(title == "All" || title == "Default") {
                    builder.setMessage("Cannot delete this category")
                    builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                        dialog.cancel()
                    })
                    builder.show()
                } else {
                    builder.setMessage("Do you want to delete " + title + " category?")
                    builder.setPositiveButton("Delete",
                            DialogInterface.OnClickListener { dialog, which ->
                                presenter.onDeleteCategory(tabsAdapter[viewPager.currentItem - 1])
                            })
                    builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                        dialog.cancel()
                    })
                    builder.show()
                }
            }
        }
        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showAllNotificationsTab() {
        viewPager.currentItem = ALL_NOTIFICATIONS
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == ADD_REMINDER_REQUEST) {
            if(resultCode == Activity.RESULT_OK) viewPager.setCurrentItem(data.getIntExtra("position", 0))
        }
    }

    override fun goToCalendar() {
        startActivity(Intent(this,CalendarActivity::class.java))
    }


    override fun goToAddReminder() {
        val intent = Intent(this, AddReminderActivity::class.java)
        startActivity(intent)
    }

    override fun updateTabsAdapter(categories: List<Categoty>) {
        tabsAdapter.clear()
        tabsAdapter.addAll(categories)
        tabsAdapter.notifyDataSetChanged()
        viewPager.adapter = tabsAdapter
    }



    companion object {
        private val ALL_NOTIFICATIONS = 0
        private val ADD_REMINDER_REQUEST = 1
    }

    override fun showError(error: String) {
            val builder = android.app.AlertDialog.Builder(this)
            builder.setMessage(error)
                    .setTitle("Add Category")
                    .setCancelable(false)
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.cancel()
                    }

            val alert = builder.create()
            alert.show()
    }
}