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

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.sqliteconfig.SQLiteDB;
import org.geometerplus.zlibrary.text.view.SaveValue;

class ShowCancelMenuAction extends FBAndroidAction {
	ShowCancelMenuAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	public void run() {
		if (Reader.getCurrentView() != Reader.BookTextView) {
			Reader.showBookTextView();
		} else {
			final List<FBReaderApp.CancelActionDescription> descriptionList =
				Reader.getCancelActionsList();
			if (descriptionList.size() == 1) {
				try{
					TipDialog();
				}catch(Exception e){
					Log.i("test", "test e tipDialog = "+e);
				}
				//Reader.closeWindow();
			} else {
				final Intent intent = new Intent();
				intent.setClass(BaseActivity, CancelActivity.class);
				intent.putExtra(CancelActivity.LIST_SIZE, descriptionList.size());
				int index = 0;
				for (FBReaderApp.CancelActionDescription description : descriptionList) {
					intent.putExtra(CancelActivity.ITEM_TITLE + index, description.Title);
					intent.putExtra(CancelActivity.ITEM_SUMMARY + index, description.Summary);
					++index;
				}
				BaseActivity.startActivityForResult(intent, FBReader.CANCEL_CODE);
			}
		}
	}
	
	//是否離開的提示視窗
	public void TipDialog() {
		new AlertDialog.Builder(this.BaseActivity).setTitle("提示").setMessage("確定離開")
		.setPositiveButton("確定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				//強制關閉家長參與 ,儲存結束時間
				saveCloseParentTime();
				Reader.closeWindow();
			}
		})
		.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
			}
		}).show();
		return;
	}
	
	private void saveCloseParentTime() {
		String user = SaveValue.UserName;
		
		final SQLiteDB db = ZLApplication.Instance().db;
		int parentInNum = db.getTableCount(ActionCode.ANNOTATION_PARENTIN_TIME);  //註記個數
		db.updateParentInTime(parentInNum);
	}
}
