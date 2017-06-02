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
import ru.binaryblitz.Chisto.ui.BaseActivity
import ru.binaryblitz.Chisto.ui.start.onboarding.base.OnBoardingLayout
import ru.binaryblitz.Chisto.ui.start.onboarding.fragments.DefaultOnboardingPage
import ru.binaryblitz.Chisto.ui.start.onboarding.fragments.WelcomePage

class StartActivity : BaseActivity(), ViewPager.OnPageChangeListener, View.OnClickListener {
    lateinit var pageIndicatorView: PageIndicatorView
    lateinit var nextBtn: Button
    lateinit var skipBtn: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_onboarding)

        val pages = arrayOf(
                WelcomePage.newInstance(),

                DefaultOnboardingPage(1, "Добавьте вещи в корзину,\nвоспользуйтесь поиском\nпо названию вещи.",
                        R.drawable.scene_one, OnBoardingLayout.AnimationType.InOut,
                        OnBoardingLayout.DEFAULT_SPEED),

                DefaultOnboardingPage(2, "Выберите подходящую химчистку\nпо цене и рейтингу.",
                        R.drawable.scene_two, OnBoardingLayout.AnimationType.InOut,
                        OnBoardingLayout.DEFAULT_SPEED),

                DefaultOnboardingPage(3, "Оплатите заказ картой,\nс помощью Android Pay или наличными\nкурьеру химчистки.",
                        R.drawable.scene_three, OnBoardingLayout.AnimationType.InOut,
                        OnBoardingLayout.DEFAULT_SPEED))


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
                return 4
            }
        }

        for (page in pages) {
            viewPager.addOnPageChangeListener(page)
        }
        viewPager.addOnPageChangeListener(this)

        nextBtn = findViewById(R.id.nextBtn) as Button
        skipBtn = findViewById(R.id.skip_onboarding) as ImageView
        pageIndicatorView = findViewById(R.id.pageIndicatorView) as PageIndicatorView
        pageIndicatorView.setViewPager(viewPager)
        pageIndicatorView.radius = 4
        pageIndicatorView.setAnimationType(AnimationType.SLIDE)
        pageIndicatorView.selectedColor = resources.getColor(R.color.colorPrimary)
        pageIndicatorView.unselectedColor = resources.getColor(R.color.textColor)

        nextBtn.setOnClickListener(this)
        skipBtn.setOnClickListener(this)

    }

    private fun openSelectCityScreen() {
        val openSelectCityScreenIntent = Intent(this@StartActivity, SelectCityActivity::class.java)
        startActivity(openSelectCityScreenIntent)
        finish()
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        if (position == 3) {
            pageIndicatorView.visibility = View.GONE
            nextBtn.visibility = View.VISIBLE
            return
        }
        pageIndicatorView.visibility = View.VISIBLE
        nextBtn.visibility = View.GONE

    }


    override fun onPageScrollStateChanged(state: Int) {}

    override fun onClick(v: View) {
        if (v.id == R.id.nextBtn || v.id == R.id.skip_onboarding)
            openSelectCityScreen()
    }
}

