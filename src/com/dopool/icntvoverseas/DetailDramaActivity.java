package com.dopool.icntvoverseas;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dopool.icntvoverseas.adapter.GalleryAdapter;
import com.dopool.icntvoverseas.adapter.SerisAdapter;
import com.dopool.icntvoverseas.entity.ChannelItem;
import com.dopool.icntvoverseas.model.ParameterConstant;
import com.dopool.icntvoverseas.model.WindowMessageID;
import com.dopool.icntvoverseas.utils.Utils;
import com.dopool.icntvoverseas.view.boderview.BorderView;
import com.dopool.icntvoverseas.view.focusrecyclerview.FocusRecyclerView;
import com.dopool.icntvoverseas.view.focusrecyclerview.GridHorizontalFocusRecyclerView;
import com.dopool.icntvoverseas.view.focusrecyclerview.RecommendRecyclerView;
import com.dopool.icntvoverseas.view.focusrecyclerview.WrapContentGridLayoutManager;
import com.dopool.icntvoverseas.view.focusrecyclerview.WrapContentLinearLayoutManager;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import dopool.cntv.base.EpgMovie;
import dopool.cntv.base.LocalizedInfo;
import dopool.cntv.base.MovieItem;
import dopool.cntv.base.SearchInfo;
import dopool.cntv.base.SearchResult;
import dopool.cntv.base.SeriesItem;
import dopool.controller.CollectionController;
import dopool.controller.CollectionController.CollectionListener;
import dopool.controller.EpgController;
import dopool.controller.EpgController.MovieDetailsRequestListener;
import dopool.controller.EpgController.SearchListRequestListener;
import dopool.controller.HistoryController;
import dopool.controller.LogController;

/**
 * TODO:当前版本暂无价格，且不明确价格显示EpgMovie中哪个字段，暂用PpvId.
 * 后续更改，请修改resource.xml,string.xml及本类中对应之处。
 */
public class DetailDramaActivity extends BaseActivity implements
		MovieDetailsRequestListener, View.OnClickListener, CollectionListener,
		SearchListRequestListener {

	private static final String TAG = DetailDramaActivity.class.getSimpleName();

	private int endNum = 30;
	private int goOnSearchEndNum = 20;
	/**
	 * 相关推荐的布局控件
	 */
	private RecommendRecyclerView recycler_drama;
	private LinearLayoutManager mLayoutManager;
	private GalleryAdapter mGalleryAdapter;
	private Context mContext = DetailDramaActivity.this;
	private ImageView iv_left, iv_right;
	private ImageView indicator_left, indicator_right;
	private List<SearchInfo> mGalleryList = new ArrayList<SearchInfo>();

	/**
	 * 海报详情页的信息展示
	 */
	private ImageView iv_drama_detail_poster;
	private TextView tv_drama_detail_title;
	private TextView tv_drama_detail_one, tv_drama_detail_two,
			tv_drama_detail_three, tv_drama_detail_four, tv_drama_detail_five,
			tv_drama_detail_six, tv_drama_detail_serven, tv_drama_detail_eight;

	private ImageLoader mImageLoader;
	private String mSerisId, mCatgItemId, mName, picURL;
	private long seriesId;
	// 判断有无收藏记录的标志位
	private boolean isCollected = false;
	private EpgController mEpgController;
	private CollectionController mCollectionController;
	private HistoryController mHistoryController;
	// 启动activity时查询收藏史
	public static final String TAG_LAUCH = "query when lauching";
	// 点击收藏按钮时查询收藏史
	public static final String TAG_CLICK = "query after operating";

	/**
	 * 如果所属套餐是电视剧，则会有相关剧集展示（点播放呼出剧集布局）
	 */
	private RelativeLayout rl_seris;
	private GridHorizontalFocusRecyclerView recycler_seris;
	private GridLayoutManager mGridLayoutManager;
	private SerisAdapter mSerisAdapter;
	private List<MovieItem> mSerisList;
	private ImageView iv_left_seris, iv_right_seris;

	/**
	 * 播放、收藏按钮
	 */
	private RelativeLayout rl_play_drama, rl_collect_drama;
	private ImageView iv_play_drama, iv_collect_drama;

	/**
	 * 搜索推荐数据的关键词 （优先级别：演员 > 导演 > 节目类型）
	 */
	private String directorStr, actorStr, typeStr;

	public static final String TAG_ACTOR = "Actor";
	public static final String TAG_DIRECTOR = "Director";
	public static final String TAG_TYPE = "Type";

	private EpgMovie mEpgMovie;
	private String actionUrl = "";
	private int seriesPos = -1;
	// 用于记录播放or收藏按钮的焦点切换
	private View oldFocus = null;
	// 根布局
	private RelativeLayout rootlLayout;
	// video标题
	private String title = "";
	// 呼出剧集列表时的间隔delay
	private static final int DELAY_SHOW_SERIES = 150;

	private FrameLayout fl_loading;

	private boolean hasDetailInfo = false;
	// 跳转到PlayerActivity的request_code（必须为正数）
	public static final int REQUEST_CODE = 1;
	// 从PlayerActivity处回传的当前播放剧集数的result_code
	public static final int RESULT_CODE = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_drama_detail);
		Bundle bundle = getIntent().getExtras();
		Serializable mObject = bundle
				.getSerializable(ParameterConstant.OBJECT_SERIALIZABLE);
		if (mObject instanceof SeriesItem) {
			SeriesItem mItem = (SeriesItem) mObject;
			seriesId = mItem.getId();
		} else if (mObject instanceof SearchInfo) {
			SearchInfo mInfo = (SearchInfo) mObject;
			seriesId = mInfo.getSeriesId();
		}
		// 直接传id的情况
		if (seriesId == 0) {
			seriesId = bundle.getLong(ParameterConstant.SERISID);
		}
		title = bundle.getString(ParameterConstant.TYPE, "");

		// 数据统计：进入详情页
		LogController.getInstance(DetailDramaActivity.this).logUpload(
				ParameterConstant.DETAIL, seriesId + "", TAG);

		mSerisId = seriesId + "";

		initController();
		initUI();
		initGallery();
		setListener();

		// 添加边框
		BorderView borderView = new BorderView(this);
		borderView.getEffect().setMargin(
				(int) getResources().getDimension(R.dimen.px6),
				(int) getResources().getDimension(R.dimen.px6));
		borderView.setBackgroundResource(R.drawable.frame);
		borderView.attachTo(recycler_drama);

		// 切图
		cutThePhoto();
		getChannels();
	}

	/**
	 * 初始化CollectionController并注册监听
	 */
	private void initController() {
		mCollectionController = CollectionController.init(mContext);
		mHistoryController = HistoryController.init(mContext);
		mEpgController = EpgController.init(this);
		mEpgController.requestMovieDetails(mSerisId, null, mCatgItemId, "",
				null, "");

		ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(
				mContext).build();
		ImageLoader.getInstance().init(configuration);
		mImageLoader = ImageLoader.getInstance();
	}

	private void initUI() {
		iv_left = (ImageView) findViewById(R.id.overlap_left);
		iv_right = (ImageView) findViewById(R.id.overlap_right);
		indicator_left = (ImageView) findViewById(R.id.iv_left_drama);
		indicator_right = (ImageView) findViewById(R.id.iv_right_drama);
		fl_loading = (FrameLayout) findViewById(R.id.container_buffering);

		rootlLayout = (RelativeLayout) findViewById(R.id.rl_drama);
		iv_drama_detail_poster = (ImageView) findViewById(R.id.iv_drama_detail_poster);
		tv_drama_detail_title = (TextView) findViewById(R.id.tv_drama_detail_title);

		tv_drama_detail_one = (TextView) findViewById(R.id.tv_drama_detail_one);
		tv_drama_detail_two = (TextView) findViewById(R.id.tv_drama_detail_two);
		tv_drama_detail_three = (TextView) findViewById(R.id.tv_drama_detail_three);
		tv_drama_detail_four = (TextView) findViewById(R.id.tv_drama_detail_four);
		tv_drama_detail_five = (TextView) findViewById(R.id.tv_drama_detail_five);
		tv_drama_detail_six = (TextView) findViewById(R.id.tv_drama_detail_six);
		tv_drama_detail_serven = (TextView) findViewById(R.id.tv_drama_detail_serven);
		tv_drama_detail_eight = (TextView) findViewById(R.id.tv_drama_detail_eight);

		rl_play_drama = (RelativeLayout) findViewById(R.id.rl_play_drama);
		rl_collect_drama = (RelativeLayout) findViewById(R.id.rl_collect_drama);
		iv_play_drama = (ImageView) findViewById(R.id.iv_drama_detail_play);
		iv_collect_drama = (ImageView) findViewById(R.id.iv_drama_detail_collect);

		mCollectionController.queryById(seriesId, TAG_LAUCH);

		rl_seris = (RelativeLayout) findViewById(R.id.rl_seris);
		recycler_seris = (GridHorizontalFocusRecyclerView) findViewById(R.id.recycler_seris);
		mGridLayoutManager = new WrapContentGridLayoutManager(mContext, 2,
				LinearLayoutManager.HORIZONTAL, false);
		recycler_seris.setLayoutManager(mGridLayoutManager);
		recycler_seris.setHasFixedSize(true);
		recycler_seris.setItemSelected(0);

		iv_left_seris = (ImageView) findViewById(R.id.iv_seris_left);
		iv_right_seris = (ImageView) findViewById(R.id.iv_seris_right);
		((GridHorizontalFocusRecyclerView) recycler_seris).setControll(
				iv_left_seris, iv_right_seris);

		mSerisList = new ArrayList<MovieItem>();

		recycler_seris
				.setOnItemClickListener(new FocusRecyclerView.OnItemClickListener() {

					@Override
					public void onItemClicked(int position) {
						// 将在recyclerView中真正的位置position转化成要显示在界面上的realPos
						int realPos = mSerisAdapter.placeRealPos(position);
						if (realPos >= 0 && realPos < mSerisList.size()) {
							actionUrl = mSerisList.get(realPos).getActionURL();
							seriesPos = realPos;
							doPlay(mEpgMovie, actionUrl, realPos);
						}
					}
				});

		recycler_seris
				.setListener(new FocusRecyclerView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(int prePosition, int position,
							int changeLinePosition, FocusRecyclerView view) {
						if (prePosition == FocusRecyclerView.INVALIDATE_POSITION
								&& ((GridLayoutManager) view.getLayoutManager())
										.findViewByPosition(position) != null) {
							((GridLayoutManager) view.getLayoutManager())
									.findViewByPosition(position).setSelected(
											false);
							return;
						}
						if (prePosition != position
								&& ((GridLayoutManager) view.getLayoutManager())
										.findViewByPosition(prePosition) != null) {
							((GridLayoutManager) view.getLayoutManager())
									.findViewByPosition(prePosition)
									.setSelected(false);
						}
						if (((GridLayoutManager) view.getLayoutManager())
								.findViewByPosition(position) != null) {
							((GridLayoutManager) view.getLayoutManager())
									.findViewByPosition(position).setSelected(
											true);
						}
					}
				});
	}

	/**
	 * 呼出剧集列表
	 */
	private void showSeris() {
		if (mSerisList != null && mSerisList.size() > 1) {
			rl_seris.setVisibility(View.VISIBLE);
			((GridHorizontalFocusRecyclerView) recycler_seris)
					.controllIndicator(false);
			rl_play_drama.setVisibility(View.GONE);
			rl_collect_drama.setVisibility(View.GONE);
		} else {
			doPlay(mEpgMovie, actionUrl, seriesPos);
		}
	}

	/**
	 * 隐藏剧集列表
	 */
	private void hideSeris() {
		rl_play_drama.setVisibility(View.VISIBLE);
		rl_collect_drama.setVisibility(View.VISIBLE);
		rl_seris.setVisibility(View.GONE);
	}

	/**
	 * 初始化底部相关推荐的布局
	 */
	private void initGallery() {
		recycler_drama = (RecommendRecyclerView) findViewById(R.id.recycler_drama);
		recycler_drama.setHasFixedSize(true);
		recycler_drama.setControll(indicator_left, indicator_right);
		mLayoutManager = new WrapContentLinearLayoutManager(mContext,
				LinearLayoutManager.HORIZONTAL, false);
		recycler_drama.setLayoutManager(mLayoutManager);

		recycler_drama
				.setOnItemClickListener(new FocusRecyclerView.OnItemClickListener() {

					@Override
					public void onItemClicked(int position) {
						SearchInfo mSearchInfo = mGalleryList.get(position);
						Intent i = new Intent(mContext,
								DetailDramaActivity.class);
						Bundle bundle = new Bundle();
						bundle.putSerializable(
								ParameterConstant.OBJECT_SERIALIZABLE,
								mSearchInfo);
						i.putExtras(bundle);
						startActivity(i);
					}
				});

		((RecommendRecyclerView) recycler_drama)
				.setOnFirstVisiableItemchangeListener(new RecommendRecyclerView.OnFirstVisiableItemchangeListener() {

					@Override
					public void onFirstVisiableItemchange(
							int firstVisibleItemPosition) {
						if (firstVisibleItemPosition == 0) {
							iv_left.setVisibility(View.GONE);
						} else {
							iv_left.setVisibility(View.VISIBLE);
						}

						if (firstVisibleItemPosition
								+ RecommendRecyclerView.NUMBER_COLOUM_PRE_SCREEN == recycler_drama
								.getAdapter().getItemCount() - 1) {
							iv_right.setVisibility(View.GONE);
						} else {
							iv_right.setVisibility(View.VISIBLE);
						}

					}
				});
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "---DetailDramaActivity---onResume()---"+this);
		mEpgController.registerMovieDetailsRequestListener(this);
		mEpgController.registerSearchListRequestListener(this);
		mCollectionController.registCollectionListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "---DetailDramaActivity---onPause()---" + this);
		mEpgController.unregisterMovieDetailsRequestListener(this);
		mEpgController.unRegisterSearchListRequestListener(this);
		mCollectionController.unRegistCollectionListener(this);
	}

	private final String STR_INFORMATION = "information";
	private final String STR_PROGRAMCLASS = "programClass";
	private final String STR_TYPE = "type";
	private final String STR_ZONE = "zone";
	private final String STR_AUDIENCES = "audiences";
	private final String STR_RELEASEDATE = "releaseDate";
	private final String STR_PPVID = "ppvID";
	private final String STR_SUBJECT = "subject";
	private final String STR_DIRECTOR = "director";
	private final String STR_ACTOR = "actor";
	private final String STR_TAG = "tag";
	private final String STR_PRESENTER = "presenter";
	private final String STR_COMPETITION = "competition";

	private void setText(String str, EpgMovie mEpgMovie, int i) {
		TextView text = null;
		switch (i) {
		case 0:
			text = tv_drama_detail_one;
			break;
		case 1:
			text = tv_drama_detail_two;
			break;
		case 2:
			if (!str.equals(STR_INFORMATION)) {
				text = tv_drama_detail_three;
			} else {
				text = tv_drama_detail_eight;
			}
			break;
		case 3:
			if (!str.equals(STR_INFORMATION)) {
				text = tv_drama_detail_four;
			} else {
				text = tv_drama_detail_eight;
			}
			break;
		case 4:
			if (!str.equals(STR_INFORMATION)) {
				text = tv_drama_detail_five;
			} else {
				text = tv_drama_detail_eight;
			}
			break;
		case 5:
			if (!str.equals(STR_INFORMATION)) {
				text = tv_drama_detail_six;
			} else {
				text = tv_drama_detail_eight;
			}
			break;
		case 6:
			if (!str.equals(STR_INFORMATION)) {
				text = tv_drama_detail_serven;
			} else {
				text = tv_drama_detail_eight;
			}
			break;
		case 7:
			text = tv_drama_detail_eight;
			break;
		}
		String s = "";
		switch (str) {
		case STR_PROGRAMCLASS:
			s = mEpgMovie.getProgramClass();
			str = getString(R.string.programClass);
			break;
		case STR_TYPE:
			s = mEpgMovie.getType();
			str = getString(R.string.type);
			break;
		case STR_ZONE:
			s = mEpgMovie.getZone();
			str = getString(R.string.zone);
			break;
		case STR_AUDIENCES:
			s = mEpgMovie.getAudiences();
			str = getString(R.string.audiences);
			break;
		case STR_RELEASEDATE:
			s = mEpgMovie.getReleaseDate();
			str = getString(R.string.releaseDate);
			break;
		case STR_INFORMATION:
			s = mEpgMovie.getInformation();
			str = getString(R.string.information);
			break;
		case STR_PPVID:
			s = mEpgMovie.getPpvId();
			str = getString(R.string.ppvID);
			break;
		case STR_SUBJECT:
			s = mEpgMovie.getSubject();
			str = getString(R.string.subject);
			break;
		case STR_DIRECTOR:
			s = mEpgMovie.getDirector();
			str = getString(R.string.director);
			break;
		case STR_ACTOR:
			s = mEpgMovie.getActor();
			str = getString(R.string.actor);
			break;
		case STR_TAG:
			s = mEpgMovie.getTag();
			str = getString(R.string.tag);
			break;
		case STR_PRESENTER:
			s = mEpgMovie.getPresenter();
			str = getString(R.string.presenter);
			break;
		case STR_COMPETITION:
			s = mEpgMovie.getCompetition();
			str = getString(R.string.competition);
			break;

		}

		text.setText(str + "：" + s);

	}

	/**
	 * 详情查询的回调
	 */
	@Override
	public void onResult(EpgMovie mEpgMovie) {
		boolean flag = false;
		if (mEpgMovie != null) {
			this.mEpgMovie = mEpgMovie;
			tv_drama_detail_title.setText(mEpgMovie.getName());
			typeStr = mEpgMovie.getType();
			if (channels != null && channels.size() > 0) {
				for (int i = 0; i < channels.size(); i++) {
					if (title.equals(channels.get(i).getName())) {
						flag = true;
						for (int j = 0; j < channels.get(i).getProperties()
								.size(); j++) {
							setText(channels.get(i).getProperties().get(j),
									mEpgMovie, j);
						}
					}
				}
				if (!flag) {
					for (int j = 0; j < channels.get(channels.size() - 1)
							.getProperties().size(); j++) {
						setText(channels.get(channels.size() - 1)
								.getProperties().get(j), mEpgMovie, j);
					}
				}
			}
			directorStr = mEpgMovie.getDirector();
			actorStr = mEpgMovie.getActor();

			mName = mEpgMovie.getName();
			picURL = mEpgMovie.getPicurl();
			
			if(!TextUtils.isEmpty(mEpgMovie.getPicurl())){
				mImageLoader.displayImage(mEpgMovie.getPicurl(),
						iv_drama_detail_poster);
			}

			if (mEpgMovie.getSources() != null
					&& mEpgMovie.getSources().size() > 1) {
				// type为电视剧或多集资源
				mSerisList = mEpgMovie.getSources();
				mSerisAdapter = new SerisAdapter(mContext, mSerisList);
				mSerisAdapter.setReycycler(recycler_seris);
				recycler_seris.setAdapter(mSerisAdapter);
			} else if (mEpgMovie.getSources() != null
					&& mEpgMovie.getSources().size() == 1) {
				// type为单集资源
				actionUrl = mEpgMovie.getSources().get(0).getActionURL();
			}

			hasDetailInfo = true;
			String keyword = "";
			try {
				keyword = URLEncoder.encode(actorStr, "UTF-8");
				mEpgController.requestSearchList(keyword, 0, endNum, TAG_ACTOR+DetailDramaActivity.this.toString());
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

	private void doCollect() {
		if (isCollected) {
			// 数据统计：取消收藏
			LogController.getInstance(DetailDramaActivity.this).logUpload(
					ParameterConstant.COLLECT,
					ParameterConstant.COLLECT_NO + "," + seriesId, TAG);

			mCollectionController.deleteById(seriesId);
		} else {
			// 数据统计：收藏
			LogController.getInstance(DetailDramaActivity.this).logUpload(
					ParameterConstant.COLLECT,
					ParameterConstant.COLLECT_YES + "," + seriesId, TAG);

			LocalizedInfo mInfo = new LocalizedInfo();
			mInfo.setId(seriesId);
			mInfo.setName(mName);
			mInfo.setPicUrl(picURL);
			mCollectionController.insert(mInfo);
		}
		mCollectionController.queryById(seriesId, TAG_CLICK);
	}

	private void doPlay(EpgMovie mItem, String actionUrl, int pos) {
		if (mItem == null)
			return;
		LocalizedInfo mInfo = new LocalizedInfo();
		mInfo.setId(seriesId);
		mInfo.setName(mName);
		mInfo.setPicUrl(picURL);
		mInfo.setTime(new Date().getTime() + "");
		mHistoryController.insert(mInfo);
		Intent intent = new Intent(mContext, PlayerActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		Bundle bundle = new Bundle();
		bundle.putSerializable(ParameterConstant.OBJECT_SERIALIZABLE, mItem);
		bundle.putString(ParameterConstant.ACTIONURL, actionUrl);
		bundle.putInt(ParameterConstant.COUNT_SERIES, pos);
		intent.putExtras(bundle);
		startActivityForResult(intent, REQUEST_CODE);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_CODE) {
			if (rl_seris == null || rl_seris.getVisibility() == View.GONE) {
				return;
			}

			int posValue = data.getIntExtra(
					ParameterConstant.NAME_POSTION_CURRENT, 0);
			int orinalPos = mSerisAdapter.obtainOriginalPos(posValue);
			if (orinalPos >= 0) {
				int prePos = recycler_seris.getSelection();
				if (prePos != orinalPos) {
					// 防止之前的prePos被回收而报NPE
					if (mGridLayoutManager.findViewByPosition(prePos) != null) {
						mGridLayoutManager.findViewByPosition(prePos)
								.setSelected(false);
					}

					// 两个pos不在同一页，则需要先滚到orinalPos对应的那一页
					if ((prePos / SerisAdapter.NUM_PER_PAGE) != (orinalPos / SerisAdapter.NUM_PER_PAGE)) {
						int originalPage = orinalPos
								/ SerisAdapter.NUM_PER_PAGE;
						mGridLayoutManager.scrollToPositionWithOffset(
								originalPage * (SerisAdapter.NUM_PER_PAGE), 0);
					}

					recycler_seris.setSelection(prePos, orinalPos,
							FocusRecyclerView.PLACEHOLDER_POSITION);
					((GridHorizontalFocusRecyclerView) recycler_seris)
							.controllIndicator(true);
					((GridHorizontalFocusRecyclerView) recycler_seris)
							.reSetFirstAndLastPosition();
				}
			}
		}

	}

	@Override
	public void onCollectionQueryAll(
			ArrayList<LocalizedInfo> listCollectionInfo, String tag) {

	}

	/**
	 * 按SerisId查询收藏与否
	 */
	@Override
	public void onCollectionQueryById(LocalizedInfo collectionInfo, String tag) {
		if (collectionInfo.getId() == seriesId) {
			if (tag.equals(TAG_LAUCH) || tag.equals(TAG_CLICK)) {
				// 说明有过收藏记录，提示已收藏
				isCollected = true;
				iv_collect_drama
						.setImageResource(R.drawable.btn_movie_collected);
			}
		} else {
			isCollected = false;
			iv_collect_drama.setImageResource(R.drawable.btn_movie_collect);
		}
	}

	@Override
	public void onCollectionQueryByPaging(
			ArrayList<LocalizedInfo> listCollectionInfo, String tag) {
	}

	/**
	 * 重写后退键
	 */
	@Override
	public void onBackPressed() {
		View view = getCurrentFocus();
		if (view instanceof GridHorizontalFocusRecyclerView) {
			hideSeris();
		} else {
			super.onBackPressed();
		}
	}

	/**
	 * activity销毁时，反注册相关监听以防数据泄露
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();

		mEpgController.unregisterMovieDetailsRequestListener(this);
		mEpgController.unRegisterSearchListRequestListener(this);
		mCollectionController.unRegistCollectionListener(this);
	}

	private void setListener() {
		rl_play_drama.setOnClickListener(this);
		rl_collect_drama.setOnClickListener(this);
	}

	/**
	 * 按关键词查询推荐数据
	 */
	@Override
	public void onSearchListResult(SearchResult searchResult) {
		String tagString = searchResult.getRequestTag().getTag();
		if(!tagString.contains(DetailDramaActivity.this.toString())){
			return;
		}
		if (searchResult == null
				|| searchResult.getSearchInfoList() == null
				|| (searchResult.getSearchInfoList() != null && searchResult
						.getSearchInfoList().size() == 0)) {
			if (tagString.contains(TAG_ACTOR)) {
				mSearchHandler
						.sendEmptyMessage(WindowMessageID.SEARCH_DIRECTOR);
			} else if (tagString.contains(TAG_DIRECTOR)) {
				mSearchHandler.sendEmptyMessage(WindowMessageID.SEARCH_TYPE);
			} else if (tagString.contains(TAG_TYPE)) {
				// 三次查询后，数据依然为空
				Toast.makeText(
						mContext,
						getResources()
								.getString(R.string.tip_no_recommendation),
						Toast.LENGTH_SHORT).show();
				if (fl_loading != null) {
					fl_loading.setVisibility(View.GONE);
				}
			}
		} else {
			if (fl_loading != null) {
				fl_loading.setVisibility(View.GONE);
			}

			if (recycler_drama.getVisibility() == View.GONE) {
				recycler_drama.setVisibility(View.VISIBLE);
			}
			mGalleryList = searchResult.getSearchInfoList();

			mGalleryAdapter = new GalleryAdapter(mContext, mGalleryList);
			recycler_drama.setAdapter(mGalleryAdapter);
			recycler_drama.scrollToPosition(0);
			recycler_drama.setItemSelected(0);
			recycler_drama.controllIndicator();
		}
	}

	private void goOnSearch(final Message msg) {
		String keyword = "";

		if (msg != null) {
			switch (msg.what) {
			case WindowMessageID.SEARCH_DIRECTOR:
				try {
					keyword = URLEncoder.encode(directorStr, "UTF-8");
					mEpgController.requestSearchList(keyword, 0,
							goOnSearchEndNum, TAG_DIRECTOR+DetailDramaActivity.this.toString());
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				break;
			case WindowMessageID.SEARCH_TYPE:
				try {
					keyword = URLEncoder.encode(typeStr, "UTF-8");
					mEpgController.requestSearchList(keyword, 0,
							goOnSearchEndNum, TAG_TYPE+DetailDramaActivity.this.toString());
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
		}
	}

	private Handler mSearchHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			goOnSearch(msg);
		};
	};

	@Override
	public void onClick(View arg0) {
		if (!hasDetailInfo
				|| !(!TextUtils.isEmpty(mName) && !TextUtils.isEmpty(picURL))) {
			return;
		}
		switch (arg0.getId()) {
		case R.id.rl_play_drama:
			showSeris();
			break;
		case R.id.rl_collect_drama:
			doCollect();
			break;

		default:
			break;
		}
	}

	/**
	 * 用于记录recycler_drama获取焦点时，它的上一个焦点按钮是播放还是收藏
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		View curr = rootlLayout.findFocus();
		if (curr != null) {
			if (curr.getId() == R.id.rl_play_drama) {
				oldFocus = rl_play_drama;
			}

			if (curr.getId() == R.id.rl_collect_drama) {
				oldFocus = rl_collect_drama;
			}

			if (curr.getId() == R.id.recycler_drama) {
				if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
					// 按上键，恢复焦点按钮
					oldFocus.requestFocus();
				}
			}
		}

		return super.dispatchKeyEvent(event);
	}

	/**
	 * 此处在未适配的盒子切割图片时采用ratio进行切割。
	 */
	private void cutThePhoto() {
		Bitmap background_bmp = BitmapFactory.decodeResource(getResources(),
				R.drawable.background);
		double widthRation = Double.valueOf(background_bmp.getWidth())/1920.0;
		double heightRation = Double.valueOf(background_bmp.getHeight())/1080.0;
		Bitmap overlap_left_bmp = Bitmap.createBitmap(background_bmp,
				Double.valueOf(widthRation * 66).intValue(),
				Double.valueOf(heightRation * 654).intValue(),
				Double.valueOf(widthRation * 12).intValue(),
				Double.valueOf(heightRation * 351).intValue());
		Bitmap overlap_right_bmp = Bitmap.createBitmap(background_bmp, Double
				.valueOf(widthRation * 1844).intValue(),
				Double.valueOf(heightRation * 654).intValue(),
				Double.valueOf(widthRation * 12).intValue(),
				Double.valueOf(heightRation * 351).intValue());
		iv_left.setScaleType(ScaleType.FIT_XY);
		iv_right.setScaleType(ScaleType.FIT_XY);
		iv_left.setImageBitmap(overlap_left_bmp);
		iv_right.setImageBitmap(overlap_right_bmp);
	}

	ArrayList<ChannelItem> channels = null;

	@SuppressWarnings("unchecked")
	private void getChannels() {
		channels = (ArrayList<ChannelItem>) Utils.readObject(this,
				Utils.getVersionCode(this) + "");
	}

}
