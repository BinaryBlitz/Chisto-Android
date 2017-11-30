package ru.binaryblitz.Chisto.ui.start.onboarding.base

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.utils.AnimatedChildViewsLayout


abstract class OnBoardingFragment : Fragment(), ViewPager.OnPageChangeListener {
    open var pagePosition: Int = 0

    var pageContainer: AnimatedChildViewsLayout? = null
    var pageImageView: ImageView? = null
    var pageTextView: TextView? = null
    var pageText: String? = null
    var pageImage: Int? = null
    var animType: AnimatedChildViewsLayout.AnimationType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.base_onboarding_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pageContainer = view.findViewById(R.id.onBoardingView) as AnimatedChildViewsLayout
        pageImageView = view.findViewById(R.id.img) as ImageView
        pageTextView = view.findViewById(R.id.title) as TextView
        pageTextView!!.text = pageText
        pageImageView!!.setImageResource(pageImage!!)
        pageContainer!!.animationType = animType!!
        pageContainer!!.setup()

    }

    override fun onPageSelected(position: Int) {}

    override fun onPageScrollStateChanged(state: Int) {}


    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        val layout = pageContainer ?: return
        val pagePosition = pagePosition
        if (position >= pagePosition) {
            layout.selectAnimationType(1.0f - positionOffset, AnimatedChildViewsLayout.Direction.Right)
        } else if (position < pagePosition) {
            layout.selectAnimationType(positionOffset, AnimatedChildViewsLayout.Direction.Left)
        }
    }
}
