package com.dopool.icntvoverseas.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

public class CustomListView extends ListView {

	public CustomListView(Context context) {
		super(context);
	}

	public CustomListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CustomListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFocusChanged(boolean gainFocus, int direction,
			Rect previouslyFocusedRect) {
		int lastSelectItem = getSelectedItemPosition();
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
		if (gainFocus) {
			// 获取焦点离开前的selection位置距listview顶端位置，通过setSelectionFromTop方法，避免出现滑动效果。
			View other = getChildAt(lastSelectItem - getFirstVisiblePosition());
			int top = (other == null) ? 0 : other.getTop();
			setSelectionFromTop(lastSelectItem, top);
		}
	}
}
