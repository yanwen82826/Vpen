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

import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.dialogs.DialogMoveTabParents;
import org.geometerplus.zlibrary.core.sqliteconfig.ConnectMysql;
import org.geometerplus.zlibrary.text.view.SaveValue;
import com.thirdEdition.geometerplus.zlibrary.ui.android.R;

import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

class GetSelfDataAction extends FBAndroidAction {
	
	private FBReaderApp Reader;
	private FBReader Base;
	private EditText edittext;
	private Dialog dialogmove;
	
	//當按下 還原自己註記 的按鈕時( 緊急時候使用 )
	GetSelfDataAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
		Reader = fbreader;
		Base = baseActivity;
	}

	public void run() {
		if( SaveValue.IsNote ) {
			SaveValue.IsNote = false; //關閉註記key
			
			LinearLayout markLayout = (LinearLayout) Base.getLayoutInflater().inflate(R.layout.restore_dialog, null);
			dialogmove = new Dialog(Base, R.style.TANCStyle);
			dialogmove.requestWindowFeature(Window.FEATURE_NO_TITLE); //要放在setContentView之前
			dialogmove.setContentView(markLayout);
			dialogmove.setCancelable(false);
			dialogmove.show();
			
			edittext = (EditText)dialogmove.findViewById(R.id.editText1);
			Button save = (Button)dialogmove.findViewById(R.id.saveBtn);
			Button cancel = (Button)dialogmove.findViewById(R.id.cancelBtn);
			save.setOnClickListener(new View.OnClickListener(){
				public void onClick(View v) {
					if( "5afwheh8zm".equals(edittext.getEditableText().toString()) ) {
						final ConnectMysql mysql = ZLApplication.Instance().mysql;
						mysql.syncDownloadDBfile();
						Reader.getTextView().clear();
						SaveValue.IsNote = true; //開啟註記key
						dialogmove.dismiss();
					} else if( "updatenow".equals(edittext.getEditableText().toString()) ) {
						SaveValue.test = true;
						UIUtil.showMessageText(Base, "請稍後!!");
						dialogmove.dismiss();
					} else {
						UIUtil.showMessageText(Base, "密碼錯誤，請重新輸入");
						edittext.setText("");
					}
					
				}
			});
			
			cancel.setOnClickListener(new View.OnClickListener(){
				public void onClick(View v) {
					Reader.getTextView().clear();
					SaveValue.IsNote = true; //開啟註記key
					dialogmove.dismiss();
				}
			});
			
			
			
		}
		//final ConnectMysql mysql = ZLApplication.Instance().mysql;
		//mysql.syncDownloadDBfile();
		//UIUtil.showMessageText(BaseActivity, "同步資料中，請稍後...");
		//action
	}
}
