package com.yiming.demo;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.yiming.optionalpullview.YMOptionalPullView;

public class WebViewDemo extends AppCompatActivity implements YMOptionalPullView.OnPullListener {

    private YMOptionalPullView opv;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_webview);
        opv = (YMOptionalPullView) findViewById(R.id.opv);
        webView = (WebView) findViewById(R.id.webview);
        initYMOptionalPullView();
        initWebview();
    }

    private void initYMOptionalPullView() {
        opv.setOnPullListener(this);
    }

    private void initWebview() {
        webView.loadUrl("http://www.baidu.com");
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setJavaScriptEnabled(true);
        settings.setDefaultTextEncodingName("UTF-8");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }

    @Override
    public YMOptionalPullView.Mode getMode() {//设置支持上下拉
        return YMOptionalPullView.Mode.DOWN_PULL;
    }

    @Override
    public void onLoad(final YMOptionalPullView opv, boolean isDownPull) {
        webView.reload();
        opv.notifyLoadComplete(true);
    }

    @Override
    public void onBackPressed() {
        if(webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
