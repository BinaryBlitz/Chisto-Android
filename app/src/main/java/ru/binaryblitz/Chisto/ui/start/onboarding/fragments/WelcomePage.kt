package ru.binaryblitz.Chisto.ui.start.onboarding.fragments

import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.ui.start.onboarding.base.OnBoardingFragment
import ru.binaryblitz.Chisto.ui.start.onboarding.base.OnBoardingLayout



class WelcomePage : OnBoardingFragment() {
    private var onBoardLayout: OnBoardingLayout? = null
        private set

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.onboarding_welcome, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        onBoardLayout = view!!.findViewById(R.id.onBoardingView) as OnBoardingLayout
        onBoardLayout!!.speed = PointF(1.0f, 0.0f)
        onBoardLayout!!.speedVariance = PointF(1.2f, 0.0f)
        onBoardLayout!!.isEnableAlphaAnimation = true
        onBoardLayout!!.setup()
    }

    override var pagePosition: Int
        get() = PAGE_POSITION
        set(value: Int) {
            super.pagePosition = value
        }

    companion object {

        val PAGE_POSITION = 0

        fun newInstance(): WelcomePage {
            val args = Bundle()
            val fragment = WelcomePage()
            fragment.arguments = args
            return fragment
        }
    }
}


