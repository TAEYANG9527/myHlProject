package com.itcalf.renhe.context.room.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FindSQLiteStore {

	private static FindSQLiteStore instance = null;

	private RenheFindSQLiteOpenHelper helper;
	public static final String DBNAME = "renmaiquanmsg_finddb";
	private static final String TYPE = "find";

	private FindSQLiteStore(Context context) {
		super();
		this.helper = new RenheFindSQLiteOpenHelper(context);
	}

	public static synchronized FindSQLiteStore getInstance(Context context) {
		if (instance == null) {
			instance = new FindSQLiteStore(context);
		}
		return instance;
	}

	public RenheFindSQLiteOpenHelper getHelper() {
		return helper;
	}

	public void setHelper(RenheFindSQLiteOpenHelper helper) {
		this.helper = helper;
	}

	public static class RenheFindSQLiteOpenHelper extends SQLiteOpenHelper {

		private static final String DATABASE_NAME = DBNAME;
		public static final String TABLE_RENMAIQUAN = "renmaiquan" + TYPE;
		public static final String TABLE_RENMAIQUAN_CONTENT = "renmaiquan_content" + TYPE;
		public static final String TABLE_RENMAIQUAN_AT_MEMBER = "renmaiquan_at_member" + TYPE;
		public static final String TABLE_RENMAIQUAN_REPLY_LIST = "renmaiquan_reply_list" + TYPE;
		public static final String TABLE_RENMAIQUAN_LIKED_LIST = "renmaiquan_liked_list" + TYPE;
		public static final String TABLE_RENMAIQUAN_PIC_LIST = "renmaiquan_pic_list" + TYPE;
		public static final String TABLE_RENMAIQUAN_UPDATE_USERFACE = "renmaiquan_update_userface" + TYPE;
		public static final String TABLE_RENMAIQUAN_TIME = "renmaiquan_time_new" + TYPE;

		private static final String SQL_CREATE_TABLE_RENMAIQUAN = "create table if not exists " + TABLE_RENMAIQUAN
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "type INTEGER," + "sid TEXT," + "name TEXT," + "userface TEXT,"
				+ "title TEXT," + "company TEXT," + "industry TEXT," + "location TEXT," + "accountType INTEGER,"
				+ "isRealName TINYINT(1)," + "objectId TEXT," + "content TEXT," + "createDate LONG)";

		private static final String SQL_CREATE_TABLE_RENMAIQUAN_CONTENT = "create table if not exists " + TABLE_RENMAIQUAN_CONTENT
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "type INTEGER," + "objectId TEXT," + "Id INTEGER,"
				+ "replyNum INTEGER," + "likedNumber INTEGER," + "liked TINYINT(1),"
				+ "ForwardMessageBoardInfo_isForwardRenhe TINYINT(1)," + "ForwardMessageBoardInfo_ObjectId TEXT,"
				+ "ForwardMessageBoardInfo_Name TEXT," + "ForwardMessageBoardInfo_Sid TEXT,"
				+ "ForwardMessageBoardInfo_Content TEXT)";

		private static final String SQL_CREATE_TABLE_AT_MEMBER = "create table if not exists " + TABLE_RENMAIQUAN_AT_MEMBER
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "objectId TEXT," + "memberSid TEXT," + "memberName TEXT,"
				+ "forward_memberSid TEXT," + "forward_memberName TEXT)";

		private static final String SQL_CREATE_TABLE_REPLY_LIST = "create table if not exists " + TABLE_RENMAIQUAN_REPLY_LIST
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "objectId TEXT," + "senderSid TEXT," + "senderName TEXT,"
				+ "reSenderSid TEXT," + "reSenderMemberName TEXT," + "replyId INTEGER," + "replyObjectId TEXT," + "content TEXT)";

		private static final String SQL_CREATE_TABLE_LIKED_LIST = "create table if not exists " + TABLE_RENMAIQUAN_LIKED_LIST
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "objectId TEXT," + "sid TEXT," + "name TEXT)";

		private static final String SQL_CREATE_TABLE_PIC_LIST = "create table if not exists " + TABLE_RENMAIQUAN_PIC_LIST
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "objectId TEXT," + "thumbnailPicUrl TEXT," + "bmiddlePicUrl TEXT,"
				+ "forward_thumbnailPicUrl TEXT," + "forward_bmiddlePicUrl TEXT," + "bmiddlePicWidth INTEGER,"
				+ "bmiddlePicHeight INTEGER," + "forward_bmiddlePicWidth INTEGER," + "forward_bmiddlePicHeight INTEGER)";

		private static final String SQL_CREATE_TABLE_UPDATE_USERFACE = "create table if not exists "
				+ TABLE_RENMAIQUAN_UPDATE_USERFACE + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "objectId TEXT,"
				+ "userfaceUrl TEXT)";

		private static final String SQL_CREATE_TABLE_TIME = "create table if not exists " + TABLE_RENMAIQUAN_TIME
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "page INTEGER default('1')," + "maxLastUpdatedDate LONG,"
				+ "cacheTime INTEGER)";

		RenheFindSQLiteOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, 1);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE_TABLE_RENMAIQUAN);
			db.execSQL(SQL_CREATE_TABLE_RENMAIQUAN_CONTENT);
			db.execSQL(SQL_CREATE_TABLE_AT_MEMBER);
			db.execSQL(SQL_CREATE_TABLE_LIKED_LIST);
			db.execSQL(SQL_CREATE_TABLE_PIC_LIST);
			db.execSQL(SQL_CREATE_TABLE_REPLY_LIST);
			db.execSQL(SQL_CREATE_TABLE_UPDATE_USERFACE);
			db.execSQL(SQL_CREATE_TABLE_TIME);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}
}
