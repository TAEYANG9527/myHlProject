package com.itcalf.renhe.context.wukong.im.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class CircleDao {
	private static CircleHelper dbHelper;
	private SQLiteDatabase write_database;
	private SQLiteDatabase read_database;
	public static String Lock = "dblock";

	public CircleDao(Context context) {
		dbHelper = new CircleHelper(context);
		write_database = dbHelper.getWritableDatabase();
		read_database = dbHelper.getReadableDatabase();
	}

	/**
	 * 判断表数据是否存在
	 * */
	public Boolean isLocal(String circleId) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = db.rawQuery("select * from " + dbHelper.TAB_DOWNCOURSE + " where circleId = ?",
				new String[] { circleId });
		if (cursor.moveToFirst()) {
			cursor.close();
			return true;
		}
		cursor.close();
		db.close();
		return false;
	}

	/**
	 * 插入或更新 表数据
	 */
	public void insertCircleInfo(String sid, String adsid, String circleId, String imConversationId, String preAvatarId,
			String name, String joinType, String note, String imMemberIds, boolean flag, boolean searchAble) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("sid", sid);
		values.put("adsid", adsid);
		values.put("circleId", circleId);
		values.put("imConversationId", imConversationId);
		values.put("preAvatarId", preAvatarId);
		values.put("name", name);
		values.put("joinType", joinType);
		values.put("note", note);
		values.put("imMemberIds", imMemberIds);
		if (searchAble) {
			values.put("searchAble", 1);
		} else {
			values.put("searchAble", 0);
		}
		if (flag)
			db.update(dbHelper.TAB_DOWNCOURSE, values, null, null);
		else
			db.insert(dbHelper.TAB_DOWNCOURSE, null, values);
		db.close();
	}

	/**
	 * 680 330 查询表数据
	 * */
	public ArrayList<HashMap<String, Object>> queryCircleInfo(String sid, String adSid) {
		ArrayList<HashMap<String, Object>> item = new ArrayList<HashMap<String, Object>>();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		String sql = null;
		Cursor cursor = null;
		sql = "select * from " + dbHelper.TAB_DOWNCOURSE + " where sid=? and adSid=?";
		cursor = database.rawQuery(sql, new String[] { sid, adSid });
		try {
			if (cursor.moveToFirst()) {
				do {
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("sid", cursor.getString(1));
					map.put("adSId", cursor.getString(2));
					map.put("circleId", cursor.getString(3));
					map.put("imConversationId", cursor.getString(4));
					map.put("preAvatarId", cursor.getString(5));
					map.put("name", cursor.getString(6));
					map.put("joinType", cursor.getString(7));
					map.put("note", cursor.getString(8));
					map.put("imMemberIds", cursor.getString(9));
					if (cursor.getInt(10) == 0) {
						map.put("searchAble", false);
					} else {
						map.put("searchAble", true);
					}
					item.add(map);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			database.close();
			cursor.close();
		}
		return item;
	}

	/**
	 * 删除表数据
	 * */
	public void deleterCircleInfo(String adsid, String circleId) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		String sql = null;
		try {
			sql = "delete from " + dbHelper.TAB_DOWNCOURSE + " where adSid=? and circleId=?";
			database.execSQL(sql, new Object[] { adsid, circleId });
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			database.close();
		}
	}

	/**
	 * 关闭database
	 */
	public void closeDataBase() {
		write_database.close();
		read_database.close();
	}
}
