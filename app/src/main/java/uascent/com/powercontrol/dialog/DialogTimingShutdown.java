package uascent.com.powercontrol.dialog;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import uascent.com.powercontrol.R;
import uascent.com.powercontrol.base.BaseActivity;
import uascent.com.powercontrol.view.WheelView;

/**
 * 自动关机时间设置
 */
public class DialogTimingShutdown extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_timing_shutdown);

        WheelView wv_time = (WheelView) findViewById(R.id.wv_time);
        wv_time.setOffset(1);
        wv_time.setItems(getScopeList(1, 48));
        wv_time.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onSelected(int selectedIndex, String item) {
                Log.d("onSelected", "selectedIndex: " + selectedIndex + ", item: " + item);
            }
        });
    }

    private List<String> getScopeList(int start, int end) {
        ArrayList<String> arr = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            arr.add(i + "");
        }
        return arr;
    }
}
