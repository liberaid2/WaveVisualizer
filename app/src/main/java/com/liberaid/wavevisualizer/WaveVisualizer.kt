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
    private val barRect = RectF()

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.WaveVisualizer, 0, 0).apply {
            try {
                borderColor = getColor(R.styleable.WaveVisualizer_borderColor, DEFAULT_BORDER_COLOR)
                bgColor = getColor(R.styleable.WaveVisualizer_bgColor, DEFAULT_BG_COLOR)
                delimiterColor = getColor(R.styleable.WaveVisualizer_delimiterColor, DEFAULT_DELIMITER_COLOR)
                barsColor = getColor(R.styleable.WaveVisualizer_barsColor, DEFAULT_BARS_COLOR)
                barWidth = getDimension(R.styleable.WaveVisualizer_barWidth, 16f)
                barsOffset = getDimension(R.styleable.WaveVisualizer_barsOffset, 8f)
            } finally {
                recycle()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val mw = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        val mh = MeasureSpec.getSize(heightMeasureSpec).toFloat()

        borderRect.set(0f, 0f, mw, mh)

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas ?: return

        paint.apply {
            style = Paint.Style.FILL
            color = bgColor
        }
        canvas.drawRect(borderRect, paint)

        paint.apply {
            style = Paint.Style.STROKE
            color = borderColor
        }
        canvas.drawRect(borderRect, paint)

        paint.color = delimiterColor
        canvas.drawLine(borderRect.left, borderRect.height() / 2f, borderRect.right, borderRect.height() / 2f, paint)

        val barsNumber = (borderRect.width() / (barWidth + barsOffset)).toInt()
        paint.apply {
            style = Paint.Style.FILL
            color = barsColor
        }

        var x = barsOffset / 2f
        val y = 8f
        val height = borderRect.height() - y

        for(i in 0 until barsNumber){
            barRect.set(x, y, x + barWidth, height)
            canvas.drawRect(barRect, paint)

            x += barWidth + barsOffset
        }
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

    var barWidth = 0f
    set(value) {
        field = value
        invalidate()
    }

    var barsOffset = 0f
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