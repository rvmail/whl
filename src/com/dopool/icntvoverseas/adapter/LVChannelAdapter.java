package com.dopool.icntvoverseas.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dopool.icntvoverseas.R;

import dopool.liveepg.bean.LiveEpgChannel;

/**
 * 直播频道列表（左侧listView）Adapter
 */
public class LVChannelAdapter extends BaseAdapter {

	private static final String TAG = LVChannelAdapter.class.getSimpleName();

	private static boolean debug = true;

	private List<LiveEpgChannel> guideList;
	private LayoutInflater mLayoutInflater;

	public LVChannelAdapter(Context mContext, List<LiveEpgChannel> guideList) {
		this.guideList = guideList;
		mLayoutInflater = LayoutInflater.from(mContext);
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
			convertView = mLayoutInflater.inflate(R.layout.item_list_channel,
					parent, false);
			mHolder.rl_item_channel = (RelativeLayout) convertView
					.findViewById(R.id.rl_item_channel);
			mHolder.tv_item_channel = (TextView) convertView
					.findViewById(R.id.tv_item_channel);

			convertView.setTag(mHolder);
		} else {
			mHolder = (GuideHolder) convertView.getTag();
		}
		mHolder.tv_item_channel.setText(guideList.get(pos).getChannelname());
		return convertView;
	}

	static class GuideHolder {
		RelativeLayout rl_item_channel;
		TextView tv_item_channel;
	}

}
