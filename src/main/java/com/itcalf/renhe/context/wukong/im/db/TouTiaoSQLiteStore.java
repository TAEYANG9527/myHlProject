package com.itcalf.renhe.context.wukong.im.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.itcalf.renhe.cache.CacheManager;

public class TouTiaoSQLiteStore {

	private static TouTiaoSQLiteStore instance = null;

	private TouTiaoSQLiteOpenHelper helper;
	public static final String DBNAME = CacheManager.CONVERSATION_DBNAME;

	private TouTiaoSQLiteStore(Context context) {
		super();
		this.helper = new TouTiaoSQLiteOpenHelper(context);
	}

	public static synchronized TouTiaoSQLiteStore getInstance(Context context) {
		if (instance == null) {
			instance = new TouTiaoSQLiteStore(context);
		}
		return instance;
	}

	public TouTiaoSQLiteOpenHelper getHelper() {
		return helper;
	}

	public void setHelper(TouTiaoSQLiteOpenHelper helper) {
		this.helper = helper;
	}

	public static class TouTiaoSQLiteOpenHelper extends SQLiteOpenHelper {

		private static final String DATABASE_NAME = DBNAME;
		public static final String TABLE_TOUTIAO = "toutiao";
		public static final String TABLE_TOUTIAOITEM = "toutiaoItem";

		private static final String SQL_CREATE_TABLE_TOUTIAO = "create table if not exists " + TABLE_TOUTIAO
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "messageList_id INTEGER," + "message_createDate LONG)";

		private static final String SQL_CREATE_TABLE_TOUTIAOITEM = "create table if not exists " + TABLE_TOUTIAOITEM
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "toutiaoList_id INTEGER," + "toutiaoList_messageId INTEGER,"
				+ "title TEXT," + "source TEXT," + "image TEXT," + "url TEXT," + "orders INTEGER," + "createDate LONG)";

		TouTiaoSQLiteOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, 1);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE_TABLE_TOUTIAO);
			db.execSQL(SQL_CREATE_TABLE_TOUTIAOITEM);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}

	}
}
