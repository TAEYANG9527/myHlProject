package com.itcalf.renhe.database.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "renhe.db";
    private static final int DATABASE_VERSION = 5;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public DBHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL(TablesConstant.SQL_CONTACT);
        db.execSQL(TablesConstant.SQL_USER);
        db.execSQL(TablesConstant.SQL_CONTACTISSAVE);
        db.execSQL(TablesConstant.SQL_EMAIL_CONTACT);
        db.execSQL(TablesConstant.SQL_MOBILE_CONTACT);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TablesConstant.SQL_USER);
        db.execSQL(TablesConstant.SQL_CONTACT);
        db.execSQL(TablesConstant.SQL_CONTACTISSAVE);
        db.execSQL(TablesConstant.SQL_EMAIL_CONTACT);
        db.execSQL(TablesConstant.SQL_MOBILE_CONTACT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //        db.execSQL("DROP TABLE IF EXISTS " + TablesConstant.SQL_USER);
        //		//				db.execSQL("DROP TABLE IF EXISTS " + TablesConstant.USER_TABLE);
        //		db.execSQL("DROP TABLE IF EXISTS " + TablesConstant.SQL_CONTACT);//升级
        //		db.execSQL("DROP TABLE IF EXISTS " + TablesConstant.SQL_CONTACTISSAVE);
        //        db.execSQL("DROP TABLE IF EXISTS " + TablesConstant.SQL_EMAIL_CONTACT);
        //        db.execSQL("DROP TABLE IF EXISTS " + TablesConstant.SQL_MOBILE_CONTACT);
        //        onCreate(db);
        db.beginTransaction();
        switch (oldVersion) {
            case 1:
                //联系人表
                db.execSQL("alter table " + TablesConstant.CONTACT_TABLE + " add column " + TablesConstant.CONTACT_TABLE_COLUMN_CARDID
                        + " integer");
                db.execSQL("alter table " + TablesConstant.CONTACT_TABLE + " add column "
                        + TablesConstant.CONTACT_TABLE_COLUMN_VCARDCONTENT + " text");
                db.execSQL("alter table " + TablesConstant.CONTACT_TABLE + " add column "
                        + TablesConstant.CONTACT_TABLE_COLUMN_PROFILESOURCETYPE + " integer");
                db.execSQL("alter table " + TablesConstant.CONTACT_TABLE + " add column " + TablesConstant.CONTACT_TABLE_COLUMN_EMAIL
                        + " text");
                //保存联系人最大id值表
                db.execSQL("alter table " + TablesConstant.CONTACTISSAVE_TABLE + " add column "
                        + TablesConstant.CONTACTISSAVE_MAXMOBILEID + " integer");
                db.execSQL("alter table " + TablesConstant.CONTACTISSAVE_TABLE + " add column "
                        + TablesConstant.CONTACTISSAVE_MAXCARDID + " integer");
            case 2:
                //手机,邮箱联系人表添加字段，头像、公司、职务
                db.execSQL("alter table " + TablesConstant.MOBILE_CONTACT_TABLE + " add column " + TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_AVATAR
                        + " text");
                db.execSQL("alter table " + TablesConstant.MOBILE_CONTACT_TABLE + " add column " + TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_COMPANY
                        + " text");
                db.execSQL("alter table " + TablesConstant.MOBILE_CONTACT_TABLE + " add column " + TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_POSITION
                        + " text");
                db.execSQL("alter table " + TablesConstant.EMAIL_CONTACT_TABLE + " add column " + TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_AVATAR
                        + " text");
                db.execSQL("alter table " + TablesConstant.EMAIL_CONTACT_TABLE + " add column " + TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_COMPANY
                        + " text");
                db.execSQL("alter table " + TablesConstant.EMAIL_CONTACT_TABLE + " add column " + TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_POSITION
                        + " text");
            case 3:
                //联系人表 增加字段shortName  《2015.11.25版本5.4.1增加》
                db.execSQL("alter table " + TablesConstant.CONTACT_TABLE + " add column " + TablesConstant.CONTACT_TABLE_COLUMN_SHORTNAME
                        + " text");
            case 4:
                db.execSQL("alter table " + TablesConstant.CONTACT_TABLE + " add column " + TablesConstant.CONTACT_TABLE_COLUMN_COLORINDEX
                        + " text");
                db.execSQL("update " + TablesConstant.CONTACTISSAVE_TABLE + " set "
                        + TablesConstant.CONTACTISSAVE_MAXMOBILEID + " = 0");
                db.execSQL("update " + TablesConstant.CONTACTISSAVE_TABLE + " set "
                        + TablesConstant.CONTACTISSAVE_MAXCARDID + " = 0");
                break;

            default:
                break;
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }
}
