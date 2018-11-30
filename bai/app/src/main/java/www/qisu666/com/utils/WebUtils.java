package www.qisu666.com.utils;

import android.net.http.SslError;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.io.IOException;

/**
 * webview设置
 */
public class WebUtils {
    public static void webviewPay(WebView wb_recharge,
                                  final ProgressBar pb_recharge,
                                  String alipayurl) throws IOException {
        pb_recharge.setVisibility(View.VISIBLE);
        wb_recharge.setVisibility(View.VISIBLE);
        wb_recharge.clearCache(true);// 设置清除缓存
        wb_recharge.clearHistory();// 设置清楚历史记录

        wb_recharge.getSettings().setAppCacheEnabled(false);
        wb_recharge.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    pb_recharge.setVisibility(View.GONE);
                } else {
                    if (View.GONE == pb_recharge.getVisibility()) {
                        pb_recharge.setVisibility(View.VISIBLE);
                    }
                    pb_recharge.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }
        });

        wb_recharge.getSettings().setJavaScriptEnabled(true);

        // 设置一直网页访问,不跳转到外部浏览器
        // wb_recharge.setWebViewClient(new WebViewClient());
        wb_recharge.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);//
        wb_recharge.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedSslError(WebView view,
                                           SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                view.loadUrl(url);   //在2.3上面不加这句话，可以加载出页面，在4.0上面必须要加入，不然出现白屏
                return true;
            }
        });
        wb_recharge.loadUrl(alipayurl);

    }
}
