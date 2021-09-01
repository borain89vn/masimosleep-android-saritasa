package com.mymasimo.masimosleep.ui

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.mymasimo.masimosleep.R
import com.mymasimo.masimosleep.util.pxFromDp

private const val BAR_Y = 18f

private const val BAR_SPACING = 0f

private const val BAR_MARGIN_TOP = 5f

class ScoreProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private val textPaint: TextPaint

    private val firstBarPaint: Paint

    private val secondBarPaint: Paint

    private val thirdBarPaint: Paint

    private val dashPaint: Paint

    private var secondBarWidthByPixel  = (resources.getInteger(R.integer.red_upper) + (69.0/150.0)*40)

    private val firstBarWidthPercent: Double = resources.getInteger(R.integer.red_upper) / 100.toDouble()

    private val secondBarWidth: Double = (secondBarWidthByPixel)/ 100.toDouble()

    private var score = 0f

    private var screenWidth = width

    private var dashGap: Int
    private var dashLength: Int
    private var dashThickness: Int


    @DrawableRes
    private var notchIcon: Int


    init {
        val ta = context.theme.obtainStyledAttributes(attrs, R.styleable.ScoreProgressBar, 0, 0)

        try {
            val barHeight = ta.getInteger(R.styleable.ScoreProgressBar_scoreBarHeight, 0)

            val firstBarColor = ta.getResourceId(R.styleable.ScoreProgressBar_firstBarColor, 0)

            val secondBarColor = ta.getResourceId(R.styleable.ScoreProgressBar_secondBarColor, 0)

            val thirdBarColor = ta.getResourceId(R.styleable.ScoreProgressBar_thirdBarColor, 0)

            val constantsColor = ta.getResourceId(R.styleable.ScoreProgressBar_constantsColor, 0)

            val dashColor = ta.getResourceId(R.styleable.ScoreProgressBar_dashColor, 0)

            score = ta.getFloat(R.styleable.ScoreProgressBar_score, 0.0f)

            notchIcon = ta.getResourceId(R.styleable.ScoreProgressBar_notchIcon, 0)

            dashGap = ta.getResourceId(R.styleable.ScoreProgressBar_dashGap, 5)
            dashLength = ta.getResourceId(R.styleable.ScoreProgressBar_dashLength, 8)
            dashThickness = ta.getResourceId(R.styleable.ScoreProgressBar_dashThickness, 4)

            firstBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = barHeight.toFloat() * context.resources.displayMetrics.density
                color = ContextCompat.getColor(context, firstBarColor)
            }

            secondBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = barHeight.toFloat() * context.resources.displayMetrics.density
                color = ContextCompat.getColor(context, secondBarColor)
            }

            thirdBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = barHeight.toFloat() * context.resources.displayMetrics.density
                color = ContextCompat.getColor(context, thirdBarColor)
            }

            textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                textSize = 14 * context.resources.displayMetrics.density
                color = ContextCompat.getColor(context, constantsColor)
            }

            dashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = ContextCompat.getColor(context, dashColor)
                style = Paint.Style.STROKE
                strokeWidth = dashThickness.toFloat()
                pathEffect = DashPathEffect( floatArrayOf(dashLength.toFloat(),dashGap.toFloat()), 0f)
            }

        } finally {
            ta.recycle()
        }
    }

    fun setFirstBarColor(@ColorRes color: Int) {
        firstBarPaint.color = ContextCompat.getColor(context, color)
        invalidate()
    }

    fun setSecondBarColor(@ColorRes color: Int) {
        secondBarPaint.color = ContextCompat.getColor(context, color)
        invalidate()
    }

    fun setThirdBarColor(@ColorRes color: Int) {
        thirdBarPaint.color = ContextCompat.getColor(context, color)
        invalidate()
    }

    fun setNotchIcon(@DrawableRes drawableRes: Int) {
        notchIcon = drawableRes
        invalidate()
    }

    fun setScore(position: Float) {
        score = position
        invalidate()
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        canvas?.apply {
            //draw first bar
            drawText(canvas, resources.getInteger(R.integer.red_lower).toString(), 0f)
            drawBar(canvas, 0f, screenWidth * firstBarWidthPercent.toFloat(), firstBarPaint)
            drawText(canvas, resources.getInteger(R.integer.red_upper).toString(), screenWidth * firstBarWidthPercent.toFloat())

            //draw second bar
            drawBar(canvas, screenWidth * firstBarWidthPercent.toFloat() + BAR_SPACING.toDp(), screenWidth * secondBarWidth.toFloat(), secondBarPaint)
            drawText(canvas, resources.getInteger(R.integer.yellow_upper).toString(), screenWidth * secondBarWidth.toFloat())

            //draw third bar
            drawBar(canvas, screenWidth * secondBarWidth.toFloat() + BAR_SPACING.toDp(), screenWidth.toFloat(), thirdBarPaint)
            drawText(canvas, resources.getInteger(R.integer.green_upper).toString(), screenWidth.toFloat())

            //draw notch
            drawImage(canvas)
            drawDashLine(canvas)

        }
    }

    private fun drawImage(canvas: Canvas) {
        val bm = BitmapFactory.decodeResource(context.resources, notchIcon)

        val newSize = BAR_Y.toDp()

        val scaled = Bitmap.createScaledBitmap(bm, newSize.toInt(), newSize.toInt(), true)
        val imageWidth = if (score == 0.0f) 0 else scaled.width
        val left = (screenWidth * score) - imageWidth/2 - (BAR_SPACING.toDp() * 3)
        canvas.drawBitmap(scaled, left, 0f, null)
    }

    private fun drawDashLine(canvas: Canvas) {
        if (score == 0f) return
        val startX = (screenWidth * score)  - (BAR_SPACING.toDp() * 3)
        canvas.drawLine(startX, BAR_Y.toDp() - (BAR_MARGIN_TOP/2).toDp(), startX, (BAR_Y * 2).toDp() - BAR_MARGIN_TOP.toDp(), dashPaint)

    }

    private fun drawText(canvas: Canvas, text: String, x: Float) {
        val bounds = Rect()

        textPaint.getTextBounds(text, 0, text.length, bounds)

        val updatedX = (x - (bounds.width() + 2f.toDp())).coerceAtLeast(0f)

        canvas.drawText(text, updatedX, (BAR_Y * 2).toDp() + 2*BAR_MARGIN_TOP.toDp(), textPaint)
    }

    private fun drawBar(canvas: Canvas, startX: Float, barLength: Float, paint: Paint) {
        canvas.drawLine(startX, BAR_Y.toDp() + BAR_MARGIN_TOP.toDp(), barLength, BAR_Y.toDp() + BAR_MARGIN_TOP.toDp(), paint)
    }

    private fun Float.toDp() = context.pxFromDp(this)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = 120 + (2*BAR_MARGIN_TOP).toDp().toInt()

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width: Int = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> widthSize.coerceAtMost(widthSize)
            else -> widthSize
        }

        val height: Int = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> desiredHeight.coerceAtMost(heightSize)
            else -> desiredHeight
        }

        screenWidth = width

        setMeasuredDimension(width, height)
    }
}