package com.dopool.icntvoverseas.view.focusrecyclerview;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView.Recycler;
import android.support.v7.widget.RecyclerView.State;
import android.util.AttributeSet;

public class WrapContentLinearLayoutManager extends LinearLayoutManager{

	public WrapContentLinearLayoutManager(Context context) {
		super(context);
	}

	public WrapContentLinearLayoutManager(Context context, AttributeSet attrs,
		int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public WrapContentLinearLayoutManager(Context context, int orientation,
			boolean reverseLayout) {
		super(context, orientation, reverseLayout);
	}
	
	@Override
	public void onLayoutChildren(Recycler recycler, State state) {
	    try {
	        super.onLayoutChildren(recycler, state);
	    } catch (IndexOutOfBoundsException e) {
	    }
	}

}
