package com.yuschukivan.remindme.fragments

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import co.metalab.asyncawait.async
import com.arellomobile.mvp.MvpAppCompatDialogFragment
import com.arellomobile.mvp.MvpDialogFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.PresenterType
import com.google.android.gms.location.places.ui.PlacePicker
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import com.yuschukivan.remindme.R
import com.yuschukivan.remindme.activities.AddReminderActivity
import com.yuschukivan.remindme.common.utils.Util
import com.yuschukivan.remindme.models.Categoty
import com.yuschukivan.remindme.models.Reminder
import com.yuschukivan.remindme.mvp.presenters.RemindDialogPresenter
import com.yuschukivan.remindme.mvp.views.RemindDialogView
import io.reactivex.subjects.PublishSubject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Ivan on 5/21/2017.
 */
class RemindDialogFragment: MvpDialogFragment(), RemindDialogView, AdapterView.OnItemSelectedListener {


    @InjectPresenter(type = PresenterType.LOCAL)
    lateinit var presenter: RemindDialogPresenter

    lateinit var dateText: EditText
    lateinit var timeText: EditText
    lateinit var priorityText: Spinner
    lateinit var categoryText: Spinner
    lateinit var commentText: TextView
    lateinit var titleText: TextView
    lateinit var deleteButton: Button
    lateinit var mapImage: ImageView
    lateinit var applyButton: Button
    lateinit var cancelButton: Button


    val priorityCategories = listOf<String>(Util.Priority.LOW, Util.Priority.NORMAL, Util.Priority.HIGH )

    lateinit var buttonsLayout: LinearLayout



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
            DateFormat.is24HourFormat(activity.applicationContext)
    ) }

    val deletes = PublishSubject.create<String>()
    val updates = PublishSubject.create<String>()

    lateinit var typeAdapter: ArrayAdapter<Categoty>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.remind_dialogfragment, container, false)

        val mArgs = arguments
        val date = mArgs.getString("date")
        val time = mArgs.getString("time")
        val priority = mArgs.getString("priority")
        val category = mArgs.getString("category")
        val comment = mArgs.getString("comment")
        val title = mArgs.getString("title")
        val id = mArgs.getLong("id")
        val map = mArgs.getByteArray("bitmap")

        dateText = view.findViewById(R.id.date_text) as EditText
        timeText = view.findViewById(R.id.time_text) as EditText
        priorityText = view.findViewById(R.id.priority_input) as Spinner
        categoryText = view.findViewById(R.id.category_input) as Spinner
        commentText = view.findViewById(R.id.comment) as EditText
        titleText = view.findViewById(R.id.title) as EditText
        deleteButton = view.findViewById(R.id.delete_button) as Button
        mapImage = view.findViewById(R.id.map_image) as ImageView
        buttonsLayout = view.findViewById(R.id.buttons_holder) as LinearLayout
        applyButton = view.findViewById(R.id.apply_changes) as Button
        cancelButton = view.findViewById(R.id.cancel) as Button

        typeAdapter = ArrayAdapter<Categoty>(activity.applicationContext, android.R.layout.simple_spinner_item, mutableListOf())

        presenter.getCategories()

        val priorityAdapter: ArrayAdapter<String> = ArrayAdapter<String>(activity.applicationContext, android.R.layout.simple_spinner_item, priorityCategories)

        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        categoryText.adapter = typeAdapter
        priorityText.adapter = priorityAdapter
        categoryText.prompt = "Category"
        priorityText.prompt = "Priority"

        categoryText.onItemSelectedListener = this
        priorityText.onItemSelectedListener = this


        dateText.setText(date)
        timeText.setText(time)
        presenter.onSetTypePosition(id)
        priorityText.setSelection(priorityCategories.indexOf(priority))
        commentText.setText(comment)
        titleText.setText(title)
        if(map != null) {
            mapImage.setImageBitmap(BitmapFactory.decodeByteArray(map, 0, map.size))
        } else {
            mapImage.visibility = View.GONE
        }

        deleteButton.setOnClickListener {
            presenter.deleteReminder(id)
        }

        presenter.loadButtons(id)

        dateText.setOnClickListener {
            presenter.onShowChooseDate()
        }

        timeText.setOnClickListener {
            presenter.onShowChooseTime()
        }

//        mapImage.setOnClickListener {
//            val builder = PlacePicker.IntentBuilder()
//            startActivityForResult(builder.build(activity), AddReminderActivity.PLACE_PICKER_REQUEST);
//        }

        mapImage.setOnClickListener {
            presenter.onMap(id)
        }

        cancelButton.setOnClickListener { dismiss() }

        applyButton.setOnClickListener { presenter.onApply(id, titleText.text.toString(), commentText.text.toString()) }
        applyButton.visibility = View.VISIBLE

        return view
    }

    override fun goToActivity(intent: Intent) {
        startActivityForResult(intent, MAP_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            AddReminderActivity.PLACE_PICKER_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {
                    async {
                        presenter.onLocationSelected(PlacePicker.getPlace(data, activity.applicationContext))

                    }
                }
            }
            MAP_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {
                    async {
                        presenter.onLocationSelected(PlacePicker.getPlace(data, activity.applicationContext))
                    }
                }
            }
        }
    }

    override fun close() {
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        deletes.onNext("ok")
        super.onDismiss(dialog)
    }

    override fun setTypeSpinner(position: Int) {
        categoryText.setSelection(position)
    }

    companion object {
        fun newInstance(reminder: Reminder): RemindDialogFragment {
            val args = Bundle()
            args.putLong("id", reminder.id)
            args.putString("category", reminder.type)
            args.putString("date", SimpleDateFormat("dd/MM/yyyy").format(reminder.date))
            args.putString("time", SimpleDateFormat("HH:mm").format(reminder.date))
            args.putString("priority", reminder.priority)
            args.putString("comment", reminder.description)
            args.putString("title", reminder.title)
            args.putByteArray("bitmap", reminder.mapImage)
            val dialog = RemindDialogFragment()
            dialog.arguments = args
            return dialog
        }

        val MAP_REQUEST = 2
    }

    override fun showTimePicker() {
        applyButton.visibility = View.VISIBLE
        tpd.show(fragmentManager, "Timepickerdialog")
    }

    override fun showDatePicker() {
        applyButton.visibility = View.VISIBLE
        dpd.show(fragmentManager, "Timepickerdialog")
    }

    override fun setTimeText(time: String) {

        timeText.setText(time)
    }

    override fun setDateText(date: String) {

        dateText.setText(date)
    }


    override fun updateCategoriesSpinner(data: MutableList<Categoty>) {
        typeAdapter.clear()
        typeAdapter.addAll(data)
        typeAdapter.notifyDataSetChanged()
    }

    override fun addButton(index: String, day: String) {
        val scale = activity.applicationContext.getResources().getDisplayMetrics().density
        val size = 42 * scale + 0.5f
        val margin = 4 * scale + 0.5f
        val params = LinearLayout.LayoutParams(size.toInt(), size.toInt())
        params.setMargins(margin.toInt(),margin.toInt(),margin.toInt(),margin.toInt())
        val btn = Button(activity.applicationContext)
        btn.id = index.toInt()
        btn.typeface = Typeface.DEFAULT_BOLD
        btn.text = day
        btn.setBackgroundResource(R.drawable.circle_button)
        buttonsLayout.addView(btn, params)
        btn.setOnClickListener {
            applyButton.visibility = View.VISIBLE
            presenter.onDayClick(btn.id) }
    }

    override fun highLight(id: Int, enable: Boolean) {
        val btn = view?.findViewById(id)
        if(enable) btn?.setBackgroundResource(R.drawable.circle_button_highlited)
        else btn?.setBackgroundResource(R.drawable.circle_button)
    }

    override fun setMapImage(bmp: Bitmap) {
        mapImage.setImageBitmap(bmp)
        showApply()
    }

    override fun showApply() {
        Log.d("Dialog", "Show")
        applyButton.visibility = View.VISIBLE
        Log.d("Dialog", "visibility: " + applyButton.visibility)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        return
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        showApply()
        val spinner = parent as Spinner
        when(spinner.id) {
            R.id.category_input -> {
                val type = parent.getItemAtPosition(position)
                presenter.onSetType(type as Categoty, position)
                showApply()
            }
            R.id.priority_input -> {
                val priority = parent.getItemAtPosition(position)
                presenter.onSetPriority(priority as String)
                showApply()
            }
        }
    }

    override fun showError(s: String) {
        val builder = android.app.AlertDialog.Builder(activity)
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