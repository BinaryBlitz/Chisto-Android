package ru.binaryblitz.Chisto.Activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import ru.binaryblitz.Chisto.Base.BaseActivity;
import ru.binaryblitz.Chisto.R;

public class WebActivity extends BaseActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        initWebView();

        webView.loadUrl(getIntent().getStringExtra("url"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setClientForPostLollipop();
        } else {
            setClient();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setClientForPostLollipop() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                processRedirect(request.getUrl().toString());
                return false;
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void setClient() {
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                processRedirect(url);
                return false;
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setBuiltInZoomControls(true);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            webView.getSettings().setDisplayZoomControls(false);
        }
    }

    private void processRedirect(String url) {
        if (url.contains("success") || url.contains("fail")) {
            openActivity(url.contains("success"));
        }
    }

    private void openActivity(boolean success) {
        Intent intent = new Intent();
        intent.putExtra("success", success);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
