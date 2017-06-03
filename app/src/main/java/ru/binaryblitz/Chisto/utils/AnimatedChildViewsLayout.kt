package ru.binaryblitz.Chisto.utils

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.widget.RelativeLayout

/**
 *  Custom RelativeLayout that animates its child views
 */

class AnimatedChildViewsLayout : RelativeLayout {
    val defaultTransitionSpeed = PointF(2.0f, 0.0f)
    var isEnableAlphaAnimation = false
    lateinit var animationType: AnimationType
    lateinit var childTransitions: Array<PointF?>

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    /**
     * Initializes the speed of child views transitions
     */
    fun setup() {
        val count = childCount
        childTransitions = arrayOfNulls<PointF>(count)
        for (i in 0..count - 1) {
            childTransitions[i] = defaultTransitionSpeed
        }
    }

    fun selectAnimationType(offset: Float, direction: Direction) {
        for (i in childTransitions.indices) {
            when (animationType) {
                AnimationType.Linear -> setLinearAnimation(i, offset, direction)
                AnimationType.InOut -> setInOutAnimation(i, offset, direction)
            }
            if (isEnableAlphaAnimation) {
                startAlphaAnimation(i, offset)
            }
        }
    }

    /**
     * Counts horizontal and vertical locations of the child view for linear transition
     */
    private fun setLinearAnimation(index: Int, offset: Float, direction: Direction) {
        if (childTransitions.isEmpty()) {
            return
        }
        var dx = 0.0f
        val dy = (1.0f - offset) * 100
        when (direction) {
            Direction.Left -> dx = (1.0f - offset) * 100
            Direction.Right -> dx = (offset - 1.0f) * 100
        }
        setLocationXY(index, dx * childTransitions[index]!!.x, dy * childTransitions[index]!!.y)
    }

    /**
     * Counts horizontal and vertical locations of the child view for InOut transition effect
     */
    private fun setInOutAnimation(index: Int, offset: Float, direction: Direction) {
        if (childTransitions.isEmpty()) {
            return
        }
        var dx = 0.0f
        val dy = 1.0f - offset
        when (direction) {
            Direction.Left -> dx = 1.0f - offset
            Direction.Right -> dx = offset - 1.0f
        }
        setLocationXY(index, dx * childTransitions[index]!!.x * 100f, dy * childTransitions[index]!!.y * 100f)
    }

    /**
     * Sets the horizontal and vertical locations of the child view.
     */
    fun setLocationXY(index: Int, translationX: Float, translationY: Float) {
        val view = getChildAt(index)
        view.translationX = translationX
        view.translationY = translationY
    }

    /**
     * Changes the opacity of the child view.
     */
    private fun startAlphaAnimation(index: Int, offset: Float) {
        val view = getChildAt(index)
        view.alpha = offset
    }

    enum class Direction {
        Right,
        Left,
        None
    }

    enum class AnimationType {
        Linear,
        InOut
    }
}
