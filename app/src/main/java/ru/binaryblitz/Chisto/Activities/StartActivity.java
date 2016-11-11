package ru.binaryblitz.Chisto.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import ru.binaryblitz.Chisto.Base.BaseActivity;
import ru.binaryblitz.Chisto.R;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class StartActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_contact_info);

//        findViewById(R.id.start_btn).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(StartActivity.this, SelectCityActivity.class);
//                startActivity(intent);
//            }
//        });
    }
}
