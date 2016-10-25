package com.chisto.Activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.chisto.Base.BaseActivity;
import com.chisto.R;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class SelectServiceActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_onboarding);
    }
}
