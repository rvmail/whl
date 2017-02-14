package com.dopool.icntvoverseas;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.dopool.icntvoverseas.app.CNTVApplication;
import com.dopool.icntvoverseas.listener.HomeKeyListener;
import com.dopool.icntvoverseas.listener.HomeKeyListener.OnHomeKeyPressListener;
import com.dopool.icntvoverseas.model.ParameterConstant;
import com.dopool.icntvoverseas.utils.Utils;
import com.dopool.icntvoverseas.view.ExitDialog;

import dopool.cntv.ottlogin.LoginInfo;
import dopool.controller.LogController;
import dopool.controller.LogController.LogListener;
import dopool.controller.LoginController;
import dopool.controller.LoginController.LoginOttListener;
import dopool.upgrade.HttpUpdateObserver;
import dopool.upgrade.UpdateUtils;
import dopool.upgrade.UpgradeInfo;

public class SplashActivity extends BaseActivity implements LoginOttListener,
		LogListener {

	private static final boolean DEBUG = true;

	// version update API
	private static final String UPDATE_PHRASE = "/stb/getUpgradeInfor/";
	private static final String SLASH = "/";

	public static final String SPLASH = "splash";
	private static final String TAG = SplashActivity.class.getSimpleName();
	private LoginController mLoginController;
	private static final int DELAY_MILLIS = 3000;
	private Handler mHander = new Handler();
	private LogController mLogController;

	private HomeKeyListener homeKeyListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		homeKeyListener = new HomeKeyListener(this);
		homeKeyListener.setOnHomeKeyPressListener(new OnHomeKeyPressListener() {

			@Override
			public void onHomeKeyPress() {
				SplashActivity.this.finish();
				System.exit(0);
			}
		});
		setContentView(R.layout.activity_splash);
		initLoginOtt();
	}

	private void initLoginOtt() {
		mLoginController = LoginController.init(this);
		mLoginController.registLoginOttListener(this);
		mLoginController.loginOtt(0);
	}

	@Override
	public void loginOttPreparing() {

	}

	@Override
	protected void onStop() {
		mHander.removeCallbacks(mDelayRunable);
		super.onStop();
	}

	@Override
	public void loginOttSuccess(LoginInfo loginInfo) {
		CNTVApplication.getInstance().setLoginInfo(loginInfo);
		mLogController = LogController.getInstance(this);
		mLogController.setLogListener(this);
		mLogController.sdkInit(null, null, loginInfo.getDeviceId(),
				loginInfo.getPlatformId(), loginInfo.getTemplateID(),
				ParameterConstant.DATASOURCE, ParameterConstant.FSOURCE, TAG);

		checkUpdate(loginInfo);
		// FIXME
		// mHander.postDelayed(mDelayRunable, DELAY_MILLIS);

	}

	private void checkUpdate(LoginInfo loginInfo) {
		// for version update
		// http://tms.is.ysten.com:8080/yst-tms/stb/getUpgradeInfor/011HE-MTKHE000001/1234/1234
		UpdateUtils mUpdateUtils = new UpdateUtils(this);
		StringBuilder builder = new StringBuilder();
		// 通过LoginInfo获取更新的服务器地址
		builder.append(loginInfo.getDeviceUpdateServer());
		builder.append(SLASH);
		builder.append(UPDATE_PHRASE);
		builder.append(loginInfo.getDeviceId());
		builder.append(SLASH);
		builder.append(loginInfo.getPlatformId());
		builder.append(SLASH);
		builder.append(Utils.getVersionCode(this));
		ArrayList<String> a = new ArrayList<String>();
		a.add(builder.toString());
		if (DEBUG) {
			Log.d(TAG, "Upgrade url: " + builder.toString());
		}
		mUpdateUtils.setApkUpdateUrls(a);

		//可增加自定义Dialog
		HttpUpdateObserver observer = new HttpUpdateObserver(context, true,
				null) {
			@Override
			public void onResult(Context context, UpgradeInfo result) {
				if (result == null) {
					mHander.postDelayed(mDelayRunable, DELAY_MILLIS);
					return;
				}
				super.onResult(context, result);
			}

			@Override
			public void onError(Context context, int result) {
				showUpdateErrorDialog();
			}

			@Override
			public void onCancel(Context context) {
				super.onCancel(context);
				showUpdateErrorDialog();
			}
		};

		mUpdateUtils.checkVersion(loginInfo.getPlatformId(), observer);
	}

	@Override
	public void loginOttFailed(LoginInfo loginInfo) {
		View v = LayoutInflater.from(this).inflate(R.layout.dialog_ottlogin,
				null);
		TextView tvTip = (TextView) v.findViewById(R.id.dialog_tv_call_service);
		TextView tvCode = (TextView) v
				.findViewById(R.id.dialog_tv_login_failed_code);
		TextView tvPositive = (TextView) v
				.findViewById(R.id.dialog_tv_positive);

		String error_code = loginInfo.getLoginState();
		tvCode.setText(error_code);
		switch (error_code) {
		case ParameterConstant.ERROR_CODE_000:
		case ParameterConstant.ERROR_CODE_775:
			tvTip.setText(getResources().getString(R.string.tip_report_error));
			break;
		case ParameterConstant.ERROR_CODE_999:
			tvTip.setText(getResources().getString(R.string.tip_auth_failed));
			break;
		case ParameterConstant.ERROR_CODE_250:
			tvTip.setText(getResources().getString(R.string.tip_service_stop));
			break;
		case ParameterConstant.ERROR_CODE_257:
			tvTip.setText(getResources().getString(R.string.tip_mac_invalid));
			break;
		case ParameterConstant.ERROR_CODE_260:
			tvTip.setText(getResources().getString(R.string.tip_token_error));
			break;
		case ParameterConstant.ERROR_CODE_765:
		case ParameterConstant.ERROR_CODE_766:
			tvTip.setText(getResources().getString(R.string.tip_network_error));
			break;
		case ParameterConstant.ERROR_CODE_776:
		case ParameterConstant.ERROR_CODE_777:
		case ParameterConstant.ERROR_CODE_788:
			tvTip.setText(getResources()
					.getString(R.string.tip_data_incomplete));
			break;
		case ParameterConstant.ERROR_CODE_755:
			tvTip.setText(getResources()
					.getString(R.string.tip_mac_read_failed));
			break;
		case ParameterConstant.ERROR_CODE_255:
			tvTip.setText(getResources().getString(
					R.string.tip_access_restriction));
			break;
		default:
			tvTip.setText(getResources().getString(
					R.string.str_login_failed_call_service));
			break;
		}
		Looper.prepare();
		ExitDialog dialog = new ExitDialog(this);
		dialog.setContentView(v);
		dialog.show();
		dialog.setCancelable(false);
		tvPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				System.exit(0);
			}
		});
		Looper.loop();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		System.exit(0);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mLoginController.unRegistLoginOttListener();
		if (mLogController != null)
			mLogController.removeLogListener(this);
	}

	private Runnable mDelayRunable = new Runnable() {

		@Override
		public void run() {
			Intent intent = new Intent(SplashActivity.this, MainActivity.class);
			intent.putExtra(SPLASH, SPLASH);
			startActivity(intent);
			finish();
		}
	};

	@Override
	public void onLogSDKInit(boolean isSuccess, String tag) {
		if (DEBUG)
			Log.i(TAG, "onLogSDKInit:" + isSuccess);
	}

	@Override
	public void onLogUpload(boolean isSuccess, String tag) {
		if (DEBUG)
			Log.i(TAG, "onLogUpload:" + isSuccess);
	}

	@Override
	public void onSetLogFileds(boolean isSuccess, String tag) {
		if (DEBUG)
			Log.i(TAG, "onSetLogFileds:" + isSuccess);
	}

	@Override
	public void onSetLoggerTag(boolean isSuccess, String tag) {
		if (DEBUG)
			Log.i(TAG, "onLogSDKInit:" + isSuccess);
	}

	@Override
	public void onLogger(boolean isSuccess, String tag) {
		if (DEBUG)
			Log.i(TAG, "onLogger:" + isSuccess);
	}

	@Override
	public void onSendLogger(boolean isSuccess, String tag) {
		if (DEBUG)
			Log.i(TAG, "onSendLogger:" + isSuccess);
	}

	@Override
	public void onGetLogSDKVersion(String version, String tag) {
		if (DEBUG)
			Log.i(TAG, "onGetLogSDKVersion:" + version);
	}

	@Override
	protected void onPause() {
		super.onPause();
		homeKeyListener.stop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		homeKeyListener.start();
	}

	private void showUpdateErrorDialog() {
		View v = LayoutInflater.from(this).inflate(R.layout.dialog_update_fail,
				null);
		TextView tvPositive = (TextView) v
				.findViewById(R.id.dialog_tv_positive);
		Looper.prepare();
		ExitDialog dialog = new ExitDialog(this);
		dialog.setContentView(v);
		dialog.show();
		dialog.setCancelable(false);
		tvPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				System.exit(0);
			}
		});
		Looper.loop();
	}
}
