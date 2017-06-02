package ru.binaryblitz.Chisto.utils

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.widget.RelativeLayout

class OnBoardingLayout : RelativeLayout {
    var speed = PointF(0.0f, 0.0f)
    var speedVariance = PointF(0.0f, 0.0f)
    var isEnableAlphaAnimation = false
    var animationType = AnimationType.Linear
    lateinit var childSpeeds: Array<PointF?>


    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    fun setup() {
        val count = childCount
        childSpeeds = arrayOfNulls<PointF>(count)
        for (i in 0..count - 1) {
            speed.x += speedVariance.x
            speed.y += speedVariance.y
            childSpeeds[i] = speed
        }
    }

    fun selectAnim(offset: Float, direction: Direction) {
        for (i in childSpeeds!!.indices) {
            when (animationType) {
                AnimationType.Linear -> animationLinear(i, offset, direction)

                AnimationType.Zoom -> animationZoom(i, offset, direction)

                AnimationType.Curve -> animationCurve(i, offset, direction)

                AnimationType.InOut -> animationInOut(i, offset, direction)
            }

            if (isEnableAlphaAnimation) {
                animationAlpha(i, offset, direction)
            }
        }
    }

    private fun animationAlpha(index: Int, offset: Float, direction: Direction) {
        val view = getChildAt(index)
        val tag = view.tag.toString()
        view.alpha = offset
    }

    private fun animationCurve(index: Int, offset: Float, direction: Direction) {
        if (childSpeeds!!.size <= 0) {
            return
        }
        var dx = 0.0f
        var dy = (1.0f - offset) * 10
        when (direction) {
            Direction.Left -> {
                dx = (1.0f - offset) * 10
                dy = (1.0f - offset) * 10
            }
            Direction.Right -> {
                dx = (offset - 1.0f) * 10
                dy = (offset - 1.0f) * 10
            }
        }

        translation(index, (Math.pow(dx.toDouble(), 3.0).toFloat() - dx * 25) * childSpeeds!![index]!!.x, (Math.pow(dy.toDouble(), 3.0).toFloat() - dy * 20) * childSpeeds!![index]!!.y)
    }

    private fun animationZoom(index: Int, offset: Float, direction: Direction) {
        val scale = 1.0f - offset
        scale(index, 1.0f - scale, 1.0f - scale)
    }

    private fun animationLinear(index: Int, offset: Float, direction: Direction) {
        if (childSpeeds!!.size <= 0) {
            return
        }
        var dx = 0.0f
        val dy = (1.0f - offset) * 100
        when (direction) {
            Direction.Left -> dx = (1.0f - offset) * 100
            Direction.Right -> dx = (offset - 1.0f) * 100
        }
        translation(index, dx * childSpeeds!![index]!!.x, dy * childSpeeds!![index]!!.y)
    }

    private fun animationInOut(index: Int, offset: Float, direction: Direction) {
        if (childSpeeds!!.size <= 0) {
            return
        }
        var dx = 0.0f
        val dy = 1.0f - offset
        when (direction) {
            Direction.Left -> dx = 1.0f - offset
            Direction.Right -> dx = offset - 1.0f
        }
        translation(index, dx * childSpeeds!![index]!!.x * 100f, dy * childSpeeds!![index]!!.y * 100f)
    }

    fun scale(index: Int, scaleX: Float, scaleY: Float) {
        val view = getChildAt(index)
        view.scaleX = scaleX
        view.scaleY = scaleY
    }

    fun translation(index: Int, translationX: Float, translationY: Float) {
        val view = getChildAt(index)
        view.translationX = translationX
        view.translationY = translationY
    }

    enum class Direction {
        Right,
        Left,
        None
    }

    enum class AnimationType {
        Linear,
        Curve,
        Zoom,
        InOut,
        Custom
    }

    interface CustomAnimationListener {
        fun animate(index: Int, offset: Float, direction: Direction)
    }

    companion object {
        var DEFAULT_SPEED = PointF(0.0f, 2.0f)
    }
}
