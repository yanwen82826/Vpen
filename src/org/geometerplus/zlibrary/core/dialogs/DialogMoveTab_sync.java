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

import org.geometerplus.android.fbreader.DictionaryUtil;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
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

//文字註記Dialog
public class DialogMoveTab_sync extends Dialog {
	
	public FBReaderApp Reader;
	public FBReader ba;
	public LayoutParams p;
	
	//DBTable
	private String DBTextTable = ActionCode.ANNOTATION_TEXT;
	private String DBTextRangeTable = ActionCode.ANNOTATION_TEXT_RANGE;
	private String DBTTSTable = ActionCode.ANNOTATION_TTS_NUMBER;
	private String DBRecordTable = ActionCode.ANNOTATION_RECORD;
	
	//SaveValue
	private int index = 0;
	private int page = 0;
	private String user;
	private String srcPath_rec = "/sdcard/sync/"; //錄音路徑
	private String srcPath_pic = "/sdcard/sync/"; //圖片路徑
	public int l, t, r, b;  //左 上 右 下
	public String str;
	
	//Dialog視窗是否出現
	private boolean isDialog = true;
	
	//Button and View
	public LinearLayout recordlayout, ws, picLayout, piclayout_text, piclayout_record, shutterLayout;
	public ImageView iv, picture, picture_text, picture_record;
	public TextView textview, textempty, text;
	public RadioGroup radio;
	public SeekBar seekbarR, seekbarG, seekbarB;
	public View colorBox;
	public Button /*voiceBtn, (電腦發音)*/vPlayBtn, vStopBtn
					, color_r, color_o, color_y, color_g, color_b, color_gray;
	
	//軟鍵盤
	//private InputMethodManager imm;
	
	//voice
	//private MediaRecorder mediaRecorder = null;
	private MediaPlayer mediaPlayer = null;
	private String fileName, userName, picFileName;
	
	
	//boolean( 錄音中不能切換tab )
	boolean IschangeTab = true;
	String strWord;
	
	//tabHost
	private TabHost tabHost;
	
	public String noteString;   //被標註的文字內容
	
	
	public DialogMoveTab_sync(Context context, int theme) {
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

		
		final SyncDB db = ZLApplication.Instance().syncdb;
		//抓行數( 幾行需要畫 )
		int drawNumber = db.getNoteRange(index, "left", DBTextRangeTable, user);
		
		for( int j=0;j<drawNumber;j++ ) {
			//方塊
			int l = db.getRange(index, "left", DBTextRangeTable, j, user);
			int r = db.getRange(index, "right", DBTextRangeTable, j, user);
			int t = db.getRange(index, "top", DBTextRangeTable, j, user);
			int b = db.getRange(index, "bottom", DBTextRangeTable, j, user);
			
			if( x+p.x+470+range >= l && x+p.x+470-range <= r && y+p.y+250+range >= t && y+p.y+250-range <= b )  //是否在選取範圍
			{
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
	    //TabHost.TabSpec spec, spec2;  // Resusable TabSpec for each tab
	    tabHost.setup();
	    tabHostSet(tabHost);
		
		try{
			//設定view
			setViewId();
			//初始化
			setBase();
			//設定textview
			setTextView(textview);
			//設定按鈕
			setBtn();
			//設定dialog位置
			if( isPicture() ) {
				setLocation(400, 340);//寬、高
			}else {
				setLocation(400, 230);//寬、高
			}
		}
		catch(Exception e) {
			Log.i("log", "test(DialogMove) e = "+e);
		}
		
	}
	
	private void setBase() {
		this.index = SaveValue.nowIndex;
		this.page = SaveValue.pageIndex;
		this.user = SaveValue.SyncUserName;
		this.srcPath_rec = this.srcPath_rec+SaveValue.SyncUserName+"/rec/";
		this.srcPath_pic = this.srcPath_pic+SaveValue.SyncUserName+"/pic/";
		this.picFileName = index+"_"+user+"_TEXT_picture.jpg";
		Log.i("test", "test picFileName = "+picFileName);
		
		//final SQLiteDB db = ZLApplication.Instance().db;
		final SyncDB db = ZLApplication.Instance().syncdb;
		//Dialog位置
		l = db.getRange(index, "left", DBTextRangeTable, 0, user);
		t = db.getRange(index, "top", DBTextRangeTable, 0, user);
		r = db.getRange(index, "right", DBTextRangeTable, 0, user);
		b = db.getRange(index, "bottom", DBTextRangeTable, 0, user);
		
		//Dialog內容
		str = db.getStrComment_text(index, "comment", "annotext", user);;
		
		//Dialog 錄音檔名
		//userName = db.getStrComment_text( index, "userid", "annotext", user);
		fileName = index +"_"+ user +"_"+ SaveValue.pageIndex + ".amr";
		
		//小鍵盤( 隱藏 )
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | 
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
		//偵測是否有值
		if( str != null )
			text.setText(str);					
		
//		//偵測是否語音
//		if( isRecord() ) {
//			//顯示音樂圖示
//			editlayout.removeAllViews();
//			editlayout.addView(iv);	
//			vPlayBtn.setEnabled(true);
//		}
		
		//偵測是否有照片
		if( isPicture() ) {
			//顯示音樂圖示
			picLayout.removeAllViews();
			picLayout.addView(picture);
			piclayout_text.addView(picture_text);
			piclayout_record.addView(picture_record);
			
			String str = srcPath_pic+picFileName;
			try {
				Bitmap bt = BitmapFactory.decodeFile(str);
				bt = zoomBitmap(bt,200,150);
				picture_text.setImageBitmap(bt);
				picture_record.setImageBitmap(bt);
				picture.setImageBitmap(bt);
				//bt.recycle();
			}catch(Exception e) {
				Log.i("test", "test e = "+e);
			}
			
			//偵測是否語音
			if( isRecord() ) {
				//顯示音樂圖示
				recordlayout.removeAllViews();
				recordlayout.addView(iv);	
				vPlayBtn.setEnabled(true);
			}
		} else {
			//偵測是否語音
			if( isRecord() ) {
				//顯示音樂圖示
				recordlayout.removeAllViews();
				recordlayout.addView(iv);	
				vPlayBtn.setEnabled(true);
			}
		}		
		
		//style
		noteString = db.getStrComment_text(index, "txt", "annotext", user);
	}

	private void tabHostSet(final TabHost tabHost) {
		
		tabHost.addTab(tabHost.newTabSpec("tab_1")
                .setContent(R.id.tab_context).setIndicator("註記"));
	    tabHost.addTab(tabHost.newTabSpec("tab_2")
                .setContent(R.id.tab_record).setIndicator("語音"));
	    tabHost.addTab(tabHost.newTabSpec("tab_4")
                .setContent(R.id.tab_takePic).setIndicator("照片"));
	    tabHost.addTab(tabHost.newTabSpec("tab_6")
                .setContent(R.id.tab_close).setIndicator("關閉"));
	    tabHost.setCurrentTab(0);//預設值

	    
	    tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
			public void onTabChanged(String tabId) {
				
				//關閉軟鍵盤
				//imm.hideSoftInputFromWindow(text.getWindowToken(), 0);
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
					if( isRecord() )
						setLocation(400, 230);
					else
						setLocation(400, 156);
				}
//				else if( "tab_4".equals(tabId) )          //翻譯
//				{
//					if( IschangeTab )
//					{
//						setLocation(400, 96);
//						SaveValue.IsNote = true; //開啟註記
//						dismiss();
//
//				        DictionaryUtil.openTextInDictionary(
//				        	ba,
//				        	noteString,
//							true,
//				        	t,
//				        	b
//						); 
//				        //紀錄動作
//						int temp = Reader.db.getTableCount(ActionCode.ANNOTATION_SYNC_TIME);
//						Reader.db.insertSyncAction(temp, "TEXT", SaveValue.nowIndex, "TRANSLATION");
//					}
//					else {
//						isNowRecord();
//						tabHost.setCurrentTabByTag("tab_2");
//					}
//				}
				else if( "tab_6".equals(tabId) )          //關閉
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
		
		piclayout_text = (LinearLayout) findViewById(R.id.picLayout_text);
		picture_text = (ImageView) findViewById(R.id.imagePicture_text);         //照片
		piclayout_text.removeView(picture_text);
		
		piclayout_record = (LinearLayout) findViewById(R.id.picLayout_record);
		picture_record = (ImageView) findViewById(R.id.imagePicture_record);         //照片
		piclayout_record.removeView(picture_record);
		
		//context
		text = (TextView) findViewById(R.id.dialog_context);	
		text.setMovementMethod(ScrollingMovementMethod.getInstance());  //滾動效果
		
		//record
		recordlayout = (LinearLayout) findViewById(R.id.recordLayout);
		iv = (ImageView) findViewById(R.id.imageRecord);               //語音圖
		textview = (TextView) findViewById(R.id.dialog_title);         //無語音
		recordlayout.removeView(iv);
		//record---btn
		//voiceBtn = (Button) findViewById(R.id.voiceBtn);
		//vRecordBtn = (Button) findViewById(R.id.voiceRecordBtn);
		vPlayBtn = (Button) findViewById(R.id.voicePlayBtn);
		vStopBtn = (Button) findViewById(R.id.voiceStopBtn);
		vPlayBtn.setEnabled(false);
		vStopBtn.setEnabled(false);
		
		//takePic----
		picLayout = (LinearLayout) findViewById(R.id.picLayout);
		picture = (ImageView) findViewById(R.id.imagePicture);               //照片
		//textview = (TextView) findViewById(R.id.dialog_title);
		picLayout.removeView(picture);
		
		
		//style
		radio = (RadioGroup) findViewById(R.id.radioStyle);
		ws = (LinearLayout) findViewById(R.id.tab_wordSetting);
		
		setViewEvent();	
	}

	private void setViewEvent() {
		
		//context
		//點選edittext才打開小鍵盤
		text.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) { 
                //text.setFocusableInTouchMode(true);   //關键盘 
                return false;
            }
		});
		
	}
	
	

	//設定位置
	private void setLocation(int width, int height) {
		int dialogWidth = width/2;
		int dialogHeight = height/2;
		
		LayoutParams p = this.getWindow().getAttributes();
		p.x = -1280/2 + l + dialogWidth;
		p.y = -743/2 + b + dialogHeight;
		if(str != null && !"".equals(str) || isRecord() )
			p.y += 25;   //18是註記高度
		
		if(p.y > 244)  //dialog位置太下面
			p.y = -743/2 + t - 120;
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
	
	public void setBtn() {
		
		//------------------------Voice------------------------
		
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
		
//		//computer voice
//		voiceBtn.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				computerVoice();
//			}
//		});
		
		picture.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				PicturePreview ppreview = new PicturePreview(ba, R.style.TANCStyle, srcPath_pic+picFileName);
				//PicturePreview ppreview = new PicturePreview(ba, srcPath_pic+picFileName);
				//LinearLayout markLayout = (LinearLayout) ppreview.getLayoutInflater().inflate(R.layout.tabhost_main, null);
				ppreview.requestWindowFeature(Window.FEATURE_NO_TITLE); //要放在setContentView之前
				ppreview.setFBReader(ba);
				//ppreview.setContentView(markLayout);
				ppreview.setCancelable(false);
				//ppreview.setPicture(srcPath_pic+fileName);
				ppreview.show();
			}
		});
	}
	
//	//computer voice
//	private void computerVoice() {
//		try {
//			String txt = this.Reader.syncdb.getStrComment_text( (index), "txt", DBTextTable, user);
//			
//			if (txt.length() > 0) {
//				this.Reader.tts.speak(txt, TextToSpeech.QUEUE_FLUSH, null);
//				this.Reader.db.insertTTSDate( (index), SaveValue.UserName );
//			}
//			
//		} catch (Exception e) {
//			Log.i("log", "test(DialogMove) computerVoice ERROR = "+e.toString());
//		}
//	}
	
	private void recordPlay() {
		
		String recodeFileTemp = null;
		
		try {
			File SDCardpath = Environment.getExternalStorageDirectory();
			//String sdcardroot = SDCardpath.toString();
			recodeFileTemp = srcPath_rec;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String tempfile = recodeFileTemp + fileName;

		mediaPlayer = new MediaPlayer();
		try {
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
		Reader.syncdb.insertRecordDate( (index), "TEXT", SaveValue.UserName, user);
		//紀錄動作
		int temp = Reader.db.getTableCount(ActionCode.ANNOTATION_SYNC_TIME);
		Reader.db.insertSyncAction(temp, "TEXT", SaveValue.nowIndex, "RECORD");
		
		recordSetting("vPlayBtn");
		mediaPlayer.setOnCompletionListener(playerlistener);
	}
	
	private void recordStop() {
		
		//播放中
		if(mediaPlayer != null) {
			if(mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
				//更新結束時間
				final SyncDB db = ZLApplication.Instance().syncdb;
				int id = db.getRecordID(index, "TEXT");
				db.updateRecordDateEnd(id, DBRecordTable);
				
				vPlayBtn.setText("播放");
				vPlayBtn.setEnabled(true);
				vStopBtn.setEnabled(false);
				IschangeTab = true; //能切換Tab
			}
		}
			
	}
	
	private void recordSetting(String btnName) {
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
			
			IschangeTab = true; //其他TAB可以按
			if( isRecord() ) {
				vPlayBtn.setEnabled(true);
				//voiceBtn.setEnabled(true);
			}
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
			final SyncDB db = ZLApplication.Instance().syncdb;
			int id = db.getRecordID(index, "TEXT");
			db.updateRecordDateEnd(id, DBRecordTable);
		}
	};
	
	private boolean isRecord() {
		final SyncDB db = ZLApplication.Instance().syncdb;
		if( db.getRecord_text( index, "rec", DBTextTable, user ) == null )
			return false;
		else
			return true;
	}
	
	private boolean isPicture() {
		//final SQLiteDB db = ZLApplication.Instance().db;
		//String str = db.getStrComment((index), "pic", DBTextTable, SaveValue.UserName);
		final SyncDB db = ZLApplication.Instance().syncdb;
		String str = db.getStrComment_text((index), "pic", DBTextTable, user);
		//String picFileName = index+"_"+SaveValue.UserName+"_TEXT_picture.jpg";
		//if(bitmap == null) {
		Log.i("test", "test str = "+str);
		if( picFileName.equals(str)) {
			return true;
		}
		else
			return false;
	}
	

	//setFBReaderApp
	public void setFBReaderApp(FBReaderApp reader) {
		this.Reader = reader;
	}
	
	//setFBReader
	public void setFBReader(FBReader ba) {
		this.ba= ba;
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
	
	//放大缩小图片
	public static Bitmap zoomBitmap(Bitmap bitmap,int w,int h){
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidht = ((float)w / width);
		float scaleHeight = ((float)h / height);
		matrix.postScale(scaleWidht, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		return newbmp;
	}	
	
	/*@Override
	public void dismiss(){
		vRecordBtn.setEnabled(true);
		vPlayBtn.setText("播放");
		vPlayBtn.setEnabled(true);
		vStopBtn.setEnabled(false);
		
		IschangeTab = true; //其他TAB可以按
		SaveValue.IsNote = true; //開啟註記key
		
		Reader.db.insertRecordDate( (index), DBRecordTable );
	}*/
}
