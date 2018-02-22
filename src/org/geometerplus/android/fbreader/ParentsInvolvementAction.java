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

class ParentsInvolvementAction extends FBAndroidAction {
	
	FBReaderApp Reader;
	
	//當按下 老師講解的按鈕時
	ParentsInvolvementAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
		Reader = fbreader;
	}

	public void run() {
		
		if( !SaveValue.IsParent ) {
			new AlertDialog.Builder(BaseActivity).setTitle("提示").setMessage("請確認身邊是否有「家庭成員陪同」?")
			.setPositiveButton("是", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
					doSomething();
				}
			})
			.setNegativeButton("否", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
				}
			}).show();
		}
		else {
			new AlertDialog.Builder(BaseActivity).setTitle("提示").setMessage("是否結束「家庭成員參與模式」?")
			.setPositiveButton("是", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
					doSomething();
				}
			})
			.setNegativeButton("否", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
				}
			}).show();
		}
		
		
	}
	
	private void doSomething() {
		String str;
		//是否在家長參與模式下
		if( !SaveValue.IsParent ) {
			str = "啟動";
			saveParentTime();
		} else {
			str = "關閉";
			saveCloseParentTime();
		}
		
		//模式切換
		SaveValue.IsParent = !SaveValue.IsParent;
		
		//顯示目前狀態
		Toast.makeText(BaseActivity,
				"家庭成員參與模式"+str,
				SaveValue.IsParent ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
		
		//重新整理頁面
		Reader.getTextView().clear();
		
		return;		
	}
	
	private void saveParentTime() {
		String user = SaveValue.UserName;
		
		final SQLiteDB db = ZLApplication.Instance().db;
		int parentInNum = db.getTableCount(ActionCode.ANNOTATION_PARENTIN_TIME);  //註記個數
		db.insertParentInData( (parentInNum+1) , user);
	}

	private void saveCloseParentTime() {
		String user = SaveValue.UserName;
		
		final SQLiteDB db = ZLApplication.Instance().db;
		int parentInNum = db.getTableCount(ActionCode.ANNOTATION_PARENTIN_TIME);  //註記個數
		db.updateParentInTime(parentInNum);
	}
}
