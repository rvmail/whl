package com.dopool.icntvoverseas.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
/**
 * home键的监听（home键为系统键，在dispatchKeyEvent或onKeyDown方法里处理无效）
 * @author Alisa
 *
 */
public class HomeKeyListener{
	private Context mContext;
	private IntentFilter mFilter;
	private OnHomeKeyPressListener mOnHomeKeyPressListener;
	private HomeKeyReceiver mReceiver;

    public HomeKeyListener(Context mContext) {
        this.mContext = mContext;
        mReceiver = new HomeKeyReceiver();
        mFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
    }
    
    /**
     * 开始监听，注册广播
     */
    public void start(){
    	if (mReceiver!=null) {
    		mContext.registerReceiver(mReceiver, mFilter);
		}
    }
    
    /**
     * 停止监听，注销广播
     */
    public void stop(){
    	if (mReceiver != null) {
    		mContext.unregisterReceiver(mReceiver);
		}
    }
    
    public void setOnHomeKeyPressListener(OnHomeKeyPressListener listener) {
        mOnHomeKeyPressListener = listener;
    }

    // 回调接口
    public interface OnHomeKeyPressListener {
        void onHomeKeyPress();
    }
    
    // 广播接收者
    class HomeKeyReceiver extends BroadcastReceiver{
        private final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
				if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
					if (mOnHomeKeyPressListener!=null) {
						mOnHomeKeyPressListener.onHomeKeyPress();
					}
				}
			}
		}
    }
}
