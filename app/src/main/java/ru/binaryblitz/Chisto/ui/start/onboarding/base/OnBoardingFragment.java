package ru.binaryblitz.Chisto.ui.start.onboarding.base;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

public abstract class OnBoardingFragment extends Fragment implements ViewPager.OnPageChangeListener {

    public static final String TAG = OnBoardingFragment.class.getSimpleName();

    protected abstract int getPagePosition();

    protected abstract OnBoardingLayout getOnBoardLayout();

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        OnBoardingLayout walkerLayout = getOnBoardLayout();
        if (walkerLayout != null) {
            int pagePosition = getPagePosition();
            if (position >= pagePosition) {
                walkerLayout.walk(1.0f - positionOffset, OnBoardingLayout.Direction.Right);
            } else if (position < pagePosition) {
                walkerLayout.walk(positionOffset, OnBoardingLayout.Direction.Left);
            }
        }
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

}