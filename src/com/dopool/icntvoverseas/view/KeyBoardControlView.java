package com.dopool.icntvoverseas.view;

import java.util.ArrayList;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.TextView;

import com.dopool.icntvoverseas.R;

public class KeyBoardControlView {

	private Activity mActivity;
	
	private ArrayList<TextView> keyViews=new ArrayList<TextView>() ;
	
	private KeySelectedListener mKeySelectedListener;
	
	private String[] keys=new String[]{"ABC","DEF","GHI","JKL","MNO","PQR","STU","VWX","YZ0","123","456","789"};
	
	private KeyBoardOnKeyListener mOnkeyListener=new KeyBoardOnKeyListener();
	public KeyBoardControlView (Activity activity){
		this.mActivity=activity;
		initView();
		setKeyListener();
	}


	private void initView() {
		TextView abcTv=(TextView) mActivity.findViewById(R.id.search_tv_keyboard_abc);
		TextView defTv=(TextView) mActivity.findViewById(R.id.search_tv_keyboard_def);
		TextView ghiTv=(TextView) mActivity.findViewById(R.id.search_tv_keyboard_ghi);
		TextView jklTv=(TextView) mActivity.findViewById(R.id.search_tv_keyboard_jkl);
		TextView mnoTv=(TextView) mActivity.findViewById(R.id.search_tv_keyboard_mno);
		TextView pqrTv=(TextView) mActivity.findViewById(R.id.search_tv_keyboard_pqr);
		TextView stuTv=(TextView) mActivity.findViewById(R.id.search_tv_keyboard_stu);
		TextView vwxTv=(TextView) mActivity.findViewById(R.id.search_tv_keyboard_vwx);
		TextView yz0Tv=(TextView) mActivity.findViewById(R.id.search_tv_keyboard_yz0);
		TextView num123Tv=(TextView) mActivity.findViewById(R.id.search_tv_keyboard_123);
		TextView num456Tv=(TextView) mActivity.findViewById(R.id.search_tv_keyboard_456);
		TextView num789Tv=(TextView) mActivity.findViewById(R.id.search_tv_keyboard_789);
		keyViews.add(abcTv);
		keyViews.add(defTv);
		keyViews.add(ghiTv);
		keyViews.add(jklTv);
		keyViews.add(mnoTv);
		keyViews.add(pqrTv);
		keyViews.add(stuTv);
		keyViews.add(vwxTv);
		keyViews.add(yz0Tv);
		keyViews.add(num123Tv);
		keyViews.add(num456Tv);
		keyViews.add(num789Tv);
		}
	
	private void setKeyListener() {
		for (int i = 0; i < keyViews.size(); i++) {
			keyViews.get(i).setTag(keys[i]);
			keyViews.get(i).setOnKeyListener(mOnkeyListener);			
		}
	}
	
	public class KeyBoardOnKeyListener implements OnKeyListener{

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			boolean onKey=false;
			if (v instanceof TextView&&KeyEvent.ACTION_DOWN==event.getAction()) {
				TextView tv=(TextView) v;
				if (KeyEvent.KEYCODE_DPAD_UP==keyCode&&mKeySelectedListener!=null&&tv.getTag()!=null) {
					String str=(String) tv.getTag();
					mKeySelectedListener.onKeySelectedListener(String.valueOf(str.charAt(0)));
					onKey=true;
				}else if ((KeyEvent.KEYCODE_ENTER==keyCode||KeyEvent.KEYCODE_DPAD_CENTER==keyCode)&&mKeySelectedListener!=null&&tv.getTag()!=null) {
					String str=(String) tv.getTag();
					mKeySelectedListener.onKeySelectedListener(String.valueOf(str.charAt(1)));
					onKey=true;
				}else if (KeyEvent.KEYCODE_DPAD_DOWN==keyCode&&(mKeySelectedListener!=null&&tv.getTag()!=null) ){
					String str=(String) tv.getTag();
					mKeySelectedListener.onKeySelectedListener(String.valueOf(str.charAt(2)));
					onKey=true;
				}else {
					onKey=false;
				}
			}
			return onKey;
		}
		
	}
	public void setKeySelectedListener(KeySelectedListener keySelectedListener) {
		this.mKeySelectedListener = keySelectedListener;
	}

	public interface KeySelectedListener{
		void onKeySelectedListener(String key);
	}
}
