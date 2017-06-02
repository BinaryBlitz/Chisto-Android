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

public class FirstPageFragment extends OnBoardingFragment {

    public static final String TAG = FirstPageFragment.class.getSimpleName();

    public static final int PAGE_POSITION = 1;
    private OnBoardingLayout onBoarding;

    public static FirstPageFragment newInstance() {
        Bundle args = new Bundle();
        FirstPageFragment fragment = new FirstPageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.onboarding_first, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onBoarding = (OnBoardingLayout) view.findViewById(R.id.walker);
        onBoarding.setSpeedVariance(new PointF(0.0f, 2.0f));
        onBoarding.setAnimationType(OnBoardingLayout.AnimationType.InOut);
        onBoarding.setIgnoredViewTags(Arrays.asList("1", "2"));
        onBoarding.setup();
    }

    @Override
    protected int getPagePosition() {
        return PAGE_POSITION;
    }

    @Override
    protected OnBoardingLayout getOnBoardLayout() {
        return onBoarding;
    }
}
