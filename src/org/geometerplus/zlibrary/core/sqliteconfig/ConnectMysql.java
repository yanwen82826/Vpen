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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import android.os.StrictMode;
import android.util.Log;
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
import org.apache.http.protocol.HTTP;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.library.BooksDatabase;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.config.ZLConfig;
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.text.view.SaveValue;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class ConnectMysql {
	
	private String NowTable;
	private int NowTableId;
	private Context con;
	private String status;

	public ConnectMysql(Context context) {
		con = context;
	}
	
	//test
	private static ConnectMysql ourInstance;

	public static ConnectMysql Instance() {
		return ourInstance;
	}
	//test
	
	//連接資料庫( 上傳 )
	private synchronized String connectDatabaseUpdateData(ArrayList<NameValuePair> nameValuePair, String database){
		//数據
        InputStream is = null;
        
        //返回值  
        String result = "";
        
		//Http post  
        try {  
            /*建立一個HttpClient的一個對象*/  
            HttpClient httpClient = new DefaultHttpClient();
            HttpParams httpParams = httpClient.getParams();
            httpParams.setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
            /*創建一個HttpPost的對象*/  
            HttpPost httpPost = new HttpPost(database);  
             /*設置请求的數據*/  
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair,"UTF-8")); 
             /*創建HttpResponse对象，處理請求*/  
            HttpResponse response = httpClient.execute(httpPost);
            /*獲取這次回應的訊息*/  
            HttpEntity entity = response.getEntity();  
            /*把這些訊息指向對象實體的数據流*/  
            is = entity.getContent();
        } catch (Exception e) { 
        	//Log.i("test","test error = "+e);
        	//UIUtil.showMessageText(this.con, "無法連結資料庫!/n"+e.toString());
        }
          
        //對IS數據做處理
        try {  
            BufferedReader br = new BufferedReader(new InputStreamReader(is,  
            		"UTF-8"), 8);
            StringBuilder sb = new StringBuilder(); 
            String line = null;  
            while ((line = br.readLine()) != null) {
                //sb.append(line + "\n");  
            	sb.append(line);
            	result = line.trim();
            } 
            //result = sb.toString();
            //Log.i("test", "test result = "+result);
            //UIUtil.showMessageText(this.con, result);
            Log.i("test", "test result133 = "+result);
            
            /*
            System.out.println("aaa" + result.substring(1,8));
            System.out.println("bbb" + result.substring(0,12));
            System.out.println("ccc" + result.substring(1,15));
            System.out.println("ddd" + result.substring(1,20));
            */
            if( "success".equals(result.substring(2, 9)) ) { // 如果MySQL同步成功  更改Local端狀態
            	Log.i("test", "test result1332 = "+result);
            	dbChangeStatus(1);
            	//同步聆聽
            }
            else if( "sync success".equals(result.substring(2, 14))  ) {
            	Log.i("test", "test sync success");
            }
            else if( "delete success".equals(result.substring(2, 16))  ) {
            	Log.i("test", "test result1333 = "+result);
            	dbDeleteData();
            }
            else if( "sync record success".equals(result.substring(2, 21))  ) {
            	Log.i("test", "test result1334 = "+result);
            	dbChangeStatus(2);
            }
            //returnResult += result;
        } catch (Exception e) {
        	//UIUtil.showMessageText(this.con, "無法連結資料庫!/n"+e.toString());
            Log.e("log_tag", "Error converting result(141) "+e.toString());  
        } finally {
        	try {
        		is.close();//讀取完數據要關閉
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				//UIUtil.showMessageText(this.con, "無法連結資料庫!");
    			return null;
			}
        }
        
        return result;
	}
	
	//連接資料庫 ( IsSync:下載資料 )
	private synchronized JSONObject[] connectDatabaseDownData(ArrayList<NameValuePair> nameValuePair, String database){
		//数據
        InputStream is = null;
        //返回值  
        String result = "";
        String resultJSON = "";
		//Http post  
        try {  
            /*建立一個HttpClient的一個對象*/  
            HttpClient httpClient = new DefaultHttpClient();
            HttpParams httpParams = httpClient.getParams();
            httpParams.setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
            /*創建一個HttpPost的對象*/  
            HttpPost httpPost = new HttpPost(database);  
             /*設置请求的數據*/  
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));  

             /*創建HttpResponse对象，處理請求*/  
            HttpResponse response = httpClient.execute(httpPost);  
            /*獲取這次回應的訊息*/  
            HttpEntity entity = response.getEntity();  
            /*把這些訊息指向對象實體的数據流*/  
            is = entity.getContent();
        } catch (Exception e) {  
        	Log.e("log_tag", "Error converting result(194) "+e.toString());  
        	//UIUtil.showMessageText(this.con, "無法連結資料庫!/n"+e.toString());
        }

        //對IS數據做處理
        try {  
            BufferedReader br = new BufferedReader(new InputStreamReader(is,  
            		"UTF-8"), 8);  
            StringBuilder sb = new StringBuilder();  
            String line = null;  
            while ((line = br.readLine()) != null) {  
                //sb.append(line + "\n");
            	sb.append(line);
            	result = line.trim();
            } 
            //result = sb.toString().trim();
            if( result.isEmpty() || result.length() == 1 ){
            	return null;
            }
        } catch (Exception e) {
        	Log.e("log_tag", "Error converting result "+e.toString());
        } finally {
        	try {
        		is.close();//讀取完數據要關閉
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				//UIUtil.showMessageText(this.con, "無法連結資料庫!");
    			return null;
			}
        }
        
        //轉換為JSON類型
        try {
        	//轉換為json data類型  
            JSONArray jArray = new JSONArray(result);
            
            if (jArray.length() > 0) {  
            	JSONObject[] json_data = new JSONObject[jArray.length()];
                for (int i = 0; i < jArray.length(); i++) {  
                    json_data[i] = jArray.getJSONObject(i); 
                    resultJSON = json_data[i].toString();
                    //獲取mysql值
                    //returnResult += "Now status = " + json_data.getString("status") + "\n";
                    //Log.e("log_tag", "test status = "+json_data[i].getString("date"));
                }
                return json_data;
            }
        } catch (Exception e) {  
        	/*
        	Log.e("log_tag", "result.length "+result.length());
        	Log.e("log_tag", "result "+result);
        	Log.e("log_tag", "Error parsing data(235) "+database);
        	*/
        }
        return null;
	}
	
	//上傳檔案
	private synchronized String connectDatabaseUploadFile(String fileName, String type) {		
		String result = "error";
		String end = "\r\n";  
	    String twoHyphens = "--";  
	    String boundary = "******";  
	    String srcPath = "/sdcard/VPen/pic/"+fileName;
	    if( type.equals("rec") )
	    	srcPath = "/sdcard/VPen/rec/"+fileName;

	   // String srcPath = "../data/data/org.geometerplus.zlibrary.ui.android";
	    
		try {
			URL url = new URL(SaveValue.webIP+"/connectdatabase/localData/uploadFile.php");
			//URL url = new URL("http://140.115.135.135/connectdatabase/localData/uploadFile.php");
		    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();  
		    // 設置每次傳輸流大小，可以有效防止內存不足  
		    // 此方法用於在預先不知道内容長度時  
		    httpURLConnection.setChunkedStreamingMode(128 * 1024);// 128K  
		    // 允許输入输出流  
		    httpURLConnection.setDoInput(true);
		    httpURLConnection.setDoOutput(true);
		    httpURLConnection.setUseCaches(false);
		    // 使用POST方法  
		    httpURLConnection.setRequestMethod("POST");  
		    httpURLConnection.setRequestProperty("Connection", "Keep-Alive");  
		    httpURLConnection.setRequestProperty("Charset", "UTF-8");  
		    httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);  
		  
		    DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());  
		    dos.writeBytes(twoHyphens + boundary + end);  
		    dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\"; filename=\""  
		    				+ srcPath.substring(srcPath.lastIndexOf("/") + 1)  
		    				+ "\""  
		    				+ end);  
		    dos.writeBytes(end);  
		  
		    FileInputStream fis = new FileInputStream(srcPath);  
		    byte[] buffer = new byte[8192]; // 8k  
		    int count = 0;  
		    // 讀取文件  
		    while ((count = fis.read(buffer)) != -1)  
		    {  
		    	dos.write(buffer, 0, count);  
		    }  
		    fis.close();  
		  
		    dos.writeBytes(end);  
		    dos.writeBytes(twoHyphens + boundary + twoHyphens + end);  
		    dos.flush();  
		  
		    InputStream is = httpURLConnection.getInputStream();  
		    InputStreamReader isr = new InputStreamReader(is, "utf-8");  
		    BufferedReader br = new BufferedReader(isr);  
		    result = br.readLine();  
		  
		    //UIUtil.showMessageText(this.con, " result = "+result);
		    dos.close();  
		    is.close();
		    Log.i("test", "test upload file ok = "+fileName);
		} catch(Exception e) {
			//UIUtil.showMessageText(con, "error :" + e);
			Log.i("test", "test upload file error = "+e);
			result = "error";
		}
		
		return result;
	}
	
	//下載檔案
	private synchronized String connectDatabaseDownloadFile(String userName, String fileName, String type, String lesson, URL url) {
		
		String result = "error";
		String end = "\r\n";  
	    String twoHyphens = "--";  
	    String boundary = "******";  
	    //String srcPath = "/sdcard/rec/"+fileName;
	    String srcPath = "/mnt/sdcard/sync/"+userName+"/pic/";
	    if( type.equals("rec") ) {
	    	srcPath = "/mnt/sdcard/sync/"+userName+"/rec/";
	    }
	    else if(type.equals("takepic")){
	    	srcPath = "/mnt/sdcard/sync/"+userName+"/"+lesson+"/";
	    }
	    else if(type.equals("picAnno")){
	    	srcPath = "/mnt/sdcard/sync/"+userName+"/"+lesson+"/picAnno/";
	    }
	    else if(type.equals("recAnno")){
	    	srcPath = "/mnt/sdcard/sync/"+userName+"/"+lesson+"/recAnno/";
	    }
	    
		try {
			//URL url = new URL(SaveValue.webIP+"/connectdatabase/localData/"+userName+"/"+type+"/"+fileName);
			//URL url = new URL("http://140.115.135.135/connectdatabase/localData/"+userName+"/"+fileName);
			//Log.i("test", "test url = "+url);
		    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
		    InputStream is = httpURLConnection.getInputStream();
		    
		    //File ofe = new File("/mnt/sdcard/rec/"+userName+"/");
		    File ofe = new File(srcPath);
		    if(!ofe.exists()){
		    	ofe.mkdirs();
		    }
		    //Log.i("test", "test srcPath+fileName = "+srcPath+fileName);
		    ofe = new File(srcPath+fileName);
		    if(!ofe.exists()){
		    	ofe.createNewFile();
		    }
		    // write string
		    FileOutputStream fos = new FileOutputStream(ofe);
		    byte data[] = new byte[1024];
		    int length = 0, getPer = 0;
		    while((getPer = is.read(data))!=-1){
		    	length+=getPer;
		    	fos.write(data, 0, getPer);
		    }
		    
		    fos.flush();
		    fos.close();
		    is.close();
		    httpURLConnection.disconnect();
		    
		} catch(Exception e) {
			result = "error";
			Log.i("test", "ConnectMysql (Line366)");
			//Log.i("test", "test url e = "+e);
		}
		
		return result;
	}

	private synchronized void dbChangeStatus(int dbNumber) {
		try {
			//int key = 0;
			if(dbNumber == 1) {
				final SQLiteDB db = ZLApplication.Instance().db;
				//key = db.updateStatus(this.NowTableId, "_id", this.NowTable, 3, status);
				db.updateStatus(this.NowTableId, "_id", this.NowTable, 3, status);
			}
			else {
				final SyncDB db = ZLApplication.Instance().syncdb;
				//key = db.updateStatus(this.NowTableId, "_id", "record", 3, status);
				db.updateStatus(this.NowTableId, "_id", "record", 3, status);
			}
			//UIUtil.showMessageText(this.con, "nowTableId = "+this.NowTableId);
			//UIUtil.showMessageText(this.con, "table = "+this.NowTable);
			//UIUtil.showMessageText(this.con, "table = "+this.NowTableId);
			
		} catch(Exception e) {
			//Log.i("log", "test (ConnectMysql) error e= "+e);
		}
	}
	
	private synchronized void dbDeleteData() {
		try {
			final SQLiteDB db = ZLApplication.Instance().db;
			db.updateStatus(this.NowTableId, "_id", this.NowTable, 4, status);
			//db.delete( this.NowTableId, "_id", this.NowTable );  //刪除動作
			
		} catch(Exception e) {
			//Log.i("log", "test (ConnectMysql) error e= "+e);
		}
	}

	public synchronized int syncAnno(String[] list, String[] dbTable, String dbName, String status, String URL) {
		this.NowTable = dbName;
		this.NowTableId = Integer.valueOf(list[0]);
		this.status = status; //目前上傳至資料庫的資料狀態
		System.out.println("目前上傳置資料庫的狀態:" + this.status);
		
		ArrayList<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
		for(int i=0;i<dbTable.length;i++) {
			nameValuePair.add(new BasicNameValuePair(dbTable[i], list[i]));
			if( "rec".equals(dbTable[i]) || "pic".equals(dbTable[i]) ) {
				Log.i("test", "test dbTable[i] = "+dbTable[i]);
				Log.i("test", "test dbTable[i] = "+list[i]);
				if(list[i] != null) {
					UIUtil.showMessageText(this.con, connectDatabaseUploadFile(list[i], dbTable[i]));
				}
				
			}
		}
		nameValuePair.add(new BasicNameValuePair("tableName", list[dbTable.length]));
		connectDatabaseUpdateData(nameValuePair, URL);
		return 0;
	}
	
	public synchronized int syncAnnoEnd(String text, String image, String record, int[] chapter, String userid, String date, String URL) {
		
		ArrayList<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
		nameValuePair.add(new BasicNameValuePair("userid", userid));
		nameValuePair.add(new BasicNameValuePair("date", date));
		nameValuePair.add(new BasicNameValuePair("text", text));
		nameValuePair.add(new BasicNameValuePair("image", image));
		nameValuePair.add(new BasicNameValuePair("record", record));
		nameValuePair.add(new BasicNameValuePair("tableName", "sync"));
		String[] str = {"L0","L1","L2","L3","L4","L5","L6","L7","L8","L9","L10"};
		for(int i=0;i<chapter.length;i++) {
			nameValuePair.add(new BasicNameValuePair(str[i], String.valueOf(chapter[i])));
		}
		
		

		//connectDatabaseUploadFile("123");
		connectDatabaseUpdateData(nameValuePair, URL);
		return 0;
	}
	
	public synchronized int syncAnnoDownload(String userid, String st, String dbName, String URL) {
		ArrayList<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
		nameValuePair.add(new BasicNameValuePair("tableName", dbName));
		nameValuePair.add(new BasicNameValuePair("userid", userid));
		nameValuePair.add(new BasicNameValuePair("status", st)); //要下載哪種狀態的資料
		System.out.println("狀態為:" + st);
		JSONObject[] json = connectDatabaseDownData(nameValuePair, URL);
		if( json == null ) {
			return 1;
		}
		//if( json != null ) //回傳server端
			//syncAnno(String[] list, String[] dbTable, String dbName, String URL);
		//Log.i("test", "test connectSyncDb json(syncAnnoDownload) = "+json);
		//if( json != null ) {
			try {
				SaveValue.syncUserOK.clear(); //清空
				changeLocalDataBase(json, st, dbName);
				updateSyncData();
			} 
			catch(Exception e) {
				Log.i("test", "test connectSyncDb error(syncAnnoDownload) = "+e);
			}
		//}
		
		return 0;
	}
	
	private void updateSyncData() {
		final SyncDB syncdb = ZLApplication.Instance().syncdb;
		final SQLiteDB db = ZLApplication.Instance().db;
		int[] chapterPage = { 8,644,1145,1616,1632,2202,2686 };
		int[] chapterAnnoNum = new int[6];
		try {
			//Log.i("test", "test db error(476) = "+SaveValue.syncUserOK.size());
			for(int i=0;i<SaveValue.syncUserOK.size();i++) {
				int temp1 = syncdb.getTableCount(ActionCode.ANNOTATION_TEXT, (String) SaveValue.syncUserOK.get(i), "DELETE");
				int temp2 = syncdb.getTableCount(ActionCode.ANNOTATION_IMAGE, (String) SaveValue.syncUserOK.get(i), "DELETE");
				int temp3 = syncdb.getVoiceCount((String) SaveValue.syncUserOK.get(i), "DELETE");
				//記錄每章節的註記數量
				for(int j=0;j<chapterPage.length-1;j++) {
					chapterAnnoNum[j] = 0;
					chapterAnnoNum[j] = syncdb.getTableCount(ActionCode.ANNOTATION_TEXT, (String) SaveValue.syncUserOK.get(i), chapterPage[j], chapterPage[j+1], "DELETE")
										+ syncdb.getTableCount(ActionCode.ANNOTATION_IMAGE, (String) SaveValue.syncUserOK.get(i), chapterPage[j], chapterPage[j+1], "DELETE")
										+ syncdb.getVoiceCount((String) SaveValue.syncUserOK.get(i), chapterPage[j], chapterPage[j+1], "DELETE");
					//Log.i("test2", "test chapterAnnoNum["+j+"]="+chapterAnnoNum[j]);
					//Log.i("test2", "test s1 = "+(String) SaveValue.syncUserOK.get(i));
				}
				
				if( db.updateSyncData((String) SaveValue.syncUserOK.get(i), "userid", ActionCode.ANNOTATION_SYNC_DATA, temp1, temp2, temp3, chapterAnnoNum) == 2 ) {
					Log.i("test", "updateSync = OK");
				}
			}
			SaveValue.IsUpdateListView = true; //更新數據
		} catch (Exception e) {
			Log.e("connectMysql", "test error = "+e);
		}
	}
	
	public synchronized void changeLocalDataBase(JSONObject[] json_data, String st, String dbName) {
		final SyncDB syncdb = ZLApplication.Instance().syncdb;
		final SQLiteDB db = ZLApplication.Instance().db;
		int i;
		System.out.println("更新中的資料表為: " + dbName);
		System.out.println("更新中的狀態為: " + st);
		if( dbName.equals( ActionCode.ANNOTATION_TEXT ) ) {
			try {
				URL url;
				if( "INSERT".equals(st) ) {          //新增	
					
					for(i=0;i<json_data.length;i++) {
						//_id red green blue type date modifieddate comment userid page rec txt p_id status
						syncdb.insertNote( json_data[i].getInt("_id"), json_data[i].getInt("red"), json_data[i].getInt("green"), json_data[i].getInt("blue"), 
											json_data[i].getString("type"), json_data[i].getString("date"), json_data[i].getString("modifieddate"), json_data[i].getString("comment"),
											json_data[i].getString("userid"), json_data[i].getInt("page"), json_data[i].getString("rec"), json_data[i].getString("txt"), json_data[i].getString("pic") );
						
						//下載錄音 //如果REC不是空的，則下載DATA
						if( !"".equals(json_data[i].getString("rec")) ) {
							url = new URL(SaveValue.webIP+"/connectdatabase/localData/"+json_data[i].getString("userid")+"/rec/"+json_data[i].getString("rec"));
							connectDatabaseDownloadFile(json_data[i].getString("userid"), json_data[i].getString("rec"), "rec", null,url);
						}
						//下載照片 //如果PIC不是空的，則下載DATA
						if( !"".equals(json_data[i].getString("pic")) ) {
							url = new URL(SaveValue.webIP+"/connectdatabase/localData/"+json_data[i].getString("userid")+"/pic/"+json_data[i].getString("pic"));
							connectDatabaseDownloadFile(json_data[i].getString("userid"), json_data[i].getString("pic"), "pic", null,url);
						}
						if( SaveValue.syncUserOK.isEmpty() || SaveValue.syncUserOK.indexOf(json_data[i].getString("userid")) == -1 )
							SaveValue.syncUserOK.add(json_data[i].getString("userid"));
						//Log.i("test", "test boolean 601 = "+SaveValue.syncUserOK.size());
						//db.updateSyncData(json_data[i].getString("userid"), "userid", ActionCode.ANNOTATION_SYNC_DATA);
					}
		
				} else if( "UPDATE".equals(st) ) {  //修改
					
					for(i=0;i<json_data.length;i++) {
						//_id red green blue type date modifieddate comment userid page rec txt p_id status
						syncdb.updateNote( json_data[i].getInt("_id"), json_data[i].getInt("red"), json_data[i].getInt("green"), json_data[i].getInt("blue"), 
											json_data[i].getString("type"), json_data[i].getString("date"), json_data[i].getString("modifieddate"), json_data[i].getString("comment"),
											json_data[i].getString("userid"), json_data[i].getInt("page"), json_data[i].getString("rec"), json_data[i].getString("txt"), json_data[i].getString("pic") );
						
						//下載錄音 //如果REC不是空的，則下載DATA
						if( !"".equals(json_data[i].getString("rec")) ) {
							url = new URL(SaveValue.webIP+"/connectdatabase/localData/"+json_data[i].getString("userid")+"/rec/"+json_data[i].getString("rec"));
							connectDatabaseDownloadFile(json_data[i].getString("userid"), json_data[i].getString("rec"), "rec", null, url);
						}
						//下載照片 //如果PIC不是空的，則下載DATA
						if( !"".equals(json_data[i].getString("pic")) ) {
							url = new URL(SaveValue.webIP+"/connectdatabase/localData/"+json_data[i].getString("userid")+"/pic/"+json_data[i].getString("pic"));
							connectDatabaseDownloadFile(json_data[i].getString("userid"), json_data[i].getString("pic"), "pic", null, url);
						}
						
						if( SaveValue.syncUserOK.isEmpty() || SaveValue.syncUserOK.indexOf(json_data[i].getString("userid")) == -1 )
							SaveValue.syncUserOK.add(json_data[i].getString("userid"));
						//db.updateSyncData(json_data[i].getString("userid"), "userid", ActionCode.ANNOTATION_SYNC_DATA);
					}
					
				} else if( "DELETE".equals(st) ) {  //刪除
					
					for(i=0;i<json_data.length;i++) {
						//_id red green blue type date modifieddate comment userid page rec txt p_id status
						syncdb.updateStatus( json_data[i].getInt("_id"), json_data[i].getString("userid"), dbName );
						//db.updateSyncData(json_data[i].getString("userid"), "userid", ActionCode.ANNOTATION_SYNC_DATA);
						if( SaveValue.syncUserOK.isEmpty() || SaveValue.syncUserOK.indexOf(json_data[i].getString("userid")) == -1 )
							SaveValue.syncUserOK.add(json_data[i].getString("userid"));
					}
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ( dbName.equals( ActionCode.ANNOTATION_IMAGE ) ) {
			try {
				URL url;
				if( "INSERT".equals(st) ) {          //新增
					for(i=0;i<json_data.length;i++) {
						//_id annoType sX sY date modifieddate comment userid page rec pic 
						syncdb.insertPicNote( json_data[i].getInt("_id"), json_data[i].getString("annoType"), json_data[i].getInt("sX"), json_data[i].getInt("sY"), json_data[i].getString("date"),
											json_data[i].getString("modifieddate"), json_data[i].getString("comment"), json_data[i].getString("userid"), json_data[i].getInt("page"),
											json_data[i].getString("rec"), json_data[i].getString("pic") );
						//下載錄音 //如果REC不是空的，則下載DATA
						if( !"".equals(json_data[i].getString("rec")) ) {
							url = new URL(SaveValue.webIP+"/connectdatabase/localData/"+json_data[i].getString("userid")+"/rec/"+json_data[i].getString("rec"));
							connectDatabaseDownloadFile(json_data[i].getString("userid"), json_data[i].getString("rec"), "rec", null, url);
						}
						//下載照片 //如果PIC不是空的，則下載DATA
						if( !"".equals(json_data[i].getString("pic")) ) {
							url = new URL(SaveValue.webIP+"/connectdatabase/localData/"+json_data[i].getString("userid")+"/pic/"+json_data[i].getString("pic"));
							connectDatabaseDownloadFile(json_data[i].getString("userid"), json_data[i].getString("pic"), "pic", null, url);
						}
							
						//db.updateSyncData(json_data[i].getString("userid"), "userid", ActionCode.ANNOTATION_SYNC_DATA);
						if( SaveValue.syncUserOK.isEmpty() || SaveValue.syncUserOK.indexOf(json_data[i].getString("userid")) == -1 )
							SaveValue.syncUserOK.add(json_data[i].getString("userid"));
						//Log.i("test", "test boolean 549 = "+SaveValue.syncUserOK.size());
					}
				} else if( "UPDATE".equals(st) ) {  //修改
					System.out.println("系統更新");
					for(i=0;i<json_data.length;i++) {
						//_id annoType sX sY date modifieddate comment userid page rec pic
						syncdb.updatePicNote( json_data[i].getInt("_id"), json_data[i].getString("annoType"), json_data[i].getInt("sX"), json_data[i].getInt("sY"), json_data[i].getString("date"),
											json_data[i].getString("modifieddate"), json_data[i].getString("comment"), json_data[i].getString("userid"), json_data[i].getInt("page"),
											json_data[i].getString("rec"), json_data[i].getString("pic") );
						//下載錄音 //如果REC不是空的，則下載DATA
						if( !"".equals(json_data[i].getString("rec")) ) {
							url = new URL(SaveValue.webIP+"/connectdatabase/localData/"+json_data[i].getString("userid")+"/rec/"+json_data[i].getString("rec"));
							connectDatabaseDownloadFile(json_data[i].getString("userid"), json_data[i].getString("rec"), "rec", null, url);
						}
						//下載照片 //如果PIC不是空的，則下載DATA
						if( !"".equals(json_data[i].getString("pic")) ) {
							url = new URL(SaveValue.webIP+"/connectdatabase/localData/"+json_data[i].getString("userid")+"/pic/"+json_data[i].getString("pic"));
							connectDatabaseDownloadFile(json_data[i].getString("userid"), json_data[i].getString("pic"), "pic", null, url);
						}
						
						//db.updateSyncData(json_data[i].getString("userid"), "userid", ActionCode.ANNOTATION_SYNC_DATA);
						if( SaveValue.syncUserOK.isEmpty() || SaveValue.syncUserOK.indexOf(json_data[i].getString("userid")) == -1 )
							SaveValue.syncUserOK.add(json_data[i].getString("userid"));
					}
					
				} else if( "DELETE".equals(st) ) {  //刪除
					System.out.println("系統刪除");
					for(i=0;i<json_data.length;i++) {
						//_id red green blue type date modifieddate comment userid page rec txt p_id status
						syncdb.updateStatus( json_data[i].getInt("_id"), json_data[i].getString("userid"), dbName );
						//db.updateSyncData(json_data[i].getString("userid"), "userid", ActionCode.ANNOTATION_SYNC_DATA);
						if( SaveValue.syncUserOK.isEmpty() || SaveValue.syncUserOK.indexOf(json_data[i].getString("userid")) == -1 )
							SaveValue.syncUserOK.add(json_data[i].getString("userid"));
					}
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ( dbName.equals( ActionCode.ANNOTATION_TEXT_RANGE ) ) {
			try {
				if( "INSERT".equals(st) ) {          //新增
					
					for(i=0;i<json_data.length;i++) {
						//_id t_id(annoText's _id) left top right bottom userid p_id status
						syncdb.insertRange( json_data[i].getInt("t_id"), json_data[i].getInt("left"), json_data[i].getInt("top"), json_data[i].getInt("right"), json_data[i].getInt("bottom"),
											json_data[i].getString("userid") );
					}
				} else if( "UPDATE".equals(st) ) {  //修改
					//有需要再修改( 註記範圍 )
				} else if( "DELETE".equals(st) ) {  //刪除
					for(i=0;i<json_data.length;i++) {
						//_id red green blue type date modifieddate comment userid page rec txt p_id status
						syncdb.updateStatus( json_data[i].getInt("_id"), json_data[i].getString("userid"), dbName );
						//db.updateSyncData(json_data[i].getString("userid"), "userid", ActionCode.ANNOTATION_SYNC_DATA);
					}
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else if ( dbName.equals(ActionCode.ANNOTATION_TAKEPIC)){
			try {
				URL url;
				if( "INSERT".equals(st) ) {          //新增
					for(i=0;i<json_data.length;i++) {
						//_id date modifieddate userid lesson pic status picname
						syncdb.insertTakePic( json_data[i].getInt("_id"), json_data[i].getString("date"), json_data[i].getString("modifieddate"), json_data[i].getString("userid"), json_data[i].getString("lesson"),
											json_data[i].getString("pic"), json_data[i].getString("picname"));
						//下載照片 //如果PIC不是空的，則下載DATA
						if( !"".equals(json_data[i].getString("pic")) ) {
							url = new URL(SaveValue.webIP+"/connectdatabase/localData/"+json_data[i].getString("userid")+"/"+json_data[i].getString("lesson")+"/"+json_data[i].getString("pic"));
							connectDatabaseDownloadFile(json_data[i].getString("userid"), json_data[i].getString("pic"), "takepic", json_data[i].getString("lesson"), url);
						}
						//db.updateSyncData(json_data[i].getString("userid"), "userid", ActionCode.ANNOTATION_SYNC_DATA);
						if( SaveValue.syncUserOK.isEmpty() || SaveValue.syncUserOK.indexOf(json_data[i].getString("userid")) == -1 )
							SaveValue.syncUserOK.add(json_data[i].getString("userid"));
						//Log.i("test", "test boolean 549 = "+SaveValue.syncUserOK.size());
					}
				} else if( "UPDATE".equals(st) ) {  //修改
					
					for(i=0;i<json_data.length;i++) {
						//_id annoType sX sY date modifieddate comment userid page rec p_id status
						syncdb.updateTakePic( json_data[i].getInt("_id"), json_data[i].getString("date"), json_data[i].getString("modifieddate"),
											json_data[i].getString("userid"), json_data[i].getString("lesson"), json_data[i].getString("pic"), json_data[i].getString("picname") );
						//下載照片 //如果PIC不是空的，則下載DATA
						if( !"".equals(json_data[i].getString("pic")) ) {
							url = new URL(SaveValue.webIP+"/connectdatabase/localData/"+json_data[i].getString("userid")+"/"+json_data[i].getString("lesson")+"/"+json_data[i].getString("pic"));
							connectDatabaseDownloadFile(json_data[i].getString("userid"), json_data[i].getString("pic"), "takepic",json_data[i].getString("lesson"), url);
						}
						
						//db.updateSyncData(json_data[i].getString("userid"), "userid", ActionCode.ANNOTATION_SYNC_DATA);
						if( SaveValue.syncUserOK.isEmpty() || SaveValue.syncUserOK.indexOf(json_data[i].getString("userid")) == -1 )
							SaveValue.syncUserOK.add(json_data[i].getString("userid"));
					}
					
				} else if( "DELETE".equals(st) ) {  //刪除
					
					for(i=0;i<json_data.length;i++) {
						//_id red green blue type date modifieddate comment userid page rec txt p_id status
						syncdb.updateStatus( json_data[i].getInt("_id"), json_data[i].getString("userid"), dbName );
						//db.updateSyncData(json_data[i].getString("userid"), "userid", ActionCode.ANNOTATION_SYNC_DATA);
						if( SaveValue.syncUserOK.isEmpty() || SaveValue.syncUserOK.indexOf(json_data[i].getString("userid")) == -1 )
							SaveValue.syncUserOK.add(json_data[i].getString("userid"));
					}
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else if(dbName.equals(ActionCode.ANNOTATION_TAKEPIC_ANNO)){
			try {
				URL url;
				if( "INSERT".equals(st) ) {          //新增
					for(i=0;i<json_data.length;i++) {
						syncdb.insertTakePic_Anno( json_data[i].getInt("_id"), json_data[i].getString("annoType"), json_data[i].getInt("sX"), json_data[i].getInt("sY"), json_data[i].getString("date"), json_data[i].getString("modifieddate"), 
											json_data[i].getString("comment"), json_data[i].getString("userid"), json_data[i].getInt("page"),json_data[i].getString("lesson"),
											json_data[i].getString("picFilePath"), json_data[i].getString("rec"), json_data[i].getString("pic"),json_data[i].getInt("p_id"));
						//下載錄音 //如果REC不是空的，則下載DATA
						if( !"".equals(json_data[i].getString("rec")) ) {
							url = new URL(SaveValue.webIP+"/connectdatabase/localData/"+json_data[i].getString("userid")+"/"+json_data[i].getString("lesson")+"/recAnno/"+json_data[i].getString("rec"));
							connectDatabaseDownloadFile(json_data[i].getString("userid"), json_data[i].getString("rec"), "recAnno", json_data[i].getString("lesson"), url);
						}
						//下載照片 //如果PIC不是空的，則下載DATA
						if( !"".equals(json_data[i].getString("pic")) ) {
							url = new URL(SaveValue.webIP+"/connectdatabase/localData/"+json_data[i].getString("userid")+"/"+json_data[i].getString("lesson")+"/picAnno/"+json_data[i].getString("pic"));
							connectDatabaseDownloadFile(json_data[i].getString("userid"), json_data[i].getString("pic"), "picAnno", json_data[i].getString("lesson"), url);
						}
							
						//db.updateSyncData(json_data[i].getString("userid"), "userid", ActionCode.ANNOTATION_SYNC_DATA);
						if( SaveValue.syncUserOK.isEmpty() || SaveValue.syncUserOK.indexOf(json_data[i].getString("userid")) == -1 )
							SaveValue.syncUserOK.add(json_data[i].getString("userid"));
					}
				} else if( "UPDATE".equals(st) ) {  //修改
					
					for(i=0;i<json_data.length;i++) {
						syncdb.updateTakePic_Anno( json_data[i].getInt("_id"), json_data[i].getString("annoType"), json_data[i].getInt("sX"), json_data[i].getInt("sY"), json_data[i].getString("date"), json_data[i].getString("modifieddate"), 
								json_data[i].getString("comment"), json_data[i].getString("userid"), json_data[i].getInt("page"),json_data[i].getString("lesson"),
								json_data[i].getString("picFilePath"), json_data[i].getString("rec"), json_data[i].getString("pic"),json_data[i].getInt("p_id"));
						//下載錄音 //如果REC不是空的，則下載DATA
						if( !"".equals(json_data[i].getString("rec")) ) {
							url = new URL(SaveValue.webIP+"/connectdatabase/localData/"+json_data[i].getString("userid")+"/"+json_data[i].getString("lesson")+"/recAnno/"+json_data[i].getString("rec"));
							connectDatabaseDownloadFile(json_data[i].getString("userid"), json_data[i].getString("rec"), "recAnno", json_data[i].getString("lesson"), url);
						}
						//下載照片 //如果PIC不是空的，則下載DATA
						if( !"".equals(json_data[i].getString("pic")) ) {
							url = new URL(SaveValue.webIP+"/connectdatabase/localData/"+json_data[i].getString("userid")+"/"+json_data[i].getString("lesson")+"/picAnno/"+json_data[i].getString("pic"));
							connectDatabaseDownloadFile(json_data[i].getString("userid"), json_data[i].getString("pic"), "picAnno", json_data[i].getString("lesson"), url);
						}
						
						//db.updateSyncData(json_data[i].getString("userid"), "userid", ActionCode.ANNOTATION_SYNC_DATA);
						if( SaveValue.syncUserOK.isEmpty() || SaveValue.syncUserOK.indexOf(json_data[i].getString("userid")) == -1 )
							SaveValue.syncUserOK.add(json_data[i].getString("userid"));
					}
					
				} else if( "DELETE".equals(st) ) {  //刪除
					
					for(i=0;i<json_data.length;i++) {
						//_id red green blue type date modifieddate comment userid page rec txt p_id status
						syncdb.updateStatus( json_data[i].getInt("_id"), json_data[i].getString("userid"), dbName );
						//db.updateSyncData(json_data[i].getString("userid"), "userid", ActionCode.ANNOTATION_SYNC_DATA);
						if( SaveValue.syncUserOK.isEmpty() || SaveValue.syncUserOK.indexOf(json_data[i].getString("userid")) == -1 )
							SaveValue.syncUserOK.add(json_data[i].getString("userid"));
					}
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	public int syncAnnotextDel(int id) {
		ArrayList<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
		nameValuePair.add(new BasicNameValuePair("del", String.valueOf(id)));
		nameValuePair.add(new BasicNameValuePair("userid", String.valueOf(SaveValue.UserName)));
		connectDatabase(nameValuePair, this.DATABASE_AnnoText);
		Log.i("test", "test del id ="+id);
		Log.i("test", "test del id ="+SaveValue.UserName);
		return 0;
	}

	public int syncAnnoimage() {
		ArrayList<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
		nameValuePair.add(new BasicNameValuePair("id", "test"));
		connectDatabase(nameValuePair, this.DATABASE_AnnoImage);
		return 0;
	}
	*/
	
	//上傳DB檔
	public synchronized int syncUpdateDBfile() {
		String[] DBstr = {"books.db","books.db-journal","config.db","config.db-journal","FBReader_note.db","FBReader_note.db-journal","sync.db","sync.db-journal"};
	    String[] LIBstr = {"libDeflatingDecompressor.so","libLineBreak.so"};
	    //String srcPath = "/data/data/org.geometerplus.zlibrary.ui.android/";
	    String srcPath = "/data/data/com.thirdEdition.geometerplus.zlibrary.ui.android/";
	    String DBPath = "databases";
	    String LIBPath = "lib";
	    
	    for(int i=0;i<DBstr.length;i++) {
	    	connectDatabaseUploadFile2(SaveValue.webIP, srcPath,DBPath,DBstr[i]);
	    	//connectDatabaseUploadFile2("140.115.135.135", srcPath,DBPath,DBstr[i]);
	    }
	    for(int i=0;i<LIBstr.length;i++) {
	    	connectDatabaseUploadFile2(SaveValue.webIP, srcPath,LIBPath,LIBstr[i]);
	    	//connectDatabaseUploadFile2("140.115.135.135", srcPath,LIBPath,LIBstr[i]);
	    }

		return 0;
	}
	
	//上傳檔案(.db)
	private synchronized String connectDatabaseUploadFile2(String web, String srcPath, String doc, String fileName) {
		String result = "error";
		String end = "\r\n";  
	    String twoHyphens = "--";  
	    String boundary = "******";
	    srcPath = srcPath+doc+"/"+fileName;
	    
	    
		try {
			URL url = new URL(web+"/connectdatabase/localData/uploadFile2.php?user="+SaveValue.UserName+"&doc="+doc);
			//URL url = new URL("http://140.115.135.135/connectdatabase/localData/uploadFile.php");
		    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();  
		    // 設置每次傳輸流大小，可以有效防止內存不足  
		    // 此方法用於在預先不知道内容長度時  
		    httpURLConnection.setChunkedStreamingMode(128 * 1024);// 128K  
		    // 允許输入输出流  
		    httpURLConnection.setDoInput(true);  
		    httpURLConnection.setDoOutput(true);  
		    httpURLConnection.setUseCaches(false);  
		    // 使用POST方法  
		    httpURLConnection.setRequestMethod("POST");  
		    httpURLConnection.setRequestProperty("Connection", "Keep-Alive");  
		    httpURLConnection.setRequestProperty("Charset", "UTF-8");  
		    httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
		    httpURLConnection.setRequestProperty("Cookie", SaveValue.UserName);
		  
		    DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());
		    //dos.writeBytes("user="+SaveValue.UserName);
		    dos.writeBytes(twoHyphens + boundary + end);  
		    dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\"; filename=\""  
		    				+ srcPath.substring(srcPath.lastIndexOf("/") + 1)  
		    				+ "\""  
		    				+ end);  
		   
		    dos.writeBytes(end);  
		  
		    FileInputStream fis = new FileInputStream(srcPath);  
		    byte[] buffer = new byte[8192]; // 8k  
		    int count = 0;  
		    // 讀取文件  
		    while ((count = fis.read(buffer)) != -1)
		    {  
		    	dos.write(buffer, 0, count);  
		    }  
		    fis.close();  
		  
		    dos.writeBytes(end);  
		    dos.writeBytes(twoHyphens + boundary + twoHyphens + end);  
		    dos.flush();  
		  
		    InputStream is = httpURLConnection.getInputStream();  
		    InputStreamReader isr = new InputStreamReader(is, "utf-8");  
		    BufferedReader br = new BufferedReader(isr);  
		    result = br.readLine();  
		  
		    //UIUtil.showMessageText(this.con, " result = "+result);
		    dos.close();  
		    is.close();
		} catch(Exception e) {
			//UIUtil.showMessageText(con, "error :" + e);
			//Log.i("test", "test error = "+e);
			result = "error";
		}
		
		return result;
	}
	
	//下載DB檔
	public synchronized int syncDownloadDBfile() {
		String[] DBstr = {"books.db","books.db-journal","config.db","config.db-journal","FBReader_note.db","FBReader_note.db-journal","sync.db","sync.db-journal"};
	    String[] LIBstr = {"libDeflatingDecompressor.so","libLineBreak.so"};
	    //String srcPath = "/data/data/org.geometerplus.zlibrary.ui.android/";
	    String srcPath = "/data/data/com.thirdEdition.geometerplus.zlibrary.ui.android/";
	    String DBPath = "databases";
	    String LIBPath = "lib";

	    for(int i=0;i<DBstr.length;i++) {
	    	if( i != DBstr.length-1 )
	    		connectDatabaseDownloadFile2(srcPath,DBPath,DBstr[i], false);
	    	else
	    		connectDatabaseDownloadFile2(srcPath,DBPath,DBstr[i], true);
	    }
	    for(int i=0;i<LIBstr.length;i++)
	    	connectDatabaseDownloadFile2(srcPath,LIBPath,LIBstr[i], false);
	    
		return 0;
	}	
	
	//下載檔案(.db)
	private synchronized String connectDatabaseDownloadFile2(String srcPath, String doc, String fileName, boolean key) {
		
		String result = "error";
		String end = "\r\n";  
	    String twoHyphens = "--";  
	    String boundary = "******";  
	    String user = SaveValue.UserName;
	    
		try {
			URL url = new URL(SaveValue.webIP+"/connectdatabase/localData/"+user+"/final/"+doc+"/"+fileName);
			//URL url = new URL("http://140.115.135.135/connectdatabase/localData/"+userName+"/"+fileName);
			//Log.i("test", "test url = "+url);
		    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
		    InputStream is = httpURLConnection.getInputStream();
		    
		    File ofe = new File(srcPath+"/"+doc+"/");
		    if(!ofe.exists()){
		    	ofe.mkdirs();
		    }
		    ofe = new File(srcPath+"/"+doc+"/"+fileName);
		    if(!ofe.exists()){
		    	ofe.createNewFile();
		    }
		    // write string
		    FileOutputStream fos = new FileOutputStream(ofe);
		    byte data[] = new byte[1024];
		    int length = 0, getPer = 0;
		    while((getPer = is.read(data))!=-1){
		    	length+=getPer;
		    	fos.write(data, 0, getPer);
		    }
		    
		    fos.flush();
		    fos.close();
		    is.close();
		    httpURLConnection.disconnect();
		    
		} catch(Exception e) {
			result = "error";
			//Log.i("test", "test url e = "+e);
		}
		
		if(key) {
			UIUtil.showMessageText(con, "同步完成");
		}
		return result;
	}	
	
	//共享數據是否開啟
	public synchronized boolean IsSyncAnno(String URL) {
		
		//数據
        InputStream is = null;
        //返回值  
        String result = "";
        
        ArrayList<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		nameValuePair.add(new BasicNameValuePair("NowTime", sdf.format(new Date())));
        
		//Http post  
        try {  
            /*建立一個HttpClient的一個對象*/  
            HttpClient httpClient = new DefaultHttpClient();
            HttpParams httpParams = httpClient.getParams();
            httpParams.setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
            /*創建一個HttpPost的對象*/  
            HttpPost httpPost = new HttpPost(URL);  
             /*設置请求的數據*/  
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));  

             /*創建HttpResponse对象，處理請求*/  
            HttpResponse response = httpClient.execute(httpPost);  
            /*獲取這次回應的訊息*/  
            HttpEntity entity = response.getEntity();  
            /*把這些訊息指向對象實體的数據流*/  
            is = entity.getContent();
        } catch (Exception e) {  
        	Log.e("log_tag", "Error converting result(183) "+e.toString());  
        }

        //對IS數據做處理
        try {  
            BufferedReader br = new BufferedReader(new InputStreamReader(is,  
            		"UTF-8"), 8);  
            StringBuilder sb = new StringBuilder();  
            String line = null;  
            while ((line = br.readLine()) != null) {  
                //sb.append(line + "\n");  
            	sb.append(line);
            } 
            result = sb.toString();
            //Log.i("test", "test result = "+result);
        } catch (Exception e) {
        	Log.e("log_tag", "Error converting result "+e.toString());
        } finally {
        	try {
        		is.close();//讀取完數據要關閉
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
        }
        //Log.i("test", "test result = ??");
        if( "true".equals(result) ) {
        	//Log.i("test", "test result = YES");
        	return true;
        } else {
        	//Log.i("test", "test result = NO");
        	return false;
        }
	}
	
	

}
