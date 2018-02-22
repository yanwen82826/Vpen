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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import org.geometerplus.android.util.UIUtil;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.sqliteconfig.SQLiteDB;

import org.geometerplus.zlibrary.text.view.SaveValue;
import com.thirdEdition.geometerplus.zlibrary.ui.android.R;

public class AnnotationSyncListAdapter extends BaseAdapter {

	private Context con;
	private Cursor c;
	private int index; //userid
	private int time; //syncTime
	private int text, image, record; //註記量
	private ArrayList listUserId=new ArrayList();
	private ArrayList listSyncTime=new ArrayList();
	private ArrayList listImage=new ArrayList(); //是否同步
	private ArrayList listNow=new ArrayList(); //是否觀摩其他使用者
	private ArrayList listCount = new ArrayList(); //註記量
	public Holder holder=null;
	//private int[] chapterPage = { 7,327,2457,4687,5185,7281,8985,9348,9859,10280,10289,10290 };
	private String[] chapter = { "L0","L1","L2","L3","L4","L5" };
	private int[] chapterPage = { 8,644,1145,1616,1632,2202,2686 };  //content005頁數
	
	public  AnnotationSyncListAdapter(Context context){
		con = context;
		
		/*
		final SQLiteDB db = ZLApplication.Instance().db;
		c=db.fetchAll();
		//UIUtil.showMessageText(con, "count1 = "+c.getCount());
		
		for(int i=0;i<c.getColumnCount();i++) {
			if(c.getColumnName(i).equals("userid")) {
				index=i;
			}
			if(c.getColumnName(i).equals("datetime")) {
				time=i;
			}
			if(c.getColumnName(i).equals("annoText")) {
				text=i;
			}
			if(c.getColumnName(i).equals("annoImage")) {
				image=i;
			}
			if(c.getColumnName(i).equals("annoRecord")) {
				record=i;
			}
		}
		c.moveToFirst();

		for(int i=0;i<c.getCount();i++) {
			listUserId.add(c.getString(index));
			listSyncTime.add(c.getString(time));
			listCount.add(c.getInt(text) + c.getInt(image) + c.getInt(record));
			c.moveToNext();
		}
		*/
		for( int i=0;i<SaveValue.log_Username.length;i++ ) {
			if( !SaveValue.log_Username[i].equals(SaveValue.UserName) ) {
				listUserId.add(SaveValue.log_Username[i]);
				listSyncTime.add("尚未同步");
				listCount.add(0);
			}
		}
			
	} 
	
	//清除同步
	public void clearData() {
		notifyDataSetChanged();
		synchronized (this) {
			listUserId = new ArrayList();
			listSyncTime = new ArrayList();
			listNow = new ArrayList();
			listImage=new ArrayList();
			for( int i=0;i<SaveValue.log_Username.length;i++ ) {
				if( !SaveValue.log_Username[i].equals(SaveValue.UserName) ) {
					listUserId.add(SaveValue.log_Username[i]);
					listSyncTime.add("尚未同步");
					listCount.add(0);
				}
			}
		}
		notifyDataSetChanged();
	}
	
	public void checkData() {
		notifyDataSetChanged();
		synchronized (this) {
			final SQLiteDB db = ZLApplication.Instance().db;
			
			String str = "";
			for(int j=0;j<chapterPage.length-1;j++) {
				if( SaveValue.pageIndex >= chapterPage[j] && SaveValue.pageIndex < chapterPage[j+1] )
					str = chapter[j];
			}
			if( str != "" )
				c=db.fetchAll(str);
			else
				c=db.fetchAll();
			//UIUtil.showMessageText(con, "count2 = "+c.getCount());
			for(int i=0;i<c.getColumnCount();i++) {
				if(c.getColumnName(i).equals("userid")) {
					index=i;
				}
				if(c.getColumnName(i).equals("datetime")) {
					time=i;
				}
				if(c.getColumnName(i).equals("annoText")) {
					text=i;
				}
				if(c.getColumnName(i).equals("annoImage")) {
					image=i;
				}
				if(c.getColumnName(i).equals("annoRecord")) {
					record=i;
				}
			}
			c.moveToFirst();
			listUserId = new ArrayList();
			listSyncTime = new ArrayList();
			listNow = new ArrayList();
			for(int i=0;i<c.getCount();i++) {
				listCount.add(c.getInt(text) + c.getInt(image) + c.getInt(record));
				listUserId.add(c.getString(index));
				listSyncTime.add(c.getString(time));
				if( !"尚未同步".equals(c.getString(time)) )
					listImage.add(i);
				if( !"".equals(SaveValue.SyncUserName) )
					listNow.add(i);

				c.moveToNext();
			}
			if( c != null )
				c.close();
		}
		notifyDataSetChanged();
	}
	
	public ArrayList getList() {
		return listUserId;
	}
	
	public boolean getListImageValue(int position) {
		//UIUtil.showMessageText(con, "listImage.getPosition = "+listImage.get(position));
		return !listImage.contains(position);
	}
	
	public String getListUserIdValue(int position) {
		return (String) listUserId.get(position);
	}
	    
	
	public int getCount() {
		// TODO Auto-generated method stub
		return listUserId.size();
	}

	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return getView(position, null, null);
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		if(convertView==null) {
			convertView=LayoutInflater.from(con).inflate(R.layout.list_layout_sync, null);
			holder=new Holder();
			//holder.icon=(ImageView)convertView.findViewById(R.id.icon);
			holder.userId=(TextView)convertView.findViewById(R.id.listUserId);
			holder.image=(ImageView)convertView.findViewById(R.id.listImageView);
			convertView.setTag(holder);
		} else {
			holder=(Holder)convertView.getTag();
		}

		holder.userId.setText((CharSequence) listUserId.get(position));
		holder.userId.setTextColor(Color.BLACK);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		if( listUserId.get(position).equals(SaveValue.SyncUserName) ) {
			holder.image.setImageBitmap(BitmapFactory.decodeStream(con.getResources().openRawResource(R.drawable.ic_list_sync_other)));
		}
		else if(  "尚未同步".equals(listSyncTime.get(position)) ) {
			holder.image.setImageBitmap(BitmapFactory.decodeStream(con.getResources().openRawResource(R.drawable.ic_list_sync_other_gray)));
		} else {
			holder.image.setImageBitmap(BitmapFactory.decodeStream(con.getResources().openRawResource(R.drawable.ic_list_sync_another)));
		}
		//holder.icon.setImageBitmap(icon);
		return convertView;
	}

	
	static class Holder {
		TextView userId;
		TextView userTime;
		ImageView image;
	}
	
	
	public Handler handler = new Handler() 
    {

        public void handleMessage(Message msg) 
        {
   	    switch (msg.what) 
	    {
	        // 當收到的Message的代號為我們剛剛訂的代號就做下面的動作。
	        case 0:
	            // 重繪UI
	        	clearData();
		    break;

            }
	    super.handleMessage(msg);
        }

    };

	
}
