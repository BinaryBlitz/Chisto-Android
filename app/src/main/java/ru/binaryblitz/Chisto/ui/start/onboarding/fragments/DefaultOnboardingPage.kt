package ru.binaryblitz.Chisto.ui.start.onboarding.fragments

import ru.binaryblitz.Chisto.ui.start.onboarding.base.OnBoardingFragment
import ru.binaryblitz.Chisto.utils.AnimatedChildViewsLayout


class DefaultOnboardingPage(pagePosition: Int, pageText: String, pageImage: Int,
                            animType: AnimatedChildViewsLayout.AnimationType) : OnBoardingFragment() {
    init {
        this.pagePosition = pagePosition
        this.pageText = pageText
        this.pageImage = pageImage
        this.animType = animType
    }
}
