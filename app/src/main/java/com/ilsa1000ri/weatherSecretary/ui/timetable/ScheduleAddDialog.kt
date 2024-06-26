//ScheduleAddDialog
package com.ilsa1000ri.weatherSecretary.ui.timetable

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ilsa1000ri.weatherSecretary.R
import com.ilsa1000ri.weatherSecretary.databinding.DialogAddEventBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ScheduleAddDialog : DialogFragment() {
    private lateinit var binding: DialogAddEventBinding

    private var selectedNotificationMinutes: Int? = 10
    private var selectedColorIndex: Int = 7
    private val notifOptions = arrayOf("5분 전", "10분 전", "15분 전", "30분 전", "1시간 전", "1일 전")
    private val notifValues = arrayOf(5, 10, 15, 30, 60, 1440) // 각 옵션에 대응하는 분 값

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogAddEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val colorNames = resources.getStringArray(R.array.color_names)
        binding.colorSelectTextView.text = colorNames[selectedColorIndex]
        binding.colorSelectTextView.setOnClickListener {
            showColorPickerDialog(binding.colorSelectTextView)
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd(E)", Locale.getDefault())
        val timeFormat = SimpleDateFormat("a hh:mm", Locale.getDefault())
        val currentCalendar = Calendar.getInstance()

        val oneHourLaterCalendar = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 1)
        }

        binding.startTimeTextView.text = timeFormat.format(currentCalendar.time)
        binding.startDateTextView.text = dateFormat.format(currentCalendar.time)
        binding.endDateTextView.text = dateFormat.format(oneHourLaterCalendar.time)
        binding.endTimeTextView.text = timeFormat.format(oneHourLaterCalendar.time)

        binding.startTimeTextView.setOnClickListener {
            showTimePicker(binding.startTimeTextView)
        }
        binding.endTimeTextView.setOnClickListener {
            showTimePicker(binding.endTimeTextView)
        }
        binding.startDateTextView.setOnClickListener {
            showDatePicker(binding.startDateTextView, binding.endDateTextView)
        }
        binding.endDateTextView.setOnClickListener {
            showDatePicker(binding.startDateTextView, binding.endDateTextView)
        }

        binding.alarmTextView.setOnClickListener {
            showNotificationDialog(binding.alarmTextView)
        }

        binding.positiveButton.setOnClickListener {
            val title = binding.titleEditText.text.toString()
            val description = binding.descriptionEditText.text.toString()

            // 사용자가 선택한 날짜 및 시간을 가져와서 startDate 및 endDate 설정
            val startDateString = binding.startDateTextView.text.toString()
            Log.d("ScheduleAddDialog", "startDateString : ${startDateString}")

            val endDateString = binding.endDateTextView.text.toString()
            val startTimeString = binding.startTimeTextView.text.toString()
            val endTimeString = binding.endTimeTextView.text.toString()
            val startDate = parseDateTime(startDateString, startTimeString)
            val endDate = parseDateTime(endDateString, endTimeString)

            val alarm = selectedNotificationMinutes ?: 10
            Log.d("ScheduleAddDialog", "alarm : ${alarm}")

            val color = selectedColorIndex
            Log.d("ScheduleAddDialog", "color : ${color}")

            val db = FirebaseFirestore.getInstance()
            val auth = FirebaseAuth.getInstance()
            val userId = auth.currentUser!!.uid
            Log.d("ScheduleAddDialog", "$userId")

            // 시작 시간과 종료 시간 비교
            if (startDate >= endDate) {
                // 시작 시간이 종료 시간보다 늦은 경우 사용자에게 메시지 표시
                AlertDialog.Builder(requireContext())
                    .setTitle("Error")
                    .setMessage("시작 시간이 종료 시간보다 늦을 수 없습니다.")
                    .setPositiveButton("확인") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            } else {
                val dbDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val startDateString = dbDateFormat.format(startDate.time)
                val startTimeForTimeManager =
                    SimpleDateFormat("HHmm", Locale.getDefault()).format(startDate.time)
                val endTimeForTimeManager =
                    SimpleDateFormat("HHmm", Locale.getDefault()).format(endDate.time)

                val timeKey = "$startTimeForTimeManager$endTimeForTimeManager"

                db.collection(userId)
                    .document(startDateString)
                    .get()
                    .addOnSuccessListener { document ->
                        val existingTimes = document.data?.keys ?: emptySet()
                        var isOverlapping = false
                        for (existingTime in existingTimes) {
                            val existingStartTime = existingTime.substring(0, 4).toInt()
                            val existingEndTime = existingTime.substring(4).toInt()
                            // 새로운 일정과 기존 일정이 겹치는지 확인
                            if (existingStartTime < endTimeForTimeManager.toInt() && startTimeForTimeManager.toInt() < existingEndTime) {
                                isOverlapping = true
                                break
                            }
                        }
                        if (isOverlapping) {
                            // 겹치는 일정이 있으면 사용자에게 메시지 표시
                            AlertDialog.Builder(
                                requireContext(),
                                R.style.ColorDialogTheme
                            )
                                .setTitle("Error")
                                .setMessage("해당 시간에 이미 일정이 있습니다.")
                                .setPositiveButton("확인") { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .show()
                        } else {
                            val subEvent = hashMapOf(
                                "summary" to title,
                                "color" to color,
                                "description" to description,
                                "reminderMinute" to alarm
                            )

                            val docRef =
                                db.collection(userId).document(startDateString)
                            docRef.get()
                                .addOnSuccessListener { documentSnapshot ->
                                    if (documentSnapshot.exists()) {
                                        val updates = hashMapOf<String, Any>(
                                            "$timeKey" to subEvent // timeKey 필드에 subEvent 추가
                                        )
                                        docRef.update(updates)
                                            .addOnSuccessListener {
                                                Log.d(
                                                    "TimetableFragment",
                                                    "DocumentSnapshot successfully updated!"
                                                )
                                                dismiss()
                                            }
                                            .addOnFailureListener { e ->
                                                Log.w(
                                                    "TimetableFragment",
                                                    "Error updating document",
                                                    e
                                                )
                                            }
                                    } else {
                                        // 문서가 존재하지 않으면 set()을 사용하여 새로 만듭니다.
                                        val data = hashMapOf<String, Any>(
                                            "$timeKey" to subEvent
                                        )
                                        docRef.set(data)
                                            .addOnSuccessListener {
                                                Log.d(
                                                    "TimetableFragment",
                                                    "DocumentSnapshot successfully created!"
                                                )
                                                dismiss()
                                            }
                                            .addOnFailureListener { e ->
                                                Log.w(
                                                    "TimetableFragment",
                                                    "Error creating document",
                                                    e
                                                )
                                            }
                                    }
                                }.addOnFailureListener { e ->
                                    Log.e(
                                        "TimetableFragment",
                                        "Error checking document existence",
                                        e
                                    )
                                }
                        }
                    }
            }
        }
        binding.cancelAddEvent.setOnClickListener {
            dismiss()
        }
    }

    // DatePickerDialog 표시 함수
    private fun showDatePicker(textView: TextView, textView2: TextView) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd(E)", Locale.getDefault()) // 요일 추가
                textView.text = dateFormat.format(calendar.time)
                textView2.text = dateFormat.format(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    private fun showTimePicker(textView: TextView) {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                val timeFormat = SimpleDateFormat("a hh:mm", Locale.getDefault())
                textView.text = timeFormat.format(calendar.time)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
        timePickerDialog.show()
    }

    private fun showNotificationDialog(textView: TextView) {
        var selectedOption = 1 // 기본값으로 "10분 전"이 선택되도록 설정

        val builder = AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
        builder.setTitle("알림 추가")
            .setSingleChoiceItems(notifOptions, selectedOption) { _, which ->
                selectedOption = which
            }
            .setPositiveButton("확인") { dialog, _ ->
                if (selectedOption != -1) {
                    textView.text = notifOptions[selectedOption]
                    selectedNotificationMinutes = notifValues[selectedOption]
                }
                dialog.dismiss()
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }

        builder.create().show()
    }

    fun dpToPx(dp: Int): Int {
        val density = requireContext().resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun showColorPickerDialog(colorSelectTextView: TextView) {
        val colorNames = resources.getStringArray(R.array.color_names)
        val eventColors = resources.obtainTypedArray(R.array.event_colors)

        val adapter = com.ilsa1000ri.weatherSecretary.ui.calendar.day.ColorAdapter(
            requireContext(),
            colorNames,
            eventColors,
            selectedColorIndex
        )
        adapter.setOnColorSelectedListener(object : com.ilsa1000ri.weatherSecretary.ui.calendar.day.ColorAdapter.OnColorSelectedListener {
            override fun onColorSelected(colorIndex: Int, colorName: String, color: Int) {
                selectedColorIndex = colorIndex
                colorSelectTextView.text = colorName
                colorSelectTextView.setTextColor(color)
            }
        })
        val dialog = adapter.create()

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.white)
    }

    private fun parseDateTime(dateString: String, timeString: String): Date {
        val dateTimeString = "$dateString $timeString"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd(E) a hh:mm", Locale.getDefault())
        return dateFormat.parse(dateTimeString) ?: Date()
    }
}