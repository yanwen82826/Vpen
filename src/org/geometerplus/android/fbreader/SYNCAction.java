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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.BackgroundColorAction;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.dialogs.DialogMoveLecture;
import org.geometerplus.zlibrary.core.dialogs.DialogMoveTabPic;
import org.geometerplus.zlibrary.core.sqliteconfig.ConnectMysql;
import org.geometerplus.zlibrary.core.sqliteconfig.SQLiteDB;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.text.view.SaveValue;
import com.thirdEdition.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.android.fbreader.library.LibraryTopLevelActivity;
import org.geometerplus.android.util.UIUtil;
import org.json.JSONArray;
import org.json.JSONObject;

class SYNCAction extends FBAndroidAction {
	
	public FBReaderApp Reader;
	public ProgressDialog pd;
	public ConnectMysql cm;
	/*0305
	private final static String DATABASE_INSERT = "http://140.115.126.179/connectdatabase/upload/connectMysql_insert.php";
	private final static String DATABASE_UPDATE = "http://140.115.126.179/connectdatabase/upload/connectMysql_update.php";
	private final static String DATABASE_DELETE = "http://140.115.126.179/connectdatabase/upload/connectMysql_delete.php";
	
	private final static String[] DBannoImage = { "id","anno_type","sX","sY","date","modifieddate","comment","userid","page","rec","p_id","status" };
	private final static String[] DBannoText = { "id","red","green","blue","type","date","modifieddate","comment","userid","page","rec","txt","pId","status" };
	private final static String[] DBannoText_drawRange = { "id","t_id","left","top","right","bottom","p_id","status" };
	private final static String[] DBtranslation = { "id","t_id","selected_text","num","userid","page","p_id", "status" };
	private final static String[] DBrecord = { "id","rec_id","anno_type","datetime","datetime_end","p_id", "status" };
	private final static String[] DBtranslation_num = { "id","translation_id","datetime","p_id","status" };
	private final static String[] DBtts_num= { "id","t_id","datetime","p_id","status" };
	private final static String[] DBparentIn_time = { "id","starttime","endtime","userid","status" };
	
	private final static String[] Database_Name = { ActionCode.ANNOTATION_IMAGE,ActionCode.ANNOTATION_TEXT,ActionCode.ANNOTATION_TEXT_RANGE,
		                                               ActionCode.ANNOTATION_PARENTIN_TIME,ActionCode.ANNOTATION_RECORD,ActionCode.ANNOTATION_TRANSLATION,
		                                               ActionCode.ANNOTATION_TRANSLATION_NUMBER,ActionCode.ANNOTATION_TTS_NUMBER };
	private final static int[] Database_Number = { 12,14,8,5,7,8,5,5 };
	*/
	
	//當按下 資料同步的按鈕時
	SYNCAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
		Reader = fbreader;
	}

	public void run() {
		//舊功能 ( 點選同步 ) (0305尚未更新)
		/*0305
		final SQLiteDB db = ZLApplication.Instance().db;
        int[] id_insert = db.getRangePage("_id", ActionCode.ANNOTATION_TEXT, SaveValue.UserName, "INSERT");  //取得文字註記ID (新增)
        int[] id_update = db.getRangePage("_id", ActionCode.ANNOTATION_TEXT, SaveValue.UserName, "UPDATE");  //取得文字註記ID (修改)
        int[] id_delete = db.getRangePage("_id", ActionCode.ANNOTATION_TEXT, SaveValue.UserName, "DELETE");  //取得文字註記ID (刪除)
		
		cm = new ConnectMysql(BaseActivity);
		
		pd = showDialog("請等候片刻...");
		pd.show();
		//UIUtil.showMessageText(this.BaseActivity, "未執行");
		
		if( id_insert.length > 0 ) {
			for(int i=0;i<id_insert.length;i++) {
				cm.syncAnno(annoDB(id_insert[i], 1), DBannoText, ActionCode.ANNOTATION_TEXT, DATABASE_INSERT);
			}
		}
		
		if( id_update.length > 0 ) {
			for(int i=0;i<id_update.length;i++) {
				cm.syncAnno(annoDB(id_update[i], 1), DBannoText, ActionCode.ANNOTATION_TEXT, DATABASE_UPDATE);
			}
		}
		
		if( id_delete.length > 0 ) {
			for(int i=0;i<id_delete.length;i++) {
				cm.syncAnno(annoDB(id_delete[i], 1), DBannoText, ActionCode.ANNOTATION_TEXT, DATABASE_DELETE);
			}
		}
		*/
		/*
		int result_t = 0;
		
		int id1 = 0;
		for(int i=0;i<MaxID;i++) {
			if( (i+1) == id[id1] ){
				cm.syncAnnotext(annoDB(id[id1], 0));
				id1++;
			}
			else { //要刪除的ID
				cm.syncAnnotextDel(i+1);
			}
		}
		
		int result_i = cm.syncAnnoimage();
		UIUtil.showMessageText(this.BaseActivity, "執行中");
		*/
		/*
		if(result_t == 0 && result_i == 0){
			UIUtil.showMessageText(this.BaseActivity, "成功連結資料庫!");
		}
		else
			UIUtil.showMessageText(this.BaseActivity, "error");
		*/
		/*0305
		UIUtil.showMessageText(this.BaseActivity, "檢驗完畢");
		pd.dismiss();
		//connecting();
		*/
	}
	
	/*0305
	//資料ID and 哪個資料表
	private String[] annoDB(int id, int tableId) {
		final SQLiteDB db = ZLApplication.Instance().db;
		String[] str = db.getAllData(Database_Name[tableId], id, Database_Number[tableId]);
		return str;
	}

	private ProgressDialog showDialog(String mes) {
		ProgressDialog dialog = new ProgressDialog(this.BaseActivity);
        dialog.setMessage(mes);
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        return dialog;
	}
	*/
}
