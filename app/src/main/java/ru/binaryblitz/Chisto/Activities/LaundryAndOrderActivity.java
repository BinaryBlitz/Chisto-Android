package ru.binaryblitz.Chisto.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import ru.binaryblitz.Chisto.Base.BaseActivity;
import ru.binaryblitz.Chisto.R;
import ru.binaryblitz.Chisto.Server.DeviceInfoStore;

public class LaundryAndOrderActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_laundry_and_order);

        findViewById(R.id.cont_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean userExists = DeviceInfoStore.getUser(LaundryAndOrderActivity.this).equals("null");

                if (!userExists) openActivity(RegistrationActivity.class);
                else openActivity(ContactInfoActivity.class);
            }
        });
    }

    private void openActivity(Class<? extends Activity> activity) {
        Intent intent = new Intent(LaundryAndOrderActivity.this, activity);
        startActivity(intent);
    }
}
