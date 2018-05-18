package com.bzzzchat.videorecorder.view

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.ScaleAnimation

import com.bzzzchat.videorecorder.R

interface RecordButtonListener {
    fun onTakeImage()
    fun onStartRecord()
    fun onStopRecord()
}

enum class RecordButtonState {
    DEFAULT, RECORDING
}
/**
 * A subclass of [android.view.View] class for creating a custom circular progressBar
 *
 * Created by Pedram on 2015-01-06.
 */
class CustomRecordButton(context: Context, attrs: AttributeSet) : View(context, attrs) {
    /**
     * ProgressBar's line thickness
     */
    private var strokeWidth = 4f
    private var progress = 0f
    private var min = 0
    private var max = 100
    /**
     * Start the progress at 12 o'clock
     */
    private val startAngle = -90
    private var color = Color.DKGRAY
    private var fillColor = Color.WHITE
    private lateinit var rectF: RectF
    private lateinit var fillRect: RectF
    private lateinit var backgroundPaint: Paint
    private lateinit var foregroundPaint: Paint
    private lateinit var fillPaint: Paint

    private var listener: RecordButtonListener? = null
    private var state = RecordButtonState.DEFAULT
    private var progressAnimator: ValueAnimator? = null

    init {
        init(context, attrs)
        setOnClickListener { listener?.onTakeImage() }
        setOnLongClickListener {
            updateRecordState()
            return@setOnLongClickListener true
        }
    }

    fun getStrokeWidth(): Float {
        return strokeWidth
    }

    fun setStrokeWidth(strokeWidth: Float) {
        this.strokeWidth = strokeWidth
        backgroundPaint.strokeWidth = strokeWidth
        foregroundPaint.strokeWidth = strokeWidth
        invalidate()
        requestLayout()//Because it should recalculate its bounds
    }

    fun getProgress(): Float {
        return progress
    }

    fun setProgress(progress: Float) {
        this.progress = progress
        invalidate()
    }

    fun getMin(): Int {
        return min
    }

    fun setMin(min: Int) {
        this.min = min
        invalidate()
    }

    fun getMax(): Int {
        return max
    }

    fun setMax(max: Int) {
        this.max = max
        invalidate()
    }

    fun getColor(): Int {
        return color
    }

    fun setColor(color: Int) {
        this.color = color
        backgroundPaint.color = adjustAlpha(color, 0.3f)
        foregroundPaint.color = color
        invalidate()
        requestLayout()
    }

    private fun init(context: Context, attrs: AttributeSet) {
        rectF = RectF()
        fillRect = RectF()
        val typedArray = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.CustomRecordButton,
                0, 0)
        //Reading values from the XML layout
        try {
            strokeWidth = typedArray.getDimension(R.styleable.CustomRecordButton_progressBarThickness, strokeWidth)
            progress = typedArray.getFloat(R.styleable.CustomRecordButton_progress, progress)
            fillColor = typedArray.getInt(R.styleable.CustomRecordButton_fillColor, fillColor)
            color = typedArray.getInt(R.styleable.CustomRecordButton_progressbarColor, color)
            min = typedArray.getInt(R.styleable.CustomRecordButton_min, min)
            max = typedArray.getInt(R.styleable.CustomRecordButton_max, max)
        } finally {
            typedArray.recycle()
        }

        backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        backgroundPaint.color = adjustAlpha(color, 0.3f)
        backgroundPaint.style = Paint.Style.STROKE
        backgroundPaint.strokeWidth = strokeWidth

        foregroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        foregroundPaint.color = color
        foregroundPaint.style = Paint.Style.STROKE
        foregroundPaint.strokeWidth = strokeWidth
        foregroundPaint.strokeCap = Paint.Cap.ROUND

        fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        fillPaint.color = fillColor
        fillPaint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawOval(fillRect, fillPaint)
        canvas.drawOval(rectF, backgroundPaint)
        val angle = 360 * progress / max
        canvas.drawArc(rectF, startAngle.toFloat(), angle, false, foregroundPaint)

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val height = View.getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        val width = View.getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val min = Math.min(width, height)
        setMeasuredDimension(min, min)
        fillRect.set(0 + strokeWidth * 2, 0 + strokeWidth * 2, min - strokeWidth * 2, min - strokeWidth * 2)
        rectF.set(0 + strokeWidth / 2, 0 + strokeWidth / 2, min - strokeWidth / 2, min - strokeWidth / 2)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            if (state == RecordButtonState.RECORDING) {
                resetRecordState()
            }
        }
        return super.onTouchEvent(event)
    }

    private fun updateRecordState() {
        state = RecordButtonState.RECORDING
        val animation = ScaleAnimation(1f, 1.3f, 1f, 1.3f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f)
        animation.duration = 300
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) { }

            override fun onAnimationStart(animation: Animation?) { }

            override fun onAnimationEnd(animation: Animation?) {
                listener?.onStartRecord()
            }
        })
        startAnimation(animation)
        animation.fillAfter = true
    }

    fun resetRecordState() {
        this.progressAnimator?.cancel()
        this.progress = 0.0f
        invalidate()
        state = RecordButtonState.DEFAULT
        val animation = ScaleAnimation(1.3f, 1f, 1.3f, 1f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f)
        animation.duration = 300
        startAnimation(animation)
        animation.fillAfter = true
        this.listener?.onStopRecord()
    }

    /**
     * Lighten the given color by the factor
     *
     * @param color  The color to lighten
     * @param factor 0 to 4
     * @return A brighter color
     */
    fun lightenColor(color: Int, factor: Float): Int {
        val r = Color.red(color) * factor
        val g = Color.green(color) * factor
        val b = Color.blue(color) * factor
        val ir = Math.min(255, r.toInt())
        val ig = Math.min(255, g.toInt())
        val ib = Math.min(255, b.toInt())
        val ia = Color.alpha(color)
        return Color.argb(ia, ir, ig, ib)
    }

    /**
     * Transparent the given color by the factor
     * The more the factor closer to zero the more the color gets transparent
     *
     * @param color  The color to transparent
     * @param factor 1.0f to 0.0f
     * @return int - A transplanted color
     */
    fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    /**
     * Set the progress with an animation.
     * Note that the [android.animation.ObjectAnimator] Class automatically set the progress
     * so don't call the [CustomRecordButton.setProgress] directly within this method.
     *
     * @param progress The progress it should animate to it.
     */
    fun setProgressWithAnimation(progress: Float, duration: Long = 1000) {
        progressAnimator?.cancel()
        val start = this.progress
        Log.e("HEHEHE", "from: $start, to: $progress")
        progressAnimator = ValueAnimator.ofFloat(start, progress)
        progressAnimator!!.duration = duration
        progressAnimator!!.interpolator = LinearInterpolator()
        progressAnimator!!.addUpdateListener {
            this.progress = it.animatedValue as Float
            invalidate()
        }
        progressAnimator!!.start()
    }

    fun setListener(listener: RecordButtonListener) {
        this.listener = listener
    }
}
