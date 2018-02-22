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

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.zlibrary.text.view.SaveValue;

public final class SQLiteDB extends SQLiteOpenHelper {
	
	private final static String DATABASE_NAME = "FBReader_note.db";
	private final static int DATABASE_VERSION = 22;
	private final static String[] status = {"INSERT", "UPDATE", "DELETE", "NOACTION", "DELETEOK"}; 

	public SQLiteDB(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	//test
	private static SQLiteDB ourInstance;

	public static SQLiteDB Instance() {
		return ourInstance;
	}
	//test
	
	@Override
	public void onCreate(SQLiteDatabase sqldb) {
		Log.i("SQLiteDB", "config");
		//��r���O--------------------------------------------------------------------------------------------------------------------------------------
		sqldb.execSQL("CREATE TABLE annotext (_id INTEGER PRIMARY KEY, red INT, green INT, blue INT, type INT," +
				"date TEXT, modifieddate TEXT, comment TEXT, userid TEXT, page INT, rec TEXT, txt TEXT, pic TEXT, p_id INT, status TEXT)");
		//��r���O---�d��
		sqldb.execSQL("CREATE TABLE annotext_range (_id INTEGER PRIMARY KEY, t_id INT, left INT, top INT, right INT, bottom INT, userid TEXT, p_id INT, status TEXT)");
		//��r���O---TTS����
		sqldb.execSQL("CREATE TABLE tts_num (_id INTEGER PRIMARY KEY, t_id INT, datetime TEXT, userid TEXT, p_id INT, status TEXT)");
		//��r���O--------------------------------------------------------------------------------------------------------------------------------------

		//�Ϥ����O--------------------------------------------------------------------------------------------------------------------------------------
		sqldb.execSQL("CREATE TABLE annoimage (_id INTEGER PRIMARY KEY, annoType TEXT, sX INT, sY INT, date TEXT, modifieddate TEXT, comment TEXT," +
						"userid TEXT, page INT, rec TEXT, pic TEXT, p_id INT, status TEXT)");
		//�Ϥ����O--------------------------------------------------------------------------------------------------------------------------------------

		//�����n������
		sqldb.execSQL("CREATE TABLE record (_id INTEGER PRIMARY KEY, rec_id INT, annoType TEXT, startTime TEXT, endTime TEXT, userid TEXT, p_id INT, status TEXT, setImage BOOLEAN)");

		//������
		sqldb.execSQL("CREATE TABLE translation (_id INTEGER PRIMARY KEY ,t_id INT, selectedText TEXT, num INT, userid TEXT, page TEXT, p_id INT, status TEXT)");
		//������----�����C���I��ɶ�
		sqldb.execSQL("CREATE TABLE translation_num (_id INTEGER PRIMARY KEY ,translation_id INT, datetime TEXT, userid TEXT, p_id INT, status TEXT)");

		//�����a���ѻP�ɶ�
		sqldb.execSQL("CREATE TABLE parentin_time (_id INTEGER PRIMARY KEY, startTime TEXT, endTime TEXT, userid TEXT, status TEXT)");
		
		//�P�B���O--------------------------------------------------------------------------------------------------------------------------------------
		//�P�B�����ɶ�
		sqldb.execSQL("CREATE TABLE sync_local_data (_id INTEGER PRIMARY KEY, userid TEXT, datetime TEXT, annoText INT, annoImage INT, annoRecord INT, L0 INT, L1 INT, L2 INT, L3 INT, L4 INT, L5 INT, L6 INT, L7 INT, L8 INT, L9 INT, L10 INT)");
		//�O���ϥΪ��[�ݮɶ�
		sqldb.execSQL("CREATE TABLE sync_time (_id INTEGER PRIMARY KEY, startTime TEXT, endTime TEXT, userid TEXT, syncUserid TEXT, p_id INT)");
		
		//�����ϥΪ̦b�[���Ҧ��ɩҰ����ʧ@
		sqldb.execSQL("CREATE TABLE sync_action (_id INTEGER PRIMARY KEY, s_id INTEGER, type TEXT, type_id INT, action TEXT, datetime TEXT, p_id INT)");
		//�P�B���O--------------------------------------------------------------------------------------------------------------------------------------
		sqldb.execSQL("CREATE TABLE takepic_anno (_id INTEGER PRIMARY KEY, annoType TEXT, sX INT, sY INT, date TEXT, modifieddate TEXT, comment TEXT, userid TEXT, page INT, lesson TEXT, picFilePath TEXT, rec TEXT, pic TEXT, p_id INT, status TEXT)");
		sqldb.execSQL("CREATE TABLE takepic (_id INTEGER PRIMARY KEY, date TEXT, modifieddate TEXT, userid TEXT, lesson TEXT, pic TEXT, status TEXT, picname TEXT)");
	}
	
	//------------------------------------------------------------------------------------------delete
	public void delete(String str, String field, String TableName) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = field + " = ?";
		String[] whereValue = { str };
		
		db.delete(TableName, where, whereValue);
		//long result = db.delete(TableName, where, whereValue);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
	}
	
	public synchronized int parentInvolvement() {
		if( SaveValue.IsParent ) {
			int num = getTableCount(ActionCode.ANNOTATION_PARENTIN_TIME);  //���O�Ӽ�
			return num;
		}
		return 0;
	}
	
	//------------------------------------------------------------------------------------------insert
	//�s�W���O��m�d��( drawRange )
	public synchronized void insertRange(int id, int left, int top, int right, int bottom, String userName) {
		
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
		cv.put("p_id", parentInvolvement());
		cv.put("status", status[0]);
		
		db.insert(ActionCode.ANNOTATION_TEXT_RANGE, null, cv);
	}
	
	//�s�W���O��m( annotext )
	public synchronized void insertNote(int _id, int r, int g, int b, String userName, int page, String txt) {
		
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv=new ContentValues();

		cv=new ContentValues();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//id
		cv.put("_id", _id);
		//color
		cv.put("red", r);
		cv.put("green", g);
		cv.put("blue", b);
		//style
		cv.put("type", 0);  //�w�]�O���u
		//date
		cv.put("date", sdf.format(new Date()));
		//mdate
		cv.put("modifieddate", sdf.format(new Date()));
		//comment
		//cv.put("comment", " ");
		//userid
		cv.put("userid", userName);
		//page
		cv.put("page", page);
		//rec
		//cv.put("rec", " ");
		//txt ( �Q�������r )
		cv.put("txt", txt);
		//p_id ( �a���ѻP )
		cv.put("p_id", parentInvolvement());
		cv.put("status", status[0]);
		
		db.insert(ActionCode.ANNOTATION_TEXT, null, cv);
	}
	//�s�W��r���O��m
	public synchronized void YaninsertPicNote(int _id, String annoType,int x, int y, String Username, String lesson){
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv=new ContentValues();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		cv.put("_id", _id);
		cv.put("annoType", annoType);
		cv.put("lesson", lesson);
		cv.put("sX", x);
		cv.put("sY", y);
		cv.put("date", sdf.format(new Date()));
		cv.put("userid",Username);
		cv.put("status", status[0]);
		cv.put("picFilePath", SaveValue.picNow);
		db.insert("takepic_anno", null, cv);
	}
	//�s�W���O��m( annoimage )
	public synchronized void insertPicNote(int _id, String annoType, int x, int y, int page, String userName) {
		
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv=new ContentValues();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//id
		cv.put("_id", _id);
		//type
		cv.put("annoType", annoType);
		//x y
		cv.put("sX", x);
		cv.put("sY", y);
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
		//p_id ( �a���ѻP )
		cv.put("p_id", parentInvolvement());
		cv.put("status", status[0]);
		
		db.insert(ActionCode.ANNOTATION_IMAGE, null, cv);
	}
	
	//�o��( tts_id )
	public synchronized void insertTTSDate(int id, String userName) {
		
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv=new ContentValues();
		//id
		cv.put("t_id", id);
		//date
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		cv.put("datetime", sdf.format(new Date()));
		cv.put("userid", userName);
		
		//p_id ( �a���ѻP )
		cv.put("p_id", parentInvolvement());
		cv.put("status", status[0]);
		
		db.insert(ActionCode.ANNOTATION_TTS_NUMBER, null, cv);
	}
	
	//��������( �}�l�ɶ� )
	public synchronized void insertRecordDate(int id, String annoType, String userName) {
		
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
		//p_id ( �a���ѻP )
		cv.put("p_id", parentInvolvement());
		cv.put("status", status[0]);
		//
		cv.put("setImage", FBReader.setImage);
		db.insert(ActionCode.ANNOTATION_RECORD, null, cv);
	}
	
	//�s�W������( translation )
	public synchronized void insertTranslationData(int id, int t_id, String selectedText, String userName, int page) {
		
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv=new ContentValues();

		cv=new ContentValues();
		//id
		cv.put("t_id", t_id); //��r���OID �p�S�� �h��0
		//selectedText
		cv.put("selectedText", selectedText);
		//num
		cv.put("num", 1);
		//username
		cv.put("userid", userName);
		//page
		cv.put("page", page);
		//p_id ( �a���ѻP )
		cv.put("p_id", parentInvolvement());
		cv.put("status", status[0]);
		
		db.insert(ActionCode.ANNOTATION_TRANSLATION, null, cv);
		insertTranslationNum(id, ActionCode.ANNOTATION_TRANSLATION_NUMBER, userName);
	}
	
	//�s�W����������( translation )
	public synchronized void insertTranslationNum(int id, String tableName, String userName) {
		
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv=new ContentValues();
		//id
		cv.put("translation_id", id);
		//date
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		cv.put("datetime", sdf.format(new Date()));
		//userid
		cv.put("userid", userName);
		//p_id ( �a���ѻP )
		cv.put("p_id", parentInvolvement());
		cv.put("status", status[0]);
		
		db.insert(tableName, null, cv);
	}
	
	//�s�W�a���ѻP����( parentInvolvement )
	public synchronized void insertParentInData(int id, String userName) {
		
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv=new ContentValues();

		cv=new ContentValues();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//p_id
		cv.put("_id", id);
		//startTime
		cv.put("startTime", sdf.format(new Date()));
		//username
		cv.put("userid", userName);
		cv.put("status", status[0]);
		
		db.insert(ActionCode.ANNOTATION_PARENTIN_TIME, null, cv);
	}	
	
	//�s�W�P�B����( sync_local_data )
	public synchronized void insertSyncData(int id, String userName) {
		
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv=new ContentValues();

		cv=new ContentValues();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//p_id
		cv.put("_id", id);
		//username
		cv.put("userid", userName);
		//date
		//cv.put("datetime", sdf.format(new Date()));
		cv.put("datetime", "�|���P�B");
		cv.put("annoText", 0);
		cv.put("annoImage", 0);
		cv.put("annoRecord", 0);
		cv.put("L0", 0);
		cv.put("L1", 0);
		cv.put("L2", 0);
		cv.put("L3", 0);
		cv.put("L4", 0);
		cv.put("L5", 0);
		cv.put("L6", 0);
		cv.put("L7", 0);
		cv.put("L8", 0);
		cv.put("L9", 0);
		cv.put("L10", 0);
		db.insert(ActionCode.ANNOTATION_SYNC_DATA, null, cv);
	}
	
	//�s�W�[�����}�l�ɶ� ( sync_Time )
	public synchronized void insertSyncTime(String userName, String syncUserName) {
		//_id INTEGER PRIMARY KEY, startTime TEXT, endTime TEXT, userid TEXT, syncUserid TEXT;
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv=new ContentValues();

		cv=new ContentValues();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		cv.put("startTime", sdf.format(new Date()));
		//userName
		cv.put("userid", userName);
		//syncUserName
		cv.put("syncUserid", syncUserName);
		//p_id ( �a���ѻP )
		cv.put("p_id", parentInvolvement());
		db.insert(ActionCode.ANNOTATION_SYNC_TIME, null, cv);
	}
	
	//�����[���ɪ��ʧ@
	public synchronized void insertSyncAction(int s_id, String type, int type_id, String action) {
		//sqldb.execSQL("CREATE TABLE sync_action (_id INTEGER PRIMARY KEY, s_id INTEGER, type TEXT, type_id INT, action TEXT, datetime TEXT, p_id INT)");
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv=new ContentValues();

		cv=new ContentValues();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		cv.put("s_id", s_id);
		//userName
		cv.put("type", type);
		//syncUserName
		cv.put("type_id", type_id);
		//syncUserName
		cv.put("action", action);
		//syncUserName
		cv.put("datetime", sdf.format(new Date()));
		//p_id ( �a���ѻP )
		cv.put("p_id", parentInvolvement());
		db.insert(ActionCode.ANNOTATION_SYNC_ACTION, null, cv);
	}
	
	//------------------------------------------------------------------------------------------update
	//��s�ק�ɶ�
	public synchronized void updateModDate(int id, String TableName) {
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		SQLiteDatabase db = this.getWritableDatabase();
		String where = "_id" + " = ?";
		String[] whereValue = { Integer.toString(id) };
		// �N�ק諸�ȩ�JContentValues
		ContentValues cv = new ContentValues();
		cv.put("modifieddate", sdf.format(new Date()));
		
		ContentValues cv_pid = new ContentValues();
		//p_id ( �a���ѻP )
		cv_pid.put("p_id", parentInvolvement());
		
		//���A
		ContentValues cv_status = new ContentValues();
		cv_status.put("status", status[1]);
		
		db.update(TableName, cv, where, whereValue);
		db.update(TableName, cv_pid, where, whereValue);
		db.update(TableName, cv_status, where, whereValue);
	}
	
	//��s��r���e
	public synchronized void updateComment(int id, String text, String TableName) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = "_id" + " = ?";
		String[] whereValue = { Integer.toString(id) };
		// �N�ק諸�ȩ�JContentValues
		ContentValues cv = new ContentValues();
		cv.put("comment", text);
		
		ContentValues cv_pid = new ContentValues();
		//p_id ( �a���ѻP )
		cv_pid.put("p_id", parentInvolvement());
		
		//���A
		ContentValues cv_status = new ContentValues();
		cv_status.put("status", status[1]);
		db.update(TableName, cv, where, whereValue);
		db.update(TableName, cv_pid, where, whereValue);
		db.update(TableName, cv_status, where, whereValue);
	}
	/*
	//�x�s�ϥΪ̦b�ۤv�窺�Ӥ��W����r���O
	public synchronized void YanupdateComment(int _id,String tablename, String userid, String comment){
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		String where = "_id" + " = ?";
		String[] whereValue = { Integer.toString(_id) };
		cv.put("userid", userid);
		cv.put("comment", comment);
		db.update(tablename, cv, where, whereValue);	
	}
	*/
	//��s��r���e
	public synchronized void updatePicture(int id, String file, String TableName) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = "_id" + " = ?";
		String[] whereValue = { Integer.toString(id) };
		// �N�ק諸�ȩ�JContentValues
		ContentValues cv = new ContentValues();
		cv.put("pic", file);
		
		ContentValues cv_pid = new ContentValues();
		//p_id ( �a���ѻP )
		cv_pid.put("p_id", parentInvolvement());
		
		//���A
		ContentValues cv_status = new ContentValues();
		cv_status.put("status", status[1]);
		db.update(TableName, cv, where, whereValue);
		db.update(TableName, cv_pid, where, whereValue);
		db.update(TableName, cv_status, where, whereValue);
	}	
	
	//��s����
	public synchronized void updateRecord(int id, String text, String TableName) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = "_id" + " = ?";
		String[] whereValue = { Integer.toString(id) };
		/* �N�ק諸�ȩ�JContentValues */
		ContentValues cv = new ContentValues();
		cv.put("rec", text);
		
		ContentValues cv_pid = new ContentValues();
		//p_id ( �a���ѻP )
		cv_pid.put("p_id", parentInvolvement());
		
		//���A
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
		/* �N�ק諸�ȩ�JContentValues */
		ContentValues cv = new ContentValues();
		cv.put("red", r);
		cv.put("green", g);
		cv.put("blue", b);

		ContentValues cv_pid = new ContentValues();
		//p_id ( �a���ѻP )
		cv_pid.put("p_id", parentInvolvement());
		
		//���A
		ContentValues cv_status = new ContentValues();
		cv_status.put("status", status[1]);
				
		db.update(TableName, cv, where, whereValue);
		db.update(TableName, cv_pid, where, whereValue);
		db.update(TableName, cv_status, where, whereValue);
	}
	
	public synchronized void updateStyle(int id, int style, String TableName) {
		//style = 0( ���� )  1( ���u ) 2( �R���u )
		SQLiteDatabase db = this.getWritableDatabase();
		String where = "_id" + " = ?";
		String[] whereValue = { Integer.toString(id) };
		/* �N�ק諸�ȩ�JContentValues */
		ContentValues cv = new ContentValues();
		cv.put("type", style);

		ContentValues cv_pid = new ContentValues();
		//p_id ( �a���ѻP )
		cv_pid.put("p_id", parentInvolvement());
		
		//���A
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
		/* �N�ק諸�ȩ�JContentValues */
		ContentValues cv = new ContentValues();
		cv.put("num", num);

		ContentValues cv_pid = new ContentValues();
		//p_id ( �a���ѻP )
		cv_pid.put("p_id", parentInvolvement());
		
		//���A
		ContentValues cv_status = new ContentValues();
		cv_status.put("status", status[1]);
		
		db.update(ActionCode.ANNOTATION_TRANSLATION, cv, where, whereValue);
		db.update(ActionCode.ANNOTATION_TRANSLATION, cv_pid, where, whereValue);
		db.update(ActionCode.ANNOTATION_TRANSLATION, cv_status, where, whereValue);
		
		insertTranslationNum(id, ActionCode.ANNOTATION_TRANSLATION_NUMBER, SaveValue.UserName);
	}
	
	//�����a���ѻP�����ɶ�
	public synchronized void updateParentInTime(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = "_id" + " = ?";
		String[] whereValue = { Integer.toString(id) };
		/* �N�ק諸�ȩ�JContentValues */
		ContentValues cv = new ContentValues();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		cv.put("endTime", sdf.format(new Date()));
		
		//���A
		ContentValues cv_status = new ContentValues();
		cv_status.put("status", status[1]);
		
		db.update(ActionCode.ANNOTATION_PARENTIN_TIME, cv, where, whereValue);
		db.update(ActionCode.ANNOTATION_PARENTIN_TIME, cv_status, where, whereValue);
	}
	
	//��������( �����ɶ� )
	public synchronized void updateRecordDateEnd(int id, String tableName) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = "_id" + " = ?";
		String[] whereValue = { Integer.toString(id) };
		/* �N�ק諸�ȩ�JContentValues */
		ContentValues cv = new ContentValues();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		cv.put("endTime", sdf.format(new Date()));
		
		//���A
		ContentValues cv_status = new ContentValues();
		cv_status.put("status", status[1]);
		
		db.update(tableName, cv, where, whereValue);
		db.update(tableName, cv_status, where, whereValue);
	}	
	
	//���R��( ��窱�A )
	public synchronized void updateStatus(int id, String field, String tableName, int  statusId) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = field + " = ?";
		String[] whereValue = { Integer.toString(id) };
		/* �N�ק諸�ȩ�JContentValues */
		ContentValues cv = new ContentValues();
		cv.put("status", status[statusId]);
		db.update(tableName, cv, where, whereValue);
	}
	//���R��(��窱�A�A�R���ϥΪ̦ۦ���᪺�Ӥ���)
	public synchronized void updateStatus(String username, String selectPicName, String lesson){
		SQLiteDatabase db = this.getWritableDatabase();
		String where = "userid" + " = ?" + " AND picname" + " = ?" + " AND lesson" + " = ?" + " AND status" + " != ?";
		String[] whereValue = { username, selectPicName, lesson, "DELETE"};
		ContentValues cv = new ContentValues();
		cv.put("status", status[2]);
		db.update("takepic", cv, where, whereValue);
	}
	
	//( ��窱�A �ݤ�����A �P�B�ݭn )
	public synchronized int updateStatus(int id, String field, String tableName, int  statusId, String statusNow) {
		///494
		Log.i("test", "test status = "+statusNow);
		if( !statusNow.equals(getStrComment(id, "status", tableName)) )
			return 0;
		SQLiteDatabase db = this.getWritableDatabase();
		String where = field + " = ?";
		String[] whereValue = { Integer.toString(id) };
		/* �N�ק諸�ȩ�JContentValues */
		ContentValues cv = new ContentValues();
		cv.put("status", status[statusId]);
		return db.update(tableName, cv, where, whereValue);
	}
	
	//��s�P�B�ɶ��I ( ��ܨϥΪ̡A�ëD_id )
	public synchronized int updateSyncData(String user, String field, String tableName, int annoText, int annoImage, int annoRecord, int[] lesson) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = field + " = ?";
		String[] whereValue = { user };
		/* �N�ק諸�ȩ�JContentValues */
		ContentValues cv = new ContentValues();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		cv.put("datetime", sdf.format(new Date()));
		//���O���A
		ContentValues cv_text = new ContentValues();
		cv_text.put("annoText", annoText);
		ContentValues cv_image = new ContentValues();
		cv_image.put("annoImage", annoImage);
		ContentValues cv_record = new ContentValues();
		cv_record.put("annoRecord", annoRecord);
		String[] str = {"L0","L1","L2","L3","L4","L5","L6","L7","L8","L9","L10"};
		try {
			db.update(tableName, cv, where, whereValue);
			db.update(tableName, cv_text, where, whereValue);
			db.update(tableName, cv_image, where, whereValue);
			db.update(tableName, cv_record, where, whereValue);
			for(int i=0;i<lesson.length;i++) {
				ContentValues cv_L0 = new ContentValues();
				cv_L0.put(str[i], lesson[i]);
				db.update(tableName, cv_L0, where, whereValue);
			 }
		} catch(Exception e) {
			Log.i("log","test (SQLiteDB) line571 = "+e);
			return 2;
		}
		
		return 1;
	}
	
	//��s�[�������ɶ�
	public synchronized int updateSyncTime(String id, String field) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = field + " = ?";
		String[] whereValue = { id };
		/* �N�ק諸�ȩ�JContentValues */
		ContentValues cv = new ContentValues();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		cv.put("endTime", sdf.format(new Date()));
		
		ContentValues cv_pid = new ContentValues();
		//p_id ( �a���ѻP )
		cv_pid.put("p_id", parentInvolvement());
		db.update(ActionCode.ANNOTATION_SYNC_TIME, cv_pid, where, whereValue);
		
		return db.update(ActionCode.ANNOTATION_SYNC_TIME, cv, where, whereValue);
	}	
	
	//------------------------------------------------------------------------------------------get
	//���otakepic_anno����r���O���
	public synchronized String[] YangetTextAnnoData(String TableName, String Username){
		String Textanno[];
		if(Username != null){
			try{
				SQLiteDatabase db = this.getWritableDatabase();
				Cursor cur;
				cur = db.rawQuery("Select * from "  + TableName + " WHERE userid='" + Username + "'", null);
				cur.moveToPosition(0); //�S���ʦ�m���ܡA�|�q-1�}�l
				int rows_num = cur.getCount();
				Textanno = new String[rows_num];
				for(int i=0; i<rows_num; i++){
					Textanno[i] = cur.getString(6);
					cur.moveToNext();
				}
				cur.close();
				return Textanno;
			}
			catch(Exception e){
			}
		}else{
			return null;
		}
		return null;
	}
	//���otakepic_anno����r���Ox��m 
		public synchronized int[] YangetTextAnnoPosX(String TableName, String Username, String lesson){
			int TextAnnoposX[];
			if(Username != null){
				try{
					SQLiteDatabase db = this.getWritableDatabase();
					Cursor cur;
					cur = db.rawQuery("Select * from " + TableName + " WHERE userid='" + Username + "' AND picFilePath='" + SaveValue.picNow + "' AND lesson='" + lesson + "' AND status !='DELETE' AND status !='DELETEOK'", null);
					cur.moveToPosition(0);
					int rows_num = cur.getCount();
					TextAnnoposX = new int[rows_num];
					for(int i=0; i<rows_num; i++){
						TextAnnoposX[i] = cur.getInt(2);
						cur.moveToNext();
					}
					cur.close();
					return TextAnnoposX;
					}
				catch(Exception e){
					Log.i("log","test (SQLiteDB) line711 = "+e);
				}
			}else{
				return null;
			}
			return null;
		}
	//���otakepic_anno����r���Oy��m
		public synchronized int[] YangetTextAnnoPosY(String TableName, String Username, String lesson){
			int TextAnnoposY[];
			if(Username != null){
				try{
					SQLiteDatabase db = this.getWritableDatabase();
					Cursor cur;
					cur = db.rawQuery("Select * from " + TableName + " WHERE userid='" + Username + "' AND picFilePath='" + SaveValue.picNow + "' AND lesson='" + lesson + "' AND status !='DELETE' AND status !='DELETEOK'", null);
					cur.moveToPosition(0);
					int rows_num = cur.getCount();
					TextAnnoposY = new int[rows_num];
					for(int i=0; i<rows_num; i++){
						TextAnnoposY[i] = cur.getInt(3);
						cur.moveToNext();
					}
					cur.close();
					return TextAnnoposY;
					}
				catch(Exception e){
					Log.i("log","test (SQLiteDB) line734 = "+e);
				}
			}else{
				return null;
			}
			return null;
		}
	//���o���O��m(Range) �z�L���� 20120304 �s�W���A
	public synchronized int[] getRangePage(String location, String tablename, int page, String user) {
		int num_arr[];
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			//Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE page ='" + page  + "' AND userid='" + user +"' ", null);
			Cursor cur;
			if( ActionCode.ANNOTATION_IMAGE.equals(tablename) ) //�p�G�O�����Ϥ����O�ƶq
				cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE page ='" + page  + "' AND userid='" + user + "' AND status!='DELETE' AND status!='DELETEOK' AND annoType != 'PARENTS' ", null);
			else
				cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE page ='" + page  + "' AND userid='" + user + "' AND status!='DELETE' AND status!='DELETEOK'", null);
			//���������O�`��
			int num = cur.getCount();
			cur.moveToFirst();
			//�ŧi
			num_arr = new int[num];

			for( int i=0;num!=0;i++ ) {
				//�u��O0
				num_arr[i] = cur.getInt(0);
				//���V�e
				cur.moveToNext();
				num--;
			}
			if(cur != null)
				cur.close();

			return num_arr;
			
		} catch (Exception e) {
			Log.i("log","test (SQLiteDB) line534 = "+e);
		}

		return null;
	}
	
	//���o���O��m(Range)
	public synchronized int[] getRangePage(String location, String tablename, int page, String user, String annoType) {
		int num_arr[];
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			//Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE page ='" + page  + "' AND userid='" + user +"' ", null);
			Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE page ='" + page  + "' AND userid='" + user + "' AND status!='DELETE' AND status!='DELETEOK' AND annoType = '"+annoType+"' ", null);
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
			if(Cur != null)
				Cur.close();

			return num_arr;
			
		} catch (Exception e) {
			Log.i("log","test (SQLiteDB) line534 = "+e);
		}

		return null;
	}	
	
	//���o���O��m(Range) //����{UserID
	public synchronized int[] getRangePage(String location, String tablename, String status) {
		int num_arr[];
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			//Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE userid='" + user +"' ", null);
			Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE status='"+ status +"' ", null);
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
			if(Cur != null)
				Cur.close();

			return num_arr;
			
		} catch (Exception e) {
			Log.i("log","test (SQLiteDB) line564 = "+e);
		}

		return null;
	}		
	
	//���o���O��m(Range) 
	public synchronized int[] getRangePage(String location, String tablename, String user, String status) {
		int num_arr[];
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			//Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE userid='" + user +"' ", null);
			Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE userid='" + user +"' AND status='"+ status +"' ", null);
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
			if(Cur != null)
				Cur.close();

			return num_arr;
			
		} catch (Exception e) {
			Log.i("log","test (SQLiteDB) line594 = "+e);
		}

		return null;
	}	
	
	//���o���O��m(Range)
	public synchronized int getRange(int id, String location, String tablename, int num) {
		int loc = -1;
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE t_id='" + id + "'", null);
			//"Select * from " + TableName + " WHERE _id='" + id + "'"
			Cur.moveToFirst();
			
			while( num != 0 ) {
				Cur.moveToNext();
				num--;
			}

			loc = Cur.getInt(num);
			if(Cur != null)
				Cur.close();
			
		} catch (Exception e) {
			Log.i("log","test (SQLiteDB) line617 = "+e+"  (getRange)");
		}

		return loc;
	}
	
	//���o���O�d��(Range)(���X��)
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
			Log.i("log","test (SQLiteDB) line633 = "+e);
		}

		return num;
	}
	
	//���o���O�d��(Range)(���X��)( ����ϥΪ� )
	public synchronized int getNoteRange(int id, String location, String tablename, String userName) {
		int num = -1;
		//�ˬd�O�_���P�@�ϥΪ�
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select * from "+ActionCode.ANNOTATION_TEXT+" WHERE userid='" + userName +"' ", null);
			Cur.moveToFirst();
			num = Cur.getCount();
			if(Cur != null)
				Cur.close();
			
		} catch (Exception e) {
			Log.i("log","test (SQLiteDB) line650 = "+e);
			return num;
		}
		
		//������o���O�d��(Range)(���X��)
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE t_id='" + id + "' ", null);
			//Log.i("test", "test sql = "+"Select "+ location +" from "+ tablename +" WHERE t_id='" + id + "' ");
			Cur.moveToFirst();
			num = Cur.getCount();
			if(Cur != null)
				Cur.close();
			
		} catch (Exception e) {
			Log.i("log","test (SQLiteDB) line663 = "+e);
		}

		return num;
	}	
	
	//���oINT����T
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
			Log.i("log","test (SQLiteDB) line680 = "+e);
		}

		return loc;
	}
	
	//���oINT����T( ����ϥΪ� )
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
			Log.i("log","test (SQLiteDB) line697 = "+e);
			return 0;
		}

		return loc;
	}	
	
	//���o�r��
	public synchronized String getStrComment(int id, String location, String tablename) {
		String str = null;
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE _id='" + id + "'", null);
			//Log.i("test", "test sql = "+"Select "+ location +" from "+ tablename +" WHERE _id='" + id + "'");
			Cur.moveToFirst();
			if( Cur.getCount() > 0 )
				str = Cur.getString(0);
			if(Cur != null)
				Cur.close();
			return str;
			
		} catch (Exception e) {
			Log.i("log","test (SQLiteDB) line717 = "+e);
		}
		return str;
	}
	
	//���o�r��( ����ϥΪ� )
	public synchronized String getStrComment(int id, String location, String tablename, String userName) {
		String str = null;
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select "+ location +" from "+ tablename +" WHERE _id='" + id + "' AND userid='" + userName + "' ", null);
			Cur.moveToFirst();
			if( Cur.getCount() > 0 )
				str = Cur.getString(0);
			if(Cur != null)
				Cur.close();
			return str;
		} catch (Exception e) {
			Log.i("log","test (SQLiteDB) line734 = "+e);
		}
		return str;
	}	
	
	//���o�r��( ����ϥΪ�,����ID )
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
			Log.i("log","test (SQLiteDB) line751 = "+e);
		}
		return str;
	}		
	
	//���o���������� (�����O)
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
			Log.i("log","test (SQLiteDB) line768 = "+e);
			return 0;
		}
	}
	
	//���o���ID (�����O)
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
			Log.i("log","test (SQLiteDB) line783 = "+e);
			return 0;
		}
	}	
	
	//���o���������� (���O)
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
			Log.i("log","test (SQLiteDB) line799 = "+e);
			return 0;
		}
	}	
	
	//���o���ID (�����O)
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
			Log.i("log","test (SQLiteDB) line814 = "+e);
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
			System.out.println("INDEX NOW is " + id);
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select "+ field +" from "+ tableName +" WHERE _id='" + id + "'", null);
			//Log.i("test", "test sql = "+"Select "+ field +" from "+ tableName +" WHERE _id='" + id + "'");
			//"Select * from " + TableName + " WHERE _id='" + id + "'"
			Cur.moveToFirst();
			name = Cur.getString(0);
			if(Cur != null)
				Cur.close();
			
		} catch (Exception e) {
			Log.i("log","test(SQLiteDB) line1163 = "+e);
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
	
	//getRecord
	public synchronized int getRecordID(int rec_id, String annoType) {
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor cur;
			if(!FBReader.setImage)
				cur = db.rawQuery("Select _id from " + ActionCode.ANNOTATION_RECORD + " WHERE rec_id='" + rec_id + "' AND annoType='" + annoType + "' AND endTime='null'", null);
			else
				cur = db.rawQuery("Select _id from " + ActionCode.ANNOTATION_RECORD + " WHERE rec_id='" + rec_id + "' AND annoType='" + annoType + "' AND endTime='null' AND setImage='1'", null);
			cur.moveToFirst();
			int x = cur.getInt(0);
			cur.close();
			return x;
		} catch( Exception e ) {
			Log.i("log", "test (SQLiteDB) line1192 = "+e);
			return 0;
		}
	
	}	
	
	//�`total( ����ϥΪ� )
	public synchronized int getTableCount(String TableName, String userName) {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cur = db.rawQuery("Select * from " + TableName + " WHERE userid='" + userName + "'", null);
		int x = cur.getCount();
		cur.close();
		return x;
	}
	
	//�`total( �p��ثe�ϥΪ̪����O�ƶq,�ư��Ѯv���� ) status�N������ܪ����A
	public synchronized int getTableCount(String TableName, String userName, String status) {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cur;
		if( ActionCode.ANNOTATION_IMAGE.equals(TableName) ) //�p�G�O�����Ϥ����O�ƶq
			cur = db.rawQuery("Select * from " + TableName + " WHERE userid='" + userName + "' AND status NOT LIKE '%"+status+"%' AND annoType != 'LECTURE' ", null);
		else
			cur = db.rawQuery("Select * from " + TableName + " WHERE userid='" + userName + "' AND status NOT LIKE '%"+status+"%' ", null);
		
		int x = cur.getCount();
		cur.close();
		return x;
	}
	
	//�`total( �p��ثe�ϥΪ̪������ƶq,�ư��Ѯv���� ) status�N������ܪ����A
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
	
	//�`total( ���� and �ϥΪ� ) 20120304 �s�W���A
	public synchronized int getTableCount(String TableName, String userName, int page) {
		
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			//Cursor cur = db.rawQuery("Select * from " + TableName + " WHERE userid='" + userName + "' AND page='" + page + "' ", null);
			Cursor cur;
			if( ActionCode.ANNOTATION_IMAGE.equals(TableName) ) //�p�G�O�����Ϥ����O�ƶq
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
	
	//�`total( ���� and �ϥΪ� and ���� )
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
	
	//total( �p��b�@�w���ƨϥΪ̪����O�ƶq,�ư��Ѯv���� ) status�N������ܪ����A
	public synchronized int getTableCount(String TableName, String userName, int from, int to, String status) {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cur;
		if( ActionCode.ANNOTATION_IMAGE.equals(TableName) ) //�p�G�O�����Ϥ����O�ƶq
			cur = db.rawQuery("Select * from " + TableName + " WHERE page >= " + from + " AND page < "+ to +" AND userid='" + userName + "' AND status NOT LIKE '%"+status+"%' AND annoType != 'LECTURE' ", null);
		else
			cur = db.rawQuery("Select * from " + TableName + " WHERE page >= " + from + " AND page < "+ to +" AND userid='" + userName + "' AND status NOT LIKE '%"+status+"%' ", null);
		
		int x = cur.getCount();
		cur.close();
		return x;
	}
	
	//total( �p��b�@�w���ƨϥΪ̪������ƶq,�ư��Ѯv���� ) status�N������ܪ����A
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
			Log.i("log", "test SQLiteDB line919 = "+e);
			return 0;
		}
		
	}
	
	//get �����ƭ�
	public synchronized String[] getAllData(String tableName, int id, int num) {
		String str_arr[];
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select * from "+ tableName +" WHERE _id='" + id +"' ", null);
			//���������O�`��
			Cur.moveToFirst();
			
			/*
			//�ŧi
			str_arr = new String[num];
			*/
			
			//�ŧi
			str_arr = new String[num+1];
			//�s�W�@����ƨ�str_arr
			str_arr[num] = tableName; //�����O����Table
			
			for( int i=0;num!=0;i++ ) {
				//�u��O0
				str_arr[i] = Cur.getString(i);
				//if( str_arr[i]==null )
				//	str_arr[i] = String.valueOf(Cur.getInt(0));
				//Log.i("log","test srt_arr["+i+"] = "+str_arr[i]);
				//���V�e
				//Cur.moveToNext();
				num--;
			}

			if(Cur != null)
				Cur.close();
			return str_arr;
			
		} catch (Exception e) {
			Log.i("log","test (SQLiteDB) line957 = "+e);
		}

		return null;
	}

	public Cursor fetchAll() {
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			//Cursor Cur = database.rawQuery("Select * from sync_local_data WHERE userid!='" + SaveValue.UserName +"' ORDER BY annoText+annoImage+annoRecord DESC ", null);
			Cursor Cur = database.rawQuery("Select * from sync_local_data WHERE userid!='" + SaveValue.UserName +"' ORDER BY annoText+annoImage+annoRecord DESC ", null);
			//���������O�`��
			Cur.moveToFirst();
			return Cur;
		} catch (Exception e) {
			Log.i("log", "test (SQLiteDB) line10110 = "+e);
		}
		return null;
	}
	
	public Cursor fetchAll(String lesson) {
		try {
			SQLiteDatabase database = this.getWritableDatabase();
			Cursor Cur = database.rawQuery("Select * from sync_local_data WHERE userid!='" + SaveValue.UserName +"' ORDER BY "+lesson+" DESC ", null);
			//���������O�`��
			Cur.moveToFirst();
			return Cur;
		} catch (Exception e) {
			Log.i("log", "test (SQLiteDB) line1011 = "+e);
		}
		return null;
	}
	//---------------------------------------------------------------------
	//
	public synchronized String[] getSubtocpicName(String username, String lesson){
		try{
			String picName[];
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor cur;
			cur = db.rawQuery("Select picname from takepic WHERE userid='" + username + "' AND status!='DELETE' AND status!='DELETEOK' AND lesson='" + lesson + "' order by _id asc" , null);
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
			
		}
		return null;
	}
	//�p�⳹�`�̭��Ӥ��ƥ�
	public synchronized int getSubtocpicNum(String username, String lesson){
		try{
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor cur;
			cur = db.rawQuery("Select * from takepic WHERE userid='" + username + "' AND status!='DELETE' AND status!='DELETEOK' AND lesson='" + lesson + "'", null);
			int x = cur.getCount();
			//System.out.println("�ӳ��`�����Ӥ��ƥ�  : " + x);
			return x;
		}
		catch(Exception e){
			Log.i("log", "test (SQLiteDB) line1405 = "+e);
			return 0;
		}
	}
	//�ǥѩ��I�����Ϥ��W������ӹϤ����Ϥ����|
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
			Log.i("log", "test (SQLiteDB) line1420 = "+e);
			return null;
		}
	}
	//
	public synchronized void insertpicture(String username, String lesson, String picFileName, String picName){
		try{
			SQLiteDatabase db = this.getWritableDatabase();
			// �N�ק諸�ȩ�JContentValues
			ContentValues cv = new ContentValues();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			cv.put("date", sdf.format(new Date()));
			cv.put("picname", picName);
			cv.put("pic", picFileName);
			cv.put("status", "UPDATE");
			cv.put("userid", username);
			cv.put("lesson", lesson);
			db.insert("takepic", null, cv);
		}
		catch(Exception e){
			
		}
	}
	//
	//------------------����U�����
	public synchronized int[] getUserLesID(String username, String lesson){
		try{
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor cur;
			cur = db.rawQuery("Select _id from takepic WHERE userid='" + username + "' AND lesson='" + lesson + "' AND status != 'DELETE'" , null);
			int num = cur.getCount();
			int[] id = new int[num];
			cur.close();
			return id;
		}
		catch(Exception e){
		}
		return null;
	}
	//
	/*
	public synchronized int[] getUserLesPage(int id){
		try{
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor cur;
			cur = db.rawQuery("Select page from takepic WHERE _id='" + id + "'" + " AND status != 'DELET'" , null);
			int num = cur.getCount();
			int[] id = new int[num];
			cur.close();
			return id;
		}
		catch(Exception e){
		}
		return null;
	}
	*/
	/*
	//
	public synchronized int selectIdByPicSrc(String picSrc){
		try{
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor cur;
			cur = db.rawQuery("Select _id from takepic WHERE pic='" + picSrc + "' AND status!='DELET'",null);
			cur.moveToFirst();
			int num = cur.getInt(0);
			cur.close();
			return num;
		}
		catch(Exception e){
			System.out.println("���oID�ɳ���");
		}
		return 0;
	}
	*/
	//
	public synchronized void updatepicture(String username, String selectPicName,String NewpicName, String lesson){
		try{
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues cv = new ContentValues();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			cv.put("modifieddate", sdf.format(new Date()));
			cv.put("picname", NewpicName);
			cv.put("status", "UPDATE");
			String where = "userid" + " = ?" + " AND picname" + " = ?" + " AND lesson" + " = ?" + " AND status" + " != ?";
			String[] whereValue = { username, selectPicName, lesson, "DELETE"};
			db.update("takepic", cv, where, whereValue);
		}
		catch(Exception e){
			System.out.println("�ק異��!!!!!!!!!");
		}
	}
	//�p��ϥΪ̦b�ۤv���᪺�Ӥ��W���O���ƶq
		public synchronized int getTakepic_anno_Num_self(String TableName, String username, String lesson){
			try{
				SQLiteDatabase db = this.getWritableDatabase();
				Cursor cur;
				cur = db.rawQuery("Select * from " + TableName + " WHERE userid='" + username + "' AND lesson='" + lesson + "' AND picFilePath ='" + SaveValue.picNow + "' AND status !='DELETE' AND status !='DELETEOK'", null);
				int x = cur.getCount();
				return x;
			}
			catch(Exception e){
				Log.i("log","test (SQLiteDB) line1527 = "+e);
				return 0;
			}
		};
	//---------------------------------------------------------------------
	//���o�S�w�����W�ϥΪ̪��Ҧ����O��� (�^�ǵ��O��ƪ�ID)
		public synchronized int[] YangetAllAnno(String location, String tablename, String user, String lesson) {
			int num_arr[]; //�H�}�C�Φ��x�s�ӭ��W�ݩ�Y�ϥΪ̪��Ҧ����O���
			try {
				SQLiteDatabase database = this.getWritableDatabase();
				Cursor cur;
				cur = database.rawQuery("Select "+ location +" from "+ tablename + " WHERE userid='" + user + "' AND picFilePath='" + SaveValue.picNow + "' AND status !='DELETE' AND status !='DELETEOK'", null);
				//���������O�`��
				int num = cur.getCount();
				cur.moveToFirst();
				//�ŧi
				num_arr = new int[num];
				for( int i=0;num!=0;i++ ) {
					//�u��O0
					num_arr[i] = cur.getInt(0);
					//���V�e
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
}
