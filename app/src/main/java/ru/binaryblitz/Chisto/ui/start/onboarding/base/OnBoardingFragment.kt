package ru.binaryblitz.Chisto.ui.start.onboarding.base

import android.graphics.PointF
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import ru.binaryblitz.Chisto.R


abstract class OnBoardingFragment : Fragment(), ViewPager.OnPageChangeListener {
    open var pagePosition: Int = 0

    var onBoarding: OnBoardingLayout? = null
    var pageImageView: ImageView? = null
    var pageTextView: TextView? = null
    var pageText: String? = null
    var pageImage: Int? = null
    var animType: OnBoardingLayout.AnimationType? = null
    var animSpeed: PointF? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.base_onboarding_screen, container, false)

    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onBoarding = view!!.findViewById(R.id.onBoardingView) as OnBoardingLayout
        pageImageView = view.findViewById(R.id.img) as ImageView
        pageTextView = view.findViewById(R.id.title) as TextView
        pageTextView!!.text = pageText
        pageImageView!!.setImageResource(pageImage!!)
        onBoarding!!.animationType = animType!!
        onBoarding!!.speedVariance = animSpeed!!
        onBoarding!!.setup()

    }

    override fun onPageSelected(position: Int) {}

    override fun onPageScrollStateChanged(state: Int) {}


    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        val layout = onBoarding
        if (layout != null) {
            val pagePosition = pagePosition
            if (position >= pagePosition) {
                layout.selectAnim(1.0f - positionOffset, OnBoardingLayout.Direction.Right)
            } else if (position < pagePosition) {
                layout.selectAnim(positionOffset, OnBoardingLayout.Direction.Left)
            }
        }
    }

}