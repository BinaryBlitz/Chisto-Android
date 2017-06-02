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

public class SecondPageFragment extends OnBoardingFragment {

    public static final String TAG = SecondPageFragment.class.getSimpleName();

    public static final int PAGE_POSITION = 2;
    private OnBoardingLayout onBoardingLayout;

    public static SecondPageFragment newInstance() {
        Bundle args = new Bundle();
        SecondPageFragment fragment = new SecondPageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.onboarding_second, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onBoardingLayout = (OnBoardingLayout) view.findViewById(R.id.walker);
        onBoardingLayout.setSpeedVariance(new PointF(0.0f, 2.0f));
        onBoardingLayout.setAnimationType(OnBoardingLayout.AnimationType.InOut);
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
