package net.sourceforge.zbar.android.CameraTest;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;


public class ViewCalendar extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        WebView WebViw = (WebView) findViewById(R.id.webView1);

       /* WebViw.setInitialScale(5);
        WebViw.getSettings().setJavaScriptEnabled(true);
        WebViw.getSettings().setLoadWithOverviewMode(true);
        WebViw.getSettings().setUseWideViewPort(true);
        WebViw.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        WebViw.setScrollbarFadingEnabled(false);*/




        WebViw.getSettings().setJavaScriptEnabled(true);
        WebViw.loadUrl("http://web52.phuket.psu.ac.th/web52/event-calendar.php");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
