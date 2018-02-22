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
import org.geometerplus.zlibrary.core.dialogs.DialogMoveTabParents;
import org.geometerplus.zlibrary.core.dialogs.DialogMoveTabPic;
import org.geometerplus.zlibrary.core.sqliteconfig.SQLiteDB;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.text.view.SaveValue;
import com.thirdEdition.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.android.fbreader.library.LibraryTopLevelActivity;
import org.geometerplus.android.util.UIUtil;

class RecordParentsVoice extends FBAndroidAction {
	
	String filename;
	int num = 0;
	//當按下 老師講解的按鈕時
	RecordParentsVoice(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	public void run() {
		
		int page = SaveValue.pageIndex;
		String user = SaveValue.UserName;
		//住一頁 第幾個講解
		num = Reader.db.getTableCount(ActionCode.ANNOTATION_IMAGE, "SIGNATURE", user, page);
		
		/*
		 親愛的家長 您好:
			建議您可以查閱孩子的筆記內容，聽聽孩子的錄音，了解並關心孩子的表現。
			請在查閱後，簡略說明”我已看過了”或”我已查閱孩子的筆記”…等。
			若發現孩子在學習上有任何問題，請透過「家長簽章」錄音說明，
			我們會馬上轉知給任課老師和導師瞭解。

		*/
		new AlertDialog.Builder(BaseActivity).setTitle("提示").setMessage(
				"親愛的家長 您好:\n" +
				"建議您可以查閱孩子的筆記內容，\n" +
				"聽聽孩子的錄音，了解並關心孩子的表現。\n\n" +
				"請在查閱後，\n" +
				"簡略說明”我已看過了”或”我已查閱孩子的筆記”…等。\n\n" +
				"若發現孩子在學習上有任何問題，\n" +
				"請透過「家長簽章」錄音說明，\n" +
				"我們會馬上轉知給任課老師和導師瞭解。")
		.setPositiveButton("開始錄音", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
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
		
		savePicNote(num);
		
		if( SaveValue.IsNote ) {
			SaveValue.IsNote = false; //關閉註記key
			LinearLayout markLayout = (LinearLayout) BaseActivity.getLayoutInflater().inflate(R.layout.record_parents, null);
		    
			DialogMoveTabParents dialogmove = new DialogMoveTabParents(BaseActivity, R.style.TANCStyle);
			dialogmove.changeDbTable("PARENTS");
			dialogmove.setFBReaderApp(Reader);
			dialogmove.requestWindowFeature(Window.FEATURE_NO_TITLE); //要放在setContentView之前
			dialogmove.setContentView(markLayout);
			dialogmove.setCancelable(false);
			dialogmove.show();
			
			dialogmove.setLocation(640, 360);
			dialogmove.recordStart();
			
		}
		
	}
	
	private void savePicNote(int num) {
		
		int page = SaveValue.pageIndex;
		String user = SaveValue.UserName;
		
		
		final SQLiteDB db = ZLApplication.Instance().db;
		//int Picnum = db.getTableCount("annoimage", user);  //註記個數
		//int Picnum = db.getTableCount("annoimage");  //註記個數
		int Picnum = db.getMaxID(ActionCode.ANNOTATION_IMAGE); //註記個數( 取最大ID )
		filename = (num+1) +"_"+ user +"_"+ page + "_parents.amr";
		
		db.insertPicNote( (Picnum+1), "PARENTS", 0, 0, page, user); //picNote 儲存
		//db.insertPicNote( (Picnum+1), "LECTURE", 50, (100 + (50 * num)), page, user); //picNote 儲存
		//db.insertLecture( (Picnum+1), user, page, filename ); //獎解儲存
		SaveValue.picNowIndex = (Picnum+1);
	}
}
