package org.geometerplus.android.fbreader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.sqliteconfig.ConnectMysql;
import org.geometerplus.zlibrary.core.sqliteconfig.SQLiteDB;
import org.geometerplus.zlibrary.core.sqliteconfig.SyncDB;
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.text.view.SaveValue;

import android.app.IntentService;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

//背景執行
public class MyService extends IntentService {
	
	final SQLiteDB db = ZLApplication.Instance().db;
	final SyncDB syncdb = ZLApplication.Instance().syncdb;
	final ConnectMysql mysql = ZLApplication.Instance().mysql;
	
	private final static String DATABASE_INSERT = SaveValue.webIP+"/connectdatabase/upload/connectMysql_insert.php";
	private final static String DATABASE_UPDATE = SaveValue.webIP+"/connectdatabase/upload/connectMysql_update.php";
	private final static String DATABASE_DELETE = SaveValue.webIP+"/connectdatabase/upload/connectMysql_delete.php";
	private final static String DATABASE_DOWNLOAD = SaveValue.webIP+"/connectdatabase/sync/connectMysql_sync.php";
	private final static String[] DBtakepic = {"id", "date","modifieddate","userid","lesson","pic","status","picname"};
	private final static String[] DBtakepic_anno = {"id", "annoType", "sX", "sY", "date", "modifieddate", "comment", "userid", "page", "lesson", "picFilePath", "rec", "pic", "p_id", "status"};
	private final static String[] DBannoImage = { "id","annoType","sX","sY","date","modifieddate","comment","userid","page","rec","pic","p_id","status" };
	private final static String[] DBannoText = { "id","red","green","blue","type","date","modifieddate","comment","userid","page","rec","txt","pic","p_id","status" };
	private final static String[] DBannoText_drawRange = { "id","t_id","left","top","right","bottom","userid","p_id","status" };
	private final static String[] DBtranslation = { "id","t_id","selectedText","num","userid","page","p_id", "status" };
	private final static String[] DBrecord = { "id","rec_id","annoType","startTime","endTime","userid","p_id", "status", "setImage" };
	private final static String[] DBtranslation_num = { "id","translation_id","datetime","userid","p_id","status" };
	private final static String[] DBtts_num= { "id","t_id","datetime","userid","p_id","status" };
	private final static String[] DBparentIn_time = { "id","startTime","endTime","userid","status" };
	private final static String[] syncDBRecord = { "id","rec_id","annoType","startTime","endTime","userid","syncUserid","p_id", "status" };
	//特殊
	private final static String[] DBSync = {"id", "userid", "datetime"};
	private final static String[] DBSync_time = {"id", "startTime", "endTime", "userid", "syncUserid", "p_id"};
	private final static String[] DBSync_action = {"id", "s_id", "type", "type_id", "action", "datetime", "p_id"};
	private final static String[][] DBLocal_Name = {  DBtakepic, DBtakepic_anno, DBannoImage, DBannoText, DBannoText_drawRange, DBparentIn_time, DBrecord,DBtranslation, DBtranslation_num, DBtts_num, syncDBRecord}; //Local端資料表欄位
	private final static String[] Database_Name = { ActionCode.ANNOTATION_TAKEPIC, ActionCode.ANNOTATION_TAKEPIC_ANNO,ActionCode.ANNOTATION_IMAGE,ActionCode.ANNOTATION_TEXT,ActionCode.ANNOTATION_TEXT_RANGE,
														ActionCode.ANNOTATION_PARENTIN_TIME,ActionCode.ANNOTATION_RECORD,
														ActionCode.ANNOTATION_TRANSLATION,ActionCode.ANNOTATION_TRANSLATION_NUMBER,
														ActionCode.ANNOTATION_TTS_NUMBER,ActionCode.SYNC_RECORD}; //Local端資料表名稱
	
	//private final static int[] chapterPage = { 7,327,2457,4687,5185,7281,8985,9348,9859,10280,10289,10290 };  //content003頁數
	//private final static int[] chapterPage = { 8,1968,4065,4911,6033,6042,6050 };  //content004頁數
	private final static int[] chapterPage = { 8,644,1145,1616,1632,2202,2686 };  //content005頁數
	private int[] chapterAnnoNum = new int[6];
	/*
	private final static String[][] DBLocal_Name = { DBannoImage,DBannoText,DBannoText_drawRange,DBparentIn_time,DBrecord,DBtranslation,DBtranslation_num,DBtts_num, syncDBRecord,
														DBSync_time, DBSync_action}; //Local端資料表欄位
	private final static String[] Database_Name = { ActionCode.ANNOTATION_IMAGE,ActionCode.ANNOTATION_TEXT,ActionCode.ANNOTATION_TEXT_RANGE,
														ActionCode.ANNOTATION_PARENTIN_TIME,ActionCode.ANNOTATION_RECORD,
														ActionCode.ANNOTATION_TRANSLATION,ActionCode.ANNOTATION_TRANSLATION_NUMBER,
														ActionCode.ANNOTATION_TTS_NUMBER,ActionCode.SYNC_RECORD,ActionCode.ANNOTATION_SYNC_TIME,
														ActionCode.ANNOTATION_SYNC_ACTION}; //Local端資料表名稱
														*/
	
	private final static int[] Database_Number = { 8,15,13,15,9,5,9,8,6,6,9 }; //欄位個數
	
	public MyService() {
		super("first");
	}

	public MyService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent arg0) {

		try {
			/*
			db.delete("stu022", "userid", "sync_local_data");
			db.delete("stu029", "userid", "sync_local_data");
			db.delete("stu030", "userid", "sync_local_data");
			db.delete("stu032", "userid", "sync_local_data");
			*/
			int result = db.getTableCount(ActionCode.ANNOTATION_SYNC_DATA);
			if( result == 0 ) {
				for(int i=0;i<SaveValue.log_Username.length;i++) {
					db.insertSyncData(i, SaveValue.log_Username[i]);
				}
				
			}
			SaveValue.IsUpdateListView = true;
			Thread.sleep(5*1000);  //先暫停更新5秒
		} catch (Exception e) {
			Log.i("test", "test service error thread"+e);
		}
		//在使用者登入後，IsUPdate 及為true;
		while(SaveValue.IsUPdate) {	
			synchronized (this) {
				try {	
					if( connectInternet() ) { //如果有連上網路
						final ZLView view = ZLApplication.Instance().getCurrentView();

						if( SaveValue.IsNowSync != true) {
							SaveValue.IsNowSync = !SaveValue.IsNowSync;
							if( !SaveValue.IsNowSync && view.Application.adter != null )
							{
								Message m = new Message();
								// 定義 Message的代號，handler才知道這個號碼是不是自己該處理的。
								m.what = 0;
								view.Application.adter.handler.sendMessage(m);
							}
						}
						
						//上傳
						//--------將Local端的每種status的資料上傳到server端
						if( insertData() ) {
							
							if( updateData() ) {
								
								if( deleteData() ) {
									
						//------------------------------------------------
									///記錄當前使用者的文字、圖片以及語音註記資料 的"個別總數"
									int text = db.getTableCount(ActionCode.ANNOTATION_TEXT, SaveValue.UserName, "DELETE");
									int image = db.getTableCount(ActionCode.ANNOTATION_IMAGE, SaveValue.UserName, "DELETE");
									int record = db.getVoiceCount(SaveValue.UserName, "DELETE");
									
									///記錄當前使用者"各個章節"的文字、圖片以及語音註記資料的"個別總數"
									for(int i=0;i<chapterPage.length-1;i++) {
										chapterAnnoNum[i] = db.getTableCount(ActionCode.ANNOTATION_TEXT, SaveValue.UserName, chapterPage[i], chapterPage[i+1], "DELETE")
															+ db.getTableCount(ActionCode.ANNOTATION_IMAGE, SaveValue.UserName, chapterPage[i], chapterPage[i+1], "DELETE")
															+ db.getVoiceCount(SaveValue.UserName, chapterPage[i], chapterPage[i+1], "DELETE");
									}
									///db.updateSyncData(....)更新local端的sync_local_data
									if ( db.updateSyncData(SaveValue.UserName, "userid", ActionCode.ANNOTATION_SYNC_DATA, text, image, record, chapterAnnoNum) != 0 && SaveValue.IsUpdateSyncTime ) {
										SaveValue.IsUpdateSyncTime = false;
										//更新server端的update_data資料表
										syncData(text, image, record, chapterAnnoNum);
										mysql.syncUpdateDBfile();
										//mysql.syncUpdateDBfile();
										Log.i("test", "test sycn");
									}
									
								}
							}
						}
						
						//下載
						if( downloadData("INSERT") ) {
							
							if( downloadData("UPDATE") ) {
								
								if( downloadData("DELETE") ) {
									
									Log.i("test", "download data OK");
								}
							}
						}
						
						
						if( SaveValue.test )
						{
							updateDB();
							SaveValue.test = false;
						}
						//if(  ) //如果是資料庫是清空的狀態，則也下載屬於自己的data
					} else {
						Log.i("test", "no internet");
					}
					
					Thread.sleep(5*1000); //每5秒同步				
					Log.i("test", "test service = OK");
		        } catch (Exception e) {
		        	Log.i("test", "test service errorn = "+e);
		        }
				
				final ZLView view = ZLApplication.Instance().getCurrentView();
				if(view.Application.adter != null){
					Message m = new Message();
					// 定義 Message的代號，handler才知道這個號碼是不是自己該處理的。
					m.what = 0;
					view.Application.adter.handler.sendMessage(m);
				}
		     }
		}
	}
	
	private boolean downloadData(String status) {
		Boolean key = false;
		try {
			for( int i=0;i<5;i++ ) {
				int x = mysql.syncAnnoDownload(SaveValue.UserName, status, Database_Name[i], DATABASE_DOWNLOAD);
				if(x==1)
					Log.i("test", Database_Name[i]+" noData Need download");
			}
			key = true;
		}
		catch(Exception e) {
			Log.i("test", "test db e = "+e);
			key = false;
		}
		
		return key;
	}

	//新增
	private boolean insertData() {

		boolean key = true;
		try {
			int[][] id_insert = new int[Database_Name.length][];
			int i;
			
			for(i=0;i<(Database_Name.length-1);i++) {
					id_insert[i] = db.getRangePage("_id", Database_Name[i], SaveValue.UserName, "INSERT");  //取得每張TABLE的ID (新增)
			}
			id_insert[i] = syncdb.getRangePage("_id", "record", SaveValue.UserName, "INSERT");  //最後一張TABLE為"同步的聆聽資料"
			
			for(i=0;i<(id_insert.length-1);i++) {
				if( id_insert[i].length > 0 ) {
					SaveValue.IsUpdateSyncTime = true;
					for(int j=0;j<id_insert[i].length;j++) {
						System.out.println("插入部分");
						mysql.syncAnno(annoDB(id_insert[i][j], i), DBLocal_Name[i], Database_Name[i], "INSERT", DATABASE_INSERT);
					}
				}
			}
			//最後一張TABLE
			if( id_insert[i].length > 0 ) {
				for(int j=0;j<id_insert[i].length;j++) {
					System.out.println("插入部分，同步表");
					mysql.syncAnno(annoDB_sync(id_insert[i][j], i), DBLocal_Name[i], Database_Name[i], "INSERT", DATABASE_INSERT);
				}
			}
			
		} catch (Exception e) {
			key = false;
			Log.i("upload", "insertData has some problem");
		}
		
		return key;
	}
	
	//修改
	private boolean updateData() {
		/*
		int[] id_update = db.getRangePage("_id", ActionCode.ANNOTATION_TEXT, SaveValue.UserName, "UPDATE");  //取得文字註記ID (修改)
		if( id_update.length > 0 ) {
			for(int i=0;i<id_update.length;i++) {
				mysql.syncAnno(annoDB(id_update[i], 1), DBannoText, ActionCode.ANNOTATION_TEXT, DATABASE_UPDATE);
			}
		} */
		
		boolean key = true;
		try {
			int[][] id_update = new int[Database_Name.length][];
			int i;
			
			for(i=0;i<(Database_Name.length-1);i++) {
				id_update[i] = db.getRangePage("_id", Database_Name[i], SaveValue.UserName, "UPDATE");  //取得每張TABLE的ID (修改)
				/*
				System.out.println("aaa");
				System.out.println("取得資料表的名稱 = " + Database_Name[i]);
				*/
			}
			id_update[i] = syncdb.getRangePage("_id", "record", SaveValue.UserName, "UPDATE");  //最後一張TABLE為"同步的聆聽資料"
			//Log.i("test","test str i= "+i);
			//Log.i("test","test str i= 0"+id_update[i].length);
			for(i=0;i<(id_update.length-1);i++) {
				if( id_update[i].length > 0 ) {
					SaveValue.IsUpdateSyncTime = true;
					for(int j=0;j<id_update[i].length;j++) {
						System.out.println("更新部分" + Database_Name[i]);
						mysql.syncAnno(annoDB(id_update[i][j], i), DBLocal_Name[i], Database_Name[i], "UPDATE", DATABASE_UPDATE);
					}
				}
			}
			
			//最後一張TABLE
			if( id_update[i].length > 0 ) {
				for(int j=0;j<id_update[i].length;j++) {
					mysql.syncAnno(annoDB_sync(id_update[i][j], i), DBLocal_Name[i], Database_Name[i], "UPDATE", DATABASE_UPDATE);
				}
			}
		} catch (Exception e) {
			key = false;
			Log.i("upload", "updateData has some problem");
		}
		return key;	
	}
	
	//刪除
	private boolean deleteData() {
		/*
		int[] id_delete = db.getRangePage("_id", ActionCode.ANNOTATION_TEXT, SaveValue.UserName, "DELETE");  //取得文字註記ID (刪除)
		if( id_delete.length > 0 ) {
			for(int i=0;i<id_delete.length;i++) {
				mysql.syncAnno(annoDB(id_delete[i], 1), DBannoText, ActionCode.ANNOTATION_TEXT, DATABASE_DELETE);
			}
		}*/
		
		boolean key = true;
		try {
			int[][] id_delete = new int[Database_Name.length][];
			int i;
			
			for(i=0;i<(Database_Name.length-1);i++) {
				id_delete[i] = db.getRangePage("_id", Database_Name[i], SaveValue.UserName, "DELETE");  //取得每張TABLE的ID (刪除)
			}
			
			for(i=0;i<(id_delete.length-1);i++) {
				if( id_delete[i].length > 0 ) {
					SaveValue.IsUpdateSyncTime = true;
					for(int j=0;j<id_delete[i].length;j++) {
						System.out.println("刪除部分" + Database_Name[i]);
						mysql.syncAnno(annoDB(id_delete[i][j], i), DBLocal_Name[i], Database_Name[i], "DELETE", DATABASE_DELETE);
					}
				}
			}
		} catch (Exception e) {
			key = false;
			Log.i("upload", "deleteData has some problem");
		}
		
		return key;	
	}
	
	//資料ID and 哪個資料表(SQLite DB)
	private String[] annoDB(int id, int tableId) {
		String[] str = db.getAllData(Database_Name[tableId], id, Database_Number[tableId]);
		for(int i=0;i<str.length;i++){
			//顯示傳輸資料的訊息
			//Log.i("test","test str[] = "+str[i]);
		}
		return str;
	}
	
	//資料ID and 哪個資料表(Sync DB)
	private String[] annoDB_sync(int id, int tableId) {
		//Log.i("test","test str = ");
		String[] str = syncdb.getAllData(Database_Name[tableId], id, Database_Number[tableId]);
		for(int i=0;i<str.length;i++){
			//顯示傳輸資料的訊息
			//Log.i("test","test str[] = "+str[i]);
		}
		return str;
	}	
	
	//sync
	private boolean syncData(int text, int image, int record, int[] chapter) {
		Log.i("syncData", "MyService (Line353) 更新server端的update_data資料表");
		String date = db.getStrComment("datetime", ActionCode.ANNOTATION_SYNC_DATA, SaveValue.UserName);
		mysql.syncAnnoEnd(String.valueOf(text), String.valueOf(image), String.valueOf(record), chapter, SaveValue.UserName, date, DATABASE_INSERT);
		return true;
	}
	
	//更新同步資料
	private void updateDB() {
		try {
			Log.i("syncData", "MyService (Line361) 更新Local端的sync_local_data資料表");
			//Log.i("test", "test db error(460) = "+SaveValue.syncUserOK.size());
			for(int i=0;i<SaveValue.log_Username.length;i++) {
				int temp1, temp2, temp3;
				////記錄每位學生的文字、圖片以及語音註記資料 的"個別總數"
				if( SaveValue.log_Username[i].equals(SaveValue.UserName) ) {
					temp1 = db.getTableCount(ActionCode.ANNOTATION_TEXT, SaveValue.log_Username[i], "DELETE");
					temp2 = db.getTableCount(ActionCode.ANNOTATION_IMAGE, SaveValue.log_Username[i], "DELETE");
					temp3 = db.getVoiceCount(SaveValue.log_Username[i], "DELETE");

					//記錄每位學生"各個章節"的文字、圖片以及語音註記資料的"個別總數"
					for(int j=0;j<chapterPage.length-1;j++) {
						chapterAnnoNum[j] = 0;
						chapterAnnoNum[j] = db.getTableCount(ActionCode.ANNOTATION_TEXT, SaveValue.log_Username[i], chapterPage[j], chapterPage[j+1], "DELETE")
											+ db.getTableCount(ActionCode.ANNOTATION_IMAGE, SaveValue.log_Username[i], chapterPage[j], chapterPage[j+1], "DELETE")
											+ db.getVoiceCount(SaveValue.log_Username[i], chapterPage[j], chapterPage[j+1], "DELETE");
					}
					
					if( db.updateSyncData(SaveValue.log_Username[i], "userid", ActionCode.ANNOTATION_SYNC_DATA, temp1, temp2, temp3, chapterAnnoNum) == 1 )
						Log.i("test", SaveValue.log_Username[i]+" Myservice.updateDB() = OK");
				} else {
					temp1 = syncdb.getTableCount(ActionCode.ANNOTATION_TEXT, SaveValue.log_Username[i], "DELETE");
					temp2 = syncdb.getTableCount(ActionCode.ANNOTATION_IMAGE, SaveValue.log_Username[i], "DELETE");
					temp3 = syncdb.getVoiceCount(SaveValue.log_Username[i], "DELETE");
					
					//記錄每章節的註記數量
					for(int j=0;j<chapterPage.length-1;j++) {
						chapterAnnoNum[j] = 0;
						chapterAnnoNum[j] = syncdb.getTableCount(ActionCode.ANNOTATION_TEXT, SaveValue.log_Username[i], chapterPage[j], chapterPage[j+1], "DELETE")
											+ syncdb.getTableCount(ActionCode.ANNOTATION_IMAGE, SaveValue.log_Username[i], chapterPage[j], chapterPage[j+1], "DELETE")
											+ syncdb.getVoiceCount(SaveValue.log_Username[i], chapterPage[j], chapterPage[j+1], "DELETE");
					}
					
					if( db.updateSyncData(SaveValue.log_Username[i], "userid", ActionCode.ANNOTATION_SYNC_DATA, temp1, temp2, temp3, chapterAnnoNum) == 1 )
						Log.i("test", SaveValue.log_Username[i]+" Myservice.updateDB() = success");
				}
				
				
				
				
			}
			SaveValue.IsUpdateListView = true; //更新數據
		} catch (Exception e) {
			Log.e("MyService", "test error = "+e);
		}
	}
	
	//檢測網路
	private boolean connectInternet() {
		ConnectivityManager connManager = (ConnectivityManager)getSystemService(this.CONNECTIVITY_SERVICE);

		NetworkInfo info = connManager.getActiveNetworkInfo();

		if (info == null || !info.isConnected()) {
			return false;
		} else {
		   if (!info.isAvailable()) {
			   return false;
		   } else {
			   return true;
		   }
		}
	}
	
}
