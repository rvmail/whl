package com.dopool.icntvoverseas.view.focusrecyclerview;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
/**
 * 此recyclerview用于搜索界面
 * @author ly
 */
public class SearchRecyclerView extends FocusRecyclerView{
	private PaginationListener mPaginationListener;
	private LinearLayoutManager manager;
	public SearchRecyclerView(Context arg0, AttributeSet arg1, int arg2) {
		super(arg0, arg1, arg2);
	}

	public SearchRecyclerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setChildrenDrawingOrderEnabled(true);
	}

	public SearchRecyclerView(Context context) {
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
		if(childCount == 9){
			if(fistVisibleItemPosition == 0){
				index = getSelection() - fistVisibleItemPosition;
			}else{
				index = getSelection() - fistVisibleItemPosition + 1;
			}
		}else if(childCount == 10){
			index = getSelection() - fistVisibleItemPosition + 1;
		}else if(childCount < 9){
			index = getSelection();
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
		super.setItemSelected(position);
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
		
		if (manager.getPosition(child) == getSelection()) {
			child.post(new Runnable() {

				@Override
				public void run() {
					setSelection(getSelection(), getSelection(),
							FocusRecyclerView.PLACEHOLDER_POSITION);
				}
			});
		}
	}
	
	private int fistVisibleItemPosition = 0;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
			//the first one, press left not response
			if(getSelection() == 0){
				return true;
			}
			if(getSelection() == fistVisibleItemPosition){
				manager.scrollToPositionWithOffset(fistVisibleItemPosition-1, 0);
				fistVisibleItemPosition -= 1;
				setSelection(getSelection(), getSelection()-1, FocusRecyclerView.PLACEHOLDER_POSITION);
			}else{
				setSelection(getSelection(), getSelection()-1, FocusRecyclerView.PLACEHOLDER_POSITION);
			}
			return true;
		}else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
			if(getSelection() == getAdapter().getItemCount() - 1){
				return true;
			}
			/**
			 * 当滑到倒数第二个时，加载数据
			 */
			if (getSelection() == getAdapter().getItemCount() - 2) {
				if (mPaginationListener!=null) {
					mPaginationListener.onPagination();
				}
			}
			if(getSelection() == fistVisibleItemPosition +7){
				manager.scrollToPositionWithOffset(fistVisibleItemPosition+1, 0);
				fistVisibleItemPosition += 1;
				setSelection(getSelection(), getSelection()+1, FocusRecyclerView.PLACEHOLDER_POSITION);
			}else{
				setSelection(getSelection(), getSelection()+1, FocusRecyclerView.PLACEHOLDER_POSITION);
			}
			return true;
		}else if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER){
				clickItem(getSelection());
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	/**用于分页请求的回调，当滑到最后一个时回到*/
	public interface PaginationListener{
		void onPagination();
	}

	public void setPaginationListener(PaginationListener mPaginationListener) {
		this.mPaginationListener = mPaginationListener;
	}
}
