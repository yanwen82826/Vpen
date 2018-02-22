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
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.text.view.SaveValue;

import android.util.Log;

class SelfDataAction extends FBAndroidAction {
	
	private FBReaderApp Reader;
	private FBReader Base;
	
	//當按下 返回自己註記 的按鈕時
	SelfDataAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
		Reader = fbreader;
		Base = baseActivity;
	}

	public void run() {
		Log.i("測試用訊息","success");
		FBReader.setImage = false;
		SaveValue.setSyncDataOff();
		Reader.getTextView().clear();
		UIUtil.showMessageText(Base, "返回目前的使用者:"+SaveValue.UserName);
		//action
	}
}