package com.dopool.icntvoverseas.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;

import com.dopool.icntvoverseas.PlayerActivity;

public class CustomSeekBar extends SeekBar{

	private PlayerActivity activity;
	public CustomSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public CustomSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		activity = (PlayerActivity) context;
	}

	public CustomSeekBar(Context context) {
		super(context);
	}

	@Override
	public View focusSearch(int direction) {
		if(direction == View.FOCUS_DOWN){
			return activity.rv_series;
		}
		return null;
	}
	
	
}
