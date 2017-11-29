package uascent.com.powercontrol.dialog;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import uascent.com.powercontrol.constant.MyConstant;
import uascent.com.powercontrol.R;
import uascent.com.powercontrol.base.BaseActivity;

public class DialogConnectPsd extends BaseActivity {


    @BindView(R.id.et_connect_psd)
    EditText mEtConnectPsd;
    @BindView(R.id.tv_dialog_cancel)
    TextView mTvDialogCancel;
    @BindView(R.id.tv_dialog_enter)
    TextView mTvDialogEnter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_connect_psd);
        ButterKnife.bind(this);

        initListener();
    }

    private void initListener() {
        mTvDialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mTvDialogEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String psd = mEtConnectPsd.getText().toString();
                if (TextUtils.isEmpty(psd) || psd.length() != 6) {
                    showShortToast(getString(R.string.connect_psd_error));
                    return;
                }

                Intent intent = new Intent();
                intent.putExtra(MyConstant.CONNECT_PSD, psd);
                setResult(MyConstant.RESULT_CODE_CONNECT_PSD, intent);
                finish();
            }
        });
    }
}
