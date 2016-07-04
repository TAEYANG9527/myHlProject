package com.itcalf.renhe.context.wukong.im.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.itcalf.renhe.context.room.db.SQLiteStore;
import com.itcalf.renhe.context.wukong.im.db.TouTiaoSQLiteStore.TouTiaoSQLiteOpenHelper;
import com.itcalf.renhe.dto.TouTiaoOperation;
import com.itcalf.renhe.dto.TouTiaoOperation.TouTiao;
import com.itcalf.renhe.dto.TouTiaoOperation.TouTiaoList;

import java.util.ArrayList;
import java.util.List;

public class TouTiaoManager {

	private Context context;

	public TouTiaoManager(Context context) {
		this.context = context;
	}

	public void insert(TouTiaoList[] touTiaosArray) {
		if (null != touTiaosArray && touTiaosArray.length > 0) {
			SQLiteDatabase dbWriter = TouTiaoSQLiteStore.getInstance(context).getHelper().getWritableDatabase();
			ContentValues values;
			for (TouTiaoList touTiaoList : touTiaosArray) {
				if (null != touTiaoList) {
					values = new ContentValues();
					values.put("messageList_id", touTiaoList.getId());
					values.put("message_createDate", touTiaoList.getCreatedDate());
					dbWriter.insert(TouTiaoSQLiteOpenHelper.TABLE_TOUTIAO, null, values);

					TouTiao[] touTiaos = touTiaoList.getToutiaoList();
					if (null != touTiaos && touTiaos.length > 0) {
						for (TouTiao tiao : touTiaos) {
							values = new ContentValues();
							values.put("toutiaoList_id", tiao.getId());
							values.put("toutiaoList_messageId", tiao.getMessageId());
							values.put("title", tiao.getTitle());
							values.put("source", tiao.getSource());
							values.put("image", tiao.getImage());
							values.put("url", tiao.getUrl());
							values.put("orders", tiao.getOrders());
							values.put("createDate", tiao.getCreatedDate());
							dbWriter.insert(TouTiaoSQLiteOpenHelper.TABLE_TOUTIAOITEM, null, values);
						}
					}
				}
			}

		}
	}

	public int delete() {
		SQLiteDatabase dbWriter = SQLiteStore.getInstance(context).getHelper().getWritableDatabase();
		try {
			dbWriter.delete(TouTiaoSQLiteOpenHelper.TABLE_TOUTIAO, null, null);
			dbWriter.delete(TouTiaoSQLiteOpenHelper.TABLE_TOUTIAOITEM, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
		return 0;
	}

	public Cursor getMainBoardsCursor(String table) {
		Cursor cursor = null;
		try {
			SQLiteDatabase dbReader = TouTiaoSQLiteStore.getInstance(context).getHelper().getReadableDatabase();
			cursor = dbReader.rawQuery("select * from " + table + " ORDER BY messageList_id ASC ", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cursor;
	}

	public Cursor getItemBoardsCursor(String table, int id) {
		Cursor cursor = null;
		try {
			SQLiteDatabase dbReader = TouTiaoSQLiteStore.getInstance(context).getHelper().getReadableDatabase();
			cursor = dbReader.rawQuery("select * from " + table + " where toutiaoList_messageId = ? ORDER BY orders DESC ",
					new String[] { id + "" });
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cursor;
	}

	public TouTiaoList[] getMessageBoardsFromCursor() {
		Cursor cursor = null;
		Cursor itemCursor = null;
		TouTiaoList[] touTiaoListsArray = null;
		TouTiao[] touTiaosArray;
		try {
			TouTiaoList touTiaoList;
			TouTiao tiao;
			List<TouTiaoList> touTiaos = new ArrayList<TouTiaoOperation.TouTiaoList>();
			cursor = getMainBoardsCursor(TouTiaoSQLiteOpenHelper.TABLE_TOUTIAO);
			if (null != cursor) {
				while (cursor.moveToNext()) {
					touTiaoList = new TouTiaoList();
					touTiaoList.setCreatedDate(cursor.getLong(2));
					touTiaoList.setId(cursor.getInt(1));
					touTiaos.add(touTiaoList);
				}
			}
			for (TouTiaoList tiaoList : touTiaos) {
				itemCursor = getItemBoardsCursor(TouTiaoSQLiteOpenHelper.TABLE_TOUTIAOITEM, tiaoList.getId());
				List<TouTiao> touTiaos2 = new ArrayList<TouTiaoOperation.TouTiao>();
				if (null != itemCursor) {
					while (itemCursor.moveToNext()) {
						tiao = new TouTiao();
						tiao.setCreatedDate(itemCursor.getLong(8));
						tiao.setId(itemCursor.getInt(1));
						tiao.setImage(itemCursor.getString(5));
						tiao.setMessageId(itemCursor.getInt(2));
						tiao.setOrders(itemCursor.getInt(7));
						tiao.setSource(itemCursor.getString(4));
						tiao.setTitle(itemCursor.getString(3));
						tiao.setUrl(itemCursor.getString(6));
						touTiaos2.add(tiao);
					}
					itemCursor.close();
				}
				if (touTiaos2.size() > 0) {
					touTiaosArray = new TouTiao[touTiaos2.size()];
					for (int i = 0; i < touTiaos2.size(); i++) {
						touTiaosArray[i] = touTiaos2.get(i);
					}
					tiaoList.setToutiaoList(touTiaosArray);
				}
			}
			if (touTiaos.size() > 0) {
				touTiaoListsArray = new TouTiaoList[touTiaos.size()];
				for (int i = 0; i < touTiaos.size(); i++) {
					touTiaoListsArray[i] = touTiaos.get(i);
				}
			}
			return touTiaoListsArray;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != cursor) {
				cursor.close();
			}
			if (null != itemCursor) {
				itemCursor.close();
			}
		}
		return null;
	}

	public void deleteDatabase() {
		if (null != TouTiaoSQLiteStore.getInstance(context).getHelper()) {
			TouTiaoSQLiteStore.getInstance(context).getHelper().close();
		}
	}
}
