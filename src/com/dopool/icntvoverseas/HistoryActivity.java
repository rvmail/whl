package com.dopool.icntvoverseas;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.State;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dopool.icntvoverseas.entity.GridItem;
import com.dopool.icntvoverseas.model.ParameterConstant;
import com.dopool.icntvoverseas.utils.ImageManager;
import com.dopool.icntvoverseas.view.boderview.BorderView;
import com.dopool.icntvoverseas.view.focusrecyclerview.GridFocusRecyclerView;
import com.dopool.icntvoverseas.view.focusrecyclerview.WrapContentGridLayoutManager;

import dopool.cntv.base.LocalizedInfo;
import dopool.controller.HistoryController;
import dopool.controller.HistoryController.HistoryListener;

/**
 * @author ly
 */
public class HistoryActivity extends BaseActivity implements HistoryListener {

	private static final String TAG = HistoryActivity.class.getSimpleName();
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");

	private HistoryController hc;
	private ArrayList<GridItem> data = new ArrayList<GridItem>();
	private ArrayList<GridItem> datas = new ArrayList<GridItem>();

	private TextView title;
	private GridFocusRecyclerView mRecyclerView;
	private ImageView down;
	private ImageView up;
	private TextView empty;

	@Override
	protected void onResume() {
		hc = HistoryController.init(this);
		hc.registHistoryListener(this);
		hc.queryAll(TAG);

		super.onResume();
	}

	@Override
	protected void onPause() {
		hc.unRegistHistoryListener(this);
		super.onPause();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		setTitle();
		setRecyclerView();

		BorderView borderView = new BorderView(this);
		borderView.setBackgroundResource(R.drawable.frame);
		borderView.getEffect().setMargin(
				(int) getResources().getDimension(R.dimen.px6),
				(int) getResources().getDimension(R.dimen.px6));
		borderView.attachTo(mRecyclerView);
	}

	// init RecyclerView
	private void setRecyclerView() {
		down = (ImageView) findViewById(R.id.down);
		up = (ImageView) findViewById(R.id.up);
		empty = (TextView) findViewById(R.id.empty);
		mRecyclerView = (GridFocusRecyclerView) findViewById(R.id.history_gv);
		mRecyclerView.setControll(up, down);
		mRecyclerView.addItemDecoration(new MyDecoration(HistoryActivity.this,
				MyDecoration.VERTICAL_LIST));
		mRecyclerView.setHasFixedSize(true);
		// manager把绘制区域分成8份，columns is 7，第一列占2份
		GridLayoutManager manager = new WrapContentGridLayoutManager(this, 8);
		manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
			@Override
			public int getSpanSize(int position) {
				if (position % GridFocusRecyclerView.NUM_COLUMNS == 0) {
					return 2;
				} else {
					return 1;
				}
			}
		});
		mRecyclerView.setLayoutManager(manager);
		mRecyclerView.setAdapter(new MyAdapter());
		mRecyclerView
				.setOnItemClickListener(new GridFocusRecyclerView.OnItemClickListener() {

					@Override
					public void onItemClicked(int position) {
						LocalizedInfo info = datas.get(position)
								.getLocalizedInfo();
						Intent intent = new Intent(HistoryActivity.this,
								DetailDramaActivity.class);
						intent.putExtra(ParameterConstant.SERISID, info.getId());
						startActivity(intent);
					}
				});
	}

	private void setTitle() {
		String TITLE = getString(R.string.str_history);
		title = (TextView) findViewById(R.id.title);
		title.setText(TITLE);
	}

	// 更新数据
	private void update() {
		if (datas.size() < 8) {
			mRecyclerView
					.setPositions(0, GridFocusRecyclerView.NUM_COLUMNS - 1);
		} else {
			mRecyclerView.setPositions(0,
					GridFocusRecyclerView.NUM_COLUMNS * 2 - 1);
		}
		mRecyclerView.getLayoutManager().scrollToPosition(0);
		mRecyclerView.setItemSelected(0);
		// 因adapter notifydatasetchange 引发未知原因问题，所以暂时每次都new adapter
		mRecyclerView.setAdapter(new MyAdapter());
		mRecyclerView.controllIndicator();
	}

	@Override
	public void onHistoryQueryById(LocalizedInfo historyInfo, String tag) {

	}

	@Override
	public void onHistoryQueryByPaging(
			ArrayList<LocalizedInfo> listHistoryInfo, String tag) {
	}

	@Override
	public void onHistoryQueryAll(ArrayList<LocalizedInfo> listHistoryInfo,
			String tag) {
		// controll show recyclerview or emptyview
		if (listHistoryInfo == null || listHistoryInfo.size() == 0) {
			if (mRecyclerView.getVisibility() == View.VISIBLE) {
				mRecyclerView.setVisibility(View.GONE);
			}
			empty.setVisibility(View.VISIBLE);
			return;
		} else {
			if (mRecyclerView.getVisibility() == View.GONE) {
				mRecyclerView.setVisibility(View.VISIBLE);
			}
			empty.setVisibility(View.GONE);
		}
		// 第一次进入页面data size 0, 不会出现这种情况。点击item进去详情页不播放返回或者播放的第一条历史则不需要更新界面
		if (data.size() > 0
				&& data.get(0).getLocalizedInfo().getId() == listHistoryInfo
						.get(0).getId()) {
			return;
		}
		data.clear();
		datas.clear();
		for (LocalizedInfo info : listHistoryInfo) {
			long time = Long.parseLong(info.getTime());
			try {
				time = formatter.parse(formatter.format(new Date(time)))
						.getTime();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			GridItem item = new GridItem(info, time);
			data.add(item);
		}
		// 对数据按section分组
		LinkedHashMap<String, Integer> pMap = new LinkedHashMap<>();
		int j = 0;
		for (int i = 0; i < data.size(); i++) {
			if ((i != 0 && data.get(i).getSection() != data.get(i - 1)
					.getSection()) || i == data.size() - 1) {
				if (i == data.size() - 1) {
					j++;
					pMap.put(data.get(i).getSection() + "", j);
					break;
				}
				pMap.put(data.get(i - 1).getSection() + "", j);
				j = 1;
			} else {
				j++;
			}
		}
		j = 0;
		// 虚拟数据
		int n = 0;
		Iterator iter = pMap.entrySet().iterator();
		while (iter.hasNext()) {
			datas.add(new GridItem(null, GridItem.HEADER_SECTION));
			Map.Entry entry = (Map.Entry) iter.next();
			Object key = entry.getKey();
			Integer val = (Integer) entry.getValue();
			for (int i = 0; i < val; i++) {
				if (datas.size() % GridFocusRecyclerView.NUM_COLUMNS == 0) {
					datas.add(new GridItem(null,
							GridItem.HEADER_FILLTER_SECTION));
				}
				datas.add(data.get(n));
				n++;
			}
			if (datas.size() % GridFocusRecyclerView.NUM_COLUMNS != 0) {
				int m = GridFocusRecyclerView.NUM_COLUMNS - datas.size()
						% GridFocusRecyclerView.NUM_COLUMNS;
				for (int i = 0; i < m; i++) {
					datas.add(new GridItem(null, GridItem.FILLTER_SECTION));
				}
			}
		}
		update();
	}

	public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ItemHolder> {
		public static final int TYPE_HEADER = 1;
		public static final int TYPE_HEADER_FILLTER = 2;
		public static final int TYPE_FILLTER = 3;
		public static final int TYPE_NORMAL = 4;

		public static final int SECTION_HEADER_FILLTER = 1;
		public static final int SECTION_HEADER = 2;

		class ItemHolder extends ViewHolder {
			public ItemHolder(View view, int type) {
				super(view);
				switch (type) {
				case TYPE_HEADER:
					header = (TextView) view.findViewById(R.id.text);
					break;
				case TYPE_NORMAL:
					name = (TextView) view.findViewById(R.id.grid_text);
					pic = (ImageView) view.findViewById(R.id.grid_item);
					break;
				}
			}

			TextView header;
			TextView name;
			ImageView pic;
		}

		@Override
		public int getItemViewType(int position) {
			if (position % GridFocusRecyclerView.NUM_COLUMNS == 0) {
				if (position > 0
						&& datas.get(position).getSection() == SECTION_HEADER_FILLTER) {
					return TYPE_HEADER_FILLTER;// 日期占位item
				} else if (position == 0
						|| datas.get(position).getSection() == SECTION_HEADER) {
					return TYPE_HEADER;// 日期item
				}
			}
			if (datas.get(position).getSection() == 0) {
				return TYPE_FILLTER;// 内容占位item
			}
			return TYPE_NORMAL;// 内容正常
		}

		@Override
		public int getItemCount() {
			return datas.size();
		}

		@Override
		public void onBindViewHolder(ItemHolder holder, int position) {
			if (getItemViewType(position) == TYPE_HEADER) {
				holder.header.setText(formatter.format(new java.util.Date(datas
						.get(position + 1).getSection())));
			}
			if (getItemViewType(position) == TYPE_NORMAL) {
				holder.name.setText(datas.get(position).getLocalizedInfo()
						.getName());
				if (!TextUtils.isEmpty(datas.get(position).getLocalizedInfo()
						.getPicUrl())) {
					ImageManager
							.getInstance()
							.getImageLoader()
							.displayImage(
									datas.get(position).getLocalizedInfo()
											.getPicUrl(), holder.pic,
									ImageManager.getInstance().getOptions());
				}

			}
		}

		@Override
		public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater mInflater = LayoutInflater
					.from(HistoryActivity.this);
			ItemHolder holder = null;
			if (viewType == TYPE_FILLTER) {
				holder = new ItemHolder(mInflater.inflate(R.layout.fillter,
						parent, false), TYPE_FILLTER);
			}
			if (viewType == TYPE_NORMAL) {
				holder = new ItemHolder(mInflater.inflate(
						R.layout.grid_item_recyclerview, parent, false),
						TYPE_NORMAL);
			}
			if (viewType == TYPE_HEADER) {
				holder = new ItemHolder(mInflater.inflate(R.layout.item,
						parent, false), TYPE_HEADER);
			}
			if (viewType == TYPE_HEADER_FILLTER) {
				holder = new ItemHolder(mInflater.inflate(
						R.layout.item_fillter, parent, false),
						TYPE_HEADER_FILLTER);
			}
			return holder;
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
			if (mOrientation == VERTICAL_LIST) {
				outRect.set(0, 0, 0,
						getResources().getDimensionPixelSize(R.dimen.px30));
			}
		}

	}

}
