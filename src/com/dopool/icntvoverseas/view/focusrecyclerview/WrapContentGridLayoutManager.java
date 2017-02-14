package com.dopool.icntvoverseas.view.focusrecyclerview;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

public class WrapContentGridLayoutManager  extends GridLayoutManager{


	public WrapContentGridLayoutManager(Context context, int spanCount,
			int orientation, boolean reverseLayout) {
		super(context, spanCount, orientation, reverseLayout);
	}

	public WrapContentGridLayoutManager(Context context, int spanCount) {
		super(context, spanCount);
	}

	public WrapContentGridLayoutManager(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}
	

		@Override
	public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
	    try {
	        super.onLayoutChildren(recycler, state);
	    } catch (IndexOutOfBoundsException e) {
	    }
	}
	
}
