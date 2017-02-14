package com.dopool.icntvoverseas.adapter;

import java.util.List;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dopool.icntvoverseas.R;
import com.dopool.icntvoverseas.adapter.SerisAdapter.SerisHolder;
import com.dopool.icntvoverseas.view.focusrecyclerview.FocusRecyclerView;

import dopool.cntv.base.MovieItem;

/**
 * 详情页情节介绍下的recyclerView的剧集Adapter
 */
public class SerisAdapter extends RecyclerView.Adapter<SerisHolder> {
	private Context mContext;
	private List<MovieItem> mList;
	private FocusRecyclerView recyclerView;

	public SerisAdapter(Context mContext, List<MovieItem> mList) {
		this.mContext = mContext;
		this.mList = mList;

		if (mList != null) {
			// 实际的剧集总数
			totalCount = mList.size();
			// 最后一页显示的item余数
			remainder = totalCount % NUM_PER_PAGE;
			if (remainder == 0) {
				totalPage = totalCount / NUM_PER_PAGE;
			} else {
				totalPage = totalCount / NUM_PER_PAGE + 1;
			}
		}
	}

	public void setReycycler(FocusRecyclerView recyclerView) {
		this.recyclerView = recyclerView;
	}

	@Override
	public int getItemCount() {
		if (mList == null)
			return 0;
		if (remainder == 0) {
			return mList.size();
		} else {
			// 添加占位item，保证itemCount是NUM_PER_PAGE的倍数
			return mList.size() + (NUM_PER_PAGE - remainder);
		}
	}

	@Override
	public void onBindViewHolder(SerisHolder viewHolder, final int position) {
		int realPosition = placeRealPos(position);

		if (realPosition < mList.size()) {
			viewHolder.itemView.setVisibility(View.VISIBLE);
			String seris = mList.get(realPosition).getName();
			viewHolder.tv_seris.setText(seris);
			// if (realPosition == recyclerView.getSelection()) {
			// viewHolder.itemView.setSelected(true);
			// }
		} else {
			viewHolder.itemView.setVisibility(View.GONE);
		}
	}

	@Override
	public SerisHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(mContext).inflate(R.layout.item_seris,
				parent, false);
		return new SerisHolder(view);
	}

	static class SerisHolder extends RecyclerView.ViewHolder {
		private TextView tv_seris;

		public SerisHolder(View itemView) {
			super(itemView);

			tv_seris = (TextView) itemView.findViewById(R.id.tv_seris);
		}
	}

	public int totalPage = 0, remainder = 0;
	// 设定每页显示8个item
	public static final int NUM_PER_PAGE = 8;
	public int totalCount;

	/**
	 * 事实上，这里用的recyclerView的排列顺序是从上到下，从左至右，如下所示：
	 *  0 2 4 6 
	 *  1 3 5 7 
	 *  而实际开发中，要求显示顺序为：
	 * 	0 1 2 3
	 *  4 5 6 7 
	 *  故需要将实际的postion转换成界面上显示的位置realPos
	 * 
	 * @param postion
	 * @return
	 */
	public int placeRealPos(int postion) {
		int num = postion % (NUM_PER_PAGE);
		int currentPage = postion / NUM_PER_PAGE + 1;
		int realPos = -1;

		switch (num) {
		case 2:
		case 4:
		case 6:
			realPos = (num / 2) + (currentPage - 1) * NUM_PER_PAGE;
			break;
		case 1:
			realPos = 4 + (currentPage - 1) * NUM_PER_PAGE;
			break;
		case 3:
			realPos = 5 + (currentPage - 1) * NUM_PER_PAGE;
			break;
		case 5:
			realPos = 6 + (currentPage - 1) * NUM_PER_PAGE;
			break;
		case 0:
		case 7:
			realPos = postion;
			break;
		default:
			break;
		}

		return realPos;
	}

	/**
	 * 与placeRealPos(int postion)相逆， 
	 * 即将界面上显示的postion转换成实际list绘制时的原始位置originalPos
	 * 
	 * @param postion
	 * @return
	 */
	public int obtainOriginalPos(int postion) {
		int num = postion % (NUM_PER_PAGE);
		int currentPage = postion / NUM_PER_PAGE + 1;
		int originalPos = -1;

		switch (num) {
		case 1:
		case 2:
		case 3:
			originalPos = (num * 2) + (currentPage - 1) * NUM_PER_PAGE;
			break;
		case 4:
			originalPos = 1 + (currentPage - 1) * NUM_PER_PAGE;
			break;
		case 5:
			originalPos = 3 + (currentPage - 1) * NUM_PER_PAGE;
			break;
		case 6:
			originalPos = 5 + (currentPage - 1) * NUM_PER_PAGE;
			break;
		case 0:
		case 7:
			originalPos = postion;
			break;
		default:
			break;
		}

		return originalPos;
	}

}
