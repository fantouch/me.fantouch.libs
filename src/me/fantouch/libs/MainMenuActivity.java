
package me.fantouch.libs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import me.fantouch.libs.test.TestUpdateHelperActivity;

public class MainMenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_menu);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.updateHelperLib:
                startActivity(new Intent(this, TestUpdateHelperActivity.class));
                break;

            default:
                break;
        }

    }
}
