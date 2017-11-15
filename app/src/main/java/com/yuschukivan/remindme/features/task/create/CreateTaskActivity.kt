package com.yuschukivan.remindme.features.task.create

import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.Html
import android.text.format.DateFormat
import android.view.View
import android.widget.*
import co.metalab.asyncawait.async
import com.arellomobile.mvp.presenter.InjectPresenter
import com.google.android.gms.location.places.ui.PlacePicker
import com.makeramen.roundedimageview.RoundedImageView
import com.philliphsu.bottomsheetpickers.time.numberpad.NumberPadTimePickerDialog
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import com.yuschukivan.remindme.R
import com.yuschukivan.remindme.activities.BaseActivity
import com.yuschukivan.remindme.adapters.SubtaskListItemAdapter
import com.yuschukivan.remindme.common.utils.RealmConfig
import com.yuschukivan.remindme.common.utils.Util
import com.yuschukivan.remindme.common.utils.find
import com.yuschukivan.remindme.features.task.assignreminder.AssignReminderDialog
import com.yuschukivan.remindme.models.Categoty
import io.reactivex.android.schedulers.AndroidSchedulers
import io.realm.Realm
import org.w3c.dom.Text
import java.util.*

/**
 * Created by yusch on 08.11.2017.
 */
class CreateTaskActivity: BaseActivity(), CreateTaskView, AdapterView.OnItemSelectedListener {

    @InjectPresenter
    lateinit var presenter: CreateTaskPresenter

    val realm = Realm.getInstance(RealmConfig.realmConfig)

    val toolbar: Toolbar by lazy { find<Toolbar>(R.id.create_subtask_toolbar).apply {
        setTitle(R.string.task)
    } }


    val progress: ProgressDialog by lazy {
        ProgressDialog(this).apply {
            title = "Location"
            setMessage("Opening maps")
            setCancelable(false)
        }
    }

    val dateInput by lazy {  findViewById(R.id.date_input) as EditText }
    val estInput by lazy { findViewById(R.id.est_input) as EditText }
    val check by lazy { findViewById(R.id.add_task) as Button }
    val nameInput by lazy { findViewById(R.id.task_name_text) as EditText }
    val noteInput by lazy { findViewById(R.id.notesText) as EditText }
    val typeSpinner by lazy { findViewById(R.id.type_spinner) as Spinner }
    val prioritySpinner by lazy { findViewById(R.id.priority_spinner) as Spinner }
    val mapImage by lazy { find<RoundedImageView>(R.id.location_pcick)}
    val addSubtaskButton by lazy { find<ImageButton>(R.id.add_subtask_button) }
    val addMapButton by lazy { find<ImageButton>(R.id.add_map_button) }
    val addReminderButton by lazy { find<ImageButton>(R.id.add_reminder_button)}
    val reminderTime by lazy { find<TextView>(R.id.reminder_time) }
    val reminderRepeats by lazy { find<TextView>(R.id.reminder_repeats) }
    val subtasksView by lazy {find<RecyclerView>(R.id.subtasks_list)}

    val priorityCategories: List<String> = ArrayList<String>().apply {
        add(Util.Priority.LOW)
        add(Util.Priority.NORMAL)
        add(Util.Priority.HIGH)
    }



    val dpd by lazy { DatePickerDialog.newInstance(
            { view, year, monthOfYear, dayOfMonth ->
                presenter.onDateChoosen(year,monthOfYear,dayOfMonth)
            },
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    ) }

    val tpd by lazy { TimePickerDialog.newInstance(
            { view, hourOfDay, minute, second ->
                presenter.onTimeChoosen(hourOfDay,minute)
            },
            Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
            Calendar.getInstance().get(Calendar.MINUTE),
            DateFormat.is24HourFormat(applicationContext)
    ) }

    val timePikerDialog by lazy { NumberPadTimePickerDialog.newInstance(
            { view, hourOfDay, minute -> presenter.onEstChosen(hourOfDay, minute) }
    ) }

    lateinit var typeAdapter: ArrayAdapter<Categoty>

    lateinit var subtasksAdapter: SubtaskListItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppDefault)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_task_activity)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        typeAdapter = ArrayAdapter<Categoty>(this, android.R.layout.simple_spinner_item, mutableListOf())
        subtasksAdapter = SubtaskListItemAdapter()

        presenter.getCategories()

        val priorityAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, priorityCategories)

        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        typeSpinner.adapter = typeAdapter
        prioritySpinner.adapter = priorityAdapter
        typeSpinner.prompt = "Category"
        prioritySpinner.prompt = "Priority"

        typeSpinner.onItemSelectedListener = this
        prioritySpinner.onItemSelectedListener = this

        addMapButton.setOnClickListener {
            val builder = PlacePicker.IntentBuilder()
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        }

        addSubtaskButton.setOnClickListener { presenter.onAddSubtask() }


        check.setOnClickListener {
            presenter.onCreateTask(nameInput.text.toString(), noteInput.text.toString())
        }

        addReminderButton.setOnClickListener { presenter.onAssignReminder() }

        dateInput.setOnClickListener {
            presenter.onShowChooseDate()
        }

        estInput.setOnClickListener {
            presenter.onShowChooseEst()
        }
    }

    override fun onResume() {
        super.onResume()

        subtasksAdapter.removes
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    query -> presenter.onRemoveSubtask(query)
                }.unsubscribeOnDestroy()
    }

    override fun onPause() {
        super.onPause()
        progress.hide()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PLACE_PICKER_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {
                    async {
                        presenter.onLocationSelected(PlacePicker.getPlace(data, applicationContext))

                    }
                }
            }
        }
    }


    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val spinner = parent as Spinner
        when(spinner.id) {
            R.id.type_spinner -> {
                val type = parent.getItemAtPosition(position)
                presenter.onSetType(type as Categoty, spinner.selectedItemPosition)
            }
            R.id.priority_spinner -> {
                val priority = parent.getItemAtPosition(position)
                presenter.onSetPriority(priority as String)
            }
        }
    }

    override fun updateCategoriesSpinner(data: MutableList<Categoty>) {
        typeAdapter.clear()
        typeAdapter.addAll(data)
        typeAdapter.notifyDataSetChanged()
    }

    override fun finishWithOk(position: Int) {
        val data = Intent()
        data.putExtra("position", position)
        setResult(RESULT_OK, data);
        finish()
    }

    override fun showTimePicker() {
        tpd.show(fragmentManager, "Timepickerdialog")
    }

    override fun showDatePicker() {
        dpd.show(fragmentManager, "Timepickerdialog")
    }

    override fun showEstPicker() {
        timePikerDialog.show(supportFragmentManager, "Timepickerdialog")
    }

    override fun showEstText(time: String) {
        estInput.setText(time)
    }

    override fun setDateText(date: String) {
        dateInput.setText(date)
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        val PLACE_PICKER_REQUEST = 1
        val REMINDER_DIALOG_REQUEST = 2
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun showError(title: String, message: String) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setMessage(message)
                .setTitle(title)
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.cancel()
                }

        val alert = builder.create()
        alert.show()
    }

    override fun showAssignReminderDialog(date: String, repeats: String) {
        val dialog =  AssignReminderDialog.newInstance(date, repeats)
        dialog.assigns.observeOn(AndroidSchedulers.mainThread())
                .subscribe { query ->
                    presenter.onAddReminder(query.date, query.repeats)
                }.unsubscribeOnDestroy()
        dialog.show(fragmentManager, "TAG")
    }

    override fun showReminderInfo(time: String, repeats: String) {
        reminderRepeats.setText(Html.fromHtml(repeats))
        reminderTime.text = time
        reminderRepeats.visibility = View.VISIBLE
        reminderTime.visibility = View.VISIBLE
        addReminderButton.setImageResource(R.drawable.dots_vertical)
    }

    override fun removeReminderInfo() {
        reminderRepeats.text = ""
        reminderTime.text = ""
        reminderRepeats.visibility = View.GONE
        reminderTime.visibility = View.GONE
        addReminderButton.setImageResource(R.drawable.plus)
    }


    override fun addSubtaskToView(name: String) {
        subtasksView.visibility = View.VISIBLE
        subtasksAdapter.add(name)
        subtasksAdapter.notifyItemChanged(subtasksAdapter.size - 1)
    }

    override fun removeSubtaskFromView(name: String) {
        subtasksAdapter.remove(name)
        if(subtasksAdapter.isEmpty()) subtasksView.visibility = View.GONE
        subtasksAdapter.notifyDataSetChanged()
    }

    override fun showCreateSubtaskDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Create Subtask")
        val editText = EditText(this)
        editText.hint = "Subtask Name"
        builder.setView(editText)
        builder.setPositiveButton("Save",
                DialogInterface.OnClickListener { dialog, which ->
                    presenter.onCreateSubtask(editText.text.toString())
                })
        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
            dialog.cancel()
        })
        builder.show()
    }

    override fun setMapImage(bmp: Bitmap) {
        mapImage.setImageBitmap(bmp)
        addMapButton.setOnClickListener { presenter.onRemoveMap() }
        addMapButton.setImageResource(R.drawable.close)
        mapImage.setOnClickListener {
            val builder = PlacePicker.IntentBuilder()
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        }
    }

    override fun removeMapImage() {
        mapImage.setImageBitmap(null)
        addMapButton.setOnClickListener {
            val builder = PlacePicker.IntentBuilder()
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        }
        addMapButton.setImageResource(R.drawable.plus)
    }

}
