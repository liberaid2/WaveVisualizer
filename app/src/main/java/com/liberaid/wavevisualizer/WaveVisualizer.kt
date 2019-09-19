package com.liberaid.wavevisualizer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class WaveVisualizer(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val borderRect = RectF()
    private val paint = Paint()

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.WaveVisualizer, 0, 0).apply {
            try {
                borderColor = getColor(R.styleable.WaveVisualizer_borderColor, DEFAULT_BORDER_COLOR)
                bgColor = getColor(R.styleable.WaveVisualizer_bgColor, DEFAULT_BG_COLOR)
                delimiterColor = getColor(R.styleable.WaveVisualizer_delimiterColor, DEFAULT_DELIMITER_COLOR)
                barsColor = getColor(R.styleable.WaveVisualizer_barsColor, DEFAULT_BARS_COLOR)
            } finally {
                recycle()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = measureHeight(heightMeasureSpec)

        borderRect.apply {
            right = width.toFloat()
            bottom = height.toFloat()
        }

        setMeasuredDimension(width, height)
    }

    private fun measureHeight(spec: Int): Int {
        val mode = MeasureSpec.getMode(spec)
        val specSize = MeasureSpec.getSize(spec)

        return when(mode){
            MeasureSpec.EXACTLY -> specSize
            MeasureSpec.AT_MOST -> Math.min(specSize, MIN_HEIGHT)
            else -> MIN_HEIGHT
        }
    }

    override fun onDraw(canvas: Canvas?) {
        canvas ?: return

        paint.color = bgColor
        canvas.drawRect(borderRect, paint)
    }


    var borderColor: Int = 0
    set(value) {
        field = value
        invalidate()
    }

    var bgColor: Int = 0
    set(value) {
        field = value
        invalidate()
    }

    var delimiterColor: Int = 0
    set(value) {
        field = value
        invalidate()
    }

    var barsColor: Int = 0
    set(value) {
        field = value
        invalidate()
    }

    companion object {
        const val DEFAULT_BORDER_COLOR = Color.TRANSPARENT
        const val DEFAULT_DELIMITER_COLOR = Color.DKGRAY
        const val DEFAULT_BARS_COLOR = Color.GRAY
        val DEFAULT_BG_COLOR = Color.parseColor("#77ed4c")

        const val MIN_HEIGHT = 300
    }
}