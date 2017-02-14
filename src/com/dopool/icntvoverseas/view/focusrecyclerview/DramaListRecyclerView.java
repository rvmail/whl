package com.dopool.icntvoverseas.view.focusrecyclerview;

import com.dopool.icntvoverseas.utils.AnimateFactory;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
/**
 * 此recyclerview用于二级展示界面
 * @author ly
 */
public class DramaListRecyclerView extends FocusRecyclerView {

	public static final int NUM_COLUMNS = 6;
	private GridLayoutManager manager;
	private ImageView up;
	private ImageView down;

	public DramaListRecyclerView(Context arg0, AttributeSet arg1, int arg2) {
		super(arg0, arg1, arg2);
	}

	public DramaListRecyclerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setChildrenDrawingOrderEnabled(true);
	}

	public DramaListRecyclerView(Context context) {
		super(context);
	}

	@Override
	public void setLayoutManager(LayoutManager layout) {
		super.setLayoutManager(layout);
		this.manager = (GridLayoutManager) layout;
	}

	private int firstVisibleItemPosition = 0;
	private int lastVisibleItemPosition = 0;

	public void setPositions(int first, int last) {
		this.firstVisibleItemPosition = first;
		this.lastVisibleItemPosition = last;
	}

	public int getlastVisibleItemPosition() {
		return lastVisibleItemPosition;
	}

	public void setControll(ImageView up, ImageView down) {
		this.up = up;
		this.down = down;
	}

	/**
	 * 列表scroll时，上下指示器箭头的控制
	 */
	public void controllIndicator() {
		if (firstVisibleItemPosition == 0) {
			up.setVisibility(View.GONE);
		} else {
			up.setVisibility(View.VISIBLE);
		}
		if (lastVisibleItemPosition >= getAdapter().getItemCount() - 1
				|| getAdapter().getItemCount() < NUM_COLUMNS * 2 + 1) {
			down.setVisibility(View.GONE);
		} else {
			down.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			int jumpPosition = getSelection() + 1;
			if (jumpPosition < getAdapter().getItemCount()) {
				if (jumpPosition % NUM_COLUMNS == 0
						&& lastVisibleItemPosition == jumpPosition - 1) {
					firstVisibleItemPosition += NUM_COLUMNS;
					lastVisibleItemPosition += NUM_COLUMNS;
					manager.scrollToPositionWithOffset(jumpPosition
							- NUM_COLUMNS, 0);
					setSelection(getSelection(), jumpPosition, jumpPosition
							- NUM_COLUMNS);
					controllIndicator();
				} else {
					setSelection(getSelection(), jumpPosition,
							FocusRecyclerView.PLACEHOLDER_POSITION);
				}
				return true;
			}
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			int jumpPosition = getSelection() - 1;
			if (jumpPosition >= 0) {
				if (getSelection() % NUM_COLUMNS == 0) {
					return super.onKeyDown(keyCode, event);
				} else {
					setSelection(getSelection(), jumpPosition,
							FocusRecyclerView.PLACEHOLDER_POSITION);
				}
				return true;
			}
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			int jumpPosition = getSelection() - NUM_COLUMNS;
			if (jumpPosition >= 0) {
				if (firstVisibleItemPosition > jumpPosition) {
					firstVisibleItemPosition -= NUM_COLUMNS;
					lastVisibleItemPosition -= NUM_COLUMNS;
					manager.scrollToPositionWithOffset(jumpPosition
							- (jumpPosition % NUM_COLUMNS), 0);
					setSelection(getSelection(), jumpPosition, jumpPosition
							+ NUM_COLUMNS);
					controllIndicator();
				} else {
					setSelection(getSelection(), jumpPosition,
							FocusRecyclerView.PLACEHOLDER_POSITION);
				}
				return true;
			}

		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			int jumpPosition = getSelection() + NUM_COLUMNS;
			if (jumpPosition < getAdapter().getItemCount()) {
				if (jumpPosition > lastVisibleItemPosition) {
					firstVisibleItemPosition += NUM_COLUMNS;
					lastVisibleItemPosition += NUM_COLUMNS;
					manager.scrollToPositionWithOffset(jumpPosition
							- NUM_COLUMNS - (jumpPosition % NUM_COLUMNS), 0);
					setSelection(getSelection(), jumpPosition, jumpPosition
							- NUM_COLUMNS);
					controllIndicator();
				} else {
					setSelection(getSelection(), jumpPosition,
							FocusRecyclerView.PLACEHOLDER_POSITION);
				}
			} else {
				jumpPosition = getAdapter().getItemCount() - 1;
				if (lastVisibleItemPosition < getAdapter().getItemCount() - 1) {
					if (jumpPosition > lastVisibleItemPosition) {
						firstVisibleItemPosition += NUM_COLUMNS;
						lastVisibleItemPosition += NUM_COLUMNS;
						manager.scrollToPositionWithOffset(jumpPosition
								- NUM_COLUMNS - (jumpPosition % NUM_COLUMNS), 0);
						setSelection(getSelection(), jumpPosition, jumpPosition
								- NUM_COLUMNS);
						controllIndicator();
					} else {
						setSelection(getSelection(), jumpPosition,
								FocusRecyclerView.PLACEHOLDER_POSITION);
					}
				} else {
					if (getSelection() < firstVisibleItemPosition + NUM_COLUMNS) {
						setSelection(getSelection(), jumpPosition,
								FocusRecyclerView.PLACEHOLDER_POSITION);
					}
				}
			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
				|| keyCode == KeyEvent.KEYCODE_ENTER) {
			clickItem(getSelection());
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onChildAttachedToWindow(final View child) {
		super.onChildAttachedToWindow(child);
		if (manager.getPosition(child) == getSelection()) {
			child.post(new Runnable() {

				@Override
				public void run() {
					if(manager.getPosition(child) == getSelection()){
						if(hasFocus()){
							setSelection(getSelection(), getSelection(),
									FocusRecyclerView.PLACEHOLDER_POSITION);
						}else{
							AnimateFactory.zoomOutView(child);
						}
					}
				}
			});
		}
	}

	private int index = 0;

	@Override
	protected int getChildDrawingOrder(int childCount, int i) {
		if (childCount <= NUM_COLUMNS * 2) {
			index = getSelection() - firstVisibleItemPosition;
		} else {
			if (firstVisibleItemPosition == 0) {
				index = getSelection() - firstVisibleItemPosition;
			} else {
				index = getSelection() - firstVisibleItemPosition + NUM_COLUMNS;
			}
		}
		if (index < 0) {
			return i;
		} else {
			if (i == childCount - 1) {
				if (index > i) {
					index = i;
				}
				return index;
			}
			if (i == index) {
				return childCount - 1;
			}
		}
		return i;
	}

}
