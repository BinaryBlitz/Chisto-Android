package ru.binaryblitz.Chisto.ui.start.onboarding;

import android.graphics.PointF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;

import ru.binaryblitz.Chisto.R;
import ru.binaryblitz.Chisto.ui.start.onboarding.base.OnBoardingFragment;
import ru.binaryblitz.Chisto.ui.start.onboarding.base.OnBoardingLayout;

public class ThirdPageFragment extends OnBoardingFragment {

    public static final String TAG = ThirdPageFragment.class.getSimpleName();

    public static final int PAGE_POSITION = 3;
    private OnBoardingLayout onBoardingLayout;

    public static ThirdPageFragment newInstance() {
        Bundle args = new Bundle();
        ThirdPageFragment fragment = new ThirdPageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.onboarding_third, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onBoardingLayout = (OnBoardingLayout) view.findViewById(R.id.walker);
        onBoardingLayout.setSpeed(new PointF(1.0f, 0.0f));
        onBoardingLayout.setSpeedVariance(new PointF(1.2f, 0.0f));
        onBoardingLayout.setEnableAlphaAnimation(true);
        onBoardingLayout.setIgnoredViewTags(Arrays.asList("1", "2"));
        onBoardingLayout.setup();
    }

    @Override
    protected int getPagePosition() {
        return PAGE_POSITION;
    }

    @Override
    protected OnBoardingLayout getOnBoardLayout() {
        return onBoardingLayout;

    }

}
