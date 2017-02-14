package com.dopool.icntvoverseas.view.focusrecyclerview;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
/**
 * 此recyclerview用于详情界面底部推荐位置
 * @author ly
 */
public class RecommendRecyclerView extends FocusRecyclerView{

	public static final int NUMBER_COLOUM_PRE_SCREEN = 7;
	private LinearLayoutManager manager;
	private ImageView left;
	private ImageView right;
	public RecommendRecyclerView(Context arg0, AttributeSet arg1, int arg2) {
		super(arg0, arg1, arg2);
	}

	public RecommendRecyclerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setChildrenDrawingOrderEnabled(true);
	}

	public RecommendRecyclerView(Context context) {
		super(context);
	}
	
	@Override
	public void setLayoutManager(LayoutManager layout) {
		super.setLayoutManager(layout);
		manager = (LinearLayoutManager) layout;
	}
	
	@Override
	protected void setChildrenDrawingOrderEnabled(boolean enabled) {
		super.setChildrenDrawingOrderEnabled(enabled);	
	}
	
	private int index = 0;
	@Override
	protected int getChildDrawingOrder(int childCount, int i) {
		if(childCount == NUMBER_COLOUM_PRE_SCREEN+2){
			if(fistVisibleItemPosition == 0){
				index = getSelection() - fistVisibleItemPosition;
			}else{
				index = getSelection() - fistVisibleItemPosition + 1;
			}
		}else if(childCount == NUMBER_COLOUM_PRE_SCREEN+3){
			index = getSelection() - fistVisibleItemPosition + 1;
		}else if(childCount < NUMBER_COLOUM_PRE_SCREEN+2){
			index = getSelection() - fistVisibleItemPosition;
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
	public void setItemSelected(int position) {
		fistVisibleItemPosition = 0;
		listener.onFirstVisiableItemchange(fistVisibleItemPosition);
		super.setItemSelected(position);
	}
	
	private int fistVisibleItemPosition = 0;
	
	OnFirstVisiableItemchangeListener listener;
	public interface OnFirstVisiableItemchangeListener{
		void onFirstVisiableItemchange(int fistVisibleItemPosition);
	}
	
	public void setOnFirstVisiableItemchangeListener(OnFirstVisiableItemchangeListener listener){
		this.listener = listener;
	}
	
	public void setControll(ImageView left, ImageView right) {
		this.left = left;
		this.right = right;
	}
	
	public void controllIndicator(){
		if(fistVisibleItemPosition == 0){
			left.setVisibility(View.GONE);
		}else{
			left.setVisibility(View.VISIBLE);
		}
		if(fistVisibleItemPosition + NUMBER_COLOUM_PRE_SCREEN == getAdapter().getItemCount() - 1 || getAdapter().getItemCount() < NUMBER_COLOUM_PRE_SCREEN+2){
			right.setVisibility(View.GONE);
		}else{
			right.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
			//the first one, press left not response
			if(getSelection() == 0){
				return true;
			}
			if(getSelection() == fistVisibleItemPosition +1 && fistVisibleItemPosition != 0){
				manager.scrollToPositionWithOffset(fistVisibleItemPosition-1, 0);
				fistVisibleItemPosition -= 1;
				listener.onFirstVisiableItemchange(fistVisibleItemPosition);
				setSelection(getSelection(), getSelection()-1, getSelection());
				controllIndicator();
			}else{
				if(getSelection() - 1 == 0){
					View newFocus = manager.findViewByPosition(fistVisibleItemPosition);
					newFocus.post(new Runnable() {
						
						@Override
						public void run() {
							if(getSelection() == fistVisibleItemPosition + 1){
								setSelection(getSelection(), getSelection()-1, FocusRecyclerView.PLACEHOLDER_POSITION);
							}
						}
					});
				}else{
					setSelection(getSelection(), getSelection()-1, FocusRecyclerView.PLACEHOLDER_POSITION);
				}
			}
			return true;
		}else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
			//the last one, press right not response
			if(getSelection() == getAdapter().getItemCount() - 1){
				return true;
			}
			if(getSelection() == fistVisibleItemPosition +NUMBER_COLOUM_PRE_SCREEN-1 && getSelection() + 2 != getAdapter().getItemCount()){
				manager.scrollToPositionWithOffset(fistVisibleItemPosition+1, 0);
				fistVisibleItemPosition += 1;
				listener.onFirstVisiableItemchange(fistVisibleItemPosition);
				setSelection(getSelection(), getSelection()+1, getSelection());
				controllIndicator();
			}else{
				if(getSelection() + 2 == getAdapter().getItemCount()){
					View newFocus = manager.findViewByPosition(getSelection()+1);
					newFocus.post(new Runnable() {
						
						@Override
						public void run() {
							if(getSelection() + 2 == getAdapter().getItemCount()){
								setSelection(getSelection(), getSelection()+1, FocusRecyclerView.PLACEHOLDER_POSITION);
							}
						}
					});
				}else{
					setSelection(getSelection(), getSelection()+1, FocusRecyclerView.PLACEHOLDER_POSITION);
				}
			}
			return true;
		}else if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER){
				clickItem(getSelection());
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
}
