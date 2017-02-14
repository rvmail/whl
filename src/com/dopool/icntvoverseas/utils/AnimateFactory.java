package com.dopool.icntvoverseas.utils;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import com.dopool.icntvoverseas.view.focusrecyclerview.DramaListRecyclerView;


public class AnimateFactory {
    /**
     * 缩放动画,用于缩放控件
     *
     * @param startScale 控件的起始尺寸倍率
     * @param endScale   控件的终点尺寸倍率
     * @return
     */
    public static Animation zoomAnimation(float startScale, float endScale, int duration) {
        ScaleAnimation anim = new ScaleAnimation(startScale, endScale, startScale, endScale,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setFillAfter(true);
        anim.setDuration(duration);
        return anim;
    }

    public static void zoomInView(View v) {
        zoomInView(v, 1.1f);
    }

    public static void zoomOutView(View v) {
        zoomOutView(v, 1.1f);
    }

    public static void zoomInView(View v, float zoomSize) {
        zoomInView(v,zoomSize,0, null);
    }

    public static void zoomOutView(View v, float zoomSize) {
        zoomOutView(v,zoomSize,0);
    }
    public static void zoomInView(final View v, float zoomSize,int duration, final View view) {
        if (v != null) {
        	ViewParent parent = v.getParent();
        	if(parent instanceof DramaListRecyclerView){
        		if(!((DramaListRecyclerView)parent).isFocused()){
        			return;
        		}
        	}
        	Animation anim = AnimateFactory.zoomAnimation(1.0f, zoomSize,duration);
        	anim.setAnimationListener(new Animation.AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					ViewParent parent = v.getParent();
		        	if(parent instanceof DramaListRecyclerView){
		        		if(!((DramaListRecyclerView)parent).isFocused()){
		        			return;
		        		}
		        	}
					if(view != null) view.setVisibility(View.VISIBLE);
					if(((ViewGroup)v).getChildAt(1) instanceof TextView){
						TextView textView = (TextView) ((ViewGroup)v).getChildAt(1);
						textView.setSelected(true);
					}
					
				}
			});
            v.startAnimation(anim);
        }
    }
    
    public static void zoomOutView(final View v, float zoomSize,int duration) {
        if (v != null) {
        	Animation anim = AnimateFactory.zoomAnimation(zoomSize, 1.0f,duration);
            anim.setAnimationListener(new Animation.AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					if(((ViewGroup)v).getChildAt(1) instanceof TextView){
						TextView textView = (TextView) ((ViewGroup)v).getChildAt(1);
						textView.setSelected(false);
					}
				}
			});
            v.startAnimation(anim);
        }
    }
    
    public static final int ANIMATION_DEFAULT = 0;
    public static final int ANIMATION_TRANSLATE = 1;

}
