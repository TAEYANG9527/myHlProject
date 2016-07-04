package com.itcalf.renhe.context.room.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.itcalf.renhe.cache.CacheManager;

public class SQLiteStore {

    private static SQLiteStore instance = null;

    private RenheSQLiteOpenHelper helper;
    public static final String DBNAME = CacheManager.DBNAME;

    private SQLiteStore(Context context) {
        super();
        this.helper = new RenheSQLiteOpenHelper(context);
    }

    public static synchronized SQLiteStore getInstance(Context context) {
        if (instance == null) {
            instance = new SQLiteStore(context);
        }
        return instance;
    }

    public RenheSQLiteOpenHelper getHelper() {
        return helper;
    }

    public void setHelper(RenheSQLiteOpenHelper helper) {
        this.helper = helper;
    }

    public static class RenheSQLiteOpenHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = DBNAME;
        private static final int DATABASE_VERSION = 3;
        public static final String TABLE_RENMAIQUAN = "renmaiquan";
        public static final String TABLE_RENMAIQUAN_CONTENT = "renmaiquan_content_v2";
        public static final String TABLE_RENMAIQUAN_AT_MEMBER = "renmaiquan_at_member";
        public static final String TABLE_RENMAIQUAN_REPLY_LIST = "renmaiquan_reply_list";
        public static final String TABLE_RENMAIQUAN_LIKED_LIST = "renmaiquan_liked_list";
        public static final String TABLE_RENMAIQUAN_PIC_LIST = "renmaiquan_pic_list";
        public static final String TABLE_RENMAIQUAN_UPDATE_USERFACE = "renmaiquan_update_userface";
        public static final String TABLE_RENMAIQUAN_TIME = "renmaiquan_time";
        public static final String TABLE_RENMAIQUAN_SHARE = "renmaiquan_share";
        public static final String TABLE_RENMAIQUAN_RECOMMEND_FRIEND = "renmaiquan_recommend_friend";
        /**
         * 名片
         */
        public static final String TABLE_VCARD = "vcard";
        public static final String TABLE_CARD = "card";
        public static final String TABLE_PROFILE = "profile";
        public static final String TABLE_RECOGNIZED_CARD = "recognized_card";

        private static final String SQL_CREATE_TABLE_RENMAIQUAN = "create table if not exists " + TABLE_RENMAIQUAN
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "type INTEGER," + "senderSid TEXT," + "name TEXT,"
                + "userface TEXT," + "title TEXT," + "company TEXT," + "industry TEXT," + "location TEXT,"
                + "accountType INTEGER," + "isRealName TINYINT(1)," + "objectId TEXT," + "content TEXT," + "createDate LONG,"
                + "source INTEGER," + "score LONG," + "sid TEXT," + "subject TEXT," + "url TEXT," + "friendState INTEGER)";

        private static final String SQL_CREATE_TABLE_RENMAIQUAN_CONTENT = "create table if not exists " + TABLE_RENMAIQUAN_CONTENT
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "type INTEGER," + "objectId TEXT," + "Id INTEGER,"
                + "replyNum INTEGER," + "likedNumber INTEGER," + "liked TINYINT(1),"
                + "ForwardMessageBoardInfo_isForwardRenhe TINYINT(1)," + "ForwardMessageBoardInfo_ObjectId TEXT,"
                + "ForwardMessageBoardInfo_Name TEXT," + "ForwardMessageBoardInfo_Sid TEXT,"
                + "ForwardMessageBoardInfo_Content TEXT," + "ForwardMessageBoardInfo_Type INTEGER," + "sid TEXT)";

        private static final String SQL_CREATE_TABLE_AT_MEMBER = "create table if not exists " + TABLE_RENMAIQUAN_AT_MEMBER
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "objectId TEXT," + "memberSid TEXT," + "memberName TEXT,"
                + "forward_memberSid TEXT," + "forward_memberName TEXT," + "sid TEXT)";

        private static final String SQL_CREATE_TABLE_REPLY_LIST = "create table if not exists " + TABLE_RENMAIQUAN_REPLY_LIST
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "objectId TEXT," + "senderSid TEXT," + "senderName TEXT,"
                + "reSenderSid TEXT," + "reSenderMemberName TEXT," + "replyId INTEGER," + "replyObjectId TEXT," + "content TEXT,"
                + "sid TEXT)";

        private static final String SQL_CREATE_TABLE_LIKED_LIST = "create table if not exists " + TABLE_RENMAIQUAN_LIKED_LIST
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "objectId TEXT," + "likeSid TEXT," + "name TEXT,userface TEXT,"
                + "sid TEXT)";

        private static final String SQL_CREATE_TABLE_PIC_LIST = "create table if not exists " + TABLE_RENMAIQUAN_PIC_LIST
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "objectId TEXT," + "thumbnailPicUrl TEXT," + "bmiddlePicUrl TEXT,"
                + "forward_thumbnailPicUrl TEXT," + "forward_bmiddlePicUrl TEXT," + "bmiddlePicWidth INTEGER,"
                + "bmiddlePicHeight INTEGER," + "forward_bmiddlePicWidth INTEGER," + "forward_bmiddlePicHeight INTEGER,"
                + "sid TEXT)";

        private static final String SQL_CREATE_TABLE_UPDATE_USERFACE = "create table if not exists "
                + TABLE_RENMAIQUAN_UPDATE_USERFACE + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "objectId TEXT,"
                + "userfaceUrl TEXT," + "sid TEXT)";

        private static final String SQL_CREATE_TABLE_TIME = "create table if not exists " + TABLE_RENMAIQUAN_TIME
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "maxCreatedDate LONG," + "minCreatedDate LONG,"
                + "maxLastUpdatedDate LONG," + "sid TEXT)";

        private static final String SQL_CREATE_TABLE_VCARD = "create table if not exists " + TABLE_VCARD
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "server_id INTEGER," + "user_id TEXT," + "image_big TEXT,"
                + "image_small TEXT," + "vcard_content TEXT," + "isrenhenmember TINYINT(1)," + "rehemembersid TEXT,"
                + "isself TINYINT(1)," + "isfriend TINYINT(1)," + "isinvite TINYINT(1)," + "status INTEGER,"
                + "recognition INTEGER," + "date_created INTEGER," + "date_modified INTEGER)";

        private static final String SQL_CREATE_TABLE_PROFILE = "create table if not exists " + TABLE_PROFILE
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "user_id INTEGER," + "image_file TEXT," + "vcard TEXT,"
                + "date_created INTEGER," + "last_modified INTEGER," + "status INTEGER," + "recognition INTEGER,"
                + "synced INTEGER)";

        private static final String SQL_CREATE_TABLE_RECOGNIZED_CARD = "create table if not exists " + TABLE_RECOGNIZED_CARD
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "user_id INTEGER," + "server_id INTEGER," + "status INTEGER)";

        private static final String SQL_UPDATE_TABLE_RECOGNIZED_CARD_ADD_IS_PROFILE = "alter table " + TABLE_RECOGNIZED_CARD
                + " add column is_profile INTEGER NOT NULL DEFAULT 0";

        private static final String SQL_UPDATE_TABLE_CARD_ADD_RENHE_MEMBER_SID = "alter table " + TABLE_CARD
                + " add column renhe_member_sid TEXT DEFAULT ''";

        private static final String SQL_CREATE_TABLE_SHARE = "create table if not exists " + TABLE_RENMAIQUAN_SHARE
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "objectId TEXT," + "type INTEGER," + "id INTEGER,url TEXT,"
                + "content TEXT,picUrl TEXT," + "profileSid TEXT,name TEXT,job TEXT,company TEXT,note TEXT,sid TEXT)";

        private static final String SQL_CREATE_TABLE_RECOMMEND_FRIEND = "create table if not exists "
                + TABLE_RENMAIQUAN_RECOMMEND_FRIEND + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + "objectId TEXT,"
                + "memberSid TEXT," + "memberName TEXT," + "memberUserface TEXT," + "memberTitle TEXT," + "memberCompany TEXT,"
                + "memberIndustry TEXT," + "memberLocation TEXT," + "memberAccountType INTEGER,"
                + "memberIsRealName NUMERIC DEFAULT 0," + "sid TEXT)";

        RenheSQLiteOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
            db.execSQL(SQL_CREATE_TABLE_VCARD);
            db.execSQL(SQL_CREATE_TABLE_SHARE);
            db.execSQL(SQL_CREATE_TABLE_RECOMMEND_FRIEND);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            switch (oldVersion) {
                case 1:
                    db.execSQL("ALTER TABLE " + TABLE_RENMAIQUAN + " ADD COLUMN subject TEXT");
                    db.execSQL("ALTER TABLE " + TABLE_RENMAIQUAN + " ADD COLUMN url TEXT");
                case 2:
                    db.execSQL("ALTER TABLE " + TABLE_RENMAIQUAN + " ADD COLUMN friendState INTEGER");
                    break;
                default:
                    break;
            }
        }

    }
}
