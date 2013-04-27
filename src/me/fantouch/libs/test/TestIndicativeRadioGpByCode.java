
package me.fantouch.libs.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

import me.fantouch.libs.R;
import me.fantouch.libs.indicativeradio.IndicativeRadioGroup;

public class TestIndicativeRadioGpByCode extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        RadioGroup gp = (RadioGroup) View.inflate(this, R.layout.indicativeradio_radiogp, null);
        IndicativeRadioGroup groupByCodeGroup = new IndicativeRadioGroup(this, gp);
        setContentView(groupByCodeGroup);
    }
}
