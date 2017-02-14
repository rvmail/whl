package com.dopool.icntvoverseas.view.boderview;

import android.animation.AnimatorSet;
import android.animation.FloatEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.dopool.icntvoverseas.utils.AnimateFactory;

/**
 * author:ly
 */
public abstract class BorderBaseEffect {
    protected boolean mScalable = true;
    protected float mScale = 1.1f;
    protected long mDurationLarge = 0;
    protected long mDurationSmall = 0;
    protected long mDurationTraslate = 100;
    protected int mMargin = 0;
    protected View mView;
    protected int focusCenterOffest = 0;   

    private AnimatorSet mAnimatorSet;{
        mAnimatorSet = new AnimatorSet();
    }
    
    private Context context;

    public static BorderBaseEffect getDefault(final int screenWidth,
    		final int screenHeight, final int screenMarginTop) {
        BorderBaseEffect borderBaseEffect = new BorderBaseEffect() {
            private View mOldFocus;
            private View mNewFocus;
            private ObjectAnimator transAnimatorX;
            private ObjectAnimator transAnimatorY;
            private ObjectAnimator scaleX;
            private ObjectAnimator scaleY;
            
            private int newX = 0;
            private int newY = 0;
            private int newWidth = 0;
            private int newHeight = 0;
            private int oldX = 0;
            private int oldY = 0;
            private int oldWidth = 0;
            private int oldHeight = 0;
            int[] newLocation = new int[2];
            int[] oldLocation = new int[2];

            private FloatEvaluator floatEvaluator;

            private DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator(1);


            @Override
            protected void setupAnimation(View view, View oldFocus, View newFocus, View tempFocus,boolean isHorizontalScroll) {
	            	view.setVisibility(View.GONE);
	                mOldFocus = oldFocus;
	                mNewFocus = newFocus;
	                mView = view;
	                getLocation(tempFocus);
                    oldWidth += mMargin * 2;
                    oldHeight += mMargin * 2;
                    newWidth += mMargin * 2;
                    newHeight += mMargin * 2;
                    newX = newX - mMargin;
                    newY = newY - mMargin- focusCenterOffest;
                    oldX = oldX - mMargin;
                    oldY = oldY - mMargin;
//                    Log.i("ly", "  "+((newX + newWidth) < screenWidth && newX > 0));
                    if(!((newX + newWidth) < screenWidth && newX > 0)){
//                    	Log.i("ly", "newX="+newX+";newWidth="+newWidth+";screenWidth="+screenWidth);
                    }
                    if(isHorizontalScroll && ((newX + newWidth) < screenWidth && newX > 0)){
                    	borderviewTranslate(view);
                    }else if (!isHorizontalScroll && ((newY + newHeight) < screenHeight && newY > screenMarginTop)){
                    	borderviewTranslate(view);
                    }
                    oldWidth -= mMargin * 2;
                    oldHeight -= mMargin * 2;
                    newWidth -= mMargin * 2;
                    newHeight -= mMargin * 2;
                    newX = newX + mMargin;
                    newY = newY + mMargin + focusCenterOffest;
                    oldX = oldX + mMargin;
                    oldY = oldY + mMargin;


                if (this.mScalable) {
                    if (oldFocus == null) {
                        if (newFocus != null){
                        	AnimateFactory.zoomInView(newFocus, this.mScale, (int) this.mDurationLarge, view);
                        }
                    } else {
                    	AnimateFactory.zoomOutView(oldFocus, this.mScale, (int) this.mDurationSmall);
                    	if (newFocus != null){
                    		AnimateFactory.zoomInView(newFocus, this.mScale, (int) this.mDurationLarge, view);
                    	}
                    }
                }

            }
            
            private void borderviewTranslate(View view){
            	if (transAnimatorX == null) {
                    transAnimatorX = ObjectAnimator.ofFloat(view,
                            "x", oldX, newX);
                    transAnimatorY = ObjectAnimator.ofFloat(view,
                            "y", oldY, newY);

                    WrapView wrapView=new WrapView(view);
                    scaleX = ObjectAnimator.ofInt(wrapView,
                            "width", oldWidth, newWidth);
                    scaleY = ObjectAnimator.ofInt(wrapView,
                            "height", oldHeight, newHeight);
                    floatEvaluator = new FloatEvaluator();
                    getAnimatorSet().playTogether(transAnimatorX, transAnimatorY, scaleX, scaleY);

                } else {
                    transAnimatorX.setEvaluator(floatEvaluator);
                    transAnimatorY.setEvaluator(floatEvaluator);

                    transAnimatorX.setFloatValues(oldX, newX);
                    transAnimatorY.setFloatValues(oldY, newY);
                    scaleX.setIntValues(oldWidth, newWidth);
                    scaleY.setIntValues(oldHeight, newHeight);

                }
                getAnimatorSet().setDuration(0);
                getAnimatorSet().start();
            }

            private void getLocation(View tempFocus) {
            	View focus;
            	if(tempFocus != null){
            		focus = tempFocus;
            	}else{
            		focus = mNewFocus;
            	}
                if (focus != null) {
                	focus = ((RelativeLayout)focus).getChildAt(0);
                	focus.getLocationOnScreen(newLocation);
                    if (mScalable) {
                        newWidth = (int) ((float) focus.getMeasuredWidth() * mScale);
                        newHeight = (int) ((float) focus.getMeasuredHeight() * mScale);
                        newX = newLocation[0] + (focus.getMeasuredWidth() - newWidth) / 2;
                        newY = newLocation[1] + (focus.getMeasuredHeight() - newHeight) / 2;
                    } else {
                        newWidth = focus.getMeasuredWidth();
                        newHeight = focus.getMeasuredHeight();
                        newX = newLocation[0];
                        newY = newLocation[1];
                    }

                }
                if (mOldFocus != null && !(mOldFocus instanceof GridView || mOldFocus instanceof ListView || mOldFocus instanceof RecyclerView)) {
                	mOldFocus = ((RelativeLayout)mOldFocus).getChildAt(0);
                    mOldFocus.getLocationOnScreen(oldLocation);
                    oldX = oldLocation[0];
                    oldY = oldLocation[1];

                    oldWidth = mOldFocus.getMeasuredWidth();
                    oldHeight = mOldFocus.getMeasuredHeight();

                } 
            }

        };
        return borderBaseEffect;
    }

    private class WrapView {
        private View view;
        private int width;
        private int height;

        public WrapView(View view) {
            this.view = view;
        }

        public int getWidth() {
            return view.getLayoutParams().width;
        }

        public void setWidth(int width) {
            this.width = width;
            view.getLayoutParams().width = width;
            view.requestLayout();
        }

        public int getHeight() {
            return view.getLayoutParams().height;
        }

        public void setHeight(int height) {
            this.height = height;
            view.getLayoutParams().height = height;
            view.requestLayout();
        }
        
    }

    public boolean isScalable() {
        return mScalable;
    }

    public void setScalable(boolean scalable) {
        this.mScalable = scalable;
    }

    public float getScale() {
        return mScale;
    }

    public void setScale(float scale) {
        this.mScale = scale;
    }

    public int getMargin() {
        return mMargin;
    }

    public void setMargin(int mMargin, int offest) {
        this.mMargin = mMargin;
        this.focusCenterOffest = offest;
    }

    protected abstract void setupAnimation(View view, View oldFocus, View newFocus, View tempFocus, boolean isHorizontalScroll);

    public void start(View view, View oldFocus, View newFocus, View tempFocus, boolean isHorizontal) {
        setupAnimation(view, oldFocus, newFocus, tempFocus, isHorizontal);
    }

    public void end() {
        mAnimatorSet.end();
        if (mView != null)
            mView.setVisibility(View.GONE);

    }

    public void cancle() {
        mAnimatorSet.cancel();
        if (mView != null)
            mView.setVisibility(View.GONE);
    }

    @SuppressLint("NewApi")
	public void pasue() {
        mAnimatorSet.pause();
    }

    public void resume() {
        mAnimatorSet.resume();
    }

    public AnimatorSet getAnimatorSet() {
        return mAnimatorSet;
    }
    
    public Context getContext(){
    	return context;
    }

    public void setDuration(long duration) {
        mDurationTraslate = duration;
        mDurationLarge = duration;
        mDurationSmall = duration;
    }

    public void setTraslateDuration(long duration) {
        mDurationTraslate = duration;
    }

    public void setScaleDuration(long duration) {
        mDurationLarge = duration;
        mDurationSmall = duration;
    }


}
