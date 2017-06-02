package ru.binaryblitz.Chisto.ui.start;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.crashlytics.android.Crashlytics;
import com.rd.PageIndicatorView;
import com.rd.animation.type.AnimationType;

import io.fabric.sdk.android.Fabric;
import ru.binaryblitz.Chisto.R;
import ru.binaryblitz.Chisto.ui.BaseActivity;
import ru.binaryblitz.Chisto.ui.start.onboarding.FirstPageFragment;
import ru.binaryblitz.Chisto.ui.start.onboarding.SecondPageFragment;
import ru.binaryblitz.Chisto.ui.start.onboarding.ThirdPageFragment;
import ru.binaryblitz.Chisto.ui.start.onboarding.WelcomePageFragment;
import ru.binaryblitz.Chisto.ui.start.onboarding.base.OnBoardingFragment;

public class StartActivity extends BaseActivity implements ViewPager.OnPageChangeListener {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_onboarding);

        final OnBoardingFragment welcomePageFragment = WelcomePageFragment.newInstance();
        final OnBoardingFragment firstPageFragment = FirstPageFragment.newInstance();
        final OnBoardingFragment secondPageFragment = SecondPageFragment.newInstance();
        final OnBoardingFragment thirdPageFragment = ThirdPageFragment.newInstance();


        final ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case WelcomePageFragment.PAGE_POSITION:
                        return welcomePageFragment;
                    case FirstPageFragment.PAGE_POSITION:
                        return firstPageFragment;
                    case SecondPageFragment.PAGE_POSITION:
                        return secondPageFragment;
                    case ThirdPageFragment.PAGE_POSITION:
                        return thirdPageFragment;
                }
                return null;
            }

            @Override
            public int getCount() {
                return 4;
            }
        });

        viewPager.addOnPageChangeListener(welcomePageFragment);
        viewPager.addOnPageChangeListener(firstPageFragment);
        viewPager.addOnPageChangeListener(secondPageFragment);
        viewPager.addOnPageChangeListener(thirdPageFragment);
        viewPager.addOnPageChangeListener(this);

        PageIndicatorView pageIndicatorView = (PageIndicatorView) findViewById(R.id.pageIndicatorView);
        pageIndicatorView.setViewPager(viewPager);
        pageIndicatorView.setRadius(4);
        pageIndicatorView.setAnimationType(AnimationType.SLIDE);
        pageIndicatorView.setSelectedColor(getResources().getColor(R.color.colorPrimary));
        pageIndicatorView.setUnselectedColor(getResources().getColor(R.color.textColor));

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}
