package com.itcalf.renhe.context.wukong.im.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.itcalf.renhe.cache.CacheManager;

public class CircleHelper extends SQLiteOpenHelper {
	private static final int DB_VIRSION = 1;
	public static final String DB_NAME = CacheManager.CIRCLE_DBNAME;
	/** 圈子表 */
	public final String TAB_DOWNCOURSE = "HLCircleTab";

	public CircleHelper(Context context) {
		super(context, DB_NAME, null, DB_VIRSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TAB_DOWNCOURSE + "(" + "_ID INTEGER PRIMARY KEY AUTOINCREMENT," + "SID TEXT," + "ADSID TEXT,"
				+ "CIRCLEID TEXT," + "IMCONVERSATIONID TEXT," + "PREAVATARID TEXT," + "NAME TEXT," + "JOINTYPE TEXT,"
				+ "NOTE TEXT," + "IMMEMBERIDS TEXT," + "SEARCHABLE NUMERIC NOT NULL DEFAULT 0)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
	}

}
