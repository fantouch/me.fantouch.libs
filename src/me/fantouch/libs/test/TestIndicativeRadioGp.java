
package me.fantouch.libs.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import me.fantouch.libs.R;
import me.fantouch.libs.indicativeradio.IndicativeRadioGroup;

public class TestIndicativeRadioGp extends Activity {
    private IndicativeRadioGroup mIndicativeRadioGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_indicative_radiogp);

        mIndicativeRadioGroup = (IndicativeRadioGroup) findViewById(R.id.mIndicativeRadioGroup);
    }

    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.hide) {
            mIndicativeRadioGroup.hide();
        } else if (id == R.id.show) {
            mIndicativeRadioGroup.show();
        }
    }

}
