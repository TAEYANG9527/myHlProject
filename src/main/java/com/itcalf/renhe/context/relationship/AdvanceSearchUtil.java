package com.itcalf.renhe.context.relationship;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.archives.edit.EditEduInfoSelectSchool;
import com.itcalf.renhe.dto.SearchCity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdvanceSearchUtil {
    public static final int CHINAL_CODE = 10001;//标识中国

    /**
     * 把assets下面的db文件保存到本地sd卡
     *
     * @param context
     * @throws IOException
     */
    public static void copyDB(Context context, String fileName) throws IOException {
        String filePath = Constants.CACHE_PATH.ASSETS_DB_PATH;
        if (new File(filePath + fileName).exists()) {
            return;
        }
        File file = new File(filePath);
        if (!file.isDirectory()) {
            file.mkdirs();
        }
        FileOutputStream fos = new FileOutputStream(filePath + fileName);
        InputStream is = context.getResources().getAssets().open(fileName);
        byte[] buffer = new byte[1024 * 5];
        int count = 0;
        while ((count = is.read(buffer)) > 0) {
            fos.write(buffer, 0, count);
        }
        fos.close();
        is.close();
    }

    public static void deleteIndustryDB(Context context) throws IOException {
        String DBNAME = "industry";
        String filePath = Constants.CACHE_PATH.ASSETS_DB_PATH;
        String renamefilePath = Constants.CACHE_PATH.ASSETS_DB_PATH
                + "delete_temp" + File.separator;
        File pathFile = new File(filePath);
        if (pathFile.exists()) {
            pathFile.renameTo(new File(renamefilePath));
        }
        CacheManager.delFolder(renamefilePath);
        copyDB(context, DBNAME);
    }

    /**
     * @param db
     * @param tableName
     * @return
     */
    public static SearchCity[] getCity(SQLiteDatabase db, String tableName) {
        SearchCity[] cityArrays = null;
        Cursor cursor = null;
        try {
            cursor = db.query(tableName, new String[]{"id", "name"}, "type=?", new String[]{"city"}, null, null,
                    "id ASC");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                cityArrays = new SearchCity[cursor.getCount()];
                do {
                    SearchCity searchCity = new SearchCity();
                    searchCity.setId(cursor.getInt(0));
                    searchCity.setName(cursor.getString(1));
                    cityArrays[cursor.getPosition()] = searchCity;
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return cityArrays;
    }

    public static SearchCity[] getChildCity(SQLiteDatabase db, String tableName, int superId) {
        SearchCity[] cityArrays = null;
        Cursor cursor = null;
        try {
            cursor = db.query(tableName, new String[]{"id", "name"}, "type=? AND super_id=?",
                    new String[]{"city", superId + ""}, null, null, "id ASC");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                cityArrays = new SearchCity[cursor.getCount()];
                do {
                    SearchCity searchCity = new SearchCity();
                    searchCity.setId(cursor.getInt(0));
                    searchCity.setName(cursor.getString(1));
                    cityArrays[cursor.getPosition()] = searchCity;
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return cityArrays;
    }

    public static SearchCity[] getProvince(SQLiteDatabase db, String tableName) {
        SearchCity[] cityArrays = null;
        Cursor cursor = null;
        try {
            cursor = db.query(tableName, new String[]{"id", "name"}, "type=? AND super_id=?",
                    new String[]{"province", CHINAL_CODE + ""}, null, null, "id ASC");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                cityArrays = new SearchCity[cursor.getCount()];
                do {
                    SearchCity searchCity = new SearchCity();
                    searchCity.setId(cursor.getInt(0));
                    searchCity.setName(cursor.getString(1));
                    cityArrays[cursor.getPosition()] = searchCity;
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return cityArrays;
    }

    public static SearchCity[] getForeignCity(SQLiteDatabase db, String tableName) {
        SearchCity[] cityArrays = null;
        Cursor cursor = null;
        try {
            cursor = db.query(tableName, new String[]{"id", "name"}, "type=?", new String[]{"country"}, null, null,
                    "id ASC");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                cityArrays = new SearchCity[cursor.getCount()];
                do {
                    SearchCity searchCity = new SearchCity();
                    searchCity.setId(cursor.getInt(0));
                    searchCity.setName(cursor.getString(1));
                    cityArrays[cursor.getPosition()] = searchCity;
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return cityArrays;
    }

    public static SearchCity getCurrentCity(SQLiteDatabase db, String tableName, String cityName) {
        SearchCity city = null;
        Cursor cursor = null;
        try {
            cursor = db.query(tableName, new String[]{"id"}, "type=? AND name=?", new String[]{"city", cityName}, null,
                    null, "id ASC");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cursor != null) {
            if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                city = new SearchCity();
                city.setId(cursor.getInt(0));
                city.setName(cityName);
            }
            cursor.close();
        }
        return city;
    }

    public static String getCityName(SQLiteDatabase db, String tableName, int cityId) {
        String name = null;
        Cursor cursor = null;
        try {
            cursor = db.query(tableName, new String[]{"name"}, "type=? AND id=?", new String[]{"city", cityId + ""},
                    null, null, "id ASC");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cursor != null) {
            if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                name = cursor.getString(0);
            }
            cursor.close();
        }
        return name;
    }

    public static String getProvinceName(SQLiteDatabase db, String tableName, int provinceId) {
        String name = null;
        Cursor cursor = db.query(tableName, new String[]{"name"}, "type=? AND id=?",
                new String[]{"province", provinceId + ""}, null, null, "id ASC");
        if (cursor != null) {
            if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                name = cursor.getString(0);
            }
            cursor.close();
        }
        return name;
    }

    public static String getIndustryName(SQLiteDatabase db, String tableName, int industryId) {
        String name = null;
        Cursor cursor = db.query(tableName, new String[]{"name"}, "id=?", new String[]{industryId + ""}, null, null,
                "id ASC");
        if (cursor != null) {
            if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                name = cursor.getString(0);
            }
            cursor.close();
        }
        return name;
    }

    public static String getChildIndustryName(SQLiteDatabase db, String tableName, int childIndustryId) {
        String name = null;
        try {
            Cursor cursor = db.query(tableName, new String[]{"name"}, "id=?", new String[]{childIndustryId + ""}, null,
                    null, "id ASC");
            if (cursor != null) {
                if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                    name = cursor.getString(0);
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

    public static SearchCity[] getIndustry(SQLiteDatabase db, String tableName) {
        SearchCity[] cityArrays = null;
        try {
            Cursor cursor = db.query(tableName, new String[]{"id", "name"}, "super_category_id =?", new String[]{"0"}, null,
                    null, "order_id DESC");
            try {
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        cityArrays = new SearchCity[cursor.getCount()];
                        do {
                            //				if(!cursor.getString(1).equals("其它")){
                            SearchCity searchCity = new SearchCity();
                            searchCity.setId(cursor.getInt(0));
                            searchCity.setName(cursor.getString(1));
                            cityArrays[cursor.getPosition()] = searchCity;
                            //				}
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    deleteIndustryDB(RenheApplication.getInstance().getApplicationContext());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                deleteIndustryDB(RenheApplication.getInstance().getApplicationContext());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return cityArrays;
    }

    /**
     * @param db
     * @param tableName
     * @param dqx_dqxx01
     * @param Municipalities
     * @return
     */
    public static SearchCity[] getChildIndustry(SQLiteDatabase db, String tableName, int super_id, String super_name) {
        SearchCity[] cityArrays = null;
        try {
            Cursor cursor = db.query(tableName, new String[]{"id", "name"}, "super_category_id=?",
                    new String[]{"" + super_id}, null, null, "order_id ASC");

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    cityArrays = new SearchCity[cursor.getCount()];
                    do {
                        //				if(!cursor.getString(1).equals("其它")){
                        if (!cursor.getString(1).equals(super_name) || cursor.getString(1).equals("其它")) {
                            SearchCity searchCity = new SearchCity();
                            searchCity.setId(cursor.getInt(0));
                            searchCity.setName(cursor.getString(1));
                            cityArrays[cursor.getPosition()] = searchCity;
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cityArrays;
    }

    public static int findPrimaryKey(SQLiteDatabase db, String tableName, String address) {
        int key = -1;
        Cursor cursor = db.query(tableName, new String[]{"DQXX01"}, "DQXX05=?", new String[]{address}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToNext()) {
                key = cursor.getInt(0);
            }
        }
        return key;
    }

    /**
     * 获取大学
     *
     * @param db
     * @param tableName
     * @return
     */
    public static List<Map<String, Object>> getSchool(SQLiteDatabase db, String tableName, String key) {
        if (db != null) {
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            Cursor cursor = null;
            try {
                cursor = db.query(tableName, new String[]{"id", "name"}, "name LIKE ?", new String[]{key + "%"}, null,
                        null, "id ASC");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        SearchCity searchCity = new SearchCity();
                        searchCity.setId(cursor.getInt(0));
                        searchCity.setName(cursor.getString(1));
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("name", cursor.getString(1));
                        map.put("id", cursor.getInt(0));
                        list.add(map);
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
            return list;
        }
        return null;
    }

    public static int getSchoolId(SQLiteDatabase db, String tableName, String key) {
        if (db != null) {
            Cursor cursor = null;
            try {
                cursor = db.query(tableName, new String[]{"id"}, "name =?", new String[]{key}, null, null, "id ASC");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        return cursor.getInt(0);
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
        }
        return EditEduInfoSelectSchool.NO_THIS_SCHOOL;
    }
}
