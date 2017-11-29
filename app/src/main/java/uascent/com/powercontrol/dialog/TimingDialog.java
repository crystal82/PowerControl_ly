package uascent.com.powercontrol.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.carbswang.android.numberpickerview.library.NumberPickerView;
import uascent.com.powercontrol.R;
import uascent.com.powercontrol.utils.Lg;

/**
 * Created by maxiao on 2017/9/25.
 */

public class TimingDialog implements NumberPickerView.OnValueChangeListener {

    private Activity         mActivity;
    private AlertDialog      mDialog;
    private NumberPickerView mPickerViewH;
    private EventListener    mListener;
    int hour = 0;
    private boolean mIsStopScroll =false;

    public abstract static class EventListener {
        public void onTimeChanged(int hour) {
        }
    }

    public TimingDialog(Activity activity, int h, EventListener listener) {
        mActivity = activity;
        mListener = listener;
        this.hour = h;
        init();
    }

    public AlertDialog show() {
        mDialog.show();
        return mDialog;
    }

    public boolean isShowing() {
        return mDialog.isShowing();
    }

    public void dismiss() {
        mDialog.dismiss();
    }

    private void init() {
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_timing,
                                       (ViewGroup) mActivity.findViewById(R.id.ll_luminance));
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, AlertDialog.THEME_HOLO_LIGHT);
        mDialog = builder.show();
        mPickerViewH = (NumberPickerView) layout.findViewById(R.id.picker_hour);
        layout.findViewById(R.id.tv_dialog_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        layout.findViewById(R.id.tv_dialog_enter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPickerViewH.isScrollFinish()) {
                    mListener.onTimeChanged(hour);
                    dismiss();
                }else{
                    mPickerViewH.stopScrollingAndCorrectPosition();
                    mIsStopScroll = true;
                }
            }
        });
        mDialog.getWindow().setContentView(layout);
        mPickerViewH.setOnValueChangedListener(this);
        setData(mPickerViewH, 0, 48, hour);
    }

    private void setData(NumberPickerView picker, int minValue, int maxValue, int value) {
        picker.setMinValue(minValue);
        picker.setMaxValue(maxValue);
        picker.setValue(value);
    }

    @Override
    public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {
        Lg.e("oldVal=" + oldVal + "  newVal=" + newVal);
        hour = newVal;
        if(mIsStopScroll){
            Lg.e("hour="+ hour);
            mListener.onTimeChanged(hour);
            dismiss();
        }
    }
}

