package com.dopool.icntvoverseas.recomview.view;

import java.util.HashMap;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.dopool.icntvoverseas.R;
import com.dopool.icntvoverseas.recomview.controller.RecomViewController;
import com.dopool.icntvoverseas.recomview.test.RecomPanelGenerator;

import dopool.cntv.recommendation.model.RecomBoxData;
import dopool.cntv.recommendation.model.RecomPanel;

/**
 * 通过此Fragment封装推荐位模块
 * 
 * @author VicHan
 */

public class RecommendPanelFragment extends Fragment {

	private DopHorizontalScrollView mDopHorizontalScrollView;

	private RecomViewController mRecomViewController;

	private RecomPanel mRecomPanel;

	private OnClickListener mOnBoxClickListener;

	public RecommendPanelFragment() {
	}

	public RecommendPanelFragment(RecomPanel panel) {
		mRecomPanel = panel;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootview = inflater.inflate(R.layout.fragment_recommend,
				container, false);
		mDopHorizontalScrollView = (DopHorizontalScrollView) rootview
				.findViewById(R.id.horizontal_scroll_view);

		return rootview;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mRecomViewController = new RecomViewController(getActivity(),
				mDopHorizontalScrollView,
				RecomPanelGenerator.generateMockData(2), 6);
		mRecomViewController.setOnBoxClickListener(mOnBoxClickListener);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mRecomViewController.release();
		mRecomViewController = null;
		mRecomPanel = null;
	}

	/**
	 * 在Fragment add成功后，动态更改Panel布局
	 * 
	 * @param panel
	 * @param itemPaddingInPixel
	 * @return
	 */
	public boolean updatePanel(RecomPanel panel, int itemPaddingInPixel) {
		if (!this.isAdded()) {
			return false;
		}

		if (mRecomViewController == null) {

		} else {
			mRecomViewController.updateLayout(panel, itemPaddingInPixel);
		}
		return false;
	}

	public void updateBoxData(HashMap<String, RecomBoxData> dataMap) {
		if (mRecomViewController != null) {
			mRecomViewController.updateData(dataMap);
		}
	}

	public void setOnBoxClickListener(OnClickListener onClickListener) {
		mOnBoxClickListener = onClickListener;
	}

	public void removeOnBoxClidkListener() {
		if (mRecomViewController != null) {
			mRecomViewController.removeOnBoxClickListener();
		}
	}
}
