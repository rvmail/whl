package com.dopool.icntvoverseas.view.focusrecyclerview;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.dopool.icntvoverseas.HistoryActivity.MyAdapter;
/**
 * 此recyclerview用于历史界面
 * @author ly
 */
public class GridFocusRecyclerView extends FocusRecyclerView{

	public static final int NUM_COLUMNS = 7;
	private GridLayoutManager manager;
	private ImageView up;
	private ImageView down;
	
	public GridFocusRecyclerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public GridFocusRecyclerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setChildrenDrawingOrderEnabled(true);
	}

	public GridFocusRecyclerView(Context context) {
		super(context);
	}
	
	@Override
	public void setLayoutManager(LayoutManager layout) {
		super.setLayoutManager(layout);
		this.manager = (GridLayoutManager) layout;
	}
	
	private int jumpPosition = 0;
	private int firstVisibleItemPosition = 0;
	private int lastVisibleItemPosition = 0;
	
	public void setPositions(int first, int last){
		this.firstVisibleItemPosition = first;
		this.lastVisibleItemPosition = last;
	}
	
	public void setControll(ImageView up, ImageView down) {
		this.up = up;
		this.down = down;
	}
	
	@Override
	public void onChildAttachedToWindow(View child) {
		super.onChildAttachedToWindow(child);
		if(getSelection() == 0 && manager.getPosition(child) == 1){
			child.post(new Runnable() {
				@Override
				public void run() {
					requestFocus();
					setSelection(1,1,FocusRecyclerView.PLACEHOLDER_POSITION);
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
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			for(jumpPosition = getSelection() + 1; jumpPosition < getAdapter().getItemCount(); jumpPosition++){
				if(getAdapter().getItemViewType(jumpPosition) == MyAdapter.TYPE_NORMAL){
					if(jumpPosition % NUM_COLUMNS == 1 &&  lastVisibleItemPosition == jumpPosition -2){
						manager.scrollToPositionWithOffset(jumpPosition - 1 - NUM_COLUMNS, 0);
						firstVisibleItemPosition += NUM_COLUMNS;
						lastVisibleItemPosition += NUM_COLUMNS;
						controllIndicator();
						setSelection(getSelection(), jumpPosition, jumpPosition - NUM_COLUMNS);
					}else{
						setSelection(getSelection(), jumpPosition, FocusRecyclerView.PLACEHOLDER_POSITION);
					}
					break;
				}
			}
			return true;
		}else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			for(jumpPosition = getSelection() - 1; jumpPosition > -1; jumpPosition--){
				if(getAdapter().getItemViewType(jumpPosition) == MyAdapter.TYPE_NORMAL){
					if(getSelection() % NUM_COLUMNS == 1 && firstVisibleItemPosition + 1 == getSelection()){
						manager.scrollToPositionWithOffset(getSelection() - 1 - NUM_COLUMNS, 0);
						setSelection(getSelection(), jumpPosition, jumpPosition + NUM_COLUMNS);
						firstVisibleItemPosition -= NUM_COLUMNS;
						lastVisibleItemPosition -= NUM_COLUMNS;
						controllIndicator();
					}else{
						setSelection(getSelection(), jumpPosition, FocusRecyclerView.PLACEHOLDER_POSITION);
					}
					break;
				}
			}
			return true;
		}else if(keyCode == KeyEvent.KEYCODE_DPAD_UP){
			for(jumpPosition = getSelection() - NUM_COLUMNS; jumpPosition > -1; jumpPosition--){
				if(getAdapter().getItemViewType(jumpPosition) == MyAdapter.TYPE_NORMAL){
					if(firstVisibleItemPosition > jumpPosition){
						manager.scrollToPositionWithOffset(jumpPosition - (jumpPosition % NUM_COLUMNS), 0);
						setSelection(getSelection(), jumpPosition, jumpPosition + NUM_COLUMNS);
						firstVisibleItemPosition -= NUM_COLUMNS;
						lastVisibleItemPosition -= NUM_COLUMNS;
						controllIndicator();
					}else{
						setSelection(getSelection(), jumpPosition, FocusRecyclerView.PLACEHOLDER_POSITION);
					}
					break;
				}
			}
			return true;
		}else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
			for(jumpPosition = getSelection() +NUM_COLUMNS; jumpPosition < getAdapter().getItemCount(); jumpPosition--){
				if(getAdapter().getItemViewType(jumpPosition) == MyAdapter.TYPE_NORMAL && jumpPosition < getAdapter().getItemCount()){
					if(lastVisibleItemPosition < jumpPosition){
						manager.scrollToPositionWithOffset(jumpPosition - (jumpPosition % NUM_COLUMNS) - NUM_COLUMNS, 0);
						setSelection(getSelection(), jumpPosition, jumpPosition - NUM_COLUMNS);
						firstVisibleItemPosition += NUM_COLUMNS;
						lastVisibleItemPosition += NUM_COLUMNS;
						controllIndicator();
					}else{
						setSelection(getSelection(), jumpPosition, FocusRecyclerView.PLACEHOLDER_POSITION);
					}
					break;
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
		if(lastVisibleItemPosition == getAdapter().getItemCount() - 1 || getAdapter().getItemCount() < NUM_COLUMNS * 2 + 1){
			down.setVisibility(View.GONE);
		}else{
			down.setVisibility(View.VISIBLE);
		}
	}
	
	private int index = 0;
	
	@Override
	protected void setChildrenDrawingOrderEnabled(boolean enabled) {
		super.setChildrenDrawingOrderEnabled(enabled);
	}
	
	@Override
	protected int getChildDrawingOrder(int childCount, int i) {
		if(childCount == NUM_COLUMNS * 3 || childCount == NUM_COLUMNS * 4){
			index = getSelection() - firstVisibleItemPosition + NUM_COLUMNS;
		}else if(childCount == NUM_COLUMNS * 2 || childCount == NUM_COLUMNS){
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

}
