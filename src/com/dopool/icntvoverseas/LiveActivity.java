package com.dopool.icntvoverseas;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import tv.icntv.been.IcntvPlayerInfo;
import tv.icntv.icntvplayersdk.IcntvLive;
import tv.icntv.icntvplayersdk.iICntvPlayInterface;
import tv.icntv.tvassistcommon.LogSDKManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.dopool.icntvoverseas.adapter.LVChannelAdapter;
import com.dopool.icntvoverseas.adapter.LVProgramAdapter;
import com.dopool.icntvoverseas.app.CNTVApplication;
import com.dopool.icntvoverseas.listener.HomeKeyListener;
import com.dopool.icntvoverseas.listener.HomeKeyListener.OnHomeKeyPressListener;
import com.dopool.icntvoverseas.model.ParameterConstant;
import com.dopool.icntvoverseas.view.ExitDialog;

import dopool.controller.LiveEpgController;
import dopool.controller.LiveEpgController.LiveEpgInitListener;
import dopool.controller.LiveEpgController.LiveEpgOnlineChanListListener;
import dopool.controller.LiveEpgController.LiveEpgProgBillListener;
import dopool.liveepg.bean.LiveEpgChannel;
import dopool.liveepg.bean.ProgramBill;

public class LiveActivity extends BaseActivity implements LiveEpgInitListener,
		LiveEpgOnlineChanListListener, LiveEpgProgBillListener {
	public static final String TYPEID = "dny";
	private static final String TAG = LiveActivity.class.getSimpleName();
	private Context mContext = LiveActivity.this;
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHH");

	private IcntvLive mIcntvPlayer;
	private IcntvPlayerInfo mIcntvPlayerInfo;
	private FrameLayout mFrameLayout;
	private FrameLayout container_live;
	private FrameLayout fl_buffering;
	private Thread mLogSDKThread;

	private ListView lv_channel, lv_program;
	private LVChannelAdapter mChannelAdapter;
	private LVProgramAdapter mProgramAdapter;
	private ArrayList<LiveEpgChannel> channelList;
	private ArrayList<ProgramBill> programList;

	private boolean isShow = true;
	private int currentPos = 0;
	private ExitDialog mExitDialog;

	private HomeKeyListener homeKeyListener = new HomeKeyListener(this);;
	private LiveEpgController liveEpgController;
	iICntvPlayInterface callback = new iICntvPlayInterface() {
		// 需要播放的视频加载结束后调用
		public void onPrepared() {
			if (fl_buffering != null) {
				fl_buffering.setVisibility(View.INVISIBLE);
			}
		}

		// 需要播放的视频播放结束调用
		public void onCompletion() {
		}

		// 播放器错误调用
		public void onError(int what, int extra, String msg) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					if (!mExitDialog.isShowing()) {
						showErrorDialog(mContext.getResources().getString(
								R.string.msg_dialog_live_error));
					}
				}
			});
		}

		// 播放器开始缓冲时调用
		public void onBufferStart(String typeString) {
			if (fl_buffering != null
					&& fl_buffering.getVisibility() != View.VISIBLE) {
				fl_buffering.setVisibility(View.VISIBLE);
			}
		}

		// 播放器结束缓冲时调用
		public void onBufferEnd(String typeString) {
			if (fl_buffering != null) {
				fl_buffering.setVisibility(View.INVISIBLE);
			}
		}

		// 开始任何一个广告或视频时到1分钟之后没有加载完成调用
		public void onTimeout() {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					if (!mExitDialog.isShowing()) {
						showErrorDialog(mContext.getResources().getString(
								R.string.msg_dialog_live_error));
					}
				}
			});
		}

	};

	private boolean firstPlay = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_live);
		firstPlay = true;

		initSDKLOG();
		initUI();
		initData();

		this.mFrameLayout = ((FrameLayout) findViewById(R.id.fra_player));// 播放器的载体
		delayedHide(AUTO_HIDE_DELAY_MILLIS);
	}

	private void play(int position) {

		if (mIcntvPlayer != null) {
			mIcntvPlayer.release();
		}

		if (firstPlay) {
			firstPlay = false;
		}
		LiveEpgChannel channel = channelList.get(position);
		String dourl = channel.getCdnaddress();
		if (mIcntvPlayerInfo == null) {
			// FIXME 因未知直播必须传那些参数，哪些从loginInfo中获取，哪些是固定值,所以部分参数暂时写死
			mIcntvPlayerInfo = new IcntvPlayerInfo();
			mIcntvPlayerInfo.setProgramID(dourl);// 节目id,播控平台取值，直播中这个字段没有实际用处，建议先把地址传进去，以作备用，也可不传
			mIcntvPlayerInfo.setProgramListID(channel.getChannelid());// 节目集id，播控平台取值（日志、广告使用）
			mIcntvPlayerInfo.setDeviceID(CNTVApplication.getInstance()
					.getLoginInfo().getDeviceId());// 设备id 验证模块取值
			mIcntvPlayerInfo.setPlatformId(CNTVApplication.getInstance()
					.getLoginInfo().getPlatformId());// 平台id
			// mIcntvPlayerInfo.setColumnId(TYPEID);// 设置栏目id（广告使用）
			mIcntvPlayerInfo.setDuration("");// 设置视频时长，直播没有时长，可不传（广告使用）
			mIcntvPlayerInfo.setDeviceMac(CNTVApplication.getInstance()
					.getMac());// mac 验证模块取值
			mIcntvPlayerInfo.setPrice(5.0f);// 设置价格（日志使用）
			mIcntvPlayerInfo.setResolution("HD");// 设置清晰度（日志使用）
			mIcntvPlayerInfo.setPlayType("1");
		}
		mIcntvPlayerInfo.setPlayUrl(dourl);// 播控平台，直播播放地址
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				mIcntvPlayer = new IcntvLive(LiveActivity.this, mFrameLayout,
						mIcntvPlayerInfo, callback);
				mIcntvPlayer.openMediaPlayer();
			}
		}, 1000);
	}

	private void initSDKLOG() {
		if (mLogSDKThread != null) {
			mLogSDKThread.interrupt();
			mLogSDKThread = null;
		}
		mLogSDKThread = new Thread(new Runnable() {

			@Override
			public void run() {
				/**
				 * sdkInit
				 * 
				 * @param path
				 *            放置配置文件的ini目录的路径
				 * @param icntvID
				 *            设备号
				 * @param platformID
				 *            平台号
				 * @param templateId
				 *            模板ID
				 * @param dataSource
				 *            目前固定为4
				 * @param fSource
				 *            目前固定为6
				 */
				LogSDKManager.getInstance().sdkInit(
						getFilesDir().getAbsolutePath(),
						CNTVApplication.getInstance().getLoginInfo()
								.getDeviceId(),
						CNTVApplication.getInstance().getLoginInfo()
								.getPlatformId(),
						CNTVApplication.getInstance().getLoginInfo()
								.getTemplateID(), ParameterConstant.DATASOURCE,
						ParameterConstant.FSOURCE);
			}
		});
		mLogSDKThread.start();
	}

	private void initUI() {
		container_live = (FrameLayout) findViewById(R.id.container_live);
		lv_channel = (ListView) findViewById(R.id.lv_channel_alive);
		lv_program = (ListView) findViewById(R.id.lv_program_alive);
		fl_buffering = (FrameLayout) findViewById(R.id.container_buffering);
	}

	private Handler mHandler = new Handler();

	private void initData() {

		liveEpgController = LiveEpgController.init(LiveActivity.this);
		liveEpgController.registLiveEpgInitListener(this);
		liveEpgController.registLiveEpgOnlineChanListListener(this);
		liveEpgController.registLiveEpgProgBillListener(this);
		liveEpgController.initLiveEpg("");

		lv_channel.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					final int position, long id) {
				if (firstPlay && position == 0) {
					play(position);
					String str = formatter.format(new Date());
					liveEpgController.getProgBill(channelList.get(position)
							.getChannelid(),
							str.substring(0, str.length() - 2), str,
							channelList.get(position).getChannelname());
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		lv_channel.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (fl_buffering != null
						&& fl_buffering.getVisibility() != View.VISIBLE) {
					fl_buffering.setVisibility(View.VISIBLE);
				}
				currentPos = position;
				play(position);
				String str = formatter.format(new Date());
				liveEpgController.getProgBill(channelList.get(position)
						.getChannelid(), str.substring(0, str.length() - 2),
						str, channelList.get(position).getChannelname());
			}
		});
		lv_channel.requestFocus();

		homeKeyListener.setOnHomeKeyPressListener(new OnHomeKeyPressListener() {

			@Override
			public void onHomeKeyPress() {
				if (mIcntvPlayer != null) {
					mIcntvPlayer.release();
				}
				LiveActivity.this.finish();
			}
		});

		mExitDialog = new ExitDialog(mContext);

	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onPause() {
		super.onPause();
		homeKeyListener.stop();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		homeKeyListener.start();
	}

	@Override
	protected void onDestroy() {
		// 结束播放器
		if (mIcntvPlayer != null) {
			mIcntvPlayer.release();
		}
		if (mHideUIRunnable != null) {
			mHideHandler.removeCallbacks(mHideUIRunnable);
		}
		mHideHandler = null;
		liveEpgController.unRegistLiveEpgInitListener();
		liveEpgController.unRegistLiveEpgOnlineChanListListener(this);
		liveEpgController.unRegistLiveEpgProgBillListener(this);
		super.onDestroy();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			delayedHide(AUTO_HIDE_DELAY_MILLIS);

			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				showExitDialog();
				return true;
			}

			if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT
					|| event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
				if (!isShow) {
					showLayer();
					delayedHide(AUTO_HIDE_DELAY_MILLIS);
				}
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}

	/**
	 * 定义自动隐藏的时间 If {@link #AUTO_HIDE} is set, the number of milliseconds to
	 * wait after user interaction before hiding the system UI.
	 */
	public static final int AUTO_HIDE_DELAY_MILLIS = 5000;

	/**
	 * 用于隐藏界面的runnable
	 */
	static HideUiRunnable mHideUIRunnable;

	private static class HideUiRunnable implements Runnable {
		WeakReference<LiveActivity> mRef;

		HideUiRunnable(LiveActivity act) {
			mRef = new WeakReference<LiveActivity>(act);
		}

		@Override
		public void run() {
			LiveActivity act = mRef.get();
			if (act == null)
				return;
			act.hideLayer();
		}
	}

	private void showLayer() {
		container_live.setVisibility(View.VISIBLE);
		isShow = true;
		if (!lv_channel.isFocused()) {
			lv_channel.requestFocus();
		}
		updateProgBillStatus();
	}

	private void hideLayer() {
		container_live.setVisibility(View.GONE);
		isShow = false;
	}

	private Handler mHideHandler = new Handler();

	/**
	 * 设置界面隐藏
	 * 
	 * @param delayMillis
	 *            多少秒后隐藏 Schedules a call to hide() in [delay] milliseconds,
	 *            canceling any previously scheduled calls.
	 */
	public void delayedHide(int millis) {
		if (mHideHandler == null)
			return;
		if (mHideUIRunnable != null) {
			mHideHandler.removeCallbacks(mHideUIRunnable);
		}

		if (isShow) {
			mHideUIRunnable = new HideUiRunnable(this);
			mHideHandler.postDelayed(mHideUIRunnable, millis);
		}
	}

	/**
	 * 退出直播dialog
	 */
	private void showExitDialog() {
		// 防止activity泄露
		if (isFinishing()) {
			return;
		}
		mExitDialog.setMessage(getResources().getString(
				R.string.msg_dialog_exit));
		mExitDialog.setCancle(getResources().getString(R.string.text_negative));
		mExitDialog
				.setConfirm(getResources().getString(R.string.text_positive));
		mExitDialog.setPositiveButton(new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				LiveActivity.this.finish();
			}
		}).setNegativeButton(new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		mExitDialog.show();
	}

	/**
	 * 播放出错dialog
	 */
	private void showErrorDialog(String errorMsg) {
		// 防止activity泄露
		if (isFinishing()) {
			return;
		}
		mExitDialog.setMessage(errorMsg);
		mExitDialog.setCancle(getResources().getString(R.string.text_exit));
		mExitDialog.setConfirm(getResources().getString(
				R.string.text_retry_live));
		mExitDialog.setPositiveButton(new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				if (currentPos == channelList.size() - 1) {
					currentPos = 0;
				} else {
					currentPos += 1;
				}
				play(currentPos);
				String str = formatter.format(new Date());
				liveEpgController.getProgBill(channelList.get(currentPos)
						.getChannelid(), str.substring(0, str.length() - 2),
						str, channelList.get(currentPos).getChannelname());
				lv_channel.setSelection(currentPos);
			}
		}).setNegativeButton(new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				LiveActivity.this.finish();
			}
		});
		mExitDialog.show();
	}

	@Override
	public void onLiveEpgOnlineChanListResult(
			ArrayList<LiveEpgChannel> liveEpgChans, boolean isSuccess) {
		if (isSuccess) {
			if (liveEpgChans != null && liveEpgChans.size() > 0) {
				channelList = liveEpgChans;
				mChannelAdapter = new LVChannelAdapter(LiveActivity.this,
						channelList);
				lv_channel.setAdapter(mChannelAdapter);
				lv_channel.requestFocus();
			}
		}
	}

	@Override
	public void onLiveEpgInitResult(boolean isInit) {
		if (isInit) {
			liveEpgController.getOnlineChanList(TYPEID, "", TAG);
		}
	}

	@Override
	public void onLiveEpgProgBillResult(ArrayList<ProgramBill> programBills,
			boolean isSuccess, String tag) {
		if (isSuccess) {
			if (tag.equals(channelList
					.get(lv_channel.getSelectedItemPosition()).getChannelname())) {
				if (programBills != null && programBills.size() > 0) {
					programList = programBills;
					mProgramAdapter = new LVProgramAdapter(LiveActivity.this,
							programList);
					lv_program.setAdapter(mProgramAdapter);
					updateProgBillStatus();
				} else {
					programList = new ArrayList<ProgramBill>();
					mProgramAdapter = new LVProgramAdapter(LiveActivity.this,
							programList);
					lv_program.setAdapter(mProgramAdapter);
				}

			}
		}
	}

	private void updateProgBillStatus() {
		int index = 0;
		SimpleDateFormat format = new SimpleDateFormat("HH:mm");
		String currentTime = format.format(new Date());
		// TODO: 二分法查找当前播放哪个节目
		long time = 0;
		try {
			time = format.parse(currentTime).getTime();
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		long startTime = 0;
		long endTime = 0;
		for (int i = 0; i < programList.size(); i++) {
			try {
				startTime = format.parse(programList.get(i).getStarttime())
						.getTime();
				endTime = format.parse(programList.get(i).getEndtime())
						.getTime();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			if (time < endTime && time > startTime) {
				index = i;
				break;
			}
		}
		if (index > 0) {
			// lv_program.setSelectionFromTop(index-2, 0);
			ArrayList<ProgramBill> programListCopy = new ArrayList<ProgramBill>();
			for (int i = index - 1; i < programList.size(); i++) {
				programListCopy.add(programList.get(i));
			}
			((LVProgramAdapter) lv_program.getAdapter())
					.setData(programListCopy);
			index = 1;
		}
		((LVProgramAdapter) lv_program.getAdapter())
				.setCurrentPlayPosititon(index);
		mProgramAdapter.notifyDataSetChanged();
	}

}
