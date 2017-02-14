package com.dopool.icntvoverseas;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dopool.icntvoverseas.model.ParameterConstant;
import com.dopool.icntvoverseas.utils.ImageManager;
import com.dopool.icntvoverseas.view.KeyBoardControlView;
import com.dopool.icntvoverseas.view.KeyBoardControlView.KeySelectedListener;
import com.dopool.icntvoverseas.view.boderview.BorderBaseEffect;
import com.dopool.icntvoverseas.view.boderview.BorderView;
import com.dopool.icntvoverseas.view.focusrecyclerview.FocusRecyclerView;
import com.dopool.icntvoverseas.view.focusrecyclerview.SearchRecyclerView;
import com.dopool.icntvoverseas.view.focusrecyclerview.SearchRecyclerView.PaginationListener;
import com.dopool.icntvoverseas.view.focusrecyclerview.WrapContentLinearLayoutManager;

import dopool.cntv.base.SearchInfo;
import dopool.cntv.base.SearchResult;
import dopool.controller.EpgController;
import dopool.controller.EpgController.SearchListRequestListener;
import dopool.controller.LogController;

public class SearchActivity extends BaseActivity implements
		KeySelectedListener, SearchListRequestListener, PaginationListener {

	private static final String TAG = SearchActivity.class.getSimpleName();
	private RelativeLayout rl_searchLayout;
	private KeyBoardControlView mKeyBoardControlView;
	private TextView firstView;
	private StringBuffer mSearchStr;
	// 记录上次搜索的关键字，避免一直按enter键重复搜索
	private String latestSearchKey = "";
	private ImageView mDeleteImgvi, mSearchImgvi;
	private TextView mSearchTv;
	private EpgController mEpgController;
	private SearchRecyclerView mSearchResultRecView;
	private LinearLayoutManager mLinearLytManager;
	private TextView emptyView;

	private ArrayList<SearchInfo> datas = new ArrayList<SearchInfo>();
	private MyAdapter adapter = new MyAdapter();
	private BorderBaseEffect effect;

	private static final int REQUEST_SEARCH_PAGE_SIZE = 20;
	private int startNum = 0;
	private int endNum = REQUEST_SEARCH_PAGE_SIZE;

	private FrameLayout fl_loading;
	private int maxLength = 12;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		initData();
		initView();
		setListener();

		// 添加边框
		BorderView borderView = new BorderView(this);
		effect = borderView.getEffect();
		effect.setMargin((int) getResources().getDimension(R.dimen.px6),
				(int) getResources().getDimension(R.dimen.px6));
		borderView.setBackgroundResource(R.drawable.frame);
		borderView.attachTo(mSearchResultRecView);
		// 数据统计：进入搜索页面
		LogController.getInstance(SearchActivity.this).logUpload(
				ParameterConstant.SEARCH, "", TAG);
	}

	private void initData() {
		mEpgController = EpgController.init(this);
		mEpgController.registerSearchListRequestListener(this);
		mSearchStr = new StringBuffer();
		mKeyBoardControlView = new KeyBoardControlView(this);
		mKeyBoardControlView.setKeySelectedListener(this);
	}

	private void initView() {
		fl_loading = (FrameLayout) findViewById(R.id.container_buffering);
		rl_searchLayout = (RelativeLayout) findViewById(R.id.rl_search);
		firstView = (TextView) findViewById(R.id.search_tv_keyboard_abc);
		mDeleteImgvi = (ImageView) findViewById(R.id.search_imgvi_keyboard_delete);
		mSearchImgvi = (ImageView) findViewById(R.id.search_imgvi_keyboard_search);
		mSearchTv = (TextView) findViewById(R.id.search_tv);
		mSearchResultRecView = (SearchRecyclerView) findViewById(R.id.search_recyclerview);
		mSearchResultRecView.setAdapter(adapter);
		mSearchResultRecView.setPaginationListener(this);
		mSearchResultRecView.setHasFixedSize(true);
		mLinearLytManager = new WrapContentLinearLayoutManager(this,
				LinearLayoutManager.HORIZONTAL, false);
		mSearchResultRecView.setLayoutManager(mLinearLytManager);
		mSearchResultRecView.setAdapter(adapter);
		mSearchResultRecView
				.setOnItemClickListener(new FocusRecyclerView.OnItemClickListener() {

					@Override
					public void onItemClicked(int position) {
						SearchInfo info = datas.get(position);
						Intent intent = new Intent(SearchActivity.this,
								DetailDramaActivity.class);
						Bundle extras = new Bundle();
						extras.putSerializable(
								ParameterConstant.OBJECT_SERIALIZABLE, info);
						intent.putExtras(extras);
						startActivity(intent);
					}
				});
		if (datas.size() == 0) {
			mSearchResultRecView.setVisibility(View.GONE);
		}
		emptyView = (TextView) findViewById(R.id.empty_view);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			if (rl_searchLayout.findFocus() instanceof RecyclerView) {
				firstView.requestFocus();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onKeySelectedListener(String key) {
		if (mSearchStr.toString().length() < maxLength) {
			mSearchStr.append(key);
			mSearchTv.setText(mSearchStr);
		}
	}

	private void setListener() {
		mDeleteImgvi.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mSearchStr.length() > 0) {
					mSearchStr.deleteCharAt(mSearchStr.length() - 1);
					mSearchTv.setText(mSearchStr);
				}
			}
		});

		mSearchImgvi.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (TextUtils.isEmpty(mSearchStr.toString())) {
					return;
				}
				if (latestSearchKey.equals(mSearchStr.toString())) {
					return;
				}

				if (fl_loading != null) {
					fl_loading.setVisibility(View.VISIBLE);
				}

				startNum = 0;
				datas.clear();
				adapter.notifyDataSetChanged();
				mSearchResultRecView.setItemSelected(0);
				latestSearchKey = new String(mSearchStr.toString());
				mEpgController.requestSearchList(mSearchStr.toString(),
						startNum, REQUEST_SEARCH_PAGE_SIZE, TAG);
				// 数据统计：搜索keyword
				LogController.getInstance(SearchActivity.this).logUpload(
						ParameterConstant.SEARCH, mSearchStr.toString(), TAG);
			}
		});

	}

	@Override
	protected void onDestroy() {
		mEpgController.unRegisterSearchListRequestListener(this);
		super.onDestroy();
	}

	@Override
	public void onSearchListResult(SearchResult searchResult) {
		if (fl_loading != null) {
			fl_loading.setVisibility(View.GONE);
		}
		if (searchResult != null
				&& searchResult.getRequestTag().getTag().equals(TAG)) {
			if (searchResult.getSearchInfoList().size() > 0) {
				datas.addAll(searchResult.getSearchInfoList());
				isFinal = false;
				if (emptyView.getVisibility() == View.VISIBLE) {
					emptyView.setVisibility(View.GONE);
				}
				if (mSearchResultRecView.getVisibility() == View.GONE) {
					mSearchResultRecView.setVisibility(View.VISIBLE);
				}
				if (startNum != 0) {
					adapter.notifyItemRangeInserted(datas.size(),
							datas.size() - 1);
				} else {
					adapter.notifyDataSetChanged();
					mSearchResultRecView.scrollToPosition(0);
				}
				// 说明已经到达最后一页
				if (searchResult.getSearchInfoList().size() < REQUEST_SEARCH_PAGE_SIZE) {
					isFinal = true;
				}
				startNum += endNum;
			} else if (startNum == 0
					&& searchResult.getSearchInfoList().size() == 0) {
				mSearchResultRecView.setVisibility(View.GONE);
				emptyView.setVisibility(View.VISIBLE);
			}
		}
	}

	public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ItemHolder> {

		public class ItemHolder extends ViewHolder {
			TextView header;
			TextView name;
			ImageView pic;

			public ItemHolder(View view) {
				super(view);
				name = (TextView) view.findViewById(R.id.grid_text);
				pic = (ImageView) view.findViewById(R.id.grid_item);
			}
		}

		@Override
		public int getItemCount() {
			return datas.size();
		}

		@Override
		public void onBindViewHolder(ItemHolder holder, int position) {
			holder.name.setText(datas.get(position).getName());
			if (!TextUtils.isEmpty(datas.get(position).getPicurl())) {
				ImageManager
						.getInstance()
						.getImageLoader()
						.displayImage(datas.get(position).getPicurl(),
								holder.pic,
								ImageManager.getInstance().getOptions());
			}
		}

		@Override
		public ItemHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
			LayoutInflater mInflater = LayoutInflater.from(SearchActivity.this);
			ItemHolder holder = new ItemHolder(mInflater.inflate(
					R.layout.grid_item_recyclerview, null));
			return holder;
		}
	}

	private boolean isFinal = false;

	@Override
	public void onPagination() {
		if (isFinal) {
			return;
		}
		mEpgController.requestSearchList(mSearchStr.toString(), startNum,
				endNum, TAG);
	}

}
