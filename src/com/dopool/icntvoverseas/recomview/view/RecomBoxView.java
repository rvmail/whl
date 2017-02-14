package com.dopool.icntvoverseas.recomview.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dopool.icntvoverseas.R;
import com.dopool.icntvoverseas.utils.ImageManager;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import dopool.cntv.recommendation.model.RecomBoxData;
import dopool.cntv.recommendation.model.RecomBoxLayout;

/**
 * 对应Model——RecomBoxLayout 和 Data
 * 
 * 
 * @author VicHan
 * 
 */
public class RecomBoxView extends RelativeLayout {

	private RecomBoxLayout mRecommendItem;

	private ImageView mImageView;

	private TextView mTextView;

	private RecomBoxData mRecomBoxData;

	public RecomBoxView(Context context, RecomBoxLayout item) {
		super(context);
		setmRecommendItem(item);
		initImageView(context);
		initTextView(context);
	}

	@SuppressLint("NewApi")
	private void initImageView(Context context) {
		mImageView = new ImageView(context);
		mImageView.setBackgroundResource(R.drawable.newtv_default);
		mImageView.setScaleType(ScaleType.FIT_XY);
		RelativeLayout.LayoutParams rl_iv = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		this.addView(mImageView, rl_iv);
	}

	private void initTextView(Context context) {
		mTextView = new TextView(context);
		RelativeLayout.LayoutParams rl_tv = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		rl_tv.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
		rl_tv.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		rl_tv.setMargins(9, 9, 9, 9);
		mTextView.setVisibility(INVISIBLE);
		this.addView(mTextView, rl_tv);
		mTextView.bringToFront();
	}

	/**
	 * @return the mRecommenditem
	 */
	public RecomBoxLayout getmRecommendItem() {
		return mRecommendItem;
	}

	/**
	 * @param mRecommenditem
	 *            the mRecommenditem to set
	 */
	public void setmRecommendItem(RecomBoxLayout recommenditem) {
		mRecommendItem = recommenditem;
	}

	/**
	 * @return the mRecomBoxData
	 */
	public RecomBoxData getRecomBoxData() {
		return mRecomBoxData;
	}

	/**
	 * 设置数据，同时更新View
	 * 
	 * @param mRecomBoxData
	 *            the mRecomBoxData to set
	 */
	public void setRecomBoxData(RecomBoxData recomBoxData) {
		if (recomBoxData == null)
			return;
		mRecomBoxData = recomBoxData;
		updateData();
	}

	/**
	 * 刷新数据
	 */
	private void updateData() {
		ImageManager imageManager = ImageManager.getInstance();
		
		if (!TextUtils.isEmpty(mRecomBoxData.getImageUrl())) {
			// 更新图片 注意默认图片
			imageManager.getImageLoader().displayImage(
					mRecomBoxData.getImageUrl(), mImageView,
					imageManager.getOptions(), new ImageLoadingListener() {

						@Override
						public void onLoadingStarted(String arg0, View arg1) {

						}

						@Override
						public void onLoadingFailed(String arg0, View arg1,
								FailReason arg2) {
							if (RecomBoxView.this.mRecommendItem
									.isDefaultFocus()) {
								RecomBoxView.this.requestFocus();
							}
							mImageView.bringToFront();
						}

						@Override
						public void onLoadingComplete(String arg0, View arg1,
								Bitmap arg2) {
							if (RecomBoxView.this.mRecommendItem
									.isDefaultFocus()) {
								RecomBoxView.this.requestFocus();
							}
							mImageView.bringToFront();
						}

						@Override
						public void onLoadingCancelled(String arg0, View arg1) {

						}
					});
		}
		
		// 判断是否显示TextView
		if (mRecomBoxData.isShowTitle()) {
			mTextView.setVisibility(VISIBLE);
			// FIXME 目前接口没有对应的字段
			mTextView.setText(mRecomBoxData.getId());
		} else {
			mTextView.setVisibility(INVISIBLE);
		}

	}

}
