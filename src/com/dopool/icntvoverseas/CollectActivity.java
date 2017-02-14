package com.dopool.icntvoverseas;

import java.util.ArrayList;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dopool.icntvoverseas.model.ParameterConstant;
import com.dopool.icntvoverseas.utils.ImageManager;
import com.dopool.icntvoverseas.view.boderview.BorderView;
import com.dopool.icntvoverseas.view.focusrecyclerview.CollectFocusRecyclerView;
import com.dopool.icntvoverseas.view.focusrecyclerview.FocusRecyclerView;

import dopool.cntv.base.LocalizedInfo;
import dopool.controller.CollectionController;
import dopool.controller.CollectionController.CollectionListener;

public class CollectActivity extends BaseActivity implements CollectionListener {

	private static final String TAG = CollectActivity.class.getSimpleName();

	private CollectionController cc;
	private ArrayList<LocalizedInfo> data = new ArrayList<LocalizedInfo>();

	private TextView emptyView;
	private TextView title;
	private CollectFocusRecyclerView recyclerView;
	private ImageView down;
	private ImageView up;
	private int position;// 标记进入详情页的位置
	private int rowNumber;// 标记进入详情页时当前item在当前页面第几行，1或者2

	public int totalItem = 0;
	public TextView count;
	private BorderView borderView;

	@Override
	protected void onResume() {
		cc = CollectionController.init(this);
		cc.registCollectionListener(this);
		cc.queryAll(TAG);
		super.onResume();
	}

	@Override
	protected void onPause() {
		cc.unRegistCollectionListener(this);
		super.onPause();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_collect);
		setTitle();
		setGridView();

		// 添加边框
		borderView = new BorderView(this);
		borderView.getEffect().setMargin(
				(int) getResources().getDimension(R.dimen.px6),
				(int) getResources().getDimension(R.dimen.px6));
		borderView.setBackgroundResource(R.drawable.frame);
		borderView.attachTo(recyclerView);

	}

	private void setGridView() {
		count = (TextView) findViewById(R.id.count);
		down = (ImageView) findViewById(R.id.down);
		up = (ImageView) findViewById(R.id.up);
		emptyView = (TextView) findViewById(R.id.empty);
		recyclerView = (CollectFocusRecyclerView) findViewById(R.id.collect_gv);
		recyclerView.setItemSelected(0);
		recyclerView.setControll(up, down);
		recyclerView.addItemDecoration(new MyDecoration(CollectActivity.this,
				MyDecoration.VERTICAL_LIST));
		recyclerView.setHasFixedSize(true);
		GridLayoutManager manager = new GridLayoutManager(this,
				CollectFocusRecyclerView.NUM_COLUMNS);
		recyclerView.setLayoutManager(manager);
		recyclerView.setAdapter(new MyAdapter());
		recyclerView
				.setOnItemClickListener(new FocusRecyclerView.OnItemClickListener() {

					@Override
					public void onItemClicked(int position) {
						CollectActivity.this.position = position;
						int firstVisibleItemPosition = recyclerView
								.getFirstVisibleItemPosition();
						if (position - firstVisibleItemPosition < CollectFocusRecyclerView.NUM_COLUMNS) {
							rowNumber = 1;
						} else {
							rowNumber = 2;
						}
						LocalizedInfo info = data.get(position);
						Intent intent = new Intent(CollectActivity.this,
								DetailDramaActivity.class);
						intent.putExtra(ParameterConstant.SERISID, info.getId());
						startActivity(intent);
					}
				});
	}

	private void setTitle() {
		String TITLE = getString(R.string.str_favorite);
		title = (TextView) findViewById(R.id.title);
		title.setText(TITLE);
	}

	// 更新数据
	private void update() {
		if (recyclerView != null) {
			recyclerView.setAdapter(new MyAdapter());// 同历史每次重新设置adapter，recyclerview内部bug
			int first = 0;
			if (position != 0 && rowNumber != 0) {
				if (data.size() < position + 1) {
					position = data.size() - 1;
					if (data.size() > CollectFocusRecyclerView.NUM_COLUMNS)
						rowNumber = 2;
				}
				int page = 0;
				if (rowNumber == 1) {
					page = position / CollectFocusRecyclerView.NUM_COLUMNS;
				} else {
					page = (position - CollectFocusRecyclerView.NUM_COLUMNS)
							/ CollectFocusRecyclerView.NUM_COLUMNS;
				}
				first = CollectFocusRecyclerView.NUM_COLUMNS * page;
				int last = first + CollectFocusRecyclerView.NUM_COLUMNS * 2 - 1;
				recyclerView.setPositions(first, last);
				Log.i("ly", "position=" + position + ";page=" + page
						+ ";first=" + first + ";last=" + last);
			} else {
				// 避免盒子性能不佳时，造成之前记录的position，rowNumber丢失，如果丢失则直接回到开始位置
				recyclerView.setPositions(0,
						CollectFocusRecyclerView.NUM_COLUMNS * 2 - 1);
			}
			recyclerView.controllIndicator();
			((GridLayoutManager) recyclerView.getLayoutManager())
					.scrollToPositionWithOffset(first, 0);
			recyclerView.setSelection(position, position,
					FocusRecyclerView.PLACEHOLDER_POSITION);
		}
	}

	// 收藏回调
	@Override
	public void onCollectionQueryAll(ArrayList<LocalizedInfo> arg0, String arg1) {
		if (data.size() != 0 && data.size() == arg0.size()) {
			return;
		}
		data = arg0;
		totalItem = data.size();
		if (data.size() > 0) {
			emptyView.setVisibility(View.GONE);
			recyclerView.setVisibility(View.VISIBLE);
			update();
		} else {
			recyclerView.setVisibility(View.GONE);
			borderView.setVisibility(View.GONE);
			emptyView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onCollectionQueryById(LocalizedInfo arg0, String arg1) {

	}

	@Override
	public void onCollectionQueryByPaging(
			ArrayList<LocalizedInfo> listCollectionInfo, String tag) {

	}

	public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ItemHolder> {

		class ItemHolder extends ViewHolder {
			public ItemHolder(View view) {
				super(view);
				name = (TextView) view.findViewById(R.id.grid_text);
				pic = (ImageView) view.findViewById(R.id.grid_item);
			}

			TextView header;
			TextView name;
			ImageView pic;
		}

		@Override
		public int getItemCount() {
			return data.size();
		}

		@Override
		public void onBindViewHolder(ItemHolder holder, int position) {
			holder.name.setText(data.get(position).getName());
			if (!TextUtils.isEmpty(data.get(position).getPicUrl())) {
				ImageManager
						.getInstance()
						.getImageLoader()
						.displayImage(data.get(position).getPicUrl(),
								holder.pic,
								ImageManager.getInstance().getOptions());
			}
		}

		@Override
		public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater mInflater = LayoutInflater
					.from(CollectActivity.this);
			ItemHolder holder = null;
			holder = new ItemHolder(mInflater.inflate(
					R.layout.grid_item_recyclerview, parent, false));
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
