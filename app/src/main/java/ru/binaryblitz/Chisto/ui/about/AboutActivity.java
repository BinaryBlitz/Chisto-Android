package ru.binaryblitz.Chisto.ui.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import ru.binaryblitz.Chisto.ui.base.BaseActivity;
import ru.binaryblitz.Chisto.R;
import ru.binaryblitz.Chisto.utils.AndroidUtilities;
import ru.binaryblitz.Chisto.utils.AppConfig;
import ru.binaryblitz.Chisto.utils.LogUtil;

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(ru.binaryblitz.Chisto.R.layout.activity_about);

        setOnClickListeners();
        setSocialsClickListeners();
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
                AndroidUtilities.INSTANCE.call(AboutActivity.this, AppConfig.phone);
            }
        });

        findViewById(R.id.bottom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmail(AppConfig.baseEmail);
            }
        });

        findViewById(R.id.send_mail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmail(AppConfig.partnerEmail);
            }
        });
    }

    private void setSocialsClickListeners() {
        findViewById(R.id.instagram).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSocial(AppConfig.instagram);
            }
        });

        findViewById(R.id.add_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSocial(AppConfig.googlePlay);
            }
        });

        findViewById(R.id.facebook).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSocial(AppConfig.facebook);
            }
        });

        findViewById(R.id.vk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSocial(AppConfig.vk);
            }
        });
    }

    private void openSocial(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    private void sendEmail(String address) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[] { address });
        i.putExtra(Intent.EXTRA_SUBJECT, "");
        i.putExtra(Intent.EXTRA_TEXT   , "");
        try {
            startActivity(Intent.createChooser(i, getString(R.string.send_from_about)));
        } catch (android.content.ActivityNotFoundException ex) {
            LogUtil.logException(ex);
        }
    }
}
