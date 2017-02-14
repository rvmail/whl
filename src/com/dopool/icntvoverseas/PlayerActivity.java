package com.dopool.icntvoverseas;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import tv.icntv.been.IcntvPlayerInfo;
import tv.icntv.icntvplayersdk.IcntvPlayer;
import tv.icntv.icntvplayersdk.iICntvPlayInterface;
import tv.icntv.tvassistcommon.LogSDKManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dopool.icntvoverseas.adapter.PlaySerisAdapter;
import com.dopool.icntvoverseas.app.CNTVApplication;
import com.dopool.icntvoverseas.listener.HomeKeyListener;
import com.dopool.icntvoverseas.listener.HomeKeyListener.OnHomeKeyPressListener;
import com.dopool.icntvoverseas.model.ParameterConstant;
import com.dopool.icntvoverseas.model.WindowMessageID;
import com.dopool.icntvoverseas.utils.Utils;
import com.dopool.icntvoverseas.view.ExitDialog;
import com.dopool.icntvoverseas.view.focusrecyclerview.FocusRecyclerView;
import com.dopool.icntvoverseas.view.focusrecyclerview.SeriesRecyclerView;
import com.dopool.icntvoverseas.view.focusrecyclerview.WrapContentLinearLayoutManager;

import dopool.cntv.base.EpgMovie;
import dopool.cntv.base.MovieItem;
import dopool.controller.LogController;

public class PlayerActivity extends BaseActivity {
	private String TAG = PlayerActivity.class.getSimpleName();
	private IcntvPlayer mIcntvPlayer;
	private IcntvPlayerInfo mIcntvPlayerInfo;
	private FrameLayout mFrameLayout;
	private ImageView btn_pause;
	public SeekBar mSeekBar;
	private TextView tv_title, tv_current, tv_total;
	private String mCurrentTime, mTotalTime;
	private RelativeLayout rlLayout;
	public SeriesRecyclerView rv_series;
	private LinearLayoutManager mLayoutManager;
	private PlaySerisAdapter mAdapter;
	private List<MovieItem> mList = new ArrayList<MovieItem>();
	private FrameLayout fl_buffering;

	private FreshHandler mFreshHandler;
	private EpgMovie mItem;
	private MovieItem movieItem;
	private int movieLength;
	private String title, actionUrl;
	private int pos = -1;
	private Context mContext = PlayerActivity.this;
	private RelativeLayout rootLayout;

	private boolean isShow;
	private static final boolean DEBUG = true;

	private HomeKeyListener homeKeyListener = new HomeKeyListener(this);
	// 获取video总时长的delay
	private final int DELAY_OBTAIN_PLAYINFO = 500;
	// 快进快退的间隔为30s
	private final int VALUE_SEEK = 30 * 1000;

	// 播放完毕退出时的delay（显示toast）
	private final int DELAY_PLAY_COMPLETE = 1000;

	private Bundle mBundle;
	private boolean isPaused;

	private long time_enter_player = 0; // 点播开始时间(从进入该activity时算起)
	private long time_start_play = 0; // 开始正常播放的时间
	private int video_resolution = 0; // 片源清晰度
	private boolean isSeekComplete = false;
	iICntvPlayInterface callback = new iICntvPlayInterface() {
		// 需要播放的视频加载结束后调用
		public void onPrepared() {
			if (fl_buffering != null) {
				fl_buffering.setVisibility(View.INVISIBLE);
			}

			if (mIcntvPlayer != null) {
				mHandler.postDelayed(new Runnable() {

					@Override
					public void run() {
						mTotalTime = Utils.toTime(mIcntvPlayer.getDuration());
						tv_total.setText(mTotalTime);
						mSeekBar.setMax(mIcntvPlayer.getDuration());

						// 数据统计：开始正常播放视频
						time_start_play = System.currentTimeMillis();
						LogController
								.getInstance(PlayerActivity.this)
								.logUpload(
										ParameterConstant.PLAYER_ACTIVITY,
										ParameterConstant.PLAY_NORMALLY_START
												+ ","
												+ mList.get(pos).getId()
												+ ","
												+ mList.get(pos).getProgramId()
												+ ","
												+ ParameterConstant.CHARGE_TYPE_FREE
												+ ","
												+ video_resolution
												+ ","
												+ movieLength
												+ ","
												+ mIcntvPlayer
														.getCurrentPosition()
												+ ","
												+ (time_start_play - time_enter_player),
										TAG);
					}
				}, DELAY_OBTAIN_PLAYINFO);
				mFreshHandler.sendEmptyMessageDelayed(
						WindowMessageID.REFLESH_TIME, DELAY_OBTAIN_PLAYINFO);
			}
		}

		// 需要播放的视频播放结束调用
		public void onCompletion() {
			// 数据统计：点播结束(endType为0)
			LogController.getInstance(PlayerActivity.this).logUpload(
					ParameterConstant.PLAYER_ACTIVITY,
					ParameterConstant.PLAY_COMPLETE + ","
							+ mList.get(pos).getId() + ","
							+ mList.get(pos).getProgramId() + ","
							+ ParameterConstant.CHARGE_TYPE_FREE + ","
							+ video_resolution + "," + movieLength + ","
							// 正常播放完毕，当前时间为视频真实时长
							+ mIcntvPlayer.getDuration() + ","
							+ ParameterConstant.ENDTYPE_NORMAL, TAG);
			showNextDialog();
		}

		// 播放器错误调用
		public void onError(int what, int extra, String msg) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					showErrorDialog(mContext.getResources().getString(
							R.string.msg_dialog_error));
				}
			});
		}

		// 播放器开始缓冲时调用
		public void onBufferStart(String typeString) {
			if (fl_buffering != null
					&& fl_buffering.getVisibility() != View.VISIBLE) {
				fl_buffering.setVisibility(View.VISIBLE);

				if (isShow) {
					mHandler.post(new Runnable() {

						@Override
						public void run() {
							hideLayer();
						}
					});
				}
			}

			if (mList == null || pos < 0) {
				return;
			}

			if (typeString
					.equals(iICntvPlayInterface.ON_BUFFER_START_TYPE_VIDEO)) {
				// 此处上报数据，获取影片时长有可能为0，故将此处逻辑挪到onPrepared方法中
			} else if (typeString
					.equals(iICntvPlayInterface.ON_BUFFER_START_TYPE_701_STATUS)) {
				// 数据统计：卡顿开始
				LogController.getInstance(PlayerActivity.this).logUpload(
						ParameterConstant.PLAYER_ACTIVITY,
						ParameterConstant.NOT_SMOOTH_START + ","
								+ mList.get(pos).getId() + ","
								+ mList.get(pos).getProgramId() + ","
								+ ParameterConstant.CHARGE_TYPE_FREE + ","
								+ video_resolution + "," + movieLength + ","
								+ mIcntvPlayer.getCurrentPosition(), TAG);
			}
		}

		// 播放器结束缓冲时调用
		public void onBufferEnd(final String typeString) {
			if (fl_buffering != null) {
				fl_buffering.setVisibility(View.INVISIBLE);
			}

			if (isPaused) {
				mIcntvPlayer.start();
				isPaused = false;
				btn_pause.setVisibility(View.GONE);

				if (isShow) {
					delayedHide(DELAY_PLAY_COMPLETE);
				}
			}

			// 加延时是为防止onBufferEnd在按键up之前回调而造成数据上报的遗漏
			mHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					if (typeString
							.equals(iICntvPlayInterface.ON_BUFFER_END_TYPE_ONSEEKCOMPLETE)
							&& isSeekComplete) {
						// 数据统计：seek结束
						LogController
								.getInstance(PlayerActivity.this)
								.logUpload(
										ParameterConstant.PLAYER_ACTIVITY,
										ParameterConstant.SEEK_COMPLETE
												+ ","
												+ mList.get(pos).getId()
												+ ","
												+ mList.get(pos).getProgramId()
												+ ","
												+ ParameterConstant.CHARGE_TYPE_FREE
												+ ","
												+ video_resolution
												+ ","
												+ movieLength
												+ ","
												+ mIcntvPlayer
														.getCurrentPosition(),
										TAG);
						isSeekComplete = false;
					}
				}
			}, DELAY_OBTAIN_PLAYINFO);

			if (typeString
					.equals(iICntvPlayInterface.ON_BUFFER_END_TYPE_702_STATUS)) {
				// 数据统计：卡顿结束
				LogController.getInstance(PlayerActivity.this).logUpload(
						ParameterConstant.PLAYER_ACTIVITY,
						ParameterConstant.NOT_SMOOTH_COMPLETE + ","
								+ mList.get(pos).getId() + ","
								+ mList.get(pos).getProgramId() + ","
								+ ParameterConstant.CHARGE_TYPE_FREE + ","
								+ video_resolution + "," + movieLength + ","
								+ mIcntvPlayer.getCurrentPosition(), TAG);
			}
		}

		// 开始任何一个广告或视频时到1分钟之后没有加载完成调用
		public void onTimeout() {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					showErrorDialog(mContext.getResources().getString(
							R.string.msg_dialog_timeout));
				}
			});
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play);
		Log.i(TAG, "---PlayerActivity---onCreate---" + this);
		mBundle = getIntent().getExtras();
		initExtraData(mBundle);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.i(TAG, "---PlayerActivity---onNewIntent---" + this);
		setIntent(intent);
		// 统一换台前，release旧播放器
		if (mIcntvPlayer != null) {
			mIcntvPlayer.release();
		}

		mBundle = intent.getExtras();
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				initExtraData(mBundle);
			}
		}, 1000);
	}

	private void initExtraData(Bundle savedInstance) {
		initSDKLOG();
		initUI();
		initData(savedInstance);

		this.mIcntvPlayerInfo = new IcntvPlayerInfo();
		long id = mItem.getId();

		this.mIcntvPlayerInfo.setPlayUrl(actionUrl);// 点播播放地址
		this.mIcntvPlayerInfo.setProgramID(movieItem.getId() + "");// 节目id
																	// 播控平台取值
		this.mIcntvPlayerInfo.setProgramListID(id + "");// 节目集id 播控平台取值（广告使用）
		this.mIcntvPlayerInfo.setDeviceID(CNTVApplication.getInstance()
				.getLoginInfo().getDeviceId());// 设备id 验证模块取值
		this.mIcntvPlayerInfo.setPlatformId(CNTVApplication.getInstance()
				.getLoginInfo().getPlatformId());// 平台ID 验证模块取值（广告使用）
		this.mIcntvPlayerInfo.setColumnId(mItem.getCatgId());// 设置栏目id（广告使用）
		this.mIcntvPlayerInfo.setDuration(movieLength + "");// 视频时长（广告使用）
		this.mIcntvPlayerInfo.setDeviceMac(CNTVApplication.getInstance()
				.getMac());// mac 验证模块取值
		this.mIcntvPlayer = new IcntvPlayer(this, mFrameLayout,
				this.mIcntvPlayerInfo, this.callback);
		this.mIcntvPlayer.openMediaPlayer();

		mFreshHandler = new FreshHandler(mIcntvPlayer);
		homeKeyListener.setOnHomeKeyPressListener(new OnHomeKeyPressListener() {

			@Override
			public void onHomeKeyPress() {
				if (mIcntvPlayer.isPlaying()) {
					mIcntvPlayer.stop();
				}
				mIcntvPlayer.release();
				returnToDetail();
			}
		});
	}

	private Thread mLogSDKThread;

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
		rootLayout = (RelativeLayout) findViewById(R.id.upper_layer);
		rootLayout.getViewTreeObserver().addOnGlobalFocusChangeListener(
				new ViewTreeObserver.OnGlobalFocusChangeListener() {

					@Override
					public void onGlobalFocusChanged(View oldFocus,
							View newFocus) {
						if (newFocus instanceof SeriesRecyclerView) {
							if (isShow
									&& rv_series.getVisibility() == View.VISIBLE) {

								if (rv_series.getSelection() >= 0
										&& rv_series.getSelection() < mList
												.size()) {
									rv_series.setItemSelected(rv_series
											.getSelection());
									rv_series.setSelection(
											0,
											rv_series.getSelection(),
											FocusRecyclerView.PLACEHOLDER_POSITION);

									if (mLayoutManager
											.findFirstVisibleItemPosition() > rv_series
											.getSelection()
											|| mLayoutManager
													.findLastVisibleItemPosition() < rv_series
													.getSelection()) {
										// 有时候滑动滞后于遥控操作，焦点就会跟不上
										mLayoutManager.scrollToPositionWithOffset(
												rv_series.getSelection(), 0);
									}
								} else {
									if (pos >= 0) {
										rv_series.setItemSelected(pos);
										rv_series
												.setSelection(
														0,
														pos,
														FocusRecyclerView.PLACEHOLDER_POSITION);
									}
								}

							}
						} else {
							if (isShow
									&& rv_series.getVisibility() == View.VISIBLE) {
								rv_series.setSelection(
										FocusRecyclerView.INVALIDATE_POSITION,
										rv_series.getSelection(),
										FocusRecyclerView.PLACEHOLDER_POSITION);
							}
						}
					}
				});
		this.mFrameLayout = ((FrameLayout) findViewById(R.id.fra_player));// 放播放器的载体
		btn_pause = (ImageView) findViewById(R.id.btn_pause);
		mSeekBar = (SeekBar) findViewById(R.id.player_seekbar_video);
		tv_title = (TextView) findViewById(R.id.text_title_play);
		tv_current = (TextView) findViewById(R.id.text_current_time);
		tv_total = (TextView) findViewById(R.id.text_total_time);
		rlLayout = (RelativeLayout) findViewById(R.id.container);
		rv_series = (SeriesRecyclerView) findViewById(R.id.recycler_series_play);
		fl_buffering = (FrameLayout) findViewById(R.id.container_buffering);
		initVolumeView();
	}

	private void initData(Bundle bundle) {
		if (bundle == null)
			return;
		time_enter_player = System.currentTimeMillis();

		mItem = (EpgMovie) bundle
				.getSerializable(ParameterConstant.OBJECT_SERIALIZABLE);
		pos = bundle.getInt(ParameterConstant.COUNT_SERIES);
		title = mItem.getName();
		// 分钟->秒->毫秒
		movieLength = Integer.parseInt(mItem.getLength()) * 60 * 1000;

		if (pos < 0) {
			pos = 0;
			movieItem = mItem.getSources().get(0);
			actionUrl = movieItem.getActionURL();
		} else {
			movieItem = mItem.getSources().get(pos);
			actionUrl = movieItem.getActionURL();
		}

		mLayoutManager = new WrapContentLinearLayoutManager(mContext,
				LinearLayoutManager.HORIZONTAL, false);
		scrollToWhich(pos);
		rv_series.setLayoutManager(mLayoutManager);
		mList = mItem.getSources();
		mAdapter = new PlaySerisAdapter(mContext, mList, pos);
		mAdapter.setReycycler(rv_series);
		rv_series.setAdapter(mAdapter);
		rv_series.setHasFixedSize(true);

		rv_series
				.setOnItemClickListener(new FocusRecyclerView.OnItemClickListener() {

					@Override
					public void onItemClicked(int position) {
						pos = position;
						showClickDialog();
					}
				});

		rv_series.setListener(new FocusRecyclerView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(int prePosition, int position,
					int changeLinePosition, FocusRecyclerView view) {
				if (prePosition == FocusRecyclerView.INVALIDATE_POSITION
						&& ((LinearLayoutManager) view.getLayoutManager())
								.findViewByPosition(position) != null) {
					((LinearLayoutManager) view.getLayoutManager())
							.findViewByPosition(position).setSelected(false);
					return;
				}
				if (prePosition != position
						&& ((LinearLayoutManager) view.getLayoutManager())
								.findViewByPosition(prePosition) != null) {
					((LinearLayoutManager) view.getLayoutManager())
							.findViewByPosition(prePosition).setSelected(false);
				}
				if (((LinearLayoutManager) view.getLayoutManager())
						.findViewByPosition(position) != null) {
					((LinearLayoutManager) view.getLayoutManager())
							.findViewByPosition(position).setSelected(true);
				}
			}
		});

		// 清晰度转换（只有数据统计才需要转换）
		if (mList.get(pos).getDefinition().equals("HD")) {
			video_resolution = ParameterConstant.RESOLUTION_HD;
		} else if (mList.get(pos).getDefinition().equals("SD")) {
			video_resolution = ParameterConstant.RESOLUTION_SD;
		}
		// 数据统计：点播开始
		LogController.getInstance(this).logUpload(
				ParameterConstant.PLAYER_ACTIVITY,
				ParameterConstant.PLAY_START + "," + mList.get(pos).getId()
						+ "," + mList.get(pos).getProgramId() + ","
						+ ParameterConstant.CHARGE_TYPE_FREE + ","
						+ video_resolution + "," + movieLength, TAG);

	}

	private void showLayer() {
		tv_title.setVisibility(View.VISIBLE);
		tv_title.setText(title);
		mSeekBar.setVisibility(View.VISIBLE);
		tv_current.setVisibility(View.VISIBLE);
		tv_total.setVisibility(View.VISIBLE);
		rlLayout.setVisibility(View.VISIBLE);
		rv_series.setVisibility(View.VISIBLE);
		rootLayout.requestChildFocus(mSeekBar, rootLayout.findFocus());
		mSeekBar.requestFocus();
		isShow = true;
		// 数据统计：调出选集页
		LogController.getInstance(PlayerActivity.this).logUpload(
				ParameterConstant.PLAYER_ACTIVITY,
				ParameterConstant.SHOW_SERIES_LAYER + ","
						+ mList.get(pos).getId() + ","
						+ mList.get(pos).getProgramId() + ","
						+ ParameterConstant.CHARGE_TYPE_FREE + ","
						+ video_resolution + "," + movieLength + ","
						+ mIcntvPlayer.getCurrentPosition(), TAG);
	}

	private void hideLayer() {
		tv_title.setVisibility(View.GONE);
		mSeekBar.setVisibility(View.GONE);
		tv_current.setVisibility(View.GONE);
		tv_total.setVisibility(View.GONE);
		rlLayout.setVisibility(View.GONE);
		rv_series.setVisibility(View.GONE);
		isShow = false;
		// 数据统计：隐藏选集页
		LogController.getInstance(PlayerActivity.this).logUpload(
				ParameterConstant.PLAYER_ACTIVITY,
				ParameterConstant.HIDE_SERIES_LAYER + ","
						+ mList.get(pos).getId() + ","
						+ mList.get(pos).getProgramId() + ","
						+ ParameterConstant.CHARGE_TYPE_FREE + ","
						+ video_resolution + "," + movieLength + ","
						+ mIcntvPlayer.getCurrentPosition(), TAG);
	}

	private void scrollToWhich(int current) {
		if (current > 7) {
			mLayoutManager.scrollToPositionWithOffset(current, 0);
			rv_series.setPosition(current);
		} else if (current <= 0) {
			mLayoutManager.scrollToPosition(0);
			rv_series.setPosition(0);
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			delayedHide(AUTO_HIDE_DELAY_MILLIS);

			if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
				if (fl_buffering != null
						&& fl_buffering.getVisibility() == View.VISIBLE) {
					return true;
				}

				if (mIcntvPlayer != null) {
					if (mIcntvPlayer.isPlaying()) {
						if (!isShow) {
							showLayer();
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
							// 显示UI的同时，调用seek
							if (rootLayout.findFocus() instanceof SeekBar) {
								seekBack(event.getRepeatCount());
								return true;
							}
						} else {
							if (rootLayout.findFocus() instanceof SeekBar) {
								seekBack(event.getRepeatCount());
								return true;
							} else {
								return rv_series.onKeyDown(event.getKeyCode(),
										event);
							}
						}
						return true;
					} else {
						// 暂停状态
						if (isShow) {
							if (rootLayout.findFocus() instanceof SeekBar) {
								seekBack(event.getRepeatCount());
								mIcntvPlayer.start();
								isPaused = false;
								btn_pause.setVisibility(View.GONE);

								return true;
							} else {
								return rv_series.onKeyDown(event.getKeyCode(),
										event);
							}
						}
					}
				}
			}

			if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
				if (fl_buffering != null
						&& fl_buffering.getVisibility() == View.VISIBLE) {
					return true;
				}

				if (mIcntvPlayer != null) {
					if (mIcntvPlayer.isPlaying()) {
						if (!isShow) {
							showLayer();
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
							// 显示UI的同时，调用seek
							if (rootLayout.findFocus() instanceof SeekBar) {
								seekForward(event.getRepeatCount());
								return true;
							}
						} else {
							if (rootLayout.findFocus() instanceof SeekBar) {
								seekForward(event.getRepeatCount());
								return true;
							} else {
								return rv_series.onKeyDown(event.getKeyCode(),
										event);
							}
						}
						return true;
					} else {
						// 暂停状态
						if (isShow) {
							if (rootLayout.findFocus() instanceof SeekBar) {
								seekForward(event.getRepeatCount());
								mIcntvPlayer.start();
								isPaused = false;
								btn_pause.setVisibility(View.GONE);
								return true;
							} else {
								return rv_series.onKeyDown(event.getKeyCode(),
										event);
							}
						}
					}
				}
			}

			if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
				if (fl_buffering != null
						&& fl_buffering.getVisibility() == View.VISIBLE) {
					return true;
				}

				// isPlaying 播放器是否正在播放视频
				// isADPlaying 播放器是否正在播放广告
				if (mIcntvPlayer.isPlaying()) {
					if (!isShow) {
						showLayer();
						delayedHide(AUTO_HIDE_DELAY_MILLIS);
					} else {
						rootLayout.requestChildFocus(mSeekBar,
								rootLayout.findFocus());
						mSeekBar.requestFocus();
					}
					return true;
				}
			}

			if ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
					|| (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER)) {
				if (fl_buffering != null
						&& fl_buffering.getVisibility() == View.VISIBLE) {
					return true;
				}

				if (!isShow) {
					pauseOrResume();
					return true;
				} else {
					if (rootLayout.findFocus() instanceof SeekBar) {
						pauseOrResume();
						return true;
					} else {
						return rv_series.onKeyDown(event.getKeyCode(), event);
					}
				}
			}

			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				showExitDialog();
				return true;
			}

			if (KeyEvent.KEYCODE_VOLUME_UP == event.getKeyCode()) {
				if (fl_buffering != null
						&& fl_buffering.getVisibility() == View.VISIBLE) {
					return true;
				}

				upVolume(getVolume());
				return true;
			} else if (KeyEvent.KEYCODE_VOLUME_DOWN == event.getKeyCode()) {
				if (fl_buffering != null
						&& fl_buffering.getVisibility() == View.VISIBLE) {
					return true;
				}

				downVolume(getVolume());
				return true;
			} else if (KeyEvent.KEYCODE_MUTE == event.getKeyCode()) {
				if (fl_buffering != null
						&& fl_buffering.getVisibility() == View.VISIBLE) {
					return true;
				}

				mute(getVolume());
				return true;
			}
		} else if (event.getAction() == KeyEvent.ACTION_UP) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT
					|| event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
				if (isShow && rootLayout.findFocus() instanceof SeekBar) {
					isSeekComplete = true;
				} else {
					isSeekComplete = false;
				}
			}
		}
		return super.dispatchKeyEvent(event);
	}

	/**
	 * 暂停或继续播放
	 */
	private void pauseOrResume() {
		if (mIcntvPlayer != null && !mIcntvPlayer.isADPlaying()) {
			if (mIcntvPlayer.isPlaying()) {
				mIcntvPlayer.pause();
				isPaused = true;
				btn_pause.setBackgroundResource(R.drawable.pause);
				btn_pause.setVisibility(View.VISIBLE);
				// 数据统计：暂停开始
				LogController.getInstance(this).logUpload(
						ParameterConstant.PLAYER_ACTIVITY,
						ParameterConstant.PAUSE_START + ","
								+ mList.get(pos).getId() + ","
								+ mList.get(pos).getProgramId() + ","
								+ ParameterConstant.CHARGE_TYPE_FREE + ","
								+ video_resolution + "," + movieLength + ","
								+ mIcntvPlayer.getCurrentPosition(), TAG);

				if (isShow) {
					if (mHideUIRunnable != null) {
						mHideHandler.removeCallbacks(mHideUIRunnable);
					}
					mHideUIRunnable = new HideUiRunnable(this);
					mHideHandler.post(mHideUIRunnable);
				}
			} else {
				mIcntvPlayer.start();
				isPaused = false;
				btn_pause.setVisibility(View.GONE);
				if (fl_buffering != null) {
					fl_buffering.setVisibility(View.INVISIBLE);
				}
				// 数据统计：暂停结束
				LogController.getInstance(this).logUpload(
						ParameterConstant.PLAYER_ACTIVITY,
						ParameterConstant.PAUSE_COMPLETE + ","
								+ mList.get(pos).getId() + ","
								+ mList.get(pos).getProgramId() + ","
								+ ParameterConstant.CHARGE_TYPE_FREE + ","
								+ video_resolution + "," + movieLength + ","
								+ mIcntvPlayer.getCurrentPosition(), TAG);
			}
		}

	}

	/**
	 * 快退，对应遥控左键
	 */
	private void seekBack(final int repeatCount) {
		// 执行seek前先去获取片源时长，若依旧获取失败则不执行seek并提示用户

		if (mIcntvPlayer.getDuration() < mIcntvPlayer.getCurrentPosition()) {
			// 仅在第一次按下时显示提示
			if (repeatCount == 0) {
				Toast.makeText(mContext,
						getResources().getString(R.string.tv_get_length_error),
						Toast.LENGTH_LONG).show();
			}
			return;
		}

		mTotalTime = Utils.toTime(mIcntvPlayer.getDuration());
		tv_total.setText(mTotalTime);
		mSeekBar.setMax(mIcntvPlayer.getDuration());

		// 快进快退时，先暂停UI刷新，防止seekBar抖动
		mFreshHandler.removeMessages(WindowMessageID.REFLESH_TIME);
		int currentPositionRight = mIcntvPlayer.getCurrentPosition();
		if ((currentPositionRight - VALUE_SEEK) >= 0) {
			mIcntvPlayer.seekTo(currentPositionRight - VALUE_SEEK);
			mSeekBar.setProgress((currentPositionRight - VALUE_SEEK));
			tv_current.setText(Utils.toTime(currentPositionRight - VALUE_SEEK));
		} else {
			mIcntvPlayer.seekTo(0);
			mSeekBar.setProgress(0);
			tv_current.setText("00:00:00");
		}

		// 数据统计：seek开始(快退)
		if (repeatCount == 0) {
			LogController.getInstance(this).logUpload(
					ParameterConstant.PLAYER_ACTIVITY,
					ParameterConstant.SEEK_START + "," + mList.get(pos).getId()
							+ "," + mList.get(pos).getProgramId() + ","
							+ ParameterConstant.CHARGE_TYPE_FREE + ","
							+ video_resolution + "," + movieLength + ","
							+ mIcntvPlayer.getCurrentPosition(), TAG);

		}
		// seek完成后，重新启动UI刷新
		mFreshHandler.sendEmptyMessageDelayed(WindowMessageID.REFLESH_TIME,
				DELAY_OBTAIN_PLAYINFO);
	}

	/**
	 * 快进，对应遥控右键
	 */
	private void seekForward(final int repeatCount) {

		if (mIcntvPlayer.getDuration() < mIcntvPlayer.getCurrentPosition()) {
			if (repeatCount == 0) {
				Toast.makeText(mContext,
						getResources().getString(R.string.tv_get_length_error),
						Toast.LENGTH_LONG).show();
			}
			return;
		}

		mTotalTime = Utils.toTime(mIcntvPlayer.getDuration());
		tv_total.setText(mTotalTime);
		mSeekBar.setMax(mIcntvPlayer.getDuration());

		mFreshHandler.removeMessages(WindowMessageID.REFLESH_TIME);
		int durationLeft = mIcntvPlayer.getDuration();
		int currentPositionLeft = mIcntvPlayer.getCurrentPosition();
		if ((currentPositionLeft + VALUE_SEEK) <= durationLeft) {
			mIcntvPlayer.seekTo(currentPositionLeft + VALUE_SEEK);
			mSeekBar.setProgress((currentPositionLeft + VALUE_SEEK));
			tv_current.setText(Utils.toTime(currentPositionLeft + VALUE_SEEK));
		} else {
			mIcntvPlayer.seekTo(durationLeft);
			mSeekBar.setProgress(mIcntvPlayer.getDuration());
			tv_current.setText(mTotalTime);
		}

		// 数据统计：seek开始(快进)
		if (repeatCount == 0) {
			LogController.getInstance(this).logUpload(
					ParameterConstant.PLAYER_ACTIVITY,
					ParameterConstant.SEEK_START + "," + mList.get(pos).getId()
							+ "," + mList.get(pos).getProgramId() + ","
							+ ParameterConstant.CHARGE_TYPE_FREE + ","
							+ video_resolution + "," + movieLength + ","
							+ mIcntvPlayer.getCurrentPosition(), TAG);
		}
		mFreshHandler.sendEmptyMessageDelayed(WindowMessageID.REFLESH_TIME,
				DELAY_OBTAIN_PLAYINFO);
	}

	/**
	 * 刷新当前播放时间的handler
	 */
	private class FreshHandler extends Handler {
		private WeakReference<IcntvPlayer> mWeak;

		public FreshHandler(IcntvPlayer vod) {
			super();
			this.mWeak = new WeakReference<IcntvPlayer>(vod);
		}

		@Override
		public void handleMessage(Message msg) {
			IcntvPlayer fgm = mWeak.get();
			if (fgm == null)
				return;
			if (fgm.getDuration() < 1)
				return;
			switch (msg.what) {
			case WindowMessageID.REFLESH_TIME:
				mCurrentTime = Utils.toTime(fgm.getCurrentPosition());
				tv_current.setText(mCurrentTime);

				mSeekBar.setProgress(fgm.getCurrentPosition());
				mFreshHandler.sendEmptyMessageDelayed(
						WindowMessageID.REFLESH_TIME, 1000);
			default:
				break;
			}
		}
	}

	/**
	 * 定义自动隐藏的时间 If {@link #AUTO_HIDE} is set, the number of milliseconds to
	 * wait after user interaction before hiding the system UI.
	 */
	public static final int AUTO_HIDE_DELAY_MILLIS = 10 * 1000;

	/**
	 * 用于隐藏界面的runnable
	 */
	static HideUiRunnable mHideUIRunnable;

	private static class HideUiRunnable implements Runnable {
		WeakReference<PlayerActivity> mRef;

		HideUiRunnable(PlayerActivity act) {
			mRef = new WeakReference<PlayerActivity>(act);
		}

		@Override
		public void run() {
			PlayerActivity act = mRef.get();
			if (act == null)
				return;
			act.hideLayer();
		}
	}

	private static Handler mHideHandler = new Handler();

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
	 * 返回详情页，剧集类movie需要将当前播放到的剧集位置回传到Detail页
	 */
	private void returnToDetail() {
		Intent newIntent = new Intent(PlayerActivity.this,
				DetailDramaActivity.class);
		newIntent.putExtra(ParameterConstant.NAME_POSTION_CURRENT, pos);
		setResult(DetailDramaActivity.RESULT_CODE, newIntent);
		PlayerActivity.this.finish();
	}

	/**
	 * 退出播放dialog
	 */
	private void showExitDialog() {
		// 防止窗体泄露
		if (isFinishing()) {
			return;
		}
		ExitDialog mExitDialog = new ExitDialog(mContext);
		mExitDialog.setMessage(getResources().getString(
				R.string.msg_dialog_exit));
		mExitDialog.setCancle(getResources().getString(R.string.text_negative));
		mExitDialog
				.setConfirm(getResources().getString(R.string.text_positive));
		mExitDialog.setPositiveButton(new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				// 数据统计：点播结束(endType为1)
				LogController.getInstance(PlayerActivity.this).logUpload(
						ParameterConstant.PLAYER_ACTIVITY,
						ParameterConstant.PLAY_COMPLETE + ","
								+ mList.get(pos).getId() + ","
								+ mList.get(pos).getProgramId() + ","
								+ ParameterConstant.CHARGE_TYPE_FREE + ","
								+ video_resolution + "," + movieLength + ","
								+ mIcntvPlayer.getCurrentPosition() + ","
								+ ParameterConstant.ENDTYPE_BY_USER, TAG);
				returnToDetail();
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
	 * 下集续播dialog
	 */
	private void showNextDialog() {
		if (mList.size() == 1) {
			Toast.makeText(mContext,
					getResources().getString(R.string.tv_play_complete),
					Toast.LENGTH_SHORT).show();
			mHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					returnToDetail();
				}
			}, DELAY_PLAY_COMPLETE);
		} else if (mList.size() > 1) {
			if (pos == mList.size() - 1) {
				// 当前播放是最后一集
				Toast.makeText(mContext,
						getResources().getString(R.string.tip_final_episode),
						Toast.LENGTH_SHORT).show();
				mHandler.postDelayed(new Runnable() {

					@Override
					public void run() {
						returnToDetail();
					}
				}, DELAY_PLAY_COMPLETE);
			} else {
				if (isFinishing()) {
					return;
				}
				ExitDialog mExitDialog = new ExitDialog(mContext);
				mExitDialog.setMessage(getResources().getString(
						R.string.msg_dialog_next));
				mExitDialog.setCancle(getResources().getString(
						R.string.text_negative));
				mExitDialog.setConfirm(getResources().getString(
						R.string.text_positive));
				mExitDialog.setPositiveButton(
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								resumePlay();
							}
						}).setNegativeButton(
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								returnToDetail();
							}
						});
				mExitDialog.show();
			}
		}
	}

	/**
	 * 播放出错dialog
	 */
	private void showErrorDialog(String errorMsg) {
		if (isFinishing()) {
			return;
		}
		ExitDialog mExitDialog = new ExitDialog(mContext);
		mExitDialog.setMessage(errorMsg);
		mExitDialog.setCancle(getResources().getString(R.string.text_exit));
		mExitDialog.setConfirm(getResources().getString(R.string.text_retry));
		mExitDialog.setPositiveButton(new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				retryToPlay();
			}
		}).setNegativeButton(new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				// 数据统计：点播结束(endType为2)
				LogController.getInstance(PlayerActivity.this).logUpload(
						ParameterConstant.PLAYER_ACTIVITY,
						ParameterConstant.PLAY_COMPLETE + ","
								+ mList.get(pos).getId() + ","
								+ mList.get(pos).getProgramId() + ","
								+ ParameterConstant.CHARGE_TYPE_FREE + ","
								+ video_resolution + "," + movieLength + ","
								// 播放出错时，当前时间为EPG传过来的总时长
								+ movieLength + ","
								+ ParameterConstant.ENDTYPE_PLAY_ERROR, TAG);
				returnToDetail();
			}
		});
		mExitDialog.show();
	}

	/**
	 * 重新加载播放器
	 */
	private void retryToPlay() {
		if (mIcntvPlayer != null) {
			mIcntvPlayer.release();
		}
		initExtraData(mBundle);
	}

	@Override
	protected void onStart() {
		super.onStart();
		// homeKeyListener.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		homeKeyListener.stop();
	}

	@Override
	protected void onStop() {
		super.onStop();
		// homeKeyListener.stop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		homeKeyListener.start();
	}

	/**
	 * 换集播放dialog
	 */
	private void showClickDialog() {
		if (pos < mList.size()) {
			if (isFinishing()) {
				return;
			}
			ExitDialog mExitDialog = new ExitDialog(mContext);
			mExitDialog.setMessage(getResources().getString(
					R.string.msg_dialog_select));
			mExitDialog.setCancle(getResources().getString(
					R.string.text_negative));
			mExitDialog.setConfirm(getResources().getString(
					R.string.text_positive));
			mExitDialog.setPositiveButton(
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							doPlay();
						}
					}).setNegativeButton(new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			mExitDialog.show();
		}
	}

	private void doPlay() {
		Intent intent = new Intent(mContext, PlayerActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable(ParameterConstant.OBJECT_SERIALIZABLE, mItem);
		bundle.putInt(ParameterConstant.COUNT_SERIES, pos);
		intent.putExtras(bundle);
		if (mIcntvPlayer.isPlaying()) {
			mIcntvPlayer.stop();
		}

		if (btn_pause.getVisibility() == View.VISIBLE) {
			btn_pause.setVisibility(View.GONE);
		}

		if (isShow) {
			hideLayer();
		}

		if (rv_series != null) {
			// 如果是剧集需要重置selection焦点
			rv_series.setSelection(0, -1,
					FocusRecyclerView.PLACEHOLDER_POSITION);
		}
		startActivity(intent);
	}

	/**
	 * 续播（剧集）
	 */
	private void resumePlay() {
		if (pos < mList.size() - 1) {
			pos = pos + 1;
			doPlay();
		}
	}

	@Override
	protected void onDestroy() {
		Log.i(TAG, "---PlayerActivity---onDestroy---" + this);

		// 结束播放器
		mIcntvPlayer.release();

		if (mHideUIRunnable != null) {
			mHideHandler.removeCallbacks(mHideUIRunnable);
		}

		if (mFreshHandler != null) {
			mFreshHandler.removeMessages(WindowMessageID.REFLESH_TIME);
		}

		super.onDestroy();
	}

	/**
	 * 音量布局的控制
	 */
	private int MAX_VOLUME = 0;
	private int RATING_NUMS = 0;
	private static int VOLUME_RATINTBAR_HIDE_DELAY = 2000;
	private RelativeLayout rl_volume;
	private RatingBar mVolumRatingBar;
	private TextView tv_volume;
	private Handler mHandler = new Handler();
	private boolean isMute;

	private void initVolumeView() {
		rl_volume = (RelativeLayout) findViewById(R.id.container_volume);
		mVolumRatingBar = (RatingBar) findViewById(R.id.play_volume_ratingbar);
		tv_volume = (TextView) findViewById(R.id.tv_volume);
		RATING_NUMS = mVolumRatingBar.getNumStars();
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		MAX_VOLUME = mAudioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	}

	private void setVolume(int volume) {
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume,
				AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
	}

	private int getVolume() {
		return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
	}

	private int upVolume(int current) {
		int tempVolume = current + MAX_VOLUME / RATING_NUMS;
		current = (tempVolume > MAX_VOLUME) ? MAX_VOLUME : tempVolume;
		setVolume(current);
		updateVolumeRatingBar(current * RATING_NUMS / MAX_VOLUME);
		return current;
	}

	private int downVolume(int current) {
		int tempVolume = current - MAX_VOLUME / RATING_NUMS;
		current = (tempVolume < 0) ? 0 : tempVolume;
		setVolume(current);
		updateVolumeRatingBar(current * RATING_NUMS / MAX_VOLUME);
		return current;
	}

	private void updateVolumeRatingBar(int rating) {
		mVolumRatingBar.setRating(rating);
		tv_volume.setText(getResources().getString(R.string.tv_volume_prefix)
				+ rating);
		rl_volume.setVisibility(View.VISIBLE);
		hideVolumeRaingBarDelay();
	}

	private void mute(int current) {
		isMute = !isMute;

		if (isMute) {
			setVolume(0);
			updateVolumeRatingBar(0);
		} else {
			setVolume(current);
			updateVolumeRatingBar(current);
		}
	}

	Runnable mHideVolumeRatingBar = new Runnable() {
		@Override
		public void run() {
			rl_volume.setVisibility(View.GONE);
		}
	};

	public void hideVolumeRaingBarDelay() {
		mHandler.removeCallbacks(mHideVolumeRatingBar);
		mHandler.postDelayed(mHideVolumeRatingBar, VOLUME_RATINTBAR_HIDE_DELAY);
	}

}
