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

import org.geometerplus.zlibrary.core.dialogs.DialogMoveTabPic;
import org.geometerplus.zlibrary.core.dialogs.DialogMoveTabPic_sync;
import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.view.SaveValue;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import com.thirdEdition.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

import android.util.Log;
import android.view.Window;
import android.widget.LinearLayout;


class ShowPicNoteTabDialogAction extends FBAndroidAction {
	////NEW DIALOG
	ShowPicNoteTabDialogAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
	}
	@Override
	public boolean isVisible() {
		final ZLTextView view = (ZLTextView)Reader.getCurrentView();
		final ZLTextModel textModel = view.getModel();
		return textModel != null && textModel.getParagraphsNumber() != 0;
	}
	public void run() {
		if( SaveValue.IsNote ) {
			SaveValue.IsNote = false; //關閉註記key
			if( !SaveValue.IsSyncData ) {
				LinearLayout markLayout = (LinearLayout) BaseActivity.getLayoutInflater().inflate(R.layout.tabhost_mainpic, null);
			    
				DialogMoveTabPic dialogmove = new DialogMoveTabPic(BaseActivity, R.style.TANCStyle);
				if( !SaveValue.Islecture )
					dialogmove.changeDbTable("IMAGE");
				else
					dialogmove.changeDbTable("LECTURE");
				dialogmove.setFBReaderApp(Reader);
				dialogmove.setFBReader(BaseActivity);
				dialogmove.requestWindowFeature(Window.FEATURE_NO_TITLE); //要放在setContentView之前
				dialogmove.setContentView(markLayout);
				dialogmove.setCancelable(false);
				dialogmove.show();
			} else {
				LinearLayout markLayout = (LinearLayout) BaseActivity.getLayoutInflater().inflate(R.layout.tabhost_sync_mainpic, null);
			    
				DialogMoveTabPic_sync dialogmove = new DialogMoveTabPic_sync(BaseActivity, R.style.TANCStyle);
				if( !SaveValue.Islecture )
					dialogmove.changeDbTable("IMAGE");
				else
					dialogmove.changeDbTable("LECTURE");
				dialogmove.setFBReaderApp(Reader);
				//dialogmove.setFBReader(BaseActivity);
				dialogmove.requestWindowFeature(Window.FEATURE_NO_TITLE); //要放在setContentView之前
				dialogmove.setContentView(markLayout);
				dialogmove.setCancelable(false);
				dialogmove.show();
				
				int temp = Reader.db.getTableCount(ActionCode.ANNOTATION_SYNC_TIME);
				Reader.db.insertSyncAction(temp, "IMAGE", SaveValue.nowIndex, "COMMENT");
			}
		}
			
		
	}
}
