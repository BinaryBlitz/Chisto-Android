package ru.binaryblitz.Chisto.ui.start;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

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


public class StartActivity extends BaseActivity implements ViewPager.OnPageChangeListener,
        View.OnClickListener {
    PageIndicatorView pageIndicatorView;
    Button nextBtn;
    ImageView skipBtn;

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

        nextBtn = (Button) findViewById(R.id.nextBtn);
        skipBtn = (ImageView) findViewById(R.id.skip_onboarding);
        pageIndicatorView = (PageIndicatorView) findViewById(R.id.pageIndicatorView);
        pageIndicatorView.setViewPager(viewPager);
        pageIndicatorView.setRadius(4);
        pageIndicatorView.setAnimationType(AnimationType.SLIDE);
        pageIndicatorView.setSelectedColor(getResources().getColor(R.color.colorPrimary));
        pageIndicatorView.setUnselectedColor(getResources().getColor(R.color.textColor));

        nextBtn.setOnClickListener(this);
        skipBtn.setOnClickListener(this);

    }

    private void openSelectCityScreen() {
        Intent openSelectCityScreenIntent = new Intent(StartActivity.this, SelectCityActivity.class);
        startActivity(openSelectCityScreenIntent);
        finish();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (position == 3) {
            pageIndicatorView.setVisibility(View.GONE);
            nextBtn.setVisibility(View.VISIBLE);
            return;
        }
        pageIndicatorView.setVisibility(View.VISIBLE);
        nextBtn.setVisibility(View.GONE);

    }


    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.nextBtn || v.getId() == R.id.skip_onboarding)
            openSelectCityScreen();
    }
}


