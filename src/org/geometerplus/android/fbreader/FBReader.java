/*
 * Copyright (C) 2009-2011 Geometer Plus <contact@geometerplus.com>
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

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.geometerplus.android.fbreader.api.PluginApi;
import org.geometerplus.android.fbreader.library.KillerCallback;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.dialogs.DialogMoveTab;
import org.geometerplus.zlibrary.core.dialogs.DialogMoveTabPic;
import org.geometerplus.zlibrary.core.dialogs.myCameraActivity;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.sqliteconfig.ConnectMysql;
import org.geometerplus.zlibrary.core.sqliteconfig.SQLiteDB;
import org.geometerplus.zlibrary.core.sqliteconfig.SyncDB;
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.text.hyphenation.ZLTextHyphenator;
import org.geometerplus.zlibrary.text.view.SaveValue;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidActivity;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.thirdEdition.geometerplus.zlibrary.ui.android.R;

public final class FBReader extends ZLAndroidActivity {
	private ArrayAdapter<String> myadapter;
	private ArrayAdapter<String> mySubtocadapter;
	private ArrayList<String> items = new ArrayList(Arrays.asList("拍攝新照片"));
	private String[] list = {"Lesson6","Lesson7","Lesson8","返回教材內容"};
	public static boolean setImage = false;
	//test
	public SQLiteDB db = new SQLiteDB(this);
	public SyncDB syncdb = new SyncDB(this);
	public ConnectMysql mysql = new ConnectMysql(this);
	public TextToSpeech tts;
	
	public static final String BOOK_PATH_KEY = "BookPath";

	final static int REPAINT_CODE = 1;
	final static int CANCEL_CODE = 2;
	final static int TAKE_PIC_TEXT = 3;	//Text回傳
	final static int TAKE_PIC_IMAGE = 4;	//Image回傳

	private int myFullScreenFlag;
	//private ImageView testview;

	private static final String PLUGIN_ACTION_PREFIX = "___";
	private final List<PluginApi.ActionInfo> myPluginActions =
		new LinkedList<PluginApi.ActionInfo>();
	private final BroadcastReceiver myPluginInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final ArrayList<PluginApi.ActionInfo> actions = getResultExtras(true).<PluginApi.ActionInfo>getParcelableArrayList(PluginApi.PluginInfo.KEY);
			if (actions != null) {
				synchronized (myPluginActions) {
					final FBReaderApp fbReader = (FBReaderApp)FBReaderApp.Instance();
					int index = 0;
					for (PluginApi.ActionInfo info : myPluginActions) {
						fbReader.removeAction(PLUGIN_ACTION_PREFIX + index++);
					}
					myPluginActions.addAll(actions);
					index = 0;
					for (PluginApi.ActionInfo info : myPluginActions) {
						fbReader.addAction(
							PLUGIN_ACTION_PREFIX + index++,
							new RunPluginAction(FBReader.this, fbReader, info.getId())
						);
					}
				}
			}
		}
	};

	@Override //為父類別(ZLAndroidActivity)中定義的抽象方法
	protected ZLFile fileFromIntent(Intent intent) {
		String filePath = intent.getStringExtra(BOOK_PATH_KEY);
		if (filePath == null) {
			final Uri data = intent.getData();
			if (data != null) {
				filePath = data.getPath();
			}
		}
		return filePath != null ? ZLFile.createFileByPath(filePath) : null;
	}

	@Override
	public void onCreate(Bundle icicle) {
		myadapter = new ArrayAdapter(this, R.layout.list_yanlist, list);
		mySubtocadapter = new ArrayAdapter(this, R.layout.list_yanlist, items);
		super.onCreate(icicle);
		Log.w("FBReader", "created");
		SaveValue.syncOpen = false;
		//登入---------------------------------------------------
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras(); 
		
		SaveValue.UserName = bundle.getString("putUsername");
		Toast.makeText(getApplicationContext(),
				"你所登入的帳號是：" + bundle.getString("putUsername"),
				Toast.LENGTH_LONG).show();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date dt1=new Date();
		try {
			Date dt2 = sdf.parse("2013/03/18 14:10:00");
			Long ut1 = dt1.getTime();
			Long ut2 = dt2.getTime();
			if( (ut2 - ut1)<0 )
				SaveValue.syncOpen = true;
			Log.i("test", "test syncOpen = "+SaveValue.syncOpen);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		SaveValue.syncOpen = true;
		SaveValue.test = true;
		//登入---------------------------------------------------
		///同步---------------------------------------------------
		SaveValue.IsUPdate = true;
		synchronized (this) {
			try {
				startService(new Intent(this, MyService.class));
	        } catch (Exception e) {
	        	Log.i("test", "test service = "+e);
	        }
	    }
		//同步---------------------------------------------------
		//錄影---------------------------------------------------
		//MyThread thread = new MyThread(1500, handler, "OpenRecordVideo");
		//new Thread(thread).start();
		//錄影---------------------------------------------------
		
		//清空隱藏註記
		if( !SaveValue.annoKey.isEmpty() )
			SaveValue.annoKey = new ArrayList();
		// TTS
		tts = new TextToSpeech(this, ttsInitListener);
		final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
		myFullScreenFlag =
			application.ShowStatusBarOption.getValue() ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
		getWindow().setFlags(
			WindowManager.LayoutParams.FLAG_FULLSCREEN, myFullScreenFlag
		);
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//this.getSystemService("STATUS_BAR");
		
		
		//UIUtil.showMessageText(this, "showStatusBar = "+application.ShowStatusBarOption.getValue() );
		
		final FBReaderApp fbReader = (FBReaderApp)FBReaderApp.Instance();
		if (fbReader.getPopupById(TextSearchPopup.ID) == null) {
			new TextSearchPopup(fbReader);
		}
		if (fbReader.getPopupById(NavigationPopup.ID) == null) {
			new NavigationPopup(fbReader);
		}
		if (fbReader.getPopupById(SelectionPopup.ID) == null) {
			new SelectionPopup(fbReader);
		}

		fbReader.addAction(ActionCode.SHOW_LIBRARY, new ShowLibraryAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_PREFERENCES, new ShowPreferencesAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_BOOK_INFO, new ShowBookInfoAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_TOC, new ShowTOCAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_BOOKMARKS, new ShowBookmarksAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_NETWORK_LIBRARY, new ShowNetworkLibraryAction(this, fbReader));
		
		fbReader.addAction(ActionCode.SHOW_MENU, new ShowMenuAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_NAVIGATION, new ShowNavigationAction(this, fbReader));
		fbReader.addAction(ActionCode.SEARCH, new SearchAction(this, fbReader));

		fbReader.addAction(ActionCode.SELECTION_SHOW_PANEL, new SelectionShowPanelAction(this, fbReader));
		fbReader.addAction(ActionCode.SELECTION_HIDE_PANEL, new SelectionHidePanelAction(this, fbReader));
		fbReader.addAction(ActionCode.SELECTION_COPY_TO_CLIPBOARD, new SelectionCopyAction(this, fbReader));
		fbReader.addAction(ActionCode.SELECTION_SHARE, new SelectionShareAction(this, fbReader));
		fbReader.addAction(ActionCode.SELECTION_TRANSLATE, new SelectionTranslateAction(this, fbReader));
		fbReader.addAction(ActionCode.SELECTION_BOOKMARK, new SelectionBookmarkAction(this, fbReader));

		fbReader.addAction(ActionCode.PROCESS_HYPERLINK, new ProcessHyperlinkAction(this, fbReader));

		fbReader.addAction(ActionCode.SHOW_CANCEL_MENU, new ShowCancelMenuAction(this, fbReader));
		
		//test  TABHOST
		fbReader.addAction(ActionCode.SHOW_ANNO_TEXT_MENU, new ShowNoteTabDialogAction(this, fbReader));  //
		fbReader.addAction(ActionCode.SHOW_ANNO_PIC_MENU, new ShowPicNoteTabDialogAction(this, fbReader));  //Tabhost		
		
		//test 圖片註記 老師講解
		fbReader.addAction(ActionCode.PIC_NOTE_OPEN, new ShowPicNoteAction(this, fbReader)); //圖片註記開啟
		fbReader.addAction(ActionCode.RECORD_TEA, new RecordTeacherVoice(this, fbReader)); //圖片註記開啟
		
		//家長參與
		fbReader.addAction(ActionCode.PARENTS_INVOLVEMENT, new ParentsInvolvementAction(this, fbReader)); //圖片註記開啟
		fbReader.addAction(ActionCode.RECORD_PAENTS, new RecordParentsVoice(this, fbReader)); //家長簽章
		
		//同步按鈕
		fbReader.addAction(ActionCode.SYNC, new SYNCAction(this, fbReader)); //同步按鈕
		//分享觀摩註記
		fbReader.addAction(ActionCode.SHOW_SHAREDATA, new ShareDataAction(this, fbReader)); //觀摩註記
		//返回自己註記
		fbReader.addAction(ActionCode.SHOW_SELFDATA, new SelfDataAction(this, fbReader)); //觀摩註記
		
		//取得DB
		fbReader.addAction(ActionCode.GET_ALL_DATA, new GetSelfDataAction(this, fbReader)); //觀摩註記
		/*
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;
		Toast.makeText(getApplicationContext(),
				"Width = "+ width + " and Height = "+ height,
				Toast.LENGTH_SHORT).show();
		*/
	}

	//點選menu執行
 	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
		if (!application.ShowStatusBarOption.getValue() &&
			application.ShowStatusBarWhenMenuIsActiveOption.getValue()) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		}
		menu.clear();
		if( !SaveValue.IsSyncData ) {
			
			//還原資料按鈕
			addMenuItem(menu, ActionCode.GET_ALL_DATA, R.drawable.ic_menu_refresh);
			//圖片註記
			///addMenuItem(menu, ActionCode.PIC_NOTE_OPEN, R.drawable.ic_menu_bookmarks); //圖片註記開關
			//錄製老師講解
			///addMenuItem(menu, ActionCode.RECORD_TEA, R.drawable.ic_menu_library); //老師講解開關
			//家長參與
			///addMenuItem(menu, ActionCode.PARENTS_INVOLVEMENT, R.drawable.ic_menu_parents); //老師講解開關
			//同步按鈕
			//addMenuItem(menu, ActionCode.SYNC, R.drawable.ic_menu_refresh); //同步按鈕
			//分享
			///addMenuItem(menu, ActionCode.SHOW_SHAREDATA, R.drawable.ic_menu_search);
			
			//addMenuItem(menu, ActionCode.SHOW_LIBRARY, R.drawable.ic_menu_library);
			//addMenuItem(menu, ActionCode.SHOW_NETWORK_LIBRARY, R.drawable.ic_menu_networklibrary);
			//目錄
			///addMenuItem(menu, ActionCode.SHOW_TOC, R.drawable.ic_menu_toc);
			//addMenuItem(menu, ActionCode.SHOW_BOOKMARKS, R.drawable.ic_menu_bookmarks);
			//addMenuItem(menu, ActionCode.SWITCH_TO_NIGHT_PROFILE, R.drawable.ic_menu_night);
			//addMenuItem(menu, ActionCode.SWITCH_TO_DAY_PROFILE, R.drawable.ic_menu_day);
			//addMenuItem(menu, ActionCode.SEARCH, R.drawable.ic_menu_search);
			//addMenuItem(menu, ActionCode.SHOW_PREFERENCES, R.drawable.ic_menu_search);
			//addMenuItem(menu, ActionCode.SHOW_BOOK_INFO);
			//addMenuItem(menu, ActionCode.ROTATE);
			//addMenuItem(menu, ActionCode.INCREASE_FONT);
			//addMenuItem(menu, ActionCode.DECREASE_FONT);
			//addMenuItem(menu, ActionCode.SHOW_NAVIGATION);
		} else {
			
			//分享
			///addMenuItem(menu, ActionCode.SHOW_SHAREDATA, R.drawable.ic_menu_search);
			//返回自己的註記內容
			///addMenuItem(menu, ActionCode.SHOW_SELFDATA, R.drawable.ic_menu_student);
			//目錄
			///addMenuItem(menu, ActionCode.SHOW_TOC, R.drawable.ic_menu_toc);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		super.onOptionsMenuClosed(menu);
		final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
		if (!application.ShowStatusBarOption.getValue() &&
			application.ShowStatusBarWhenMenuIsActiveOption.getValue()) {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			final String pattern = intent.getStringExtra(SearchManager.QUERY);
			final Runnable runnable = new Runnable() {
				public void run() {
					final FBReaderApp fbReader = (FBReaderApp)FBReaderApp.Instance();
					final TextSearchPopup popup = (TextSearchPopup)fbReader.getPopupById(TextSearchPopup.ID);
					popup.initPosition();
					fbReader.TextSearchPatternOption.setValue(pattern);
					if (fbReader.getTextView().search(pattern, true, false, false, false) != 0) {
						runOnUiThread(new Runnable() {
							public void run() {
								fbReader.showPopup(popup.getId());
							}
						});
					} else {
						runOnUiThread(new Runnable() {
							public void run() {
								UIUtil.showErrorMessage(FBReader.this, "textNotFound");
								popup.StartPosition = null;
							}
						});
					}
				}
			};
			UIUtil.wait("search", runnable, this);
			startActivity(new Intent(this, getClass()));
		} else {
			super.onNewIntent(intent);
		}
	}

	RelativeLayout root;
	@Override
	public void onStart() {
		super.onStart();
		final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();

		final int fullScreenFlag =
			application.ShowStatusBarOption.getValue() ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
		if (fullScreenFlag != myFullScreenFlag) {
			finish();
			startActivity(new Intent(this, this.getClass()));
		}

		final FBReaderApp fbReader = (FBReaderApp)FBReaderApp.Instance();
		//final RelativeLayout root = (RelativeLayout)findViewById(R.id.root_view);
		root = (RelativeLayout)findViewById(R.id.root_view);
		((PopupPanel)fbReader.getPopupById(TextSearchPopup.ID)).createControlPanel(this, root, PopupWindow.Location.Bottom);
		((PopupPanel)fbReader.getPopupById(NavigationPopup.ID)).createControlPanel(this, root, PopupWindow.Location.Bottom);
		((PopupPanel)fbReader.getPopupById(SelectionPopup.ID)).createControlPanel(this, root, PopupWindow.Location.Floating);
		
		fbReader.setDB(db);
		fbReader.setSync(syncdb);
		fbReader.setMysql(mysql);
		fbReader.setTTS(tts);
		fbReader.setLayout(root, this);
		
		
		if( root.getChildCount() < 5 ) {
			addOtherView(root);
			fbReader.setAdapter(adter, lvm);
		}
		
		
		synchronized (myPluginActions) {
			myPluginActions.clear();
		}

		sendOrderedBroadcast(
			new Intent(PluginApi.ACTION_REGISTER),
			null,
			myPluginInfoReceiver,
			null,
			RESULT_OK,
			null,
			null
		);
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			//判斷是否剛拍完照(自行拍照新增註記頁面，非註記內拍照)
			if(SaveValue.finishTakpic){
				final SQLiteDB db = ZLApplication.Instance().db;
				AlertDialog.Builder editDialog = new AlertDialog.Builder(FBReader.this);
				editDialog.setMessage("請輸入圖片名稱");
				final EditText editText = new EditText(FBReader.this);
				editText.setText("");
				editDialog.setView(editText);
				editDialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(editText.getText().toString() != null || editText.getText().toString() != ""){
							SaveValue.picNow = SaveValue.picUri; //一拍完馬上改變照片的路徑(ondraw裡面呼叫的路徑)
							String picFileName = SaveValue.picUri; //圖檔名稱(非)使用者自定義明子
							//System.out.println("輸入的字串為:" + editText.getText().toString());
							db.insertpicture(SaveValue.UserName, SaveValue.picLesson, picFileName, editText.getText().toString());
							//頁面刷新(跳轉至新拍攝的照片)
							root.postInvalidate();
						}
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				})
				.show();
				SaveValue.finishTakpic = false;
			}
			sendBroadcast(new Intent(getApplicationContext(), KillerCallback.class));
		} catch (Throwable t) {
		}
		PopupPanel.restoreVisibilities(FBReaderApp.Instance());
		
	}

	@Override
	public void onStop() {
		PopupPanel.removeAllWindows(FBReaderApp.Instance());
		if( SaveValue.IsParent && !SaveValue.IsSystemActivity ) {  //取消家長參與狀態
			SaveValue.IsParent = !SaveValue.IsParent;
			final SQLiteDB db = ZLApplication.Instance().db;
			int parentInNum = db.getTableCount(ActionCode.ANNOTATION_PARENTIN_TIME);  //註記個數
			db.updateParentInTime(parentInNum);
			changeBtnPic();
		}
		if( SaveValue.IsSyncData ) {
			SaveValue.IsUpdateListView = true;
			SaveValue.IsSyncData = !SaveValue.IsSyncData; //取消觀摩其他人註記
			SaveValue.setSyncDataOff();
		}
		super.onStop();
	}

	@Override
	protected FBReaderApp createApplication(ZLFile file) {
		if (SQLiteBooksDatabase.Instance() == null) {
			new SQLiteBooksDatabase(this, "READER");
		}
		return new FBReaderApp(file != null ? file.getPath() : null);
	}

	@Override
	public boolean onSearchRequested() {
		final FBReaderApp fbreader = (FBReaderApp)FBReaderApp.Instance();
		final FBReaderApp.PopupPanel popup = fbreader.getActivePopup();
		fbreader.hideActivePopup();
		final SearchManager manager = (SearchManager)getSystemService(SEARCH_SERVICE);
		manager.setOnCancelListener(new SearchManager.OnCancelListener() {
			public void onCancel() {
				if (popup != null) {
					fbreader.showPopup(popup.getId());
				}
				manager.setOnCancelListener(null);
			}
		});
		startSearch(fbreader.TextSearchPatternOption.getValue(), true, null, false);
		return true;
	}

	public void showSelectionPanel() {
		final FBReaderApp fbReader = (FBReaderApp)FBReaderApp.Instance();
		final ZLTextView view = fbReader.getTextView();
		((SelectionPopup)fbReader.getPopupById(SelectionPopup.ID))
			.move(view.getSelectionStartY(), view.getSelectionEndY());
		fbReader.showPopup(SelectionPopup.ID);
	}

	public void hideSelectionPanel() {
		final FBReaderApp fbReader = (FBReaderApp)FBReaderApp.Instance();
		final FBReaderApp.PopupPanel popup = fbReader.getActivePopup();
		if (popup != null && popup.getId() == SelectionPopup.ID) {
			FBReaderApp.Instance().hideActivePopup();
		}
	}

	public DialogMoveTab dmt = null;
	public DialogMoveTabPic dmtp = null;
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		final FBReaderApp fbreader = (FBReaderApp)FBReaderApp.Instance();
		switch (requestCode) {
			case REPAINT_CODE:
			{
				final BookModel model = fbreader.Model;
				if (model != null) {
					final Book book = model.Book;
					if (book != null) {
						book.reloadInfoFromDatabase();
						ZLTextHyphenator.Instance().load(book.getLanguage());
					}
				}
				fbreader.clearTextCaches();
				fbreader.getViewWidget().repaint();
				break;
			}
			case CANCEL_CODE:
				fbreader.runCancelAction(resultCode - 1);
				break;
			
			//拍照回傳
			case TAKE_PIC_TEXT:
				dmt.resetPicture();
				break;
				
				//拍照回傳
			case TAKE_PIC_IMAGE:
				dmtp.resetPicture();
				break;
		}
	}

	public void navigate() {
		((NavigationPopup)FBReaderApp.Instance().getPopupById(NavigationPopup.ID)).runNavigation();
	}

	private void addMenuItem(Menu menu, String actionId, String name) {
		final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
		application.myMainWindow.addMenuItem(menu, actionId, null, name);
	}

	private void addMenuItem(Menu menu, String actionId, int iconId) {
		final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
		application.myMainWindow.addMenuItem(menu, actionId, iconId, null);
	}

	private void addMenuItem(Menu menu, String actionId) {
		final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
		application.myMainWindow.addMenuItem(menu, actionId, null, null);
	}

	
	//創建menu執行
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		/*
		//圖片註記
		addMenuItem(menu, ActionCode.PIC_NOTE_OPEN, R.drawable.ic_menu_bookmarks); //圖片註記開關
		//錄製老師講解
		addMenuItem(menu, ActionCode.RECORD_TEA, R.drawable.ic_menu_library); //老師講解開關
		//家長參與
		addMenuItem(menu, ActionCode.PARENTS_INVOLVEMENT, R.drawable.ic_menu_parents); //老師講解開關
		//同步按鈕
		//addMenuItem(menu, ActionCode.SYNC, R.drawable.ic_menu_refresh); //同步按鈕
		//分享
		addMenuItem(menu, ActionCode.SHOW_SHAREDATA, R.drawable.ic_menu_search);
		
		//addMenuItem(menu, ActionCode.SHOW_LIBRARY, R.drawable.ic_menu_library);
		//addMenuItem(menu, ActionCode.SHOW_NETWORK_LIBRARY, R.drawable.ic_menu_networklibrary);
		//目錄
		addMenuItem(menu, ActionCode.SHOW_TOC, R.drawable.ic_menu_toc);
		//addMenuItem(menu, ActionCode.SHOW_BOOKMARKS, R.drawable.ic_menu_bookmarks);
		//addMenuItem(menu, ActionCode.SWITCH_TO_NIGHT_PROFILE, R.drawable.ic_menu_night);
		//addMenuItem(menu, ActionCode.SWITCH_TO_DAY_PROFILE, R.drawable.ic_menu_day);
		//addMenuItem(menu, ActionCode.SEARCH, R.drawable.ic_menu_search);
		//addMenuItem(menu, ActionCode.SHOW_PREFERENCES, R.drawable.ic_menu_search);
		//addMenuItem(menu, ActionCode.SHOW_BOOK_INFO);
		//addMenuItem(menu, ActionCode.ROTATE);
		//addMenuItem(menu, ActionCode.INCREASE_FONT);
		//addMenuItem(menu, ActionCode.DECREASE_FONT);
		//addMenuItem(menu, ActionCode.SHOW_NAVIGATION);
		*/
			
		synchronized (myPluginActions) {
			int index = 0;
			for (PluginApi.ActionInfo info : myPluginActions) {
				if (info instanceof PluginApi.MenuActionInfo) {
					addMenuItem(
						menu,
						PLUGIN_ACTION_PREFIX + index++,
						((PluginApi.MenuActionInfo)info).MenuItemName
					);
				}
			}
		}

		final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
		application.myMainWindow.refreshMenu();

		return true;
	}
	
	
	//test
	@Override
	public void onDestroy() {
		/* 釋放TextToSpeech的資源 */
		tts.shutdown();
		SaveValue.IsUPdate = false; //取消同步資訊
		if( SaveValue.IsParent ) {  //取消家長參與狀態
			SaveValue.IsParent = !SaveValue.IsParent;
			final SQLiteDB db = ZLApplication.Instance().db;
			int parentInNum = db.getTableCount(ActionCode.ANNOTATION_PARENTIN_TIME);  //註記個數
			db.updateParentInTime(parentInNum);
		}
		
		if( SaveValue.IsSyncData ) {
			SaveValue.IsSyncData = !SaveValue.IsSyncData; //取消觀摩其他人註記
			SaveValue.setSyncDataOff();
		}
			
		super.onDestroy();
	}
	
	//test
	private TextToSpeech.OnInitListener ttsInitListener = new TextToSpeech.OnInitListener() {
		
		public void onInit(int status) {
			Locale loc = new Locale("us", "", "");
			if (tts.isLanguageAvailable(loc) == TextToSpeech.LANG_AVAILABLE) {
				tts.setLanguage(loc);
			}
			tts.setOnUtteranceCompletedListener(ttsUtteranceCompletedListener);
			//Log.i("log", "TextToSpeech.OnInitListener");
			
		}
	};
	
	private TextToSpeech.OnUtteranceCompletedListener ttsUtteranceCompletedListener = new TextToSpeech.OnUtteranceCompletedListener() {
		
		public void onUtteranceCompleted(String utteranceId) {
			//Log.i("log", "TextToSpeech.OnUtteranceCompletedListener");
		}
	};
	
	public AnnotationSyncListAdapter adter;
	public ListViewMenu lvm, tocLvm, subtocLvm;
	private int fontSize = 14;
	private int iconX = 8;
	private void addOtherView(final RelativeLayout root) {
		lvm = new ListViewMenu(this);
		lvm.showLeft(296); //高度
		//if(SaveValue.syncOpen)----------------------------KIRK 2016/7/11
		adter = new AnnotationSyncListAdapter(FBReader.this);
		//else ------- Kirk 2016/7/11
		//adter = null;  ------- Kirk 2016/7/11
		lvm.setAdapter(adter);
		/*
		//長按事件
		lvm.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if( !SaveValue.IsToc ) {
					tocLvm = new ListViewMenu(FBReader.this);
					tocLvm.YansetTOC();
					tocLvm.setAdapter(myadapter);
					tocLvm.setLayoutParams(new RelativeLayout.LayoutParams(100,200));
					tocLvm.setY(296 + 50*position);
					tocLvm.setX(60);
					root.addView(tocLvm);}
				else {
					if(SaveValue.IsSubToc){
						root.removeView(subtocLvm);
						SaveValue.IsSubToc = false;
					}
					root.removeView(tocLvm);
					SaveValue.IsToc = false;
					//把陣列設為初始值
					items = new ArrayList(Arrays.asList("拍攝新照片"));
				}
				return false;
			}
			
		});
		*/
		//點擊事件
		lvm.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) { //ListView l, View v, int position, long id
				if( adter.getListImageValue(arg2) ){
					UIUtil.showMessageText(getApplicationContext(), adter.getListUserIdValue(arg2)+" 尚未同步");
				}
				//else包下面 2016/7/11 KIRK
				String temp = SaveValue.SyncUserName; //紀錄改變前觀摩的使用者
				SaveValue.IsUpdateListView = true;
					
				if( !"".equals(temp) && !((String) adter.getList().get(arg2)).equals(temp) ) {  //
					setImage = false;
					SaveValue.setSyncDataOff();
					SaveValue.setSyncDataOn( (String) adter.getList().get(arg2) );
				}
				else if( "".equals(temp)) { // 第一次切換註記
					if(setImage == true){
						setImage = false;
					}
					SaveValue.setSyncDataOn( (String) adter.getList().get(arg2) );
				}
				FBReaderApp.Instance().hideActivePopup();
				user.setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.ic_list_sync_user_close))); //原使用者
				//tick.setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.ic_empty))); //有家長錄音的右下角圖示  家長2
				teach.setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.ic_teacher_close))); //老師講解
				sign.setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.z_viewobjects))); //家長錄音
				//db.getTableCount(ActionCode.ANNOTATION_SYNC_TIME); 取得個數
				final ZLView view = ZLApplication.Instance().getCurrentView();
				view.clear(); //重新繪製canvas
				UIUtil.showMessageText(getApplicationContext(), "目前觀看: "+SaveValue.SyncUserName+"的註記內容");
				}
		});
		root.removeView(lvm);
		root.addView(lvm, 4);
		
		setFamilyBtn(5, 8);
		setTeacherBtn(5, 80);
		setSignature(5, 152);
		setDirectoryBtn(18, 224);//directory-icon
		setUserBtn(10, 296);
		//setTick(1215, 690);  家長3
		//setSignature(1220, 5);
		
		//tempPage = 0;
		animationImage(65,2);
	}
	
	private void saveParentTime() {
		String user = SaveValue.UserName;
		
		final SQLiteDB db = ZLApplication.Instance().db;
		int parentInNum = db.getTableCount(ActionCode.ANNOTATION_PARENTIN_TIME);  //註記個數
		db.insertParentInData( (parentInNum+1) , user);
	}

	private void saveCloseParentTime() {
		final SQLiteDB db = ZLApplication.Instance().db;
		int parentInNum = db.getTableCount(ActionCode.ANNOTATION_PARENTIN_TIME);  //註記個數
		db.updateParentInTime(parentInNum);
	}
	
	//Tick(檢驗是否家長錄音) 家長4
//	private void setTick(int x, int y) {
//		tick = new ImageView(this);
//		//text = new TextView(this); 
//		tick.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT));
//		//text.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT));
//		tick.setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.ic_empty)));
//		//text.setText("家長簽章");
//		//text.setTextSize(fontSize);
//		//text.setTextColor(Color.BLACK);
//		tick.setY(y);
//		//text.setY(tick.getY() + 48);
//		//text.setX(1215);/
//		//text.setX(x);
//		//tick.setX(1225);
//		tick.setX(x);
//		
//		root.removeView(text);
//		root.removeView(tick);
//		
//		root.addView(tick);
//		root.addView(text);
//	}	
	//家長錄音
	private void setSignature(final int x, final int y) {
		sign = new ImageView(this);
		text = new TextView(this); 
		sign.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT));
		text.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT));
		sign.setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.z_viewobjects)));
		text.setText("個人活動");
		text.setTextSize(fontSize);
		text.setTextColor(Color.BLACK);
		sign.setY(y);
		text.setY(sign.getY() + 48);
		//text.setX(1215);/
		text.setX(x);
		//sign.setX(1225);
		sign.setX(x);
		
		root.removeView(text);
		root.removeView(sign);
		
		root.addView(sign);
		root.addView(text);
		
		//個人活動
		sign.setOnClickListener( new OnClickListener() { 
			public void onClick(View v) {
				
				//setImage = true;
				//v.postInvalidate();
				if( !SaveValue.IsToc ) {
					tocLvm = new ListViewMenu(FBReader.this);
					tocLvm.YansetTOC();
					tocLvm.setAdapter(myadapter);
					tocLvm.setLayoutParams(new RelativeLayout.LayoutParams(120,260));
					tocLvm.setY(y);
					tocLvm.setX(x+50);
					root.addView(tocLvm);
					
					tocLvm.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView arg0, View arg1, int arg2, long arg3) {
							// TODO Auto-generated method stub
							ListViewMenu listviewmenu = (ListViewMenu)arg0;
							String chosenLesson = listviewmenu.getItemAtPosition(arg2).toString();
							SaveValue.picLesson = chosenLesson;
							if(!SaveValue.IsSubToc){
								addMenuView(chosenLesson, x, y);
							}
							else{
								root.removeView(subtocLvm);
								SaveValue.IsSubToc = false;
								//把陣列設為初始值
								items = new ArrayList(Arrays.asList("拍攝新照片"));
								addMenuView(chosenLesson, x, y);
							}
						}
					});
				} else {
					if(SaveValue.IsSubToc){
						root.removeView(subtocLvm);
						SaveValue.IsSubToc = false;
					}
					root.removeView(tocLvm);
					SaveValue.IsToc = false;
					//把陣列設為初始值
					items = new ArrayList(Arrays.asList("拍攝新照片"));
				}
				/*
				Intent intent = new Intent();
				intent.setClass(FBReader.this, TakePicture.class);
				startActivity(intent);
				*/
				/*
				Intent intent = new Intent(Intent.ACTION_RUN);
		        Bundle bundle = new Bundle();  
		        bundle.putString("user_id", SaveValue.UserName);  
		        intent.putExtras(bundle);  
				intent.setComponent(new ComponentName("com.example.treasure_hunt", "com.example.treasure_hunt.MainActivity"));
				PackageManager manager = getPackageManager();
				List list = manager.queryIntentActivities(intent, manager.COMPONENT_ENABLED_STATE_DEFAULT);

				if(list.size() > 0)
				{
				 Log.i("Log", "Have application" + list.size());
				 startActivity(intent);
				}
				else
				{
				    Log.i("Log", "None application");
				}*/
			}
		});
	}
	//根據使用者點選的章節，呈現該章節使用者拍的照片選單
	private void addMenuView(String Lesson, int x, int y){
		final SQLiteDB db = ZLApplication.Instance().db;
		final SyncDB syncdb = ZLApplication.Instance().syncdb;
		SaveValue.IsSubToc = true;
		if(Lesson == "Lesson6"){
			int num;
			if(SaveValue.IsSyncData){
				items = new ArrayList(Arrays.asList());
				
				num = syncdb.getSubtocpicNum(SaveValue.SyncUserName, SaveValue.picLesson);
				for(int i = 0; i<num; i++){
					items.add(i, syncdb.getSubtocpicName(SaveValue.SyncUserName, SaveValue.picLesson)[i]);
				} 
			}
			else{
				num = db.getSubtocpicNum(SaveValue.UserName, SaveValue.picLesson);
				for(int i = 0; i<num; i++){
					items.add(i, db.getSubtocpicName(SaveValue.UserName, SaveValue.picLesson)[i]);
				}
			}
			if(items.size() == 0){
				items = new ArrayList(Arrays.asList("該章節尚無照片"));
			}
			mySubtocadapter = new ArrayAdapter(this, R.layout.list_yanlist, items);
			subtocLvm = new ListViewMenu(FBReader.this);
			subtocLvm.setCacheColorHint(Color.TRANSPARENT);
			subtocLvm.YansetTOC();
			subtocLvm.setAdapter(mySubtocadapter);
			subtocLvm.setLayoutParams(new RelativeLayout.LayoutParams(100,150));
			subtocLvm.setY(y);
			subtocLvm.setX(x+170);
		}
		else if(Lesson == "Lesson7"){
			int num;
			if(SaveValue.IsSyncData){
				items = new ArrayList(Arrays.asList());
				
				num = syncdb.getSubtocpicNum(SaveValue.SyncUserName, SaveValue.picLesson);
				for(int i = 0; i<num; i++){
					items.add(i, syncdb.getSubtocpicName(SaveValue.SyncUserName, SaveValue.picLesson)[i]);
				} 
			}
			else{
				num = db.getSubtocpicNum(SaveValue.UserName, SaveValue.picLesson);
				for(int i = 0; i<num; i++){
					items.add(i, db.getSubtocpicName(SaveValue.UserName, SaveValue.picLesson)[i]);
				}
			}
			if(items.size() == 0){
				items = new ArrayList(Arrays.asList("該章節尚無照片"));
			}
			mySubtocadapter = new ArrayAdapter(this, R.layout.list_yanlist, items);
			subtocLvm = new ListViewMenu(FBReader.this);
			subtocLvm.setCacheColorHint(Color.TRANSPARENT);
			subtocLvm.YansetTOC();
			subtocLvm.setAdapter(mySubtocadapter);
			subtocLvm.setLayoutParams(new RelativeLayout.LayoutParams(100,150));
			subtocLvm.setY(y+64);
			subtocLvm.setX(x+170);
		}
		else if(Lesson == "Lesson8"){
			int num;
			if(SaveValue.IsSyncData){
				items = new ArrayList(Arrays.asList());
				num = syncdb.getSubtocpicNum(SaveValue.SyncUserName, SaveValue.picLesson);
				for(int i = 0; i<num; i++){
					items.add(i, syncdb.getSubtocpicName(SaveValue.SyncUserName, SaveValue.picLesson)[i]); } 
			}
			else{
				num = db.getSubtocpicNum(SaveValue.UserName, SaveValue.picLesson);
				for(int i = 0; i<num; i++){
					items.add(i, db.getSubtocpicName(SaveValue.UserName, SaveValue.picLesson)[i]);
				}
			}
			if(items.size() == 0){
				items = new ArrayList(Arrays.asList("該章節尚無照片"));
			}
			mySubtocadapter = new ArrayAdapter(this, R.layout.list_yanlist, items);
			subtocLvm = new ListViewMenu(FBReader.this);
			subtocLvm.setCacheColorHint(Color.TRANSPARENT);
			subtocLvm.YansetTOC();
			subtocLvm.setAdapter(mySubtocadapter);
			subtocLvm.setLayoutParams(new RelativeLayout.LayoutParams(100,150));
			subtocLvm.setY(y + 130);
			subtocLvm.setX(x + 170);
		}
		else if(Lesson == "Lesson9"){
			int num;
			if(SaveValue.IsSyncData){
				items = new ArrayList(Arrays.asList());
				num = syncdb.getSubtocpicNum(SaveValue.SyncUserName, SaveValue.picLesson);
				for(int i = 0; i<num; i++){
					items.add(i, syncdb.getSubtocpicName(SaveValue.SyncUserName, SaveValue.picLesson)[i]); } 
			}
			else{
				num = db.getSubtocpicNum(SaveValue.UserName, SaveValue.picLesson);
				for(int i = 0; i<num; i++){
					items.add(i, db.getSubtocpicName(SaveValue.UserName, SaveValue.picLesson)[i]);
				}
			}
			if(items.size() == 0){
				items = new ArrayList(Arrays.asList("該章節尚無照片"));
			}
			mySubtocadapter = new ArrayAdapter(this, R.layout.list_yanlist, items);
			subtocLvm = new ListViewMenu(FBReader.this);
			subtocLvm.setCacheColorHint(Color.TRANSPARENT);
			subtocLvm.YansetTOC();
			subtocLvm.setAdapter(mySubtocadapter);
			subtocLvm.setLayoutParams(new RelativeLayout.LayoutParams(100,150));
			subtocLvm.setY(y + 196);
			subtocLvm.setX(x + 170);
		}
		else if(Lesson == "返回教材內容"){
			setImage = false;
			root.removeView(tocLvm);
			SaveValue.IsToc = false;
			if(SaveValue.IsSubToc){
				root.removeView(subtocLvm);
			}
			//把陣列設為初始值
			items = new ArrayList(Arrays.asList("拍攝新照片"));
			SaveValue.pageIndex = SaveValue.tempIndex;
			return;
		}
		else{
			root.removeView(tocLvm);
			SaveValue.IsToc = false;
			if(SaveValue.IsSubToc){
				root.removeView(subtocLvm);
			}
		}
		root.addView(subtocLvm);
		//課程內照片的點擊事件
		subtocLvm.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ListViewMenu listviewmenu = (ListViewMenu)parent;
				String chosenitem = listviewmenu.getItemAtPosition(position).toString();
				//必須等到選擇的鈕不為拍攝新照片，再去存新的圖片路徑，不然會導致在FBReader.setimage為true，去點"拍攝新照片"時
				//db.getPicFilePath 抓不到東西，picNow為null，導致FBReader中onDraw的BitMap為Null
				if(chosenitem != "拍攝新照片"){
					//儲存選擇照片所屬的章節(用來畫每一張圖片上的註記用，如果直接用picLesson存，會導致在選擇其他章節時同時更動同一張圖片上的註記顯示)
					SaveValue.nowLesson = SaveValue.picLesson;
					//儲存圖片路徑(非完整路徑)
					if(!SaveValue.IsSyncData ){
						SaveValue.picNow = db.getPicFilePath(SaveValue.UserName, SaveValue.picLesson, chosenitem);
					}
					else if(SaveValue.IsSyncData && chosenitem == "該章節尚無照片"){
						return;
					}
					else{
						SaveValue.picNow = syncdb.getPicFilePath(SaveValue.SyncUserName, SaveValue.picLesson, chosenitem);
						SaveValue.syncsrcPath_pic = "/sdcard/sync/" + SaveValue.SyncUserName + "/" 
								+ SaveValue.picLesson + "/" + SaveValue.picNow;
					}
				}
				final ZLView myview = ZLApplication.Instance().getCurrentView();
				myview.clear();
				root.postInvalidate();
				ActionSelect(chosenitem);
			};
		});
		if(!SaveValue.IsSyncData){
			subtocLvm.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
				// TODO Auto-generated method stub
				final int pos = position;
				final String selectItem = parent.getItemAtPosition(position).toString();
				final SQLiteDB db = ZLApplication.Instance().db;
				if(selectItem != "拍攝新照片"){
					new AlertDialog.Builder(FBReader.this)
					.setMessage("請選擇動作")
					.setPositiveButton("修改", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							AlertDialog.Builder editDialog = new AlertDialog.Builder(FBReader.this);
							editDialog.setMessage("請輸入圖片名稱");
							final EditText editText = new EditText(FBReader.this);
							editText.setText(selectItem);
							editDialog.setView(editText);
							editDialog.setPositiveButton("修改", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										mySubtocadapter.remove(selectItem);
										mySubtocadapter.insert(editText.getText().toString(), position);
										db.updatepicture(SaveValue.UserName, selectItem ,editText.getText().toString(), SaveValue.picLesson);
									}
							})
							.setNegativeButton("取消", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									subtocLvm.setAdapter(mySubtocadapter);
								}
							})
							.show();
						}
					})
					.setNegativeButton("刪除", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							//if()
							db.updateStatus(SaveValue.UserName, selectItem, SaveValue.picLesson);
							items.remove(pos);
							subtocLvm.setAdapter(mySubtocadapter);
							final ZLView view = ZLApplication.Instance().getCurrentView();
							view.clear();
							setImage = false;
						}
					})
					.show();
				}
				return false;
			}
		});}
	}
	private void ActionSelect(String item){
		if(item == "拍攝新照片"){
			items = new ArrayList(Arrays.asList("拍攝新照片"));
			YanchangeActivity();
			root.removeView(subtocLvm);
			root.removeView(tocLvm);
			SaveValue.IsToc = false;
			SaveValue.IsSubToc = false;
			
		}
		else{
			//判斷是不是已經進入個人活動區
			if(!setImage){
				FBReader.setImage = true;
			}
			items = new ArrayList(Arrays.asList("拍攝新照片"));
			root.removeView(subtocLvm);
			root.removeView(tocLvm);
			SaveValue.IsToc = false;
			SaveValue.IsSubToc = false;
		}
		/*
			root.removeView(subtocLvm);
			root.removeView(tocLvm);
			SaveValue.IsToc = false;
			SaveValue.IsSubToc = false;
			FBReader.setImage = true;
			*/
	}
	private static final int TAKE_PIC = 4;
	private void YanchangeActivity(){
		final SQLiteDB db = ZLApplication.Instance().db;
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		int takepic_id = db.getMaxID("takepic") + 1; //新的圖片放入SQLite時的ID(新的一筆資料)
		bundle.putInt("index", takepic_id);
		bundle.putString("lesson", SaveValue.picLesson);
		intent.putExtras(bundle);
		intent.setClass(this, myCameraActivity.class);
		this.startActivity(intent);
	}
	
	//改變家長參與的圖片
	private void changeBtnPic() {
		if( SaveValue.IsParent ) {
			family.setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.ic_parent)));
    		saveParentTime();
    		UIUtil.showMessageText(this, "「家長參與」模式開啟");
    	} else {
    		family.setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.ic_parent_close)));
    		saveCloseParentTime();
    		UIUtil.showMessageText(this, "「家長參與」模式關閉");
    	}
	}
	
	//設定使用者按鈕
	public ImageView btn, user, family, sign, teach, testimage; //other, 使用者, 家人, 家長錄音, 老師講解
	//public ImageView btn, user, family, sign, teach, tick; //other, 使用者, 家人, 家長錄音, 老師講解, 家長錄音標示
	private TextView text; //使用者
	private void setUserBtn(int x, int y) {
		user = new ImageView(this);
		text = new TextView(this); 
		user.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT));
		text.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT)); 
		user.setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.ic_list_sync_user)));
		text.setText(SaveValue.UserName);
		text.setTextSize(fontSize+2);
		text.setTextColor(Color.BLACK);
		user.setY(y);
		text.setY(user.getY() + 45);
		//text.setX(1215);\
		text.setX(x);
		user.setX(iconX);
		
		root.removeView(text);
		root.removeView(user);
		
		root.addView(user);
		root.addView(text);
		
		//切換註記功能
		user.setOnClickListener( new OnClickListener() { 
			public void onClick(View v) {
				if( SaveValue.IsSyncData ) {
					final FBReaderApp fbReader = (FBReaderApp)FBReaderApp.Instance();
					fbReader.doAction(ActionCode.SHOW_SELFDATA);
					user.setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.ic_list_sync_user)));
					//tick.setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.ic_tick_close))); //有家長錄音的右下角圖示     家長1
					teach.setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.ic_teacher))); //老師講解
					sign.setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.z_viewobjects))); //家長錄音
					SaveValue.setSyncDataOff();
					SaveValue.IsUpdateListView = true; //開始重畫ListView
				}
				//v.showContextMenu();
				/* 自己做選單
				final ListViewMenu lvm = new ListViewMenu(FBReader.this);
				lvm.setItem();
				root.addView(lvm, root.getChildCount());
				*/
			}
		});
		
		/* 關閉
		btn.setOnLongClickListener(null);
		btn.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {      
            public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
            	int i=1;
                menu.setHeaderTitle("您想要執行下列哪一個功能?");
                if( !SaveValue.IsParent )
                	menu.add(0, i++, 0, "家長參與 OPEN");
                else
                	menu.add(0, i++, 0, "家長參與 CLOSE");
  	
                menu.add(0, i++, 0, "錄製老師講解");
            	//menu.add(0, i++, 0, "同步設定");
            	menu.add(0, i++, 0, "目錄");
            	
            	if( SaveValue.IsSyncData ) 
                	menu.add(0, i++, 0, "註記切換回自己 ("+SaveValue.UserName+")");
            }
            
        });
        */
	}
	
	//目錄按鈕
	private void setDirectoryBtn(final int x, final int y) {
		btn = new ImageView(this);
		text = new TextView(this); 
		btn.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT));
		text.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT)); 
		btn.setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.ic_directory)));
		text.setText("目錄");
		text.setTextSize(fontSize);
		text.setTextColor(Color.BLACK);
		btn.setY(y);
		text.setY(btn.getY() + 44);
		//text.setX(1215);\
		text.setX(x);
		btn.setX(iconX);
		
		root.removeView(text);
		root.removeView(btn);
		
		root.addView(btn);
		root.addView(text);
		
		//目錄功能
		btn.setOnClickListener( new OnClickListener() { 
			public void onClick(View v) {
				final FBReaderApp fbReader = (FBReaderApp)FBReaderApp.Instance();
				//fbReader.doAction(ActionCode.SHOW_TOC);
				//v.showContextMenu();
				//自己做選單
				if( !SaveValue.IsToc ) {
					tocLvm = new ListViewMenu(FBReader.this);
					tocLvm.setTOC();
					tocLvm.setLayoutParams(new RelativeLayout.LayoutParams(350,500));
					tocLvm.setY(y);
					tocLvm.setX(x+40);
					root.addView(tocLvm, root.getChildCount());
				} else {
					if(SaveValue.IsSubToc){
						root.removeView(subtocLvm);
						SaveValue.IsSubToc = false;
					}
					root.removeView(tocLvm);
					SaveValue.IsToc = false;
				}
			}
		});		
	}
	
	//老師講解按鈕
	private void setTeacherBtn(int x, int y) {
		teach = new ImageView(this);
		text = new TextView(this); 
		teach.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT));
		text.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT)); 
		teach.setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.ic_teacher)));
		text.setText("老師講解");
		text.setTextSize(fontSize);
		text.setTextColor(Color.BLACK);
		teach.setY(y);
		text.setY(teach.getY() + 48);
		//text.setX(1215);\
		text.setX(x);
		teach.setX(iconX);
		
		root.removeView(text);
		root.removeView(teach);
		
		root.addView(teach);
		root.addView(text);
		
		//老師講解功能
		teach.setOnClickListener( new OnClickListener() { 
			public void onClick(View v) {
				if( !SaveValue.IsSyncData ) { //同步時，不能老師講解
					final FBReaderApp fbReader = (FBReaderApp)FBReaderApp.Instance();
					fbReader.doAction(ActionCode.RECORD_TEA);
					//fbReader.doAction(ActionCode.PIC_NOTE_OPEN);
				} else {
					UIUtil.showMessageText(FBReader.this, "目前為觀摩模式，「錄製老師講解」請切回原使用者!");
				}
			}
		});
	}

	//家長參與按鈕
	private void setFamilyBtn(int x, int y) {
		family = new ImageView(this);
		text = new TextView(this); 
		family.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT));
		text.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT)); 
		family.setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.ic_parent_close)));
		text.setText("家長參與");
		text.setTextSize(fontSize);
		text.setTextColor(Color.BLACK);
		family.setY(y);
		text.setY(family.getY() + 45);
		//text.setX(1215);\
		text.setX(x);
		family.setX(iconX);
		
		root.removeView(text);
		root.removeView(family);
		
		root.addView(family);
		root.addView(text);
		
		//家長參與功能
		family.setOnClickListener( new OnClickListener() { 
			public void onClick(View v) {
				SaveValue.IsParent = !SaveValue.IsParent;
				changeBtnPic();
			}
		});		
	}
	/*
	private void setTestImage(){
		testimage = new ImageView(this);
		testimage.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT));
		testimage.setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.testimage)));
		root.addView(testimage);
	}
	*/
	
	//提示視窗
	public ImageView v;
	private void animationImage(int x, int y) {
		//SaveValue.animationOn.setDuration(1500);
		SaveValue.animationOff.setDuration(3000);
		v = new ImageView(this);
		v.setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.hint_message)));
		v.setAlpha(0);
		v.setX(x);
		v.setY(y);
		v.setAnimation(SaveValue.animationOff);
		
		root.removeView(v);
		root.addView(v);
		//v.startAnimation(SaveValue.animationOff);
		//MyThread thread = new MyThread(1500, handler, "AnimationOff");
		//new Thread(thread).start();
	}
	
	private final int ID_USER = 0;
	//private int tempPage = 0;
	public Handler handler = new Handler(){  
        public void handleMessage(Message msg) { 
            switch (msg.what) {
            case ID_USER:
            	try {
            		Bundle data = msg.getData();
                    int ms = data.getInt("OpenRecordVideo");
                    if( ms == 300 ) {
                    	UIUtil.showMessageText(FBReader.this, "RecordVideo");
                    	//sv = (SurfaceView) findViewById(R.id.sv);
                    	//sv.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                    	//sv.setAlpha(0);
                    	//onRec();
                    }
                    SaveValue.IsAnimation = false;
            	} catch (Exception e) {
            		Log.i("test", "test e = "+e);
            	}
            }  
        };  
    };
    
    public boolean isRecording=false;
    public void onRec() {
        if (this.isRecording) {
            //Log.d(TAG, "停止錄影");
            this.isRecording = false;
            this.mr.stop();
            this.mr.release();
        }
        else {
        	try {
        	//int cameraType = 1; // front
        	//c = Camera.open(cameraType);
        	c = Camera.open();
        	Log.d("test", "question1");
        	//c = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            this.isRecording = true;
            this.mr = new MediaRecorder();
            // 以下 this.mr.setXXX() 順序非常非常重要
            //c.unlock();
            Log.d("test", "question2");
            this.mr.setCamera(c);
            this.mr.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            this.mr.setAudioSource(MediaRecorder.AudioSource.MIC);
            this.mr.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            this.mr.setOutputFile(this.createFilePath());
            this.mr.setVideoEncoder(MediaRecorder.VideoEncoder.H263);
            this.mr.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            Log.d("test", "question3");
            // 長寬比有關系
            //this.mr.setVideoSize(500, 480);
            //this.mr.setVideoFrameRate(15);
            // 設定預覽視窗
            this.mr.setPreviewDisplay(this.sv.getHolder().getSurface());
            
                this.mr.prepare();
                this.mr.start();
            }
            catch (Exception e) {
                Log.e("test", "question:"+e.getMessage(), e);
            }
        }
    }
    
    private String createFilePath() {
        File sdCardDir = Environment.getExternalStorageDirectory();
        File vrDir = new File(sdCardDir, "cw1205");
        if (!vrDir.exists()) {
            vrDir.mkdir();
        }
        File file = new File(vrDir, System.currentTimeMillis() + ".3gp");
        String filePath = file.getAbsolutePath();
        //Log.d("test", "輸出路徑：" + filePath);
        return filePath;
    }
	
	
}
