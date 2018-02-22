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

import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.FBView;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.sqliteconfig.SQLiteDB;
import org.geometerplus.zlibrary.text.view.SaveValue;

public class SelectionTranslateAction extends FBAndroidAction {
    SelectionTranslateAction(FBReader baseActivity, FBReaderApp fbreader) {
        super(baseActivity, fbreader);
    }

    public void run() {
    	
        final FBView fbview = Reader.getTextView();
        String str = fbview.getSelectedText();
        
        DictionaryUtil.openTextInDictionary(
			BaseActivity,
        	fbview.getSelectedText(),
			fbview.getCountOfSelectedWords() == 1,
        	fbview.getSelectionStartY(),
			fbview.getSelectionEndY()
		);
        fbview.clearSelection();
        
        final SQLiteDB db = ZLApplication.Instance().db;
        int page = SaveValue.pageIndex;
        String user = SaveValue.UserName;
        
        //目前translation TABLE的值有多少
        int translateTableNum = db.getTableCount(ActionCode.ANNOTATION_TRANSLATION, user);
        //和 str 相同的有多少
        int translateNum = db.getTranslateNum(ActionCode.ANNOTATION_TRANSLATION, str);
        
        if( translateNum > 0 ){ //如果資料(selectText)有重複 則不新增
        	int id = db.getTranslateId("translation", str);
        	db.updateTranslateNum(id, (translateNum+1));
        }else{
        	//紀錄
        	db.insertTranslationData( (translateTableNum+1), 0, str, user, page);  //0是未標註
        }
    }
}
