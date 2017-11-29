package uascent.com.powercontrol.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import uascent.com.powercontrol.R;

/**
 * Created by maxiao on 2017/9/27.
 */

public class HelpDialog {
    private Activity mActivity;
    private AlertDialog mDialog;


    public HelpDialog(Activity activity) {
        mActivity = activity;
        init();
    }

    public AlertDialog show() {
        mDialog.show();
        return mDialog;
    }

    public void dismiss() {
        mDialog.dismiss();
    }

    private void init() {
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_help,
                (ViewGroup) mActivity.findViewById(R.id.ll_luminance));
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, AlertDialog.THEME_HOLO_LIGHT);
        mDialog = builder.show();
        mDialog.setCanceledOnTouchOutside(false);
        layout.findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mDialog.getWindow().setContentView(layout);
    }
}

