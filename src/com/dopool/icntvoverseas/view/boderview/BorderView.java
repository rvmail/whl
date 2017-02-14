package com.dopool.icntvoverseas.view.boderview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalFocusChangeListener;

import com.dopool.icntvoverseas.CollectActivity;
import com.dopool.icntvoverseas.DramaListActivity;
import com.dopool.icntvoverseas.R;
import com.dopool.icntvoverseas.utils.AnimateFactory;
import com.dopool.icntvoverseas.view.focusrecyclerview.CollectFocusRecyclerView;
import com.dopool.icntvoverseas.view.focusrecyclerview.DramaListRecyclerView;
import com.dopool.icntvoverseas.view.focusrecyclerview.FocusRecyclerView;
import com.dopool.icntvoverseas.view.focusrecyclerview.FocusRecyclerView.OnItemSelectedListener;
import com.dopool.icntvoverseas.view.focusrecyclerview.GridFocusRecyclerView;
import com.dopool.icntvoverseas.view.focusrecyclerview.GridHorizontalFocusRecyclerView;
import com.dopool.icntvoverseas.view.focusrecyclerview.RecommendRecyclerView;
import com.dopool.icntvoverseas.view.focusrecyclerview.SearchRecyclerView;
/**
 * author:ly
 */
public class BorderView extends View implements OnGlobalFocusChangeListener{
    private static String TAG = "BorderView";
    private Context context;

    private BorderBaseEffect mEffect;
    private BorderView mBorderView;
    private boolean mEnableBorder = true;
    private ViewGroup mViewGroup;
    private boolean mFocusLimit = false;
    private boolean mFirstFocus = true;

    private OnItemSelectedListener focusOnItemSelectedListener;

    public BorderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(context);
    }

    public BorderView(Context context) {
        super(context);
        this.context = context;
        init(context);
    }

    private void init(Context context) {
        mBorderView = this;
        if (mEffect == null){
        	int screenWidth = context.getResources().getDimensionPixelSize(R.dimen.px1920);
        	int screenHeight = context.getResources().getDimensionPixelSize(R.dimen.px1080);
        	int screenMarginTop = context.getResources().getDimensionPixelSize(R.dimen.px138);
        	mEffect = BorderBaseEffect.getDefault(screenWidth, screenHeight, screenMarginTop);
        }
        setVisibility(GONE);
    }

    public BorderBaseEffect getEffect() {
        return mEffect;
    }

    public void setEffect(BorderBaseEffect effect) {
        mEffect = effect;
    }

    public void attachTo(ViewGroup viewGroup) {
        try {
            if (mViewGroup != viewGroup) {
                mViewGroup = viewGroup;
                ViewTreeObserver viewTreeObserver = mViewGroup.getViewTreeObserver();
                if (viewTreeObserver.isAlive()) {
                    viewTreeObserver.addOnGlobalFocusChangeListener(this);
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @SuppressLint("NewApi")
	public void detachFrom(ViewGroup viewGroup) {
        try {
            if (viewGroup == mViewGroup) {
                ViewTreeObserver viewTreeObserver = mViewGroup.getViewTreeObserver();
                viewTreeObserver.removeOnGlobalFocusChangeListener(this);
                if (getParent() == mViewGroup) {
                    mViewGroup.removeView(this);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean isFocusLimit() {
        return mFocusLimit;
    }

    public void setFocusLimit(boolean focusLimit) {
        this.mFocusLimit = focusLimit;
    }

    private View ownOldFocus;
    @Override
    public void onGlobalFocusChanged(View oldFocus, View newFocus) {
    	if(ownOldFocus != null && oldFocus == null){
    		oldFocus = ownOldFocus;
    	}
        try {
            if (!mEnableBorder) return;
            if (mFocusLimit) {
                if (mViewGroup.indexOfChild(newFocus) < 0) {
                    mEffect.end();
                    return;
                }
                if (mViewGroup.indexOfChild(oldFocus) < 0) {
                    oldFocus = null;
                }
            }
            if (mViewGroup.getRootView() instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) mViewGroup.getRootView();
                if (this.getParent() != viewGroup) {
                    ViewGroup vg = (ViewGroup) this.getParent();
                    if (vg != null) {
                        Log.d(TAG, "removeView");
                        detachFrom(vg);
                        vg.removeView(this);
                        oldFocus = null;
                    }
                    viewGroup.addView(this);
                }
            }
            
            if(mViewGroup instanceof FocusRecyclerView){
            	FocusRecyclerView recyclerView = (FocusRecyclerView) mViewGroup;
            	if(focusOnItemSelectedListener == null){
            		focusOnItemSelectedListener = new FocusRecyclerView.OnItemSelectedListener() {
						
            			private View oldFocus = null;
            			private View newFocus = null;
            			private View tempFocus = null;
 						@Override
						public void onItemSelected(final int prePosition, final int position, int changeLinePosition, final FocusRecyclerView recyclerView) {
							if(recyclerView instanceof GridFocusRecyclerView 
									|| recyclerView instanceof DramaListRecyclerView 
									|| recyclerView instanceof CollectFocusRecyclerView ){
								if(changeLinePosition != FocusRecyclerView.PLACEHOLDER_POSITION){
									if(recyclerView instanceof GridFocusRecyclerView 
											|| recyclerView instanceof DramaListRecyclerView
											|| recyclerView instanceof CollectFocusRecyclerView ){
										tempFocus = ((GridLayoutManager)recyclerView.getLayoutManager()).findViewByPosition(changeLinePosition);
									}
								}else{
									tempFocus = null;
								}
								if(recyclerView instanceof GridFocusRecyclerView 
										|| recyclerView instanceof DramaListRecyclerView
										|| recyclerView instanceof CollectFocusRecyclerView ){
									oldFocus = ((GridLayoutManager)recyclerView.getLayoutManager()).findViewByPosition(prePosition);
									newFocus = ((GridLayoutManager)recyclerView.getLayoutManager()).findViewByPosition(position);
								}
								mEffect.start(mBorderView, oldFocus, newFocus, tempFocus, false);//非横向滚动
							}else{
								if(recyclerView instanceof RecommendRecyclerView){
									if(changeLinePosition != FocusRecyclerView.PLACEHOLDER_POSITION){
										tempFocus = ((LinearLayoutManager)recyclerView.getLayoutManager()).findViewByPosition(changeLinePosition);
									}else{
										tempFocus = null;
									}
									oldFocus = ((LinearLayoutManager)recyclerView.getLayoutManager()).findViewByPosition(prePosition);
									newFocus = ((LinearLayoutManager)recyclerView.getLayoutManager()).findViewByPosition(position);
								}else{
									oldFocus = ((LinearLayoutManager)recyclerView.getLayoutManager()).findViewByPosition(prePosition);
									newFocus = ((LinearLayoutManager)recyclerView.getLayoutManager()).findViewByPosition(position);
									//这里不使用tempFocus是因为如果移动就会超出屏幕，就会自动忽略。
									tempFocus = null;
								}
								mEffect.start(mBorderView, oldFocus, newFocus, tempFocus, true);//横向滚动
							}
							if(recyclerView instanceof DramaListRecyclerView){
								int currentItem = position + 1;
								// 此处刷新是因为选中的item位置不同
								((DramaListActivity)context).tv_drama_count.setText(currentItem+"/"+((DramaListActivity)context).totalItem);
							}
							if(recyclerView instanceof CollectFocusRecyclerView){
								int currentItem = position + 1;
								// 此处刷新是因为选中的item位置不同
								((CollectActivity)context).count.setText(currentItem+"/"+((CollectActivity)context).totalItem);
							}
						}
					};
					recyclerView.setListener(focusOnItemSelectedListener);
            	}
            }
            
            
            if(newFocus instanceof GridHorizontalFocusRecyclerView){
            	GridHorizontalFocusRecyclerView gridHorizontalFocusRecyclerView = (GridHorizontalFocusRecyclerView) newFocus;
            		gridHorizontalFocusRecyclerView.setSelection(gridHorizontalFocusRecyclerView.getSelection(), 
            				gridHorizontalFocusRecyclerView.getSelection(),FocusRecyclerView.PLACEHOLDER_POSITION);
            }
            
            //三级页推荐位不再使用不再使用LinearFocusRecyclerView与搜索页保持一致使用SearchRecyclerView
            //recyclerview焦点消失蓝边隐藏item缩小
            if(oldFocus instanceof SearchRecyclerView || oldFocus instanceof RecommendRecyclerView){
        		this.setVisibility(View.GONE);
        		AnimateFactory.zoomOutView(((FocusRecyclerView) oldFocus).getLayoutManager()
        				.findViewByPosition(((FocusRecyclerView) oldFocus).getSelection()));
        	}
            //recyclerview焦点获取蓝边显示item放大
            if(newFocus instanceof SearchRecyclerView || newFocus instanceof RecommendRecyclerView){
            	FocusRecyclerView recyclerView = (FocusRecyclerView) mViewGroup;
            	if(recyclerView.getSelection() == 0 && recyclerView.getChildAt(0) != null){
            		mEffect.start(mBorderView, null, recyclerView.getChildAt(0), null, true);
            	}else{
            		recyclerView.setSelection(recyclerView.getSelection(), recyclerView.getSelection(),FocusRecyclerView.PLACEHOLDER_POSITION);
            	}
            }
            //焦点移出时选中哪个，回来还是对应的放大
            if(newFocus instanceof DramaListRecyclerView){
            	DramaListRecyclerView dlr = (DramaListRecyclerView) newFocus;
            	if(mFirstFocus){
            		mFirstFocus = false;
            		dlr.setSelection(0, 0,FocusRecyclerView.PLACEHOLDER_POSITION);
            	}else{
            		dlr.setSelection(dlr.getSelection(), dlr.getSelection(),FocusRecyclerView.PLACEHOLDER_POSITION);
            	}
            }
            //焦点移出时选中位置缩小
            if(oldFocus instanceof DramaListRecyclerView){
            	this.setVisibility(View.GONE);
        		AnimateFactory.zoomOutView(((FocusRecyclerView) oldFocus).getLayoutManager()
        				.findViewByPosition(((FocusRecyclerView) oldFocus).getSelection()));
            }
            
            

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        ownOldFocus = newFocus;

    }


    public void setFirstFocus(boolean b) {
        this.mFirstFocus = b;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown");

        return super.onKeyDown(keyCode, event);
    }

}
