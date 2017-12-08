package com.yuschukivan.remindme.features.nearby

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.yuschukivan.remindme.R
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Marker
import com.yuschukivan.remindme.activities.BaseActivity
import com.yuschukivan.remindme.adapters.SubtaskItemAdapter
import com.yuschukivan.remindme.common.utils.Util
import com.yuschukivan.remindme.features.task.list.TasksFragment
import com.yuschukivan.remindme.features.task.list.TasksFragment.Companion.REQUEST_TASK_EDIT
import com.yuschukivan.remindme.features.task.view.TaskActivity
import com.yuschukivan.remindme.models.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by yusch on 29.10.2017.
 */
class NearByActivity: BaseActivity(), OnMapReadyCallback, NearByView, GoogleMap.OnMarkerClickListener {

    @InjectPresenter
    lateinit var presenter: NearByPresenter

    lateinit var mMap: GoogleMap
    lateinit var subtasksAdapter: SubtaskItemAdapter
    lateinit var actionsDialog: AlertDialog

    val toolbar by lazy { (findViewById(R.id.location_toolbar) as Toolbar).apply {
        title = "Near By"
    } }

    val taskCard by lazy { findViewById(R.id.card_view) as CardView}
    val taskName by lazy { findViewById(R.id.task_title) as TextView }
    val taskStatus by lazy { findViewById(R.id.task_status) as ImageView }
    val taskPriority by lazy { findViewById(R.id.task_priority) as TextView }
    val taskDate by lazy { findViewById(R.id.task_date) as TextView }
    val taskAddress by lazy { findViewById(R.id.address_text) as TextView }
    val showSubtasks by lazy { findViewById(R.id.show_subtasks) as TextView}
    val navigationButton by lazy { findViewById(R.id.buttonNavigate) as ImageButton}
    val subtasksView by lazy { findViewById(R.id.subtasks_view) as RecyclerView}
    val estimatedView by lazy { findViewById(R.id.task_estimated) as TextView }


    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppDefault)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby)
        setSupportActionBar(toolbar)
        taskCard.visibility = View.GONE
        navigationButton.visibility = View.GONE
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        setListners()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    fun setListners() {
        listOf(taskCard, taskName, taskStatus, taskPriority, taskDate, taskAddress)
                .forEach {
                    it.setOnLongClickListener {
                        Log.d("NearBy", "show actions")
                        presenter.onShowActions() }
                }
    }



    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true;
        presenter.getCurrentLocation()
        presenter.onMapLoad()
    }

    override fun showMarkers(closeTasks: MutableList<TaskLocationPair>) {
        mMap.clear()
        closeTasks.forEach { task ->
            mMap.addMarker(task.marker)
        }
    }

    override fun moveToCurrentLocation(longitude: Double, latitude: Double) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 13f))

        val cameraPosition = CameraPosition.Builder()
                .target(LatLng(latitude, longitude))      // Sets the center of the map to location user
                .zoom(17f)                   // Sets the zoom
                .bearing(90f)                // Sets the orientation of the camera to east
                .tilt(40f)                   // Sets the tilt of the camera to 30 degrees
                .build()                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        presenter.onTaskInfo(marker.title)
        return true
    }

    override fun showInfo(task: Task) {

        subtasksView.visibility = View.GONE
        estimatedView.text = "────"
        task.estimatedTime?.let {
            estimatedView.text = "${it / 60} H ${it % 60} Min"
        }

        showSubtasks.text = when {
            task.subTasks.isEmpty() -> "No Subtasks"
            else -> "Show Subtasks"
        }

        if(task.subTasks.isNotEmpty()) {
            showSubtasks.setOnClickListener {
                if(presenter.subtasksShown) {
                    presenter.subtasksShown = false
                    subtasksView.visibility = View.GONE
                    showSubtasks.text = "Show Subtasks"
                } else {
                    presenter.subtasksShown = true
                    subtasksView.visibility = View.VISIBLE
                    showSubtasks.text = "Hide Subtasks"
                }
            }
        } else {
            showSubtasks.text = "No Subtasks"
        }


        subtasksAdapter = SubtaskItemAdapter({
            subtaskId, subtaskPosition -> presenter.completesSubtask(subtaskId, subtaskPosition)
        })

        subtasksView.layoutManager = LinearLayoutManager(applicationContext)
        subtasksView.adapter = subtasksAdapter
        presenter.updateSubtasks(task)

        when(task.priority) {
            Util.Priority.HIGH -> taskPriority.setTextColor(Color.parseColor("#D50000"))
            Util.Priority.NORMAL -> taskPriority.setTextColor(Color.parseColor("#f57c00"))
            Util.Priority.LOW -> taskPriority.setTextColor(Color.parseColor("#1B5E20"))
        }

        setStateIcon(task)

        taskName.text = task.name
        taskPriority.text = "${task.priority} priority"
        taskDate.text = SimpleDateFormat("dd/MM/yyyy HH:mm").format(task.dueDate)
        taskAddress.text = task.address
        taskCard.visibility = View.VISIBLE
        navigationButton.visibility = View.VISIBLE
        navigationButton.setOnClickListener { presenter.findRoutes(task.latLong!!, task.address!!) }
    }

    override fun updateSubtasksView(subtasks: List<SubTask>) {
        subtasksAdapter.clear()
        subtasksAdapter.addAll(subtasks)
        subtasksAdapter.notifyDataSetChanged()
    }

    override fun showSubtasks() {
        subtasksView.visibility = View.VISIBLE
    }
    override fun hideInfo() {
        taskCard.visibility = View.GONE
    }

    override fun hideSubtasks() {
        subtasksView.visibility = View.GONE
    }

    override fun dispatchIntent(mapIntent: Intent) {
        startActivity(intent)
    }

    override fun updateSubtasksItem(position: Int) {
        subtasksAdapter.notifyItemChanged(position)
    }

    override fun startEditing(intent: Intent) {
        startActivityForResult(intent, REQUEST_TASK_EDIT)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == TasksFragment.REQUEST_TASK_EDIT) {
                if(resultCode == Activity.RESULT_OK) {
                    presenter.getCurrentLocation()
                    presenter.onMapLoad()
                    presenter.onTaskInfo()
                }
                onResume()
            }
        }
    }

    override fun setStateIcon(task: Task) {
        when {
            task.doneDate != null && task.dueDate!!.time < task.doneDate!!.time  -> {
                taskStatus.setBackgroundResource(R.drawable.calendar_check_yellow)
                taskStatus.setColorFilter(Color.parseColor("#1B5E20"))
            }
            task.dueDate!!.time < Calendar.getInstance().time.time -> {
                taskStatus.setBackgroundResource(R.drawable.alert_circle_outline)
                taskStatus.setColorFilter(Color.parseColor("#D50000"))
            }
            task.doneDate != null -> {
                taskStatus.setBackgroundResource(R.drawable.calendar_check)
                taskStatus.setColorFilter(Color.parseColor("#1B5E20"))
            }
            else -> {taskStatus.setBackgroundResource(R.color.mdtp_white)}
        }
    }

    override fun showActionsDialog(task: Task) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(task.name)
        val view = layoutInflater.inflate(R.layout.task_actions_dialog, null)
        val editButton = view.findViewById(R.id.edit_button) as Button
        editButton.setOnClickListener {
            presenter.onEditTask(task)
            actionsDialog.dismiss()
        }
        val deleteButton = view.findViewById(R.id.delete_button) as Button
        deleteButton.setOnClickListener {
            presenter.onDeleteTask(task)
            actionsDialog.dismiss()
            presenter.getCurrentLocation()
            presenter.onMapLoad()
        }
        val doneButton = view.findViewById(R.id.done_button) as Button
        if ( task.doneDate == null) {
            doneButton.setOnClickListener {
                presenter.onDoneTask(task)
                actionsDialog.dismiss()
            }
        } else {
            doneButton.text = "UNDO"
            doneButton.setCompoundDrawablesWithIntrinsicBounds(null, null, getDrawable(R.drawable.calendar_remove), null)
            doneButton.setOnClickListener {
                presenter.onUndoTask(task)
                actionsDialog.dismiss()
            }
        }

        builder.setView(view)
        actionsDialog = builder.create()
        actionsDialog.show()
    }
}