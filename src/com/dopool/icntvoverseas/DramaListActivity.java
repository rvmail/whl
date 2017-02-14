package com.dopool.icntvoverseas;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.State;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dopool.icntvoverseas.adapter.GVDramaAdapter;
import com.dopool.icntvoverseas.adapter.LVGuideAdapter;
import com.dopool.icntvoverseas.model.ParameterConstant;
import com.dopool.icntvoverseas.model.WindowMessageID;
import com.dopool.icntvoverseas.view.CustomListView;
import com.dopool.icntvoverseas.view.boderview.BorderView;
import com.dopool.icntvoverseas.view.focusrecyclerview.DramaListRecyclerView;
import com.dopool.icntvoverseas.view.focusrecyclerview.FocusRecyclerView.OnItemClickListener;
import com.dopool.icntvoverseas.view.focusrecyclerview.WrapContentGridLayoutManager;

import dopool.cntv.base.CategoryItem;
import dopool.cntv.base.EpgCategory;
import dopool.cntv.base.EpgSeries;
import dopool.cntv.base.SeriesItem;
import dopool.controller.EpgController;
import dopool.controller.EpgController.CategoryRequestListener;
import dopool.controller.EpgController.SeriesRequestListener;
import dopool.controller.LogController;

public class DramaListActivity extends BaseActivity implements
		CategoryRequestListener, SeriesRequestListener {

	private int pageNum = 1;
	private int pageSize = 15;
	private int wifiLevel = 4;

	public TextView tv_drama_count;
	public ImageView iv_up, iv_down;
	public DramaListRecyclerView dramaListRecyclerView;
	public CustomListView lv_drama;
	private RelativeLayout rootLayout;
	private ImageView iv_search, iv_setting, iv_wifi;
	private WifiManager wifiManager = null; // Wifi管理器
	private WifiInfo wifiInfo = null; // 获得的Wifi信息
	private static final long INTERVAL_WIFI = 1000 * 10; // Wifi信息的查询间隔
	// 用于记录搜索or设置按钮的焦点切换
	private View oldFocusView = null;

	private GVDramaAdapter mDramaAdapter;
	private GridLayoutManager gridLayoutManager;

	private List<CategoryItem> mCategoryItems;
	private LVGuideAdapter mGuideAdapter;
	private FrameLayout fl_loading_catg;

	public static int select_item = -1;

	private EpgController mEpgController;
	private static final String TAG = DramaListActivity.class.getSimpleName();

	public int currentItem = 0, totalItem = 0;
	public static final int NUM_PER_REQUEST = 24;
	private int requestPage, totalPage;
	private long catgItemId = 0;
	private long currentCatgItemId = 0;

	/**
	 * 用于分页请求，记录每次请求的list数据
	 */
	private SparseArray<List<SeriesItem>> mSparseArray;
	private TextView emptyView;

	private BorderView borderView;
	/**
	 * 是否为首次左侧list默认焦点
	 */
	protected boolean isDefaultFocus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_drama_list);

		initData();
		initUI();
		initEpgData();
		setListener();

		// 添加边框
		borderView = new BorderView(this);
		borderView.getEffect().setMargin(
				(int) getResources().getDimension(R.dimen.px6),
				(int) getResources().getDimension(R.dimen.px6));
		borderView.setBackgroundResource(R.drawable.frame);
		borderView.attachTo(dramaListRecyclerView);

		isDefaultFocus = true;
	}

	private void initEpgData() {
		mEpgController = EpgController.init(this);
		mEpgController.registerSeriesRequestListener(this);
		mEpgController.registerCategoryRequestListener(this);
		mEpgController.requestCategory(ParameterConstant.CATGITEMID_VOD,
				pageNum, pageSize, "");
		// 数据统计：进入某个栏目
		LogController.getInstance(DramaListActivity.this).logUpload(
				ParameterConstant.COLUMN,
				ParameterConstant.COLOMN_DEFAULT_OPERATE + ","
						+ ParameterConstant.CATGITEMID_VOD, TAG);
	}

	/**
	 * 初始化布局控件
	 */
	private void initUI() {
		dramaListRecyclerView = (DramaListRecyclerView) findViewById(R.id.gv_drama);
		lv_drama = (CustomListView) findViewById(R.id.lv_drama_type);
		mGuideAdapter = new LVGuideAdapter(DramaListActivity.this);

		rootLayout = (RelativeLayout) findViewById(R.id.rl_drama);
		iv_search = (ImageView) findViewById(R.id.iv_search_drama);
		iv_setting = (ImageView) findViewById(R.id.iv_setting_drama);
		iv_wifi = (ImageView) findViewById(R.id.iv_wifi_drama);
		tv_drama_count = (TextView) findViewById(R.id.tv_count_drama);
		iv_up = (ImageView) findViewById(R.id.iv_up);
		iv_down = (ImageView) findViewById(R.id.iv_down);
		lv_drama.requestFocus();
		mGuideAdapter.setGainFocus(true);

		dramaListRecyclerView.addItemDecoration(new MyDecoration(
				DramaListActivity.this, MyDecoration.VERTICAL_LIST));
		dramaListRecyclerView.setHasFixedSize(true);
		gridLayoutManager = new WrapContentGridLayoutManager(DramaListActivity.this,
				DramaListRecyclerView.NUM_COLUMNS);
		dramaListRecyclerView.setLayoutManager(gridLayoutManager);
		mDramaAdapter = new GVDramaAdapter(getBaseContext(),
				dramaListRecyclerView);
		dramaListRecyclerView.setAdapter(mDramaAdapter);
		dramaListRecyclerView.setPositions(0,
				DramaListRecyclerView.NUM_COLUMNS * 2 - 1);
		dramaListRecyclerView.setControll(iv_up, iv_down);

		fl_loading_catg = (FrameLayout) findViewById(R.id.container_buffering_catg);
		emptyView = (TextView) findViewById(R.id.empty_view);
	}

	// 按信号强弱刷新wifi图片显示
	private FreshHandler mFreshHandler = new FreshHandler(this);

	private class FreshHandler extends Handler {
		private WeakReference<DramaListActivity> mWeak;

		public FreshHandler(DramaListActivity act) {
			super();
			this.mWeak = new WeakReference<DramaListActivity>(act);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			DramaListActivity act = mWeak.get();
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

	@Override
	protected void onStop() {
		if (mFreshHandler != null) {
			mFreshHandler.removeMessages(WindowMessageID.REFLESH_TIME);
			mFreshHandler = null;
		}

		super.onStop();
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

	/**
	 * 初始化数据
	 */
	private void initData() {
		mCategoryItems = new ArrayList<CategoryItem>();
		requestPage = 1;
		mSparseArray = new SparseArray<List<SeriesItem>>();
	}

	/**
	 * 给各控件设置监听
	 */
	private void setListener() {
		iv_search.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(DramaListActivity.this,
						SearchActivity.class);
				startActivity(intent);
			}
		});
		iv_setting.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(Settings.ACTION_SETTINGS);
				startActivity(intent);
			}
		});
		lv_drama.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				mGuideAdapter.setGainFocus(hasFocus);
				mGuideAdapter.notifyDataSetChanged();
			}
		});
		lv_drama.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// 数据统计：获取某个子栏目
				try {
					if (isDefaultFocus && position == 0) {

						LogController
								.getInstance(DramaListActivity.this)
								.logUpload(
										ParameterConstant.COLUMN,
										ParameterConstant.COLOMN_DEFAULT_OPERATE
												+ ","
												+ mCategoryItems.get(position)
														.getId(), TAG);
						isDefaultFocus = false;
					} else {
						LogController.getInstance(DramaListActivity.this)
								.logUpload(
										ParameterConstant.COLUMN,
										ParameterConstant.COLOMN_USER_OPERATE
												+ ","
												+ mCategoryItems.get(position)
														.getId(), TAG);
					}
				} catch (Exception e) {
					isDefaultFocus = false;
				}

				dramaListRecyclerView.smoothScrollToPosition(0);
				dramaListRecyclerView.setItemSelected(-100);
				dramaListRecyclerView.setPositions(0,
						DramaListRecyclerView.NUM_COLUMNS * 2 - 1);
				borderView.setFirstFocus(true);
				mDramaAdapter.clear();
				select_item = position;
				currentCatgItemId = catgItemId = mCategoryItems.get(position)
						.getId();

				mSparseArray.clear();
				requestPage = 1;
				if (fl_loading_catg != null) {
					fl_loading_catg.setVisibility(View.VISIBLE);
				}
				mEpgController.requestSeriesList(catgItemId + "", requestPage,
						NUM_PER_REQUEST, catgItemId + "");
				mGuideAdapter.notifyDataSetChanged();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		dramaListRecyclerView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClicked(int position) {
				Intent intent = new Intent(DramaListActivity.this,
						DetailDramaActivity.class);
				Bundle bundle = new Bundle();
				// 当前页码，以及在当前页码的相对位置
				int currentPage, relativePos;
				if ((position + 1) % NUM_PER_REQUEST == 0) {
					currentPage = (position + 1) / NUM_PER_REQUEST;
					relativePos = NUM_PER_REQUEST - 1;
				} else {
					currentPage = (position + 1) / NUM_PER_REQUEST + 1;
					relativePos = (position + 1) % NUM_PER_REQUEST - 1;
				}

				if (mSparseArray != null
						&& mSparseArray.get(currentPage) != null) {
					Serializable mSerializable = mSparseArray.get(currentPage)
							.get(relativePos);
					if (mSerializable == null)
						return;
					bundle.putSerializable(
							ParameterConstant.OBJECT_SERIALIZABLE,
							mSerializable);
					try {
						String title = mCategoryItems.get(
								lv_drama.getSelectedItemPosition()).getTitle();
						bundle.putString(ParameterConstant.TYPE, title);
					} catch (IndexOutOfBoundsException e) {
					}
					intent.putExtras(bundle);
					startActivity(intent);
				}
			}
		});

		dramaListRecyclerView
				.addOnScrollListener(new RecyclerView.OnScrollListener() {
					@Override
					public void onScrolled(RecyclerView recyclerView, int dx,
							int dy) {
						super.onScrolled(recyclerView, dx, dy);
						if (NUM_PER_REQUEST * requestPage == dramaListRecyclerView
								.getlastVisibleItemPosition() + 1
								&& requestPage < totalPage) {
							requestPage++;
							if (fl_loading_catg != null) {
								fl_loading_catg.setVisibility(View.VISIBLE);
							}
							mEpgController.requestSeriesList(catgItemId + "",
									requestPage, NUM_PER_REQUEST, catgItemId
											+ "");
						}
					}
				});

	}

	@Override
	protected void onDestroy() {
		select_item = 0;
		mEpgController.unregisterSeriesRequestListener(this);
		mEpgController.unregisterCategoryRequestListener(this);
		super.onDestroy();
	}

	@Override
	public void onResult(EpgSeries mEpgSeries) {
		// 请求返回的数据CatgItemId 与 当前currentCatgItemId 不等时
		// 放弃本次请求返回数据（避免请求未返回时切换到其他Catg时造成数据混乱问题）
		if (!mEpgSeries.getRequestTag().getTag().equals(currentCatgItemId + "")) {
			return;
		}

		if (fl_loading_catg != null) {
			fl_loading_catg.setVisibility(View.GONE);
		}

		if (mEpgSeries.getProgramSeries() != null
				&& mEpgSeries.getProgramSeries().size() > 0) {
			if(dramaListRecyclerView.getVisibility() == View.GONE){
				dramaListRecyclerView.setVisibility(View.VISIBLE);
			}
			if(emptyView.getVisibility() == View.VISIBLE){
				emptyView.setVisibility(View.GONE);
			}
			if(tv_drama_count.getVisibility() == View.GONE){
				tv_drama_count.setVisibility(View.VISIBLE);
			}
			boolean preIsCache = false;
			if (mSparseArray.indexOfKey(requestPage) >= 0) {
				// 已经有缓存
				preIsCache = true;
				mDramaAdapter.addData(mEpgSeries.getProgramSeries(),
						preIsCache, mSparseArray.get(requestPage));
			} else {
				// 没有缓存
				mDramaAdapter.addData(mEpgSeries.getProgramSeries(),
						preIsCache, null);
			}
			mSparseArray.put(requestPage, mEpgSeries.getProgramSeries());

			if (requestPage == 1) {
				totalItem = mEpgSeries.getCount();
				if (totalItem % NUM_PER_REQUEST == 0) {
					totalPage = totalItem / NUM_PER_REQUEST;
				} else {
					totalPage = totalItem / NUM_PER_REQUEST + 1;
				}
				tv_drama_count.setText(currentItem + "/" + totalItem);
			}
		}else if (mSparseArray.size() == 0){
			dramaListRecyclerView.setVisibility(View.GONE);
			emptyView.setVisibility(View.VISIBLE);
			tv_drama_count.setVisibility(View.GONE);
		}

		dramaListRecyclerView.controllIndicator();
	}

	@Override
	public void onResult(EpgCategory category) {
		mCategoryItems = category.getCatgItems();
		mGuideAdapter.setData(mCategoryItems);
		lv_drama.setAdapter(mGuideAdapter);

		if (fl_loading_catg != null) {
			fl_loading_catg.setVisibility(View.GONE);
		}
	}

	public class MyDecoration extends RecyclerView.ItemDecoration {
		private final int[] ATTRS = new int[] { android.R.attr.listDivider };

		public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;

		public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

		private Drawable mDivider;

		private int mOrientation;

		public MyDecoration(Context context, int orientation) {
			final TypedArray a = context.obtainStyledAttributes(ATTRS);
			mDivider = a.getDrawable(0);
			mDivider.setAlpha(0);
			a.recycle();
			setOrientation(orientation);
		}

		public void setOrientation(int orientation) {
			if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
				throw new IllegalArgumentException("invalid orientation");
			}
			mOrientation = orientation;
		}

		@Override
		public void onDrawOver(Canvas c, RecyclerView parent, State state) {

			if (mOrientation == VERTICAL_LIST) {
				drawVertical(c, parent);
			} else {
				drawHorizontal(c, parent);
			}
		}

		public void drawVertical(Canvas c, RecyclerView parent) {
			final int left = parent.getPaddingLeft();
			final int right = parent.getWidth() - parent.getPaddingRight();

			final int childCount = parent.getChildCount();
			for (int i = 0; i < childCount; i++) {
				final View child = parent.getChildAt(i);
				final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
						.getLayoutParams();
				final int top = child.getBottom() + params.bottomMargin;
				final int bottom = (int) (top + getResources().getDimension(
						R.dimen.px30));
				mDivider.setBounds(left, top, right, bottom);
				mDivider.draw(c);
			}
		}

		public void drawHorizontal(Canvas c, RecyclerView parent) {
			final int top = parent.getPaddingTop();
			final int bottom = parent.getHeight() - parent.getPaddingBottom();

			final int childCount = parent.getChildCount();
			for (int i = 0; i < childCount; i++) {
				final View child = parent.getChildAt(i);
				final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
						.getLayoutParams();
				final int left = child.getRight() + params.rightMargin;
				final int right = left + mDivider.getIntrinsicHeight();
				mDivider.setBounds(left, top, right, bottom);
				mDivider.draw(c);
			}
		}

		@Override
		public void getItemOffsets(Rect outRect, View view,
				RecyclerView parent, State state) {
			// super.getItemOffsets(outRect, view, parent, state);
			if (mOrientation == VERTICAL_LIST) {
				outRect.set(0, 0, 0,
						getResources().getDimensionPixelSize(R.dimen.px30));
			}
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			View curr = rootLayout.findFocus();
			if (curr != null) {
				if (curr.getId() == R.id.iv_search_drama) {
					oldFocusView = iv_search;
				}

				if (curr.getId() == R.id.iv_setting_drama) {
					oldFocusView = iv_setting;
				}

				if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
					// 当前焦点在ListView的第一个item时，按上键，之前记录的控件请求获取焦点
					if (oldFocusView != null
							&& (curr instanceof ListView && lv_drama
									.getSelectedItemPosition() == 0)) {
						oldFocusView.requestFocus();
						return true;
					}

					if (curr.getId() == R.id.gv_drama) {
						// 当前焦点在RecyclerView的第一行item时，按上键，之前记录的控件请求获取焦点
						if ((oldFocusView != null)
								&& (dramaListRecyclerView.getSelection() < DramaListRecyclerView.NUM_COLUMNS)
								&& (dramaListRecyclerView.getSelection() > -1)) {
							oldFocusView.requestFocus();
							return true;
						} else {
							if (event.getRepeatCount() > 0) {
								mDramaAdapter.getImageLoader().pause();
							} else {
								mDramaAdapter.getImageLoader().resume();
							}
						}
					}
				} else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
					if (curr.getId() == R.id.gv_drama) {
						// 一直按着下键不放即RecyclerView的状态为SCROLL_FLING时，暂停图片加载
						if (event.getRepeatCount() > 0) {
							mDramaAdapter.getImageLoader().pause();
						} else {
							mDramaAdapter.getImageLoader().resume();
						}
					}
				}
			}
		} else if (event.getAction() == KeyEvent.ACTION_UP) {
			// 松开按键，请求加载当前图片资源
			mDramaAdapter.getImageLoader().resume();
		}
		return super.dispatchKeyEvent(event);
	}
}
