package ru.binaryblitz.Chisto.ui.start

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.crashlytics.android.Crashlytics
import com.rd.PageIndicatorView
import com.rd.animation.type.AnimationType
import io.fabric.sdk.android.Fabric
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.ui.base.BaseActivity
import ru.binaryblitz.Chisto.ui.start.onboarding.base.OnBoardingFragment
import ru.binaryblitz.Chisto.ui.start.onboarding.fragments.DefaultOnboardingPage
import ru.binaryblitz.Chisto.ui.start.onboarding.fragments.WelcomePage
import ru.binaryblitz.Chisto.utils.AnimatedChildViewsLayout

class StartActivity : BaseActivity(), ViewPager.OnPageChangeListener, View.OnClickListener {
    lateinit var pageIndicatorView: PageIndicatorView
    lateinit var nextButton: Button
    lateinit var skipButton: ImageView
    val PAGES_COUNT = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_onboarding)

        val viewPager = initOnBoardingPages()
        initViewElements(viewPager)
    }

    private fun initViewElements(viewPager: ViewPager) {
        nextButton = findViewById(R.id.nextBtn) as Button
        skipButton = findViewById(R.id.skip_onboarding) as ImageView
        pageIndicatorView = findViewById(R.id.pageIndicatorView) as PageIndicatorView
        pageIndicatorView.setViewPager(viewPager)
        pageIndicatorView.radius = 4
        pageIndicatorView.setAnimationType(AnimationType.SLIDE)
        pageIndicatorView.selectedColor = resources.getColor(R.color.colorPrimary)
        pageIndicatorView.unselectedColor = resources.getColor(R.color.textColor)

        nextButton.setOnClickListener(this)
        skipButton.setOnClickListener(this)
    }

    private fun initOnBoardingPages(): ViewPager {
        val pages = arrayOf(
                WelcomePage.newInstance(),
                DefaultOnboardingPage(1, getString(R.string.page_one_text),
                        R.drawable.scene_one, AnimatedChildViewsLayout.AnimationType.InOut),
                DefaultOnboardingPage(2, getString(R.string.page_two_text),
                        R.drawable.scene_two, AnimatedChildViewsLayout.AnimationType.InOut),
                DefaultOnboardingPage(3, getString(R.string.page_three_text),
                        R.drawable.scene_three, AnimatedChildViewsLayout.AnimationType.InOut))
        return initViewPager(pages)
    }

    private fun initViewPager(pages: Array<OnBoardingFragment>): ViewPager {
        val viewPager = findViewById(R.id.view_pager) as ViewPager
        viewPager.adapter = object : FragmentPagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment? {
                when (position) {
                    0 -> return pages[0]
                    1 -> return pages[1]
                    2 -> return pages[2]
                    3 -> return pages[3]
                }
                return null
            }

            override fun getCount(): Int {
                return PAGES_COUNT
            }
        }
        for (page in pages) {
            viewPager.addOnPageChangeListener(page)
        }
        viewPager.addOnPageChangeListener(this)
        return viewPager
    }

    private fun openSelectCityScreen() {
        val openSelectCityScreenIntent = Intent(this@StartActivity, SelectCityActivity::class.java)
        startActivity(openSelectCityScreenIntent)
        finish()
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        if (position == PAGES_COUNT - 1) {
            pageIndicatorView.visibility = View.GONE
            nextButton.visibility = View.VISIBLE
            return
        }
        pageIndicatorView.visibility = View.VISIBLE
        nextButton.visibility = View.GONE
    }

    override fun onPageScrollStateChanged(state: Int) {}

    override fun onClick(v: View) {
        if (v.id == R.id.nextBtn || v.id == R.id.skip_onboarding)
            openSelectCityScreen()
    }
}
