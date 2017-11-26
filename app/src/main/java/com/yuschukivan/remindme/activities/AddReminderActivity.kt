package com.yuschukivan.remindme.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.text.format.DateFormat
import android.view.View
import android.widget.*
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import com.yuschukivan.remindme.R
import com.yuschukivan.remindme.common.utils.RealmConfig
import com.yuschukivan.remindme.common.utils.Util
import com.yuschukivan.remindme.common.utils.find
import com.yuschukivan.remindme.models.Categoty
import com.yuschukivan.remindme.mvp.presenters.AddReminderPresenter
import com.yuschukivan.remindme.mvp.views.AddReminderView
import io.realm.Realm
import java.util.*
import kotlin.collections.ArrayList
import android.graphics.Typeface
import android.widget.LinearLayout
import com.google.android.gms.location.places.ui.PlacePicker
import android.app.Activity
import android.app.ProgressDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import co.metalab.asyncawait.async


/**
 * Created by Ivan on 5/9/2017.
 */
class AddReminderActivity: MvpAppCompatActivity(), AddReminderView, AdapterView.OnItemSelectedListener{



    @InjectPresenter
    lateinit var presenter: AddReminderPresenter

    val realm = Realm.getInstance(RealmConfig.realmConfig)

    val toolbar: Toolbar by lazy { find<Toolbar>(R.id.add_reminder_toolbar).apply {
        setTitle(R.string.add_reminder)
    } }


    val progress: ProgressDialog by lazy {
        ProgressDialog(this).apply {
            title = "Location"
            setMessage("Opening maps")
            setCancelable(false)
        }
    }

    val dateInput by lazy {  findViewById(R.id.date_input) as EditText }
    val timeInput by lazy { findViewById(R.id.time_input) as EditText }
    val check by lazy { findViewById(R.id.add_check) as Button }
    val nameInput by lazy { findViewById(R.id.reminder_name_text) as EditText }
    val typeSpinner by lazy { findViewById(R.id.type_spinner) as Spinner }
    val prioritySpinner by lazy { findViewById(R.id.priority_spinner) as Spinner }

    val buttonsLayout by lazy { find<LinearLayout>(R.id.buttons_layout)}

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

    lateinit var typeAdapter: ArrayAdapter<Categoty>

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppDefault)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.avtivity_addreminder)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        typeAdapter = ArrayAdapter<Categoty>(this, android.R.layout.simple_spinner_item, mutableListOf())

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

        buttonsLayout.removeAllViews()

        presenter.loadButtons()

        check.setOnClickListener {
            presenter.onAddReminder(nameInput.text.toString(), dateInput.text.toString(), timeInput.text.toString())
        }

        dateInput.setOnClickListener {
            presenter.onShowChooseDate()
        }

        timeInput.setOnClickListener {
            presenter.onShowChooseTime()
        }
    }

    override fun onPause() {
        super.onPause()
        progress.hide()
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

    override fun addButton(index: String, day: String) {
        val scale = applicationContext.getResources().getDisplayMetrics().density
        val size = 42 * scale + 0.5f
        val margin = 4 * scale + 0.5f
        val params = LinearLayout.LayoutParams(size.toInt(), size.toInt())
        params.setMargins(margin.toInt(),margin.toInt(),margin.toInt(),margin.toInt())
        val btn = Button(this)
        btn.id = index.toInt()
        btn.typeface = Typeface.DEFAULT_BOLD
        btn.text = day
        btn.setBackgroundResource(R.drawable.circle_button)
        buttonsLayout.addView(btn, params)
        btn.setOnClickListener { presenter.onDayClick(btn.id) }
    }

    override fun highLight(id: Int, enable: Boolean) {
        val btn = findViewById(id)
        if(enable) btn.setBackgroundResource(R.drawable.circle_button_highlited)
        else btn.setBackgroundResource(R.drawable.circle_button)
    }


    override fun updateCategoriesSpinner(data: MutableList<Categoty>) {
        typeAdapter.clear()
        typeAdapter.addAll(data)
        typeAdapter.notifyDataSetChanged()
    }

    override fun finishActivity(position: Int) {
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

    override fun setTimeText(time: String) {
        timeInput.setText(time)
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
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun showError(s: String) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setMessage(s)
                .setTitle("Reminder")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.cancel()
                }

        val alert = builder.create()
        alert.show()
    }

}