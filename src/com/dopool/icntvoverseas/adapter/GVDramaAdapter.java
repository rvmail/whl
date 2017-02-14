package com.dopool.icntvoverseas.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dopool.icntvoverseas.R;
import com.dopool.icntvoverseas.utils.ImageManager;
import com.dopool.icntvoverseas.view.ItemHolder;
import com.dopool.icntvoverseas.view.focusrecyclerview.DramaListRecyclerView;
import com.nostra13.universalimageloader.core.ImageLoader;

import dopool.cntv.base.SeriesItem;

/**
 * 二级列表页的海报数据Adapter
 */
public class GVDramaAdapter extends RecyclerView.Adapter<ItemHolder> {

	private ArrayList<SeriesItem> mList = new ArrayList<SeriesItem>();
	private LayoutInflater mLayoutInflater;
	private ImageManager mImageManager;
	private ImageLoader mImageLoader;

	public GVDramaAdapter(Context mContext,
			DramaListRecyclerView dramaListRecyclerView) {
		mLayoutInflater = LayoutInflater.from(mContext);

		mImageManager = ImageManager.getInstance();
		mImageLoader = mImageManager.getImageLoader();
	}

	public ImageLoader getImageLoader() {
		return mImageLoader;
	}

	public void addData(List<SeriesItem> data, boolean preIsCache,
			List<SeriesItem> preData) {
		if (preIsCache) {
			int startPosition = mList.size() - 1;
			int endPosition = mList.size() - preData.size() - 1;
			for (int i = startPosition; i > endPosition; i--) {
				mList.remove(i);
			}
			mList.addAll(data);
			if (preData.size() < data.size()) {
				notifyItemRangeChanged(mList.size() - data.size(),
						preData.size());
				notifyItemRangeInserted(
						mList.size() + (data.size() - preData.size()),
						data.size() - preData.size());
			} else if (preData.size() == data.size()) {
				notifyItemRangeChanged(mList.size() - data.size(),
						preData.size());
			}
		} else {
			mList.addAll(data);
			notifyItemRangeInserted(mList.size() - data.size(), data.size());
		}
	}

	public void clear() {
		mList.clear();
	}

	@Override
	public int getItemCount() {
		return mList != null ? mList.size() : 0;
	}

	@Override
	public void onBindViewHolder(ItemHolder holder, int position) {
		String title = mList.get(position).getName();
		String url = mList.get(position).getImgUrl();
		holder.name.setText(title);
		if (!TextUtils.isEmpty(url)) {
			mImageLoader.displayImage(url, holder.pic,
					mImageManager.getOptions());
		}
	}

	@Override
	public ItemHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
		ItemHolder holder = new ItemHolder(mLayoutInflater.inflate(
				R.layout.grid_item_recyclerview, null));
		return holder;
	}

}
