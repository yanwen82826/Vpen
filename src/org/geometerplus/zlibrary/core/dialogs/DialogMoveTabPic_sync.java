/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.core.dialogs;

import java.io.File;
import java.io.IOException;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.sqliteconfig.SQLiteDB;
import org.geometerplus.zlibrary.core.sqliteconfig.SyncDB;
import org.geometerplus.zlibrary.text.view.SaveValue;
import com.thirdEdition.geometerplus.zlibrary.ui.android.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.view.WindowManager.LayoutParams;

public class DialogMoveTabPic_sync extends Dialog {	
	public FBReaderApp Reader;
	public LayoutParams p;
	
	//Table
	private String DBImageTable = ActionCode.ANNOTATION_IMAGE;
	private String recordTable = ActionCode.ANNOTATION_RECORD;
	private String annoType;
	
	//SaveValue
	public int index = 0;
	private int page = 0;
	private String srcPath_rec = "/sdcard/sync/"; //錄音路徑
	private String srcPath_pic = "/sdcard/sync/"; //圖片路徑
	private String user;
	public int l, t;
	public String str;
	
	//Dialog視窗是否出現
	private boolean isDialog = true;
	
	//Button and View
	public LinearLayout editlayout, ws;
	public ImageView iv;
	public TextView textview, textempty, text;
	public RadioGroup radio;
	public SeekBar seekbarR, seekbarG, seekbarB;
	public View colorBox;
	public Button voiceBtn, vPlayBtn, vStopBtn;
	
	//軟鍵盤
	//public InputMethodManager imm;
	
	//voice
	public MediaRecorder mediaRecorder = null;
	public MediaPlayer mediaPlayer = null;
	public String fileName, userName;
	
	//boolean( 錄音中不能切換tab )
	public boolean IschangeTab = true;
	String strWord;
	
	//tabHost
	public TabHost tabHost;
	
	
	public DialogMoveTabPic_sync(Context context, int theme) {
		super(context, theme);
		p = getWindow().getAttributes();
	}

	@Override
	public boolean onTouchEvent( MotionEvent event ) {
		
		float x = event.getX();
		float y = event.getY();
		
		if( !IschangeTab ) {
			isNowRecord();
			return false;
		}
		
		//範圍大小
		int range = 15;
		
		if( p.x < -470 )
			p.x = -470;
		else if( p.x > 470 )
			p.x = 470;
		else if( p.y < -250 )
			p.y = -250;
		else if(p.y > 250)
			p.y = 250;

		if( index != -1 )
		{	
			if( (x+p.x+470+range >= l) && (x+p.x+470-range <= l+20+SaveValue.wordWidth )
					&& (y+p.y+250+range >= t) && (y+p.y+250-range <= t+20)  )
			{
				//saveEditText();
				Reader.getTextView().clear();
				SaveValue.IsNote = true; //開啟註記key
				dismiss();
			}
		}
		
		return true;
	}
	
	@Override
	public void setContentView(View view) {
		super.setContentView(view);
		
		//imm = ((InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE));
		
		tabHost = (TabHost) this.findViewById(R.id.tab_host);  // The activity TabHost
	    tabHost.setup();
	    tabHostSet(tabHost, 0);
	    
		try{
			//設定view
			setViewId();
			//初始化
			setBase();
			//設定標題欄
			setTextView(textview);
			//設定按鈕
			setBtn();
			//設定dialog位置
			setLocation(400, 230);//寬、高
		}
		catch(Exception e) {
			Log.i("log", "test(DialogMove) e = "+e);
		}
	}
	
	//初始化
	private void setBase() {
		this.index = SaveValue.picNowIndex;
		this.page = SaveValue.pageIndex;
		this.user = SaveValue.SyncUserName;
		this.srcPath_rec = this.srcPath_rec+SaveValue.SyncUserName+"/rec/";
		this.srcPath_pic = this.srcPath_pic+SaveValue.SyncUserName+"/pic/";
		
		final SyncDB db = ZLApplication.Instance().syncdb;
		if(!FBReader.setImage){
			//Dialog位置
			l = db.getIntData(index, "sX", DBImageTable, user); //X
			t = db.getIntData(index, "sY", DBImageTable, user); //Y

			//Dialog內容
			str = db.getStrComment(index, "comment", DBImageTable, user);
			
			//Dialog 錄音檔名
			userName = db.getStrComment( index, "userid", DBImageTable);
			fileName = index +"_"+ userName +"_"+ SaveValue.pageIndex + "_picNote.amr";
			
			//小鍵盤( 隱藏 )
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | 
	                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
			
			//偵測是否有值
			if( str != null )
				text.setText(str);		
			
			//偵測是否語音
			if( isRecord(DBImageTable) ) {
				//顯示音樂圖示
				editlayout.removeAllViews();
				editlayout.addView(iv);	
				vPlayBtn.setEnabled(true);
			}		
		}
		else{
			this.srcPath_rec = "/sdcard/sync/"+SaveValue.SyncUserName+"/"+SaveValue.nowLesson+"/recAnno/";
			//Dialog位置
			l = db.getIntData(index, "sX", "takepic_anno", user); //X
			t = db.getIntData(index, "sY", "takepic_anno", user); //Y
			System.out.println("註記位置X: " + l + " 註記位置Y: " + t);

			//Dialog內容
			str = db.getStrComment(index, "comment", "takepic_anno", user);
			
			//Dialog 錄音檔名
			userName = db.getStrComment( index, "userid", "takepic_anno");
			fileName = index +"_"+ SaveValue.nowLesson+"_"+userName +"_recAnno_picNote.amr";
			
			//小鍵盤( 隱藏 )
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | 
	                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
			
			//偵測是否有值
			if( str != null )
				text.setText(str);		
			
			//偵測是否語音
			if( isRecord("takepic_anno") ) {
				//顯示音樂圖示
				editlayout.removeAllViews();
				editlayout.addView(iv);	
				vPlayBtn.setEnabled(true);
			}		
		}
	}
		

	private void tabHostSet(final TabHost tabHost, int key) {
		
		tabHost.addTab(tabHost.newTabSpec("tab_1")
                .setContent(R.id.tab_context).setIndicator("註記"));
	    tabHost.addTab(tabHost.newTabSpec("tab_2")
                .setContent(R.id.tab_record).setIndicator("語音"));
	    tabHost.addTab(tabHost.newTabSpec("tab_4")
                .setContent(R.id.tab_close).setIndicator("關閉"));
	    tabHost.setCurrentTab(key);//預設值

	    
	    tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
			public void onTabChanged(String tabId) {
				
				//關閉軟鍵盤
				//imm.hideSoftInputFromWindow(edittext.getWindowToken(), 0);
				text.setFocusableInTouchMode(false);

				if( "tab_1".equals(tabId) )               //註記
				{
					if( IschangeTab )
						setLocation(400, 230);
					else {
						isNowRecord();
						tabHost.setCurrentTabByTag("tab_2");
					}		
				}
				else if( "tab_2".equals(tabId) )          //語音
				{
					if( isRecord(DBImageTable) )
						setLocation(400, 230);
					else
						setLocation(400, 156);
				}
				else if( "tab_4".equals(tabId) )          //關閉
				{
					if( IschangeTab )
					{
						setLocation(400, 96);
						Reader.getTextView().clear();
						SaveValue.IsNote = true; //開啟註記key
						dismiss();
					}
					else {
						isNowRecord();
						tabHost.setCurrentTabByTag("tab_2");
					}
				}
			}
		});
	}
	
	private void setViewId() {
		
		//context
		text = (TextView) findViewById(R.id.dialog_context);
		
		//record
		editlayout = (LinearLayout) findViewById(R.id.editLayout);
		iv = (ImageView) findViewById(R.id.imageRecord);               //語音圖
		textview = (TextView) findViewById(R.id.dialog_title);         //無語音
		editlayout.removeView(iv);
		//record---btn
		vPlayBtn = (Button) findViewById(R.id.voicePlayBtn);
		vStopBtn = (Button) findViewById(R.id.voiceStopBtn);
		vPlayBtn.setEnabled(false);
		vStopBtn.setEnabled(false);
		
		setViewEvent();	
	}

	private void setViewEvent() {
		//context
		//點選edittext才打開小鍵盤
		text.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) { 
                //edittext.setFocusableInTouchMode(true);   //關键盘 
                return false;
            }
		});
	}
	
	//設定位置
	protected void setLocation(int width, int height) {
		int dialogWidth = width/2;
		int dialogHeight = height/2;
		
		LayoutParams p = this.getWindow().getAttributes();
		p.x = -1280/2 + l + dialogWidth;
		p.y = -743/2 + t + dialogHeight;
		//if(SaveValue.picEditStr[index] != null && !"".equals(SaveValue.picEditStr[index]) || isRecord() )
			p.y += 25;   //18是註記高度
		
		if(p.y > 244)  //dialog位置太下面
			p.y = -743/2 + t -120;
		
		this.onWindowAttributesChanged(p);
	}

	//setTextView
	public void setTextView(TextView textview) {
		
		//textview
		textview.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				//onClick
			}
		});
		
		textview.setOnTouchListener(new View.OnTouchListener() {
			
			LayoutParams p = getWindow().getAttributes();
			
			//標註按下去時
			public boolean onTouch(View v, MotionEvent event) {
				
				float x = event.getX();
				float y = event.getY();
				
				try{
					switch(event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							dialogMove(x, y);
							break;
						case MotionEvent.ACTION_MOVE:
							dialogMove(x, y);
							break;
						case MotionEvent.ACTION_UP:
							dialogMove(x, y);
							break;
					}
				}
				catch(Exception e) {
					Log.i("error", e.toString());
				}
				return false;
			}
			
			//移動Dialog
			public void dialogMove(float x, float y) {
				
				p.x += (int) x-150;
				p.y += (int) y;
				
				if( p.x < -640 )
					p.x = -640;
				else if( p.x > 640 )
					p.x = 640;
				else if( p.y < -371 )
					p.y = -371;
				else if(p.y > 250)
					p.y = 250;
				
				//1280 743
				getWindow().setAttributes(p);
			}
		});
		
	}
	
	//按鈕設定
	public void setBtn() {
		
		//play
		vPlayBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				recordPlay();
			}
		});		
		
		//stop
		vStopBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				recordStop();
			}
		});
	}	
	
	private void recordPlay() {
		
		String recodeFileTemp = null;
		
		try {
			File SDCardpath = Environment.getExternalStorageDirectory();
			//String sdcardroot = SDCardpath.toString();
			recodeFileTemp = srcPath_rec;
		} catch (Exception e) {
			e.printStackTrace();
		}

		final SyncDB db = ZLApplication.Instance().syncdb;
		if(FBReader.setImage == false){
			fileName = db.getStrComment(index, "rec", DBImageTable);
		}
		else{
			fileName = db.getStrComment(index, "rec", "takepic_anno");
		}
		String tempfile = recodeFileTemp + fileName;
		//Log.i("test", "filename = "+fileName);
		Log.i("@@@@@@@@@audio1", tempfile);
		mediaPlayer = new MediaPlayer();
		try {
			Log.i("@@@@@@@@@audio2", tempfile);
			mediaPlayer.setDataSource(tempfile);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			mediaPlayer.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mediaPlayer.start();
		//新增註記紀錄
		Reader.syncdb.insertRecordDate(index, this.annoType, SaveValue.UserName, user);
		//紀錄動作
		int temp = Reader.db.getTableCount(ActionCode.ANNOTATION_SYNC_TIME);
		Reader.db.insertSyncAction(temp, annoType, SaveValue.nowIndex, "RECORD");
		if(FBReader.setImage == false){
			recordSetting("vPlayBtn",DBImageTable);
		}
		else{
			recordSetting("vPlayBtn","takepic_anno");
		}
		
		mediaPlayer.setOnCompletionListener(playerlistener);
	}
	
	private void recordStop() {
		
		//播放中
		if(mediaPlayer != null) {
			if(mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
				//更新結束時間
				int id = Reader.syncdb.getRecordID(index, annoType);
				Reader.syncdb.updateRecordDateEnd(id, recordTable);
				
				vPlayBtn.setText("播放");
				vPlayBtn.setEnabled(true);
				vStopBtn.setEnabled(false);
				IschangeTab = true; //能切換Tab
			}
		}

	}
	
	protected void recordSetting(String btnName, String tableName) {
		IschangeTab = true; //能切換Tab
		if( btnName.toString().equals("vPlayBtn") ) {
			//IschangeTab
			IschangeTab = false; //不能切換Tab
			strWord = "播放";
			vPlayBtn.setText("播放中...");
			vPlayBtn.setEnabled(false);
			vStopBtn.setEnabled(true);
		}
		else if( btnName.toString().equals("vStopBtn") ) {
			IschangeTab = true; //能切換Tab

			if( isRecord(tableName));
				vPlayBtn.setEnabled(true);
			vStopBtn.setEnabled(false);
		}
	}
	
	//恢復原狀( 播放完畢後 )
	MediaPlayer.OnCompletionListener playerlistener = new MediaPlayer.OnCompletionListener() {
		public void onCompletion(MediaPlayer mp) {
			vPlayBtn.setText("播放");
			vPlayBtn.setEnabled(true);
			vStopBtn.setEnabled(false);
			
			IschangeTab = true; //其他TAB可以按
			
			//更新結束時間
			//final SyncDB db = ZLApplication.Instance().syncdb;
			int id = Reader.syncdb.getRecordID(index, annoType);
			Reader.syncdb.updateRecordDateEnd(id, recordTable);
		}
	};
	
	private boolean isRecord(String tableName) {
		final SyncDB db = ZLApplication.Instance().syncdb;
		if( db.getRecord(index, "rec", tableName ) == null )
			return false;
		else
			return true;
	}
	

	//setFBReader
	public void setFBReaderApp(FBReaderApp reader) {
		this.Reader = reader;
	}
		
	
	//確認視窗( record進行中 )
	private void isNowRecord() {
		if( isDialog ) {
			isDialog = false;
			new AlertDialog.Builder(getContext()).setTitle("警告").setMessage("目前正在"+strWord+"中...\n請按停止"+strWord+"，才能執行您的動作!").setPositiveButton("確定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					isDialog = true;
					return;
				}
			}).show();
		}
	}
	
	public void changeDbTable(String annType) {
		this.annoType = annType;
	}
}
