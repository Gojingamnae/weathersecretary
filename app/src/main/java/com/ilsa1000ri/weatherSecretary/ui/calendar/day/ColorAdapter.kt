package com.ilsa1000ri.weatherSecretary.ui.calendar.day

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.ilsa1000ri.weatherSecretary.R

class ColorAdapter(
    context: Context,
    private val colorNames: Array<String>,
    private val eventColors: TypedArray,
    private var selectedColorIndex: Int
) : AlertDialog.Builder(context) {

    interface OnColorSelectedListener {
        fun onColorSelected(colorIndex: Int, colorName: String, color: Int)
    }

    private var listener: OnColorSelectedListener? = null

    fun setOnColorSelectedListener(listener: OnColorSelectedListener) {
        this.listener = listener
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun create(): AlertDialog {
        val radioGroup = RadioGroup(context).apply {
            orientation = RadioGroup.VERTICAL
        }

        for (i in colorNames.indices) {
            val radioButton = inflater.inflate(R.layout.custom_radio_button, null) as RadioButton
            radioButton.text = colorNames[i]
            radioButton.setTextColor(eventColors.getColor(i, 0))
            val colorDrawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(eventColors.getColor(i, 0))
            }
            radioButton.setCompoundDrawablesWithIntrinsicBounds(colorDrawable, null, null, null)
            radioButton.isChecked = i == selectedColorIndex
            radioButton.id = i
            radioGroup.addView(radioButton)
        }

        setView(radioGroup)

        setPositiveButton("선택") { dialog, _ ->
            for (i in 0 until radioGroup.childCount) {
                val radioButton = radioGroup.getChildAt(i) as RadioButton
                if (radioButton.isChecked) {
                    selectedColorIndex = i
                    listener?.onColorSelected(
                        selectedColorIndex,
                        colorNames[selectedColorIndex],
                        eventColors.getColor(selectedColorIndex, 0)
                    )
                    break
                }
            }
            dialog.dismiss()
        }

        setNegativeButton("취소") { dialog, _ ->
            dialog.dismiss()
        }

        return super.create()
    }
}
