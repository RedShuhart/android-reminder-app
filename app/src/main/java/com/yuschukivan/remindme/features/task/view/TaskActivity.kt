package com.yuschukivan.remindme.features.task.view

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.arellomobile.mvp.presenter.InjectPresenter
import com.yuschukivan.remindme.R
import com.yuschukivan.remindme.activities.*
import com.yuschukivan.remindme.adapters.TaskTabAdapter
import com.yuschukivan.remindme.common.utils.find
import com.yuschukivan.remindme.features.calendar.CalendarActivity
import com.yuschukivan.remindme.features.nearby.NearByActivity
import com.yuschukivan.remindme.features.task.create.CreateTaskActivity
import com.yuschukivan.remindme.models.Categoty

/**
 * Created by yusch on 08.11.2017.
 */
class TaskActivity: BaseActivity(), TaskView {

    @InjectPresenter
    lateinit var presenter: TaskPresenter

    val toolbar: Toolbar by lazy {
        find<Toolbar>(R.id.toolbar).apply {
            setTitle(R.string.tasks)
            setOnMenuItemClickListener { false }
            inflateMenu( R.menu.menu)
        }
    }

    val viewPager by lazy { find<ViewPager>(R.id.view_pager) }
    val tabLayout by lazy { find<TabLayout>(R.id.tab_layout) }
    val actionButton by lazy {find<FloatingActionButton>(R.id.action_button)}

    val doneButton: Button by lazy {find<Button>(R.id.DONE)}
    val overdueButton: Button by lazy {find<Button>(R.id.OVERDUE)}

    lateinit var drawerLayout: DrawerLayout
    lateinit var toggle: ActionBarDrawerToggle
    lateinit var tabsAdapter: TaskTabAdapter

    val filterButtons = mutableListOf<Button>()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppDefault)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.task_activity)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setShowHideAnimationEnabled(true)
        filterButtons.add(doneButton)
        filterButtons.add(overdueButton)
        filterButtons.forEach { it.setOnClickListener { onChooseFiltering(it as Button)  } }
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

        val navigationView = findViewById(R.id.navigation) as NavigationView
        navigationView.setNavigationItemSelectedListener { item ->
            drawerLayout.closeDrawers()
            when (item.itemId) {
                R.id.notification_item -> presenter.onMainActivity()
                R.id.calendar_item -> presenter.onCalendar()
                R.id.nearby_item -> presenter.dispatchLocationIntent()
                R.id.statistics_item -> presenter.onStatistics()
            }
            false
        }

        actionButton.setOnClickListener { presenter.onAddTask() }
    }

    fun initTabLayout(position: Int) {
        presenter.loadCategories()
        tabLayout.setupWithViewPager(viewPager)
        viewPager.setCurrentItem(position, true)
        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                presenter.saveCurrentTab(tab.position)
            }
        })
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
                    builder.setMessage("Do you want to delete $title category?")
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CREATE_TASK_REQUEST) {
            if(resultCode == Activity.RESULT_OK) {
                data?.let { viewPager.currentItem = data.getIntExtra("position", 0)}
            }
        }
    }

    override fun goToCalendar() {
        startActivity(Intent(this, CalendarActivity::class.java))
    }

    override fun goToNearBy() {
        startActivity(Intent(this, NearByActivity::class.java))
    }

    override fun goToAddTask() {
        val intent = Intent(this, CreateTaskActivity::class.java)
        startActivity(intent)
    }

    override fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun goToStatistics(intent: Intent) {
        startActivity(intent)
    }

    override fun updateTabsAdapter(categories: List<Categoty>, filters: MutableList<String>, deleted: Boolean) {
        tabsAdapter = TaskTabAdapter(supportFragmentManager, filters)
        tabsAdapter.addAll(categories)
        tabsAdapter.notifyDataSetChanged()
        viewPager.adapter = tabsAdapter
        if(deleted) viewPager.currentItem = 0
            else viewPager.currentItem = presenter.currentTab
    }

    companion object {
        private val ALL_NOTIFICATIONS = 0
        private val CREATE_TASK_REQUEST = 1
        private val LOCATION_FINE_PERMISSION_REQUEST = 4
    }

    override fun showError(s: String) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setMessage(s)
                .setTitle("Add Category")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.cancel()
                }

        val alert = builder.create()
        alert.show()
    }

    override fun askPermissions() {
        ActivityCompat.requestPermissions(this,
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_FINE_PERMISSION_REQUEST)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_FINE_PERMISSION_REQUEST -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    presenter.dispatchLocationIntent()

                } else {
                    Toast.makeText(this,"Please allow location tracking", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    override fun highLight(id: Int, enable: Boolean) {
        val btn = findViewById(id) as Button
        if (enable) {
            btn.setTextColor(Color.parseColor("#006064"))
            val color = Color.parseColor("#006064");
            val mode = PorterDuff.Mode.SRC_ATOP;
            btn.compoundDrawables[1].setColorFilter(color,mode)
        } else {
            btn.setTextColor(Color.parseColor("#A1A1A1"))
            val color = Color.parseColor("#A1A1A1");
            val mode = PorterDuff.Mode.SRC_ATOP;
            btn.compoundDrawables[1].setColorFilter(color,mode)
        }
    }

    fun onChooseFiltering(myButton: Button) {
        presenter.setFilter(myButton.id)
    }
}
