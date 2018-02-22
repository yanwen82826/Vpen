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
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
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

public class DialogMoveTabParents extends Dialog {
	
	public FBReaderApp Reader;
	public LayoutParams p;
	
	//Table
	private String DBImageTable = ActionCode.ANNOTATION_IMAGE;
	private String recordTable = ActionCode.ANNOTATION_RECORD;
	private String annoType;
	
	//SaveValue
	public int index = 0;
	private int page = 0;
	private String user;
	public int l, t;
	public String str, str_D;
	
	//Dialog視窗是否出現
	private boolean isDialog = true;
	
	//Button and View
	public LinearLayout editlayout, ws;
	public ImageView iv;
	public TextView content;
	public RadioGroup radio;
	public Button voiceBtn, vRecordBtn, vPlayBtn, vStopBtn, saveBtn;
	
	//軟鍵盤
	public InputMethodManager imm;
	
	//voice
	public MediaRecorder mediaRecorder = null;
	public MediaPlayer mediaPlayer = null;
	public String fileName, userName;
	
	String strWord;
	
	//tabHost
	public TabHost tabHost;
	
	
	public DialogMoveTabParents(Context context, int theme) {
		super(context, theme);
		p = getWindow().getAttributes();
	}
	public DialogMoveTabParents(Context context) {
		super(context);
		p = getWindow().getAttributes();
	}

	@Override
	public boolean onTouchEvent( MotionEvent event ) {
		
		float x = event.getX();
		float y = event.getY();
		
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
	    
		try{
			//設定view
			setViewId();
			//初始化
			setBase();
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
		this.user = SaveValue.UserName;
		
		final SQLiteDB db = ZLApplication.Instance().db;
		//Dialog位置
		//l = db.getIntData(index, "sX", DBImageTable, user); //X
		//t = db.getIntData(index, "sY", DBImageTable, user); //Y
		
		//Dialog 錄音檔名
		userName = db.getStrComment( index, "userid", DBImageTable);
		fileName = index +"_"+ userName +"_"+ SaveValue.pageIndex + "_parents.amr";
		
		//小鍵盤( 隱藏 )
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | 
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
		//偵測是否語音
		if( isRecord() ) {
			//顯示音樂圖示
			//editlayout.removeAllViews();
			//editlayout.addView(iv);	
			vPlayBtn.setEnabled(true);
		}		
	}
	
	private void setViewId() {
		
		//record
		editlayout = (LinearLayout) findViewById(R.id.editLayout);
		iv = (ImageView) findViewById(R.id.imageRecord);               //語音圖
		content = (TextView) findViewById(R.id.dialog_content);
		content.setText("您可以說明以下任一內容：\n1. 關於孩子在學習上所遇到的問題。\n2. 老師在教學上的建議。\n3. 我已查閱過孩子的筆記內容。\n\n謝謝~");
		//record---btn
		vRecordBtn = (Button) findViewById(R.id.voiceRecordBtn);
		vPlayBtn = (Button) findViewById(R.id.voicePlayBtn);
		vStopBtn = (Button) findViewById(R.id.voiceStopBtn);
		saveBtn = (Button) findViewById(R.id.saveBtn);
		vPlayBtn.setEnabled(false);
		vStopBtn.setEnabled(false);
		saveBtn.setEnabled(true);
	}
	
	//設定位置
	public void setLocation(int width, int height) {
		int dialogWidth = width;
		int dialogHeight = height;
		
		LayoutParams p = this.getWindow().getAttributes();
		p.x = -1280/2 + l + dialogWidth;
		p.y = -743/2 + t + dialogHeight;
		//if(SaveValue.picEditStr[index] != null && !"".equals(SaveValue.picEditStr[index]) || isRecord() )
			p.y += 25;   //18是註記高度
		
		if(p.y > 244)  //dialog位置太下面
			p.y = -743/2 + t -120;
		
		this.onWindowAttributesChanged(p);
	}
	
	//按鈕設定
	public void setBtn() {
		
		//record
		vRecordBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//editlayout.removeAllViews();
				//editlayout.addView(iv);
				setLocation(640, 360);
				
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
		
		//送出
		saveBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				final SQLiteDB db = ZLApplication.Instance().db;
				db.updateModDate((index), DBImageTable);
				Reader.getTextView().clear();
				SaveValue.IsNote = true; //開啟註記key
				dismiss();
			}
		});
	}	
	
	//record
	public void recordStart() {
		
		try {
			File SDCardpath = Environment.getExternalStorageDirectory();
			File myDataPath = new File(SDCardpath.getAbsolutePath()
					+ "/rec");
			String sdcardroot = SDCardpath.toString();
			if (!myDataPath.exists())
				myDataPath.mkdirs();
			File recodeFile = new File(SDCardpath.getAbsolutePath()
					+ "/rec/" + fileName);
			String recodeFileTemp = SDCardpath.getAbsolutePath() + "/rec/";
			
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
			recodeFileTemp = SDCardpath.getAbsolutePath() + "/rec/";
		} catch (Exception e) {
			e.printStackTrace();
		}

		final SQLiteDB db = ZLApplication.Instance().db;
		fileName = db.getStrComment(index, "rec", DBImageTable);
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
				saveBtn.setEnabled(true);
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
		if( btnName.toString().equals("vRecordBtn") ) {
			strWord = "錄音";
			vRecordBtn.setText("錄音中...");
			vRecordBtn.setEnabled(false);
			vPlayBtn.setEnabled(false);
			vStopBtn.setEnabled(true);
			saveBtn.setEnabled(false);
		}
		else if( btnName.toString().equals("vPlayBtn") ) {
			strWord = "播放";
			vRecordBtn.setEnabled(false);
			vPlayBtn.setText("播放中...");
			vPlayBtn.setEnabled(false);
			vStopBtn.setEnabled(true);
			saveBtn.setEnabled(false);
		}
		else if( btnName.toString().equals("vStopBtn") ) {
			vRecordBtn.setText("錄音");
			vRecordBtn.setEnabled(true);
			if( isRecord() )
				vPlayBtn.setEnabled(true);
			vStopBtn.setEnabled(false);
			saveBtn.setEnabled(true);
		}
	}
	
	//恢復原狀( 播放完畢後 )
	MediaPlayer.OnCompletionListener playerlistener = new MediaPlayer.OnCompletionListener() {
		public void onCompletion(MediaPlayer mp) {
			vRecordBtn.setEnabled(true);
			vPlayBtn.setText("播放");
			vPlayBtn.setEnabled(true);
			vStopBtn.setEnabled(false);
			saveBtn.setEnabled(true);
			
			//更新結束時間
			int id = Reader.db.getRecordID(index, annoType);
			Reader.db.updateRecordDateEnd(id, recordTable);
		}
	};
	
	
	//save
	protected void saveRecordData(String fileName) {
		//是否有檔名存在
		if( !isRecord() ) {
			this.Reader.db.updateRecord(index, fileName, DBImageTable);
		}
	}
	
	private boolean isRecord() {
		if( this.Reader.db.getRecord(index, "rec", DBImageTable ) == null )
			return false;
		else
			return true;
	}
	

	//setFBReader
	public void setFBReaderApp(FBReaderApp reader) {
		this.Reader = reader;
	}
		
	//確認視窗( delete )
	private void isDelete() {
		new AlertDialog.Builder(getContext()).setTitle("刪除確認").setMessage("是否需要刪除註記?").setPositiveButton("確定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
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
}
