package in.co.rajkumaar.amritarepo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class WebViewActivity extends AppCompatActivity {


    ProgressDialog dialog ;
    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }
    public void dismissProgressDialog(){
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
    public void showProgressDialog(){

        dialog.setMessage("Loading..");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        Bundle b = getIntent().getExtras();
        String webviewlink = "" + b.get("webview");
        this.setTitle(webviewlink.substring(webviewlink.lastIndexOf('/'), webviewlink.length()));
        WebView mywebview = (WebView) findViewById(R.id.webView);
        mywebview.getSettings().setJavaScriptEnabled(true);
        dialog= new ProgressDialog(WebViewActivity.this);
        mywebview.setDrawingCacheBackgroundColor(getResources().getColor(R.color.colorBackground));
        mywebview.setBackgroundColor(getResources().getColor(R.color.colorBackground));
        mywebview.getSettings().setSupportZoom(true);
        mywebview.getSettings().setBuiltInZoomControls(true);
        mywebview.getSettings().setLoadWithOverviewMode(true);
        mywebview.getSettings().setUseWideViewPort(true);
        showProgressDialog();
        mywebview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                dismissProgressDialog();
            }
        });
        try {
            // load the urlz
            mywebview.loadUrl(webviewlink);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
