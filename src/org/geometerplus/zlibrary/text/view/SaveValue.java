package org.geometerplus.zlibrary.text.view;

import java.util.ArrayList;

import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.sqliteconfig.SQLiteDB;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

public class SaveValue {
	
	//圖片名稱
	public static String picUri = "";
	//判斷是否剛拍照完
	public static Boolean finishTakpic = false;
	//文字註記-------------------------------------------------------------------
	//目前點選highLight
	public static int nowIndex = -1;
	//文字註記是否顯示
	//public static int[] annoKey; 
	public static ArrayList annoKey = new ArrayList(); //紀錄那些註記不顯示 處理自己的註記
	public static ArrayList annoKeySync = new ArrayList(); //紀錄那些註記不顯示 處理其他人的註記(每當切換任何使用者，陣列的值要清空)
	public static int key = 0; //annoKey的個數
	
	//文字註記-------------------------------------------------------------------
	//自行新增的圖片(非註記)---------------------------------------------------------------
	public static String picLesson = null; //選擇拍攝的圖片時用(takepic)
	public static String  picNow = null; //當前選擇的圖片名稱(檔案名稱，非使用者命名)
	public static String nowLesson = null;//所選圖片所屬的章節(在使用者選擇所拍攝的照片時才會進行存取，takepic_anno用)
	public static String syncsrcPath_pic = null;//同步時，要顯示的圖片路徑
	//圖片註記-------------------------------------------------------------------
	//目前點選圖片註記
	public static int picNowIndex = -1;
	public static int tempIndex = -1; //儲存切換模式前的頁面(第幾頁)
	//width( 選取的文字寬度 )
	public static float wordWidth = 0;
	//老師講解次數
	public static int lecture_num = 0;
	//圖片註記-------------------------------------------------------------------		
	
	//page information
	public static int pageIndex = 1;
	public static boolean draw = false;
	/*
	public static String[] log_Username = {   "stu001", "stu002", "stu003", "stu004", "stu005", 
												"stu006", "stu007", "stu008", "stu009", "stu010",
												"stu011", "stu012", "stu013", "stu014", "stu015",
												"stu016", "stu017", "stu018", "stu019", "stu020",
												"stu021", "stu022", "stu023", "stu024", "stu025",
												"stu026", "stu027", "stu028", "stu029", "stu030",
												"stu031", "stu032", "stu033", "stu034", "stu035",
												"stu036", "stu037", "stu038", "stu039", "stu040"};
	
	*/
	public static String[] log_Username = {   "stu001", "stu002", "stu003", "stu004", "stu005", 
		"stu006", "stu007", "stu008", "stu009", "stu010",
		"stu011", "stu012", "stu013", "stu014", "stu015",
		"stu016", "stu017", "stu018", "stu019", "stu020",
		"stu021", "stu022", "stu023", "stu024", "stu025",
		"stu026", "stu027", "stu028", "stu029", "stu030"};

	public static ArrayList syncUserOK = new ArrayList();
	public static String UserName; //使用者名稱
	public static String SyncUserName = ""; //同儕使用者名稱

	public static boolean picNoteOpen = false;
	public static boolean NoteOpen = true;
	
	//test 紀錄y
	public static int y_range = 0;
	
	//用來計算每一行字( 一句 )的像素
	public static int w = 0;
	public static boolean w_key = true;
	
	//註記對話框出現
	public static boolean IsNote = true;
	
	//是否老師講解的註記
	public static boolean Islecture = false;
	
	//是否刷新頁面( 切換目錄時需要 )
	public static boolean IsRepaint = false;
	
	//偵測目前是否為切換到系統的其他Activity(非FBReader Activity)
	public static boolean IsSystemActivity = false;
	
	//是否家長參與
	public static boolean IsParent = false; //預設為關閉
	
	//是否能連線
	public static boolean IsConnect = false; //預設為無連線狀態
	
	//書籍名稱
	public static String bookName = "content20140428.epub";
	//

	//執行同步動作的鑰匙( false 則不同步 )( true 則每10秒同步一次資料 )
	public static boolean IsUPdate = false;

	//目前正在同步其他人的數據嗎?( 觀摩模式 )
	public static boolean IsSyncData = false;
	
	//( 觀摩模式是否打開 )
	public static boolean IsListVisible = false;
	
	//有資料上傳?
	public static boolean IsUpdateSyncTime = false;
	
	//是否更新listView
	public static boolean IsUpdateListView = false;
	
	
	public static void setSyncDataOn(String userName){
		final SQLiteDB db = ZLApplication.Instance().db;
		IsSyncData = true;
		IsUpdateListView = true;
		SyncUserName = userName; //空值
		db.insertSyncTime(SaveValue.UserName, SaveValue.SyncUserName);
		annoKeySync.clear(); //清空隱藏的註記資料，讓註記資料不隱藏
	}
	
	public static void setSyncDataOff(){
		final SQLiteDB db = ZLApplication.Instance().db;
		IsSyncData = false;
		SyncUserName = ""; //空值
		int temp = db.getTableCount(ActionCode.ANNOTATION_SYNC_TIME); //取得個數
		db.updateSyncTime(String.valueOf(temp), "_id");
		annoKeySync.clear(); //清空隱藏的註記資料，讓註記資料不隱藏
	}
	
	//Animation
	public static Animation animationOn=new AlphaAnimation(0, 1);
	public static Animation animationOff=new AlphaAnimation(1, 0);
	
	//網址
	public static String webIP = "http://140.115.135.135";
	//public static String webIP = "http://140.115.135.135";
	//public static String webIP = "http://140.115.126.197";
	//public static String webIP = "http://140.115.126.179";
	
	//目前是否允許同步
	public static boolean IsNowSync = false;
	
	//是否動畫提示中
	public static boolean IsAnimation = false;
	
	//是否切換目錄
	public static boolean IsToc = false;
	public static boolean IsSubToc = false;
	
	//測試同步用
	public static boolean test = false;
	
	//測試同步用
	public static boolean syncOpen = false;
	
	public static int tttt = 0;
	public static int tttt2 = 0;
}