
package me.fantouch.libs.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import me.fantouch.libs.R;

public class TestCrashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_crash);
    }

    public void onClick(View v) {
        if (v.getId() == R.id.crashBtn)
            throw new NullPointerException("Yeah~,it crashed");
    }
}
