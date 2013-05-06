
package me.fantouch.libs.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import me.fantouch.libs.R;
import me.fantouch.libs.iostitlebar.IOSTitleBar;
import me.fantouch.libs.iostitlebar.TitleStateBundle;

public class TestIOSTitleBarActivity extends Activity {
    private IOSTitleBar titelbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_iostitlebar);
        titelbar = (IOSTitleBar) findViewById(R.id.titelbar);
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back) {
            titelbar.performBundle(makeBundle(false), false, true);
        } else if (id == R.id.next) {
            titelbar.performBundle(makeBundle(true), true, true);

        }
    }

    int titleIdx = 0;

    private TitleStateBundle makeBundle(boolean isNext) {
        if (isNext) {
            titleIdx++;
            return new TitleStateBundle(true, "titleIdx" + titleIdx, null, true, null, titleIdx
                    + "");
        } else {
            titleIdx--;
            return new TitleStateBundle(true, "titleIdx" + titleIdx, null, true, null, titleIdx
                    + "");

        }

    }
}
