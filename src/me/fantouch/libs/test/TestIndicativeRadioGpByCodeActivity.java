
package me.fantouch.libs.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.RelativeLayout.LayoutParams;

import me.fantouch.libs.R;
import me.fantouch.libs.indicativeradio.IndicativeRadioGroup;

public class TestIndicativeRadioGpByCodeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        RadioGroup gp = (RadioGroup) View.inflate(this, R.layout.indicativeradio_radiogp, null);
        gp.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT));

        IndicativeRadioGroup groupByCodeGroup = new IndicativeRadioGroup(this, gp);
        groupByCodeGroup.setBackgroundResource(android.R.color.white);
        groupByCodeGroup.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT));

        setContentView(groupByCodeGroup);
    }
}
