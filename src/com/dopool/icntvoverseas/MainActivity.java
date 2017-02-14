package com.dopool.icntvoverseas;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dopool.icntvoverseas.app.CNTVApplication;
import com.dopool.icntvoverseas.entity.ChannelItem;
import com.dopool.icntvoverseas.model.ParameterConstant;
import com.dopool.icntvoverseas.model.WindowMessageID;
import com.dopool.icntvoverseas.recomview.view.RecomBoxView;
import com.dopool.icntvoverseas.recomview.view.RecommendPanelFragment;
import com.dopool.icntvoverseas.utils.Utils;
import com.dopool.icntvoverseas.view.ExitDialog;

import dopool.cntv.ottlogin.LoginInfo;
import dopool.cntv.recommendation.model.RecomBoxData;
import dopool.cntv.recommendation.model.RecomPanel;
import dopool.controller.EpgController;
import dopool.controller.EpgController.EpgInitListener;
import dopool.controller.LogController;
import dopool.controller.RecommendController;
import dopool.controller.RecommendController.RecommendRequestListener;

public class MainActivity extends BaseActivity implements OnClickListener,
		EpgInitListener, OnFocusChangeListener, RecommendRequestListener {

	public static final String CHANNELS = "channels";
	private static final String TAG = MainActivity.class.getSimpleName();
	private static boolean DEBUG = true;

	// 推荐位Fragment的TAG
	private static String RECOM_FRAGMENT_TAG = "recom_fragment_tag";
	private int wifiLevel = 4;

	private EpgController mEpgController;

	private ImageView iv_search, iv_setting, iv_wifi;
	private WifiManager wifiManager = null; // Wifi管理器
	private WifiInfo wifiInfo = null; // 获得的Wifi信息
	private static final long INTERVAL_WIFI = 1000 * 10;

	private RecommendPanelFragment mRecommendFragment;

	private RelativeLayout rl_alive, rl_vod, rl_history, rl_collect;
	private TextView tv_alive, tv_vod, tv_history, tv_collect, tv_alive_en,
			tv_vod_en, tv_history_en, tv_collect_en;

	boolean jumpToLive;
	long delay = 10 * 1000;
	boolean hasOperation = false;

	private RelativeLayout rootLayout;
	private View oldFocusView = null;

	private FrameLayout fl_loading;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		if (getIntent().getStringExtra(SplashActivity.SPLASH).equals(
				SplashActivity.SPLASH)) {
			jumpToLive = true;
		}

		initEpgController();

		initUI();

		iv_search.setOnClickListener(this);
		iv_setting.setOnClickListener(this);

		rl_alive.setOnClickListener(this);
		rl_vod.setOnClickListener(this);
		rl_history.setOnClickListener(this);
		rl_collect.setOnClickListener(this);
		rl_alive.setOnFocusChangeListener(this);
		rl_vod.setOnFocusChangeListener(this);
		rl_history.setOnFocusChangeListener(this);
		rl_collect.setOnFocusChangeListener(this);

		initRecommendFragment();

		getChannels();
		// 数据统计：进入应用（开机）
		LogController.getInstance(this)
				.logUpload(
						ParameterConstant.ONOROFF,
						ParameterConstant.ONOROFF_ON + ","
								+ Utils.getVersionCode(this), TAG);
		// 数据统计：进入首页
		LogController.getInstance(this).logUpload(ParameterConstant.HOME,
				ParameterConstant.HOME_ENTER + "", TAG);
		// 确认从splash进入，启动计时，10s不操作进入直播
		if (jumpToLive) {
			TimerTask task = new TimerTask() {

				@Override
				public void run() {
					if (!hasOperation) {
						jumpToLive = false;
						Intent intent = new Intent(MainActivity.this,
								LiveActivity.class);
						startActivity(intent);
					}
				}
			};
			Timer timer = new Timer();
			timer.schedule(task, delay);
		}
	}

	private void getChannels() {
		SharedPreferences preferences = getSharedPreferences(CHANNELS,
				Context.MODE_PRIVATE);
		String result = preferences.getString(Utils.getVersionCode(this) + "",
				"");
		if (result == null || result.equals("")) {
			parseXml();
			Utils.saveObject(this, Utils.getVersionCode(this) + "", channels);
		}
	}

	ArrayList<ChannelItem> channels = new ArrayList<ChannelItem>();

	private void parseXml() {
		XmlPullParserFactory factory;
		try {
			InputStream open = getResources().getAssets().open("resource.xml");
			factory = XmlPullParserFactory.newInstance();
			XmlPullParser xpp = factory.newPullParser();
			xpp.setInput(open, "UTF-8");
			int evtType = xpp.getEventType();
			// 一直循环，直到文档结束
			ChannelItem channel = null;
			ArrayList<String> properties = null;
			while (evtType != XmlPullParser.END_DOCUMENT) {
				switch (evtType) {
				case XmlPullParser.START_TAG:
					if (xpp.getName().equals("channel")) {
						channel = new ChannelItem();
						properties = new ArrayList<String>();
						channel.setProperties(properties);
						channel.setName(xpp.getAttributeValue(0));
						channels.add(channel);
					}
					if (xpp.getName().equals("item")) {
						String name = xpp.getAttributeValue(0);
						if (xpp.nextText().equals("true")) {
							properties.add(name);
						}
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				default:
					break;
				}
				// 获得下一个节点的信息
				evtType = xpp.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initEpgController() {
		mEpgController = EpgController.init(this);
		mEpgController.registerEpgInitListener(this);
		LoginInfo mInfo = CNTVApplication.getInstance().getLoginInfo();
		mEpgController.initEpgModule(mInfo.getEpgServer(),
				mInfo.getSearchServer(), mInfo.getTemplateID());
		mRecommendController = RecommendController.init(this);
		mRecommendController.registerListener(this);
		mRecommendController.requestPanelLayout(mInfo.getEpgServer(),
				ParameterConstant.REQUESTID_RECOMMEND);
		mRecommendController.requestPanelBoxData(mInfo.getEpgServer(),
				ParameterConstant.REQUESTID_RECOMMEND);
	}

	@Override
	protected void onStop() {
		if (mFreshHandler != null) {
			mFreshHandler.removeMessages(WindowMessageID.REFLESH_TIME);
			mFreshHandler = null;
		}

		super.onStop();
	}

	@Override
	protected void onDestroy() {
		mEpgController.unregisterEpgInitListener(this);
		// 数据统计：退出应用（关机）
		LogController.getInstance(this).logUpload(ParameterConstant.ONOROFF,
				ParameterConstant.ONOROFF_OFF + "", TAG);
		super.onDestroy();
	}

	/**
	 * UI初始化
	 */
	private void initUI() {
		rootLayout = (RelativeLayout) findViewById(R.id.rl_main);
		iv_wifi = (ImageView) findViewById(R.id.iv_wifi_main);
		iv_setting = (ImageView) findViewById(R.id.iv_setting_main);
		iv_search = (ImageView) findViewById(R.id.iv_search_main);

		rl_alive = (RelativeLayout) findViewById(R.id.rl_alive);
		tv_alive = (TextView) findViewById(R.id.tv_alive);
		tv_alive_en = (TextView) findViewById(R.id.tv_alive_en);
		rl_vod = (RelativeLayout) findViewById(R.id.rl_vod);
		tv_vod = (TextView) findViewById(R.id.tv_vod);
		tv_vod_en = (TextView) findViewById(R.id.tv_vod_en);
		rl_history = (RelativeLayout) findViewById(R.id.rl_history);
		tv_history = (TextView) findViewById(R.id.tv_history);
		tv_history_en = (TextView) findViewById(R.id.tv_history_en);
		rl_collect = (RelativeLayout) findViewById(R.id.rl_collect);
		tv_collect = (TextView) findViewById(R.id.tv_collect);
		tv_collect_en = (TextView) findViewById(R.id.tv_collect_en);

		tv_alive.getPaint().setFakeBoldText(true);
		tv_alive_en.getPaint().setFakeBoldText(true);
		tv_vod.getPaint().setFakeBoldText(true);
		tv_vod_en.getPaint().setFakeBoldText(true);
		tv_history.getPaint().setFakeBoldText(true);
		tv_history_en.getPaint().setFakeBoldText(true);
		tv_collect.getPaint().setFakeBoldText(true);
		tv_collect_en.getPaint().setFakeBoldText(true);

		fl_loading = (FrameLayout) findViewById(R.id.container_buffering);
	}

	// 按信号强弱刷新wifi图片显示
	private FreshHandler mFreshHandler = new FreshHandler(this);

	private class FreshHandler extends Handler {
		private WeakReference<MainActivity> mWeak;

		public FreshHandler(MainActivity act) {
			super();
			this.mWeak = new WeakReference<MainActivity>(act);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			MainActivity act = mWeak.get();
			if (act == null)
				return;
			if (msg != null) {
				switch (msg.what) {
				case WindowMessageID.REFLESH_TIME:
					obtainWifiInfo();
					mFreshHandler.sendEmptyMessageDelayed(
							WindowMessageID.REFLESH_TIME, INTERVAL_WIFI);
					break;

				default:
					break;
				}

			}
		}
	}

	@Override
	protected void onResume() {
		if (mFreshHandler == null) {
			mFreshHandler = new FreshHandler(this);
		}

		// 刷新wifi信号强度
		obtainWifiInfo();
		mFreshHandler.sendEmptyMessageDelayed(WindowMessageID.REFLESH_TIME,
				INTERVAL_WIFI);

		super.onResume();
	}

	/**
	 * 获取并刷新wifi信号的强弱
	 * 
	 * @return
	 */
	private void obtainWifiInfo() {
		// Wifi的连接速度及信号强度
		wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		wifiInfo = wifiManager.getConnectionInfo();
		int strength = 0;

		if (wifiInfo.getBSSID() != null) {
			// 链接信号强度
			strength = WifiManager.calculateSignalLevel(wifiInfo.getRssi(),
					wifiLevel);
		}

		switch (strength) {
		case 1:
			iv_wifi.setImageResource(R.drawable.wifi3);
			break;
		case 2:
			iv_wifi.setImageResource(R.drawable.wifi2);
			break;
		case 3:
			iv_wifi.setImageResource(R.drawable.wifi1);
			break;
		case 4:
			iv_wifi.setImageResource(R.drawable.wifi);
			break;
		case 0:
			iv_wifi.setImageResource(R.drawable.wifi4);
			break;

		default:
			iv_wifi.setImageResource(R.drawable.wifi4);
			break;
		}
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;

		switch (v.getId()) {
		case R.id.rl_alive:
			intent = new Intent(MainActivity.this, LiveActivity.class);
			break;
		case R.id.rl_vod:
			intent = new Intent(MainActivity.this, DramaListActivity.class);
			break;
		case R.id.rl_history:
			intent = new Intent(MainActivity.this, HistoryActivity.class);
			break;
		case R.id.rl_collect:
			intent = new Intent(MainActivity.this, CollectActivity.class);
			break;
		case R.id.iv_search_main:
			intent = new Intent(MainActivity.this, SearchActivity.class);
			break;
		case R.id.iv_setting_main:
			intent = new Intent(Settings.ACTION_SETTINGS);
			break;
		default:
			break;
		}
		if (intent != null) {
			startActivity(intent);
		}
	}

	@Override
	public void onEpgInited(boolean isSucceed) {
		Log.i(TAG, "--onEpgInited---" + isSucceed);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (!iv_search.isFocusable()) {
			iv_search.setFocusable(true);
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onFocusChange(View view, boolean hasFcus) {
		// 必须先设未选中字体颜色和不加粗（尽管xml布局里已设过），
		// 如果不设置，则会在获取焦点一次之后全部变为选中状态的设置
		tv_alive.setTextColor(getResources().getColor(R.color.text_bottom_main));
		tv_alive_en.setTextColor(getResources().getColor(
				R.color.text_bottom_main));

		tv_vod.setTextColor(getResources().getColor(R.color.text_bottom_main));
		tv_vod_en.setTextColor(getResources()
				.getColor(R.color.text_bottom_main));

		tv_history.setTextColor(getResources().getColor(
				R.color.text_bottom_main));
		tv_history_en.setTextColor(getResources().getColor(
				R.color.text_bottom_main));

		tv_collect.setTextColor(getResources().getColor(
				R.color.text_bottom_main));
		tv_collect_en.setTextColor(getResources().getColor(
				R.color.text_bottom_main));

		if (hasFcus) {
			if (view.getId() == R.id.rl_alive) {
				tv_alive.setTextColor(Color.WHITE);
				tv_alive_en.setTextColor(Color.WHITE);
			}
			if (view.getId() == R.id.rl_vod) {
				tv_vod.setTextColor(Color.WHITE);
				tv_vod_en.setTextColor(Color.WHITE);
			}
			if (view.getId() == R.id.rl_history) {
				tv_history.setTextColor(Color.WHITE);
				tv_history_en.setTextColor(Color.WHITE);
			}
			if (view.getId() == R.id.rl_collect) {
				tv_collect.setTextColor(Color.WHITE);
				tv_collect_en.setTextColor(Color.WHITE);
			}
		}
	}

	private RecommendController mRecommendController;

	private void initRecommendFragment() {
		if (DEBUG) {
			Log.e(TAG, "-----initFragment-----");
		}
		mRecommendFragment = new RecommendPanelFragment();
		FragmentManager manager = getFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		// 移除可能存在的旧的实例
		Fragment fragment = manager.findFragmentByTag(RECOM_FRAGMENT_TAG);
		if (fragment != null) {
			transaction.remove(fragment);
		}

		transaction.add(R.id.frame_recommendview, mRecommendFragment,
				RECOM_FRAGMENT_TAG);

		transaction.commit();

		mRecommendFragment.setOnBoxClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					RecomBoxView box = (RecomBoxView) v;

					// 数据统计：从首页的某个推荐位进入下一页面
					LogController.getInstance(MainActivity.this).logUpload(
							ParameterConstant.HOME,
							ParameterConstant.HOME_ENTER_OTHERS_FROM_RECOMMEND
									+ "," + box.getmRecommendItem().getId(),
							TAG);

					Intent intent = new Intent(MainActivity.this,
							DetailDramaActivity.class);
					Bundle bundle = new Bundle();
					bundle.putLong(ParameterConstant.SERISID, box
							.getRecomBoxData().getDetailId());
					intent.putExtras(bundle);
					startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(
							MainActivity.this,
							v.getTag()
									+ " "
									+ getResources().getString(
											R.string.tip_parse_error),
							Toast.LENGTH_SHORT).show();
				}

			}
		});

	}

	@Override
	public void onPanelLayoutResult(ArrayList<RecomPanel> panelList) {
		if (mRecommendFragment != null && panelList != null
				&& panelList.size() > 0)
			mRecommendFragment.updatePanel(panelList.get(0), 6);
	}

	@Override
	public void onPanelDataResult(HashMap<String, RecomBoxData> dataMap) {
		if (mRecommendFragment != null && dataMap != null)
			mRecommendFragment.updateBoxData(dataMap);
		if (fl_loading != null) {
			fl_loading.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (jumpToLive)
			hasOperation = true;

		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				showExitDialog();
				return true;
			}

			View curr = rootLayout.findFocus();
			if (curr != null) {
				if (curr.getId() == R.id.iv_search_main) {
					oldFocusView = iv_search;
				}

				if (curr.getId() == R.id.iv_setting_main) {
					oldFocusView = iv_setting;
				}
				if (curr instanceof RecomBoxView) {
					if (oldFocusView != null
							&& ((RecomBoxView) curr).getmRecommendItem()
									.getTopPosition() == 1) {
						if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
							oldFocusView.requestFocus();
							return true;
						}
					} else {
						return super.dispatchKeyEvent(event);
					}
				}
			}
		}

		return super.dispatchKeyEvent(event);
	}

	/**
	 * 退出应用dialog
	 */
	private void showExitDialog() {
		ExitDialog mExitDialog = new ExitDialog(MainActivity.this);
		mExitDialog.setMessage(getResources().getString(R.string.msg_exit_app));
		mExitDialog.setCancle(getResources().getString(R.string.text_negative));
		mExitDialog
				.setConfirm(getResources().getString(R.string.text_positive));
		mExitDialog.setPositiveButton(new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				MainActivity.this.finish();
				System.exit(0);
			}
		}).setNegativeButton(new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		mExitDialog.show();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		System.exit(0);
	}
}
