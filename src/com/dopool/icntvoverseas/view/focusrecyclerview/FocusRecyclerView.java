package com.dopool.icntvoverseas.view.focusrecyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
/**
 * 此recyclerview为base class
 * @author ly
 */

public class FocusRecyclerView extends RecyclerView{

	public static final int INVALIDATE_POSITION = -1;
	public static final int PLACEHOLDER_POSITION = -2;
	public FocusRecyclerView(Context context) {
		super(context);
	}

	public FocusRecyclerView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public FocusRecyclerView(Context arg0, AttributeSet arg1, int arg2) {
		super(arg0, arg1, arg2);
	}

	public interface OnItemSelectedListener {
		public void onItemSelected(int prePosition, int position, int changeLinePosition, FocusRecyclerView view);
	}
	
	public interface OnItemClickListener{
		public void onItemClicked(int position);
	}
	
	public void setListener(OnItemSelectedListener listener){
		this.listener = listener;
	}
	
	public void setOnItemClickListener(OnItemClickListener listener){
		this.onItemClickListener = listener;
	}
	
	protected OnItemClickListener onItemClickListener;
	protected OnItemSelectedListener listener;
	protected int selectedItem = -100;
	/**
	 * @param prePosition:选中蓝框起始位置
	 * @param selectedItem:除换行外选中蓝框到达位置
	 * @param changeLinePosition:换行蓝框到达位置 ,{@link #PLACEHOLDER_POSITION}占位position无任何意义
	 */
	public void setSelection(int prePosition, int selectedItem, int changeLinePosition){
		this.selectedItem = selectedItem;
		if(listener != null){
			listener.onItemSelected(prePosition, selectedItem, changeLinePosition, this);
		}
	}
	
	public void setItemSelected(int position){
		this.selectedItem = position;
	}
	
	public int getSelection(){
		return selectedItem;
	}
	
	public void clickItem(int position){
		onItemClickListener.onItemClicked(position);
	}
	
}
