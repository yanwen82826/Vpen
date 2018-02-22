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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.LinkedList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import org.geometerplus.fbreader.library.BooksDatabase;
import org.geometerplus.zlibrary.core.config.ZLConfig;
import org.geometerplus.zlibrary.text.view.SaveValue;

public final class UploadDatabase extends SQLiteOpenHelper {
	
	private final static String DATABASE_NAME = "FBReader_note.db";
	private final static int DATABASE_VERSION = 1;
	private final static String TABLE_NAME = "annotext";
	public final static String FIELD_id = "_id";

	public UploadDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	//test
	private static UploadDatabase ourInstance;

	public static UploadDatabase Instance() {
		return ourInstance;
	}
	//test

	@Override
	public void onCreate(SQLiteDatabase sqldb) {
		//��r���O--------------------------------------------------------------------------------------------------------------------------------------
		sqldb.execSQL("CREATE TABLE annotext (_id INTEGER PRIMARY KEY, draw_id INT, red INT, green INT, blue INT, type INT," +
				"date TEXT, comment TEXT, userid TEXT, page INT, rec TEXT, rec_id INT, tts_id INT, txt TEXT, p_id INT)");
		//��r���O---�d��
		sqldb.execSQL("CREATE TABLE drawRange (_id INTEGER PRIMARY KEY, draw_id INT, left INT, top INT, right INT, bottom INT, p_id INT)");
		//��r���O---�����������
		sqldb.execSQL("CREATE TABLE recordtable (_id INTEGER PRIMARY KEY, rec_id INT, datetime TEXT, datetime_end TEXT, p_id INT)");
		//��r���O---TTS����
		sqldb.execSQL("CREATE TABLE ttstable (_id INTEGER PRIMARY KEY, tts_id INT, datetime TEXT, p_id INT)");
		//��r���O--------------------------------------------------------------------------------------------------------------------------------------

		//�Ϥ����O--------------------------------------------------------------------------------------------------------------------------------------
		sqldb.execSQL("CREATE TABLE annoimage (_id INTEGER PRIMARY KEY, draw_id INT, sX INT, sY INT, mdate TEXT, comment TEXT," +
						"userid TEXT, page INT, mrec TEXT, mrec_id INT, p_id INT)");
		//�Ϥ����O---�����������
		sqldb.execSQL("CREATE TABLE recordtableimage (_id INTEGER PRIMARY KEY, rec_id INT, datetime TEXT, datetime_end TEXT, p_id INT)");
		//�Ϥ����O--------------------------------------------------------------------------------------------------------------------------------------

		//�Ѯv���ѵ��O
		sqldb.execSQL("CREATE TABLE lecture (_id INTEGER PRIMARY KEY ,lec_id TEXT, userid TEXT, page TEXT, rec TEXT, p_id INT)");
		//�Ѯv���ѵ��O---�����n������
		sqldb.execSQL("CREATE TABLE lecturetable (_id INTEGER PRIMARY KEY, rec_id INT, datetime TEXT, datetime_end TEXT, p_id INT)");

		//������
		sqldb.execSQL("CREATE TABLE translation (_id INTEGER PRIMARY KEY ,t_id INT, selected_text TEXT, num INT, userid TEXT, page TEXT, IsNote INT, p_id INT)");
		//������----�����C���I��ɶ�
		sqldb.execSQL("CREATE TABLE translation_num (_id INTEGER PRIMARY KEY ,t_id INT, datetime TEXT, p_id INT)");

		//�����a���ѻP�ɶ�
		sqldb.execSQL("CREATE TABLE parentIn_time (_id INTEGER PRIMARY KEY ,p_id INT, starttime TEXT, endtime TEXT, userid TEXT)");

		sqldb.execSQL("CREATE TABLE user (_id INTEGER PRIMARY KEY ,username TEXT,password TEXT)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
		db.execSQL(sql);
		onCreate(db);
	}
	
	public long CreateUser() {
		SQLiteDatabase db = this.getWritableDatabase();
		/* �N�s�W���ȩ�JContentValues */
		ContentValues cv = new ContentValues();
		cv.put("username", "stu001");
		cv.put("password", "stu001");
		long row = db.insert("user", null, cv);
		return row;
	}
	
	public Cursor select() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null,
				null, null);
		return cursor;
	}
	
	//------------------------------------------------------------------------------------------delete
	public void delete(int id, String field, String TableName) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = field + " = ?";
		String[] whereValue = { Integer.toString(id) };
		long result = db.delete(TableName, where, whereValue);
	}
	
	public synchronized int parentInvolvement() {
		if( SaveValue.IsParent ) {
			int num = getTableCount("parentIn_time");  //���O�Ӽ�
			return num;
		}
		return 0;
	}
	
	//------------------------------------------------------------------------------------------insert
	//�s�W���O��m�d��( drawRange )
	public synchronized void insertRange(int id, int left, int top, int right, int bottom) {
		
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv=new ContentValues();
		//id
		cv.put("draw_id", id);
		//l t r b
		cv.put("left", left);
		cv.put("top", top);
		cv.put("right", right);
		cv.put("bottom", bottom);
		cv.put("p_id", parentInvolvement());
		long result = db.insert("drawRange", null, cv);
		//Log.i("log","test (SQLiteDB) insertRangeResult = "+result);

	}
	
	//�s�W���O��m( annotext )
	public synchronized void insertNote(int draw_id, int r, int g, int b, int page, String txt, String userName) {
		
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv=new ContentValues();

		cv=new ContentValues();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//id
		cv.put("draw_id", draw_id);
		//color
		cv.put("red", r);
		cv.put("green", g);
		cv.put("blue", b);
		//style
		cv.put("type", 0);  //�w�]�O���u
		//date
		cv.put("date", sdf.format(new Date()));
		//comment
		//cv.put("comment", " ");
		//userid
		cv.put("userid", userName);
		//page
		cv.put("page", page);
		//rec
		//cv.put("rec", " ");
		//rec_id
		cv.put("rec_id", draw_id);
		//tts_id
		cv.put("tts_id", draw_id);
		//txt ( �Q�������r )
		cv.put("txt", txt);
		//p_id ( �a���ѻP )
		cv.put("p_id", parentInvolvement());
		
		long result = db.insert("annotext", null, cv);
		//Log.i("log","test (SQLiteDB) insertNoteResult = "+result);

	}
	
	//�s�W���O��m( annoimage )
	public synchronized void insertPicNote(int id, int x, int y, int page, String userName) {
		
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv=new ContentValues();

		cv=new ContentValues();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//id
		cv.put("draw_id", id);
		//x y
		cv.put("sX", x);
		cv.put("sY", y);
		//date
		cv.put("mdate", sdf.format(new Date()));
		//comment
		//cv.put("comment", " ");
		//userid
		cv.put("userid", userName);
		//page
		cv.put("page", page);
		//mrec
		//cv.put("mrec", " ");
		//mrec_id
		cv.put("mrec_id", id);
		//p_id ( �a���ѻP )
		cv.put("p_id", parentInvolvement());
		long result = db.insert("annoimage", null, cv);
		//Log.i("log","test (SQLiteDB) insertNoteResult = "+result);
	}
	
	//�s�W�Ѯv���ѵ��O��m( lecture )
	public synchronized void insertLecture(int id, String userName, int page, String fileName) {
		
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv=new ContentValues();

		cv=new ContentValues();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//id
		cv.put("lec_id", id);
		//username
		cv.put("userid", userName);
		//page
		cv.put("page", page);
		//mrec
		//cv.put("rec", " ");
		//mrec_id
		cv.put("rec", fileName);
		
		//p_id ( �a���ѻP )
		cv.put("p_id", parentInvolvement());
		
		long result = db.insert("lecture", null, cv);
		//Log.i("log","test (SQLiteDB) insertNoteResult = "+result);
	}	
	
	//�o��( tts_id )
	public synchronized void insertTTSDate(int id) {
		
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv=new ContentValues();
		//id
		cv.put("tts_id", id);
		//date
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		cv.put("datetime", sdf.format(new Date()));
		
		//p_id ( �a���ѻP )
		cv.put("p_id", parentInvolvement());
		
		long result = db.insert("ttstable", null, cv);
		//Log.i("log","test (SQLiteDB) insertRangeResult = "+result);

	}
	
	//��������( �}�l�ɶ� )
	public synchronized void insertRecordDate(int id, String tableName) {
		
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv=new ContentValues();
		//id
		cv.put("rec_id", id);
		//date
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		cv.put("datetime", sdf.format(new Date()));
		//p_id ( �a���ѻP )
		cv.put("p_id", parentInvolvement());
		
		long result = db.insert(tableName, null, cv);
		//Log.i("log","test (SQLiteDB) insertRangeResult = "+result);
	}
	
	//�s�W������( translation )
	public synchronized void insertTranslationData(int id, String selected_text, String userName, int page, int IsNote) {
		
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv=new ContentValues();

		cv=new ContentValues();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//id
		cv.put("t_id", id);
		//selectedText
		cv.put("selected_text", selected_text);
		//num
		cv.put("num", 1);
		//username
		cv.put("userid", userName);
		//page
		cv.put("page", page);
		//isNote
		cv.put("IsNote", IsNote);
		//p_id ( �a���ѻP )
		cv.put("p_id", parentInvolvement());
		
		long result = db.insert("translation", null, cv);
		
		insertTranslationNum(id, "translation_num");
	}
	
	//�s�W����������( translation )
	public synchronized void insertTranslationNum(int id, String tableName) {
		
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv=new ContentValues();
		//id
		cv.put("t_id", id);
		//date
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		cv.put("datetime", sdf.format(new Date()));
		//p_id ( �a���ѻP )
		cv.put("p_id", parentInvolvement());
		
		long result = db.insert(tableName, null, cv);
	}
	
	//�s�W�a���ѻP����( parentInvolvement )
	public synchronized void insertParentInData(int id, String userName) {
		
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv=new ContentValues();

		cv=new ContentValues();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//p_id
		cv.put("p_id", id);
		//starttime
		cv.put("starttime", sdf.format(new Date()));
		//username
		cv.put("userid", userName);
		
		long result = db.insert("parentIn_time", null, cv);
	}	
	
	//------------------------------------------------------------------------------------------update_table
	//��z     + �ɦW����
	public synchronized void updateSort_draw(int updateID) {
		int id = this.getTableCount("annotext")+1;
		if( id == updateID )
			return;
		
		SQLiteDatabase db = this.getWritableDatabase();
		String where = "draw_id" + " = ?";
		String[] whereValue = { Integer.toString(id) };
		
		ContentValues cv = new ContentValues();
		cv.put("draw_id", updateID);
		
		ContentValues cv_id = new ContentValues();
		cv_id.put("rec_id", updateID);
		cv_id.put("tts_id", updateID);
		
		//���ɦW
		if( getRecord( id, "rec", "annotext" ) != null ) {
			ContentValues cv_rec = new ContentValues();
			cv_rec.put("rec", updateID+"_"+getStrComment( id, "userid", "annotext")+"_"+getIntData( id, "page", "annotext")+".amr");
			int f = db.update("annotext", cv_rec, where, whereValue);
		}
		
		//annotext �M drawRange �n�@�_��
		int g = db.update("annotext", cv_id, where, whereValue);
		int r = db.update("annotext", cv, where, whereValue);
		int e = db.update("drawRange", cv, where, whereValue);
		
	}
	
	//��z
	public synchronized void updateSort_rec(int updateID) {
		int id = this.getTableCount("annotext")+1;
		if( id == updateID || this.getTableCount("recordtable") == 0 )
			return;
		
		SQLiteDatabase db = this.getWritableDatabase();
		String where = "rec_id" + " = ?";
		String[] whereValue = { Integer.toString(id) };
		
		ContentValues cv = new ContentValues();
		cv.put("rec_id", updateID);
		
		int r = db.update("recordtable", cv, where, whereValue);
	}	
		
	//��z
	public synchronized void updateSort_tts(int updateID) {
		int id = this.getTableCount("annotext")+1;
		if( id == updateID || this.getTableCount("ttstable") == 0 )
			return;
		
		SQLiteDatabase db = this.getWritableDatabase();
		String where = "tts_id" + " = ?";
		String[] whereValue = { Integer.toString(id) };
		
		ContentValues cv = new ContentValues();
		cv.put("tts_id", updateID);
		
		int r = db.update("ttstable", cv, where, whereValue);
	}
	
	//picNote
	//��z 
	public synchronized void updateSort_draw_pic(int updateID) {
		int id = this.getTableCount("annoimage")+1;
		if( id == updateID )
			return;
		
		SQLiteDatabase db = this.getWritableDatabase();
		String where = "draw_id" + " = ?";
		String[] whereValue = { Integer.toString(id) };
		
		ContentValues cv = new ContentValues();
		cv.put("draw_id", updateID);
		
		ContentValues cv_id = new ContentValues();
		cv_id.put("mrec_id", updateID);
		
		//���ɦW
		//if( getRecord( id ) != null ) {
		//	ContentValues cv_rec = new ContentValues();
		//	cv_rec.put("rec", updateID+"_"+getStrComment( id, "userid", "annotext")+"_"+getIntData( id, "page", "annotext")+".amr");
		//	int f = db.update("annotext", cv_rec, where, whereValue);
		//}
		
		//annotext �M drawRange �n�@�_��
		int g = db.update("annoimage", cv_id, where, whereValue);
		int r = db.update("annoimage", cv, where, whereValue);		
	}
	
	//��z
	public synchronized void updateSort_rec_pic(int updateID, String tableName) {
		int id = this.getTableCount("annoimage")+1;
		if( id == updateID )
			return;
		
		SQLiteDatabase db = this.getWritableDatabase();
		String where = "rec_id" + " = ?";
		String[] whereValue = { Integer.toString(id) };
		
		ContentValues cv = new ContentValues();
		cv.put("rec_id", updateID);
		
		int r = db.update(tableName, cv, where, whereValue);
	}
	
	//------------------------------------------------------------------------------------------update
	//�ȲM��
	public synchronized void updateComment(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = "draw_id" + " = ?";
		String[] whereValue = { Integer.toString(id) };
		/* �N�ק諸�ȩ�JContentValues */
		ContentValues cv = new ContentValues();
		cv.put("comment", "");
		int r = db.update("annotext", cv, where, whereValue);
	}
	
	public synchronized void updateComment(int id, String text, String TableName) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = "draw_id" + " = ?";
		String[] whereValue = { Integer.toString(id) };
		/* �N�ק諸�ȩ�JContentValues */
		ContentValues cv = new ContentValues();
		cv.put("comment", text);
		
		ContentValues cv_pid = new ContentValues();
		//p_id ( �a���ѻP )
		cv_pid.put("p_id", parentInvolvement());
		db.update(TableName, cv_pid, where, whereValue);
		
		int result = db.update(TableName, cv, where, whereValue);
	}
	
	public synchronized void updateRecord(int id, String text, String TableName) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = "draw_id" + " = ?";
		String[] whereValue = { Integer.toString(id) };
		/* �N�ק諸�ȩ�JContentValues */
		ContentValues cv = new ContentValues();
		cv.put("rec", text);
		
		ContentValues cv_pid = new ContentValues();
		//p_id ( �a���ѻP )
		cv_pid.put("p_id", parentInvolvement());
		db.update(TableName, cv_pid, where, whereValue);
		
		int result = db.update(TableName, cv, where, whereValue);
	}
	
	public synchronized void updateRecord_picNote(int id, String text, String TableName) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = "draw_id" + " = ?";
		String[] whereValue = { Integer.toString(id) };
		/* �N�ק諸�ȩ�JContentValues */
		ContentValues cv = new ContentValues();
		cv.put("mrec", text);

		ContentValues cv_pid = new ContentValues();
		//p_id ( �a���ѻP )
		cv_pid.put("p_id", parentInvolvement());
		db.update(TableName, cv_pid, where, whereValue);
		
		int result = db.update(TableName, cv, where, whereValue);
	}
	
	public synchronized void updateTTStimes(int id, String TableName) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = "draw_id" + " = ?";
		String[] whereValue = { Integer.toString(id) };
		/* �N�ק諸�ȩ�JContentValues */
		ContentValues cv = new ContentValues();
		cv.put("tts_id", (this.getTTStimes(id)+1));

		ContentValues cv_pid = new ContentValues();
		//p_id ( �a���ѻP )
		cv_pid.put("p_id", parentInvolvement());
		db.update(TableName, cv_pid, where, whereValue);
		
		int result = db.update(TableName, cv, where, whereValue);
	}
	
	public synchronized void updateColor(int id, int r, int g, int b, String TableName) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = "draw_id" + " = ?";
		String[] whereValue = { Integer.toString(id) };
		/* �N�ק諸�ȩ�JContentValues */
		ContentValues cv = new ContentValues();
		cv.put("red", r);
		cv.put("green", g);
		cv.put("blue", b);

		ContentValues cv_pid = new ContentValues();
		//p_id ( �a���ѻP )
		cv_pid.put("p_id", parentInvolvement());
		db.update(TableName, cv_pid, where, whereValue);
		
		db.update(TableName, cv, where, whereValue);
	}
	
	public synchronized void updateStyle(int id, int style, String TableName) {
		//style = 0( ���� )  1( ���u ) 2( �R���u )
		SQLiteDatabase db = this.getWritableDatabase();
		String where = "draw_id" + " = ?";
		String[] whereValue = { Integer.toString(id) };
		/* �N�ק諸�ȩ�JContentValues */
		ContentValues cv = new ContentValues();
		cv.put("type", style);

		ContentValues cv_pid = new ContentValues();
		//p_id ( �a���ѻP )
		cv_pid.put("p_id", parentInvolvement());
		db.update(TableName, cv_pid, where, whereValue);
		
		db.update(TableName, cv, where, whereValue);
	}
	
	public synchronized void updateTranslateNum(int id, int num) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = "t_id" + " = ?";
		String[] whereValue = { Integer.toString(id) };
		/* �N�ק諸�ȩ�JContentValues */
		ContentValues cv = new ContentValues();
		cv.put("num", num);
		db.update("translation", cv, where, whereValue);

		ContentValues cv_pid = new ContentValues();
		//p_id ( �a���ѻP )
		cv_pid.put("p_id", parentInvolvement());
		db.update("translation", cv_pid, where, whereValue);
		
		insertTranslationNum(id, "translation_num");
	}
	
	//�����a���ѻP�����ɶ�
	public synchronized void updateParentInTime(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = "p_id" + " = ?";
		String[] whereValue = { Integer.toString(id) };
		/* �N�ק諸�ȩ�JContentValues */
		ContentValues cv = new ContentValues();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		cv.put("endtime", sdf.format(new Date()));
		db.update("parentIn_time", cv, where, whereValue);
	}
	
	//��������( �����ɶ� )
	public synchronized void updateRecordDateEnd(int id, String tableName) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = "_id" + " = ?";
		String[] whereValue = { Integer.toString(id) };
		/* �N�ק諸�ȩ�JContentValues */
		ContentValues cv = new ContentValues();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		cv.put("datetime_end", sdf.format(new Date()));
		db.update(tableName, cv, where, whereValue);
	}	
	
//----------------------------------------------------------------------------------------------------

	public synchronized void updateLocation(int id, int left, int top, int right, int bottom, String TableName) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = FIELD_id + " = ?";
		String[] whereValue = { Integer.toString(id) };
		/* �N�ק諸�ȩ�JContentValues */
		ContentValues cv = new ContentValues();
		cv.put("left", left);
		//cv.put("right", right);
		//cv.put("top", top);
		//cv.put("bottom", bottom);
		int g = db.update(TableName, cv, where, whereValue);
		//Log.i("log","test id = "+g);
	}

	
	//------------------------------------------------------------------------------------------get
	//���o���O��m(Range) �z�L����
	public synchronized int[] getRangePage(String location, String tablename, int page, String user) {
		int num_arr[];
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE page ='" + page  + "' AND userid='" + user +"' ", null);
			//���������O�`��
			int num = Cur.getCount();
			Cur.moveToFirst();
			//�ŧi
			num_arr = new int[num];

			for( int i=0;num!=0;i++ ) {
				//�u��O0
				num_arr[i] = Cur.getInt(0);
				//���V�e
				Cur.moveToNext();
				num--;
			}

			return num_arr;
			
		} catch (Exception e) {
			Log.i("log","test (SQLiteDBXX) e = "+e+"  (getRange)");
		}

		return null;
	}
	
	//���o���O��m(Range) 
	public synchronized int[] getRangePage(String location, String tablename, String user) {
		int num_arr[];
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE userid='" + user +"' ", null);
			//���������O�`��
			int num = Cur.getCount();
			Cur.moveToFirst();
			//�ŧi
			num_arr = new int[num];

			for( int i=0;num!=0;i++ ) {
				//�u��O0
				num_arr[i] = Cur.getInt(0);
				//���V�e
				Cur.moveToNext();
				num--;
			}

			return num_arr;
			
		} catch (Exception e) {
			Log.i("log","test (SQLiteDBXX) e = "+e+"  (getRange)");
		}

		return null;
	}	
	
	//���o���O��m(Range)
	public synchronized int getRange(int id, String location, String tablename, int num) {
		int loc = -1;
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE draw_id='" + id + "'", null);
			//"Select * from " + TableName + " WHERE _id='" + id + "'"
			Cur.moveToFirst();
			
			while( num != 0 ) {
				Cur.moveToNext();
				num--;
			}

			loc = Cur.getInt(num);
			
		} catch (Exception e) {
			Log.i("log","test getRange");
			Log.i("log","test id = "+id);
			Log.i("log","test id = "+location);
			Log.i("log","test id = "+tablename);
			Log.i("log","test id = "+num);
			Log.i("log","test (SQLiteDBXX) e = "+e+"  (getRange)");
		}

		return loc;
	}
	
	//���o���O�d��(Range)(���X��)
	public synchronized int getNoteRange(int id, String location, String tablename) {
		int num = -1;
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE draw_id='" + id + "'", null);
			Cur.moveToFirst();
			num = Cur.getCount();
			
		} catch (Exception e) {
			Log.i("log","test (SQLiteDB) e = "+e+"  (getNoteRange)");
		}

		return num;
	}
	
	//���o���O�d��(Range)(���X��)( ����ϥΪ� )
	public synchronized int getNoteRange(int id, String location, String tablename, String userName) {
		int num = -1;
		//�ˬd�O�_���P�@�ϥΪ�
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select * from annotext WHERE userid='" + userName +"' ", null);
			Cur.moveToFirst();
			num = Cur.getCount();
			
		} catch (Exception e) {
			Log.i("log","test getNoteRange");
			Log.i("log","test id = "+id);
			Log.i("log","test id = "+location);
			Log.i("log","test id = "+tablename);
			Log.i("log","test id = "+num);
			Log.i("log","test (SQLiteDB) e = "+e+"  (null?)");
			return num;
		}
		
		//������o���O�d��(Range)(���X��)
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE draw_id='" + id + "' ", null);
			Cur.moveToFirst();
			num = Cur.getCount();
			
		} catch (Exception e) {
			Log.i("log","test (SQLiteDB) e = "+e+"  (getNoteRange)");
		}

		return num;
	}	
	
	//���oINT����T
	public synchronized int getIntData(int id, String location, String tablename) {
		int loc = -1;
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE draw_id='" + id + "'", null);
			//"Select * from " + TableName + " WHERE _id='" + id + "'"
			Cur.moveToFirst();
			loc = Cur.getInt(0);
			
		} catch (Exception e) {
			Log.i("log","test (SQLiteDB.getIntComment) e = "+e+"  ("+location+")");
		}

		return loc;
	}
	
	//���oINT����T( ����ϥΪ� )
	public synchronized int getIntData(int id, String location, String tablename, String userName) {
		int loc = -1;
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE draw_id='" + id + "' AND userid='" + userName +"' ", null);
			//"Select * from " + TableName + " WHERE _id='" + id + "'"
			Cur.moveToFirst();
			loc = Cur.getInt(0);
			
		} catch (Exception e) {
			//Log.i("log","test (SQLiteDB.getIntComment) e = "+e+"  ("+location+")");
			return 0;
		}

		return loc;
	}	
	
	//���o�r��
	public synchronized String getStrComment(int id, String location, String tablename) {
		String str = null;
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE draw_id='" + id + "'", null);
			Cur.moveToFirst();
			if( Cur.getCount() > 0 )
				str = Cur.getString(0);
			
		} catch (Exception e) {
			Log.i("log","getStrComment");
			Log.i("log","id = "+id);
			Log.i("log","id = "+location);
			Log.i("log","id = "+tablename);
			Log.i("log","test (SQLiteDB.getStrComment) e = "+e+"  ("+location+")");
			str = "";
		}
		return str;
	}
	
	//���o�r��( ����ϥΪ� )
	public synchronized String getStrComment(int id, String location, String tablename, String userName) {
		String str = null;
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE draw_id='" + id + "' AND userid='" + userName +"' ", null);
			Cur.moveToFirst();
			if( Cur.getCount() > 0 )
				str = Cur.getString(0);
			
		} catch (Exception e) {
			Log.i("log","test (SQLiteDB.getStrComment) e = "+e+"  ("+location+")");
			str = "";
		}
		return str;
	}	
	
	//���o���������� //���е�
	public synchronized int getTranslateNum(String tableName, String text) {
		
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor cur = database.rawQuery("Select num from "+ tableName +" WHERE selected_text='" + text + "' ", null);
			cur.moveToFirst();
			int num = cur.getInt(0);
			return num;
			
		} catch (Exception e) {
			//Log.i("log","test (SQLiteDB.getIntComment) e = "+e+"  ("+location+")");
			return 0;
		}
	}
	
	//���oID
	public synchronized int getTranslateId(String tableName, String text) {
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor cur = database.rawQuery("Select t_id from "+ tableName +" WHERE selected_text='" + text + "' ", null);
			cur.moveToFirst();
			int num = cur.getInt(0);
			return num;
			
		} catch (Exception e) {
			//Log.i("log","test (SQLiteDB.getIntComment) e = "+e+"  ("+location+")");
			return 0;
		}
	}	
	
	//���o���������� //�w�е�
	public synchronized int getTranslateNum(String tableName, int id) {
		
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor cur = database.rawQuery("Select num from "+ tableName +" WHERE IsNote='" + id + "' ", null);
			cur.moveToFirst();
			int num = cur.getInt(0);
			return num;
			
		} catch (Exception e) {
			//Log.i("log","test (SQLiteDB.getIntComment) e = "+e+"  ("+location+")");
			return 0;
		}
	}	
	
	//���oID //�w�е�
	public synchronized int getTranslateId(String tableName, int index) {
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor cur = database.rawQuery("Select t_id from "+ tableName +" WHERE IsNote='" + index + "' ", null);
			cur.moveToFirst();
			int num = cur.getInt(0);
			return num;
			
		} catch (Exception e) {
			//Log.i("log","test (SQLiteDB.getIntComment) e = "+e+"  ("+location+")");
			return 0;
		}
	}	
	
	public synchronized int getTTStimes(int id) {
		int times = 0;
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select tts_id from annotext WHERE draw_id='" + id + "'", null);
			
			//"Select * from " + TableName + " WHERE _id='" + id + "'"
			Cur.moveToFirst();
			times = Cur.getInt(0);
			
		} catch (Exception e) {
			Log.i("log","test(SQLiteDB) e = "+e+"  (getTTStimes)");
		}
		
		return times;
	}
	
	public synchronized String getRecord(int id, String field, String tableName) {
		String name = null;
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select "+ field +" from "+ tableName +" WHERE draw_id='" + id + "'", null);
			
			//"Select * from " + TableName + " WHERE _id='" + id + "'"
			Cur.moveToFirst();
			name = Cur.getString(0);
			
		} catch (Exception e) {
			Log.i("log","test(SQLiteDB) e = "+e+"  (getTTStimes)");
		}
		
		return name;
	}
	
	//------------------------------------------------------------------------------------------get

	//�`total
	public synchronized int getTableCount(String TableName) {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cur = db.rawQuery("Select * from " + TableName, null);
		int x = cur.getCount();
		cur.close();
		return x;
	}
	
	//�`total( ����ϥΪ� )
	public synchronized int getTableCount(String TableName, String userName) {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cur = db.rawQuery("Select * from " + TableName + " WHERE userid='" + userName + "'", null);
		int x = cur.getCount();
		cur.close();
		return x;
	}
	
	//�`total( ���� and �ϥΪ� )
	public synchronized int getTableCount(String TableName, String userName, int page) {
		
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor cur = db.rawQuery("Select * from " + TableName + " WHERE userid='" + userName + "' AND page='" + page + "' ", null);
			int x = cur.getCount();
			cur.close();
			//Log.i("log", "test log = "+x);
			return x;
		}
		catch (Exception e) {
			Log.i("log", "test SQLiteDB error = "+e);
			return 0;
		}
	}
	
	//get _ID ( ���̤jID )
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
			Log.i("log", "test SQLiteDB error = "+e);
			return 0;
		}
	}	

}
