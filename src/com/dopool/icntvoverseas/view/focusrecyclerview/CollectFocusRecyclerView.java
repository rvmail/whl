package com.dopool.icntvoverseas.view.focusrecyclerview;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
/**
 * 此recyclerview用于收藏界面
 * @author ly
 */
public class CollectFocusRecyclerView extends FocusRecyclerView{

	private GridLayoutManager manager;
	public static final int NUM_COLUMNS = 7;
	private ImageView up;
	private ImageView down;
	
	public CollectFocusRecyclerView(Context arg0, AttributeSet arg1, int arg2) {
		super(arg0, arg1, arg2);
	}

	public CollectFocusRecyclerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setChildrenDrawingOrderEnabled(true);
	}

	public CollectFocusRecyclerView(Context context) {
		super(context);
	}
	
	@Override
	public void setLayoutManager(LayoutManager layout) {
		super.setLayoutManager(layout);
		manager = (GridLayoutManager) layout;
	}
	
	@Override
	protected void setChildrenDrawingOrderEnabled(boolean enabled) {
		super.setChildrenDrawingOrderEnabled(enabled);	
	}
	private int index = 0;
	@Override
	protected int getChildDrawingOrder(int childCount, int i) {
		if(childCount > NUM_COLUMNS * 2){
			index = getSelection() - manager.findFirstVisibleItemPosition() + NUM_COLUMNS;
		}else{
			index = getSelection() - firstVisibleItemPosition;
		}
		if(index<0){
			return i;
		}else{
			if(i == childCount - 1){
				if(index > i){
					index = i;
				}
				return index;
			}
			if(i == index){
				return childCount - 1;
			}
		}
		return i;
		
	}
	
	@Override
	public void onChildAttachedToWindow(View child) {
		super.onChildAttachedToWindow(child);
		if(getSelection() == 0 && manager.getPosition(child) == 0){
			child.post(new Runnable() {
				
				@Override
				public void run() {
					requestFocus();
					setSelection(0,0,FocusRecyclerView.PLACEHOLDER_POSITION);
				}
			});
		}
		if(manager.getPosition(child) == getSelection() && getSelection() != 0){
			child.post(new Runnable() {
				
				@Override
				public void run() {
					setSelection(getSelection(),getSelection(),FocusRecyclerView.PLACEHOLDER_POSITION);
				}
			});
		}
	}
	
	public void setPositions(int first, int last){
		this.firstVisibleItemPosition = first;
		this.lastVisibleItemPosition = last;
	}
	
	public int getFirstVisibleItemPosition(){
		return firstVisibleItemPosition;
	}
	
	private int firstVisibleItemPosition = 0;
	private int lastVisibleItemPosition = 0;
	int jumpPosition = 0;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			jumpPosition = getSelection() + 1;
			if(jumpPosition < getAdapter().getItemCount()){
				if(jumpPosition % NUM_COLUMNS == 0 &&  lastVisibleItemPosition == jumpPosition -1){
					manager.scrollToPositionWithOffset(jumpPosition - NUM_COLUMNS, 0);
					firstVisibleItemPosition += NUM_COLUMNS;
					lastVisibleItemPosition += NUM_COLUMNS;
					controllIndicator();
					setSelection(getSelection(), jumpPosition, jumpPosition - NUM_COLUMNS);
				}else{
					setSelection(getSelection(), jumpPosition, FocusRecyclerView.PLACEHOLDER_POSITION);
				}
				return true;
			}
		}else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			jumpPosition = getSelection() - 1;
			if(jumpPosition > -1){
				if(getSelection() % NUM_COLUMNS == 0 && firstVisibleItemPosition == getSelection()){
					manager.scrollToPositionWithOffset(getSelection() - NUM_COLUMNS, 0);
					setSelection(getSelection(), jumpPosition, jumpPosition + NUM_COLUMNS);
					firstVisibleItemPosition -= NUM_COLUMNS;
					lastVisibleItemPosition -= NUM_COLUMNS;
					controllIndicator();
				}else{
					setSelection(getSelection(), jumpPosition, FocusRecyclerView.PLACEHOLDER_POSITION);
				}
				return true;
			}
		}else if(keyCode == KeyEvent.KEYCODE_DPAD_UP){
			jumpPosition = getSelection() - NUM_COLUMNS;
			if(jumpPosition > -1){
				if(firstVisibleItemPosition > jumpPosition){
					manager.scrollToPositionWithOffset(jumpPosition - (jumpPosition % NUM_COLUMNS), 0);
					setSelection(getSelection(), jumpPosition, jumpPosition + NUM_COLUMNS);
					firstVisibleItemPosition -= NUM_COLUMNS;
					lastVisibleItemPosition -= NUM_COLUMNS;
					controllIndicator();
				}else{
					setSelection(getSelection(), jumpPosition, FocusRecyclerView.PLACEHOLDER_POSITION);
				}
			}
			return true;
		}else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
			jumpPosition = getSelection() +NUM_COLUMNS;
			if(jumpPosition < getAdapter().getItemCount()){
				if(lastVisibleItemPosition < jumpPosition){
					manager.scrollToPositionWithOffset(jumpPosition - (jumpPosition % NUM_COLUMNS) - NUM_COLUMNS, 0);
					setSelection(getSelection(), jumpPosition, jumpPosition - NUM_COLUMNS);
					firstVisibleItemPosition += NUM_COLUMNS;
					lastVisibleItemPosition += NUM_COLUMNS;
					controllIndicator();
				}else{
					setSelection(getSelection(), jumpPosition, FocusRecyclerView.PLACEHOLDER_POSITION);
				}
			}else if(jumpPosition >= getAdapter().getItemCount()){
				jumpPosition = getAdapter().getItemCount() - 1;
				if(jumpPosition != getSelection()){
					if(lastVisibleItemPosition < jumpPosition){
						manager.scrollToPositionWithOffset(jumpPosition - (jumpPosition % NUM_COLUMNS) - NUM_COLUMNS, 0);
						setSelection(getSelection(), jumpPosition, jumpPosition - NUM_COLUMNS);
						firstVisibleItemPosition += NUM_COLUMNS;
						lastVisibleItemPosition += NUM_COLUMNS;
						controllIndicator();
					}else if(lastVisibleItemPosition - getSelection() >= NUM_COLUMNS){
						setSelection(getSelection(), jumpPosition, FocusRecyclerView.PLACEHOLDER_POSITION);
					}
				}
			}
			return true;
		}else if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER){
			clickItem(getSelection());
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public void controllIndicator(){
		if(firstVisibleItemPosition == 0){
			up.setVisibility(View.GONE);
		}else{
			up.setVisibility(View.VISIBLE);
		}
		if(lastVisibleItemPosition >= getAdapter().getItemCount() - 1 || getAdapter().getItemCount() < NUM_COLUMNS * 2 + 1){
			down.setVisibility(View.GONE);
		}else{
			down.setVisibility(View.VISIBLE);
		}
	}
	
	public void setControll(ImageView up, ImageView down) {
		this.up = up;
		this.down = down;
	}
	
}
