package com.ilsa1000ri.weatherSecretary.ui.timetable

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Point
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import java.util.Arrays
import com.ilsa1000ri.weatherSecretary.R

class TimetableView : LinearLayout {
    private var rowCount = 0
    private var columnCount = 0
    private var cellHeight = 0
    private var sideCellWidth = 0
    private var headerTitle: Array<String> = arrayOf()
    private var stickerColors: Array<String> = arrayOf()
    private var startTime = 0
    private var stickerBox: RelativeLayout? = null
    var tableHeader: TableLayout? = null
    var tableBox: TableLayout? = null
    private var context: Context
    var stickers = HashMap<Int, Sticker>()
    private var stickerCount = -1
    private var stickerSelectedListener: OnStickerSelectedListener? = null

    constructor(context: Context) : super(context, null) {
        this.context = context
    }

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.context = context
        getAttrs(attrs)
        init()
    }

    private fun getAttrs(attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.TimetableView)
        rowCount = a.getInt(R.styleable.TimetableView_row_count, DEFAULT_ROW_COUNT) - 1
        columnCount = a.getInt(R.styleable.TimetableView_column_count, DEFAULT_COLUMN_COUNT)
        cellHeight = a.getDimensionPixelSize(
            R.styleable.TimetableView_cell_height, dp2Px(DEFAULT_CELL_HEIGHT_DP)
        )
        sideCellWidth = a.getDimensionPixelSize(
            R.styleable.TimetableView_side_cell_width, dp2Px(DEFAULT_SIDE_CELL_WIDTH_DP)
        )
        val titlesId = a.getResourceId(R.styleable.TimetableView_header_title, R.array.header_title)
        headerTitle = resources.getStringArray(titlesId)
        val colorsId = a.getResourceId(R.styleable.TimetableView_sticker_colors, R.array.default_sticker_color)
        stickerColors = resources.getStringArray(colorsId)
        startTime = a.getInt(R.styleable.TimetableView_start_time, DEFAULT_START_TIME)
        a.recycle()
    }

    private fun init() {
        val layoutInflater =
            getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = layoutInflater.inflate(R.layout.view_timetable, this, false)
        addView(view)

        stickerBox = view.findViewById<RelativeLayout>(R.id.sticker_box)
        tableHeader = view.findViewById<TableLayout>(R.id.table_header)
        tableBox = view.findViewById<TableLayout>(R.id.table_box)
        createTable()
    }

    fun setOnStickerSelectEventListener(listener: OnStickerSelectedListener?) {
        stickerSelectedListener = listener
    }

    fun add(schedules: List<Schedules>) {
        for (schedule in schedules) {
            val tv = TextView(context)
            val param = createStickerParam(schedule)
            tv.layoutParams = param
            tv.setPadding(10, 0, 10, 0)
            tv.text = schedule.Title
            tv.setTextColor(Color.parseColor("#FFFFFF"))
            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_STICKER_FONT_SIZE_DP.toFloat())
            tv.setTypeface(null, Typeface.BOLD)
            tv.setOnClickListener {
                stickerSelectedListener?.OnStickerSelected(stickerCount + 1, schedules)
            }
            val sticker = Sticker().apply {
                addTextView(tv)
                addSchedule(schedule)
            }
            stickers[++stickerCount] = sticker
            stickerBox!!.addView(tv)
            setStickerColor()
        }
    }

    fun createSaveData(): String {
        return SaveManager.saveSticker(stickers)
    }

    fun load(data: String?) {
        removeAll()
        stickers = SaveManager.loadSticker(data)
        var maxKey = 0
        for (key in stickers.keys) {
            val schedules = stickers[key]!!.schedules
            add(schedules)
            if (maxKey < key) maxKey = key
        }
        stickerCount = maxKey + 1
        setStickerColor()
    }

    fun removeAll() {
        stickerBox?.removeAllViews()
        stickers.clear()
        stickerCount = -1 // 초기화
    }

    fun edit(idx: Int, schedules: ArrayList<Schedules>) {
        remove(idx)
        add(schedules)
    }

    fun remove(idx: Int) {
        val sticker = stickers[idx]
        for (tv in sticker!!.view) {
            stickerBox!!.removeView(tv)
        }
        stickers.remove(idx)
        setStickerColor()
    }

    private fun setStickerColor() {
        val size = stickers.size
        val orders = IntArray(size)
        var i = 0
        for (key in stickers.keys) {
            orders[i++] = key
        }
        Arrays.sort(orders)
        val colorSize = stickerColors.size
        i = 0
        while (i < size) {
            for (v in stickers[orders[i]]!!.view) {
                v.setBackgroundColor(Color.parseColor(stickerColors[i % colorSize]))
            }
            i++
        }
    }

    private fun createTable() {
        createTableHeader()
        for (i in 0 until rowCount) {
            val tableRow = TableRow(context)
            tableRow.layoutParams = createTableLayoutParam()
            for (k in 0 until columnCount) {
                val tv = TextView(context)
                tv.layoutParams = createTableRowParam(cellHeight)
                if (k == 0) {
                    tv.text = getHeaderTime(i)
                    tv.setTextColor(resources.getColor(com.ilsa1000ri.weatherSecretary.R.color.colorHeaderText))
                    tv.setTextSize(
                        TypedValue.COMPLEX_UNIT_DIP,
                        DEFAULT_SIDE_HEADER_FONT_SIZE_DP.toFloat()
                    )
                    tv.setBackgroundColor(resources.getColor(com.ilsa1000ri.weatherSecretary.R.color.colorHeader))
                    tv.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                    tv.layoutParams = createTableRowParam(sideCellWidth, cellHeight)
                } else {
                    tv.text = ""
                    tv.background = resources.getDrawable(com.ilsa1000ri.weatherSecretary.R.drawable.item_border)
                    tv.gravity = Gravity.RIGHT
                }
                tableRow.addView(tv)
            }
            tableBox!!.addView(tableRow)
        }
    }

    private fun createTableHeader() {
        val tableRow = TableRow(context)
        tableRow.layoutParams = createTableLayoutParam()
        for (i in 0 until columnCount) {
            val tv = TextView(context)
            if (i == 0) {
                tv.layoutParams = createTableRowParam(sideCellWidth, cellHeight)
            } else {
                tv.layoutParams = createTableRowParam(cellHeight)
            }
            tv.setTextColor(resources.getColor(com.ilsa1000ri.weatherSecretary.R.color.colorHeaderText))
            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_HEADER_FONT_SIZE_DP.toFloat())
            tv.text = headerTitle[i]
            tv.gravity = Gravity.CENTER
            tableRow.addView(tv)
        }
        tableHeader!!.addView(tableRow)
    }

    private fun createStickerParam(schedule: Schedules): RelativeLayout.LayoutParams {
        val cell_w = calCellWidth()
        val param = RelativeLayout.LayoutParams(cell_w, calStickerHeightPx(schedule))
        param.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        param.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        param.setMargins(
            sideCellWidth + cell_w * schedule.day,
            schedule.startTime?.let { calStickerTopPxByTime(it) } ?: 0,
            0,
            0
        )
        return param
    }

    private fun calCellWidth(): Int {
        val display =
            (context as Activity).windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return (size.x - paddingLeft - paddingRight - sideCellWidth) / (columnCount - 1)
    }

    private fun calStickerHeightPx(schedule: Schedules): Int {
        val startTopPx = schedule.startTime?.let { calStickerTopPxByTime(it) } ?: 0
        val endTopPx = schedule.endTime?.let { calStickerTopPxByTime(it) } ?: 0
        return endTopPx - startTopPx
    }

    private fun calStickerTopPxByTime(time: Time): Int {
        return (time.hour - startTime) * cellHeight + (time.minute / 60.0f * cellHeight).toInt()
    }

    private fun createTableLayoutParam(): TableLayout.LayoutParams {
        return TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.MATCH_PARENT
        )
    }

    private fun createTableRowParam(h_px: Int): TableRow.LayoutParams {
        return TableRow.LayoutParams(calCellWidth(), h_px)
    }

    private fun createTableRowParam(w_px: Int, h_px: Int): TableRow.LayoutParams {
        return TableRow.LayoutParams(w_px, h_px)
    }

    private fun getHeaderTime(i: Int): String {
        val p = (startTime + i) % 24
        val res = if (p <= 12) p else p - 12
        return res.toString()
    }

    private fun onCreateByBuilder(builder: Builder) {
        rowCount = builder.getRowCount()
        columnCount = builder.getColumnCount()
        cellHeight = builder.getCellHeight()
        sideCellWidth = builder.getSideCellWidth()
        headerTitle = builder.getHeaderTitle()
        stickerColors = builder.getStickerColors()
        startTime = builder.getStartTime()
        init()
    }

    interface OnStickerSelectedListener {
        fun OnStickerSelected(idx: Int, schedules: List<Schedules>?)
    }

    internal class Builder(private val context: Context) {
        private var rowCount: Int
        private var columnCount: Int
        private var cellHeight: Int
        private var sideCellWidth: Int
        private var headerTitle: Array<String>
        private var stickerColors: Array<String>
        private var startTime: Int

        init {
            rowCount = DEFAULT_ROW_COUNT
            columnCount = DEFAULT_COLUMN_COUNT
            cellHeight = dp2Px(DEFAULT_CELL_HEIGHT_DP)
            sideCellWidth = dp2Px(DEFAULT_SIDE_CELL_WIDTH_DP)
            headerTitle = context.resources.getStringArray(R.array.header_title)
            stickerColors = context.resources.getStringArray(R.array.default_sticker_color)
            startTime = DEFAULT_START_TIME
        }
        fun getRowCount() = rowCount
        fun getColumnCount() = columnCount
        fun getCellHeight() = cellHeight
        fun getSideCellWidth() = sideCellWidth
        fun getHeaderTitle() = headerTitle
        fun getStickerColors() = stickerColors
        fun getStartTime() = startTime

        fun build(): TimetableView {
            val timetableView = TimetableView(context)
            timetableView.onCreateByBuilder(this)
            return timetableView
        }
    }

    companion object {
        private const val DEFAULT_ROW_COUNT = 25
        private const val DEFAULT_COLUMN_COUNT = 8
        private const val DEFAULT_CELL_HEIGHT_DP = 50
        private const val DEFAULT_SIDE_CELL_WIDTH_DP = 30
        private const val DEFAULT_START_TIME = 0
        private const val DEFAULT_SIDE_HEADER_FONT_SIZE_DP = 13
        private const val DEFAULT_HEADER_FONT_SIZE_DP = 15
        private const val DEFAULT_STICKER_FONT_SIZE_DP = 13
        private fun dp2Px(dp: Int): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }
    }
}