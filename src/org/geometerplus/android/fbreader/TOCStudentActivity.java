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

import java.util.ArrayList;

import android.os.Bundle;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.sqliteconfig.SQLiteDB;
import org.geometerplus.zlibrary.core.sqliteconfig.SyncDB;
import org.geometerplus.zlibrary.core.tree.ZLTree;

import com.thirdEdition.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.zlibrary.text.view.SaveValue;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

public class TOCStudentActivity extends ListActivity {

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.list);
		getListView().setEmptyView(findViewById(R.id.empty));

		try {
			final SQLiteDB db = ZLApplication.Instance().db;
			Cursor c=db.fetchAll();

			startManagingCursor(c);

			c.moveToFirst();
			
			DataAdapter ad=new DataAdapter(this,c);
			//String[] from=new String[]{"userid"};
			//int[] to =new int[]{R.id.empty};
			//SimpleCursorAdapter ad = new SimpleCursorAdapter(this,R.layout.list,c,from,to);
			setListAdapter(ad);
			getListView().setTextFilterEnabled(false);
		} catch (Exception e) {
			UIUtil.showMessageText(getApplicationContext(), "請稍等一下，資料庫在處理同步資料");
			this.finish();
		}

	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		LinearLayout lay=(LinearLayout)l.getChildAt(position-l.getFirstVisiblePosition());
		TextView txt=(TextView)lay.findViewById(R.id.listUserId);
		
		String temp = SaveValue.SyncUserName; //紀錄改變前觀摩的使用者
		
		if( !"".equals(temp) )
			SaveValue.setSyncDataOff();
		
		SaveValue.setSyncDataOn( (String) listUserId.get(position) );
		UIUtil.showMessageText(this, "目前觀看: "+SaveValue.SyncUserName+"的註記內容");
		this.finish();
	}

	private ArrayList listUserId=new ArrayList(); //使用者ID
	private ArrayList listSyncTime=new ArrayList(); //同步時間點
	private ArrayList listAnntationText=new ArrayList(); //文字註記量
	private ArrayList listAnntationImage=new ArrayList(); //圖片註記量
	private ArrayList listAnntationRecord=new ArrayList(); //錄音量
	
	public class DataAdapter extends BaseAdapter{

		private Context context;
		private Cursor cursor;
		private int index; //userid
		private int time; //syncTime
		private int text, image, record; //註記
		
		public DataAdapter(Context con,Cursor c) {
			context = con;
			cursor = c;
			
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
				listSyncTime.add("最近更新時間 : "+c.getString(time));
				listAnntationText.add("文字註記量:"+c.getString(text));
				listAnntationImage.add("圖片註記量:"+c.getString(image));
				listAnntationRecord.add("        錄音量:"+c.getString(record));
				c.moveToNext();
			}
		}
		
		public int getCount() {
			return listUserId.size();
		}

		public Object getItem(int position) {
			return getView(position, null, null);
		}

		public long getItemId(int position) {
			if(cursor!=null)
			{
				//重要
				cursor.moveToPosition(position);
				return cursor.getLong(0);
			}else{
				return 0;
			}
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			Holder holder=null;

			if(convertView==null) {
				convertView=LayoutInflater.from(context).inflate(R.layout.list_text, null);
				holder=new Holder();
				//holder.icon=(ImageView)convertView.findViewById(R.id.icon);
				holder.check=(CheckBox) convertView.findViewById(R.id.checkBox);
				//holder.userId=(TextView)convertView.findViewById(R.id.listUserId);
				holder.userSyncTime=(TextView)convertView.findViewById(R.id.listSyncTime);
				holder.text=(TextView)convertView.findViewById(R.id.annotationText);
				holder.image=(TextView)convertView.findViewById(R.id.annotationImage);
				holder.record=(TextView)convertView.findViewById(R.id.annotationRecord);
				convertView.setTag(holder);
			} else {
				holder=(Holder)convertView.getTag();
			}
			holder.check.setText("  "+(CharSequence) listUserId.get(position));
			holder.text.setText("  "+(CharSequence) listAnntationText.get(position));
			holder.image.setText("  "+(CharSequence) listAnntationImage.get(position));
			holder.record.setText("  "+(CharSequence) listAnntationRecord.get(position));
			//holder.userId.setText((CharSequence) listUserId.get(position));
			holder.userSyncTime.setText((CharSequence) listSyncTime.get(position));
			//holder.icon.setImageBitmap(icon);
			return convertView;
		}

	}
	
	static class Holder {
		CheckBox check;
		TextView userId;
		TextView userName;
		TextView userSyncTime;
		TextView text, image, record;
	}

}
