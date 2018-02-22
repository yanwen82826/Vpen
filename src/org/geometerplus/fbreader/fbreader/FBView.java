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

import java.util.*;

import org.geometerplus.zlibrary.core.sqliteconfig.SQLiteDB;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;

import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.view.*;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidWidget;

import com.thirdEdition.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.bookmodel.FBHyperlinkType;
import org.geometerplus.fbreader.bookmodel.TOCTree;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

public final class FBView extends ZLTextView {
	private FBReaderApp myReader;

	FBView(FBReaderApp reader) {
		super(reader);
		myReader = reader;
	}

	public void setModel(ZLTextModel model) {
		super.setModel(model);
		if (myFooter != null) {
			myFooter.resetTOCMarks();
		}
	}

	private int myStartY;
	private boolean myIsBrightnessAdjustmentInProgress;
	private int myStartBrightness;

	private String myZoneMapId;
	private TapZoneMap myZoneMap;

	private TapZoneMap getZoneMap() {
		//final String id =
		//	ScrollingPreferences.Instance().TapZonesSchemeOption.getValue().toString();
		final String id =
			ScrollingPreferences.Instance().HorizontalOption.getValue()
				? "right_to_left" : "up";
		if (!id.equals(myZoneMapId)) {
			myZoneMap = new TapZoneMap(id);
			myZoneMapId = id;
		}
		return myZoneMap;
	}

	public boolean onFingerSingleTap(int x, int y) {
		if (super.onFingerSingleTap(x, y)) {
			return true;
		}

		final ZLTextRegion region = findRegion(x, y, MAX_SELECTION_DISTANCE, ZLTextRegion.HyperlinkFilter);
		if (region != null) {
			selectRegion(region);
			myReader.getViewWidget().reset();
			myReader.getViewWidget().repaint();
			myReader.doAction(ActionCode.PROCESS_HYPERLINK);
			return true;
		}

		myReader.doActionWithCoordinates(getZoneMap().getActionByCoordinates(
			x, y, myContext.getWidth(), myContext.getHeight(),
			isDoubleTapSupported() ? TapZoneMap.Tap.singleNotDoubleTap : TapZoneMap.Tap.singleTap
		), x, y);

		return true;
	}

	@Override
	public boolean isDoubleTapSupported() {
		return myReader.EnableDoubleTapOption.getValue();
	}

	@Override
	public boolean onFingerDoubleTap(int x, int y) {
		if (super.onFingerDoubleTap(x, y)) {
			return true;
		}
		myReader.doActionWithCoordinates(getZoneMap().getActionByCoordinates(
			x, y, myContext.getWidth(), myContext.getHeight(), TapZoneMap.Tap.doubleTap
		), x, y);
		return true;
	}

	public boolean onFingerPress(int x, int y) {
		if (super.onFingerPress(x, y)) {
			return true;
		}

		final ZLTextSelectionCursor cursor = findSelectionCursor(x, y, MAX_SELECTION_DISTANCE);
		if (cursor != ZLTextSelectionCursor.None) {
			myReader.doAction(ActionCode.SELECTION_HIDE_PANEL);
			moveSelectionCursorTo(cursor, x, y);
			return true;
		}

		if (myReader.AllowScreenBrightnessAdjustmentOption.getValue() && x < myContext.getWidth() / 10) {
			myIsBrightnessAdjustmentInProgress = true;
			myStartY = y;
			myStartBrightness = ZLibrary.Instance().getScreenBrightness();
			return true;
		}

		startManualScrolling(x, y);
		return true;
	}

	private boolean isFlickScrollingEnabled() {
		final ScrollingPreferences.FingerScrolling fingerScrolling =
			ScrollingPreferences.Instance().FingerScrollingOption.getValue();
		return
			fingerScrolling == ScrollingPreferences.FingerScrolling.byFlick ||
			fingerScrolling == ScrollingPreferences.FingerScrolling.byTapAndFlick;
	}

	private void startManualScrolling(int x, int y) {
		if (!isFlickScrollingEnabled()) {
			return;
		}
		final boolean horizontal = ScrollingPreferences.Instance().HorizontalOption.getValue();
		final Direction direction = horizontal ? Direction.rightToLeft : Direction.up;
		myReader.getViewWidget().startManualScrolling(x, y, direction);
	}

	public boolean onFingerMove(int x, int y) {
		if (super.onFingerMove(x, y)) {
			return true;
		}
		final ZLTextSelectionCursor cursor = getSelectionCursorInMovement();
		if (cursor != ZLTextSelectionCursor.None) {
			if( y == SaveValue.y_range )
				moveSelectionCursorTo(cursor, x, y);
			else
				moveSelectionCursorTo(cursor, x, SaveValue.y_range);
			return true;
		}

		synchronized (this) {
			if (myIsBrightnessAdjustmentInProgress) {
				if (x >= myContext.getWidth() / 5) {
					myIsBrightnessAdjustmentInProgress = false;
					startManualScrolling(x, y);
				} else {
					final int delta = (myStartBrightness + 30) * (myStartY - y) / myContext.getHeight();
					ZLibrary.Instance().setScreenBrightness(myStartBrightness + delta);
					return true;
				}
			}

			if (isFlickScrollingEnabled()) {
				myReader.getViewWidget().scrollManuallyTo(x, y);
			}
		}
		return true;
	}

	public boolean onFingerRelease(int x, int y) {
		if (super.onFingerRelease(x, y)) {
			return true;
		}

		final ZLTextSelectionCursor cursor = getSelectionCursorInMovement();
		if (cursor != ZLTextSelectionCursor.None) {
			releaseSelectionCursor();
			return true;
		}

		if (myIsBrightnessAdjustmentInProgress) {
			myIsBrightnessAdjustmentInProgress = false;
			return true;
		}

		if (isFlickScrollingEnabled()) {
			myReader.getViewWidget().startAnimatedScrolling(
				x, y, ScrollingPreferences.Instance().AnimationSpeedOption.getValue()
			);
			return true;
		}

		return true;
	}
	
	//long Press
	//test
	private boolean selection(int x, int y) {
		myReader.doAction(ActionCode.SELECTION_HIDE_PANEL);  //隱藏小選單
		initSelection(x, y);
		final ZLTextSelectionCursor cursor = findSelectionCursor(x, y);
		if (cursor != ZLTextSelectionCursor.None) {
			moveSelectionCursorTo(cursor, x, y);
		}
		//記錄剛選取文字的軸
		SaveValue.y_range = y;
		return true;
	}
	
	//20111012
	private boolean region(int x, int y) {
		
		int temp = 20;
		final ZLTextRegion region = findRegion(x, y, MAX_SELECTION_DISTANCE, ZLTextRegion.AnyRegionFilter);
		
		for(int i=1;i<=temp;i++) {
			final ZLTextRegion tempRegion1 = findRegion(x, y-i, MAX_SELECTION_DISTANCE, ZLTextRegion.AnyRegionFilter);
			final ZLTextRegion tempRegion2 = findRegion(x, y+i, MAX_SELECTION_DISTANCE, ZLTextRegion.AnyRegionFilter);
			if( tempRegion1 != null && tempRegion1.getSoul() instanceof ZLTextWordRegionSoul ) {
				y -= i;
				if( selection(x, y) )
					return true;
			} else if( tempRegion2 != null && tempRegion2.getSoul() instanceof ZLTextWordRegionSoul ) {
				y += i;
				if( selection(x, y) )
					return true;
			}
		}

		if (region != null) {
			final ZLTextRegion.Soul soul = region.getSoul();
			boolean doSelectRegion = false;
			
			//判斷使用者點的物件
			//文字
			if (soul instanceof ZLTextWordRegionSoul) { //左邊是右邊的一部分嗎?
				if( selection(x, y) )
					return true;
			}else {
				//Log.i("test", "test selection annopic x="+x+" and y="+y);
				myReader.doAction(ActionCode.SELECTION_HIDE_PANEL);  //隱藏小選單
				myReader.getViewWidget().reset();
				myReader.getViewWidget().repaint();
				clearSelection();
				//Long Click 圖片註記  20120404
				if( !SaveValue.IsSyncData ) {
					/*
					int newx = checkXY(x, 130, 1200);
					if( newx < 150 )
						newx = 150;
					int newy = checkXY(y, 25, 700);
					
					//偵測是否在文字區域
					onPress(newx, newy); //20110831
					 */
					onPress(x, y); //20110831
				}
			}
			
			if (doSelectRegion) {
				selectRegion(region);
				myReader.getViewWidget().reset();
				myReader.getViewWidget().repaint();
				return true;
			}
		}
		return false;
	}	

	public boolean onFingerLongPress(int x, int y) {
		if (super.onFingerLongPress(x, y)) {
			return true;
		}
		return region(x,y);

		/*final ZLTextRegion region = findRegion(x, y, MAX_SELECTION_DISTANCE, ZLTextRegion.AnyRegionFilter);
		if (region != null) {
			final ZLTextRegion.Soul soul = region.getSoul();
			boolean doSelectRegion = false;
			if (soul instanceof ZLTextWordRegionSoul) {
				switch (myReader.WordTappingActionOption.getValue()) {
					case startSelecting:
						myReader.doAction(ActionCode.SELECTION_HIDE_PANEL);
						initSelection(x, y);
						final ZLTextSelectionCursor cursor = findSelectionCursor(x, y);
						if (cursor != ZLTextSelectionCursor.None) {
							moveSelectionCursorTo(cursor, x, y);
						}
						return true;
					case selectSingleWord:
					case openDictionary:
						doSelectRegion = true;
						break;
				}
			} else if (soul instanceof ZLTextImageRegionSoul) {
				//doSelectRegion =
				//	myReader.ImageTappingActionOption.getValue() !=
				//	FBReaderApp.ImageTappingAction.doNothing;
			} else if (soul instanceof ZLTextHyperlinkRegionSoul) {
				doSelectRegion = true;
			}
        
			if (doSelectRegion) {
				selectRegion(region);
				myReader.getViewWidget().reset();
				myReader.getViewWidget().repaint();
				return true;
			}
		}

		return false;*/
	}

	public boolean onFingerMoveAfterLongPress(int x, int y) {
		if (super.onFingerMoveAfterLongPress(x, y)) {
			return true;
		}

		final ZLTextSelectionCursor cursor = getSelectionCursorInMovement();
		if (cursor != ZLTextSelectionCursor.None) {
			if( y == SaveValue.y_range )
				moveSelectionCursorTo(cursor, x, y);
			else
				moveSelectionCursorTo(cursor, x, SaveValue.y_range);
			//moveSelectionCursorTo(cursor, x, y);
			return true;
		}

		ZLTextRegion region = getSelectedRegion();
		if (region != null) {
			ZLTextRegion.Soul soul = region.getSoul();
			if (soul instanceof ZLTextHyperlinkRegionSoul ||
				soul instanceof ZLTextWordRegionSoul) {
				if (myReader.WordTappingActionOption.getValue() !=
					FBReaderApp.WordTappingAction.doNothing) {
					region = findRegion(x, y, MAX_SELECTION_DISTANCE, ZLTextRegion.AnyRegionFilter);
					if (region != null) {
						soul = region.getSoul();
						if (soul instanceof ZLTextHyperlinkRegionSoul
							 || soul instanceof ZLTextWordRegionSoul) {
							selectRegion(region);
							myReader.getViewWidget().reset();
							myReader.getViewWidget().repaint();
						}
					}
				}
			}
		}
		return true;
	}

	public boolean onFingerReleaseAfterLongPress(int x, int y) {
		if (super.onFingerReleaseAfterLongPress(x, y)) {
			return true;
		}

		final ZLTextSelectionCursor cursor = getSelectionCursorInMovement();
		if (cursor != ZLTextSelectionCursor.None) {
			releaseSelectionCursor();
			return true;
		}

		final ZLTextRegion region = getSelectedRegion();
		if (region != null) {
			final ZLTextRegion.Soul soul = region.getSoul();

			boolean doRunAction = false;
			if (soul instanceof ZLTextWordRegionSoul) {
				doRunAction =
					myReader.WordTappingActionOption.getValue() ==
					FBReaderApp.WordTappingAction.openDictionary;
			} else if (soul instanceof ZLTextImageRegionSoul) {
				doRunAction =
					myReader.ImageTappingActionOption.getValue() ==
					FBReaderApp.ImageTappingAction.openImageView;
			}

			if (doRunAction) {
				myReader.doAction(ActionCode.PROCESS_HYPERLINK);
				return true;
			}
		}

		return false;
	}

	public boolean onTrackballRotated(int diffX, int diffY) {
		if (diffX == 0 && diffY == 0) {
			return true;
		}

		final Direction direction = (diffY != 0) ?
			(diffY > 0 ? Direction.down : Direction.up) :
			(diffX > 0 ? Direction.leftToRight : Direction.rightToLeft);

		ZLTextRegion region = getSelectedRegion();
		final ZLTextRegion.Filter filter =
			(region != null && region.getSoul() instanceof ZLTextWordRegionSoul)
				|| myReader.NavigateAllWordsOption.getValue()
					? ZLTextRegion.AnyRegionFilter : ZLTextRegion.ImageOrHyperlinkFilter;
		region = nextRegion(direction, filter);
		if (region != null) {
			selectRegion(region);
		} else {
			if (direction == Direction.down) {
				scrollPage(true, ZLTextView.ScrollingMode.SCROLL_LINES, 1);
			} else if (direction == Direction.up) {
				scrollPage(false, ZLTextView.ScrollingMode.SCROLL_LINES, 1);
			}
		}

		myReader.getViewWidget().reset();
		myReader.getViewWidget().repaint();

		return true;
	}

	@Override
	public int getLeftMargin() {
		return myReader.LeftMarginOption.getValue();
	}

	@Override
	public int getRightMargin() {
		return myReader.RightMarginOption.getValue();
	}

	@Override
	public int getTopMargin() {
		return myReader.TopMarginOption.getValue();
	}

	@Override
	public int getBottomMargin() {
		return myReader.BottomMarginOption.getValue();
	}

	@Override
	public ZLFile getWallpaperFile() {
		final String filePath = myReader.getColorProfile().WallpaperOption.getValue();
		if ("".equals(filePath)) {
			return null;
		}
		
		final ZLFile file = ZLFile.createFileByPath(filePath);
		if (file == null || !file.exists()) {
			return null;
		}
		return file;
	}

	@Override
	public ZLColor getBackgroundColor() {
		return myReader.getColorProfile().BackgroundOption.getValue();
	}

	@Override
	public ZLColor getSelectedBackgroundColor() {
		return myReader.getColorProfile().SelectionBackgroundOption.getValue();
	}

	@Override
	public ZLColor getSelectedForegroundColor() {
		return myReader.getColorProfile().SelectionForegroundOption.getValue();
	}

	@Override
	public ZLColor getTextColor(ZLTextHyperlink hyperlink) {
		final ColorProfile profile = myReader.getColorProfile();
		switch (hyperlink.Type) {
			default:
			case FBHyperlinkType.NONE:
				return profile.RegularTextOption.getValue();
			case FBHyperlinkType.INTERNAL:
				return myReader.Model.Book.isHyperlinkVisited(hyperlink.Id)
					? profile.VisitedHyperlinkTextOption.getValue()
					: profile.HyperlinkTextOption.getValue();
			case FBHyperlinkType.EXTERNAL:
				return profile.HyperlinkTextOption.getValue();
		}
	}

	@Override
	public ZLColor getHighlightingColor() {
		return myReader.getColorProfile().HighlightingOption.getValue();
	}

	private class Footer implements FooterArea {
		private Runnable UpdateTask = new Runnable() {
			public void run() {
				myReader.getViewWidget().repaint();
			}
		};

		private ArrayList<TOCTree> myTOCMarks;

		public int getHeight() {
			return myReader.FooterHeightOption.getValue();
		}

		public synchronized void resetTOCMarks() {
			myTOCMarks = null;
		}

		private final int MAX_TOC_MARKS_NUMBER = 100;
		private synchronized void updateTOCMarks(BookModel model) {
			myTOCMarks = new ArrayList<TOCTree>();
			TOCTree toc = model.TOCTree;
			if (toc == null) {
				return;
			}
			int maxLevel = Integer.MAX_VALUE;
			if (toc.getSize() >= MAX_TOC_MARKS_NUMBER) {
				final int[] sizes = new int[10];
				for (TOCTree tocItem : toc) {
					if (tocItem.Level < 10) {
						++sizes[tocItem.Level];
					}
				}
				for (int i = 1; i < sizes.length; ++i) {
					sizes[i] += sizes[i - 1];
				}
				for (maxLevel = sizes.length - 1; maxLevel >= 0; --maxLevel) {
					if (sizes[maxLevel] < MAX_TOC_MARKS_NUMBER) {
						break;
					}
				}
			}
			for (TOCTree tocItem : toc.allSubTrees(maxLevel)) {
				myTOCMarks.add(tocItem);
			}
		}

		public synchronized void paint(ZLPaintContext context) {
			final FBReaderApp reader = myReader;
			if (reader == null) {
				return;
			}
			final BookModel model = reader.Model;
			if (model == null) {
				return;
			}

			//final ZLColor bgColor = getBackgroundColor();
			// TODO: separate color option for footer color
			final ZLColor fgColor = getTextColor(ZLTextHyperlink.NO_LINK);
			final ZLColor fillColor = reader.getColorProfile().FooterFillOption.getValue();

			final int left = getLeftMargin();
			final int right = context.getWidth() - getRightMargin();
			final int height = getHeight();
			final int lineWidth = height <= 10 ? 1 : 2;
			final int delta = height <= 10 ? 0 : 1;
			context.setFont(
				reader.FooterFontOption.getValue(),
				height <= 10 ? height + 3 : height + 1,
				height > 10, false, false
			);

			final int pagesProgress = computeCurrentPage();
			final int bookLength = computePageNumber();

			final StringBuilder info = new StringBuilder();
			if (reader.FooterShowProgressOption.getValue()) {
				info.append(pagesProgress);
				info.append("/");
				info.append(bookLength);
			}
			if (reader.FooterShowBatteryOption.getValue()) {
				if (info.length() > 0) {
					info.append(" ");
				}
				info.append(reader.getBatteryLevel());
				info.append("%");
			}
			if (reader.FooterShowClockOption.getValue()) {
				if (info.length() > 0) {
					info.append(" ");
				}
				info.append(ZLibrary.Instance().getCurrentTimeString());
			}
			final String infoString = info.toString();

			final int infoWidth = context.getStringWidth(infoString);
			final ZLFile wallpaper = getWallpaperFile();
			if (wallpaper != null) {
				context.clear(wallpaper, wallpaper instanceof ZLResourceFile);
			} else {
				context.clear(getBackgroundColor());
			}

			// draw info text
			context.setTextColor(fgColor);
			context.drawString(right - infoWidth, height - delta, infoString);

			// draw gauge
			final int gaugeRight = right - (infoWidth == 0 ? 0 : infoWidth + 10);
			myGaugeWidth = gaugeRight - left - 2 * lineWidth;

			context.setLineColor(fgColor);
			context.setLineWidth(lineWidth);
			context.drawLine(left, lineWidth, left, height - lineWidth);
			context.drawLine(left, height - lineWidth, gaugeRight, height - lineWidth);
			context.drawLine(gaugeRight, height - lineWidth, gaugeRight, lineWidth);
			context.drawLine(gaugeRight, lineWidth, left, lineWidth);

			final int gaugeInternalRight =
				left + lineWidth + (int)(1.0 * myGaugeWidth * pagesProgress / bookLength);

			context.setFillColor(fillColor);
			context.fillRectangle(left + 1, height - 2 * lineWidth, gaugeInternalRight, lineWidth + 1);

			if (reader.FooterShowTOCMarksOption.getValue()) {
				if (myTOCMarks == null) {
					updateTOCMarks(model);
				}
				final int fullLength = sizeOfFullText();
				for (TOCTree tocItem : myTOCMarks) {
					TOCTree.Reference reference = tocItem.getReference();
					if (reference != null) {
						final int refCoord = sizeOfTextBeforeParagraph(reference.ParagraphIndex);
						final int xCoord =
							left + 2 * lineWidth + (int)(1.0 * myGaugeWidth * refCoord / fullLength);
						context.drawLine(xCoord, height - lineWidth, xCoord, lineWidth);
					}
				}
			}
		}

		// TODO: remove
		int myGaugeWidth = 1;
		/*public int getGaugeWidth() {
			return myGaugeWidth;
		}*/

		/*public void setProgress(int x) {
			// set progress according to tap coordinate
			int gaugeWidth = getGaugeWidth();
			float progress = 1.0f * Math.min(x, gaugeWidth) / gaugeWidth;
			int page = (int)(progress * computePageNumber());
			if (page <= 1) {
				gotoHome();
			} else {
				gotoPage(page);
			}
			myReader.getViewWidget().reset();
			myReader.getViewWidget().repaint();
		}*/
	}

	private Footer myFooter;

	@Override
	public Footer getFooterArea() {
		if (myReader.ScrollbarTypeOption.getValue() == SCROLLBAR_SHOW_AS_FOOTER) {
			if (myFooter == null) {
				myFooter = new Footer();
				myReader.addTimerTask(myFooter.UpdateTask, 15000);
			}
		} else {
			if (myFooter != null) {
				myReader.removeTimerTask(myFooter.UpdateTask);
				myFooter = null;
			}
		}
		return myFooter;
	}

	@Override
	protected void releaseSelectionCursor() {
		super.releaseSelectionCursor();
		if (getCountOfSelectedWords() > 0) {
			if(!FBReader.setImage)
				myReader.doAction(ActionCode.SELECTION_SHOW_PANEL);
			else
				addPicNote(ZLAndroidWidget.x, ZLAndroidWidget.y);
		}
	}

	public String getSelectedText() {
		final TextBuildTraverser traverser = new TextBuildTraverser(this);
		if (!isSelectionEmpty()) {
			traverser.traverse(getSelectionStartPosition(), getSelectionEndPosition());
		}
		return traverser.getText();
	}

	public int getCountOfSelectedWords() {
		final WordCountTraverser traverser = new WordCountTraverser(this);
		if (!isSelectionEmpty()) {
			traverser.traverse(getSelectionStartPosition(), getSelectionEndPosition());
		}
		return traverser.getCount();
	}

	public static final int SCROLLBAR_SHOW_AS_FOOTER = 3;

	@Override
	public int scrollbarType() {
		return myReader.ScrollbarTypeOption.getValue();
	}

	@Override
	public Animation getAnimationType() {
		return ScrollingPreferences.Instance().AnimationOption.getValue();
	}
	
	/*舊
	//強制開啟書籍
	@Override
	public boolean settingBook() {
		//myReader.doAction(ActionCode.SHOW_DIALOG_NOTE);
		myReader.doAction(ActionCode.SHOW_LIBRARY);
		return true;
	}*/
	
	@Override
	public boolean onClick() {
		myReader.doAction(ActionCode.SHOW_ANNO_TEXT_MENU);
		return true;
	}
	
	@Override
	public boolean onClick_pic() {
		myReader.doAction(ActionCode.SHOW_ANNO_PIC_MENU); 
		return true;
	}
	
	@Override
	public int checkXY(int x, int temp, int limit) {
		x = Math.abs(x);
		if( x%temp >= temp/2 )
			x = x - x%temp +temp;
		else
			x = x - x%temp;
		
		if( x > limit )
			x = limit;
		//if( x < 0 )
		//	x = 0;
		if( x < 50 )
			x = 50;
		
		return x;
	}
	
	public boolean checkRegion(int x, int y) {
		final ZLTextRegion region = findRegion(x, y, MAX_SELECTION_DISTANCE, ZLTextRegion.AnyRegionFilter);
		if (region != null ) {
			final ZLTextRegion.Soul soul = region.getSoul();
			if (soul instanceof ZLTextWordRegionSoul) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void onPress(int x, int y) {
		if(!FBReader.setImage){
			final ZLTextRegion region = findRegion(x, y, MAX_SELECTION_DISTANCE, ZLTextRegion.AnyRegionFilter);
			if( region != null ) {   //使用者選取位置不在文字上
				final ZLTextRegion.Soul soul = region.getSoul();
				if (soul instanceof ZLTextWordRegionSoul) {  //如果是文字 則不註記
					return;
				}
			}
		}
		addPicNote(x, y);
	}
	
	private void addPicNote(int x, int y) {
		if(!FBReader.setImage){
			SaveValue.picNoteOpen = false; // 回到預設值
			int page = SaveValue.pageIndex;
			String user = SaveValue.UserName;
			
			final SQLiteDB db = ZLApplication.Instance().db;
		
			try {
				int num = db.getMaxID(ActionCode.ANNOTATION_IMAGE); //註記個數( 取最大ID )
				db.insertPicNote( (num+1), "IMAGE", x, y, page, user); //picNote 儲存
				SaveValue.picNowIndex = num+1;
				Log.i("log", "SaveValue.picNowIndex has been assigned (FBView Line 809)");
				myReader.doAction(ActionCode.SHOW_ANNO_PIC_MENU);
			}
			catch (Exception e) {
				Log.i("log", "addPicNote method"+e+ "error(FBView Line 809)");
			}
		}
		else{
			final SQLiteDB db = ZLApplication.Instance().db;
			try{
				SaveValue.picNoteOpen = false;
				//int num = db.getTableCount("annoimage", user); //註記個數
				//int num = db.getTableCount("annoimage"); //註記個數
				//**********下兩行跟秀出圖片註記對畫框有關(必須用)
				int num = db.getMaxID("takepic_anno");
				db.YaninsertPicNote((num+1), "IMAGE", x, y, SaveValue.UserName, SaveValue.picLesson);
				SaveValue.picNowIndex = num+1;
				Log.i("log", "SaveValue.picNowIndex has been assigned (FBView Line 826)");
				myReader.doAction(ActionCode.SHOW_ANNO_PIC_MENU);
			}
			catch(Exception e){
				Log.i("log", "addPicNote method"+e+ "error");
			}
		}
		
	}
	
	//提示視窗
	@Override
	public void animationImage(int x, int y) {
		SaveValue.IsAnimation = true;
		SaveValue.animationOff.setDuration(3000);
		final ZLApplication view = ZLApplication.Instance();
		view.fbread.v.setAlpha(255);
		view.fbread.v.startAnimation(SaveValue.animationOff);
		
		//SaveValue.animationOff.start();
		//view.root.removeView(v);
  	  	
//  	  	if( !SaveValue.IsSyncData ) {  家長5
//  	  		final SQLiteDB db = ZLApplication.Instance().db;
//  	  		int[] id = db.getRangePage("_id", ActionCode.ANNOTATION_IMAGE, SaveValue.pageIndex, SaveValue.UserName, "PARENTS"); 
//  	  		view.fbread.tick.setImageBitmap(null);
//  	  		if( id.length >= 1 )                 
//  	  			view.fbread.tick.setImageBitmap(BitmapFactory.decodeStream(view.fbread.getResources().openRawResource(R.drawable.ic_tick)));
//  	  		else
//  	  			view.fbread.tick.setImageBitmap(BitmapFactory.decodeStream(view.fbread.getResources().openRawResource(R.drawable.ic_tick_close)));
//  	  	}
  	  	
  	  	
  	  	MyThread thread = new MyThread(1500, handler, "AnimationOff");
  	  	new Thread(thread).start();
		
	}
	private final int ID_USER = 0;
	//private int tempPage = 0;
	public Handler handler = new Handler(){  
        public void handleMessage(Message msg) { 
            switch (msg.what) {
            case ID_USER:
            	try {
            		Bundle data = msg.getData();
                    int ms = data.getInt("AnimationOff");
                    if( ms == 300 ) {
                    	final ZLApplication view = ZLApplication.Instance();
                    	view.fbread.v.setAlpha(0);
                    }
                    SaveValue.IsAnimation = false;
            	} catch (Exception e) {
            		Log.i("test", "test e = "+e);
            	}
            }  
        };  
    };
}
