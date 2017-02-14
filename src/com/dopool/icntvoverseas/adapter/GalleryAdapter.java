package com.dopool.icntvoverseas.adapter;

import java.util.List;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dopool.icntvoverseas.R;
import com.dopool.icntvoverseas.adapter.GalleryAdapter.GalleryHolder;
import com.dopool.icntvoverseas.utils.ImageManager;
import com.nostra13.universalimageloader.core.ImageLoader;

import dopool.cntv.base.SearchInfo;

/**
 * 详情页相关推荐的Adapter
 */
public class GalleryAdapter extends RecyclerView.Adapter<GalleryHolder> {
	private Context mContext;
	private List<SearchInfo> mList;
	private ImageManager manager;
	private ImageLoader mLoader;

	public GalleryAdapter(Context mContext, List<SearchInfo> mList) {
		this.mContext = mContext;
		this.mList = mList;
		manager = ImageManager.getInstance();
		mLoader = manager.getImageLoader();
	}

	@Override
	public int getItemCount() {
		return mList != null ? mList.size() : 0;
	}

	@Override
	public void onBindViewHolder(final GalleryHolder viewHolder,
			final int position) {
		viewHolder.tv_gallery.setText(mList.get(position).getName());
		if (!TextUtils.isEmpty(mList.get(position).getPicurl())) {
			mLoader.displayImage(mList.get(position).getPicurl(),
					viewHolder.img_gallery, manager.getOptions());
		}

	}

	@Override
	public GalleryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(mContext).inflate(
				R.layout.grid_item_recyclerview, parent, false);
		return new GalleryHolder(view);
	}

	static class GalleryHolder extends RecyclerView.ViewHolder {
		ImageView img_gallery;
		TextView tv_gallery;
		View itemView;

		public GalleryHolder(View itemView) {
			super(itemView);
			this.itemView = itemView;
			img_gallery = (ImageView) itemView.findViewById(R.id.grid_item);
			tv_gallery = (TextView) itemView.findViewById(R.id.grid_text);
		}

	}

}
