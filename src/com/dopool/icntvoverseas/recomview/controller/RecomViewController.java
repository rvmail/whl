package com.dopool.icntvoverseas.recomview.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.dopool.icntvoverseas.R;
import com.dopool.icntvoverseas.model.ParameterConstant;
import com.dopool.icntvoverseas.recomview.view.DopHorizontalScrollView;
import com.dopool.icntvoverseas.recomview.view.RecomBoxView;
import com.dopool.icntvoverseas.utils.AnimateFactory;

import dopool.cntv.recommendation.model.RecomBoxData;
import dopool.cntv.recommendation.model.RecomBoxLayout;
import dopool.cntv.recommendation.model.RecomPanel;

/**
 * 首页推荐位的controller，实现布局初始化、数据更新、以及动画的添加
 * 
 * @author VicHan
 */
public class RecomViewController {
	private static final String TAG = "RecomViewController";

	private static final boolean DEBUG = false;

	private Context mAppContext;

	private DopHorizontalScrollView mScrollView;

	private RelativeLayout mRelativeLayout;

	private RecomPanel mPanel;

	private List<RecomBoxLayout> mLayoutBoxes;

	private List<RecomBoxView> mBoxViews;

	private int mHeightWeightSum;

	private int mItemPaddingInPixel;

	private int mWeightLengthInPixel;

	private HashMap<String, RecomBoxData> mDataMap;

	private OnClickListener mOnBoxClickListener;

	private Handler mHandler;

	public RecomViewController(Context context,
			DopHorizontalScrollView rootLayout, RecomPanel recomPanel,
			int itemPaddingInPixel) {
		if (context == null || rootLayout == null || recomPanel == null
				|| recomPanel.getLayoutBoxes() == null
				|| recomPanel.getLayoutBoxes().size() == 0
				|| recomPanel.getHeight() <= 0 || itemPaddingInPixel < 0)
			throw new IllegalArgumentException(
					"RecomViewController init failed");
		mAppContext = context.getApplicationContext();

		// 初始化layout
		mScrollView = rootLayout;
		mRelativeLayout = (RelativeLayout) rootLayout
				.findViewById(R.id.relative_layout);
		if (mRelativeLayout != null) {
			mRelativeLayout.removeAllViews();
		}

		initPanelSetting(recomPanel, itemPaddingInPixel);

		initBoxLayout(true);

		mHandler = new Handler();
	}

	private void initPanelSetting(RecomPanel panel, int itemPaddingInPixel) {
		mPanel = panel;
		mLayoutBoxes = panel.getLayoutBoxes();
		mBoxViews = new ArrayList<RecomBoxView>();
		mHeightWeightSum = panel.getHeight();
		mItemPaddingInPixel = itemPaddingInPixel;
	}

	private void initBoxLayout(final boolean isDefault) {
		mRelativeLayout.getViewTreeObserver().addOnPreDrawListener(
				new OnPreDrawListener() {

					@Override
					public boolean onPreDraw() {
						mHandler.removeCallbacks(mUpdateRunable);

						int height = mRelativeLayout.getHeight();

						// 设置padding，用于放大显示
						int padding = (int) (height
								* (ParameterConstant.RECOM_BOX_ENLARGE_FACTOR - 1) / 2);
						mRelativeLayout
								.setPadding(
										(int) (padding
												* ParameterConstant.RECOM_BOX_ASPECT_RATIO + mItemPaddingInPixel),
										padding,
										(int) (padding
												* ParameterConstant.RECOM_BOX_ASPECT_RATIO + mItemPaddingInPixel),
										padding);
						// 不被padding遮挡
						mRelativeLayout.setClipToPadding(false);

						// 自定义方法，用于在不显示FadingEdge时，能够完全显示当前焦点的子view
						mScrollView
								.setCustomizeFadingEdge((int) (padding
										* ParameterConstant.RECOM_BOX_ASPECT_RATIO + mItemPaddingInPixel));

						mWeightLengthInPixel = (height - padding * 2)
								/ mHeightWeightSum;
						if (DEBUG) {
							Log.d(TAG, "-----unitLengthInPixel-----"
									+ mWeightLengthInPixel);
						}

						mHandler.postDelayed(mUpdateRunable, 200);

						mRelativeLayout.getViewTreeObserver()
								.removeOnPreDrawListener(this);
						return false;
					}
				});
	}

	private Runnable mUpdateRunable = new Runnable() {

		@Override
		public void run() {
			mRelativeLayout.removeAllViews();
			mBoxViews.clear();
			for (int i = 0; i < mLayoutBoxes.size(); i++) {
				addRecomBox(mLayoutBoxes.get(i), i);
			}

			// 更新数据
			if (mDataMap != null && mDataMap.size() > 0) {
				updateItemsData();
			}
		}
	};

	private void addRecomBox(final RecomBoxLayout item, int i) {
		
		// 初始化view
		final RecomBoxView recomBox = new RecomBoxView(mAppContext, item);
		
		if (DEBUG) {
			Log.d(TAG, "---addRecomBox---" + item.getId());
			Log.d(TAG,
					"---addRecomBox---recomBox" + recomBox + " ---tag---"
							+ item.getId());
		}
		recomBox.setTag(item.getId());
		// 记录BoxView的引用
		mBoxViews.add(recomBox);

		recomBox.setPadding(mItemPaddingInPixel, mItemPaddingInPixel,
				mItemPaddingInPixel, mItemPaddingInPixel);

		recomBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View paramView) {
				if (mOnBoxClickListener != null) {
					mOnBoxClickListener.onClick(paramView);
				}
			}
		});

		// 初始化大小：宽和高
		RelativeLayout.LayoutParams lp = new LayoutParams(
				(int) (mWeightLengthInPixel * item.getWidthWeight() * ParameterConstant.RECOM_BOX_ASPECT_RATIO),
				mWeightLengthInPixel * item.getHeightWeight());

		// 设置相对位置
		int left = item.getLeftPosition();
		int top = item.getTopPosition();
		lp.setMargins(
				(int) ((left - 1) * mWeightLengthInPixel * ParameterConstant.RECOM_BOX_ASPECT_RATIO),
				(top - 1) * mWeightLengthInPixel, 0, 0);
		// 设置选中效果
		recomBox.setBackgroundResource(R.drawable.catg_selector);
		// 添加view
		mRelativeLayout.addView(recomBox, lp);
		if (!item.isMockData()) {

			recomBox.setFocusable(true);
			recomBox.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View paramView, boolean paramBoolean) {
					if (paramBoolean) {
						AnimateFactory.zoomInView(paramView,
								ParameterConstant.RECOM_BOX_ENLARGE_FACTOR);
						recomBox.bringToFront();
					} else {
						AnimateFactory.zoomOutView(paramView,
								ParameterConstant.RECOM_BOX_ENLARGE_FACTOR);
					}

				}
			});
		}
	}

	public boolean updateLayout(RecomPanel panel, int itemPaddingInPixel) {
		try {
			initPanelSetting(panel, itemPaddingInPixel);
			initBoxLayout(false);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean updateData(HashMap<String, RecomBoxData> dataMap) {
		mDataMap = dataMap;

		if (mDataMap != null && mDataMap.size() > 0) {
			updateItemsData();
			return true;
		}
		return false;
	}

	private void updateItemsData() {
		if (mBoxViews != null) {
			for (int i = 0; i < mBoxViews.size(); i++) {
				try {
					RecomBoxView view = mBoxViews.get(i);
					String tag = (String) view.getTag();
					RecomBoxData boxData = mDataMap.get(tag);
					view.setRecomBoxData(boxData);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}

		}
	}

	/**
	 * @param mOnBoxClickListener
	 *            the mOnBoxClickListener to set
	 */
	public void setOnBoxClickListener(OnClickListener onClickListener) {
		this.mOnBoxClickListener = onClickListener;
	}

	public void removeOnBoxClickListener() {
		this.mOnBoxClickListener = null;
	}

	public void release() {
		removeOnBoxClickListener();
		mAppContext = null;
		mScrollView = null;
		mRelativeLayout.removeAllViews();
		mRelativeLayout = null;
		mPanel = null;
		mLayoutBoxes = null;
		mHeightWeightSum = 0;
		mItemPaddingInPixel = 0;
		mWeightLengthInPixel = 0;
		mDataMap = null;
	}
}
