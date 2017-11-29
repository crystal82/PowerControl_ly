package uascent.com.powercontrol.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uascent.com.powercontrol.R;
import uascent.com.powercontrol.bean.BleScanBean;
import uascent.com.powercontrol.utils.MyUtils;

/**
 * 作者：HWQ on 2017/5/11 16:29
 * 描述：
 */

public class BleConnectAdapter extends BaseAdapter {
    private List<BleScanBean> mStringList;
    private Context           context;
    private LayoutInflater    inflater;

    public BleConnectAdapter(Context context, List<BleScanBean> items) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.mStringList = items;
    }

    @Override
    public int getCount() {
        return mStringList.size();
    }

    @Override
    public Object getItem(int position) {
        return mStringList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_ble_connect, null);
            holder = new ViewHolder(convertView, context);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        BleScanBean bleScanBean = mStringList.get(position);
        holder.initData(bleScanBean);
        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.tv_ble_name)
        TextView  mTvBleName;
        @BindView(R.id.tv_ble_mac)
        TextView  mTvBleMac;
        @BindView(R.id.iv_ble_icon)
        ImageView mIvBleIcon;
        @BindView(R.id.tv_ble_state)
        TextView  mTvBleState;

        Context mContext;

        ViewHolder(View view, Context context) {
            ButterKnife.bind(this, view);
            mContext = context;
        }

        private void initData(BleScanBean bleScanBean) {
            mTvBleName.setText(bleScanBean.name);
           // mTvBleMac.setText(bleScanBean.mac);
            if (bleScanBean.state) {
                onStateConnect();
            } else {
                onStateDisConnect();
            }
        }

        private void onStateConnect() {
            mTvBleState.setText("Connected");
            mTvBleState.setTextColor(0xff00A650);
            MyUtils.setDrawable(mContext, mIvBleIcon, R.mipmap.ble_connected);
        }

        private void onStateDisConnect() {
            mTvBleState.setTextColor(0xff000000);
            mTvBleState.setText("Not Connected");
            MyUtils.setDrawable(mContext, mIvBleIcon, R.mipmap.ble_disconnect_icon);
        }
    }
}