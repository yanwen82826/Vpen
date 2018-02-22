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
import org.geometerplus.zlibrary.text.view.SaveValue;
import com.thirdEdition.geometerplus.zlibrary.ui.android.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;

public class DialogMoveTabPic extends Dialog {
	
	public FBReaderApp Reader;
	public FBReader ba;
	public LayoutParams p;
	
	//Table
	private String DBImageTable = ActionCode.ANNOTATION_IMAGE;
	private String recordTable = ActionCode.ANNOTATION_RECORD;
	private String annoType;
	
	//SaveValue
	public int index = 0;
	private int page = 0;
	private String user;
	private String srcPath_pic = "/sdcard/VPen/pic/"; //圖片路徑
	private String srcPath_rec = "/sdcard/VPen/rec/"; //錄音路徑
	public int l, t;
	public String str, str_D;
	
	//Dialog視窗是否出現
	private boolean isDialog = true;
	
	//Button and View
	public LinearLayout recordlayout, ws, picLayout, piclayout_text, piclayout_record, shutterLayout;
	public ImageView iv, picture, picture_text, picture_record;
	public TextView textview, textempty, recordTextview;
	public EditText edittext;
	public RadioGroup radio;
	public SeekBar seekbarR, seekbarG, seekbarB;
	public View colorBox;
	public Button saveBtn, cancelBtn, voiceBtn, vRecordBtn, vPlayBtn, vStopBtn, shutterBtn;
	
	//軟鍵盤
	public InputMethodManager imm;
	
	//voice
	public MediaRecorder mediaRecorder = null;
	public MediaPlayer mediaPlayer = null;
	public String fileName, userName, picFileName;
	
	//boolean( 錄音中不能切換tab )
	public boolean IschangeTab = true;
	String strWord;
	
	//tabHost
	public TabHost tabHost;
	
	
	public DialogMoveTabPic(Context context, int theme) {
		super(context, theme);
		p = getWindow().getAttributes();
	}

	@Override
	public boolean onTouchEvent( MotionEvent event ){
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
		
		imm = ((InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE));
		
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
			//設定dialog位置 (在原有的註記系統有圖片 或 在自己拍的照片上有圖片檔註記)
			if(!FBReader.setImage){
				if( isPicture(DBImageTable)) {
					setLocation(400, 340);//寬、高
				}else {
					setLocation(400, 230);//寬、高
				}
			}
			else{
				if( isPicture("takepic_anno")) {
					setLocation(400, 340);//寬、高
				}else {
					setLocation(400, 230);//寬、高
				}
			}
			
		}
		catch(Exception e) {
			Log.i("log", "test(DialogMove) e = "+e);
		} finally {
			ba.dmtp = this;
		}
	}
	
	//初始化
	private void setBase() {
		this.index = SaveValue.picNowIndex;
		this.page = SaveValue.pageIndex;
		this.user = SaveValue.UserName;
		final SQLiteDB db = ZLApplication.Instance().db;
		//此picFileName用來與sqlite中的圖片比較圖檔名稱(一樣的話就會顯示在註記對話窗)
		if(!FBReader.setImage){
			picFileName = index+"_"+SaveValue.UserName+"_IMAGE_picture.jpg";
		}
		else{
			picFileName = index+"_"+SaveValue.picLesson+"_"+SaveValue.UserName+"_IMAGEAnno_picture.jpg";
		}
		
		if(!FBReader.setImage){
			//Dialog位置
			System.out.println("顯示視窗位置");
			System.out.println("index = " + index);
			l = db.getIntData(index, "sX", DBImageTable, user); //X
			t = db.getIntData(index, "sY", DBImageTable, user); //Y

			//Dialog內容
			str_D = str = db.getStrComment(index, "comment", DBImageTable, user);
			
			//Dialog 錄音檔名
			userName = db.getStrComment( index, "userid", DBImageTable);
			fileName = index +"_"+ userName +"_"+ SaveValue.pageIndex + "_picNote.amr";
			
			//小鍵盤( 隱藏 )
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | 
	                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
			
			//偵測是否有值
			if( str != null ) {
				edittext.setText(str);
				recordTextview.setText(str);
			} else {
				str_D = "";
			}
			
			//偵測是否有照片
			if( isPicture(DBImageTable) ) {
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
				if( isRecord(DBImageTable) ) {
					//顯示音樂圖示
					recordlayout.removeAllViews();
					recordlayout.addView(recordTextview);
					recordlayout.addView(iv);
					vPlayBtn.setEnabled(true);
				}
			} else {
				//偵測是否語音
				if( isRecord(DBImageTable) ) {
					//顯示音樂圖示
					recordlayout.removeAllViews();
					recordlayout.addView(recordTextview);
					recordlayout.addView(iv);
					vPlayBtn.setEnabled(true);
				}
			}
		}else{
			//Dialog位置
			System.out.println("顯示視窗位置");
			System.out.println("index = " + this.index);
			l = db.getIntData(index, "sX", "takepic_anno", user); //X
			t = db.getIntData(index, "sY", "takepic_anno", user); //Y
			//Dialog內容
			str_D = str = db.getStrComment(index, "comment", "takepic_anno", user);
			//偵測是否有值
			if( str != null ) {
				edittext.setText(str);
				recordTextview.setText(str);
			} else {
				str_D = "";
			}
			
			//Dialog 錄音檔名
			userName = db.getStrComment( index, "userid", "takepic_anno");
			fileName = index + "_" + SaveValue.picLesson + "_" + userName + "_recAnno_picNote.amr";
			//小鍵盤( 隱藏 )
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | 
	                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
			//偵測是否有照片
			if( isPicture("takepic_anno") ) {
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
				if( isRecord("takepic_anno") ) {
					//顯示音樂圖示
					recordlayout.removeAllViews();
					recordlayout.addView(recordTextview);
					recordlayout.addView(iv);
					vPlayBtn.setEnabled(true);
				}
			} else {
				//偵測是否語音
				if( isRecord("takepic_anno") ) {
					//顯示音樂圖示
					recordlayout.removeAllViews();
					recordlayout.addView(recordTextview);
					recordlayout.addView(iv);
					vPlayBtn.setEnabled(true);
				}
			}
		}
					
	}

	private void tabHostSet(final TabHost tabHost, int key) {
		
		tabHost.addTab(tabHost.newTabSpec("tab_1")
                .setContent(R.id.tab_context).setIndicator("註記"));
	    tabHost.addTab(tabHost.newTabSpec("tab_2")
                .setContent(R.id.tab_record).setIndicator("語音"));
	    tabHost.addTab(tabHost.newTabSpec("tab_5")
                .setContent(R.id.tab_takePic).setIndicator("照片"));
	    tabHost.addTab(tabHost.newTabSpec("tab_3")
                .setContent(R.id.tab_delete).setIndicator("刪除"));
	    tabHost.addTab(tabHost.newTabSpec("tab_4")
                .setContent(R.id.tab_close).setIndicator("關閉"));
	    tabHost.setCurrentTab(key);//預設值
	    

	    
	    tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
			public void onTabChanged(String tabId) {
				
				//關閉軟鍵盤
				imm.hideSoftInputFromWindow(edittext.getWindowToken(), 0);
				edittext.setFocusableInTouchMode(false);
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
					if( (isRecord(DBImageTable) && !FBReader.setImage) || (isRecord("takepic_anno") && FBReader.setImage) ){
						setLocation(400, 230);
					}
					else{
						setLocation(400, 156);
					}
					recordTextview.setText(edittext.getEditableText().toString());
				}
				else if( "tab_3".equals(tabId) )          //刪除
				{
					if( IschangeTab )
					{
						saveEditText();
						Reader.getTextView().clear();
						SaveValue.IsNote = true; //開啟註記key
						dismiss();
						isDelete();
					}
					else {
						isNowRecord();
						tabHost.setCurrentTabByTag("tab_2");
					}
				}
				else if( "tab_4".equals(tabId) )          //關閉
				{
					if( IschangeTab )
					{
						setLocation(400, 96);
						saveEditText();
						Reader.getTextView().clear();
						SaveValue.IsNote = true; //開啟註記key
						dismiss();
					}
					else {
						isNowRecord();
						tabHost.setCurrentTabByTag("tab_2");
					}
				}
				else if( "tab_5".equals(tabId) )          //照片
				{
					if( IschangeTab ) {
						if( isPicture(DBImageTable) || (isPicture("takepic_anno") && FBReader.setImage) ){
							setLocation(400, 230);
						}
						else{
							setLocation(400, 156);
						}
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
		
		textview = (TextView) findViewById(R.id.dialog_title);         //Title
		
		//註記-------------------------------------------------------------------------------------------
		piclayout_text = (LinearLayout) findViewById(R.id.picLayout_text);
		picture_text = (ImageView) findViewById(R.id.imagePicture_text);         //照片
		piclayout_text.removeView(picture_text);
		//context
		edittext = (EditText) findViewById(R.id.dialog_context);
		//context---btn
		saveBtn = (Button) findViewById(R.id.saveTextNote);
		cancelBtn = (Button) findViewById(R.id.cancelBtn);
		//註記-------------------------------------------------------------------------------------------
		
		//錄音-------------------------------------------------------------------------------------------
		//record
		recordlayout = (LinearLayout) findViewById(R.id.recordlayout);
		iv = (ImageView) findViewById(R.id.imageRecord);               //語音圖
		recordTextview = (TextView) findViewById(R.id.record_title);
		recordlayout.removeView(iv);
		
		piclayout_record = (LinearLayout) findViewById(R.id.picLayout_record);
		picture_record = (ImageView) findViewById(R.id.imagePicture_record);         //照片
		piclayout_record.removeView(picture_record);
		
		//record---btn
		vRecordBtn = (Button) findViewById(R.id.voiceRecordBtn);
		vPlayBtn = (Button) findViewById(R.id.voicePlayBtn);
		vStopBtn = (Button) findViewById(R.id.voiceStopBtn);
		vPlayBtn.setEnabled(false);
		vStopBtn.setEnabled(false);
		//錄音-------------------------------------------------------------------------------------------
		
		//拍照-------------------------------------------------------------------------------------------
		//takePic
		picLayout = (LinearLayout) findViewById(R.id.picLayout);
		picture = (ImageView) findViewById(R.id.imagePicture);               //照片
		//textview = (TextView) findViewById(R.id.dialog_title);
		picLayout.removeView(picture);
		shutterBtn = (Button) findViewById(R.id.shutterBtn);		
		//拍照-------------------------------------------------------------------------------------------
		
		setViewEvent();	
	}

	private void setViewEvent() {
		//context
		//點選edittext才打開小鍵盤
		edittext.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) { 
                edittext.setFocusableInTouchMode(true);   //關键盘 
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
				System.out.println("標註案下了");
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
		//save(保存鈕)
		saveBtn.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				saveEditText();
				Reader.getTextView().clear();
				SaveValue.IsNote = true; //開啟註記key
				dismiss();
			}
		});
		
		//cancel
		cancelBtn.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				saveEditText();
				Reader.getTextView().clear();
				SaveValue.IsNote = true; //開啟註記key
				dismiss();
			}
		});
		
		//record
		vRecordBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				recordlayout.removeAllViews();
				recordlayout.addView(recordTextview);
				recordlayout.addView(iv);
				setLocation(400, 230);
				if( (isRecord(DBImageTable) && !FBReader.setImage) || (isRecord("takepic_anno") && FBReader.setImage) )
					isConfirmRecord();  //確認是否重新錄音
				else
					recordStart();
			}
		});
		
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
		
		//------------------------Shutter------------------------
		//takePic(開始拍照)
		shutterBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				/*LinearLayout i = new LinearLayout(ba);
				i.addView(shutterLayout);
				LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
				i.setLayoutParams(p);
				addContentView(i, p);
				Log.i("test", "testAddContentView");*/
				if(!FBReader.setImage){
					try {
						if( isPicture(DBImageTable) )
							isConfirmShutter();
						else {
							changeActivity();
						}
						
					}catch(Exception e) {
						Log.i("test", "test e = "+e);
					}
				}
				else{
					try{
						if(isPicture("takepic_anno")){
							isConfirmShutter();
						}
						else{
							changeActivity();
						}
					}
					catch(Exception e){
						
					}
				}
			}
		});			
		
		//照片
		picture.setOnClickListener(picutureClick);
		picture_text.setOnClickListener(picutureClick);
		picture_record.setOnClickListener(picutureClick);
	}
	//點選所拍攝的照片放大預覽
	View.OnClickListener picutureClick = new View.OnClickListener() {
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
	};
	
	//save
	public void saveEditText() {
//		if( "".equals(edittext.getEditableText().toString()) || edittext.getEditableText().toString() == null 
//				|| "".equals(edittext.getEditableText().toString())) {
//			//沒註記  檢查是否有錄音
//			if( !isRecord() ) 
//			{
//				//沒註記 沒錄音 檢查拍照
//				delEditText();
//				return;
//			}
//		}
		//如果沒有輸入文字註解也沒有錄音也沒有拍照，直接刪除該註記內容
		if(!FBReader.setImage){
			if( ("".equals(edittext.getEditableText().toString()) || edittext.getEditableText().toString() == null 
					|| "".equals(edittext.getEditableText().toString())) && !isRecord(DBImageTable) && !isPicture(DBImageTable) ) {
					delEditText(DBImageTable);
					return;
			}
		}
		else{
			if( ("".equals(edittext.getEditableText().toString()) || edittext.getEditableText().toString() == null 
					|| "".equals(edittext.getEditableText().toString())) && !isRecord("takepic_anno") && !isPicture("takepic_anno") ) {
					delEditText("takepic_anno");
					return;
			}
		}
		
		//--------------------------------
		//有做註記上的更動的話就更新狀態及內容
		if(!FBReader.setImage){
			try {
				final SQLiteDB db = ZLApplication.Instance().db;
				if( !str_D.equals(edittext.getEditableText().toString()) ) {
					db.updateComment(index, edittext.getEditableText().toString(), DBImageTable);
					db.updateModDate((index), DBImageTable); //更新修改時間
				}
			} catch(Exception e) {
				Log.i("log", "test error e= "+e);
			}
		}
		else{
			try{
				final SQLiteDB db = ZLApplication.Instance().db;
				if( !str_D.equals(edittext.getEditableText().toString()) ){
					String comment = edittext.getEditableText().toString();
					db.updateComment(index, comment, "takepic_anno");
					db.updateModDate((index), "takepic_anno");
					}
				else{
				}
				}
			catch(Exception e){
				Log.i("log", "test (DialogMoveTabPic) Line 706 = "+e);
			}
			
		}
		//----------------------------------
	}	
	
	//delete
	public void delEditText(String tablename) {
		try {
			final SQLiteDB db = ZLApplication.Instance().db;
			
			/*不要刪除
			db.delete(( index), "_id", DBImageTable );
			db.delete( (index), "rec_id", recordTable );
			*/
			db.updateStatus(index, "_id", tablename, 2);
			
		} catch(Exception e) {
			Log.i("log", "test (DialogMove) error e= "+e);
		}
	}		
	
	//record
	public void recordStart() {
		
		try {
			File SDCardpath = Environment.getExternalStorageDirectory();
//			File myDataPath = new File(SDCardpath.getAbsolutePath()
//					+ "/rec");
			File myDataPath = new File(srcPath_rec);
			String sdcardroot = SDCardpath.toString();
			if (!myDataPath.exists())
				myDataPath.mkdirs();
//			File recodeFile = new File(SDCardpath.getAbsolutePath()
//					+ "/rec/" + fileName);
			File recodeFile = new File(srcPath_rec+fileName);
			//String recodeFileTemp = SDCardpath.getAbsolutePath() + "/rec/";
			
			mediaRecorder = new MediaRecorder();
			//設定音源
			mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			
			//mediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
			//設定輸出檔案的格式
			mediaRecorder
					.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
			//設定編碼格式
			mediaRecorder
					.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			//設定錄音檔位置
			mediaRecorder.setOutputFile(recodeFile.getAbsolutePath());
			mediaRecorder.prepare();
			
			//開始錄音
			mediaRecorder.start();
			
			//按鈕設定
			recordSetting("vRecordBtn");
			
			//儲存
			saveRecordData(fileName);
			
		}
		catch(Exception e) {
			Log.i("Log", "test(DialogMove) recordStart error = "+e);
		}
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
		
		//String tempfile = recodeFileTemp + fileName;

		final SQLiteDB db = ZLApplication.Instance().db;
		if(!FBReader.setImage)
			fileName = db.getStrComment(index, "rec", DBImageTable);
		else if(FBReader.setImage)
			fileName = db.getStrComment(index, "rec", "takepic_anno");
		String tempfile = recodeFileTemp + fileName;
		//Log.i("test", "filename = "+fileName);

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
		Reader.db.insertRecordDate(index, this.annoType, SaveValue.UserName);
		recordSetting("vPlayBtn");
		mediaPlayer.setOnCompletionListener(playerlistener);
	}
	
	private void recordStop() {
		
		//播放中
		if(mediaPlayer != null) {
			if(mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
				//更新結束時間
				int id = Reader.db.getRecordID(index, annoType);
				Reader.db.updateRecordDateEnd(id, recordTable);
				
				vPlayBtn.setText("播放");
				vPlayBtn.setEnabled(true);
				vStopBtn.setEnabled(false);
				vRecordBtn.setEnabled(true);
				IschangeTab = true; //能切換Tab
			}
		}		
		
		//錄音中
		if (mediaRecorder != null) {		
			try {
				mediaRecorder.stop();
				mediaRecorder.release();
				mediaRecorder = null;
				recordSetting("vStopBtn");
			}
			catch( Exception e ) {
				recordError("很抱歉!!\n我們並無錄到音!!\n請重新錄音");
			}
		}

	}
	
	protected void recordSetting(String btnName) {
		IschangeTab = true; //能切換Tab
		if( btnName.toString().equals("vRecordBtn") ) {
			//IschangeTab
			IschangeTab = false; //不能切換Tab
			strWord = "錄音";
			vRecordBtn.setText("錄音中...");
			vRecordBtn.setEnabled(false);
			vPlayBtn.setEnabled(false);
			vStopBtn.setEnabled(true);
		}
		else if( btnName.toString().equals("vPlayBtn") ) {
			//IschangeTab
			IschangeTab = false; //不能切換Tab
			strWord = "播放";
			vRecordBtn.setEnabled(false);
			vPlayBtn.setText("播放中...");
			vPlayBtn.setEnabled(false);
			vStopBtn.setEnabled(true);
		}
		else if( btnName.toString().equals("vStopBtn") ) {
			IschangeTab = true; //能切換Tab
			
			vRecordBtn.setText("錄音");
			vRecordBtn.setEnabled(true);
			if( (isRecord(DBImageTable) && !FBReader.setImage) || (isRecord("takepic_anno") && FBReader.setImage) )
				vPlayBtn.setEnabled(true);
			vStopBtn.setEnabled(false);
		}
	}
	
	//恢復原狀( 播放完畢後 )
	MediaPlayer.OnCompletionListener playerlistener = new MediaPlayer.OnCompletionListener() {
		public void onCompletion(MediaPlayer mp) {
			vRecordBtn.setEnabled(true);
			vPlayBtn.setText("播放");
			vPlayBtn.setEnabled(true);
			vStopBtn.setEnabled(false);
			
			IschangeTab = true; //其他TAB可以按
			
			//更新結束時間
			int id = Reader.db.getRecordID(index, annoType);
			Reader.db.updateRecordDateEnd(id, recordTable);
		}
	};
	
	
	//save
	protected void saveRecordData(String fileName) {
		//是否有檔名存在
		if( (!isRecord(DBImageTable) && !FBReader.setImage)) {
			this.Reader.db.updateRecord(index, fileName, DBImageTable);
		}
		else if(!isRecord("takepic_anno") && FBReader.setImage){
			this.Reader.db.updateRecord(index, fileName, "takepic_anno");
		}
	}
	
	private boolean isRecord(String tablename) {
		if( this.Reader.db.getRecord(index, "rec", tablename ) == null )
			return false;
		else
			return true;
	}
	
	private boolean isPicture(String tablename) {
		final SQLiteDB db = ZLApplication.Instance().db;
		String str = db.getStrComment((index), "pic", tablename, SaveValue.UserName);
		//System.out.println("抓到的圖片名稱為: " + str);
		//System.out.println("比較的檔案名稱為: " + picFileName);
		//String picFileName = index+"_"+SaveValue.UserName+"_TEXT_picture.jpg";
		//if(bitmap == null) {
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
		
		
	//確認視窗( delete )
	private void isDelete() {
		new AlertDialog.Builder(getContext()).setTitle("刪除確認").setMessage("是否需要刪除註記?").setPositiveButton("確定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if(!FBReader.setImage){
					delEditText(DBImageTable);
				}
				else{
					delEditText("takepic_anno");
				}
				Reader.getTextView().clear();
				SaveValue.IsNote = true; //開啟註記key
				dismiss();
			}
		}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		}).show();
	}
	
	//確認視窗( record )
	private void isConfirmRecord() {
		new AlertDialog.Builder(getContext()).setTitle("錄音確認").setMessage("目前已有錄好的聲音!\n是否需要重新錄音?").setPositiveButton("確定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				recordStart();
			}
		}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		}).show();
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
	
	//確認視窗( record進行中 )
	private void recordError(String com) {
		new AlertDialog.Builder(getContext()).setTitle("警告").setMessage(com).setPositiveButton("確定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		}).show();
	}	
	
	public void changeDbTable(String annType) {
		this.annoType = annType;
	}
	
	//確認視窗( 拍照 )
	private void isConfirmShutter() {
		new AlertDialog.Builder(getContext()).setTitle("拍照確認").setMessage("目前已有拍好的照片!\n是否需要重新拍照?").setPositiveButton("確定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				changeActivity();
			}
		}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		}).show();
	}	
	
	private static final int TAKE_PIC = 4;
	private void changeActivity() { //拍照
		Intent intent = new Intent();
		//設定傳送參數
    	Bundle bundle = new Bundle();
    	bundle.putString("id", String.valueOf(index));
    	bundle.putString("type", "IMAGE");
    	if(!FBReader.setImage){
    		bundle.putString("db", DBImageTable);
    	}
    	else{
    		bundle.putString("db", "takepic_anno");
    	}
    	intent.putExtras(bundle);	//將參數放入intent
    	//startActivityForResult(intent, 0);	//呼叫page2並要求回傳值
		//切換
    	intent.setClass(ba, CameraActivity.class);
    	ba.startActivityForResult(intent, TAKE_PIC);
    	//ba.startActivity(intent);
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
	
	//放上照片 (放在註記對話窗上)
	public void resetPicture() {
		
		try {
			if( isPicture(DBImageTable) || isPicture("takepic_anno") && FBReader.setImage) {
				//顯示音樂圖示
				picLayout.removeAllViews();
				picLayout.addView(picture);
				
				//Log.i("test", "test e = 0");
				piclayout_text.removeAllViews();
				piclayout_text.addView(picture_text);
				//Log.i("test", "test e = 1");
				piclayout_record.removeAllViews();
				piclayout_record.addView(picture_record);
				//Log.i("test", "test e = 2");
				
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
				
			}
		}catch(Exception e) {
			Log.i("test", "test e = "+e);
		}
	}	
}
