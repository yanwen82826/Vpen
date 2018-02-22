package org.geometerplus.zlibrary.text.view;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class MyThread implements Runnable {
		private int interval = 300; //default
		private Handler mHandler = null; //default
		private final int ID_USER = 0;
		private String str = "";
		
		public MyThread(int time, Handler h, String s) {
			interval = time;
			mHandler = h;
			str = s;
		}
		
		public void run() {
			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Message msg = new Message();     
            msg.what = ID_USER;
            Bundle data = new Bundle();
            data.putInt(str, 300);
            msg.setData(data);
        
            mHandler.sendMessage(msg);
         }
}