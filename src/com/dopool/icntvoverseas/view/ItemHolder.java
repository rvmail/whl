package com.dopool.icntvoverseas.view;

import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dopool.icntvoverseas.R;

public class ItemHolder extends ViewHolder{
	public TextView name;
	public ImageView pic;
	public ItemHolder(View view) {
		super(view);
		name = (TextView) view.findViewById(R.id.grid_text);
		pic = (ImageView) view.findViewById(R.id.grid_item);
	}
}