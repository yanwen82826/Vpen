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

import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.text.view.SaveValue;

import android.content.Intent;
import android.util.Log;

class ShowTOCAction extends RunActivityAction {
	ShowTOCAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader, TOCActivity.class);
	}

	public boolean isVisible() {
		return (Reader.Model != null) && Reader.Model.TOCTree.hasChildren();
	}
	
	@Override
	public void run() {
		super.run();
		SaveValue.IsRepaint = true; //因為切換目錄需要刷新頁面
		SaveValue.IsSystemActivity = true; //目前切換到"目錄的Activity"
		
		if (Reader.getCurrentView() != Reader.BookTextView) {
			Reader.showBookTextView();
		} else {
			final List<FBReaderApp.CancelActionDescription> descriptionList = 
				Reader.getCancelActionsList();
			if (descriptionList.size() == 1) {
				//Reader.closeWindow();
			} else {
				Log.i("test", "test showBookTextView2");
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
}
