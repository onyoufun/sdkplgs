package com.linxcool.sdkface.minigame;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.ryan.github.view.FastWebView;
import com.ryan.github.view.FastWebViewPool;
import com.ryan.github.view.config.CacheConfig;
import com.ryan.github.view.config.DefaultMimeTypeFilter;
import com.ryan.github.view.config.FastCacheMode;
import com.ryan.github.view.offline.Chain;
import com.ryan.github.view.utils.LogUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class WebViewActivity extends AppCompatActivity {

    private static final String TAG = "WebViewActivity";
    private FastWebView fastWebView;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface", "JavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogUtils.d("------------- start once load -------------");

        FastWebView.setDebug(true);
        fastWebView = new FastWebView(this);
        fastWebView.setWebChromeClient(new MonitorWebChromeClient());
        fastWebView.setWebViewClient(new MonitorWebViewClient());
        fastWebView.setFocusable(true);
        fastWebView.setFocusableInTouchMode(true);

        setContentView(fastWebView);

        WebSettings webSettings = fastWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);
        webSettings.setDefaultTextEncodingName("UTF-8");
        webSettings.setBlockNetworkImage(true);

        // 设置正确的cache mode以支持离线加载
        webSettings.setCacheMode(NetworkUtils.getWebCacheMode(this));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webSettings.setAllowFileAccessFromFileURLs(true);
            webSettings.setAllowUniversalAccessFromFileURLs(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptThirdPartyCookies(fastWebView, true);
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }
        CacheConfig config = new CacheConfig.Builder(this)
                .setCacheDir(getCacheDir() + File.separator + "minigame")
                .setExtensionFilter(new CustomMimeTypeFilter())
                .build();
        fastWebView.setCacheMode(FastCacheMode.FORCE, config);
        fastWebView.addResourceInterceptor((Chain chain) -> chain.process(chain.getRequest()));
        fastWebView.addJavascriptInterface(this, "android");

        Map<String, String> headers = new HashMap<>();
        headers.put("custom", "test");
        String url = "https://meta-igs.web.app/bigfish/index.html";
        fastWebView.loadUrl(url, headers);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fastWebView != null) {
            fastWebView.destroy();
        }
    }

    public class MonitorWebViewClient extends WebViewClient {

        private boolean first = true;

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            view.getSettings().setBlockNetworkImage(false);
            view.loadUrl("javascript:android.sendResource(JSON.stringify(window.performance.timing))");
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            if (first) {
                first = false;
            }
            return super.shouldInterceptRequest(view, request);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (fastWebView.canGoBack()) {
                fastWebView.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public class MonitorWebChromeClient extends WebChromeClient {

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
        }
    }

    public class CustomMimeTypeFilter extends DefaultMimeTypeFilter {
        CustomMimeTypeFilter() {
            addMimeType("text/html");
        }
    }
}