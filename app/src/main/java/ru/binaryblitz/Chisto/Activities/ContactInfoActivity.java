package ru.binaryblitz.Chisto.Activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import ru.binaryblitz.Chisto.Base.BaseActivity;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class ContactInfoActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(ru.binaryblitz.Chisto.R.layout.activity_contact_info);
    }
}
