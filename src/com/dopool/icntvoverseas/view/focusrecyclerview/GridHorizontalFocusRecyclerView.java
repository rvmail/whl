package com.dopool.icntvoverseas.view.focusrecyclerview;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.dopool.icntvoverseas.adapter.SerisAdapter;

/**
 * 此recyclerview用于详情页剧集
 * 
 * @author ly
 */
public class GridHorizontalFocusRecyclerView extends FocusRecyclerView {

	private GridLayoutManager manager;
	private SerisAdapter mAdapter;
	private ImageView left;
	private ImageView right;

	private int currentPage = 1;

	public GridHorizontalFocusRecyclerView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void setLayoutManager(LayoutManager layout) {
		super.setLayoutManager(layout);
		manager = (GridLayoutManager) layout;
	}

	@Override
	public void setAdapter(Adapter adapter) {
		super.setAdapter(adapter);
		mAdapter = (SerisAdapter) adapter;
	}

	public int getFirstVisibleItemPosition() {
		return firstVisibleItemPosition;
	}

	public int getLastVisibleItemPosition() {
		return lastVisibleItemPosition;
	}

	// 根据currentPage重置首末位置值
	public void reSetFirstAndLastPosition() {
		firstVisibleItemPosition = (currentPage - 1)
				* SerisAdapter.NUM_PER_PAGE;
		lastVisibleItemPosition = (currentPage - 1) * SerisAdapter.NUM_PER_PAGE
				+ 7;
	}

	private int firstVisibleItemPosition = 0;
	private int lastVisibleItemPosition = 7;

	@Override
	public void onChildAttachedToWindow(View child) {
		super.onChildAttachedToWindow(child);

		if (getSelection() == 0 && manager.getPosition(child) == 0) {
			child.post(new Runnable() {
				@Override
				public void run() {
					requestFocus();
					setSelection(0, 0, FocusRecyclerView.PLACEHOLDER_POSITION);
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

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			if (getSelection() % SerisAdapter.NUM_PER_PAGE == 1) {
				// 第二行第一列，左移焦点切换至第一行第四列的item
				setSelection(getSelection(), getSelection() + 5,
						FocusRecyclerView.PLACEHOLDER_POSITION);
			} else if (getSelection() % SerisAdapter.NUM_PER_PAGE == 0) {
				// 第一行第一列
				if (getSelection() == 0) {
					currentPage = 1;
					return true;
				}
				if (firstVisibleItemPosition >= 8) {
					currentPage--;
					manager.scrollToPositionWithOffset(
							firstVisibleItemPosition - 8, 0);
					firstVisibleItemPosition -= 8;
					lastVisibleItemPosition -= 8;
				}
				controllIndicator(false);
				// 这个是左移翻页的情况
				setSelection(getSelection(), getSelection() - 1,
						FocusRecyclerView.PLACEHOLDER_POSITION);
			} else {
				setSelection(getSelection(), getSelection() - 2,
						FocusRecyclerView.PLACEHOLDER_POSITION);
			}

			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			if (getSelection() % SerisAdapter.NUM_PER_PAGE == 6
					&& mAdapter.placeRealPos(getSelection()) != mAdapter.totalCount - 1) {
				// 第一行第四列(非最后一集)，右移焦点切换至第二行第一列的item
				setSelection(getSelection(), getSelection() - 5,
						FocusRecyclerView.PLACEHOLDER_POSITION);
			} else if (getSelection() == lastVisibleItemPosition
					&& currentPage < mAdapter.totalPage) {
				currentPage++;
				manager.scrollToPositionWithOffset(
						firstVisibleItemPosition + 8, 0);
				firstVisibleItemPosition += 8;
				lastVisibleItemPosition += 8;

				if ((firstVisibleItemPosition / 8) + 1 == mAdapter.totalPage) {
					// 最后一页
					currentPage = mAdapter.totalPage;
				}
				controllIndicator(false);
				// 右移翻页
				setSelection(getSelection(), getSelection() + 1,
						FocusRecyclerView.PLACEHOLDER_POSITION);
			} else {
				if (mAdapter.placeRealPos(getSelection()) < mAdapter.totalCount - 1) {
					setSelection(getSelection(), getSelection() + 2,
							FocusRecyclerView.PLACEHOLDER_POSITION);
				} else {
					setSelection(getSelection(), getSelection(),
							FocusRecyclerView.PLACEHOLDER_POSITION);
					return true;
				}

			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			if (getSelection() % 2 != 0)
				setSelection(getSelection(), getSelection() - 1,
						FocusRecyclerView.PLACEHOLDER_POSITION);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			// 最后一页
			if (getSelection() / 8 + 1 == mAdapter.totalPage) {
				if (mAdapter.remainder < 5 && mAdapter.remainder > 0) {
					// 即最后一页只有第一行，那么按下键，焦点应转移到相关推荐的第一个item上
					setSelection(INVALIDATE_POSITION, getSelection(),
							FocusRecyclerView.PLACEHOLDER_POSITION);
					return super.onKeyDown(keyCode, event);
				} else if (mAdapter.remainder == 5) {
					// 最后一页有5个item，第一行4个，第二行1个
					if (getSelection() % 2 == 0) {
						// 从第一行的item按下键，焦点都会转移到第二行的第一个item上（即显示的最后一个剧集）
						setSelection(getSelection(),
								(getSelection() / 8) * 8 + 1,
								FocusRecyclerView.PLACEHOLDER_POSITION);
						return true;
					} else {
						// 从第二行的第一个item按下键，焦点应转移到相关推荐的第一个item上
						setSelection(INVALIDATE_POSITION, getSelection(),
								FocusRecyclerView.PLACEHOLDER_POSITION);
						return super.onKeyDown(keyCode, event);
					}
				} else if (mAdapter.remainder == 6) {
					// 最后一页有6个item，第一行4个，第二行2个
					if ((getSelection() % 8 == 4) || (getSelection() % 8 == 6)) {
						// 从第一行的第三、四个item按下键，焦点都会转移到第二行的第二个item上
						setSelection(getSelection(),
								(getSelection() / 8) * 8 + 3,
								FocusRecyclerView.PLACEHOLDER_POSITION);
						return true;
					} else if (getSelection() % 2 == 0
							&& getSelection() + 1 < mAdapter.totalCount) {
						setSelection(getSelection(), getSelection() + 1,
								FocusRecyclerView.PLACEHOLDER_POSITION);
						return true;
					} else {
						setSelection(INVALIDATE_POSITION, getSelection(),
								FocusRecyclerView.PLACEHOLDER_POSITION);
						return super.onKeyDown(keyCode, event);
					}
				} else if (mAdapter.remainder == 7) {
					// 最后一页有7个item，第一行4个，第二行3个
					if (getSelection() % 8 == 6) {
						// 从第一行的第四个item按下键，焦点都会转移到第二行的第三个item上
						setSelection(getSelection(),
								(getSelection() / 8) * 8 + 5,
								FocusRecyclerView.PLACEHOLDER_POSITION);
						return true;
					} else if (getSelection() % 2 == 0
							&& getSelection() + 1 < mAdapter.totalCount) {
						setSelection(getSelection(), getSelection() + 1,
								FocusRecyclerView.PLACEHOLDER_POSITION);
						return true;
					} else {
						setSelection(INVALIDATE_POSITION, getSelection(),
								FocusRecyclerView.PLACEHOLDER_POSITION);
						return super.onKeyDown(keyCode, event);
					}
				} else if (getSelection() % 2 == 0
						&& getSelection() + 1 < mAdapter.totalCount) {
					setSelection(getSelection(), getSelection() + 1,
							FocusRecyclerView.PLACEHOLDER_POSITION);
					return true;
				} else {
					setSelection(INVALIDATE_POSITION, getSelection(),
							FocusRecyclerView.PLACEHOLDER_POSITION);
					return super.onKeyDown(keyCode, event);
				}
			} else if (getSelection() % 2 == 0
					&& getSelection() + 1 < mAdapter.totalCount) {
				// 不是最后一页，即每页都能显示8个完整的item。这是从第一行的item按下键的情况
				setSelection(getSelection(), getSelection() + 1,
						FocusRecyclerView.PLACEHOLDER_POSITION);
				return true;
			} else {
				// 不是最后一页，即每页都能显示8个完整的item。这是从第二行的item按下键的情况
				setSelection(INVALIDATE_POSITION, getSelection(),
						FocusRecyclerView.PLACEHOLDER_POSITION);
				return super.onKeyDown(keyCode, event);
			}
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
				|| keyCode == KeyEvent.KEYCODE_ENTER) {
			clickItem(getSelection());
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	// @param: isJump true:表示是非此页面控制dpad导致的剧集列表滚动， false:表示是此页面控制dpad导致剧集滚动
	public void controllIndicator(boolean isJump) {
		if (isJump)
			currentPage = getSelection() / SerisAdapter.NUM_PER_PAGE + 1;
		if (currentPage == 1) {
			left.setVisibility(View.GONE);
		} else {
			left.setVisibility(View.VISIBLE);
		}
		if (currentPage == mAdapter.totalPage) {
			right.setVisibility(View.GONE);
		} else {
			right.setVisibility(View.VISIBLE);
		}
	}

	public void setControll(ImageView left, ImageView right) {
		this.left = left;
		this.right = right;
	}

}
