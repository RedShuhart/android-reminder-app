package com.yuschukivan.remindme.features.task.assignreminder

import android.graphics.Typeface
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import com.arellomobile.mvp.MvpDialogFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.PresenterType
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import com.yuschukivan.remindme.R
import io.reactivex.subjects.PublishSubject
import java.util.*

/**
 * Created by yusch on 15.11.2017.
 */
class AssignReminderDialog: AssignReminderView, MvpDialogFragment() {


    @InjectPresenter(type = PresenterType.LOCAL)
    lateinit var presenter: AssignReminderPresenter

    lateinit var dateText: EditText
    lateinit var applyButton: Button
    lateinit var cancelButton: Button
    lateinit var deleteButton: Button

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

    val assigns = PublishSubject.create<ReminderInfo>()
    val deletes = PublishSubject.create<String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.assign_reminder_dialog, container, false)

        val mArgs = arguments
        val date: String = mArgs.getString("date", "")
        val repeats: String = mArgs.getString("repeats", "")
        val wasAssigned: Boolean = mArgs.getBoolean("wasAssigned", false)


        dateText = view.findViewById(R.id.time_text) as EditText
        buttonsLayout = view.findViewById(R.id.assign_reminder_buttons_layout) as LinearLayout
        applyButton = view.findViewById(R.id.assign_reminder_button) as Button
        cancelButton = view.findViewById(R.id.cancel) as Button
        deleteButton = view.findViewById(R.id.delete_assigned) as Button

        presenter.onLoad(date, repeats)

        dateText.setOnClickListener {
            presenter.onShowChooseDate()
        }

        if(wasAssigned) deleteButton.visibility = View.VISIBLE
            else deleteButton.visibility = View.GONE

        cancelButton.setOnClickListener { dismiss() }

        deleteButton.setOnClickListener { presenter.onDelete() }

        applyButton.setOnClickListener { presenter.onApply() }

        return view
    }

    companion object {

        class ReminderInfo (
                val date: String,
                val repeats: String
        )

        fun newInstance(date: String, repeats: String?, wasAssigned: Boolean): AssignReminderDialog {
            val args = Bundle()
            args.putString("date", date)
            args.putString("repeats", repeats)
            args.putBoolean("wasAssigned", wasAssigned)
            val dialog = AssignReminderDialog()
            dialog.arguments = args
            return dialog
        }
    }

    override fun showDatePicker() {
        dpd.show(fragmentManager, "Timepickerdialog")
    }

    override fun showTimePicker() {
        tpd.show(fragmentManager, "Timepickerdialog")
    }

    override fun setDateText(date: String) {
        dateText.setText(date)
    }

    override fun finishWithDelete() {
        deletes.onNext("deleted")
        dismiss()
    }

    override fun finishWithAssign(date: String, repeats: String) {
        assigns.onNext(ReminderInfo(date, repeats))
        dismiss()
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
}