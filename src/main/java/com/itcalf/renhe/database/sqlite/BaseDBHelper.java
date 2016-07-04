package com.itcalf.renhe.database.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.itcalf.renhe.bean.ContactIsSave;

/**
 * 数据库操作抽象类
 *
 * @author piers.xie
 */
public abstract class BaseDBHelper {

	protected DBHelper mDBHelper;
	protected SQLiteDatabase mSQLiteWriter;
	protected SQLiteDatabase mSQLiteReader;
	protected String mTable;

	public BaseDBHelper() {
		super();
	}

	public BaseDBHelper(Context context, String table) {
		super();
		this.mDBHelper = new DBHelper(context);
		this.mTable = table;
	}

	protected DBHelper getDBHelper() {
		return mDBHelper;
	}

	protected String getTable() {
		return mTable;
	}

	protected void setTable(String mTable) {
		this.mTable = mTable;
	}

	/**
	 * 查询表所有记录
	 *
	 * @return
	 */
	public synchronized Cursor findAll() {
		if (null == mSQLiteReader)
			mSQLiteReader = mDBHelper.getReadableDatabase();
		return mSQLiteReader.query(mTable, null, null, null, null, null, null);
	}

	/**
	 * 查询表记录
	 *
	 * @param columns
	 * @param selection
	 * @param selectionArgs
	 * @param groupBy
	 * @param having
	 * @param orderBy
	 * @return
	 */
	public synchronized Cursor find(String[] columns, String selection, String[] selectionArgs, String groupBy, String having,
			String orderBy) {
		if (null == mSQLiteReader)
			mSQLiteReader = mDBHelper.getReadableDatabase();
		return mSQLiteReader.query(mTable, columns, selection, selectionArgs, groupBy, having, orderBy);
	}

	/**
	 * 插入一条记录到数据库
	 *
	 * @return
	 */
	public synchronized long insertData(ContentValues values) {
		if (null == mSQLiteWriter)
			mSQLiteWriter = mDBHelper.getWritableDatabase();
		if (null != mTable && null != values) {
			return mSQLiteWriter.insert(mTable, null, values);
		} else {
			return -1;
		}
	}

	/**
	 * 条件查询某一条数据
	 */
	public synchronized int findByTerm(String sid) {
		if (null == mSQLiteReader)
			mSQLiteReader = mDBHelper.getReadableDatabase();
		Cursor cursor = mSQLiteReader.rawQuery("select " + TablesConstant.CONTACTISSAVE_MAXCID + " from " + mTable + " where "
				+ TablesConstant.CONTACTISSAVE_SID + " =? ", new String[] { sid });

		int maxCid = -1;// 默认-1
		try {
			if (null != cursor) {
				while (cursor.moveToNext()) {
					maxCid = cursor.getInt(0);
				}
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return maxCid;
	}

	/**
	 * 条件查询 maxLasrUpdatedDate
	 */
	public synchronized long findMaxLastUpdatedDateByTerm(String sid) {
		if (null == mSQLiteReader)
			mSQLiteReader = mDBHelper.getReadableDatabase();
		Cursor cursor = mSQLiteReader.rawQuery("select " + TablesConstant.CONTACTISSAVE_MAXLASTUPDATEDDATE + " from " + mTable
				+ " where " + TablesConstant.CONTACTISSAVE_SID + " =? ", new String[] { sid });

		long maxLasrUpdatedDate = -1;// 默认-1
		try {
			if (null != cursor) {
				while (cursor.moveToNext()) {
					maxLasrUpdatedDate = cursor.getLong(0);
				}
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return maxLasrUpdatedDate;
	}

	/**
	 * 获取联系人保存的最大ids
	 * @param sid
	 * @return
	 */
	public synchronized ContactIsSave findMaxIdsByTerm(String sid) {
		if (null == mSQLiteReader)
			mSQLiteReader = mDBHelper.getReadableDatabase();
		Cursor cursor = mSQLiteReader.rawQuery("select * from " + mTable + " where " + TablesConstant.CONTACTISSAVE_SID + " =? ",
				new String[] { sid });

		ContactIsSave contactIsSave = new ContactIsSave();
		try {
			if (null != cursor) {
				while (cursor.moveToNext()) {
					contactIsSave.setSid(cursor.getString(0));
					contactIsSave.setMaxCid(cursor.getInt(1));
					contactIsSave.setMaxLastUpdatedDate(cursor.getLong(2));
					contactIsSave.setMaxMobileId(cursor.getInt(3));
					contactIsSave.setMaxCardId(cursor.getInt(4));
				}
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return contactIsSave;
	}

	/**
	 * 条件查询EMAIL联系人的保存时间
	 */
	public synchronized long findEmailContactTimeByTerm(String sid) {
		if (null == mSQLiteReader)
			mSQLiteReader = mDBHelper.getReadableDatabase();
		Cursor cursor = mSQLiteReader.rawQuery("select " + TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_CREATETIME + " from "
				+ mTable + " where " + TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_MYSID + " =? ", new String[] { sid });
		long maxCid = 0l;// 默认-1
		if (null != cursor) {
			while (cursor.moveToNext()) {
				maxCid = Long.parseLong(cursor.getString(0) != null ? cursor.getString(0) : "0");
			}
		}
		return maxCid;
	}

	/**
	 * 条件查询Mobile
	 */
	public synchronized int findMobileContactMaxidByTerm(String sid) {
		if (null == mSQLiteReader)
			mSQLiteReader = mDBHelper.getReadableDatabase();
		Cursor cursor = mSQLiteReader.rawQuery("select " + TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_MAXID + " from " + mTable
				+ " where " + TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_MYSID + " =? ", new String[] { sid });
		int maxCid = 0;// 默认0
		if (null != cursor) {
			while (cursor.moveToNext()) {
				maxCid = cursor.getInt(0);
			}
		}
		return maxCid;
	}

	/**
	 * 更新一条记录到数据库
	 *
	 * @return
	 */
	public synchronized long updateData(ContentValues values, String whereClaus, String[] whereArgs) {
		if (null == mSQLiteWriter) {
			mSQLiteWriter = mDBHelper.getWritableDatabase();
		}
		if (null != mTable && null != values) {
			return mSQLiteWriter.update(mTable, values, whereClaus, whereArgs);
		} else {
			return -1;
		}
	}

	/**
	 * 删除一条记录到数据库
	 *
	 * @return
	 */
	public synchronized long delData(String whereClaus, String[] whereArgs) {
		if (null == mSQLiteWriter) {
			mSQLiteWriter = mDBHelper.getWritableDatabase();
		}
		if (null != mTable) {
			return mSQLiteWriter.delete(mTable, whereClaus, whereArgs);
		} else {
			return -1;
		}
	}

	/**
	 * 判断是否存在ID的记录
	 *
	 * @return
	 */
	public synchronized boolean isExistContent(String pKColumn, String pkValue, String emColumn, String emValue) {
		boolean isExist = false;
		if (null == mSQLiteReader)
			mSQLiteReader = mDBHelper.getReadableDatabase();
		Cursor lCursor = mSQLiteReader.query(mTable, new String[] { pKColumn, emColumn },
				pKColumn + "=?" + " and " + emColumn + "=?", new String[] { pkValue, emValue }, null, null, null);
		if (null != lCursor && lCursor.moveToFirst()) {
			isExist = true;
		}
		lCursor.close();
		return isExist;
	}

	/**
	 * 判断email是否存在
	 */
	public synchronized boolean isExist(String pKColumn, String pkValue) {
		boolean isExist = false;
		if (null == mSQLiteReader)
			mSQLiteReader = mDBHelper.getReadableDatabase();
		// Cursor lc = mSQLiteReader.rawQuery("select * from " + mTable, null);
		Cursor lCursor = mSQLiteReader.query(mTable, new String[] { pKColumn }, pKColumn + "=?", new String[] { pkValue }, null,
				null, null);
		if (null != lCursor && lCursor.moveToFirst()) {
			isExist = true;
		}
		lCursor.close();
		return isExist;
	}

	/**
	 * 删除表中数据
	 */
	public synchronized boolean delContant(String sql) {
		boolean flag = false;
		if (null == mSQLiteWriter)
			mSQLiteWriter = mDBHelper.getWritableDatabase();
		if (sql.length() < 3) // 简单判断SQL语句的合法性
			return false;
		try {
			mSQLiteWriter.execSQL(sql);
			flag = true;
		} catch (SQLException ex) {
			ex.printStackTrace();
			return false;
		}
		return flag;
	}

	/**
	 * 关闭数据库
	 */
	public synchronized void closeDB() {
		if (null != mSQLiteWriter) {
			mSQLiteWriter.close();
		}
		if (null != mSQLiteReader) {
			mSQLiteReader.close();
		}
		mDBHelper.close();
	}

}
