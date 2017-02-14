package com.dopool.icntvoverseas.utils;

import android.content.Context;
import android.graphics.Bitmap;

import com.dopool.icntvoverseas.R;
import com.dopool.icntvoverseas.app.CNTVApplication;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class ImageManager {

	private static volatile ImageManager mInstance;
	private ImageLoaderConfiguration mConfiguration;
	private ImageLoader mImageLoader;
	private DisplayImageOptions mOptions;

	private ImageManager(Context mContext) {
		mConfiguration = new ImageLoaderConfiguration.Builder(mContext)
				.threadPoolSize(3).threadPriority(Thread.MAX_PRIORITY - 2)
				// 硬盘缓存设为50M
				.diskCacheSize(50 * 1024 * 1024).diskCacheFileCount(1000)
				.memoryCache(new WeakMemoryCache())
				// 设置图片加载方式为LIFO
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				// 拒绝(同一个图片URL)根据不同大小的imageview保存不同大小图片
				.denyCacheImageMultipleSizesInMemory().build();
		// 全局初始化此配置
		ImageLoader.getInstance().init(mConfiguration);

		mOptions = new DisplayImageOptions.Builder().cacheInMemory(true)
				.cacheOnDisk(true).showImageOnLoading(R.drawable.newtv_default)
				.showImageOnFail(R.drawable.newtv_default)
				.delayBeforeLoading(100)
				.build();
		mImageLoader = ImageLoader.getInstance();
	}

	public static ImageManager getInstance() {
		if (mInstance == null)
			synchronized (ImageManager.class) {
				if (mInstance == null)
					mInstance = new ImageManager(CNTVApplication.getInstance());
			}
		return mInstance;
	}

	public ImageLoader getImageLoader() {
		return mImageLoader;
	}

	public DisplayImageOptions getOptions() {
		return mOptions;
	}
}
