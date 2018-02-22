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

package org.geometerplus.fbreader.fbreader;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.sqliteconfig.SQLiteDB;
import org.geometerplus.zlibrary.text.view.SaveValue;
import org.geometerplus.zlibrary.text.view.ZLTextView;

public class BackgroundColorAction extends FBAction {
	BackgroundColorAction(FBReaderApp fbreader) {
		super(fbreader);
	}

	@Override
	protected void run() {

		/*save*/
		//選取範圍固定
		saveSelected();
		//clear all......
		Reader.getTextView().clearSelection();
		
	}
	
	public void saveSelected() {
		
		final SQLiteDB db = ZLApplication.Instance().db;
		//int num = db.getTableCount("annotext");  //註記個數
		int num = db.getMaxID(ActionCode.ANNOTATION_TEXT);  //註記個數( 取最大ID )
		int page = SaveValue.pageIndex;
		String user = SaveValue.UserName;
		
		for(int i=0;i<ZLTextView.y;i++) {
			//新增註記範圍
			Reader.db.insertRange((num+1), ZLTextView.left[i], ZLTextView.top[i], ZLTextView.right[i], ZLTextView.bottom[i], SaveValue.UserName);//draw
		}
		//選取的文字
		String txt = Reader.getTextView().getSelectedText();
		//新增註記
		Reader.db.insertNote((num+1), 255, 0, 0, user, page, txt);//note
	}
}

