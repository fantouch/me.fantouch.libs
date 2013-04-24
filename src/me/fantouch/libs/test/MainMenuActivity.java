
package me.fantouch.libs.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import me.fantouch.libs.R;
import me.fantouch.libs.log.Logg;

public class MainMenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logg.d("Hello~~");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Logg.sendReportFiles(getApplicationContext(), SendService.class);
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.updateHelper) {
            startActivity(new Intent(this, TestUpdateHelperActivity.class));
        } else if (id == R.id.log) {
            startActivity(new Intent(this, TestLogActivity.class));
        } else if (id == R.id.crash) {
            startActivity(new Intent(this, TestCrashActivity.class));
        } else if (id == R.id.indicativeRadioGp) {
            startActivity(new Intent(this, TestIndicativeRadioGp.class));
        }

    }
}
