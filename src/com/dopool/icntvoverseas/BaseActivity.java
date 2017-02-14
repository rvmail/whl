package com.dopool.icntvoverseas;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

/**
 * @Description 基类
 * @author Alisa
 * 
 */
public abstract class BaseActivity extends FragmentActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // 设置横屏
		context = BaseActivity.this;
		breathingAnimation = AnimationUtils.loadAnimation(context,
				R.anim.breathing);
	}

	protected void openActivity(Class<?> pClass) {
		openActivity(pClass, null);
	}

	protected void openActivity(Class<?> pClass, Bundle pBundle) {
		Intent intent = new Intent(this, pClass);
		if (pBundle != null) {
			intent.putExtras(pBundle);
		}
		startActivity(intent);
		overridePendingTransition(android.R.anim.fade_in,
				android.R.anim.fade_out);
	}

	protected void openActivity(String pAction) {
		openActivity(pAction, null);
	}

	protected void openActivity(String pAction, Bundle pBundle) {
		Intent intent = new Intent(pAction);
		if (pBundle != null) {
			intent.putExtras(pBundle);
		}
		startActivity(intent);
		overridePendingTransition(android.R.anim.fade_in,
				android.R.anim.fade_out);
	}

	/**
	 * 内存空间不足
	 */
	protected void handleOutmemoryError() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(BaseActivity.this, getResources().getString(R.string.tip_no_enough_memory),
						Toast.LENGTH_SHORT)
						.show();
				finish();
			}
		});
	}

	/**
	 * Activity关闭和启动动画
	 */
	public void finish() {
		super.finish();
		overridePendingTransition(android.R.anim.fade_in,
				android.R.anim.fade_out);
	}

	protected Context context;
	protected Animation breathingAnimation;
	protected int mWidth;
	protected int mHeight;
	protected String from;
	protected String devicetype;
	protected String version;
	protected String params;

	protected double screenSize;
	protected AudioManager mAudioManager = null;
}
