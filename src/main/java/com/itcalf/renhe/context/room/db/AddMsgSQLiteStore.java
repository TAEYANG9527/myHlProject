package com.itcalf.renhe.context.room.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AddMsgSQLiteStore {

    private static AddMsgSQLiteStore instance = null;

    private AddSQLiteOpenHelper helper;
    public static final String DBNAME = "renmaiquan_addmsg_v2";

    private AddMsgSQLiteStore(Context context) {
        super();
        this.helper = new AddSQLiteOpenHelper(context);
    }

    public static synchronized AddMsgSQLiteStore getInstance(Context context) {
        if (instance == null) {
            instance = new AddMsgSQLiteStore(context);
        }
        return instance;
    }

    public AddSQLiteOpenHelper getHelper() {
        return helper;
    }

    public void setHelper(AddSQLiteOpenHelper helper) {
        this.helper = helper;
    }

    public static class AddSQLiteOpenHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = DBNAME;
        //发布新留言的缓存表
        public static final String TABLE_RENMAIQUAN_ADD_NEWMESSAGE_ID = "renmaiquan_add_newmsg_id";
        public static final String TABLE_RENMAIQUAN_ADD_NEWMESSAGE = "renmaiquan_add_newmsg";

        private static final String SQL_CREATE_TABLE_ADD_NEWMESSAGE_ID = "create table if not exists "
                + TABLE_RENMAIQUAN_ADD_NEWMESSAGE_ID + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "create_time LONG,"
                + "messageboardPublicationId INTEGER," + "content TEXT," + "atmembers TEXT)";

        private static final String SQL_CREATE_TABLE_ADD_NEWMESSAGE = "create table if not exists "
                + TABLE_RENMAIQUAN_ADD_NEWMESSAGE + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "create_time LONG,"
                + "messageboardPublicationId INTEGER," + "messageboardPhotoResourceId TEXT,"
                + "thumbnailPicPath TEXT," + "bmiddlePicPath TEXT)";

        AddSQLiteOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_TABLE_ADD_NEWMESSAGE_ID);
            db.execSQL(SQL_CREATE_TABLE_ADD_NEWMESSAGE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

    }
}
