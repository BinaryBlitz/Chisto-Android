package ru.binaryblitz.Chisto.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import ru.binaryblitz.Chisto.Base.BaseActivity;
import ru.binaryblitz.Chisto.R;
import ru.binaryblitz.Chisto.Utils.LogUtil;

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(ru.binaryblitz.Chisto.R.layout.activity_about);

        setOnClickListeners();
    }

    private void setOnClickListeners() {
        findViewById(R.id.left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.phone_call).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                call();
            }
        });

        findViewById(R.id.bottom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmail();
            }
        });

        findViewById(R.id.send_mail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmail();
            }
        });
    }

    private void sendEmail() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"recipient@example.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "subject of email");
        i.putExtra(Intent.EXTRA_TEXT   , "body of email");
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            LogUtil.logException(ex);
        }
    }

    private void call() {
        String number = "+7 999 111-22-33";
        Uri call = Uri.parse("tel:" + number);
        Intent intent = new Intent(Intent.ACTION_DIAL, call);
        startActivity(intent);
    }
}
