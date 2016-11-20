package ru.binaryblitz.Chisto.Activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import ru.binaryblitz.Chisto.Base.BaseActivity;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import ru.binaryblitz.Chisto.R;

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(ru.binaryblitz.Chisto.R.layout.activity_about);

        findViewById(R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
