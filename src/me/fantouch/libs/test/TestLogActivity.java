
package me.fantouch.libs.test;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import me.fantouch.libs.R;
import me.fantouch.libs.log.ELog;

public class TestLogActivity extends Activity {
    private static final int TEST_TIMES = 1000;
    private CheckBox toFileChkbox;
    private TextView tv;
    private Button startBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_log);
        toFileChkbox = (CheckBox) TestLogActivity.this.findViewById(R.id.toFileChkbox);
        tv = (TextView) findViewById(R.id.tv);
        startBtn = (Button) findViewById(R.id.startBtn);
        startBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ELog.setEnableLogcat(true);
                ELog.setEnableLogToFile(toFileChkbox.isChecked(), TestLogActivity.this);
                v.setEnabled(false);
                tv.append("\n\n");
                tv.append("Log循环输出" + TEST_TIMES + "次耗时" + TestLog() + "毫秒");
                tv.append("\n");
                tv.append("ELog循环输出" + TEST_TIMES + "次耗时" + TestELog() + "毫秒");
                v.setEnabled(true);
            }
        });
    }

    private long TestLog() {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < TEST_TIMES; i++) {
            Log.v("TestLog", i + "");
        }
        return System.currentTimeMillis() - startTime;
    }

    private long TestELog() {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < TEST_TIMES; i++) {
            ELog.v(i + "");
        }
        return System.currentTimeMillis() - startTime;
    }
}
