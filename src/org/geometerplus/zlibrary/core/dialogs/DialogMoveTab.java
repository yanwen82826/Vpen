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
import org.geometerplus.android.fbreader.Login;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.sqliteconfig.SQLiteDB;
import org.geometerplus.zlibrary.text.view.SaveValue;
import com.thirdEdition.geometerplus.zlibrary.ui.android.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;

//文字註記Dialog
public class DialogMoveTab extends Dialog {
	
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
	private String srcPath_pic = "/sdcard/VPen/pic/"; //錄音路徑
	private String srcPath_rec = "/sdcard/VPen/rec/"; //圖片路徑
	public int l, t, r, b;  //左 上 右 下
	public String str;
	
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
	public Button saveBtn, cancelBtn, voiceBtn, vRecordBtn, vPlayBtn, vStopBtn
					, color_r, color_o, color_y, color_g, color_b, color_gray
					, shutterBtn, cutBtn;
	
	//軟鍵盤
	private InputMethodManager imm;
	
	//voice
	private MediaRecorder mediaRecorder = null;
	private MediaPlayer mediaPlayer = null;
	private String fileName, userName, picFileName;
	
	//style
	private int Notestyle = 0; //預設是0  選項底色
	private int Notestyle_r = 255; //預設是255
	private int Notestyle_g = 0; //預設是0
	private int Notestyle_b = 0; //預設是0
	public String noteString;   //被標註的文字內容
	
	//default
	private int Notestyle_D, Notestyle_r_D, Notestyle_g_D, Notestyle_b_D;
	private String str_D;
	
	
	//boolean( 錄音中不能切換tab )
	boolean IschangeTab = true;
	String strWord;
	
	//tabHost
	private TabHost tabHost;
	
	
	public DialogMoveTab(Context context, int theme) {
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

		
		final SQLiteDB db = ZLApplication.Instance().db;
		//抓行數( 幾行需要畫 )
		int drawNumber = db.getNoteRange(index, "left", DBTextRangeTable, user);
		
		for( int j=0;j<drawNumber;j++ ) {
			//方塊
			int l = db.getRange(index, "left", DBTextRangeTable, j);
			int r = db.getRange(index, "right", DBTextRangeTable, j);
			int t = db.getRange(index, "top", DBTextRangeTable, j);
			int b = db.getRange(index, "bottom", DBTextRangeTable, j);
			
			if( x+p.x+470+range >= l && x+p.x+470-range <= r && y+p.y+250+range >= t && y+p.y+250-range <= b )  //是否在選取範圍
			{
				saveEditText();
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
		} finally {
			ba.dmt = this;
		}
	}
	
	private void setBase() {
		this.index = SaveValue.nowIndex;
		this.page = SaveValue.pageIndex;
		this.user = SaveValue.UserName;
		picFileName = index+"_"+SaveValue.UserName+"_TEXT_picture.jpg";
		
		final SQLiteDB db = ZLApplication.Instance().db;
		//Dialog位置
		l = db.getRange(index, "left", DBTextRangeTable, 0);
		t = db.getRange(index, "top", DBTextRangeTable, 0);
		r = db.getRange(index, "right", DBTextRangeTable, 0);
		b = db.getRange(index, "bottom", DBTextRangeTable, 0);
		
		//Dialog內容
		str_D = str = db.getStrComment(index, "comment", "annotext", user);
		
		//Dialog 錄音檔名
		userName = db.getStrComment( index, "userid", "annotext");
		fileName = index +"_"+ userName +"_"+ SaveValue.pageIndex + ".amr";
		
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
		
		
		//piclayout_text.removeView(picture_text);
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
				//recordlayout.addView(recordTextview);
				recordlayout.addView(recordTextview);
				recordlayout.addView(iv);
				vPlayBtn.setEnabled(true);
			}
		} else {
			//偵測是否語音
			if( isRecord() ) {
				//顯示音樂圖示
				recordlayout.removeAllViews();
				//recordlayout.addView(recordTextview);
				recordlayout.addView(recordTextview);
				recordlayout.addView(iv);
				vPlayBtn.setEnabled(true);
			}
		}
		
		
		//style
		noteString = db.getStrComment(index, "txt", "annotext");
		//setColor
		Notestyle_r_D = Notestyle_r = db.getIntData(index, "red", "annotext", user); //預設是255
		Notestyle_g_D = Notestyle_g = db.getIntData(index, "green", "annotext", user); //預設是0
		Notestyle_b_D = Notestyle_b = db.getIntData(index, "blue", "annotext", user); //預設是0
		Notestyle_D = Notestyle = db.getIntData(index, "type", "annotext", user); //預設是0
	}

	private void tabHostSet(final TabHost tabHost) {
		
		tabHost.addTab(tabHost.newTabSpec("tab_1")
                .setContent(R.id.tab_context).setIndicator("註記"));
	    tabHost.addTab(tabHost.newTabSpec("tab_2")
                .setContent(R.id.tab_record).setIndicator("語音"));
	    tabHost.addTab(tabHost.newTabSpec("tab_7")
                .setContent(R.id.tab_takePic).setIndicator("照片"));
	    tabHost.addTab(tabHost.newTabSpec("tab_3")
                .setContent(R.id.tab_wordSetting).setIndicator("風格"));
	    tabHost.addTab(tabHost.newTabSpec("tab_4")
                .setContent(R.id.tab_dictory).setIndicator("翻譯"));
	    tabHost.addTab(tabHost.newTabSpec("tab_5")
                .setContent(R.id.tab_delete).setIndicator("刪除"));
	    tabHost.addTab(tabHost.newTabSpec("tab_6")
                .setContent(R.id.tab_close).setIndicator("關閉"));
	    tabHost.setCurrentTab(0);//預設值

	    
	    tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
			public void onTabChanged(String tabId) {
				int x, y;
				
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
					if( isRecord() )
						setLocation(400, 230);
					else
						setLocation(400, 156);
					
					recordTextview.setText(edittext.getEditableText().toString());
				}
				else if( "tab_3".equals(tabId) )          //風格
				{
					if( IschangeTab )
						setLocation(400, 238);
					else {
						isNowRecord();
						tabHost.setCurrentTabByTag("tab_2");
					}
				}
				else if( "tab_4".equals(tabId) )          //翻譯
				{
					if( IschangeTab )
					{
						setLocation(400, 96);
						SaveValue.IsNote = true; //開啟註記
						dismiss();

				        DictionaryUtil.openTextInDictionary(
				        	ba,
				        	noteString,
							true,
				        	t,
				        	b
						);
				        
				        final SQLiteDB db = ZLApplication.Instance().db;
				        //目前translation TABLE的值有多少
				        int translateTableNum = db.getTableCount(ActionCode.ANNOTATION_TRANSLATION, user);
				        //和 str 相同的有多少
				        int translateNum = db.getTranslateNum(ActionCode.ANNOTATION_TRANSLATION, index);
				        
				        if( translateNum > 0 ){ //如果資料(selectText)有重複 則不新增
				        	int id = db.getTranslateId("translation", index);
				        	db.updateTranslateNum(id, (translateNum+1));
				        }else{
				        	//存入資料庫   紀錄
				        	db.insertTranslationData( (translateTableNum+1), index, noteString, user, page);  //index>0是已標註
				        }
				        
					}
					else {
						isNowRecord();
						tabHost.setCurrentTabByTag("tab_2");
					}
				}
				else if( "tab_5".equals(tabId) )          //刪除
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
				else if( "tab_6".equals(tabId) )          //關閉
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
				else if( "tab_7".equals(tabId) )          //照片
				{
					if( IschangeTab ) {
						if( isPicture() )
							setLocation(400, 230);
						else
							setLocation(400, 156);
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
		
		piclayout_text = (LinearLayout) findViewById(R.id.picLayout_text);
		picture_text = (ImageView) findViewById(R.id.imagePicture_text);         //照片
		piclayout_text.removeView(picture_text);
		//context
		edittext = (EditText) findViewById(R.id.dialog_context);
		//context---btn
		saveBtn = (Button) findViewById(R.id.saveTextNote);
		cancelBtn = (Button) findViewById(R.id.cancelBtn);		
		
		//語音----------------------------------------------------------------------------------------------
		//record
		recordlayout = (LinearLayout) findViewById(R.id.recordLayout);
		iv = (ImageView) findViewById(R.id.imageRecord);               //語音圖
		recordTextview = (TextView) findViewById(R.id.record_title);
		recordlayout.removeView(iv);
		
		piclayout_record = (LinearLayout) findViewById(R.id.picLayout_record);
		picture_record = (ImageView) findViewById(R.id.imagePicture_record);         //照片
		piclayout_record.removeView(picture_record);
		
		//record---btn
		voiceBtn = (Button) findViewById(R.id.voiceBtn);
		vRecordBtn = (Button) findViewById(R.id.voiceRecordBtn);
		vPlayBtn = (Button) findViewById(R.id.voicePlayBtn);
		vStopBtn = (Button) findViewById(R.id.voiceStopBtn);
		vPlayBtn.setEnabled(false);
		vStopBtn.setEnabled(false);
		//語音----------------------------------------------------------------------------------------------
		
		//takePic----
		picLayout = (LinearLayout) findViewById(R.id.picLayout);
		picture = (ImageView) findViewById(R.id.imagePicture);               //照片
		//textview = (TextView) findViewById(R.id.dialog_title);
		picLayout.removeView(picture);
		shutterBtn = (Button) findViewById(R.id.shutterBtn);
		//cutBtn = (Button) findViewById(R.id.cutBtn);                      //剪下
		
		//shutterLayout = (LinearLayout) findViewById(R.id.takepiclayout);
		
		
		//style
		radio = (RadioGroup) findViewById(R.id.radioStyle);
		ws = (LinearLayout) findViewById(R.id.tab_wordSetting);
		//style---color
		color_r = (Button) findViewById(R.id.color_r);
		color_o = (Button) findViewById(R.id.color_o);
		color_y = (Button) findViewById(R.id.color_y);
		color_g = (Button) findViewById(R.id.color_g);
		color_b = (Button) findViewById(R.id.color_b);
		color_gray = (Button) findViewById(R.id.color_gray);

		
		setViewColor(color_r, 255, 0, 0);
		setViewColor(color_o, 255, 128, 0);
		setViewColor(color_y, 255, 255, 0);
		setViewColor(color_g, 0, 255, 0);
		setViewColor(color_b, 0, 0, 255);
		setViewColor(color_gray, 128, 128, 128);
		setViewEvent();
		
		Log.i("test", "test ok1"+picLayout);
	}
	
	private void setViewColor(Button c, final int r, final int g, final int b) {
		
		c.setBackgroundColor(Color.rgb(r, g, b));
		//color
		c.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				Notestyle_r = r;
				Notestyle_g = g;
				Notestyle_b = b;
				saveEditText();
				Reader.getTextView().clear();
				//dismiss();
			}
		});		
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
		
		//style
		//偵測點選的radio
		switch(Notestyle) {
			case 0:
				radio.check( R.id.radio_style0 );
				break;
			case 1:
				radio.check( R.id.radio_style1 );
				break;
			//case 2:
			//	radio.check( R.id.radio_style2 );
			//	break;
		}
		
		radio.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId)
			{
				int temp = Notestyle;
				//透過id來辨認不同的radiobutton
				switch(checkedId)
				{
					case R.id.radio_style0:
						Notestyle = 0;
						break;
					case R.id.radio_style1:
						Notestyle = 1;
						break;
					//case R.id.radio_style2:
					//	Notestyle = 2;
					//	break;
					default :break;
				}
				
				if( temp != Notestyle ) {
					saveEditText();
					Reader.getTextView().clear();
				}	
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
		if(str != null && !"".equals(str) || isRecord()  || isPicture() )
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
		
		//save
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
				Reader.getTextView().clear();
				SaveValue.IsNote = true; //開啟註記key
				dismiss();
			}
		});
		
		//------------------------Voice------------------------
		//record
		vRecordBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				recordlayout.removeAllViews();
				recordlayout.addView(recordTextview);
				recordlayout.addView(iv);
				setLocation(400, 230);
				
				if( isRecord() )
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
		
		//computer voice
		voiceBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				computerVoice();
			}
		});
		
		//------------------------Shutter------------------------
		//takePic
		shutterBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				/*LinearLayout i = new LinearLayout(ba);
				i.addView(shutterLayout);
				LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
				i.setLayoutParams(p);
				addContentView(i, p);
				Log.i("test", "testAddContentView");*/
				try {
					if( isPicture() )
						isConfirmShutter();
					else {
						changeActivity();
					}
					
				}catch(Exception e) {
					Log.i("test", "test e = "+e);
				}
				
			}
		});	
		
//		cutBtn.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				//PicturePreview ppreview = new PicturePreview(ba, R.style.TANCStyle, srcPath_pic+picFileName);
//				PicturePreview ppreview = new PicturePreview(ba, srcPath_pic+picFileName);
//				//LinearLayout markLayout = (LinearLayout) ppreview.getLayoutInflater().inflate(R.layout.tabhost_main, null);
//				ppreview.requestWindowFeature(Window.FEATURE_NO_TITLE); //要放在setContentView之前
//				ppreview.setFBReader(ba);
//				//ppreview.setContentView(markLayout);
//				ppreview.setCancelable(false);
//				//ppreview.setPicture(srcPath_pic+fileName);
//				ppreview.show();
//			}
//		});
		
		//照片
		picture.setOnClickListener(picutureClick);
		picture_text.setOnClickListener(picutureClick);
		picture_record.setOnClickListener(picutureClick);		
	}
	
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
		try {
			final SQLiteDB db = ZLApplication.Instance().db;
			//Log.i("log","test (SQLiteDB) InsertNoteID(成功新增註記) = "+(index));
			boolean b = false;
			if( !str_D.equals(edittext.getEditableText().toString()) ) {
				db.updateComment((index), edittext.getEditableText().toString(), DBTextTable);
				b = true;
			}
			if( Notestyle_D != Notestyle ) {
				db.updateStyle((index), Notestyle, DBTextTable);
				b = true;
			}
			if( Notestyle_r_D != Notestyle_r || Notestyle_g_D != Notestyle_g || Notestyle_b_D != Notestyle_b ) {
				db.updateColor((index), Notestyle_r, Notestyle_g, Notestyle_b, DBTextTable);
				b = true;
			}
			if(b) {
				db.updateModDate((index), DBTextTable); //更新修改時間
			}
		} catch(Exception e) {
			Log.i("log", "test error e= "+e);
		}
	}
	
	//delete
	public void delEditText() {
		try {
			final SQLiteDB db = ZLApplication.Instance().db;
			
			
			/* 不要刪除
			db.delete( (index), "_id", DBTextTable ); //annoText
			db.delete( (index), "t_id", DBTextRangeTable ); //annoText_drawRange
			db.delete( (index), "rec_id", DBRecordTable ); //record
			db.delete( (index), "t_id", DBTTSTable ); //tts_num
			*/
			db.updateStatus(index, "_id", DBTextTable, 2);
			db.updateStatus(index, "t_id", DBTextRangeTable, 2);
			
		} catch(Exception e) {
			Log.i("log", "test (DialogMove) error e= "+e);
		}
	}
	
	//computer voice
	private void computerVoice() {
		try {
			String txt = this.Reader.db.getStrComment( (index), "txt", DBTextTable);
			
			if (txt.length() > 0) {
				this.Reader.tts.speak(txt, TextToSpeech.QUEUE_FLUSH, null);
				this.Reader.db.insertTTSDate( (index), SaveValue.UserName );
			}
			
		} catch (Exception e) {
			Log.i("log", "test(DialogMove) computerVoice ERROR = "+e.toString());
		}
	}
	
	//record
	private void recordStart() {
		
		try {
			
			File SDCardpath = Environment.getExternalStorageDirectory();
			//File myDataPath = new File(SDCardpath.getAbsolutePath()+"/VPen/rec/");
			File myDataPath = new File(srcPath_rec);
			String sdcardroot = SDCardpath.toString();
			if (!myDataPath.exists())
				myDataPath.mkdirs();
			//File recodeFile = new File(SDCardpath.getAbsolutePath() + "/VPen/rec/" + fileName);
			File recodeFile = new File(srcPath_rec+fileName);
			//String recodeFileTemp = SDCardpath.getAbsolutePath() + "/VPen/rec/";
			
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
			//recodeFileTemp = SDCardpath.getAbsolutePath() + "/VPen/rec/";
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
		Reader.db.insertRecordDate( (index), "TEXT", SaveValue.UserName);
		recordSetting("vPlayBtn");
		mediaPlayer.setOnCompletionListener(playerlistener);
	}
	
	private void recordStop() {
		
		//播放中
		if(mediaPlayer != null) {
			if(mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
				//更新結束時間
				int id = Reader.db.getRecordID(index, "TEXT");
				Reader.db.updateRecordDateEnd(id, DBRecordTable);
				
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
	
	private void recordSetting(String btnName) {
		IschangeTab = true; //能切換Tab
		
		if( btnName.toString().equals("vRecordBtn") ) {
			//IschangeTab
			IschangeTab = false; //不能切換Tab
			strWord = "錄音";
			vRecordBtn.setText("錄音中...");
			vRecordBtn.setEnabled(false);
			vPlayBtn.setEnabled(false);
			voiceBtn.setEnabled(false);
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
			
			IschangeTab = true; //其他TAB可以按
			vRecordBtn.setText("錄音");
			vRecordBtn.setEnabled(true);
			if( isRecord() ) {
				vPlayBtn.setEnabled(true);
				voiceBtn.setEnabled(true);
			}
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
			int id = Reader.db.getRecordID(index, "TEXT");
			Reader.db.updateRecordDateEnd(id, DBRecordTable);
		}
	};
	
	
	//save
	private void saveRecordData(String fileName) {
		//是否有檔名存在
		if( !isRecord()) {
			this.Reader.db.updateRecord( index, fileName, DBTextTable);
		}
	}
	
	private boolean isRecord() {
		if( this.Reader.db.getRecord( index, "rec", DBTextTable ) == null )
			return false;
		else
			return true;
	}
	
	private boolean isPicture() {
		final SQLiteDB db = ZLApplication.Instance().db;
		String str = db.getStrComment((index), "pic", DBTextTable, SaveValue.UserName);
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
		AlertDialog al = new AlertDialog.Builder(getContext()).setTitle("是否刪除註記").setPositiveButton("確定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				delEditText();
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
	
	private static final int TAKE_PIC = 3;
	private void changeActivity() { //拍照
		Intent intent = new Intent();
		//設定傳送參數
    	Bundle bundle = new Bundle();
    	bundle.putString("id", String.valueOf(index));
    	bundle.putString("type", "TEXT");
    	bundle.putString("db", DBTextTable);
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
	
	//放上照片
	public void resetPicture() {
		
		try {
			if( isPicture() ) {
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

	/*
	private Bitmap loadBitmap(String url) { //loadBitmap
		Bitmap bm = null;
	    InputStream is = null;
	    BufferedInputStream bis = null;
	    try 
	    {
	        URLConnection conn = new URL(url).openConnection();
	        conn.connect();
	        is = conn.getInputStream();
	        bis = new BufferedInputStream(is, 8192);
	        bm = BitmapFactory.decodeStream(bis);
	    }
	    catch (Exception e) 
	    {
	        e.printStackTrace();
	    }
	    finally {
	        if (bis != null) 
	        {
	            try 
	            {
	                bis.close();
	            }
	            catch (IOException e) 
	            {
	                e.printStackTrace();
	            }
	        }
	        if (is != null) 
	        {
	            try 
	            {
	                is.close();
	            }
	            catch (IOException e) 
	            {
	                e.printStackTrace();
	            }
	        }
	    }
	    return bm;
	}*/
	
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
