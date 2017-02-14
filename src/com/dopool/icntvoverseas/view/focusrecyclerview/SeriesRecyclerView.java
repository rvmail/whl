package com.dopool.icntvoverseas.view.focusrecyclerview;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import com.dopool.icntvoverseas.PlayerActivity;
import com.dopool.icntvoverseas.adapter.PlaySerisAdapter;
/**
 * 此recyclerview用于播放界面
 * @author ly
 */
public class SeriesRecyclerView extends FocusRecyclerView{

	private int NUMBER_PRE_SCREEN = 7;
	private PlayerActivity activity;
	private LinearLayoutManager manager;
	private PlaySerisAdapter mAdapter;
	
	
	public SeriesRecyclerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		activity = (PlayerActivity) context;
	}
	
	@Override
	public View focusSearch(int direction) {
		if(direction == View.FOCUS_DOWN){
			return activity.mSeekBar;
		}
		return null;
	}
	
	@Override
	public void setLayoutManager(LayoutManager layout) {
		super.setLayoutManager(layout);
		manager = (LinearLayoutManager) layout;
	}
	
	@Override
	public void setAdapter(Adapter adapter) {
		super.setAdapter(adapter);
		mAdapter = (PlaySerisAdapter) adapter;
	}
	
	int firstPosition = 0;
	public void setPosition(int firstPosition){
		this.firstPosition = firstPosition;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
			if (getSelection()==0) {
				return true;
			} else {
				if (getSelection()==firstPosition) {
					firstPosition -= 1;
					manager.scrollToPositionWithOffset(firstPosition, 0);
				}
				setSelection(getSelection(), getSelection()-1, FocusRecyclerView.PLACEHOLDER_POSITION);
				return true;
			}
		}else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
			if (getSelection()==mAdapter.getItemCount()-1) {
				return true;
			}else{
				if (getSelection() + 1>firstPosition+NUMBER_PRE_SCREEN-1) {
					firstPosition += 1;
					manager.scrollToPositionWithOffset(firstPosition, 0);
				}
				setSelection(getSelection(), getSelection()+1, FocusRecyclerView.PLACEHOLDER_POSITION);
				return true;
			}
		}else if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER){
			clickItem(getSelection());
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onChildAttachedToWindow(View child) {
		super.onChildAttachedToWindow(child);
		if(manager.getPosition(child) == getSelection()){
			child.setSelected(true);
		}
	}
	
}
