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

import android.content.Intent;

import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.bookmodel.BookModel;

import org.geometerplus.android.fbreader.library.LibraryTopLevelActivity;

class ShowLibraryAction extends FBAndroidAction {
	ShowLibraryAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	public void run() {
		final BookModel model = Reader.Model;
		Intent intent = new Intent(BaseActivity.getApplicationContext(), LibraryTopLevelActivity.class);
		if (model != null && model.Book != null) {
			intent.putExtra(LibraryTopLevelActivity.SELECTED_BOOK_PATH_KEY, model.Book.File.getPath());
		}
		BaseActivity.startActivity(intent);
		
		/*��
		Intent intent = new Intent(BaseActivity.getApplicationContext(), FBReader.class)
		.setAction(Intent.ACTION_VIEW)
		.putExtra(FBReader.BOOK_PATH_KEY, "/mnt/sdcard/content002.epub")   //���w��m
		.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		BaseActivity.startActivity(intent);*/
	}
}
