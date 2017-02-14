package com.dopool.icntvoverseas.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dopool.icntvoverseas.R;
import com.dopool.icntvoverseas.adapter.PlaySerisAdapter.SerisHolder;
import com.dopool.icntvoverseas.view.focusrecyclerview.FocusRecyclerView;

import dopool.cntv.base.MovieItem;

/**
 * 播放页剧集的Adapter
 */
public class PlaySerisAdapter extends RecyclerView.Adapter<SerisHolder> {
	private Context mContext;
	private List<MovieItem> mList;
	private int current;

	private FocusRecyclerView recyclerView;

	public PlaySerisAdapter(Context mContext, List<MovieItem> mList, int current) {
		this.mContext = mContext;
		this.mList = mList;
		this.current = current;
	}


	public void setReycycler(FocusRecyclerView recyclerView) {
		this.recyclerView = recyclerView;
	}

	@Override
	public int getItemCount() {
		return mList != null ? mList.size() : 0;
	}

	@Override
	public void onBindViewHolder(SerisHolder viewHolder, final int position) {
		String seris = mList.get(position).getName();
		viewHolder.tv_seris.setText(seris);
		if (current == position) {
			viewHolder.img_item_play
					.setBackgroundResource(R.drawable.btn_current_series);
		} else {
			viewHolder.img_item_play.setBackgroundResource(Color.TRANSPARENT);
		}

		if (position == recyclerView.getSelection()) {
			viewHolder.itemView.setSelected(true);
		}
	}

	@Override
	public SerisHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(mContext).inflate(
				R.layout.item_series_play, parent, false);
		return new SerisHolder(view);
	}

	static class SerisHolder extends RecyclerView.ViewHolder {
		TextView tv_seris;
		ImageView img_item_play;

		public SerisHolder(View itemView) {
			super(itemView);

			tv_seris = (TextView) itemView.findViewById(R.id.tv_item_play);
			img_item_play = (ImageView) itemView
					.findViewById(R.id.img_item_play);
		}

	}

}
