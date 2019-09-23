package com.liberaid.wavevisualizer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs
import kotlin.math.min

@ExperimentalUnsignedTypes
class WaveVisualizer(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val waveReader = WaveReader()
    private val amplitudes = mutableListOf<Int>()

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

        paint.apply {
            style = Paint.Style.FILL
            color = barsColor
        }

        var x = barsOffset / 2f
        var y: Float
        var h: Float
        val maxAmp = amplitudes.max()?.toFloat() ?: 255f
        var frac: Float

        amplitudes.forEach{
            frac = it / maxAmp
            y = borderRect.height() / 2 - frac * borderRect.height()
            h = borderRect.height() / 2 + frac * borderRect.height()

            barRect.set(x, y, x + barWidth, h)
            canvas.drawRect(barRect, paint)

            x += barWidth + barsOffset
        }
    }

    fun loadWaveFromAssets(filename: String): Boolean {
        waveReader.readWaveHeaderFromAssets(context, filename)

        if(!waveReader.isHeaderRead || !waveReader.isHeaderValid() || !waveReader.isPCMFormat())
            return false

        amplitudes.clear()

        /* Hardcoded for 2 channels */
        val barsNumber = (borderRect.width() / (barWidth + barsOffset)).toInt()
        val samplesPerBar = waveReader.waveSamples / barsNumber
        var readSamples = (waveReader.bytesRead - WaveReader.HEADER_SIZE) / ( waveReader.bitsPerSample / 8 )

        while(readSamples < waveReader.waveSamples){
            val toRead = min(samplesPerBar, waveReader.waveSamples - readSamples)
            val average = waveReader.read16Average(toRead)[0]
            amplitudes.add(abs(average.toInt()))

            readSamples = (waveReader.bytesRead - WaveReader.HEADER_SIZE) / ( waveReader.bitsPerSample / 8 )
        }

        return true
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