package com.dopool.icntvoverseas.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.dopool.icntvoverseas.R;

/**
 * 退出的Dialog
 * 
 * @author Alisa
 * 
 */
public class ExitDialog extends Dialog {
	private TextView tv_exit_msg;
	private Button tv_exit_confirm;
	private Button tv_exit_cancel;

	public ExitDialog(Context context) {
		super(context, R.style.CustomDialog);
		View v = LayoutInflater.from(context).inflate(
				R.layout.layout_alert_dialog, null);
		tv_exit_msg = (TextView) v.findViewById(R.id.alertTitle);
		tv_exit_confirm = (Button) v.findViewById(R.id.positiveBtn);
		tv_exit_cancel = (Button) v.findViewById(R.id.negativeBtn);
		setContentView(v);
		setScreenBrightness();
		tv_exit_confirm.requestFocus();
	}

	public void setMessage(String message) {
		tv_exit_msg.setText(message);
	}

	public void setConfirm(String confirm) {
		tv_exit_confirm.setText(confirm);
	}

	public void setCancle(String cancle) {
		tv_exit_cancel.setText(cancle);
	}

	@Override
	public void show() {
		this.setCanceledOnTouchOutside(true);
		try {
			super.show();
		} catch (WindowManager.BadTokenException e) {
			// TODO: handle exception
		}
	}

	/**
	 * 此处设置亮度值。dimAmount代表黑暗数量，也就是昏暗的多少，设置为0则代表完全明亮。 范围是0.0到1.0
	 */
	private void setScreenBrightness() {
		Window window = getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.dimAmount = 0.5f;
		window.setAttributes(lp);
	}

	public ExitDialog setPositiveButton(final DialogInterface.OnClickListener l) {
		if (tv_exit_confirm != null) {
			tv_exit_confirm.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					l.onClick(ExitDialog.this, DialogInterface.BUTTON_POSITIVE);
				}
			});
		} else {
			throw new IllegalArgumentException(
					"Parameter 'positiveButtonID' is illegal !");
		}
		return this;
	}

	public ExitDialog setNegativeButton(final DialogInterface.OnClickListener l) {
		if (tv_exit_cancel != null) {
			tv_exit_cancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					l.onClick(ExitDialog.this, DialogInterface.BUTTON_NEGATIVE);
				}
			});
		} else {
			throw new IllegalArgumentException(
					"Parameter 'positiveButtonID' is illegal !");
		}
		return this;
	}

}