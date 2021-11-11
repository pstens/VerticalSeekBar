@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.lukelorusso.verticalseekbar

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import kotlinx.android.synthetic.main.layout_verticalseekbar.view.*
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * A nicer, redesigned and vertical SeekBar
 */
open class HorizontalSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_MAX_VALUE = 100
        private const val DEFAULT_PROGRESS = 50
        private const val DEFAULT_DRAWABLE_BACKGROUND: String = "#f6f6f6"
        private const val DEFAULT_DRAWABLE_PROGRESS_START: String = "#4D88E1"
        private const val DEFAULT_DRAWABLE_PROGRESS_END: String = "#7BA1DB"
    }

    enum class Placeholder {
        OUTSIDE,
        INSIDE,
        MIDDLE
    }

    private var onProgressChangeListener: ((Int) -> Unit)? = null
    private var onPressListener: ((Int) -> Unit)? = null
    private var onReleaseListener: ((Int) -> Unit)? = null

    var clickToSetProgress = true
        set(value) {
            field = value
            applyAttributes()
        }
    var barCornerRadius: Int = 0
        set(value) {
            field = value
            applyAttributes()
        }
    var barBackgroundDrawable: Drawable? = null
        set(value) {
            field = value
            applyAttributes()
        }
    var barBackgroundStartColor: Int = Color.parseColor(DEFAULT_DRAWABLE_BACKGROUND)
        set(value) {
            field = value
            barBackgroundDrawable = null
            applyAttributes()
        }
    var barBackgroundEndColor: Int = Color.parseColor(DEFAULT_DRAWABLE_BACKGROUND)
        set(value) {
            field = value
            barBackgroundDrawable = null
            applyAttributes()
        }
    var barProgressDrawable: Drawable? = null
        set(value) {
            field = value
            applyAttributes()
        }
    var barProgressStartColor: Int = Color.parseColor(DEFAULT_DRAWABLE_PROGRESS_START)
        set(value) {
            field = value
            barProgressDrawable = null
            applyAttributes()
        }
    var barProgressEndColor: Int = Color.parseColor(DEFAULT_DRAWABLE_PROGRESS_END)
        set(value) {
            field = value
            barProgressDrawable = null
            applyAttributes()
        }
    var barHeight: Int? = null
        set(value) {
            field = value
            applyAttributes()
        }
    var minLayoutWidth: Int = 0
        set(value) {
            field = value
            applyAttributes()
        }
    var minLayoutHeight: Int = 0
        set(value) {
            field = value
            applyAttributes()
        }
    var maxPlaceholderDrawable: Drawable? = null
        set(value) {
            field = value
            applyAttributes()
        }
    var maxPlaceholderPosition = Placeholder.MIDDLE
        set(value) {
            field = value
            applyAttributes()
        }
    var minPlaceholderDrawable: Drawable? = null
        set(value) {
            field = value
            applyAttributes()
        }
    var minPlaceholderPosition = Placeholder.MIDDLE
        set(value) {
            field = value
            applyAttributes()
        }
    var showThumb = true
        set(value) {
            field = value
            applyAttributes()
        }
    var thumbContainerColor: Int = Color.WHITE
        set(value) {
            field = value
            applyAttributes()
        }
    var thumbContainerCornerRadius: Int = 0
        set(value) {
            field = value
            applyAttributes()
        }
    var thumbPlaceholderDrawable: Drawable? = null
        set(value) {
            field = value
            applyAttributes()
        }
    var useThumbToSetProgress = true
        set(value) {
            field = value
            applyAttributes()
        }
    var maxValue = DEFAULT_MAX_VALUE
        set(value) {
            val newValue = when {
                value < 1 -> 1
                else -> value
            }
            if (progress > newValue) progress = newValue
            field = newValue
            updateViews()
        }
    var progress: Int = DEFAULT_PROGRESS
        set(value) {
            val newValue = when {
                value < 0 -> 0
                value > maxValue -> maxValue
                else -> value
            }
            if (field != newValue) {
                onProgressChangeListener?.invoke(newValue)
            }
            field = newValue
            updateViews()
        }
    private var xDelta: Int = 0
    private var initEnded =
        false // if true allows the view to be updated after setting an attribute programmatically

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        inflate(context, R.layout.layout_horizontalseekbar, this)

        if (attrs != null) {
            val attributes =
                context.obtainStyledAttributes(attrs, R.styleable.VerticalSeekBar, 0, 0)
            try {
                clickToSetProgress =
                    attributes.getBoolean(
                        R.styleable.VerticalSeekBar_vsb_click_to_set_progress,
                        clickToSetProgress
                    )
                barCornerRadius = attributes.getLayoutDimension(
                    R.styleable.VerticalSeekBar_vsb_bar_corner_radius,
                    barCornerRadius
                )
                barBackgroundStartColor =
                    attributes.getColor(
                        R.styleable.VerticalSeekBar_vsb_bar_background_gradient_start,
                        barBackgroundStartColor
                    )
                barBackgroundEndColor =
                    attributes.getColor(
                        R.styleable.VerticalSeekBar_vsb_bar_background_gradient_end,
                        barBackgroundEndColor
                    )
                attributes.getDrawable(R.styleable.VerticalSeekBar_vsb_bar_background)?.also {
                    barBackgroundDrawable = it
                }
                barProgressStartColor =
                    attributes.getColor(
                        R.styleable.VerticalSeekBar_vsb_bar_progress_gradient_start,
                        barProgressStartColor
                    )
                barProgressEndColor =
                    attributes.getColor(
                        R.styleable.VerticalSeekBar_vsb_bar_progress_gradient_end,
                        barProgressEndColor
                    )
                attributes.getDrawable(R.styleable.VerticalSeekBar_vsb_bar_progress).also {
                    barProgressDrawable = it
                }
                barHeight = attributes.getDimensionPixelSize(
                    R.styleable.VerticalSeekBar_vsb_bar_width,
                    barHeight ?: container.layoutParams.height
                )
                attributes.getLayoutDimension(
                    R.styleable.VerticalSeekBar_android_layout_width,
                    minLayoutWidth
                ).also {
                    container.layoutParams.width =
                        if (it != -1 && it < minLayoutWidth) minLayoutWidth // wrap_content
                        else it
                }
                attributes.getLayoutDimension(
                    R.styleable.VerticalSeekBar_android_layout_height,
                    minLayoutHeight
                ).also {
                    container.layoutParams.height =
                        if (it != -1 && it < minLayoutHeight) minLayoutHeight // wrap_content
                        else it
                }
                attributes.getDrawable(R.styleable.VerticalSeekBar_vsb_max_placeholder_src).also {
                    maxPlaceholderDrawable = it
                }
                maxPlaceholderPosition = Placeholder.values()[attributes.getInt(
                    R.styleable.VerticalSeekBar_vsb_max_placeholder_position,
                    maxPlaceholderPosition.ordinal
                )]
                attributes.getDrawable(R.styleable.VerticalSeekBar_vsb_min_placeholder_src).also {
                    minPlaceholderDrawable = it
                }
                minPlaceholderPosition = Placeholder.values()[attributes.getInt(
                    R.styleable.VerticalSeekBar_vsb_min_placeholder_position,
                    minPlaceholderPosition.ordinal
                )]
                showThumb =
                    attributes.getBoolean(R.styleable.VerticalSeekBar_vsb_show_thumb, showThumb)
                thumbContainerColor =
                    attributes.getColor(
                        R.styleable.VerticalSeekBar_vsb_thumb_container_tint,
                        thumbContainerColor
                    )
                thumbContainerCornerRadius = attributes.getLayoutDimension(
                    R.styleable.VerticalSeekBar_vsb_thumb_container_corner_radius,
                    thumbContainerCornerRadius
                )
                attributes.getDrawable(R.styleable.VerticalSeekBar_vsb_thumb_placeholder_src).also {
                    thumbPlaceholderDrawable = it
                }
                attributes.getInt(R.styleable.VerticalSeekBar_vsb_max_value, maxValue).also {
                    maxValue = it
                }
                attributes.getInt(R.styleable.VerticalSeekBar_vsb_progress, progress).also {
                    progress = it
                }
                useThumbToSetProgress =
                    attributes.getBoolean(
                        R.styleable.VerticalSeekBar_vsb_use_thumb_to_set_progress,
                        useThumbToSetProgress
                    )

            } finally {
                attributes.recycle()
            }
        }

        initEnded = true
        applyAttributes()
    }

    fun setOnProgressChangeListener(listener: ((Int) -> Unit)?) {
        this.onProgressChangeListener = listener
    }

    fun setOnPressListener(listener: ((Int) -> Unit)?) {
        this.onPressListener = listener
    }

    fun setOnReleaseListener(listener: ((Int) -> Unit)?) {
        this.onReleaseListener = listener
    }

    //region PROTECTED METHODS
    protected fun Context.dpToPixel(dp: Float): Float =
        dp * (resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)

    protected fun Context.pixelToDp(px: Float): Float =
        px / (resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    //endregion

    @SuppressLint("ClickableViewAccessibility")
    private fun applyAttributes() {
        if (initEnded) {
            initEnded = false // will be released at the end

            var thumbCardView: CardView? = null // nullable for customization
            try {
                thumbCardView = thumb.findViewById(R.id.thumbCardView)
            } catch (ignored: NoSuchFieldError) {
            }


            var thumbPlaceholder: ImageView? = null // nullable for customization
            try {
                thumbPlaceholder = thumb.findViewById(R.id.thumbPlaceholder)
            } catch (ignored: NoSuchFieldError) {
            }


            // Customizing drawableCardView
            barCardView.layoutParams.height = barHeight ?: 0

            // Customizing drawableBackground
            if (barBackgroundDrawable == null) barBackgroundDrawable = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(barBackgroundStartColor, barBackgroundEndColor)
            ).apply { cornerRadius = 0f }
            barBackground.background = barBackgroundDrawable

            // Customizing drawableProgress
            if (barProgressDrawable == null) barProgressDrawable = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(barProgressStartColor, barProgressEndColor)
            ).apply { cornerRadius = 0f }
            barProgress.background = barProgressDrawable

            // Applying card corner radius
            barCardView.radius = barCornerRadius.toFloat()
            thumbCardView?.radius = thumbContainerCornerRadius.toFloat()

            // Applying custom placeholders
            maxPlaceholder.setImageDrawable(maxPlaceholderDrawable) // can also be null
            minPlaceholder.setImageDrawable(minPlaceholderDrawable) // can also be null

            // Let's shape the thumb
            val thumbMeasureIncrease =
                if (thumbCardView != null) (ViewCompat.getElevation(thumbCardView)
                        + context.dpToPixel(1F)).roundToInt()
                else 0
            if (showThumb) {
                thumbPlaceholderDrawable?.also { thumbPlaceholder?.setImageDrawable(it) } // CANNOT be null
                thumb.visibility = View.VISIBLE
                val states = arrayOf(
                    intArrayOf(android.R.attr.state_enabled),  // enabled
                    intArrayOf(-android.R.attr.state_enabled), // disabled
                    intArrayOf(-android.R.attr.state_checked), // unchecked
                    intArrayOf(android.R.attr.state_pressed)   // pressed
                )
                val colors = arrayOf(
                    thumbContainerColor,
                    thumbContainerColor,
                    thumbContainerColor,
                    thumbContainerColor
                ).toIntArray()

                if (thumbCardView != null)
                    ViewCompat.setBackgroundTintList(thumbCardView, ColorStateList(states, colors))
                thumb.measure(0, 0)
                thumb.layoutParams = (thumb.layoutParams as LayoutParams).apply {
                    width = thumb.measuredWidth + thumbMeasureIncrease
                    height = thumb.measuredHeight + thumbMeasureIncrease
                    thumbCardView?.layoutParams =
                        (thumbCardView?.layoutParams as LayoutParams).apply {
                            rightMargin = thumbMeasureIncrease / 2
                        }
                }
            } else thumb.visibility = View.GONE

            // Adding some margin to drawableCardView, maxPlaceholder and minPlaceholder
            val maxPlaceholderLayoutParams = (maxPlaceholder.layoutParams as LayoutParams)
            val minPlaceholderLayoutParams = (minPlaceholder.layoutParams as LayoutParams)
            barCardView.layoutParams = (barCardView.layoutParams as LayoutParams).apply {
                val thumbHalfWidth =
                    if (showThumb) thumb.measuredWidth / 2
                    else 0

                val maxPlaceholderHalfWidth = (maxPlaceholder.drawable?.intrinsicWidth ?: 0) / 2
                when (maxPlaceholderPosition) {
                    Placeholder.INSIDE -> {
                        rightMargin = thumbHalfWidth
                        maxPlaceholderLayoutParams.rightMargin = rightMargin
                    }
                    Placeholder.OUTSIDE -> {
                        rightMargin = maxPlaceholder.drawable.intrinsicWidth +
                                if (thumbHalfWidth > maxPlaceholder.drawable.intrinsicWidth)
                                    thumbHalfWidth - maxPlaceholder.drawable.intrinsicWidth
                                else 0
                        maxPlaceholderLayoutParams.rightMargin =
                            rightMargin - maxPlaceholder.drawable.intrinsicWidth
                    }
                    else -> {
                        rightMargin = max(thumbHalfWidth, maxPlaceholderHalfWidth)
                        maxPlaceholderLayoutParams.rightMargin =
                            rightMargin - maxPlaceholderHalfWidth
                    }
                }
                maxPlaceholderLayoutParams.leftMargin = maxPlaceholderLayoutParams.rightMargin
                maxPlaceholder.layoutParams = maxPlaceholderLayoutParams

                val minPlaceholderHalfWidth = (minPlaceholder.drawable?.intrinsicWidth ?: 0) / 2
                when (minPlaceholderPosition) {
                    Placeholder.INSIDE -> {
                        leftMargin = thumbHalfWidth
                        minPlaceholderLayoutParams.leftMargin = leftMargin
                    }
                    Placeholder.OUTSIDE -> {
                        leftMargin = minPlaceholder.drawable.intrinsicWidth +
                                if (thumbHalfWidth > minPlaceholder.drawable.intrinsicWidth)
                                    thumbHalfWidth - minPlaceholder.drawable.intrinsicWidth
                                else 0
                        minPlaceholderLayoutParams.leftMargin =
                            leftMargin - minPlaceholder.drawable.intrinsicWidth
                    }
                    else -> {
                        leftMargin = max(thumbHalfWidth, minPlaceholderHalfWidth)
                        minPlaceholderLayoutParams.leftMargin =
                            leftMargin - minPlaceholderHalfWidth
                    }
                }
                leftMargin += thumbMeasureIncrease
                minPlaceholderLayoutParams.leftMargin += thumbMeasureIncrease
                minPlaceholderLayoutParams.rightMargin = maxPlaceholderLayoutParams.leftMargin
                minPlaceholder.layoutParams = minPlaceholderLayoutParams
            }

            // FIXME: Figure out how to make this work with horizontal layout
            // here we intercept the click on the thumb
//            if (showThumb && useThumbToSetProgress) thumb.setOnTouchListener { thumb, event ->
//                val rawX = event.rawX.roundToInt()
//                when (event.action and MotionEvent.ACTION_MASK) {
//
//                    MotionEvent.ACTION_DOWN -> { // here we get the max top y coordinate (yDelta)
//                        xDelta = rawX +
//                                (barCardView.layoutParams as LayoutParams).rightMargin -
//                                (thumb.layoutParams as LayoutParams).rightMargin -
//                                thumb.measuredWidth / 2
//                        onPressListener?.invoke(progress)
//                    }
//
//                    MotionEvent.ACTION_MOVE -> {
//                        val fillWidth = barCardView.measuredWidth
//                        val positionX = (rawX + maxValue - xDelta)  // here we calculate the displacement
//                        when { // here we update progress
//                            positionX in 1 until fillWidth -> {
//                                val newValue =  (positionX.toFloat() * maxValue / fillWidth)
//                                progress = newValue.roundToInt()
//                            }
//                            positionX <= 0 -> progress = 0
//                            positionX >= fillWidth -> progress = maxValue
//                        }
//                    }
//
//                    MotionEvent.ACTION_UP -> onReleaseListener?.invoke(progress)
//
//                }
//                true
//            } else thumb.setOnTouchListener(null)

            // here we intercept the click on the bar
            if (clickToSetProgress || showThumb && useThumbToSetProgress) barCardView.setOnTouchListener { bar, event ->
                val positionX = event.x.roundToInt()
                val action = {
                    val fillWidth = bar.measuredWidth
                    when { // here we update progress
                        positionX in 1 until fillWidth -> {
                            val newValue = (positionX.toFloat() * maxValue / fillWidth)
                            progress = newValue.roundToInt()
                        }
                        positionX <= 0 -> progress = 0
                        positionX >= fillWidth -> progress = maxValue
                    }
                }
                when (event.action and MotionEvent.ACTION_MASK) {

                    MotionEvent.ACTION_DOWN -> {
                        action.invoke()
                        onPressListener?.invoke(progress)
                    }

                    MotionEvent.ACTION_MOVE -> if (useThumbToSetProgress) action.invoke()

                    MotionEvent.ACTION_UP -> onReleaseListener?.invoke(progress)

                }
                true
            } else barCardView.setOnTouchListener(null)

            initEnded = true

            updateViews()
        }
    }

    /**
     * Inside here the views are repositioned based on the new value
     */
    private fun updateViews() {
        if (initEnded) post {
            val barCardViewLayoutParams = barCardView.layoutParams as LayoutParams
            val fillWidth =
                width - barCardViewLayoutParams.rightMargin - barCardViewLayoutParams.leftMargin
            val marginByProgress = fillWidth - (progress * fillWidth / maxValue)
            thumb.layoutParams = (thumb.layoutParams as LayoutParams).apply {
                rightMargin = marginByProgress
                Log.d("aoe", "updateViews: $marginByProgress")
                val thumbHalfWidth = if (showThumb) thumb.measuredWidth / 2 else 0
                if (barCardViewLayoutParams.rightMargin > thumbHalfWidth) {
                    val displacement = barCardViewLayoutParams.rightMargin - thumbHalfWidth
                    rightMargin += displacement
                }
            }
            barProgress.translationX =
                -(barBackground.width * (maxValue - progress) / maxValue).toFloat()
            invalidate()
        }
    }

}
