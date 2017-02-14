package com.dopool.icntvoverseas.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dopool.icntvoverseas.DramaListActivity;
import com.dopool.icntvoverseas.R;

import dopool.cntv.base.CategoryItem;

/**
 * 二级列表页左侧listView的Adapter
 */
public class LVGuideAdapter extends BaseAdapter {

	private static final String TAG = LVGuideAdapter.class.getSimpleName();

	private List<CategoryItem> guideList;
	private Context mContext;
	private LayoutInflater mLayoutInflater;

	private int mPreviousFocus;

	private boolean gainFocus = false;

	public LVGuideAdapter(Context mContext) {
		this.mContext = mContext;
		guideList = new ArrayList<CategoryItem>();
		mLayoutInflater = LayoutInflater.from(mContext);
	}

	public void setData(List<CategoryItem> guideList) {
		this.guideList = guideList;
	}

	@Override
	public int getCount() {
		return guideList != null ? guideList.size() : 0;
	}

	@Override
	public Object getItem(int pos) {
		return guideList != null ? guideList.get(pos) : null;
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		GuideHolder mHolder = null;
		if (convertView == null) {
			mHolder = new GuideHolder();
			convertView = mLayoutInflater.inflate(R.layout.item_list_guide,
					parent, false);
			mHolder.tv_item_guide = (TextView) convertView
					.findViewById(R.id.tv_item_guide);

			convertView.setTag(mHolder);
		} else {
			mHolder = (GuideHolder) convertView.getTag();
		}

		this.mPreviousFocus = DramaListActivity.select_item;

		if (mPreviousFocus == pos) {
			mHolder.tv_item_guide.setTextSize(TypedValue.COMPLEX_UNIT_PX,
					mContext.getResources().getDimension(R.dimen.px55));
			mHolder.tv_item_guide.setTextColor(mContext.getResources()
					.getColor(R.color.text_rb_movie_focused));
			if (gainFocus) {
				mHolder.tv_item_guide
						.setBackgroundResource(R.drawable.rb_focused);
			} else {
				mHolder.tv_item_guide
						.setBackgroundResource(R.drawable.bg_guide_item_normal);
			}
		} else {
			mHolder.tv_item_guide
					.setBackgroundResource(R.drawable.bg_guide_item_normal);
			mHolder.tv_item_guide.setTextSize(TypedValue.COMPLEX_UNIT_PX,
					mContext.getResources().getDimension(R.dimen.px40));
			mHolder.tv_item_guide.setTextColor(mContext.getResources()
					.getColor(R.color.text_rb_movie_normal));
		}

		mHolder.tv_item_guide.setText(guideList.get(pos).getTitle());
		return convertView;
	}

	static class GuideHolder {
		TextView tv_item_guide;
	}

	public boolean isGainFocus() {
		return gainFocus;
	}

	public void setGainFocus(boolean gainFocus) {
		this.gainFocus = gainFocus;
	}
}
