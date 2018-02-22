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
import java.util.List;

import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.tree.ZLTree;
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.text.view.SaveValue;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;
import com.thirdEdition.geometerplus.zlibrary.ui.android.R;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class ListViewMenu extends ListView {

	private AnnotationSyncListAdapter adter;
	private FBReader fbreader = null;
	private ListView listview= null;
	private TOCAdapter myAdapter;
	private ZLTree<?> mySelectedItem;
	
	public ListViewMenu(FBReader context) {
		super(context);
		fbreader = context;
	}
	public void YansetTOC(){
		try{
			SaveValue.IsToc = true;
			setBackgroundColor(Color.argb(150, 255, 255, 255));
			setCacheColorHint(Color.TRANSPARENT);
		}
		catch(Exception e){
			System.out.println("出現問題");
		}
	}
	public void setTOC() {
		try {
			SaveValue.IsToc = true;
			this.listview = this;
			final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
			final TOCTree root = fbreader.Model.TOCTree;
			myAdapter = new TOCAdapter(root);
			final ZLTextWordCursor cursor = fbreader.BookTextView.getStartCursor();
			int index = cursor.getParagraphIndex();	
			if (cursor.isEndOfParagraph()) {
				++index;
			}
			TOCTree treeToSelect = null;
			// TODO: process multi-model texts
			for (TOCTree tree : root) {
				final TOCTree.Reference reference = tree.getReference();
				if (reference == null) {
					continue;
				}
				if (reference.ParagraphIndex > index) {
					break;
				}
				treeToSelect = tree;
			}
			myAdapter.selectItem(treeToSelect);
			mySelectedItem = treeToSelect;
			setBackgroundColor(Color.argb(150, 255, 255, 255));
			setCacheColorHint(Color.TRANSPARENT);
		} catch (Exception e) {
			Log.i("test", "test TOC e= "+e);
		}
	}
	
	public void showLeft(int y) { //顯示在左方
		//控制大小
		setLayoutParams(new RelativeLayout.LayoutParams(56,668-y));
		setY(y+72);
		setAlpha(20);
		setBackgroundColor(Color.TRANSPARENT);
		setCacheColorHint(Color.TRANSPARENT);
	}
	
	public void showRight(int y) { //顯示在右方
		//控制大小
		setLayoutParams(new RelativeLayout.LayoutParams(56,668-y));
		setX(1215);
		setY(y+72);
		setAlpha(20);
		setBackgroundColor(Color.TRANSPARENT);
		setCacheColorHint(Color.TRANSPARENT);
	}
	
	public void setItem(){
		setLayoutParams(new RelativeLayout.LayoutParams(500,200));
		setX(640);
		setY(350);
		setBackgroundColor(Color.BLACK);
		DataAdapter ad=new DataAdapter(fbreader);
		setAdapter(ad);
	}
	
	private ArrayList listItem=new ArrayList();
	private String[] listItem_default = {"「家長參與」模式 OPEN","錄製老師講解","同步設定","目錄"};
	public class DataAdapter extends BaseAdapter{

		private Context context;
		
		public DataAdapter(Context con) {
			context = con;

			for(int i=0;i<listItem_default.length;i++) {
				listItem.add(listItem_default[i]);
			}
		}
		
		public int getCount() {
			return listItem_default.length;
		}

		public Object getItem(int position) {
			return getView(position, null, null);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			Holder holder=null;

			if(convertView==null) {
				convertView=LayoutInflater.from(context).inflate(R.layout.list_item_menu, null);
				holder=new Holder();
				//holder.icon=(ImageView)convertView.findViewById(R.id.icon);
				holder.Item=(TextView)convertView.findViewById(R.id.listItem);
				convertView.setTag(holder);
			} else {
				holder=(Holder)convertView.getTag();
			}

			holder.Item.setText((CharSequence) listItem_default[position]);
			//holder.icon.setImageBitmap(icon);
			return convertView;
		}

	}
	
	static class Holder {
		TextView Item;
	}
	
	private static final int PROCESS_TREE_ITEM_ID = 0;
	private static final int READ_BOOK_ITEM_ID = 1;
	
	private final class TOCAdapter extends ZLTreeAdapter {
		TOCAdapter(TOCTree root) {
			super(listview, root);
		}

		@Override
		public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
			final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
			final TOCTree tree = (TOCTree)getItem(position);
			if (tree.hasChildren()) {
				menu.setHeaderTitle(tree.getText());
				final ZLResource resource = ZLResource.resource("tocView");
				menu.add(0, PROCESS_TREE_ITEM_ID, 0, resource.getResource(isOpen(tree) ? "collapseTree" : "expandTree").getValue());
				menu.add(0, READ_BOOK_ITEM_ID, 0, resource.getResource("readText").getValue());
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View view = (convertView != null) ? convertView :
				LayoutInflater.from(parent.getContext()).inflate(R.layout.toc_tree_item, parent, false);
			final TOCTree tree = (TOCTree)getItem(position);
			//view.setBackgroundColor((tree == mySelectedItem) ? 0xff808080 : 0);
			//setIcon((ImageView)view.findViewById(R.id.toc_tree_item_icon), tree);
			TextView temp = (TextView)view.findViewById(R.id.toc_tree_item_text);
			temp.setText(tree.getText());
			temp.setTextSize(14);
			temp.setTextColor(Color.BLACK);
			return view;
		}

		void openBookText(TOCTree tree) {
			final TOCTree.Reference reference = tree.getReference();
			if (reference != null) {
				//finish();
				fbreader.root.removeView(listview);
				final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
				fbreader.addInvisibleBookmark();
				fbreader.BookTextView.gotoPosition(reference.ParagraphIndex, 0, 0);
				fbreader.showBookTextView();
			}
		}

		@Override
		protected boolean runTreeItem(ZLTree<?> tree) {
			if (super.runTreeItem(tree)) {
				return true;
			}
			openBookText((TOCTree)tree);
			return true;
		}
	}
	
}

