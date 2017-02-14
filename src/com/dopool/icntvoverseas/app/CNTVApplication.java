package com.dopool.icntvoverseas.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.dopool.icntvoverseas.model.ParameterConstant;
import com.dopool.icntvoverseas.utils.FontUtils;

import dopool.cntv.ottlogin.LoginInfo;

public class CNTVApplication extends Application {
	private static CNTVApplication mApplication;
	private LoginInfo mLoginInfo;
	String macAddress;
	private static final String LOGININFOPREFERENCE = "loginInfoPreference";

	public static CNTVApplication getInstance() {
		return mApplication;
	}

	@Override
	public void onCreate() {
		mApplication = this;
		getMacAddress();
		super.onCreate();
		// 全局替换系统字体MONOSPACE，需要在application的style里设
		// <item name="android:typeface">monospace</item>
		FontUtils.getInstance().replaceSystemDefaultFontFromAsset(this,
				"fonts/msyh.ttf");
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	public LoginInfo getLoginInfo() {

		if (mLoginInfo == null) {
			SharedPreferences loginInfoPreferences = this.getSharedPreferences(
					LOGININFOPREFERENCE, Context.MODE_PRIVATE);
			mLoginInfo = new LoginInfo();
			String loginState = loginInfoPreferences.getString(
					ParameterConstant.LOGIN_STATE, "");
			mLoginInfo.setLoginState(loginState);
			String userId = loginInfoPreferences.getString(
					ParameterConstant.USER_ID, "");
			mLoginInfo.setUserId(userId);
			String deviceId = loginInfoPreferences.getString(
					ParameterConstant.DEVICE_ID, "");
			mLoginInfo.setDeviceId(deviceId);
			String templateID = loginInfoPreferences.getString(
					ParameterConstant.TEMPLATE_ID, "");
			mLoginInfo.setTemplateID(templateID);
			String platformId = loginInfoPreferences.getString(
					ParameterConstant.PLATFORM_ID, "");
			mLoginInfo.setPlatformId(platformId);
			String token = loginInfoPreferences.getString(
					ParameterConstant.TOKEN, "");
			mLoginInfo.setToken(token);
			String epgServer = loginInfoPreferences.getString(
					ParameterConstant.EPG_SERVER, "");
			mLoginInfo.setEpgServer(epgServer);
			String searchServer = loginInfoPreferences.getString(
					ParameterConstant.SEARCH_SERVER, "");
			mLoginInfo.setSearchServer(searchServer);
			String logServer = loginInfoPreferences.getString(
					ParameterConstant.LOG_SERVER, "");
			mLoginInfo.setLogServer(logServer);
			String snsServer = loginInfoPreferences.getString(
					ParameterConstant.SNS_SERVER, "");
			mLoginInfo.setSnsServer(snsServer);
			String deviceUpdateServer = loginInfoPreferences.getString(
					ParameterConstant.DEVICE_UPDATE_SERVER, "");
			mLoginInfo.setDeviceUpdateServer(deviceUpdateServer);
			String version = loginInfoPreferences.getString(
					ParameterConstant.VERSION, "");
			mLoginInfo.setVersion(version);
		}
		return mLoginInfo;
	}

	public void setLoginInfo(LoginInfo loginInfo) {
		this.mLoginInfo = loginInfo;
		SharedPreferences loginInfoPreferences = this.getSharedPreferences(
				LOGININFOPREFERENCE, Context.MODE_PRIVATE);
		Editor editor = loginInfoPreferences.edit();
		editor.putString("loginState", mLoginInfo.getLoginState());
		editor.putString("userId", mLoginInfo.getUserId());
		editor.putString("deviceId", mLoginInfo.getDeviceId());
		editor.putString("templateID", mLoginInfo.getTemplateID());
		editor.putString("platformId", mLoginInfo.getPlatformId());
		editor.putString("token", mLoginInfo.getToken());
		editor.putString("epgServer", mLoginInfo.getEpgServer());
		editor.putString("searchServer", mLoginInfo.getSearchServer());
		editor.putString("logServer", mLoginInfo.getLogServer());
		editor.putString("snsServer", mLoginInfo.getSnsServer());
		editor.putString("deviceUpdateServer",
				mLoginInfo.getDeviceUpdateServer());
		editor.putString("version", mLoginInfo.getVersion());
		editor.commit();
	}

	private void getMacAddress() {
		WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = (null == wifiMgr ? null : wifiMgr.getConnectionInfo());
		if (null != info) {
			macAddress = info.getMacAddress();
		}
	}

	public String getMac() {
		return macAddress;
	}

}
