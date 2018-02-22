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
	
	//����U �Ѯv���Ѫ����s��
	ParentsInvolvementAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
		Reader = fbreader;
	}

	public void run() {
		
		if( !SaveValue.IsParent ) {
			new AlertDialog.Builder(BaseActivity).setTitle("����").setMessage("�нT�{����O�_���u�a�x�������P�v?")
			.setPositiveButton("�O", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
					doSomething();
				}
			})
			.setNegativeButton("�_", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
				}
			}).show();
		}
		else {
			new AlertDialog.Builder(BaseActivity).setTitle("����").setMessage("�O�_�����u�a�x�����ѻP�Ҧ��v?")
			.setPositiveButton("�O", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
					doSomething();
				}
			})
			.setNegativeButton("�_", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
				}
			}).show();
		}
		
		
	}
	
	private void doSomething() {
		String str;
		//�O�_�b�a���ѻP�Ҧ��U
		if( !SaveValue.IsParent ) {
			str = "�Ұ�";
			saveParentTime();
		} else {
			str = "����";
			saveCloseParentTime();
		}
		
		//�Ҧ�����
		SaveValue.IsParent = !SaveValue.IsParent;
		
		//��ܥثe���A
		Toast.makeText(BaseActivity,
				"�a�x�����ѻP�Ҧ�"+str,
				SaveValue.IsParent ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
		
		//���s��z����
		Reader.getTextView().clear();
		
		return;		
	}
	
	private void saveParentTime() {
		String user = SaveValue.UserName;
		
		final SQLiteDB db = ZLApplication.Instance().db;
		int parentInNum = db.getTableCount(ActionCode.ANNOTATION_PARENTIN_TIME);  //���O�Ӽ�
		db.insertParentInData( (parentInNum+1) , user);
	}

	private void saveCloseParentTime() {
		String user = SaveValue.UserName;
		
		final SQLiteDB db = ZLApplication.Instance().db;
		int parentInNum = db.getTableCount(ActionCode.ANNOTATION_PARENTIN_TIME);  //���O�Ӽ�
		db.updateParentInTime(parentInNum);
	}
}
