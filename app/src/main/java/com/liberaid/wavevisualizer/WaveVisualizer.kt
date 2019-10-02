package com.liberaid.wavevisualizer

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.animation.addListener
import java.io.File
import kotlin.math.abs
import kotlin.math.min

@ExperimentalUnsignedTypes
class WaveVisualizer(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val waveReader = WaveReader()
    private val amplitudes = mutableListOf<Int>()

    private val borderRect = RectF()
    private val paint = Paint()
    private val barRect = RectF()

    private var sizeScale = 0f
    private var playingProgress = 0f

    private var scalingAnimator: ValueAnimator? = null
    private var playingAnimator: ValueAnimator? = null

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.WaveVisualizer, 0, 0).apply {
            try {
                borderColor = getColor(R.styleable.WaveVisualizer_borderColor, DEFAULT_BORDER_COLOR)
                bgColor = getColor(R.styleable.WaveVisualizer_bgColor, DEFAULT_BG_COLOR)
                delimiterColor = getColor(R.styleable.WaveVisualizer_delimiterColor, DEFAULT_DELIMITER_COLOR)
                barsColor = getColor(R.styleable.WaveVisualizer_barsColor, DEFAULT_BARS_COLOR)
                barsPlayedColor = getColor(R.styleable.WaveVisualizer_barsPlayedColor, DEFAULT_BARS_PLAYED_COLOR)
                barWidth = getDimension(R.styleable.WaveVisualizer_barWidth, 16f)
                barsOffset = getDimension(R.styleable.WaveVisualizer_barsOffset, 8f)
                animationDuration = getInteger(R.styleable.WaveVisualizer_animationDuration, DEFAULT_ANIMATION_DURATION)
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
            color = barsPlayedColor
        }

        var x = barsOffset / 2f
        var y: Float
        var h: Float
        val maxAmp = amplitudes.max()?.toFloat() ?: 255f
        var frac: Float

        amplitudes.forEach{
            if(x > borderRect.width() * playingProgress)
                paint.color = barsColor

            frac = it / maxAmp * sizeScale
            y = borderRect.height() / 2 - frac * borderRect.height()
            h = borderRect.height() / 2 + frac * borderRect.height()

            barRect.set(x, y, x + barWidth, h)
            canvas.drawRect(barRect, paint)

            x += barWidth + barsOffset
        }
    }

    fun animatePlayingProgress() {
        if(!waveReader.isHeaderRead || !waveReader.isHeaderValid() || !waveReader.isPCMFormat())
            throw IllegalStateException("Wave file is not read or header/format is not valid")

        playingAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = waveReader.duration
            interpolator = LinearInterpolator()
            addUpdateListener {
                playingProgress = it.animatedFraction
                invalidate()
            }

            start()
        }
    }

    fun loadWaveFromFile(file: File): Boolean {
        waveReader.readWaveHeaderFromFile(file)
        return handleWave()
    }

    fun loadWaveFromFile(filename: String): Boolean {
        waveReader.readWaveHeaderFromFile(filename)
        return handleWave()
    }

    fun loadWaveFromAssets(filename: String): Boolean {
        waveReader.readWaveHeaderFromAssets(context, filename)
        return handleWave()
    }

    private fun handleWave(): Boolean {
        if(!waveReader.isHeaderRead || !waveReader.isHeaderValid() || !waveReader.isPCMFormat())
            return false

        amplitudes.clear()
        sizeScale = 0f
        playingProgress = 0f

        /* Hardcoded for 2 channels */
        val barsNumber = (borderRect.width() / (barWidth + barsOffset)).toInt()
        val samplesPerBar = waveReader.waveSamples / barsNumber
        var readSamples = (waveReader.bytesRead - WaveReader.HEADER_SIZE) / ( waveReader.bitsPerSample / 8 )

        while(readSamples < waveReader.waveSamples){
            val toRead = min(samplesPerBar, waveReader.waveSamples - readSamples)
            val average = waveReader.readAverage(toRead)[0]
            amplitudes.add(abs(average.toInt()))

            readSamples = (waveReader.bytesRead - WaveReader.HEADER_SIZE) / ( waveReader.bitsPerSample / 8 )
        }

        scalingAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = animationDuration.toLong()
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                sizeScale = it.animatedFraction
                invalidate()
            }

            addListener(onEnd = { invalidate() })
            start()
        }

        return true
    }

    override fun onDetachedFromWindow() {
        scalingAnimator?.cancel()
        scalingAnimator = null

        playingAnimator?.cancel()
        playingAnimator = null

        super.onDetachedFromWindow()
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

    var barsPlayedColor: Int = 0
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

    var animationDuration = DEFAULT_ANIMATION_DURATION
    set(value) {
        if(value > 0)
            field = value

        invalidate()
    }

    companion object {
        const val DEFAULT_BORDER_COLOR = Color.TRANSPARENT
        const val DEFAULT_DELIMITER_COLOR = Color.DKGRAY
        const val DEFAULT_BARS_COLOR = Color.GRAY
        const val DEFAULT_BARS_PLAYED_COLOR = Color.RED
        val DEFAULT_BG_COLOR = Color.parseColor("#77ed4c")

        const val DEFAULT_ANIMATION_DURATION = 200
    }
}