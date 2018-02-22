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

package org.geometerplus.zlibrary.core.sqliteconfig;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.text.view.SaveValue;

public final class SyncDB extends SQLiteOpenHelper {
	
	private final static String DATABASE_NAME = "sync.db";
	private final static int DATABASE_VERSION = 1;
	private final static String[] status = {"INSERT", "UPDATE", "DELETE", "NOACTION", "DELETEOK"}; 

	public SyncDB(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	//test
	private static SyncDB ourInstance;

	public static SyncDB Instance() {
		return ourInstance;
	}
	//test

	@Override
	public void onCreate(SQLiteDatabase sqldb) {
		//文字註記--------------------------------------------------------------------------------------------------------------------------------------
		sqldb.execSQL("CREATE TABLE annotext (_id INTEGER PRIMARY KEY, id INT, red INT, green INT, blue INT, type INT," +
				"date TEXT, modifieddate TEXT, comment TEXT, userid TEXT, page INT, rec TEXT, txt TEXT, pic TEXT, p_id INT, status TEXT)");
		//文字註記---範圍
		sqldb.execSQL("CREATE TABLE annotext_range (_id INTEGER PRIMARY KEY, t_id INT, left INT, top INT, right INT, bottom INT, userid TEXT, p_id INT, status TEXT)");
		//文字註記--------------------------------------------------------------------------------------------------------------------------------------

		//圖片註記--------------------------------------------------------------------------------------------------------------------------------------
		sqldb.execSQL("CREATE TABLE annoimage (_id INTEGER PRIMARY KEY, id INT, annoType TEXT, sX INT, sY INT, date TEXT, modifieddate TEXT, comment TEXT," +
						"userid TEXT, page INT, rec TEXT, pic TEXT, p_id INT, status TEXT)");
		//圖片註記--------------------------------------------------------------------------------------------------------------------------------------
		//播放聲音紀錄
		sqldb.execSQL("CREATE TABLE record (_id INTEGER PRIMARY KEY, rec_id INT, annoType TEXT, startTime TEXT, endTime TEXT, userid TEXT, syncUserid TEXT, p_id INT, status TEXT)");
		//自行拍攝照片
		sqldb.execSQL("CREATE TABLE takepic_anno (_id INTEGER PRIMARY KEY, id INT, annoType TEXT, sX INT, sY INT, date TEXT, modifieddate TEXT, comment TEXT, userid TEXT, page INT, lesson TEXT, picFilePath TEXT, rec TEXT, pic TEXT, p_id INT, status TEXT)");
		sqldb.execSQL("CREATE TABLE takepic (_id INTEGER PRIMARY KEY, id INT, date TEXT, modifieddate TEXT, userid TEXT, lesson TEXT, pic TEXT, status TEXT, picname TEXT)");
	}
	
	//------------------------------------------------------------------------------------------delete
	public void delete(int id, String field, String TableName) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = field + " = ?";
		String[] whereValue = { Integer.toString(id) };
		
		db.delete(TableName, where, whereValue);
		//long result = db.delete(TableName, where, whereValue);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
	}
	
	public synchronized int parentInvolvement() {
		if( SaveValue.IsParent ) {
			final SQLiteDB db = ZLApplication.Instance().db;
			int num = db.getTableCount(ActionCode.ANNOTATION_PARENTIN_TIME);  //註記個數
			return num;
		}
		return 0;
	}
	
	//------------------------------------------------------------------------------------------insert
	//新增註記位置範圍( drawRange )
	public synchronized Long insertRange(int id, int left, int top, int right, int bottom, String userName) {
		
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv=new ContentValues();
		//id
		cv.put("t_id", id);
		//l t r b
		cv.put("left", left);
		cv.put("top", top);
		cv.put("right", right);
		cv.put("bottom", bottom);
		cv.put("userid", userName);
		cv.put("status", status[3]);
		
		//db.insert(ActionCode.ANNOTATION_TEXT_RANGE, null, cv);
		//Log.i("test", "test server image = "+db.insert(ActionCode.ANNOTATION_TEXT_RANGE, null, cv));
		return db.insert(ActionCode.ANNOTATION_TEXT_RANGE, null, cv);
	}
	
	//新增註記位置( annotext )
	public synchronized Long insertNote(int id, int r, int g, int b, String type, String date, String mdate, String comment, String userName, int page, String rec, String txt, String pic) {
		
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv=new ContentValues();
		cv=new ContentValues();
		//_id red green blue type date modifieddate comment userid page rec txt p_id status
		//id
		cv.put("id", id);
		//color
		cv.put("red", r);
		cv.put("green", g);
		cv.put("blue", b);
		//style
		cv.put("type", type);  //預設是底線
		//date
		cv.put("date", date);
		//mdate
		cv.put("modifieddate", mdate);
		//comment
		cv.put("comment", ("".equals(comment) ? null : comment));
		//userid
		cv.put("userid", userName);
		//page
		cv.put("page", page);
		//rec
		cv.put("rec", ("".equals(rec) ? null : rec));
		//txt ( 被選取的文字 )
		cv.put("txt", txt);
		//txt ( 照片 )
		cv.put("pic", pic);
		cv.put("status", status[3]);
		
		//Log.i("test", "test server result0 = "+ ("".equals(rec) ? null : rec));
		//db.insert(ActionCode.ANNOTATION_TEXT, null, cv);
		//Log.i("test", "test server text = "+db.insert(ActionCode.ANNOTATION_TEXT, null, cv));
		return db.insert(ActionCode.ANNOTATION_TEXT, null, cv);
		
	}
	
	//新增註記位置( annoimage )
	public synchronized Long insertPicNote(int id, String annoType, int x, int y, String date, String mdate, String comment, String userName, int page, String rec, String pic) {
		
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv=new ContentValues();
		//_id annoType sX sY date modifieddate comment userid page rec p_id status
		cv=new ContentValues();
		//id
		cv.put("id", id);
		//type
		cv.put("annoType", annoType);
		//x y
		cv.put("sX", x);
		cv.put("sY", y);
		//date
		cv.put("date", date);
		//mdate
		cv.put("modifieddate", mdate);
		//comment
		cv.put("comment", ("".equals(comment) ? null : comment));
		//userid
		cv.put("userid", userName);
		//page
		cv.put("page", page);
		//rec
		cv.put("rec", ("".equals(rec) ? null : rec));
		//txt ( 照片 )
		cv.put("pic", pic);
		cv.put("status", status[3]);
		
		//db.insert(ActionCode.ANNOTATION_IMAGE, null, cv);
		//Log.i("test", "test server image = "+db.insert(ActionCode.ANNOTATION_IMAGE, null, cv));
		return db.insert(ActionCode.ANNOTATION_IMAGE, null, cv);
	}
	
	//錄音播放( 開始時間 )
	public synchronized void insertRecordDate(int id, String annoType, String userName, String syncUserName) {
		
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv=new ContentValues();
		//id
		cv.put("rec_id", id);
		//annoType
		cv.put("annoType", annoType);
		//date
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		cv.put("startTime", sdf.format(new Date()));
		cv.put("endTime", "null");
		//userid
		cv.put("userid", userName);
		cv.put("syncUserid", syncUserName);
		//p_id ( 家長參與 )
		cv.put("p_id", parentInvolvement());
		cv.put("status", status[0]);
		
		db.insert(ActionCode.ANNOTATION_RECORD, null, cv);
	}
	//插入自行拍攝的照片(takepic表)
	public synchronized void insertTakePic(int id, String date, String modifieddate, String userid, String lesson, String pic, String picname){
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv=new ContentValues();
		cv.put("id", id);
		cv.put("date", date);
		cv.put("modifieddate", modifieddate);
		cv.put("userid", userid);
		cv.put("lesson", lesson);
		cv.put("pic", pic);
		cv.put("status", status[3]);
		cv.put("picname", picname);
		db.insert(ActionCode.ANNOTATION_TAKEPIC, null, cv);
	}
	
	//插入自行拍攝照片上的註記(takepic_anno表)
		public synchronized void insertTakePic_Anno(int id, String annoType, int sX, int sY, String date, String modifieddate, String comment, String userid, int page, String lesson, String picFilePath, String rec, String pic, int p_id){
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues cv=new ContentValues();
			cv.put("id", id);
			cv.put("annoType", annoType);
			cv.put("sX", sX);
			cv.put("sY", sY);
			cv.put("date", date);
			cv.put("modifieddate", modifieddate);
			cv.put("comment", comment);
			cv.put("userid", userid);
			cv.put("page", page);
			cv.put("lesson", lesson);
			cv.put("picFilePath", picFilePath);
			cv.put("rec", rec);
			cv.put("pic", pic);
			cv.put("p_id", p_id);
			cv.put("status", status[3]);
			db.insert(ActionCode.ANNOTATION_TAKEPIC_ANNO, null, cv);
		}
	//-----
		//
		public synchronized String[] getSubtocpicName(String username, String lesson){
			try{
				String picName[];
				SQLiteDatabase db = this.getWritableDatabase();
				Cursor cur;
				cur = db.rawQuery("Select picname from takepic WHERE userid='" + username + "' AND status!='DELETE' AND lesson='" + lesson + "' order by _id asc" , null);
				int x = cur.getCount();
				cur.moveToFirst();
				picName = new String[x];
				for(int i =0; i<x; i++){
					picName[i] = cur.getString(0);
					cur.moveToNext();
				}
				return picName;
			}
			catch(Exception e){
				Log.i("log", "test (SyncSQLiteDB) line275 = "+e);
			}
			return null;
		}
		//計算章節裡面照片數目
		public synchronized int getSubtocpicNum(String username, String lesson){
			try{
				SQLiteDatabase db = this.getWritableDatabase();
				Cursor cur;
				cur = db.rawQuery("Select * from takepic WHERE userid='" + username + "' AND status!='DELETE' AND lesson='" + lesson + "'", null);
				int x = cur.getCount();
				return x;
			}
			catch(Exception e){
				Log.i("log", "test (SyncSQLiteDB) line275 = "+e);
				return 0;
			}
		}
		//藉由所點擊的圖片名稱獲取該圖片的圖片路徑(觀看他人時)
		public synchronized String getPicFilePath(String username, String lesson , String PicClick){
			try{
				SQLiteDatabase db = this.getWritableDatabase();
				Cursor cur;
				cur = db.rawQuery("Select pic from takepic WHERE userid='" + username + "' AND status!='DELETE' AND lesson='" + lesson + "' AND picname='" + PicClick + "'" , null);
				cur.moveToFirst();
				String picFilePath = cur.getString(0);
				cur.close();
				return picFilePath;
			}
			catch(Exception e){
				Log.i("log", "test (SyncSQLiteDB) line288 = "+e);
				return null;
			}
		}
		//
	//------------------------------------------------------------------------------------------update
	//更新文字註記
	public synchronized void updateNote(int id, int r, int g, int b, String type, String date, String mdate, String comment, String userName, int page, String rec, String txt, String pic) {

		SQLiteDatabase db = this.getWritableDatabase();
		String where = "id" + " = ? AND userid = ?";
		String[] whereValue = { Integer.toString(id), userName };
		// 將修改的值放入ContentValues
		ContentValues cv = new ContentValues();
		//color
		cv.put("red", r);
		cv.put("green", g);
		cv.put("blue", b);
		//style
		cv.put("type", type);  //預設是底線
		//date
		cv.put("date", date);
		//mdate
		cv.put("modifieddate", mdate);
		//comment
		cv.put("comment", ("".equals(comment) ? null : comment));
		//userid
		cv.put("userid", userName);
		//page
		cv.put("page", page);
		//rec
		cv.put("rec", ("".equals(rec) ? null : rec));
		//txt ( 被選取的文字 )
		cv.put("txt", txt);
		//照片
		cv.put("pic", pic);
		cv.put("status", status[3]);		
		
		db.update(ActionCode.ANNOTATION_TEXT, cv, where, whereValue);
	}
	
	//更新圖片註記
	public synchronized void updatePicNote(int id, String annoType, int x, int y, String date, String mdate, String comment, String userName, int page, String rec, String pic) {

		SQLiteDatabase db = this.getWritableDatabase();
		String where = "id" + " = ? AND userid = ?";
		String[] whereValue = { Integer.toString(id), userName };
		// 將修改的值放入ContentValues
		ContentValues cv = new ContentValues();
		//type
		cv.put("annoType", annoType);
		//x y
		cv.put("sX", x);
		cv.put("sY", y);
		//date
		cv.put("date", date);
		//mdate
		cv.put("modifieddate", mdate);
		//comment
		cv.put("comment", ("".equals(comment) ? null : comment));
		//userid
		cv.put("userid", userName);
		//page
		cv.put("page", page);
		//rec
		cv.put("rec", ("".equals(rec) ? null : rec));
		//照片
		cv.put("pic", pic);
		cv.put("status", status[3]);	
		//Log.i("test", "test result = "+id+" "+annoType+" "+x+" "+y+" "+date+" "+mdate+" "+comment+" "+userName+" "+page+" "+rec+" ");
		
		db.update(ActionCode.ANNOTATION_IMAGE, cv, where, whereValue);
	}	
	//更新自行拍攝的圖片表(Takepic)
	public synchronized void updateTakePic(int id, String date, String modifieddate, String userid, String lesson, String pic, String picname){
		SQLiteDatabase db = this.getWritableDatabase();
		String where = "id" + " = ? AND userid = ?";
		String[] whereValue = { Integer.toString(id), userid };
		// 將修改的值放入ContentValues
		ContentValues cv = new ContentValues();
		cv.put("id", id);
		cv.put("pic", pic);
		cv.put("status", status[3]);	
		cv.put("date", date);
		cv.put("modifieddate", modifieddate);
		cv.put("userid", userid);
		cv.put("lesson", lesson);
		cv.put("picname", picname);
		//Log.i("test", "test result = "+id+" "+annoType+" "+x+" "+y+" "+date+" "+mdate+" "+comment+" "+userName+" "+page+" "+rec+" ");
		
		db.update(ActionCode.ANNOTATION_TAKEPIC, cv, where, whereValue);
	}
	//更新自行拍攝圖片上的註記(Takepic_anno)
	public synchronized void updateTakePic_Anno(int id, String annoType, int sX, int sY, String date, String modifieddate, String comment, String userid, int page, String lesson, String picFilePath, String rec, String pic, int p_id){
		SQLiteDatabase db = this.getWritableDatabase();
		String where = "id" + " = ? AND userid = ?";
		String[] whereValue = { Integer.toString(id), userid };
		// 將修改的值放入ContentValues
		ContentValues cv = new ContentValues();
		cv.put("id", id);
		cv.put("annoType", annoType);
		cv.put("sX", sX);	
		cv.put("sY", sY);
		cv.put("date", date);
		cv.put("modifieddate", modifieddate);
		cv.put("comment", comment);
		cv.put("userid", userid);
		cv.put("page", page);
		cv.put("lesson", lesson);
		cv.put("picFilePath", picFilePath);
		cv.put("rec", rec);
		cv.put("pic", pic);
		cv.put("p_id", p_id);
		cv.put("status", status[3]);
		//Log.i("test", "test result = "+id+" "+annoType+" "+x+" "+y+" "+date+" "+mdate+" "+comment+" "+userName+" "+page+" "+rec+" ");
		
		db.update(ActionCode.ANNOTATION_TAKEPIC_ANNO, cv, where, whereValue);
	}
	
	//假刪除( 更改狀態 )
	public synchronized void updateStatus(int id, String userName, String tableName) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where;
		if( !ActionCode.ANNOTATION_TEXT_RANGE.equals(tableName) )
			where = "id" + " = ? AND userid = ?";
		else
			where = "t_id" + " = ? AND userid = ?";
		String[] whereValue = { Integer.toString(id), userName };
		/* 將修改的值放入ContentValues */
		ContentValues cv = new ContentValues();
		cv.put("status", status[2]);
		db.update(tableName, cv, where, whereValue);
	}		
	
	//更新修改時間
	public synchronized void updateModDate(int id, String TableName) {
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		SQLiteDatabase db = this.getWritableDatabase();
		String where = "_id" + " = ?";
		String[] whereValue = { Integer.toString(id) };
		// 將修改的值放入ContentValues
		ContentValues cv = new ContentValues();
		cv.put("modifieddate", sdf.format(new Date()));
		
		ContentValues cv_pid = new ContentValues();
		
		//狀態
		ContentValues cv_status = new ContentValues();
		cv_status.put("status", status[1]);
		
		db.update(TableName, cv, where, whereValue);
		db.update(TableName, cv_pid, where, whereValue);
		db.update(TableName, cv_status, where, whereValue);
	}
	
	//更新文字內容
	public synchronized void updateComment(int id, String text, String TableName) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = "_id" + " = ?";
		String[] whereValue = { Integer.toString(id) };
		// 將修改的值放入ContentValues
		ContentValues cv = new ContentValues();
		cv.put("comment", text);
		
		ContentValues cv_pid = new ContentValues();
		
		//狀態
		ContentValues cv_status = new ContentValues();
		cv_status.put("status", status[1]);
		
		db.update(TableName, cv, where, whereValue);
		db.update(TableName, cv_pid, where, whereValue);
		db.update(TableName, cv_status, where, whereValue);
	}
	
	//更新錄音
	public synchronized void updateRecord(int id, String text, String TableName) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = "_id" + " = ?";
		String[] whereValue = { Integer.toString(id) };
		/* 將修改的值放入ContentValues */
		ContentValues cv = new ContentValues();
		cv.put("rec", text);
		
		ContentValues cv_pid = new ContentValues();
		
		//狀態
		ContentValues cv_status = new ContentValues();
		cv_status.put("status", status[1]);
		
		db.update(TableName, cv, where, whereValue);
		db.update(TableName, cv_pid, where, whereValue);
		db.update(TableName, cv_status, where, whereValue);
	}
	
	public synchronized void updateColor(int id, int r, int g, int b, String TableName) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = "_id" + " = ?";
		String[] whereValue = { Integer.toString(id) };
		/* 將修改的值放入ContentValues */
		ContentValues cv = new ContentValues();
		cv.put("red", r);
		cv.put("green", g);
		cv.put("blue", b);

		ContentValues cv_pid = new ContentValues();;
		
		//狀態
		ContentValues cv_status = new ContentValues();
		cv_status.put("status", status[1]);
				
		db.update(TableName, cv, where, whereValue);
		db.update(TableName, cv_pid, where, whereValue);
		db.update(TableName, cv_status, where, whereValue);
	}
	
	public synchronized void updateStyle(int id, int style, String TableName) {
		//style = 0( 底色 )  1( 底線 ) 2( 刪除線 )
		SQLiteDatabase db = this.getWritableDatabase();
		String where = "_id" + " = ?";
		String[] whereValue = { Integer.toString(id) };
		/* 將修改的值放入ContentValues */
		ContentValues cv = new ContentValues();
		cv.put("type", style);

		ContentValues cv_pid = new ContentValues();
		
		//狀態
		ContentValues cv_status = new ContentValues();
		cv_status.put("status", status[1]);
		
		db.update(TableName, cv, where, whereValue);
		db.update(TableName, cv_pid, where, whereValue);
		db.update(TableName, cv_status, where, whereValue);
	}
	
	public synchronized void updateTranslateNum(int id, int num) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = "_id" + " = ?";
		String[] whereValue = { Integer.toString(id) };
		/* 將修改的值放入ContentValues */
		ContentValues cv = new ContentValues();
		cv.put("num", num);

		ContentValues cv_pid = new ContentValues();
		
		//狀態
		ContentValues cv_status = new ContentValues();
		cv_status.put("status", status[1]);
		
		db.update(ActionCode.ANNOTATION_TRANSLATION, cv, where, whereValue);
		db.update(ActionCode.ANNOTATION_TRANSLATION, cv_pid, where, whereValue);
		db.update(ActionCode.ANNOTATION_TRANSLATION, cv_status, where, whereValue);
		
	}
	
	//紀錄家長參與結束時間
	public synchronized void updateParentInTime(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = "_id" + " = ?";
		String[] whereValue = { Integer.toString(id) };
		/* 將修改的值放入ContentValues */
		ContentValues cv = new ContentValues();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		cv.put("endTime", sdf.format(new Date()));
		
		//狀態
		ContentValues cv_status = new ContentValues();
		cv_status.put("status", status[1]);
		
		db.update(ActionCode.ANNOTATION_PARENTIN_TIME, cv, where, whereValue);
		db.update(ActionCode.ANNOTATION_PARENTIN_TIME, cv_status, where, whereValue);
	}
	
	//錄音播放( 結束時間 )
	public synchronized void updateRecordDateEnd(int id, String tableName) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = "_id" + " = ?";
		String[] whereValue = { Integer.toString(id) };
		/* 將修改的值放入ContentValues */
		ContentValues cv = new ContentValues();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		cv.put("endTime", sdf.format(new Date()));
		
		//狀態
		ContentValues cv_status = new ContentValues();
		cv_status.put("status", status[1]);
		
		db.update(tableName, cv, where, whereValue);
		db.update(tableName, cv_status, where, whereValue);
	}	
	
	//假刪除( 更改狀態 )
	public synchronized void updateStatus(int id, String field, String tableName, int  statusId) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = field + " = ?";
		String[] whereValue = { Integer.toString(id) };
		/* 將修改的值放入ContentValues */
		ContentValues cv = new ContentValues();
		cv.put("status", status[statusId]);
		db.update(tableName, cv, where, whereValue);
	}
	
	//( 更改狀態 需比較狀態 同步需要 )
	public synchronized int updateStatus(int id, String field, String tableName, int  statusId, String statusNow) {
		///494
		if( !statusNow.equals(getStrComment(id, "status", tableName)) )
			return 0;
		SQLiteDatabase db = this.getWritableDatabase();
		String where = field + " = ?";
		String[] whereValue = { Integer.toString(id) };
		/* 將修改的值放入ContentValues */
		ContentValues cv = new ContentValues();
		cv.put("status", status[statusId]);
		return db.update(tableName, cv, where, whereValue);
	}
	
	//更新同步時間點 ( 選擇使用者，並非_id )
	public synchronized int updateSyncData(String id, String field, String tableName) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = field + " = ?";
		String[] whereValue = { id };
		/* 將修改的值放入ContentValues */
		ContentValues cv = new ContentValues();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		cv.put("datetime", sdf.format(new Date()));
		
		return db.update(tableName, cv, where, whereValue);
	}	
	
	//------------------------------------------------------------------------------------------get
	//取得註記位置(Range) 透過頁數 20120304 新增狀態
	public synchronized int[] getRangePage(String location, String tablename, int page, String user) {
		int num_arr[];
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			//Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE page ='" + page  + "' AND userid='" + user +"' ", null);
			Cursor Cur;
			if( ActionCode.ANNOTATION_IMAGE.equals(tablename) ) //如果是偵測圖片註記數量
				Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE page ='" + page  + "' AND userid='" + user + "' AND status!='DELETE' AND status!='DELETEOK' AND annoType != 'PARENTS' ", null);
			else
				Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE page ='" + page  + "' AND userid='" + user + "' AND status!='DELETE' AND status!='DELETEOK'", null);
			//此頁的註記總數
			int num = Cur.getCount();
			Cur.moveToFirst();
			//宣告
			num_arr = new int[num];

			for( int i=0;num!=0;i++ ) {
				//只能是0
				num_arr[i] = Cur.getInt(0);
				//欄位向前
				Cur.moveToNext();
				num--;
			}
			if(Cur != null)
				Cur.close();

			return num_arr;
			
		} catch (Exception e) {
			Log.i("log","test (SyncDB) line534 = "+e);
		}

		return null;
	}
	
	//取得註記位置(Range) //不辨認UserID
	public synchronized int[] getRangePage(String location, String tablename, String status) {
		int num_arr[];
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			//Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE userid='" + user +"' ", null);
			Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE status='"+ status +"' ", null);
			//此頁的註記總數
			int num = Cur.getCount();
			Cur.moveToFirst();
			//宣告
			num_arr = new int[num];

			for( int i=0;num!=0;i++ ) {
				//只能是0
				num_arr[i] = Cur.getInt(0);
				//欄位向前
				Cur.moveToNext();
				num--;
			}
			if(Cur != null)
				Cur.close();

			return num_arr;
			
		} catch (Exception e) {
			Log.i("log","test (SyncDB) line564 = "+e);
		}

		return null;
	}		
	
	//取得註記位置(Range) 
	public synchronized int[] getRangePage(String location, String tablename, String user, String status) {
		int num_arr[];
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			//Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE userid='" + user +"' ", null);
			//Log.i("test","test str 222= "+"Select "+ location +" from "+ tablename +" WHERE userid='" + user +"' AND status='"+ status +"' ");
			Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE userid='" + user +"' AND status='"+ status +"' ", null);
			//Log.i("test","test str 222= "+Cur.getCount());
			//此頁的註記總數
			int num = Cur.getCount();
			Cur.moveToFirst();
			//宣告
			num_arr = new int[num];

			for( int i=0;num!=0;i++ ) {
				//只能是0
				num_arr[i] = Cur.getInt(0);
				//Log.i("test","test str 222= "+num_arr[i]);
				//欄位向前
				Cur.moveToNext();
				num--;
			}
			if(Cur != null)
				Cur.close();

			return num_arr;
			
		} catch (Exception e) {
			Log.i("log","test (SyncDB) line594 = "+e);
		}

		return null;
	}	
	
	//取得註記位置(Range)
	public synchronized int getRange(int id, String location, String tablename, int num, String userid) {
		int loc = -1;
		try {
//			Log.i("test", "test getRanage = "+id);
//			Log.i("test", "test getRanage = "+location);
//			Log.i("test", "test getRanage = "+tablename);
//			Log.i("test", "test getRanage = "+num);
//			Log.i("test", "test getRanage = "+userid);
//			Log.i("test", "test sql = "+"Select "+ location +" from "+ tablename +" WHERE t_id='" + id + "' AND userid ='" + userid + "'");
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE t_id='" + id + "' AND userid ='" + userid + "'", null);
			//"Select * from " + TableName + " WHERE _id='" + id + "'"
			Cur.moveToFirst();
			while( num != 0 ) {
				Cur.moveToNext();
				num--;
			}
			Cur.moveToFirst();
			loc = Cur.getInt(0);
			if(Cur != null)
				Cur.close();
			
		} catch (Exception e) {
			Log.i("log","test (SyncDB) line617 = "+e+"  (getRange)");
		}

		return loc;
	}
	
	//取得註記範圍(Range)(有幾行)
	public synchronized int getNoteRange(int id, String location, String tablename) {
		int num = -1;
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE _id='" + id + "'", null);
			Cur.moveToFirst();
			num = Cur.getCount();
			if(Cur != null)
				Cur.close();
		} catch (Exception e) {
			Log.i("log","test (SyncDB) line633 = "+e);
		}

		return num;
	}
	
	//取得註記範圍(Range)(有幾行)( 分辨使用者 )
	public synchronized int getNoteRange(int id, String location, String tablename, String userName) {
		int num = -1;
		//檢查是否為同一使用者
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select * from "+ActionCode.ANNOTATION_TEXT+" WHERE userid='" + userName +"' ", null);
			Cur.moveToFirst();
			num = Cur.getCount();
			if(Cur != null)
				Cur.close();
		} catch (Exception e) {
			Log.i("log","test (SyncDB) line650 = "+e);
			return num;
		}
		
		//執行取得註記範圍(Range)(有幾行)
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE t_id='" + id + "' ", null);
			//Log.i("test", "test sql = "+"Select "+ location +" from "+ tablename +" WHERE t_id='" + id + "' ");
			Cur.moveToFirst();
			num = Cur.getCount();
			if(Cur != null)
				Cur.close();
		} catch (Exception e) {
			Log.i("log","test (SyncDB) line663 = "+e);
		}

		return num;
	}	
	
	//取得INT的資訊
	public synchronized int getIntData(int id, String location, String tablename) {
		int loc = -1;
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE _id='" + id + "'", null);
			//"Select * from " + TableName + " WHERE _id='" + id + "'"
			Cur.moveToFirst();
			loc = Cur.getInt(0);
			if(Cur != null)
				Cur.close();
		} catch (Exception e) {
			Log.i("log","test (SyncDB) line680 = "+e);
		}

		return loc;
	}
	
	//取得INT的資訊( 分辨使用者 ) pic
	public synchronized int getIntData(int id, String location, String tablename, String userName) {
		int loc = -1;
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE _id='" + id + "' AND userid='" + userName +"' ", null);
			//"Select * from " + TableName + " WHERE _id='" + id + "'"
			Cur.moveToFirst();
			loc = Cur.getInt(0);
			if(Cur != null)
				Cur.close();
		} catch (Exception e) {
			Log.i("log","test (SyncDB) line697 = "+e);
			return 0;
		}

		return loc;
	}
	
	//取得INT的資訊( 分辨使用者 ) text
	public synchronized int getIntData_text(int id, String location, String tablename, String userName) {
		int loc = -1;
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE id='" + id + "' AND userid='" + userName +"' ", null);
			//"Select * from " + TableName + " WHERE _id='" + id + "'"
			Cur.moveToFirst();
			loc = Cur.getInt(0);
			if(Cur != null)
				Cur.close();
		} catch (Exception e) {
			Log.i("log","test (SyncDB) line6970 = "+e);
			return 0;
		}

		return loc;
	}		
	
	//取得字串
	public synchronized String getStrComment(int id, String location, String tablename) {
		String str = null;
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE _id='" + id + "'", null);
			Cur.moveToFirst();
			if( Cur.getCount() > 0 )
				str = Cur.getString(0);
			if(Cur != null)
				Cur.close();
			return str;
			
		} catch (Exception e) {
			Log.i("log","test (SyncDB) line717 = "+e);
		}
		return str;
	}
	
	//取得字串( 分辨使用者 ) pic
	public synchronized String getStrComment(int id, String location, String tablename, String userName) {
		String str = null;
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE _id='" + id + "' AND userid='" + userName +"' ", null);
			Cur.moveToFirst();
			if( Cur.getCount() > 0 )
				str = Cur.getString(0);
			if(Cur != null)
				Cur.close();
			return str;
		} catch (Exception e) {
			Log.i("log","test (SyncDB) line734 = "+e);
		}
		return str;
	}
	
	//取得字串( 分辨使用者 ) text
	public synchronized String getStrComment_text(int id, String location, String tablename, String userName) {
		String str = null;
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE id='" + id + "' AND userid='" + userName +"' ", null);
			Cur.moveToFirst();
			if( Cur.getCount() > 0 )
				str = Cur.getString(0);
			if(Cur != null)
				Cur.close();
			return str;
		} catch (Exception e) {
			Log.i("log","test (SyncDB) line734 = "+e);
		}
		return str;
	}		
	
	//取得字串( 分辨使用者,不用ID )
	public synchronized String getStrComment(String location, String tablename, String userName) {
		String str = null;
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE userid='" + userName +"' ", null);
			Cur.moveToFirst();
			if( Cur.getCount() > 0 )
				str = Cur.getString(0);
			if(Cur != null)
				Cur.close();
			return str;
		} catch (Exception e) {
			Log.i("log","test (SyncDB) line751 = "+e);
		}
		return str;
	}		
	
	//取得辭典按的次數 (未註記)
	public synchronized int getTranslateNum(String tableName, String text) {
		
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor cur = database.rawQuery("Select num from "+ tableName +" WHERE selectedText='" + text + "' ", null);
			cur.moveToFirst();
			int num = cur.getInt(0);
			if(cur != null)
				cur.close();
			return num;
			
		} catch (Exception e) {
			Log.i("log","test (SyncDB) line768 = "+e);
			return 0;
		}
	}
	
	//取得辭典ID (未註記)
	public synchronized int getTranslateId(String tableName, String text) {
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor cur = database.rawQuery("Select _id from "+ tableName +" WHERE selectedText='" + text + "' ", null);
			cur.moveToFirst();
			int num = cur.getInt(0);
			if(cur != null)
				cur.close();
			return num;
			
		} catch (Exception e) {
			Log.i("log","test (SyncDB) line783 = "+e);
			return 0;
		}
	}	
	
	//取得辭典按的次數 (註記)
	public synchronized int getTranslateNum(String tableName, int id) {
		
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor cur = database.rawQuery("Select num from "+ tableName +" WHERE t_id='" + id + "' ", null);
			cur.moveToFirst();
			int num = cur.getInt(0);
			if(cur != null)
				cur.close();
			return num;
			
		} catch (Exception e) {
			Log.i("log","test (SyncDB) line799 = "+e);
			return 0;
		}
	}	
	
	//取得辭典ID (有註記)
	public synchronized int getTranslateId(String tableName, int index) {
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor cur = database.rawQuery("Select _id from "+ tableName +" WHERE t_id='" + index + "' ", null);
			cur.moveToFirst();
			int num = cur.getInt(0);
			if(cur != null)
				cur.close();
			return num;
			
		} catch (Exception e) {
			Log.i("log","test (SyncDB) line814 = "+e);
			return 0;
		}
		
	}	
	
	public synchronized int getTTStimes(int id) {
		int times = 0;
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select t_id from "+ActionCode.ANNOTATION_TEXT+" WHERE _id='" + id + "'", null);
			
			//"Select * from " + TableName + " WHERE _id='" + id + "'"
			Cur.moveToFirst();
			times = Cur.getInt(0);
			if(Cur != null)
				Cur.close();
			
		} catch (Exception e) {
			Log.i("log","test(SQLiteDB) line830 = "+e);
		}
		
		return times;
	}
	
	public synchronized String getRecord(int id, String field, String tableName) {
		String name = null;
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select "+ field +" from "+ tableName +" WHERE _id='" + id + "'", null);
			
			//"Select * from " + TableName + " WHERE _id='" + id + "'"
			Cur.moveToFirst();
			name = Cur.getString(0);
			if(Cur != null)
				Cur.close();
			
		} catch (Exception e) {
			Log.i("log","test(SQLiteDB) line847 = "+e);
		}
		
		return name;
	}
	
	public synchronized String getRecord_text(int id, String field, String tableName, String userid) {
		String name = null;
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select "+ field +" from "+ tableName +" WHERE id='" + id + "' AND userid='" + userid +"'", null);
			
			//"Select * from " + TableName + " WHERE _id='" + id + "'"
			Cur.moveToFirst();
			name = Cur.getString(0);
			if(Cur != null)
				Cur.close();
			
		} catch (Exception e) {
			Log.i("log","test(SQLiteDB) line847 = "+e);
		}
		
		return name;
	}
	
	//------------------------------------------------------------------------------------------get

	//總total
	public synchronized int getTableCount(String TableName) {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cur = db.rawQuery("Select * from " + TableName, null);
		int x = cur.getCount();
		cur.close();
		return x;
	}
	
	//getRecord
	public synchronized int getRecordID(int rec_id, String annoType) {
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor cur = db.rawQuery("Select _id from " + ActionCode.ANNOTATION_RECORD + " WHERE rec_id='" + rec_id + "' AND annoType='" + annoType + "' AND endTime='null' ", null);
			cur.moveToFirst();
			int x = cur.getInt(0);
			cur.close();
			return x;
		} catch( Exception e ) {
			Log.i("log", "test (SQLiteDB) line875 = "+e);
			return 0;
		}
	
	}	
	
	//總total( 分辨使用者 )
	public synchronized int getTableCount(String TableName, String userName) {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cur = db.rawQuery("Select * from " + TableName + " WHERE userid='" + userName + "'", null);
		int x = cur.getCount();
		cur.close();
		return x;
	}
	
	//總total( 計算目前使用者的註記數量,排除老師講解 ) status代表不能顯示的狀態
	public synchronized int getTableCount(String TableName, String userName, String status) {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cur;
		if( ActionCode.ANNOTATION_IMAGE.equals(TableName) ) //如果是偵測圖片註記數量
			cur = db.rawQuery("Select * from " + TableName + " WHERE userid='" + userName + "' AND status NOT LIKE '%"+status+"%' AND annoType != 'LECTURE' ", null);
		else
			cur = db.rawQuery("Select * from " + TableName + " WHERE userid='" + userName + "' AND status NOT LIKE '%"+status+"%' ", null);
		
		int x = cur.getCount();
		cur.close();
		return x;
	}
	
	//總total( 計算目前使用者的錄音數量,排除老師講解 ) status代表不能顯示的狀態
	public synchronized int getVoiceCount(String userName, String status) {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cur_text = db.rawQuery("Select * from annotext WHERE userid='" + userName + "' AND status NOT LIKE '%"+status+"%' AND rec != 'null'", null);
		int x = cur_text.getCount();
		Cursor cur_image = db.rawQuery("Select * from annoimage WHERE userid='" + userName + "' AND status NOT LIKE '%"+status+"%' AND rec LIKE '%picNote%'", null);
		x += cur_image.getCount();
		
		cur_text.close();
		cur_image.close();
		return x;
	}
	//總Anno (picFilePath) 2016/3/21
	public synchronized int getAnnoCount(String picFilePath, String userName){
		try{
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor cur;
			cur = db.rawQuery("Select * from takepic_anno WHERE picFilePath='" + picFilePath + "'AND userid='" + userName +"' AND status!='DELETE' AND status!='DELETEOK'", null);
			int x = cur.getCount();
			cur.close();
			return x;
		}
		catch(Exception e){
			return 0;
		}
	}
	//總total( 頁數 and 使用者 ) 20120304 新增狀態
	public synchronized int getTableCount(String TableName, String userName, int page) {
		
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			//Cursor cur = db.rawQuery("Select * from " + TableName + " WHERE userid='" + userName + "' AND page='" + page + "' ", null);
			Cursor cur;
			if( ActionCode.ANNOTATION_IMAGE.equals(TableName) ) //如果是偵測圖片註記數量
				cur = db.rawQuery("Select * from " + TableName + " WHERE userid='" + userName + "' AND page='" + page + "' AND status!='DELETE' AND status!='DELETEOK' AND annoType != 'PARENTS' ", null);
			else
				cur = db.rawQuery("Select * from " + TableName + " WHERE userid='" + userName + "' AND page='" + page + "' AND status!='DELETE' AND status!='DELETEOK'", null);
			int x = cur.getCount();
			cur.close();
			//Log.i("log", "test log = "+x);
			return x;
		}
		catch (Exception e) {
			Log.i("log", "test (SQLiteDB) line886 = "+e);
			return 0;
		}
	}
	
	//總total( 頁數 and 使用者 and 類型 )
	public synchronized int getTableCount(String TableName, String type, String userName, int page) {
		
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor cur = db.rawQuery("Select * from " + TableName + " WHERE userid='" + userName + "' AND page='" + page + "' AND annoType='" + type +"' ", null);
			int x = cur.getCount();
			cur.close();
			//Log.i("log", "test log = "+x);
			return x;
		}
		catch (Exception e) {
			Log.i("log", "test SQLiteDB line903 = "+e);
			return 0;
		}
	}
	
	//total( 計算在一定頁數使用者的註記數量,排除老師講解 ) status代表不能顯示的狀態
	public synchronized int getTableCount(String TableName, String userName, int from, int to, String status) {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cur;
		if( ActionCode.ANNOTATION_IMAGE.equals(TableName) ) //如果是偵測圖片註記數量
			cur = db.rawQuery("Select * from " + TableName + " WHERE page >= " + from + " AND page < "+ to +" AND userid='" + userName + "' AND status NOT LIKE '%"+status+"%' AND annoType != 'LECTURE' ", null);
		else
			cur = db.rawQuery("Select * from " + TableName + " WHERE page >= " + from + " AND page < "+ to +" AND userid='" + userName + "' AND status NOT LIKE '%"+status+"%' ", null);
		
		int x = cur.getCount();
		cur.close();
		return x;
	}
	
	//total( 計算在一定頁數使用者的錄音數量,排除老師講解 ) status代表不能顯示的狀態
	public synchronized int getVoiceCount(String userName, int from, int to, String status) {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cur_text = db.rawQuery("Select * from annotext WHERE page >= " + from + " AND page < "+ to +" AND userid='" + userName + "' AND status NOT LIKE '%"+status+"%' AND rec != 'null'", null);
		int x = cur_text.getCount();
		Cursor cur_image = db.rawQuery("Select * from annoimage WHERE page >= " + from + " AND page < "+ to +" AND userid='" + userName + "' AND status NOT LIKE '%"+status+"%' AND rec LIKE '%picNote%'", null);
		x += cur_image.getCount();
		
		cur_text.close();
		cur_image.close();
		return x;
	}	
	
	//get _ID ( 取最大ID )
	public synchronized int getMaxID(String TableName) {
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor cur = db.rawQuery("Select _id from " + TableName + " order by _id desc limit 0, 1", null);
			cur.moveToFirst();
			int num = cur.getInt(0);
			cur.close();
			return num;
		}
		catch (Exception e) {
			Log.i("log", "test SQLiteDB line919 = "+e);
			return 0;
		}
		
	}
	
	//get 全部數值
	public synchronized String[] getAllData(String tableName, int id, int num) {
		String str_arr[];
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select * from record WHERE _id='" + id +"' ", null);
			//此頁的註記總數
			Cur.moveToFirst();
			
			/*
			//宣告
			str_arr = new String[num];
			*/
			
			//宣告
			str_arr = new String[num+1];
			//新增一筆資料到str_arr
			str_arr[num] = tableName; //紀錄是哪個Table
			
			for( int i=0;num!=0;i++ ) {
				//只能是0
				str_arr[i] = Cur.getString(i);
				//欄位向前
				//Cur.moveToNext();
				num--;
			}
			if(Cur != null)
				Cur.close();

			return str_arr;
			
		} catch (Exception e) {
			Log.i("log","test (SyncDB) line957 = "+e);
		}

		return null;
	}
	//
	//取得特定圖片上使用者的所有註記資料 (回傳註記資料的ID)
	public synchronized int[] getTakepic_Anno(String location, String tablename, String user, String lesson) {
		int num_arr[]; //以陣列形式儲存該頁上屬於某使用者的所有註記資料
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor cur;
			cur = database.rawQuery("Select "+ location +" from "+ tablename + " WHERE userid='" + user + "' AND picFilePath='" + SaveValue.picNow + "' AND status !='DELETE' AND status !='DELETEOK'", null);
			//此頁的註記總數
			int num = cur.getCount();
			cur.moveToFirst();
			//宣告
			num_arr = new int[num];
			for( int i=0;num!=0;i++ ) {
				//只能是0
				num_arr[i] = cur.getInt(0);
				//欄位向前
				cur.moveToNext();
				num--;
			}
			if(cur != null)
				cur.close();
			return num_arr;	
		} catch (Exception e) {
			Log.i("log","test (SQLiteDB) line1542 = "+e);
		}
		return null;
	}
	//取得takepic_anno的註記位置 
	public synchronized int getTakepic_anno_Pos(String location, String TableName, String Username, int id){
		int loc = -1;
		if(Username != null){
			try{
				SQLiteDatabase db = this.getWritableDatabase();
				Cursor cur;
				cur = db.rawQuery("Select " + location + " from " + TableName + " WHERE userid='" + Username + "' AND picFilePath='" + SaveValue.picNow + "' AND _id='" + id + "' AND status !='DELETE' AND status !='DELETEOK'", null);
				cur.moveToFirst();
				loc = cur.getInt(0);
				if(cur != null){
					cur.close();
				}
			}
			catch(Exception e){
				Log.i("log","error occur when get the takepic_anno data(Sync)");
			}
		}else{
			Log.i("log","error occur when get the takepic_anno data(Sync)");
		}
		return loc;
	}
	//計算其他使用者在拍攝的照片上註記的數量
	public synchronized int getTakepic_anno_Num_other(String TableName, String username, String lesson){
		try{
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor cur;
			cur = db.rawQuery("Select * from " + TableName + " WHERE userid='" + username + "' AND lesson='" + lesson + "' AND picFilePath ='" + SaveValue.picNow + "' AND status !='DELETE' AND status !='DELETEOK'", null);
			int x = cur.getCount();
			return x;
		}
		catch(Exception e){
			Log.i("log","test (syncSQLiteDB) line1527 = "+e);
			return 0;
		}
	};
}
