package uascent.com.powercontrol.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;

import uascent.com.powercontrol.R;


public class CustomWaitDialog1 extends Dialog {
	private Context context = null;
	private static CustomWaitDialog1 customProgressDialog = null;

	public CustomWaitDialog1(Context context) {
		super(context);
		this.context = context;
	}

	public CustomWaitDialog1(Context context, int theme) {
		super(context, theme);
	}

	public static CustomWaitDialog1 createDialog(Context context) {
		customProgressDialog = new CustomWaitDialog1(context,
													 R.style.CustomProgressDialog);
		customProgressDialog.setCanceledOnTouchOutside(false);
		customProgressDialog.setContentView(R.layout.dialog_progress_custom);
		customProgressDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

		return customProgressDialog;
	}

	public void onWindowFocusChanged(boolean hasFocus) {

		if (customProgressDialog == null) {
			return;
		}

		ImageView imageView = (ImageView) customProgressDialog
				.findViewById(R.id.loadingImageView);
		AnimationDrawable animationDrawable = (AnimationDrawable) imageView
				.getBackground();
		animationDrawable.start();
	}
	
	public CustomWaitDialog1 setTitile(String strTitle){  
        return customProgressDialog;  
    }  
      
	public CustomWaitDialog1 setMessage(String strMessage){  
        TextView tvMsg = (TextView)customProgressDialog.findViewById(R.id.id_tv_loadingmsg);  
          
        if (tvMsg != null){  
            tvMsg.setText(strMessage);  
        }  
          
        return customProgressDialog;  
    }  
}
