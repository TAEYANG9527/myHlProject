package com.itcalf.renhe.context.room.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.itcalf.renhe.cache.CacheManager;

public class AddNewMsgSQLiteStore {

    private static AddNewMsgSQLiteStore instance = null;

    private AddNewMsgRenheSQLiteOpenHelper helper;
    public static final String DBNAME = CacheManager.ADD_MSG_DBNAME;

    private AddNewMsgSQLiteStore(Context context) {
        super();
        this.helper = new AddNewMsgRenheSQLiteOpenHelper(context);
    }

    public static synchronized AddNewMsgSQLiteStore getInstance(Context context) {
        if (instance == null) {
            instance = new AddNewMsgSQLiteStore(context);
        }
        return instance;
    }

    public AddNewMsgRenheSQLiteOpenHelper getHelper() {
        return helper;
    }

    public void setHelper(AddNewMsgRenheSQLiteOpenHelper helper) {
        this.helper = helper;
    }

    public static class AddNewMsgRenheSQLiteOpenHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = DBNAME;
        private static final int DATABASE_VERSION = 1;
        public static final String TABLE_RENMAIQUAN = "renmaiquan";
        public static final String TABLE_RENMAIQUAN_CONTENT = "renmaiquan_content";
        public static final String TABLE_RENMAIQUAN_AT_MEMBER = "renmaiquan_at_member";
        public static final String TABLE_RENMAIQUAN_PIC_LIST = "renmaiquan_pic_list";

        private static final String SQL_CREATE_TABLE_RENMAIQUAN = "create table if not exists " + TABLE_RENMAIQUAN
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "type INTEGER," + "senderSid TEXT," + "name TEXT,"
                + "userface TEXT," + "title TEXT," + "company TEXT," + "industry TEXT," + "location TEXT,"
                + "accountType INTEGER," + "isRealName TINYINT(1)," + "objectId TEXT," + "content TEXT," + "createDate LONG,"
                + "source INTEGER," + "score LONG," + "sid TEXT," + "subject TEXT," + "url TEXT," + "messageboardPublicationId INTEGER)";

        private static final String SQL_CREATE_TABLE_RENMAIQUAN_CONTENT = "create table if not exists " + TABLE_RENMAIQUAN_CONTENT
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "type INTEGER," + "objectId TEXT," + "Id INTEGER,"
                + "replyNum INTEGER," + "likedNumber INTEGER," + "liked TINYINT(1),"
                + "ForwardMessageBoardInfo_isForwardRenhe TINYINT(1)," + "ForwardMessageBoardInfo_ObjectId TEXT,"
                + "ForwardMessageBoardInfo_Name TEXT," + "ForwardMessageBoardInfo_Sid TEXT,"
                + "ForwardMessageBoardInfo_Content TEXT," + "ForwardMessageBoardInfo_Type INTEGER," + "sid TEXT)";

        private static final String SQL_CREATE_TABLE_AT_MEMBER = "create table if not exists " + TABLE_RENMAIQUAN_AT_MEMBER
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "objectId TEXT," + "memberSid TEXT," + "memberName TEXT,"
                + "forward_memberSid TEXT," + "forward_memberName TEXT," + "sid TEXT)";


        private static final String SQL_CREATE_TABLE_PIC_LIST = "create table if not exists " + TABLE_RENMAIQUAN_PIC_LIST
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "objectId TEXT," + "thumbnailPicUrl TEXT," + "bmiddlePicUrl TEXT,"
                + "forward_thumbnailPicUrl TEXT," + "forward_bmiddlePicUrl TEXT," + "bmiddlePicWidth INTEGER,"
                + "bmiddlePicHeight INTEGER," + "forward_bmiddlePicWidth INTEGER," + "forward_bmiddlePicHeight INTEGER,"
                + "sid TEXT," + "resourceId TEXT,"+ "uploadState INTEGER)";


        AddNewMsgRenheSQLiteOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_TABLE_RENMAIQUAN);
            db.execSQL(SQL_CREATE_TABLE_RENMAIQUAN_CONTENT);
            db.execSQL(SQL_CREATE_TABLE_AT_MEMBER);
            db.execSQL(SQL_CREATE_TABLE_PIC_LIST);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            switch (oldVersion) {
                case 1:
                    break;
                default:
                    break;
            }
        }

    }
}
