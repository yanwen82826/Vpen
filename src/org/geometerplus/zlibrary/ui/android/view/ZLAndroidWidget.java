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

package org.geometerplus.zlibrary.ui.android.view;

import java.util.Calendar;

import android.content.Context;
import android.graphics.*;
import android.graphics.PorterDuff.Mode;
import android.view.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.zlibrary.core.sqliteconfig.SQLiteDB;
import org.geometerplus.zlibrary.core.sqliteconfig.SyncDB;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.core.view.ZLViewWidget;
import org.geometerplus.zlibrary.core.view.ZLView.PageIndex;
import org.geometerplus.zlibrary.core.application.ZLApplication;

import org.geometerplus.zlibrary.text.view.MyThread;
import org.geometerplus.zlibrary.text.view.SaveValue;
import com.thirdEdition.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidActivity;
import org.geometerplus.zlibrary.ui.android.util.ZLAndroidColorUtil;
import org.geometerplus.zlibrary.ui.android.util.ZLAndroidKeyUtil;

public class ZLAndroidWidget extends View implements ZLViewWidget, View.OnLongClickListener {
	private final Paint myPaint = new Paint();
	private final BitmapManager myBitmapManager = new BitmapManager(this);
	private Bitmap myFooterBitmap;
	//take pic
	private String srcPath_pic = "/sdcard/VPen/pic/"; //圖片路徑

	//note
	private Paint mPaintPic = new Paint();
	private Paint mPaintText = new Paint();  
	private Paint mPaintTextBg = new Paint();	
	
	
	public ZLAndroidWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ZLAndroidWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ZLAndroidWidget(Context context) {
		super(context);
		init();
	}

	private void init() {
		// next line prevent ignoring first onKeyDown DPad event
		// after any dialog was closed
		setFocusableInTouchMode(true);
		setDrawingCacheEnabled(false);
		setOnLongClickListener(this);
		//setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		getAnimationProvider().terminate();
		if (myScreenIsTouched) {
			final ZLView view = ZLApplication.Instance().getCurrentView();
			myScreenIsTouched = false;
			view.onScrollingFinished(ZLView.PageIndex.current);
		}
	}
	@Override
	//先判斷有沒有進入個人活動，再判斷有沒有同步觀看註記
	protected void onDraw(final Canvas canvas) {
		final Context context = getContext();
		if (context instanceof ZLAndroidActivity) {
			((ZLAndroidActivity)context).createWakeLock();
		} else {
			System.err.println("A surprise: view's context is not a ZLAndroidActivity");
		}
		super.onDraw(canvas);	

//		final int w = getWidth();
//		final int h = getMainAreaHeight();
		
		//判斷是否進入個人活動觀看照片(FBReader.setimage)
		if(!FBReader.setImage){
			if(getAnimationProvider().inProgress()){
				onDrawInScrolling(canvas);
				SaveValue.tttt = 0;
			}
			else{
				//Log.i("test", "test paint0"+(SaveValue.tttt++));
				//Log.i("test", "test paint draw = "+(SaveValue.draw));
				onDrawStatic(canvas);
				ZLApplication.Instance().onRepaintFinished();
			}
		}
		else{
			String picFilePath;
			Bitmap testImage;
			//判斷是否同步觀看其他人的個人活動
			//(指定存取圖片的路徑)
			if(SaveValue.IsSyncData){
				testImage = BitmapFactory.decodeFile(SaveValue.syncsrcPath_pic);
			}
			else{
				picFilePath = srcPath_pic + SaveValue.picNow;
				testImage = BitmapFactory.decodeFile(picFilePath);
			}
		    Bitmap resizeBmp = Bitmap.createScaledBitmap(testImage, this.getWidth(), this.getHeight(), true);
			//清除屏幕
			myPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
		    canvas.drawPaint(myPaint);
		    myPaint.setXfermode(new PorterDuffXfermode(Mode.SRC));
		    //畫新照片
			canvas.drawBitmap(resizeBmp,0, 0, myPaint);
			//畫照片上的註記
			if(!SaveValue.IsSyncData){
				drawTakepic_anno_Self(canvas);
			}
			else{
				drawTakepic_anno_Other(canvas);
			}
			final ZLView view = ZLApplication.Instance().getCurrentView();
			view.Application.lvm.setEnabled(true); //----------kirk(SaveValue.IsNowSync) 2016/7/11
			//-----------------------------------------------------------------------Kirk 2016/7/11
			//if( !SaveValue.IsNowSync ) {   2016/7/11
			//view.Application.lvm.setAlpha(0); 2016/7/11
			//} 2016/7/11
			if( SaveValue.IsUpdateListView && SaveValue.IsNowSync ) {
				if( SaveValue.IsUpdateListView ) {
					view.Application.lvm.setAlpha(255);
					//view.Application.lvm.setEnabled(true);
					if( view.Application.adter != null ){
						view.Application.adter.checkData();
					}
					SaveValue.IsUpdateListView = false;
				}
			}
			ZLApplication.Instance().onRepaintFinished();
		}
		/*
		if (getAnimationProvider().inProgress() && !FBReader.setImage) {
		    onDrawInScrolling(canvas);
			SaveValue.tttt = 0;
			//SaveValue.draw = true; //開始畫註記
		}
		else if(!getAnimationProvider().inProgress() && FBReader.setImage){
			//清除屏幕
			myPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
		    canvas.drawPaint(myPaint);
		    myPaint.setXfermode(new PorterDuffXfermode(Mode.SRC));
			canvas.drawBitmap(resizeBmp,0, 0, myPaint);
			canvas.save();
		}
		else {
			Log.i("test", "test paint0"+(SaveValue.tttt++));
			//Log.i("test", "test paint draw = "+(SaveValue.draw));
			onDrawStatic(canvas);
			ZLApplication.Instance().onRepaintFinished();
		}
		*/
	}

	private AnimationProvider myAnimationProvider;
	private ZLView.Animation myAnimationType;
	private AnimationProvider getAnimationProvider() {
		final ZLView.Animation type = ZLApplication.Instance().getCurrentView().getAnimationType();
		if (myAnimationProvider == null || myAnimationType != type) {
			myAnimationType = type;
			switch (type) {
				case none:
					myAnimationProvider = new NoneAnimationProvider(myBitmapManager);
					break;
				case curl:
					myAnimationProvider = new CurlAnimationProvider(myBitmapManager);
					break;
				case slide:
					myAnimationProvider = new SlideAnimationProvider(myBitmapManager);
					break;
				case shift:
					myAnimationProvider = new ShiftAnimationProvider(myBitmapManager);
					break;
			}
		}
		return myAnimationProvider;
	}

	private void onDrawInScrolling(Canvas canvas) {
		final ZLView view = ZLApplication.Instance().getCurrentView();

//		final int w = getWidth();
//		final int h = getMainAreaHeight();

		final AnimationProvider animator = getAnimationProvider();
		final AnimationProvider.Mode oldMode = animator.getMode();
		animator.doStep();
		if (animator.inProgress()) {
			animator.draw(canvas);
			if (animator.getMode().Auto) {
				postInvalidate();
				if( !SaveValue.IsAnimation )
					view.animationImage(65, 2);
			}
			
			//drawFooter(canvas);
		} else {
			switch (oldMode) {
				case AnimatedScrollingForward:
				{
					final ZLView.PageIndex index = animator.getPageToScrollTo();
					myBitmapManager.shift(index == ZLView.PageIndex.next);
					view.onScrollingFinished(index);
					ZLApplication.Instance().onRepaintFinished();
					break;
				}
				case AnimatedScrollingBackward:
					view.onScrollingFinished(ZLView.PageIndex.current);
					break;
			}
			onDrawStatic(canvas);
		}
	}

	public void reset() {
		myBitmapManager.reset();
	}

	public void repaint() {
		postInvalidate();
	}

	public void startManualScrolling(int x, int y, ZLView.Direction direction) {
		final AnimationProvider animator = getAnimationProvider();
		animator.setup(direction, getWidth(), getMainAreaHeight());
		animator.startManualScrolling(x, y);
	}

	public void scrollManuallyTo(int x, int y) {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		final AnimationProvider animator = getAnimationProvider();
		if (view.canScroll(animator.getPageToScrollTo(x, y))) {
			animator.scrollTo(x, y);
			postInvalidate();
		}
	}

	public void startAnimatedScrolling(ZLView.PageIndex pageIndex, int x, int y, ZLView.Direction direction, int speed) {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		if (pageIndex == ZLView.PageIndex.current || !view.canScroll(pageIndex)) {
			return;
		}
		final AnimationProvider animator = getAnimationProvider();
		animator.setup(direction, getWidth(), getMainAreaHeight());
		animator.startAnimatedScrolling(pageIndex, x, y, speed);
		if (animator.getMode().Auto) {
			postInvalidate();
		}
	}

	public void startAnimatedScrolling(ZLView.PageIndex pageIndex, ZLView.Direction direction, int speed) {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		if (pageIndex == ZLView.PageIndex.current || !view.canScroll(pageIndex)) {
			return;
		}
		final AnimationProvider animator = getAnimationProvider();
		animator.setup(direction, getWidth(), getMainAreaHeight());
		animator.startAnimatedScrolling(pageIndex, null, null, speed);
		if (animator.getMode().Auto) {
			postInvalidate();
		}
	}

	public void startAnimatedScrolling(int x, int y, int speed) {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		final AnimationProvider animator = getAnimationProvider();
		if (!view.canScroll(animator.getPageToScrollTo(x, y))) {
			animator.terminate();
			return;
		}
		animator.startAnimatedScrolling(x, y, speed);
		postInvalidate();
	}

	void drawOnBitmap(Bitmap bitmap, ZLView.PageIndex index) {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		if (view == null) {
			return;
		}

		final ZLAndroidPaintContext context = new ZLAndroidPaintContext(
			new Canvas(bitmap),
			getWidth(),
			getMainAreaHeight(),
			view.isScrollbarShown() ? getVerticalScrollbarWidth() : 0
		);
		view.paint(context, index);
	}

	private void drawFooter(Canvas canvas) {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		final ZLView.FooterArea footer = view.getFooterArea();

		if (footer == null) {
			myFooterBitmap = null;
			return;
		}

		if (myFooterBitmap != null &&
			(myFooterBitmap.getWidth() != getWidth() ||
			 myFooterBitmap.getHeight() != footer.getHeight())) {
			myFooterBitmap = null;
		}
		if (myFooterBitmap == null) {
			myFooterBitmap = Bitmap.createBitmap(getWidth(), footer.getHeight(), Bitmap.Config.RGB_565);
		}
		final ZLAndroidPaintContext context = new ZLAndroidPaintContext(
			new Canvas(myFooterBitmap),
			getWidth(),
			footer.getHeight(),
			view.isScrollbarShown() ? getVerticalScrollbarWidth() : 0
		);
		footer.paint(context);
		canvas.drawBitmap(myFooterBitmap, 0, getHeight() - footer.getHeight(), myPaint);
	}

	private void onDrawStatic(Canvas canvas) {
		myBitmapManager.setSize(getWidth(), getMainAreaHeight());
		canvas.drawBitmap(myBitmapManager.getBitmap(ZLView.PageIndex.current), 0, 0, myPaint);
		//Log.i("test", "test save");
		//test
		
		//if ( SaveValue.draw ) {
			if( !SaveValue.IsSyncData ) {
				drawAnnotationSelf(canvas);
			} else {
				drawAnnotationOtherSelf(canvas);
			}
		//}
		
		
		final ZLView view = ZLApplication.Instance().getCurrentView();
		
		view.Application.lvm.setEnabled(true); //-------------Kirk 2016/7/11
		if( !SaveValue.IsNowSync ) {
			//view.Application.lvm.setAlpha(0); //--------------Kirk  2016/7/11
		}
		if( SaveValue.IsUpdateListView /* && SaveValue.IsNowSync */) {
		if( SaveValue.IsUpdateListView ) {
			view.Application.lvm.setAlpha(255);
			//view.Application.lvm.setEnabled(true);
			if( view.Application.adter != null )
				view.Application.adter.checkData();
			SaveValue.IsUpdateListView = false;
		}
		}
		//drawSyncOther(canvas); //右方小圖示  選擇學生
	}
	
	private void drawAnnotationOtherSelf(Canvas canvas) {
		final SyncDB db = ZLApplication.Instance().syncdb;
		String userName = SaveValue.SyncUserName;
		int NoteNumber = 0;
		int NotePicNumber = 0;
		try {
			//文字註記個數( 此頁的註記個數 )
			NoteNumber = db.getTableCount(ActionCode.ANNOTATION_TEXT, userName, SaveValue.pageIndex);
			//Log.i("test", "test (Sync) notenumber = "+NoteNumber);
			//Log.i("test", "test (Sync) notenumber = "+ActionCode.ANNOTATION_TEXT);
			//Log.i("test", "test (Sync) notenumber = "+userName);
			//Log.i("test", "test (Sync) notenumber = "+SaveValue.pageIndex);
			//圖片註記個數( 此頁的註記個數 )
			NotePicNumber = db.getTableCount(ActionCode.ANNOTATION_IMAGE, userName, SaveValue.pageIndex);
			//Log.i("log", "test NoteNumber = "+NotePicNumber);
		} catch(Exception e) {
			Log.i("test", "db error :" +e);
		}
		
		//畫底色 + 文字註記
		if( NoteNumber != 0 ) //代表此頁有文字註記
		{
			try {
				//畫底色
				contextDraw(canvas, NoteNumber, db);
				//文字註記
				drawAnnotatedText(canvas, NoteNumber, db);
			} catch (Exception e) {
				Log.i("test", "db error in annoText");
			}
		}
		
		//圖片註記
		if( NotePicNumber != 0 ) //代表此頁有圖片註記
		{
			try {
				//圖片註記
				drawAnnoImage(canvas, NotePicNumber, db);
			} catch(Exception e) {
				Log.i("test", "db error in annoImage");
			}
		}
		
		//drawFooter(canvas);
	}
	//-----------------------------------------------------------
	//自行拍攝照片上的註記
	private void drawTakepic_anno_Self(Canvas canvas){
		final SQLiteDB db = ZLApplication.Instance().db;
		int UserNotePicNumber = 0;
		try{
			//
			UserNotePicNumber = db.getTakepic_anno_Num_self("takepic_anno", SaveValue.UserName, SaveValue.nowLesson);
			//System.out.println("使用者註記次數為"+ UserNotePicNumber);
		}
		catch(Exception e){
			Log.i("log", "test (ZLAndroidWidget) line403 = "+e);
		}
		if(UserNotePicNumber != 0){
			draw_takepic_anno_self(canvas, UserNotePicNumber, db, SaveValue.nowLesson);
		}
		else{
			Log.i("log", "test (ZLAndroidWidget) line414");
		}
	}
	//他人拍攝照片上的註記
	private void drawTakepic_anno_Other(Canvas canvas){
		final SyncDB db = ZLApplication.Instance().syncdb;
		String userName = SaveValue.SyncUserName;
		int UserNotePicNumber = 0;
		try{
			UserNotePicNumber = db.getAnnoCount(SaveValue.picNow, userName);
		}
		catch(Exception e){
			Log.i("log", "error occur when getting other's annotation");
		}
		if(UserNotePicNumber != 0){
			draw_takepic_anno(canvas, UserNotePicNumber, db);
		}
	}
	//---------------------------------------------------------------------------------
	private void drawAnnotationSelf(Canvas canvas) {
		final SQLiteDB db = ZLApplication.Instance().db;
		int NoteNumber = 0;
		int NotePicNumber = 0;
		
		try {
			//文字註記個數( 此頁的註記個數 )
			NoteNumber = db.getTableCount(ActionCode.ANNOTATION_TEXT, SaveValue.UserName, SaveValue.pageIndex);
			//圖片註記個數( 此頁的註記個數 )
			NotePicNumber = db.getTableCount(ActionCode.ANNOTATION_IMAGE, SaveValue.UserName, SaveValue.pageIndex);
			//Log.i("log", "test NoteNumber = "+NotePicNumber);
		} catch(Exception e) {
			Log.i("test", "db error :" +e);
		}
		
		/*20120413之前
		if( SaveValue.IsParent )  //是否家長參與
		{
			//icon_parent_involvement
			drawParentIcon(canvas);
		}
		*/
		
		//畫底色 + 文字註記
		if( NoteNumber != 0 ) //代表此頁有文字註記
		{
			try {
				//畫底色
				contextDraw(canvas, NoteNumber, db);
				//文字註記
				drawAnnotatedText(canvas, NoteNumber, db);
			} catch (Exception e) {
				Log.i("test", "db error in annoText");
			}
		}
		
		//圖片註記
		if( NotePicNumber != 0 ) //代表此頁有圖片註記
		{
			try {
				//圖片註記
				drawAnnoImage(canvas, NotePicNumber, db);
			} catch(Exception e) {
				Log.i("test", "db error in annoImage");
			}
		}
		//drawFooter(canvas);
	}

	//Draw 斜線   Annotated( 文字註記self )
	public void drawAnnotatedText(Canvas canvas, int num, SQLiteDB db) {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		
		//使用紙名稱
		String user = SaveValue.UserName;
		//頁數
		int page = SaveValue.pageIndex;
				
		//mPaintPic
		mPaintPic.setStyle(Paint.Style.FILL);
		mPaintPic.setColor(Color.rgb(177, 162, 251));
		//mPaintText
		mPaintText.setStyle(Paint.Style.FILL);
		mPaintText.setColor(Color.BLACK);
		mPaintText.setTextSize(18);
		//mPaintTextBg
		//mPaintTextBg.setColor(Color.rgb(153, 217, 234));
		mPaintTextBg.setColor(Color.rgb(180, 240, 255));
		mPaintTextBg.setAlpha( 255 );		

		//註記標示
		Bitmap bm = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.ic_list_note));
		//註記標示(Voice)
		Bitmap bm_voice = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.ic_list_voice));
		//註記標示(shutter)
		Bitmap bm_shutter = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.ic_list_shutter));


		//取得此頁中的文字註記資料
		int[] id = db.getRangePage("_id", ActionCode.ANNOTATION_TEXT, page, user);
		
		for( int i=0;i<num;i++ ) {
			if( SaveValue.annoKey.indexOf(id[i]) == -1 ) { //非消失選項
				Bitmap b1 = Bitmap.createBitmap(200, 25, Bitmap.Config.ARGB_4444);
				Canvas cc1 = new Canvas(b1);
				//方塊
				int l = db.getRange(id[i], "left", ActionCode.ANNOTATION_TEXT_RANGE, 0);
				int t = db.getRange(id[i], "top", ActionCode.ANNOTATION_TEXT_RANGE, 0);
				int b = db.getRange(id[i], "bottom", ActionCode.ANNOTATION_TEXT_RANGE, 0);
				//comment
				String str_text = db.getStrComment(id[i], "comment", ActionCode.ANNOTATION_TEXT, user);
				//voice
				String str_voice = db.getStrComment(id[i], "rec", ActionCode.ANNOTATION_TEXT, user);
				//shutter
				String str_shutter = db.getStrComment(id[i], "pic", ActionCode.ANNOTATION_TEXT, user);
				int left = 0;
				int top = 0;
				int right = 20;
				int bottom = 18;
				
				//註記標示
				if( (str_text != null && !str_text.equals("")) || str_voice != null || str_shutter != null ) {
					cc1.drawRect(left, top, right, bottom, mPaintPic);  //劃出註記標示背景
					cc1.drawBitmap(bm, left-2, top, mPaintPic);  //劃出註記
					
					left = right;
					right = left + 20;
				}
				
				//rec
				if( str_voice != null ) {
					cc1.drawRect(left, top, right, bottom, mPaintPic);  //劃出註記標示背景
					cc1.drawBitmap(bm_voice, left, top, mPaintPic);  //劃出註記
					
					left = right;
					right = left + 20;
				}
				
				//pic
				if( str_shutter != null ) {
					cc1.drawRect(left, top, right, bottom, mPaintPic);  //劃出註記標示背景
					cc1.drawBitmap(bm_shutter, left, top, mPaintPic);  //劃出註記
					
					left = right;
					right = left + 20;
				}
				
				//text
				if( str_text != null && !str_text.equals("") ) {
					//text
					//測量文字寬度
					float pwidths = mPaintText.measureText(changeStr(str_text, 10), 0, changeStr(str_text, 10).length());
					cc1.drawRect(left, top, left+pwidths, bottom, mPaintTextBg);
					cc1.drawText(changeStr(str_text, 10), left, bottom, mPaintText);
				}
				
				//偵測下方是否有文字
				left = l;
				top = b+2;
				
				if(view.checkRegion(left, top) || view.checkRegion(left, top+20) || view.checkRegion(left+2, top+10) || view.checkRegion(left+22, top+10)
						|| view.checkRegion(left+32, top+10) ) //上方有文字?
				{
					top = t-2;
					if( view.checkRegion(left, top) || view.checkRegion(left, top-20) || view.checkRegion(left+12, top-10) || view.checkRegion(left-22, top-10)
							|| view.checkRegion(left-32, top-10) ) { //下方有文字?
						//UIUtil.showMessageText(getContext(), "view = true");
						canvas.drawBitmap(b1, l, b+2, null);
					} else {
						top = b+2;
						//畫在上方
						canvas.drawBitmap(b1, l, t-20, null);
					}
					
				}
				else
					canvas.drawBitmap(b1, l, b+2, null);
				
				
				b1.recycle();
			}
		}
		bm.recycle();
		bm_voice.recycle();
		
		/*第一版本
		Bitmap b1 = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_4444);//1280 743
		//Bitmap b1 = myBitmapManager.getBitmap(ZLView.PageIndex.current);
		Canvas cc1 = new Canvas(b1);

		//取得此頁中的文字註記資料
		int[] id = db.getRangePage("draw_id", "annotext", page, user);
		
		
		for( int i=0;i<num;i++ ) {
			//方塊
			int l = db.getRange(id[i], "left", "drawRange", 0);
			int t = db.getRange(id[i], "top", "drawRange", 0);
			int b = db.getRange(id[i], "bottom", "drawRange", 0);
			//comment
			String str_text = db.getStrComment(id[i], "comment", "annotext", user);
			//voice
			String str_voice = db.getStrComment(id[i], "rec", "annotext", user);
			
			int left = l;
			int top = b+2;
			int right = left + 20;
			int bottom = top + 18;
			
			//註記標示
			if( (str_text != null && !str_text.equals("")) || str_voice != null ) {
				if(view.checkRegion(left, top) || view.checkRegion(left, top+20) || view.checkRegion(left+2, top+10) || view.checkRegion(left+22, top+10)
						|| view.checkRegion(left+32, top+10) )
				{
					//如果是true 則下方有文字
					bottom = t-2;
					top = bottom - 18;
				}
				
				cc1.drawRect(left, top, right, bottom, mPaintPic);
				cc1.drawBitmap(bm, left-2, top, mPaintPic);
				
				left = right;
				right = left + 20;
			}
			
			//rec
			if( str_voice != null ) {
				cc1.drawRect(left, top, right, bottom, mPaintPic);
				cc1.drawBitmap(bm_voice, left, top, mPaintPic);
				
				left = right;
				right = left + 20;
			}
			
			//text
			if( str_text != null && !str_text.equals("") ) {
				//text
				//測量文字寬度
				float pwidths = mPaintText.measureText(changeStr(str_text, 10), 0, changeStr(str_text, 10).length());
				cc1.drawRect(left, top, left+pwidths, bottom, mPaintTextBg);
				cc1.drawText(changeStr(str_text, 10), left, bottom, mPaintText);
			}
		}
		
		canvas.drawBitmap(b1, 0, 0, null);
		
		bm.recycle();
		bm_voice.recycle();
		b1.recycle();
		*/
	}	
	//顯示使用者在自己拍的特定照片上的註記內容
	public void draw_takepic_anno_self(Canvas canvas, int num, SQLiteDB db, String lesson){
		//使用者名稱
		String user = SaveValue.UserName;
		//mPaintPic
		mPaintPic.setStyle(Paint.Style.FILL);
		mPaintPic.setColor(Color.rgb(120, 190, 255));
		//mPaintText
		mPaintText.setStyle(Paint.Style.FILL);
		mPaintText.setColor(Color.BLACK);
		mPaintText.setTextSize(18);
		//mPaintTextBg
		//mPaintTextBg.setColor(Color.rgb(153, 217, 234));
		mPaintTextBg.setColor(Color.rgb(180, 240, 255));
		mPaintTextBg.setAlpha( 180 );
		
		//註記標示
		Bitmap bm = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.ic_list_note));
		//註記標示(Voice)
		Bitmap bm_voice = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.ic_list_voice));
		//註記標示(shutter)
		Bitmap bm_shutter = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.ic_list_shutter));				
		
		//取得特定圖片上的註記資料
		int[] id = db.YangetAllAnno("_id", "takepic_anno", user, lesson);
		
		//System.out.println("id[1]= " + id[1]);
		for( int i=0;i<num;i++ ) {
			Bitmap b1 = Bitmap.createBitmap(200, 25, Bitmap.Config.ARGB_4444);
			Canvas cc1 = new Canvas(b1);
			//方塊
			int l = db.YangetTextAnnoPosX("takepic_anno", SaveValue.UserName, lesson)[i];
			int t = db.YangetTextAnnoPosY("takepic_anno", SaveValue.UserName, lesson)[i];
			//comment
			//String str_text = db.YangetTextAnnoData("takepic_anno", SaveValue.UserName)[i];
			//comment
			String str_text = db.getStrComment(id[i], "comment", "takepic_anno", user);
			//voice
			String str_voice = db.getStrComment(id[i], "rec", "takepic_anno", user);
			//shutter
			String str_shutter = db.getStrComment(id[i], "pic", "takepic_anno", user);
			int left = 0;
			int top = 0;
			int right = 20;
			int bottom = 18;
			
//			//畫標記
//			cc1.drawRect(left, top, right, bottom, mPaintPic);
//			cc1.drawBitmap(bm, left-2, top, mPaintPic);
//			
//			left = right;
//			right = left + 20;
			
			//註記標示(註記內容前面的框框)
			if( (str_text != null && !str_text.equals("") || str_shutter != null || str_voice != null)) {
				cc1.drawRect(left, top, right, bottom, mPaintPic);  //劃出註記標示背景
				cc1.drawBitmap(bm, left-2, top, mPaintPic);  //劃出註記
				
				left = right;
				right = left + 20;
			}
			//rec
			if( str_voice != null ) {
				cc1.drawRect(left, top, right, bottom, mPaintPic);
				cc1.drawBitmap(bm_voice, left, top, mPaintPic);
				
				left = right;
				right = left + 20;
			}
			
			//pic
			if( str_shutter != null ) {
				cc1.drawRect(left, top, right, bottom, mPaintPic);  //劃出註記標示背景
				cc1.drawBitmap(bm_shutter, left, top, mPaintPic);  //劃出註記
				
				left = right;
				right = left + 20;
			}
			//text(實際註記內容-->文字)
			if( str_text != null && !"".equals(str_text) ) {
				//測量文字寬度
				float pwidths = mPaintText.measureText(changeStr(str_text, 5), 0, changeStr(str_text, 5).length());
				cc1.drawRect(left, top, left+pwidths, bottom, mPaintTextBg);		
				cc1.drawText(changeStr(str_text, 5), left, bottom, mPaintText);
			}
			
			canvas.drawBitmap(b1, l, t, null);
			b1.recycle();
			
		}
		
		bm.recycle();
		bm_voice.recycle();
	}
	//Draw 斜線   Annotated Image ( 圖片註記self )
	public void drawAnnoImage(Canvas canvas, int num, SQLiteDB db) {
		
		//使用紙名稱
		String user = SaveValue.UserName;
		//頁數
		int page = SaveValue.pageIndex;		
		
		//mPaintPic
		mPaintPic.setStyle(Paint.Style.FILL);
		mPaintPic.setColor(Color.rgb(120, 190, 255));
		//mPaintText
		mPaintText.setStyle(Paint.Style.FILL);
		mPaintText.setColor(Color.BLACK);
		mPaintText.setTextSize(18);
		//mPaintTextBg
		//mPaintTextBg.setColor(Color.rgb(153, 217, 234));
		mPaintTextBg.setColor(Color.rgb(180, 240, 255));
		mPaintTextBg.setAlpha( 180 );
		
		//註記標示
		Bitmap bm = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.ic_list_note));
		//註記標示(Voice)
		Bitmap bm_voice = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.ic_list_voice));
		//註記標示(shutter)
		Bitmap bm_shutter = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.ic_list_shutter));				
		
		//取得此頁中的圖片註記資料
		int[] id = db.getRangePage("_id", ActionCode.ANNOTATION_IMAGE, page, user);
		
		for( int i=0;i<num;i++ ) {
			Bitmap b1 = Bitmap.createBitmap(200, 25, Bitmap.Config.ARGB_4444);
			Canvas cc1 = new Canvas(b1);
			//方塊
			int l = db.getIntData(id[i], "sX", ActionCode.ANNOTATION_IMAGE, user); //X
			int t = db.getIntData(id[i], "sY", ActionCode.ANNOTATION_IMAGE, user); //Y
			//comment
			String str_text = db.getStrComment(id[i], "comment", ActionCode.ANNOTATION_IMAGE, user);
			//voice
			String str_voice = db.getStrComment(id[i], "rec", ActionCode.ANNOTATION_IMAGE, user);
			//shutter
			String str_shutter = db.getStrComment(id[i], "pic", ActionCode.ANNOTATION_IMAGE, user);
			
			int left = 0;
			int top = 0;
			int right = 20;
			int bottom = 18;
			
//			//畫標記
//			cc1.drawRect(left, top, right, bottom, mPaintPic);
//			cc1.drawBitmap(bm, left-2, top, mPaintPic);
//			
//			left = right;
//			right = left + 20;
			
			//註記標示(註記內容前面的框框)
			if( (str_text != null && !str_text.equals("")) || str_voice != null || str_shutter != null ) {
				cc1.drawRect(left, top, right, bottom, mPaintPic);  //劃出註記標示背景
				cc1.drawBitmap(bm, left-2, top, mPaintPic);  //劃出註記
				
				left = right;
				right = left + 20;
			}
			
			//rec
			if( str_voice != null ) {
				cc1.drawRect(left, top, right, bottom, mPaintPic);
				cc1.drawBitmap(bm_voice, left, top, mPaintPic);
				
				left = right;
				right = left + 20;
			}
			
			//pic
			if( str_shutter != null ) {
				cc1.drawRect(left, top, right, bottom, mPaintPic);  //劃出註記標示背景
				cc1.drawBitmap(bm_shutter, left, top, mPaintPic);  //劃出註記
				
				left = right;
				right = left + 20;
			}
			
			//text(實際註記內容-->文字)
			if( str_text != null && !"".equals(str_text) ) {
				//測量文字寬度
				float pwidths = mPaintText.measureText(changeStr(str_text, 5), 0, changeStr(str_text, 5).length());
				cc1.drawRect(left, top, left+pwidths, bottom, mPaintTextBg);		
				cc1.drawText(changeStr(str_text, 5), left, bottom, mPaintText);
			}
			
			canvas.drawBitmap(b1, l, t, null);
			b1.recycle();
		}
		
		bm.recycle();
		bm_voice.recycle();
		
		
		/*第一版本
		Bitmap b1 = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_4444);//1280 743
		//Bitmap b1 = myBitmapManager.getBitmap(ZLView.PageIndex.current);
		Canvas cc1 = new Canvas(b1);

		//取得此頁中的圖片註記資料
		int[] id = db.getRangePage("draw_id", "annoimage", page, user);
		
		for( int i=0;i<num;i++ ) {
			//方塊
			int left = db.getIntData(id[i], "sX", "annoimage", user); //X
			int top = db.getIntData(id[i], "sY", "annoimage", user); //Y
			//comment
			String str_text = db.getStrComment(id[i], "comment", "annoimage", user);
			//voice
			String str_voice = db.getStrComment(id[i], "mrec", "annoimage", user);
			
			int right = left + 20;
			int bottom = top + 18;
			
			//畫標記
			cc1.drawRect(left, top, right, bottom, mPaintPic);
			cc1.drawBitmap(bm, left-2, top, mPaintPic);
			
			left = right;
			right = left + 20;
			
			//rec
			if( str_voice != null ) {
				cc1.drawRect(left, top, right, bottom, mPaintPic);
				cc1.drawBitmap(bm_voice, left, top, mPaintPic);
				
				left = right;
				right = left + 20;
			}
			
			//text
			if( str_text != null && !"".equals(str_text) ) {
				//測量文字寬度
				float pwidths = mPaintText.measureText(changeStr(str_text, 5), 0, changeStr(str_text, 5).length());
				cc1.drawRect(left, top, left+pwidths, bottom, mPaintTextBg);		
				cc1.drawText(changeStr(str_text, 5), left, bottom, mPaintText);
			}
		}
		
		canvas.drawBitmap(b1, 0, 0, null);
		
		bm.recycle();
		bm_voice.recycle();
		b1.recycle();
		*/
	}
	
	//Draw Parents Involvement Icon
	public void drawParentIcon(Canvas canvas) {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		
		//使用者名稱
		String user = SaveValue.UserName;
		//頁數
		int page = SaveValue.pageIndex;		
		
		//mPaintPic
		mPaintPic.setStyle(Paint.Style.FILL);
		mPaintPic.setColor(Color.rgb(120, 190, 255));
		//mPaintText
		mPaintText.setStyle(Paint.Style.FILL);
		mPaintText.setColor(Color.BLACK);
		mPaintText.setTextSize(18);
		//mPaintTextBg
		//mPaintTextBg.setColor(Color.rgb(153, 217, 234));
		mPaintTextBg.setColor(Color.rgb(180, 240, 255));
		mPaintTextBg.setAlpha( 180 );
		
		Bitmap bm = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.icon_parent_involvement));
		
		Bitmap b1 = Bitmap.createBitmap(getWidth(), getWidth(), Bitmap.Config.ARGB_4444);//1280 743
		//Bitmap b1 = myBitmapManager.getBitmap(ZLView.PageIndex.current);
		Canvas cc1 = new Canvas(b1);
		cc1.drawBitmap(bm, 10, 10, mPaintPic);
		
		canvas.drawBitmap(b1, 0, 0, null);
		
		bm.recycle();
		b1.recycle();
	}	
	
	//Draw 斜線   Annotated Image ( 文字註記OtherSelf )
	public void drawAnnotatedText(Canvas canvas, int num, SyncDB db) {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		
		//使用紙名稱
		String user = SaveValue.SyncUserName;
		//頁數
		int page = SaveValue.pageIndex;
				
		//mPaintPic
		mPaintPic.setStyle(Paint.Style.FILL);
		mPaintPic.setColor(Color.rgb(177, 162, 251));
		//mPaintText
		mPaintText.setStyle(Paint.Style.FILL);
		mPaintText.setColor(Color.BLACK);
		mPaintText.setTextSize(18);
		//mPaintTextBg
		//mPaintTextBg.setColor(Color.rgb(153, 217, 234));
		mPaintTextBg.setColor(Color.rgb(180, 240, 255));
		mPaintTextBg.setAlpha( 255 );		

		//註記標示
		Bitmap bm = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.ic_list_note));
		//註記標示(Voice)
		Bitmap bm_voice = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.ic_list_voice));
		//註記標示(shutter)
		Bitmap bm_shutter = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.ic_list_shutter));

		//取得此頁中的文字註記資料
		int[] id = db.getRangePage("id", ActionCode.ANNOTATION_TEXT, page, user);
		
		for( int i=0;i<num;i++ ) {
			if( SaveValue.annoKeySync.indexOf(id[i]) == -1 ) { //非消失選項
				Bitmap b1 = Bitmap.createBitmap(200, 25, Bitmap.Config.ARGB_4444);
				Canvas cc1 = new Canvas(b1);
				//方塊
				Log.i("test", "test this is drawAnnotatedText");
				int l = db.getRange(id[i], "left", ActionCode.ANNOTATION_TEXT_RANGE, 0, user);
				int t = db.getRange(id[i], "top", ActionCode.ANNOTATION_TEXT_RANGE, 0, user);
				int b = db.getRange(id[i], "bottom", ActionCode.ANNOTATION_TEXT_RANGE, 0, user);
				//comment
				String str_text = db.getStrComment_text(id[i], "comment", ActionCode.ANNOTATION_TEXT, user);
				//voice
				String str_voice = db.getStrComment_text(id[i], "rec", ActionCode.ANNOTATION_TEXT, user);
				//shutter
				String str_shutter = db.getStrComment_text(id[i], "pic", ActionCode.ANNOTATION_TEXT, user);
				
				int left = 0;
				int top = 0;
				int right = 20;
				int bottom = 18;
				
				//註記標示
				if( (str_text != null && !str_text.equals("")) || str_voice != null || str_shutter != null  ) {
					cc1.drawRect(left, top, right, bottom, mPaintPic);  //劃出註記標示背景
					cc1.drawBitmap(bm, left-2, top, mPaintPic);  //劃出註記
					
					left = right;
					right = left + 20;
				}
				
				//rec
				if( str_voice != null ) {
					cc1.drawRect(left, top, right, bottom, mPaintPic);  //劃出註記標示背景
					cc1.drawBitmap(bm_voice, left, top, mPaintPic);  //劃出註記
					
					left = right;
					right = left + 20;
				}
				
				//pic
				if( str_shutter != null ) {
					cc1.drawRect(left, top, right, bottom, mPaintPic);  //劃出註記標示背景
					cc1.drawBitmap(bm_shutter, left, top, mPaintPic);  //劃出註記
					
					left = right;
					right = left + 20;
				}
				
				//text
				if( str_text != null && !str_text.equals("") ) {
					//text
					//測量文字寬度
					float pwidths = mPaintText.measureText(changeStr(str_text, 10), 0, changeStr(str_text, 10).length());
					cc1.drawRect(left, top, left+pwidths, bottom, mPaintTextBg);
					cc1.drawText(changeStr(str_text, 10), left, bottom, mPaintText);
				}

				//偵測下方是否有文字
				left = l;
				top = b+2;
				
				if(view.checkRegion(left, top) || view.checkRegion(left, top+20) || view.checkRegion(left+2, top+10) || view.checkRegion(left+22, top+10)
						|| view.checkRegion(left+32, top+10) ) //上方有文字?
				{
					top = t-2;
					if( view.checkRegion(left, top) || view.checkRegion(left, top-20) || view.checkRegion(left+12, top-10) || view.checkRegion(left-22, top-10)
							|| view.checkRegion(left-32, top-10) ) { //下方有文字?
						//UIUtil.showMessageText(getContext(), "view = true");
						canvas.drawBitmap(b1, l, b+2, null);
					} else {
						top = b+2;
						//畫在上方
						canvas.drawBitmap(b1, l, t-20, null);
					}
					
				}
				else
					canvas.drawBitmap(b1, l, b+2, null);
				
				b1.recycle();
			}
		}
		bm.recycle();
		bm_voice.recycle();
	}
	
	//Draw 斜線   Annotated Image ( 圖片註記OtherSelf )
	public void drawAnnoImage(Canvas canvas, int num, SyncDB db) {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		
		//使用紙名稱
		String user = SaveValue.SyncUserName;
		//頁數
		int page = SaveValue.pageIndex;		
		
		//mPaintPic
		mPaintPic.setStyle(Paint.Style.FILL);
		mPaintPic.setColor(Color.rgb(120, 190, 255));
		//mPaintText
		mPaintText.setStyle(Paint.Style.FILL);
		mPaintText.setColor(Color.BLACK);
		mPaintText.setTextSize(18);
		//mPaintTextBg
		//mPaintTextBg.setColor(Color.rgb(153, 217, 234));
		mPaintTextBg.setColor(Color.rgb(180, 240, 255));
		mPaintTextBg.setAlpha( 180 );
		
		//註記標示
		Bitmap bm = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.ic_list_note));
		//註記標示(Voice)
		Bitmap bm_voice = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.ic_list_voice));
		//註記標示(shutter)
		Bitmap bm_shutter = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.ic_list_shutter));		
		
		//取得此頁中的圖片註記資料
		int[] id = db.getRangePage("_id", ActionCode.ANNOTATION_IMAGE, page, user);
		
		for( int i=0;i<num;i++ ) {
			Bitmap b1 = Bitmap.createBitmap(200, 25, Bitmap.Config.ARGB_4444);
			Canvas cc1 = new Canvas(b1);
			//方塊
			int l = db.getIntData(id[i], "sX", ActionCode.ANNOTATION_IMAGE, user); //X
			int t = db.getIntData(id[i], "sY", ActionCode.ANNOTATION_IMAGE, user); //Y
			//comment
			String str_text = db.getStrComment(id[i], "comment", ActionCode.ANNOTATION_IMAGE, user);
			//voice
			String str_voice = db.getStrComment(id[i], "rec", ActionCode.ANNOTATION_IMAGE, user);
			//shutter
			String str_shutter = db.getStrComment_text(id[i], "pic", ActionCode.ANNOTATION_IMAGE, user);
			
			int left = 0;
			int top = 0;
			int right = 20;
			int bottom = 18;
			
//			//畫標記
//			cc1.drawRect(left, top, right, bottom, mPaintPic);
//			cc1.drawBitmap(bm, left-2, top, mPaintPic);
//			
//			left = right;
//			right = left + 20;
			
			//註記標示
			if( (str_text != null && !str_text.equals("")) || str_voice != null || str_shutter != null ) {
				cc1.drawRect(left, top, right, bottom, mPaintPic);  //劃出註記標示背景
				cc1.drawBitmap(bm, left-2, top, mPaintPic);  //劃出註記
				
				left = right;
				right = left + 20;
			}
			
			//rec
			if( str_voice != null && !str_voice.equals("")) {
				cc1.drawRect(left, top, right, bottom, mPaintPic);
				cc1.drawBitmap(bm_voice, left, top, mPaintPic);
				
				left = right;
				right = left + 20;
			}
			//pic
			if( str_shutter != null && !str_shutter.equals("")) {
				cc1.drawRect(left, top, right, bottom, mPaintPic);  //劃出註記標示背景
				cc1.drawBitmap(bm_shutter, left, top, mPaintPic);  //劃出註記
				
				left = right;
				right = left + 20;
			}
			
			//text
			if( str_text != null && !"".equals(str_text) ) {
				//測量文字寬度
				float pwidths = mPaintText.measureText(changeStr(str_text, 5), 0, changeStr(str_text, 5).length());
				cc1.drawRect(left, top, left+pwidths, bottom, mPaintTextBg);		
				cc1.drawText(changeStr(str_text, 5), left, bottom, mPaintText);
			}
			
			canvas.drawBitmap(b1, l, t, null);
			b1.recycle();
		}
		
		bm.recycle();
		bm_voice.recycle();
	}
	//Draw 斜線  Takepic_anno ( 圖片註記OtherSelf )
		public void draw_takepic_anno(Canvas canvas, int num, SyncDB db) {
			final ZLView view = ZLApplication.Instance().getCurrentView();
			//使用者名稱
			String user = SaveValue.SyncUserName;
			//mPaintPic
			mPaintPic.setStyle(Paint.Style.FILL);
			mPaintPic.setColor(Color.rgb(120, 190, 255));
			//mPaintText
			mPaintText.setStyle(Paint.Style.FILL);
			mPaintText.setColor(Color.BLACK);
			mPaintText.setTextSize(18);
			//mPaintTextBg
			//mPaintTextBg.setColor(Color.rgb(153, 217, 234));
			mPaintTextBg.setColor(Color.rgb(180, 240, 255));
			mPaintTextBg.setAlpha( 180 );
			
			//註記標示
			Bitmap bm = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.ic_list_note));
			//註記標示(Voice)
			Bitmap bm_voice = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.ic_list_voice));
			//註記標示(shutter)
			Bitmap bm_shutter = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.ic_list_shutter));		
			
			//取得此頁中的圖片註記資料
			int[] id = db.getTakepic_Anno("_id", "takepic_anno", user, SaveValue.nowLesson);
			
			for( int i=0;i<num;i++ ) {
				Bitmap b1 = Bitmap.createBitmap(200, 25, Bitmap.Config.ARGB_4444);
				Canvas cc1 = new Canvas(b1);
				//方塊
				int l = db.getTakepic_anno_Pos("sX", "takepic_anno", user, id[i]); //X
				int t = db.getTakepic_anno_Pos("sY", "takepic_anno", user, id[i]); //Y
				//comment
				String str_text = db.getStrComment(id[i], "comment", "takepic_anno", user);
				//voice
				String str_voice = db.getStrComment(id[i], "rec", "takepic_anno", user);
				//shutter
				String str_shutter = db.getStrComment_text(id[i], "pic", "takepic_anno", user);
				
				int left = 0;
				int top = 0;
				int right = 20;
				int bottom = 18;
				
//				//畫標記
//				cc1.drawRect(left, top, right, bottom, mPaintPic);
//				cc1.drawBitmap(bm, left-2, top, mPaintPic);
//				
//				left = right;
//				right = left + 20;
				
				//註記標示
				//三種註記皆不為空，才畫
				if( (str_text != null && !str_text.equals("")) || str_voice != null || str_shutter != null ) {
					cc1.drawRect(left, top, right, bottom, mPaintPic);  //劃出註記標示背景
					cc1.drawBitmap(bm, left-2, top, mPaintPic);  //劃出註記
					
					left = right;
					right = left + 20;
				}
				
				//rec
				if( str_voice != null && !str_voice.equals("")) {
					cc1.drawRect(left, top, right, bottom, mPaintPic);
					cc1.drawBitmap(bm_voice, left, top, mPaintPic);
					
					left = right;
					right = left + 20;
				}
				//pic
				if( str_shutter != null && !str_shutter.equals("")) {
					cc1.drawRect(left, top, right, bottom, mPaintPic);  //劃出註記標示背景
					cc1.drawBitmap(bm_shutter, left, top, mPaintPic);  //劃出註記
					
					left = right;
					right = left + 20;
				}
				
				//text
				if( str_text != null && !"".equals(str_text) ) {
					//測量文字寬度
					float pwidths = mPaintText.measureText(changeStr(str_text, 5), 0, changeStr(str_text, 5).length());
					cc1.drawRect(left, top, left+pwidths, bottom, mPaintTextBg);		
					cc1.drawText(changeStr(str_text, 5), left, bottom, mPaintText);
				}
				
				canvas.drawBitmap(b1, l, t, null);
				b1.recycle();
			}
			
			bm.recycle();
			bm_voice.recycle();
		}

	//hightLight self
	public  void contextDraw(Canvas canvas, int num, SQLiteDB db) {
		String user = SaveValue.UserName;
		int page = SaveValue.pageIndex;
		
		//調整透明度
		Paint myFillPaint = new Paint();
		myFillPaint.setAlpha(70);
		
		int[] id = db.getRangePage("_id", ActionCode.ANNOTATION_TEXT, page, user);
		
		for( int i=0;i<num;i++ ) {
			int lineNumber = db.getNoteRange(id[i], "left", ActionCode.ANNOTATION_TEXT_RANGE, user);
			for( int j=0;j<lineNumber;j++ ) {
				int x0 = db.getRange(id[i], "left", ActionCode.ANNOTATION_TEXT_RANGE, j);
				int y0 = db.getRange(id[i], "top", ActionCode.ANNOTATION_TEXT_RANGE, j);
				int x1 = db.getRange(id[i], "right", ActionCode.ANNOTATION_TEXT_RANGE, j);
				int y1 = db.getRange(id[i], "bottom", ActionCode.ANNOTATION_TEXT_RANGE, j);
				int temp;
				
				if (x1 < x0) {
					temp = x1;
					x1 = x0;
					x0 = temp;
				}
				if (y1 < y0) {
					temp = y1;
					y1 = y0;
					y0 = temp;
				}
				
				int color_r = db.getIntData(id[i], "red", ActionCode.ANNOTATION_TEXT, user);
				int color_g = db.getIntData(id[i], "green", ActionCode.ANNOTATION_TEXT, user);
				int color_b = db.getIntData(id[i], "blue", ActionCode.ANNOTATION_TEXT, user);
				
				myFillPaint.setColor(ZLAndroidColorUtil.rgb(new ZLColor(color_r, color_g, color_b)));
				myFillPaint.setAlpha(70);
				
				//style
				switch( db.getIntData(id[i], "type", ActionCode.ANNOTATION_TEXT, user) ) {
				case 0:  //底色
					canvas.drawRect(x0, y0, x1, y1, myFillPaint);
					break;
				case 1:  //底線
					myFillPaint.setAlpha(255);
					canvas.drawRect(x0, y1-3, x1, y1, myFillPaint);
					break;
				case 2:  //刪除線
					myFillPaint.setAlpha(128);
					canvas.drawRect(x0, y0+33, x1, y1-12, myFillPaint);
					break;
				}
			}
		}
		
	}
	
	//hightLight Otherself
	public  void contextDraw(Canvas canvas, int num, SyncDB db) {
		String user = SaveValue.SyncUserName;
		int page = SaveValue.pageIndex;
		
		//調整透明度
		Paint myFillPaint = new Paint();
		myFillPaint.setAlpha(70);
		
		int[] id = db.getRangePage("id", ActionCode.ANNOTATION_TEXT, page, user);  //這頁面有多少ID
		
		for( int i=0;i<num;i++ ) {
			int lineNumber = db.getNoteRange(id[i], "left", ActionCode.ANNOTATION_TEXT_RANGE, user); //取得ID數量
			Log.i("test", "test this is contextDraw(1062) = "+lineNumber);
			for( int j=0;j<1;j++ ) {
				int x0 = db.getRange(id[i], "left", ActionCode.ANNOTATION_TEXT_RANGE, j, user);
				int y0 = db.getRange(id[i], "top", ActionCode.ANNOTATION_TEXT_RANGE, j, user);
				int x1 = db.getRange(id[i], "right", ActionCode.ANNOTATION_TEXT_RANGE, j, user);
				int y1 = db.getRange(id[i], "bottom", ActionCode.ANNOTATION_TEXT_RANGE, j, user);
				int temp;
				
				if (x1 < x0) {
					temp = x1;
					x1 = x0;
					x0 = temp;
				}
				if (y1 < y0) {
					temp = y1;
					y1 = y0;
					y0 = temp;
				}
				
				int color_r = db.getIntData_text(id[i], "red", ActionCode.ANNOTATION_TEXT, user);
				int color_g = db.getIntData_text(id[i], "green", ActionCode.ANNOTATION_TEXT, user);
				int color_b = db.getIntData_text(id[i], "blue", ActionCode.ANNOTATION_TEXT, user);
				
				myFillPaint.setColor(ZLAndroidColorUtil.rgb(new ZLColor(color_r, color_g, color_b)));
				myFillPaint.setAlpha(70);
				
				//style
				switch( db.getIntData_text(id[i], "type", ActionCode.ANNOTATION_TEXT, user) ) {
				case 0:  //底色
					canvas.drawRect(x0, y0, x1, y1, myFillPaint);
					break;
				case 1:  //底線
					myFillPaint.setAlpha(255);
					canvas.drawRect(x0, y1-3, x1, y1, myFillPaint);
					break;
				case 2:  //刪除線
					myFillPaint.setAlpha(128);
					canvas.drawRect(x0, y0+33, x1, y1-12, myFillPaint);
					break;
				}
			}
		}
		
	}
		
	//防止字串過長
	public String changeStr(String str, int limit) {
		//Log.i("bytes", "test str.getBytes = "+str.getBytes().length);
		if( str.length() > limit )
			str = str.substring(0, limit) + "...";
		return str;
	}
	
	public int changeStrlength(String str, int limit) {
		int temp = str.length()*12;
		if( str.length() > limit ) {
			str = str.substring(0, limit) + "...";
			//temp = str.length();
			temp = str.length()*8;
		}
		return temp;
	}

	/*
	private void drawSyncOther(Canvas canvas) {
		try {
			//mPaintPic
			mPaintPic.setStyle(Paint.Style.FILL);
			mPaintPic.setColor(Color.rgb(177, 162, 251));
			//mPaintText
			mPaintText.setStyle(Paint.Style.FILL);
			mPaintText.setColor(Color.BLACK);
			mPaintText.setTextSize(18);
			//人物標示
			Bitmap bm = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.ic_list_sync_other));
			//bm.setDensity(5);
			Bitmap b1 = Bitmap.createBitmap(100, 1300, Bitmap.Config.ARGB_4444);
			Canvas cc1 = new Canvas(b1);
			String[] Username = {"stu001", "stu002", "stu003", "stu004", "stu005", "stu006", "stu007", "stu008", "stu009", "stu010", "stu011", "stu012"
											, "stu013", "stu014", "stu015", "stu016", "stu017", "stu018", "stu019", "stu020", "stu021", "stu022", "stu023", "stu024"
											, "stu025", "stu026", "stu027", "stu028", "stu029", "stu030"};
			for(int i=0;i<30;i++) {
				cc1.drawBitmap(bm, 5, i*70, mPaintPic);
				cc1.drawText(Username[i], 0, i*70+45, mPaintText);
			}
			canvas.drawBitmap(b1, 10, 10, null);
		}catch(Exception e) {
			Log.i("test", "test error = "+e);
		}
	}
	*/
	
	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			onKeyDown(KeyEvent.KEYCODE_DPAD_CENTER, null);
		} else {
			ZLApplication.Instance().getCurrentView().onTrackballRotated((int)(10 * event.getX()), (int)(10 * event.getY()));
		}
		return true;
	}


	private class LongClickRunnable implements Runnable {
		public void run() {
			if (performLongClick()) {
				myLongClickPerformed = true;
			}
		}
	}
	private volatile LongClickRunnable myPendingLongClickRunnable;
	private volatile boolean myLongClickPerformed;

	private void postLongClickRunnable() {
        myLongClickPerformed = false;
		myPendingPress = false;
        if (myPendingLongClickRunnable == null) {
            myPendingLongClickRunnable = new LongClickRunnable();
        }
        if( SaveValue.IsNote ) //20120416
        	postDelayed(myPendingLongClickRunnable, 2 * ViewConfiguration.getLongPressTimeout() / 3);
    }

	private class ShortClickRunnable implements Runnable {
		public void run() {
			final ZLView view = ZLApplication.Instance().getCurrentView();
			view.onFingerSingleTap(myPressedX, myPressedY);
			myPendingPress = false;
			myPendingShortClickRunnable = null;
		}
	}
	private volatile ShortClickRunnable myPendingShortClickRunnable;

	private volatile boolean myPendingPress;
	private volatile boolean myPendingDoubleTap;
	private int myPressedX, myPressedY;
	private boolean myScreenIsTouched;
	//test
	private boolean IsCheckPicNote = false;  //是否啟動圖片註記
	private boolean IsCheckNote = false;     //是否啟動文字註記	
	private int interval = 200; // ms 連點2次的最大時間間隔
	private long priorTime = 0; //前次點選的時間點
	public  static int x, y;
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		x = (int)event.getX();
		y = (int)event.getY();
		
		final ZLView view = ZLApplication.Instance().getCurrentView();
		switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
				
				MyThread thread = new MyThread(interval, mHandler, "Dialog Open");              
                new Thread(thread).start();
                /*test
				//偵測是否為圖片註記
				if(  checkPicNote(x, y) && IsCheckNote )
				{
					final SQLiteDB db = ZLApplication.Instance().db;
					int l = db.getIntData(SaveValue.picNowIndex, "sX", ActionCode.ANNOTATION_IMAGE, SaveValue.UserName); //X
					SaveValue.Islecture = false;
					if( l == 15 )
						SaveValue.Islecture = true;
					
					view.onClick_pic();
				}//新增圖片註記
				else if(IsCheckPicNote) {
					IsCheckPicNote = false;
					
					//偵測是否在文字區域
					view.onPress(x, y); //20110831
				}
				//test*/
				
				
				if (myPendingDoubleTap) {
					view.onFingerDoubleTap(x, y);
				} if (myLongClickPerformed) {
					view.onFingerReleaseAfterLongPress(x, y);
				} else {
					if (myPendingLongClickRunnable != null) {
						removeCallbacks(myPendingLongClickRunnable);
						myPendingLongClickRunnable = null;
					}
					if (myPendingPress) {
						if (view.isDoubleTapSupported()) {
        					if (myPendingShortClickRunnable == null) {
            					myPendingShortClickRunnable = new ShortClickRunnable();
        					}
        					postDelayed(myPendingShortClickRunnable, ViewConfiguration.getDoubleTapTimeout());
						} else {
							//view.onFingerSingleTap(x, y);
						}
					} else {
						view.onFingerRelease(x, y);
					}
				}
				myPendingDoubleTap = false;
				myPendingPress = false;
				myScreenIsTouched = false;
				break;
			case MotionEvent.ACTION_DOWN:
				Calendar cal = Calendar.getInstance();
				
				if( SaveValue.picNoteOpen )
				{
					IsCheckPicNote = true; //圖片註記功能 是否啟動
				}
				if( SaveValue.NoteOpen )
				{
					IsCheckNote = true; //文字註記功能 是否啟動
				}
				
				if( priorTime == 0 ) {
					priorTime = cal.getTimeInMillis(); //第一次點選
				} else {
					if( (cal.getTimeInMillis() - priorTime) < interval && checkHighlight(x, y) ) { //檢查是否為文字註記
						//連點2次
						//UIUtil.showMessageText(getContext(), "now time = "+(cal.getTimeInMillis() - priorTime));
						//消除註記
						//SaveValue.annoKey = !SaveValue.annoKey;
						
						
						if( SaveValue.IsSyncData ) {
							if( SaveValue.annoKeySync.isEmpty() || !SaveValue.annoKeySync.remove((Object)SaveValue.nowIndex) ) {
								SaveValue.annoKeySync.add(SaveValue.nowIndex);
							}
						} else {
							if( SaveValue.annoKey.isEmpty() || !SaveValue.annoKey.remove((Object)SaveValue.nowIndex) ) {
								SaveValue.annoKey.add(SaveValue.nowIndex);
							}
						}
							
						
						view.clear();
						priorTime = 0;
						IsCheckPicNote = false;
						IsCheckNote = false;
					}else{
						priorTime = cal.getTimeInMillis();
					}
				}
				
				
				if (myPendingShortClickRunnable != null) {
					removeCallbacks(myPendingShortClickRunnable);
					myPendingShortClickRunnable = null;
					myPendingDoubleTap = true;
				} else {
					postLongClickRunnable();
					myPendingPress = true;
				}
				myScreenIsTouched = true;
				myPressedX = x;
				myPressedY = y;
				break;
			case MotionEvent.ACTION_MOVE:
			{
				final int slop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
				//final boolean isAMove =
				//	Math.abs(myPressedX - x) > slop || Math.abs(myPressedY - y) > slop;
				final boolean isAMove =
							Math.abs(myPressedX - x) > slop;
				if (isAMove) {
					//註記關閉
					IsCheckNote = false;
					myPendingDoubleTap = false;
				}
				if (myLongClickPerformed) {
					view.onFingerMoveAfterLongPress(x, y);
				} else {
					if (myPendingPress) {
						if (isAMove) {
							if (myPendingShortClickRunnable != null) {
								removeCallbacks(myPendingShortClickRunnable);
								myPendingShortClickRunnable = null;
							}
							if (myPendingLongClickRunnable != null) {
								removeCallbacks(myPendingLongClickRunnable);
							}
							view.onFingerPress(myPressedX, myPressedY);
							myPendingPress = false;
						}
					}
					if (!myPendingPress) {
						view.onFingerMove(x, y);
					}
				}
				break;
			}
		}

		return true;
	}

	public boolean onLongClick(View v) {
		IsCheckPicNote = false;
		IsCheckNote = false; //翻頁  不啟動文字註記
		final ZLView view = ZLApplication.Instance().getCurrentView();
		//Toast.makeText("LongClick", "test", Toast.LENGTH_SHORT);
		return view.onFingerLongPress(myPressedX, myPressedY);
	}
	
	
	private final int ID_USER = 0;
	/*
	class MyThread implements Runnable {
		public void run() {
			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Message msg = new Message();     
            msg.what = ID_USER;
            Bundle data = new Bundle();
            data.putInt("Dialog Open", 300);
            msg.setData(data);
        
            mHandler.sendMessage(msg);
          }
	 }
	 */
	 private Handler mHandler = new Handler(){  
         public void handleMessage(Message msg) {  
             switch (msg.what) {  
             case ID_USER:  
                   Bundle data = msg.getData();
                   int ms = data.getInt("Dialog Open");
                   if( ms == 300 )
                	   check();
             }  
         };  
     };
	 //在 mhandler 裡面被呼叫 (點開註記內容)
     public void check(){
    	 Log.i("checkAnno", "annotation awakwed");
    	 final ZLView view = ZLApplication.Instance().getCurrentView();
    	 //test
    	 //偵測是否為文字註記
    	 if( checkHighlight(x, y) && IsCheckNote && FBReader.setImage==false)
    	 {
    	 	view.onClick();
    	 }
    	 //checkXY(要控制的軸向, 圖片註記最大範圍, 可註記的最大範圍限制--螢幕的寬度)
    	 //x 130 1200
    	 //y 50 700
    	 /* 取消限定位置的圖片註記
    	 int newx = view.checkXY(x, 130, 1200);
    	 if( newx < 150 )
    	 newx = 150;
    	 int newy = view.checkXY(y, 25, 700);
    	 */
    	 //偵測是否為圖片註記
    	 //if( ( checkPicNote(x, y) || checkPicNote(newx, newy) ) && IsCheckNote )
    	 if( checkPicNote(x, y) && IsCheckNote )
    	 {	
    		 final SQLiteDB db = ZLApplication.Instance().db;
    	 	 int l;
    	 	 if(!FBReader.setImage){
    	 		 l = db.getIntData(SaveValue.picNowIndex, "sY", ActionCode.ANNOTATION_IMAGE, SaveValue.UserName); //Y
    	 	 }
    	 	 else{
    	 		 l = db.getIntData(SaveValue.picNowIndex, "sY", "takepic_anno", SaveValue.UserName); //Y
    	 	 }
    	 	 SaveValue.Islecture = false;
    	 	 if( l == 8 ){
    	 		 SaveValue.Islecture = true;
    	 	 }
    	 	 view.onClick_pic();
    	 }	 //新增圖片註記
    	 else if(IsCheckPicNote) {
    		 IsCheckPicNote = false;
    	 	 //偵測是否在文字區域
    	 	 //view.onPress(newx, newy); //20110831 20120414
    	 	 view.onPress(x, y); // 20120414
    	  }
    	 	//test
 	}
	
     //test 偵測按的位置 是否為 文字註記
     public boolean checkHighlight(int x, int y) {
  		//偵測範圍大小
  		final int range = 15;
  		int page = SaveValue.pageIndex;
  		String user;
  		if( !SaveValue.IsSyncData ) {
  			user = SaveValue.UserName;
  			final SQLiteDB db = ZLApplication.Instance().db;
  			//文字註記個數( 此頁的註記個數 )
  			int NoteNumber = db.getTableCount(ActionCode.ANNOTATION_TEXT, user, page);
  			
  			if( NoteNumber > 0 )
  			{
  				//取得此頁中的文字註記資料
  				int[] id = db.getRangePage("_id", ActionCode.ANNOTATION_TEXT, page, user);
  				
  				for( int i=0;i<NoteNumber;i++ ) {
  					//抓行數( 幾行需要畫 )
  					int drawNumber = db.getNoteRange(id[i], "left", ActionCode.ANNOTATION_TEXT_RANGE, user);
  					for( int j=0;j<drawNumber;j++ ) {
  						//方塊
  						int l = db.getRange(id[i], "left", ActionCode.ANNOTATION_TEXT_RANGE, j);
  						int r = db.getRange(id[i], "right", ActionCode.ANNOTATION_TEXT_RANGE, j);
  						int t = db.getRange(id[i], "top", ActionCode.ANNOTATION_TEXT_RANGE, j);
  						int b = db.getRange(id[i], "bottom", ActionCode.ANNOTATION_TEXT_RANGE, j);
  						
  						if( x+range >= l && x-range <= r && y+range >= t && y-range <= b )  //是否在選取範圍
  						{
  							SaveValue.nowIndex = id[i];  //db的 draw_id
  							//Log.i("test","test id = "+id[i]);
  							return true;
  						}
  					}
  				}
  			}
  		} else {
  			user = SaveValue.SyncUserName;
  			final SyncDB db = ZLApplication.Instance().syncdb;
  			//文字註記個數( 此頁的註記個數 )
  			int NoteNumber = db.getTableCount(ActionCode.ANNOTATION_TEXT, user, page);
  			//Log.i("test", "test drawNumber0 = "+NoteNumber);
  			if( NoteNumber > 0 )
  			{
  				//取得此頁中的文字註記資料
  				int[] id = db.getRangePage("id", ActionCode.ANNOTATION_TEXT, page, user);
  				
  				for( int i=0;i<NoteNumber;i++ ) {
  					//Log.i("log","test (SyncDB) line534 = "+id[i]);
  					//抓行數( 幾行需要畫 )
  					int drawNumber = db.getNoteRange(id[i], "left", ActionCode.ANNOTATION_TEXT_RANGE, user);
  					//Log.i("test", "test drawNumber1 = "+drawNumber);
  					for( int j=0;j<drawNumber;j++ ) {
  						//方塊
  						Log.i("test", "test this is checkHighlight(1492)");
  						int l = db.getRange(id[i], "left", ActionCode.ANNOTATION_TEXT_RANGE, j, user);
  						int r = db.getRange(id[i], "right", ActionCode.ANNOTATION_TEXT_RANGE, j, user);
  						int t = db.getRange(id[i], "top", ActionCode.ANNOTATION_TEXT_RANGE, j, user);
  						int b = db.getRange(id[i], "bottom", ActionCode.ANNOTATION_TEXT_RANGE, j, user);
  						
  						if( x+range >= l && x-range <= r && y+range >= t && y-range <= b )  //是否在選取範圍
  						{
  							SaveValue.nowIndex = id[i];  //db的 draw_id
  							//Log.i("test","test id = "+id[i]);
  							return true;
  						}
  					}
  				}
  			}
  		}
  		
  		return false;
  	}
		
     //test 偵測按的位置 是否為 圖片註記
     public boolean checkPicNote(int x, int y) {
 		//轉換XY
 		//final ZLView view = ZLApplication.Instance().getCurrentView();
 		//int tX = view.checkXY(x, 130, 1200);
 		//int tY = view.checkXY(y, 50, 700);
 		
 		//偵測範圍大小
 		final int range = 15;
 		int page = SaveValue.pageIndex;
 		String user;
 		//非同步觀看中
 		if( !SaveValue.IsSyncData ) {
 			user = SaveValue.UserName;
 			final SQLiteDB db = ZLApplication.Instance().db;
 			//圖片註記個數( 此頁的註記個數 )
 			int NotePicNumber;
 			if(!FBReader.setImage){
 				NotePicNumber = db.getTableCount(ActionCode.ANNOTATION_IMAGE, user, page);
 			}
 			else{
 				NotePicNumber = db.getTakepic_anno_Num_self("takepic_anno", SaveValue.UserName, SaveValue.nowLesson);
 			}
 			//非同步，沒進入個人活動
 			if( NotePicNumber > 0  && !FBReader.setImage)
 			{
 				Log.i("message","非個人活動區標註案下");
 				//取得此頁中的圖片註記資料
 				int[] id = db.getRangePage("_id", ActionCode.ANNOTATION_IMAGE, page, user);
 				
 				for( int i=0;i<NotePicNumber;i++ ) {
 					//方塊
 					int l = db.getIntData(id[i], "sX", ActionCode.ANNOTATION_IMAGE, user); //X
 					int t = db.getIntData(id[i], "sY", ActionCode.ANNOTATION_IMAGE, user); //Y
 					int r = l + 20;
 					int b = t + 20;
 					
 					//comment
 					String str_text = db.getStrComment(id[i], "comment", ActionCode.ANNOTATION_IMAGE, user);
 					
 					if( str_text != null && !"".equals(str_text) ) {
 						//測量文字寬度
 						//mPaintText
 						mPaintText.setStyle(Paint.Style.FILL);
 						mPaintText.setColor(Color.BLACK);
 						mPaintText.setTextSize(18);
 						
 						float pwidths = mPaintText.measureText(changeStr(str_text, 5), 0, changeStr(str_text, 5).length());
 						r += pwidths;
 						//偵測
 						if( x+range >= l && x-range <= r && y+range >= t && y-range <= b )
 						{
 							SaveValue.picNowIndex = id[i];
 							SaveValue.wordWidth = pwidths;
 							return true;
 						}
 					}
 					else {
 						if( x+range >= l && x-range <= r && y+range >= t && y-range <= b )
 						{
 							SaveValue.picNowIndex = id[i];
 							SaveValue.wordWidth = 0;
 							return true;
 						}
 					}
 					
 					
 				}
 			}
 			//非同步，但進入個人活動
 			else if( NotePicNumber >0 && FBReader.setImage){ ////
 				int[] id = db.YangetAllAnno("_id", "takepic_anno", user, SaveValue.nowLesson);
 				for( int i=0;i<NotePicNumber;i++ ) {
 					Log.i("message","個人活動區標註案下");
 					//方塊
 					int l = db.YangetTextAnnoPosX("takepic_anno", SaveValue.UserName, SaveValue.nowLesson)[i];
 					int t = db.YangetTextAnnoPosY("takepic_anno", SaveValue.UserName, SaveValue.nowLesson)[i];
 					int r = l + 20;
 					int b = t + 20;
 					
 					//comment
 					String str_text = db.getStrComment(id[i], "comment", "takepic_anno", user);
 					
 					if( str_text != null && !"".equals(str_text) ) {
 						//測量文字寬度
 						//mPaintText
 						mPaintText.setStyle(Paint.Style.FILL);
 						mPaintText.setColor(Color.BLACK);
 						mPaintText.setTextSize(18);
 						
 						float pwidths = mPaintText.measureText(changeStr(str_text, 5), 0, changeStr(str_text, 5).length());
 						r += pwidths;
 						//偵測
 						if( x+range >= l && x-range <= r && y+range >= t && y-range <= b )
 						{
 							SaveValue.picNowIndex = id[i];
 							SaveValue.wordWidth = pwidths;
 							return true;
 						}
 					}
 					else {
 						if( x+range >= l && x-range <= r && y+range >= t && y-range <= b )
 						{
 							SaveValue.picNowIndex = id[i];
 							SaveValue.wordWidth = 0;
 							return true;
 						}
 					}	
 				}
 			}
 		} 
 		//同步觀看中
 		else {
 			user = SaveValue.SyncUserName;
 			final SyncDB db = ZLApplication.Instance().syncdb;
 			//圖片註記個數( 此頁的註記個數 )
 			int NotePicNumber;
 			if(!FBReader.setImage){
 				NotePicNumber = db.getTableCount(ActionCode.ANNOTATION_IMAGE, user, page);
 			}
 			else{
 				NotePicNumber = db.getTakepic_anno_Num_other("takepic_anno", user, SaveValue.nowLesson);
 			}
 			//同步中，沒進入個人活動
 			if( NotePicNumber > 0 && !FBReader.setImage)
 			{
 				//取得此頁中的圖片註記資料
 				int[] id = db.getRangePage("_id", ActionCode.ANNOTATION_IMAGE, page, user);
 				for( int i=0;i<NotePicNumber;i++ ) {
 					//方塊
 					int l = db.getIntData(id[i], "sX", ActionCode.ANNOTATION_IMAGE, user); //X
 					int t = db.getIntData(id[i], "sY", ActionCode.ANNOTATION_IMAGE, user); //Y
 					int r = l + 20;
 					int b = t + 20;
 					
 					//comment
 					String str_text = db.getStrComment(id[i], "comment", ActionCode.ANNOTATION_IMAGE, user);
 					
 					if( str_text != null && !"".equals(str_text) ) {
 						//測量文字寬度
 						//mPaintText
 						mPaintText.setStyle(Paint.Style.FILL);
 						mPaintText.setColor(Color.BLACK);
 						mPaintText.setTextSize(18);
 						
 						float pwidths = mPaintText.measureText(changeStr(str_text, 5), 0, changeStr(str_text, 5).length());
 						r += pwidths;
 						//偵測
 						if( x+range >= l && x-range <= r && y+range >= t && y-range <= b )
 						{
 							SaveValue.picNowIndex = id[i];
 							SaveValue.wordWidth = pwidths;
 							return true;
 						}
 					}
 					else {
 						if( x+range >= l && x-range <= r && y+range >= t && y-range <= b )
 						{
 							SaveValue.picNowIndex = id[i];
 							SaveValue.wordWidth = 0;
 							return true;
 						}
 					}
 				}
 			}
 			//同步中且進入個人活動
 			else if( NotePicNumber > 0 && FBReader.setImage)
 			{
 				//取得此頁中的圖片註記資料
 				int[] id = db.getTakepic_Anno("_id", "takepic_anno", user, SaveValue.nowLesson);
 				for( int i=0;i<NotePicNumber;i++ ) {
 					//方塊
 					int l = db.getTakepic_anno_Pos("sX", "takepic_anno", user, id[i]);//X
 					int t = db.getTakepic_anno_Pos("sY", "takepic_anno", user, id[i]); //Y
 					int r = l + 20;
 					int b = t + 20;
 					
 					//comment
 					String str_text = db.getStrComment(id[i], "comment", "takepic_anno", user);
 					
 					if( str_text != null && !"".equals(str_text) ) {
 						//測量文字寬度
 						//mPaintText
 						mPaintText.setStyle(Paint.Style.FILL);
 						mPaintText.setColor(Color.BLACK);
 						mPaintText.setTextSize(18);
 						
 						float pwidths = mPaintText.measureText(changeStr(str_text, 5), 0, changeStr(str_text, 5).length());
 						r += pwidths;
 						//偵測
 						if( x+range >= l && x-range <= r && y+range >= t && y-range <= b )
 						{
 							SaveValue.picNowIndex = id[i];
 							SaveValue.wordWidth = pwidths;
 							return true;
 						}
 					}
 					else {
 						if( x+range >= l && x-range <= r && y+range >= t && y-range <= b )
 						{
 							SaveValue.picNowIndex = id[i];
 							SaveValue.wordWidth = 0;
 							return true;
 						}
 					}
 				}
 			}
 		}
 		return false;
 	}

     private String myKeyUnderTracking;
     private long myTrackingStartTime;
     
     public boolean onKeyDown(int keyCode, KeyEvent event) {
 		final ZLApplication application = ZLApplication.Instance();

 		switch (keyCode) {
 			case KeyEvent.KEYCODE_VOLUME_DOWN:
 			case KeyEvent.KEYCODE_VOLUME_UP:
 			case KeyEvent.KEYCODE_BACK:
 			case KeyEvent.KEYCODE_ENTER:
 			case KeyEvent.KEYCODE_DPAD_CENTER:
 			{
 				final String keyName = ZLAndroidKeyUtil.getKeyNameByCode(keyCode);
 				if (myKeyUnderTracking != null) {
 					if (myKeyUnderTracking.equals(keyName)) {
 						return true;
 					} else {
 						myKeyUnderTracking = null;
 					}
 				}
 				if (application.hasActionForKey(keyName, true)) {
 					myKeyUnderTracking = keyName;
 					myTrackingStartTime = System.currentTimeMillis();
 					return true;
 				} else {
 					return application.doActionByKey(keyName, false);
 				}
 			}
 			case KeyEvent.KEYCODE_DPAD_LEFT:
 				application.getCurrentView().onTrackballRotated(-1, 0);
 				return true;
 			case KeyEvent.KEYCODE_DPAD_RIGHT:
 				application.getCurrentView().onTrackballRotated(1, 0);
 				return true;
 			case KeyEvent.KEYCODE_DPAD_DOWN:
 				application.getCurrentView().onTrackballRotated(0, 1);
 				return true;
 			case KeyEvent.KEYCODE_DPAD_UP:
 				application.getCurrentView().onTrackballRotated(0, -1);
 				return true;
 			default:
 				return false;
 		}
 	}

     public boolean onKeyUp(int keyCode, KeyEvent event) {
  		switch (keyCode) {
  			case KeyEvent.KEYCODE_VOLUME_DOWN:
  			case KeyEvent.KEYCODE_VOLUME_UP:
  			case KeyEvent.KEYCODE_BACK:
  			case KeyEvent.KEYCODE_ENTER:
  			case KeyEvent.KEYCODE_DPAD_CENTER:
  				if (myKeyUnderTracking != null) {
  					final String keyName = ZLAndroidKeyUtil.getKeyNameByCode(keyCode);
  					if (myKeyUnderTracking.equals(keyName)) {
  						final boolean longPress = System.currentTimeMillis() >
  							myTrackingStartTime + ViewConfiguration.getLongPressTimeout();
  						ZLApplication.Instance().doActionByKey(keyName, longPress);
  					}
  					myKeyUnderTracking = null;
  				}
  				return true;
  			default:
  				return false;
  		}
  	}

     protected int computeVerticalScrollExtent() {
  		final ZLView view = ZLApplication.Instance().getCurrentView();
  		if (!view.isScrollbarShown()) {
  			return 0;
  		}
  		final AnimationProvider animator = getAnimationProvider();
  		if (animator.inProgress()) {
  			final int from = view.getScrollbarThumbLength(ZLView.PageIndex.current);
  			final int to = view.getScrollbarThumbLength(animator.getPageToScrollTo());
  			final int percent = animator.getScrolledPercent();
  			return (from * (100 - percent) + to * percent) / 100;
  		} else {
  			return view.getScrollbarThumbLength(ZLView.PageIndex.current);
  		}
  	}

     protected int computeVerticalScrollOffset() {
  		final ZLView view = ZLApplication.Instance().getCurrentView();
  		if (!view.isScrollbarShown()) {
  			return 0;
  		}
  		final AnimationProvider animator = getAnimationProvider();
  		if (animator.inProgress()) {
  			final int from = view.getScrollbarThumbPosition(ZLView.PageIndex.current);
  			final int to = view.getScrollbarThumbPosition(animator.getPageToScrollTo());
  			final int percent = animator.getScrolledPercent();
  			return (from * (100 - percent) + to * percent) / 100;
  		} else {
  			return view.getScrollbarThumbPosition(ZLView.PageIndex.current);
  		}
  	}

     protected int computeVerticalScrollRange() {
  		final ZLView view = ZLApplication.Instance().getCurrentView();
  		if (!view.isScrollbarShown()) {
  			return 0;
  		}
  		return view.getScrollbarFullSize();
  	}

     private int getMainAreaHeight() {
  		final ZLView.FooterArea footer = ZLApplication.Instance().getCurrentView().getFooterArea();
  		return footer != null ? getHeight() - footer.getHeight() : getHeight();
  	}
}
