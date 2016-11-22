package ru.binaryblitz.Chisto.Activities;

import android.os.Build;
import android.os.Bundle;
import android.webkit.WebView;

import ru.binaryblitz.Chisto.Base.BaseActivity;
import ru.binaryblitz.Chisto.R;

public class WebActivity extends BaseActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setBuiltInZoomControls(true);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            webView.getSettings().setDisplayZoomControls(false);
        }

        webView.loadUrl(getIntent().getStringExtra("url"));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
