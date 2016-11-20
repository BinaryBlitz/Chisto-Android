package ru.binaryblitz.Chisto.Activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import ru.binaryblitz.Chisto.Base.BaseActivity;
import ru.binaryblitz.Chisto.R;

public class MyOrderActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_my_order);

        findViewById(R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
