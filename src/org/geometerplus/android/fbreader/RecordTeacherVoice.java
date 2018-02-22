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

package org.geometerplus.android.fbreader;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.BackgroundColorAction;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.dialogs.DialogMoveLecture;
import org.geometerplus.zlibrary.core.dialogs.DialogMoveTabPic;
import org.geometerplus.zlibrary.core.sqliteconfig.SQLiteDB;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.text.view.SaveValue;
import com.thirdEdition.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.android.fbreader.library.LibraryTopLevelActivity;
import org.geometerplus.android.util.UIUtil;

class RecordTeacherVoice extends FBAndroidAction {
	
	String filename;
	int num = 0;
	//當按下 老師講解的按鈕時
	RecordTeacherVoice(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	public void run() {
		
		int page = SaveValue.pageIndex;
		String user = SaveValue.UserName;
		//住一頁 第幾個講解
		num = Reader.db.getTableCount(ActionCode.ANNOTATION_IMAGE, "LECTURE", user, page);
		
		if(num == 10) {
			UIUtil.showMessageText(BaseActivity, "很抱歉! 「老師講解」最多錄製10次!");
			return;
		}
		
		new AlertDialog.Builder(BaseActivity).setTitle("提示").setMessage("是否開始錄製老師講解?")
		.setPositiveButton("確定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				//UIUtil.showMessageText(BaseActivity, "請在畫面上任一空白處點擊，您想要註記的位置");
				doSomething();
			}
		})
		.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
			}
		}).show();
		return;
	}
	
	public void doSomething() {
		//SaveValue.picNoteOpen = true;
		
		savePicNote(num);
		
		if( SaveValue.IsNote ) {
			SaveValue.IsNote = false; //關閉註記key
			LinearLayout markLayout = (LinearLayout) BaseActivity.getLayoutInflater().inflate(R.layout.tabhost_mainpic, null);
		    
			DialogMoveLecture dialogmove = new DialogMoveLecture(BaseActivity, R.style.TANCStyle);
			//dialogmove.changeDbTable("annoImage", "lecturetable");
			dialogmove.changeDbTable("LECTURE");
			dialogmove.setFBReaderApp(Reader);
			dialogmove.setFBReader(BaseActivity);
			dialogmove.requestWindowFeature(Window.FEATURE_NO_TITLE); //要放在setContentView之前
			dialogmove.setContentView(markLayout);
			dialogmove.setCancelable(false);
			dialogmove.show();
			
			dialogmove.recordS(SaveValue.lecture_num, filename);
		}
		
		
	}
	
	private void savePicNote(int num) {
		
		int page = SaveValue.pageIndex;
		String user = SaveValue.UserName;
		
		
		final SQLiteDB db = ZLApplication.Instance().db;
		//int Picnum = db.getTableCount("annoimage", user);  //註記個數
		//int Picnum = db.getTableCount("annoimage");  //註記個數
		int Picnum = db.getMaxID(ActionCode.ANNOTATION_IMAGE); //註記個數( 取最大ID )
		filename = (num+1) +"_"+ user +"_"+ page + "_lecture.amr";
		
		db.insertPicNote( (Picnum+1), "LECTURE", 70 + (100 * num), 8, page, user); //picNote 儲存
		//db.insertPicNote( (Picnum+1), "LECTURE", 50, (40 + (50 * num)), page, user); //picNote 儲存
		//db.insertPicNote( (Picnum+1), "LECTURE", 50, (100 + (50 * num)), page, user); //picNote 儲存
		//db.insertLecture( (Picnum+1), user, page, filename ); //獎解儲存
		
		SaveValue.lecture_num = num+1;
		SaveValue.picNowIndex = (Picnum+1);
	}
}
