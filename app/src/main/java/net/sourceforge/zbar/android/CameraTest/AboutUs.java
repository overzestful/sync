package net.sourceforge.zbar.android.CameraTest;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

/**
 * Created by overzestful on 8/5/2558.
 */
public class AboutUs extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_aboutus);
    }
}
