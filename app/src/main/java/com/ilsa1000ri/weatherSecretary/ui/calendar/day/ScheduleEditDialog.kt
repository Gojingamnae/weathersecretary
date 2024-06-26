//ScheduleEditDialog
package com.ilsa1000ri.weatherSecretary.ui.calendar.day

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.res.TypedArray
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.ilsa1000ri.weatherSecretary.R
import com.ilsa1000ri.weatherSecretary.databinding.DialogUpdateEventBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.ilsa1000ri.weatherSecretary.ui.timetable.ColorAdapter
import com.ilsa1000ri.weatherSecretary.ui.timetable.Time

class ScheduleEditDialog(private var schedule: Schedule) : DialogFragment() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = auth.currentUser
    private lateinit var binding: DialogUpdateEventBinding
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd (E)", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("a hh:mm", Locale.getDefault())
    private var selectedColorIndex: Int = 7
    private val timeManagerFormat = SimpleDateFormat("HHmm", Locale.getDefault())
    lateinit var originDate:Date
    lateinit var originStartTime:Date
    lateinit var originEndTime:Date
    private val notifOptions = arrayOf("5분 전", "10분 전", "15분 전", "30분 전", "1시간 전", "1일 전")
    private val notifValues = arrayOf(5, 10, 15, 30, 60, 1440) // 각 옵션에 대응하는 분 값

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = DialogUpdateEventBinding.inflate(inflater, container, false).apply {
        binding = this
        setupUI()
    }.root

    private fun setupUI() {
        binding.titleEditText.setText(schedule.summary)
        binding.descriptionEditText.setText(schedule.description)
        binding.startDateTextView.text = dateFormat.format(schedule.startDate)
        originDate = schedule.startDate

        binding.startTimeTextView.text = timeFormat.format(schedule.startDate)
        originStartTime = schedule.startDate
        Log.d("ScheduleEditDialog", "originStartTime:${timeFormat.format(originStartTime)}")

        binding.endDateTextView.text = dateFormat.format(schedule.endDate)
        originEndTime = schedule.endDate
        Log.d("ScheduleEditDialog", "originEndTime:${timeFormat.format(originEndTime)}")

        binding.endTimeTextView.text = timeFormat.format(schedule.endDate)

        val colors = resources.obtainTypedArray(R.array.event_colors)
        val colorNames = resources.getStringArray(R.array.color_names)
        selectedColorIndex = findColorIndex(schedule.color, colors)

        binding.colorSelectTextView.text = colorNames[selectedColorIndex]
        binding.colorSelectTextView.setOnClickListener {
            showColorPickerDialog(binding.colorSelectTextView)
        }

        // startDateTextView 클릭 시 날짜 선택 다이얼로그 표시
        binding.startDateTextView.setOnClickListener {
            showDatePickerDialog(schedule.startDate) { date ->
                schedule = schedule.copy(startDate = date)
                binding.startDateTextView.text = dateFormat.format(date)
                binding.endDateTextView.text = dateFormat.format(date)
            }
        }

        // endDateTextView 클릭 시 날짜 선택 다이얼로그 표시
        binding.endDateTextView.setOnClickListener {
            showDatePickerDialog(schedule.endDate) { date ->
                schedule = schedule.copy(endDate = date)
                binding.startDateTextView.text = dateFormat.format(date)
                binding.endDateTextView.text = dateFormat.format(date)
            }
        }

        // startTimeTextView 클릭 시 시간 선택 다이얼로그 표시
        binding.startTimeTextView.setOnClickListener {
            showTimePickerDialog(schedule.startDate) { time ->
                schedule = schedule.copy(startDate = time)
                binding.startTimeTextView.text = timeFormat.format(time)
            }
        }

        // endTimeTextView 클릭 시 시간 선택 다이얼로그 표시
        binding.endTimeTextView.setOnClickListener {
            showTimePickerDialog(schedule.endDate) { time ->
                schedule = schedule.copy(endDate = time)
                binding.endTimeTextView.text = timeFormat.format(time)
            }
        }

        val StartTimeForTimeManager = timeManagerFormat.format(originStartTime)
        val EndTimeForTimeManager = timeManagerFormat.format(originEndTime)
        val originSummaryDocId = "$StartTimeForTimeManager$EndTimeForTimeManager"

        val currentStartDateString =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(schedule.startDate)
        colors.recycle()

        val initialNotifIndex =
            notifValues.indexOf(schedule.reminderMinute).takeIf { it != -1 } ?: 1
        binding.alarmTextView.text = notifOptions[initialNotifIndex]
        binding.alarmTextView.setOnClickListener {
            showNotificationDialog(binding.alarmTextView)
        }

        binding.positiveButton.setOnClickListener {
            val title = binding.titleEditText.text.toString()
            val description = binding.descriptionEditText.text.toString()
            val startDate = schedule.startDate
            val endDate = schedule.endDate
            val color = schedule.color
            val reminderMinute = schedule.reminderMinute

            val updatedSchedule = schedule.copy(
                summary = title,
                description = description,
                startDate = startDate,
                endDate = endDate,
                color = color,
                reminderMinute = reminderMinute
            )

            currentUser?.uid?.let { userId ->
                val newStartDateString =
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(startDate)

                val currentStartTimeForTimeManager = timeManagerFormat.format(schedule.startDate)
                val currentEndTimeForTimeManager = timeManagerFormat.format(schedule.endDate)
                val currentSummaryDocId =
                    "$currentStartTimeForTimeManager$currentEndTimeForTimeManager"

                val currentEventMapRef = db.collection(userId)
                    .document(currentStartDateString)

                val newEventMapRef = db.collection(userId)
                    .document(newStartDateString)

                // 요약 필드 업데이트
                updateSummaryFields(
                    originSummaryDocId, //이전 시작 시간 & 이전 끝나는 시간
                    currentSummaryDocId, //새로운 시작 시간 & 새로운 끝나는 시간
                    currentEventMapRef,
                    newEventMapRef,
                    color,
                    title,
                    description,
                    reminderMinute
                )

            }
        }

        binding.scheduleDeleteButton.setOnClickListener {
            currentUser?.uid?.let { userId ->

                val currentEventMapRef = db.collection(userId)
                    .document(currentStartDateString)

                currentEventMapRef.update(mapOf(originSummaryDocId to FieldValue.delete()))
                    .addOnSuccessListener {
                        Log.d(
                            "FirestoreSuccess",
                            "Document field $currentEventMapRef 의 $originSummaryDocId successfully deleted"
                        )
                        dismiss()
                    }
                    .addOnFailureListener { e ->
                        Log.e(
                            "FirestoreError",
                            "Error deleting document field $originSummaryDocId",
                            e
                        )
                    }
            }
        }
    }


    private fun updateSummaryFields(originSummaryDocId:String, summaryDocId: String, currentEventMapRef: DocumentReference,
                                    newEventMapRef: DocumentReference, color: Int, summary: String, description: String?, reminderMinute: Int) {
        val summaryData = mapOf(
            "color" to color,
            "summary" to summary,
            "description" to description,
            "reminderMinute" to reminderMinute
        )

        currentEventMapRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // If the document exists, delete the specific map entry
                currentEventMapRef.update(mapOf(originSummaryDocId to FieldValue.delete()))
                    .addOnSuccessListener {
                        Log.d("FirestoreSuccess", "Document field $currentEventMapRef 의 $originSummaryDocId successfully deleted")

                        // Add new data to the new document reference
                        newEventMapRef.set(mapOf(summaryDocId to summaryData))
                            .addOnSuccessListener {
                                Log.d("FirestoreSuccess", "Document $newEventMapRef successfully updated with new data")

                                // Check if the document is empty and delete if it is
                                currentEventMapRef.get().addOnSuccessListener { updatedDocument ->
                                    if (updatedDocument.exists() && updatedDocument.data.isNullOrEmpty()) {
                                        currentEventMapRef.delete()
                                            .addOnSuccessListener {
                                                Log.d("FirestoreSuccess", "Empty document $currentEventMapRef successfully deleted")
                                                dismiss()
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("FirestoreError", "Error deleting empty document $currentEventMapRef", e)
                                            }
                                    } else {
                                        dismiss()
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("FirestoreError", "Error updating document with new data", e)
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirestoreError", "Error deleting document field $summaryDocId", e)
                    }
            } else {
                // If the document does not exist, create a new one
                newEventMapRef.set(mapOf(summaryDocId to summaryData))
                    .addOnSuccessListener {
                        Log.d("FirestoreSuccess", "New document $newEventMapRef successfully created")
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirestoreError", "Error creating new $newEventMapRef document", e)
                    }
            }
        }.addOnFailureListener { e ->
            Log.e("FirestoreError", "Error checking $newEventMapRef document existence", e)
        }
    }




    private fun showDatePickerDialog(date: Date, onDateSet: (Date) -> Unit) {
        val calendar = Calendar.getInstance().apply { time = date }
        DatePickerDialog(requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                onDateSet(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePickerDialog(date: Date, onTimeSet: (Date) -> Unit) {
        val calendar = Calendar.getInstance().apply { time = date }
        TimePickerDialog(requireContext(),
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                onTimeSet(calendar.time)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun showColorPickerDialog(colorSelectTextView: TextView) {
        val colorNames = resources.getStringArray(R.array.color_names)
        val eventColors = resources.obtainTypedArray(R.array.event_colors)

        val adapter = ColorAdapter(requireContext(), colorNames, eventColors, selectedColorIndex)
        adapter.setOnColorSelectedListener(object : ColorAdapter.OnColorSelectedListener {
            override fun onColorSelected(colorIndex: Int, colorName: String, color: Int) {
                selectedColorIndex = colorIndex
                colorSelectTextView.text = colorName
                colorSelectTextView.setTextColor(color)
                schedule = schedule.copy(color = colorIndex)
            }
        })
        val dialog = adapter.create()

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.white)
    }

    private fun showNotificationDialog(textView: TextView) {
        var selectedOption = notifValues.indexOf(schedule.reminderMinute)

        val builder = AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
        builder.setTitle("알림 추가")
            .setSingleChoiceItems(notifOptions, selectedOption) { _, which ->
                selectedOption = which
            }
            .setPositiveButton("확인") { dialog, _ ->
                if (selectedOption != -1) {
                    textView.text = notifOptions[selectedOption]
                    schedule = schedule.copy(reminderMinute = notifValues[selectedOption])
                }
                dialog.dismiss()
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }

        builder.create().show()
    }

    private fun findColorIndex(color: Int?, colors: TypedArray): Int {
        color?.let {
            for (i in 0 until colors.length()) {
                if (colors.getColor(i, 0) == it) {
                    return i
                }
            }
        }
        return 0
    }

}