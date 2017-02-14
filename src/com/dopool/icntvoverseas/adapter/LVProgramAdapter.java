package com.dopool.icntvoverseas.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dopool.icntvoverseas.R;

import dopool.liveepg.bean.ProgramBill;

/**
 * 直播节目列表（右侧listView）Adapter
 */
public class LVProgramAdapter extends BaseAdapter {

	private static final String TAG = LVProgramAdapter.class.getSimpleName();

	private static boolean debug = true;

	private List<ProgramBill> programList;
	private LayoutInflater mLayoutInflater;
	private int currentPlayPosition;
	private Context mContext;

	public LVProgramAdapter(Context mContext, List<ProgramBill> programList) {
		this.mContext = mContext;
		this.programList = programList;
		mLayoutInflater = LayoutInflater.from(mContext);
	}
	
	public void setData(List<ProgramBill> programList){
		this.programList = programList;
	}

	@Override
	public int getCount() {
		return programList != null ? programList.size() : 0;
	}

	@Override
	public Object getItem(int pos) {
		return programList != null ? programList.get(pos) : null;
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}
	
	public void setCurrentPlayPosititon(int position){
		currentPlayPosition = position;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		ProgramHolder mHolder = null;
		if (convertView == null) {
			mHolder = new ProgramHolder();
			convertView = mLayoutInflater.inflate(R.layout.item_list_program,
					parent, false);
			mHolder.iv_item_program = (ImageView) convertView
					.findViewById(R.id.iv_item_program);
			mHolder.tv_item_program_time = (TextView) convertView
					.findViewById(R.id.tv_item_program_time);
			mHolder.tv_item_program_title = (TextView) convertView
					.findViewById(R.id.tv_item_program_title);

			convertView.setTag(mHolder);
		} else {
			mHolder = (ProgramHolder) convertView.getTag();
		}
		if(pos == currentPlayPosition){
//			mHolder.tv_item_program_title.setTextColor(mContext.getResources().getColor(R.color.dopool_white));
			mHolder.iv_item_program.setVisibility(View.VISIBLE);
		}else{
//			mHolder.tv_item_program_title.setTextColor(mContext.getResources().getColor(R.color.live_text_color));
			mHolder.iv_item_program.setVisibility(View.INVISIBLE);
		}
		mHolder.tv_item_program_time.setText(programList.get(pos).getStarttime() +" - "+ programList.get(pos).getEndtime());
		mHolder.tv_item_program_title
				.setText(programList.get(pos).getProname());
		return convertView;
	}

	static class ProgramHolder {
		ImageView iv_item_program;
		TextView tv_item_program_time, tv_item_program_title;
	}

}
