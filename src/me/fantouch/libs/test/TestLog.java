
package me.fantouch.libs.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import me.fantouch.libs.R;
import me.fantouch.libs.log.ELog;

public class TestLog extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity_log);

        findViewById(R.id.startBtn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);

                for (int i = 0; i < 10; i++) {
                    ELog.v(i + "");
                }

                v.setEnabled(true);
            }
        });

        ELog.setEnableLogCat(true);
        ELog.setEnableLogToFile(true, this);
    }
}
