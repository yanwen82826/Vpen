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

import android.view.View;
import android.widget.RelativeLayout;

import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import com.thirdEdition.geometerplus.zlibrary.ui.android.R;

class SelectionPopup extends ButtonsPopupPanel {
	final static String ID = "SelectionPopup";

	SelectionPopup(FBReaderApp fbReader) {
		super(fbReader);
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void createControlPanel(FBReader activity, RelativeLayout root, PopupWindow.Location location) {
		if (myWindow != null) {
			return;
		}

		myWindow = new PopupWindow(activity, root, location, false);

		addButton(ActionCode.SELECTION_BACKGROUND, true, R.drawable.selection_bookmark);
        //addButton(ActionCode.SELECTION_SHARE, true, R.drawable.selection_share);
        addButton(ActionCode.SELECTION_TRANSLATE, true, R.drawable.selection_translate);
        addButton(ActionCode.SELECTION_COPY_TO_CLIPBOARD, true, R.drawable.selection_copy);
        //addButton(ActionCode.SELECTION_BOOKMARK, true, R.drawable.selection_bookmark);
        addButton(ActionCode.SELECTION_CLEAR, true, R.drawable.selection_close);
    }
    
    public void move(int selectionStartY, int selectionEndY) {
		if (myWindow == null) {
			return;
		}

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
			RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
		);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        final int verticalPosition;
        //screenHeight = 752
        final int screenHeight = ((View)myWindow.getParent()).getHeight();
        //�p�����ù��U��Z��
		final int diffTop = screenHeight - selectionEndY;
		//�p�����ù��W��Z��
		final int diffBottom = selectionStartY;
		
		/*
		if (diffTop > diffBottom) {
			verticalPosition = diffTop > myWindow.getHeight() + 20
				? RelativeLayout.ALIGN_PARENT_BOTTOM : RelativeLayout.CENTER_VERTICAL;
		} else {
			verticalPosition = diffBottom > myWindow.getHeight() + 20
				? RelativeLayout.ALIGN_PARENT_TOP : RelativeLayout.CENTER_VERTICAL;
		}

        layoutParams.addRule(verticalPosition);*/
		if(diffBottom > 50)
			layoutParams.setMargins(0, diffBottom-50, 0, 0);
		else if(diffTop > diffBottom+300)
        	layoutParams.setMargins(0, diffBottom+100, 0, 0);
        else
        	layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		
        myWindow.setLayoutParams(layoutParams);
    }
}