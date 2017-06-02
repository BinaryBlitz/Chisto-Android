package ru.binaryblitz.Chisto.ui.start.onboarding.fragments

import android.graphics.PointF
import ru.binaryblitz.Chisto.ui.start.onboarding.base.OnBoardingFragment
import ru.binaryblitz.Chisto.utils.OnBoardingLayout


class DefaultOnboardingPage(pagePosition: Int, pageText: String, pageImage: Int,
                            animType: OnBoardingLayout.AnimationType,
                            animSpeed: PointF) : OnBoardingFragment() {
    init {
        this.pagePosition = pagePosition
        this.pageText = pageText
        this.pageImage = pageImage
        this.animSpeed = animSpeed
        this.animType = animType
    }
}
