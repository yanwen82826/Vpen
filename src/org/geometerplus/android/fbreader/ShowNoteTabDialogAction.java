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

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.dialogs.DialogMoveTab;
import org.geometerplus.zlibrary.core.dialogs.DialogMoveTab_sync;
import org.geometerplus.zlibrary.core.sqliteconfig.SQLiteDB;
import org.geometerplus.zlibrary.core.sqliteconfig.SyncDB;
import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.view.SaveValue;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import com.thirdEdition.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

import android.util.Log;
import android.view.Window;
import android.widget.LinearLayout;


class ShowNoteTabDialogAction extends FBAndroidAction {
	
	ShowNoteTabDialogAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	@Override
	public boolean isVisible() {
		final ZLTextView view = (ZLTextView)Reader.getCurrentView();
		final ZLTextModel textModel = view.getModel();
		return textModel != null && textModel.getParagraphsNumber() != 0;
	}

	public void run() {
		//UIUtil.showMessageText(BaseActivity, "IsSyncData = "+SaveValue.IsSyncData);
		if( SaveValue.IsNote )
		{	
			//UIUtil.showMessageText(BaseActivity, "IsSyncData = "+SaveValue.IsSyncData);
			SaveValue.IsNote = false; //關閉註記key
			//LinearLayout markLayout = (LinearLayout) BaseActivity.getLayoutInflater().inflate(R.layout.tabhost_main, null);
			//DialogMove dialogmove = new DialogMove(BaseActivity, R.style.TANCStyle);
			if( !SaveValue.IsSyncData ) {
				DialogMoveTab dialogmovet = new DialogMoveTab(BaseActivity, R.style.TANCStyle);
				LinearLayout markLayout = (LinearLayout) dialogmovet.getLayoutInflater().inflate(R.layout.tabhost_main, null);
				dialogmovet.setFBReaderApp(Reader);
				dialogmovet.setFBReader(BaseActivity);
				dialogmovet.requestWindowFeature(Window.FEATURE_NO_TITLE); //要放在setContentView之前
				dialogmovet.setContentView(markLayout);
				dialogmovet.setCancelable(false);
				dialogmovet.show();
				 
			} else {
				DialogMoveTab_sync dialogmovet = new DialogMoveTab_sync(BaseActivity, R.style.TANCStyle);
				LinearLayout markLayout = (LinearLayout) dialogmovet.getLayoutInflater().inflate(R.layout.tabhost_sync_main, null);
				dialogmovet.setFBReaderApp(Reader);
				dialogmovet.setFBReader(BaseActivity);
				dialogmovet.requestWindowFeature(Window.FEATURE_NO_TITLE); //要放在setContentView之前
				dialogmovet.setContentView(markLayout);
				dialogmovet.setCancelable(false);
				dialogmovet.show();
				
				int temp = Reader.db.getTableCount(ActionCode.ANNOTATION_SYNC_TIME);
				Reader.db.insertSyncAction(temp, "TEXT", SaveValue.nowIndex, "COMMENT");
			}
			
		}
	}
}
