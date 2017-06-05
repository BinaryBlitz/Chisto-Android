package ru.binaryblitz.Chisto.ui.start;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import ru.binaryblitz.Chisto.R;
import ru.binaryblitz.Chisto.network.DeviceInfoStore;
import ru.binaryblitz.Chisto.ui.base.BaseActivity;
import ru.binaryblitz.Chisto.ui.categories.SelectCategoryActivity;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_splash);

        if (DeviceInfoStore.getCityObject(this) == null) {
            openActivity(StartActivity.class);
        } else {
            openActivity(SelectCategoryActivity.class);
        }
    }

    private void openActivity(Class<? extends Activity> activity) {
        Intent intent = new Intent(SplashActivity.this, activity);
        startActivity(intent);
        finish();
    }
}
