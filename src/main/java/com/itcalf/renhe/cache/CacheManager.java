package com.itcalf.renhe.cache;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.room.db.AddMsgSQLiteStore;
import com.itcalf.renhe.context.room.db.AddNewMsgManager;
import com.itcalf.renhe.context.room.db.AddNewMsgSQLiteStore;
import com.itcalf.renhe.context.room.db.FindSQLiteStore;
import com.itcalf.renhe.context.room.db.RenMaiQuanManager;
import com.itcalf.renhe.context.uilimagedisplayer.CircleBitmapDisplayer;
import com.itcalf.renhe.context.wukong.im.db.TouTiaoManager;
import com.itcalf.renhe.dto.SearchHistoryItem;
import com.itcalf.renhe.utils.DraftUtil;
import com.itcalf.renhe.utils.FileSizeUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.L;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 缓存管理器
 *
 * @author xp
 */
public class CacheManager {
    private static final String TAG = "CacheManager";
    public static final String DBNAME = "hl_circle_db_V2";
    public static final String ADD_MSG_DBNAME = "hl_add_msg_db";
    private static CacheManager cache;
    private Context ct;
    public static final String PROFILE = "10001";
    private SQLiteDatabase db;
    private static final String SEARCHDBNAME = "searchdb";
    private static final String SEARCHTABLE = "advancesearch_history";
    private SQLiteDatabase searchdb;
    private static final int SEARCH_HISTORY_MAX_COUNT = 10;//搜索历史最多显示10条,显示最新的10条

    private static final String ROOM_CACHE_FOLDER_SUFFIX = "_renhe_room_cache_new";
    public static final String CONVERSATION_DBNAME = "conversation_db";
    public static final String CIRCLE_DBNAME = "hll_circle_db";

    public static CacheManager getInstance() {
        if (null == cache) {
            cache = new CacheManager();
        }
        return cache;
    }

    public CacheManager populateData(Context ct) {
        this.ct = ct;
        return cache;
    }

    /**
     * 缓存对象
     *
     * @param obj
     * @param cacheFile
     * @return
     */
    public boolean saveObject(Object obj, String email, String cacheFile) {
        File suspend_f = new File(ExternalStorageUtil.getCacheDataPath(ct, email), cacheFile + ROOM_CACHE_FOLDER_SUFFIX);
        return saveSeri(obj, suspend_f);

    }

    /**
     * 删除缓存对象
     *
     * @param email
     * @param cacheFile
     * @return wangning
     */
    public boolean deleteObject(String email, String cacheFile) {
        File path = new File(ExternalStorageUtil.getCacheDataPath(ct, email), cacheFile + ROOM_CACHE_FOLDER_SUFFIX);
        if (path.exists()) {
            path.delete();
            return true;
        }
        return false;
    }

    private static boolean saveSeri(Object obj, File suspend_f) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        boolean keep = true;
        try {
            fos = new FileOutputStream(suspend_f);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(obj);
            oos.flush();
        } catch (Exception e) {
            keep = false;
            Log.e(TAG, "保存到本地时Error");
        } finally {
            try {
                if (oos != null)
                    oos.close();
                if (fos != null)
                    fos.close();
                if (keep == false)
                    suspend_f.delete();
                oos = null;
                fos = null;
                suspend_f = null;
            } catch (Exception e) {
                Log.e(TAG, "保存到本地时关闭流Error");
            }
        }
        return keep;
    }

    /**
     * 读取缓存对象
     *
     * @param path
     * @return
     */
    public Object getObject(String email, String path) {
        File suspend_f = new File(ExternalStorageUtil.getCacheDataPath(ct, email), path + ROOM_CACHE_FOLDER_SUFFIX);
        if (ifExpired(email, path + ROOM_CACHE_FOLDER_SUFFIX)) {
            if (isNetworkConnected(ct)) {
                return null;
            }
        }
        return getSeri(suspend_f);
    }

    public boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    private static Object getSeri(File suspend_f) {
        Object obj = null;
        FileInputStream fis = null;
        ObjectInputStream is = null;
        try {
            fis = new FileInputStream(suspend_f);
            is = new ObjectInputStream(fis);
            obj = is.readObject();
        } catch (Exception e) {
            Log.e(TAG, "读取本地Cache时关闭流Error");
        } finally {
            try {
                if (fis != null)
                    fis.close();
                if (is != null)
                    is.close();
                fis = null;
                is = null;
                suspend_f = null;
            } catch (Exception e) {
                Log.e(TAG, "读取本地Cache时关闭流Error");
            }
        }
        return obj;
    }

    /**
     * 清除缓存目录
     */
    public void clearCache(String email, boolean clearDB) {
        Logger.w("im>>>" + ImageLoader.getInstance().getDiskCache().getDirectory().getAbsolutePath());
        delFolder(ImageLoader.getInstance().getDiskCache().getDirectory().getAbsolutePath());
        //打开或创建message.db数据库
        new RenMaiQuanManager(ct).deleteDatabase();
        new AddNewMsgManager(ct).deleteDatabase();
        new TouTiaoManager(ct).deleteDatabase();
        if (clearDB) {
            ct.deleteDatabase(DBNAME);
            ct.deleteDatabase(AddNewMsgSQLiteStore.DBNAME);
            ct.deleteDatabase(FindSQLiteStore.DBNAME);
        }
        ct.deleteDatabase(AddMsgSQLiteStore.DBNAME);
        ct.deleteDatabase(AddNewMsgSQLiteStore.DBNAME);
        ct.deleteDatabase(CONVERSATION_DBNAME);
        SharedPreferences msp = ct.getSharedPreferences("conversation_list", 0);
        SharedPreferences.Editor mEditor = msp.edit();
        mEditor.putLong("toutiao_maxUpdatedDate", 0);
        mEditor.putLong("toutiao_minUpdatedDate", 0);
        mEditor.commit();
//        FileUtils.deleteDir();
        DraftUtil.clearAllDraft();
    }

    /**
     * 获取指定email缓存目录大小
     */
    public double getCacheSize() {

        double size = 0;
//        String path = Environment.getExternalStorageDirectory() + File.separator + "Android" + File.separator + "data"
//                + File.separator + email + File.separator + "cache" + File.separator;
//        size = FileSizeUtil.getFileOrFilesSize(path, 3);
//        size = size + FileSizeUtil.getFileOrFilesSize(DEFAULT_IMAGECACHE_FOLDER, 3);
        size = size + FileSizeUtil.getFileOrFilesSize(ImageLoader.getInstance().getDiskCache().getDirectory().getAbsolutePath(), 3);
        db = (db != null && db.isOpen()) ? db : openDB();
        if (db != null)
            size = size + FileSizeUtil.getFileOrFilesSize(db.getPath(), 3);
        return size;
    }

    /**
     * 删除文件夹
     *
     * @return boolean
     */
    public static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); // 删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath.toString();
            java.io.File myFilePath = new java.io.File(filePath);
            myFilePath.delete(); // 删除空文件夹
        } catch (Exception e) {
            Log.e(TAG, "删除文件夹Error");
            e.printStackTrace();
        }

    }

    /**
     * 删除文件夹里面的所有文件
     *
     * @param path String 文件夹路径 如 c:/fqf
     */
    public static void delAllFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        if (!file.isDirectory()) {
            return;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件
                delFolder(path + "/" + tempList[i]);// 再删除空文件夹
            }
        }
    }

    /**
     * 判断缓存文件是否过期 48小时
     */
    public boolean ifExpired(String email, String path) {
        boolean expired = true;
        File suspend_f = new File(ExternalStorageUtil.getCacheDataPath(ct, email), path);
        long expiredTime = System.currentTimeMillis() - suspend_f.lastModified();
        if (expiredTime < 48 * 60 * 60 * 1000) { //缓存过期时间是48小时
            expired = false;
        }
        return expired;
    }

    //建立数据库
    public SQLiteDatabase openDB() {
        try {
            db = RenheApplication.getInstance().openOrCreateDatabase(DBNAME, Context.MODE_PRIVATE, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return db;
    }

    //判断表是否存在

    /**
     * 判断某张表是否存在
     *
     * @return
     */
    public boolean isTableExist(String tableName) {
        boolean result = false;
        if (null != db && db.isOpen()) {
            if (tableName == null) {
                return false;
            }
            Cursor cursor = null;
            try {
                String sql = "select count(*) as c from Sqlite_master  where type ='table' and name ='" + tableName.trim() + "' ";
                cursor = db.rawQuery(sql, null);
                if (cursor.moveToNext()) {
                    int count = cursor.getInt(0);
                    if (count > 0) {
                        result = true;
                    }
                }

            } catch (Exception e) {
            }
        }
        return result;
    }

    public static final String DEFAULT_IMAGECACHE_FOLDER = new StringBuilder()
            .append(Environment.getExternalStorageDirectory().getAbsolutePath()).append(File.separator).append("AndroidSystm")
            .append(File.separator).append("AndroidRenhe").append(File.separator).append("ImageCache").toString();

    public Bitmap getDiskBitmap(String pathString) {
        Bitmap bitmap = null;
        try {
            File file = new File(pathString);
            if (file.exists()) {
                bitmap = BitmapFactory.decodeFile(pathString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static AlphaAnimation getInAlphaAnimation(long durationMillis) {
        AlphaAnimation inAlphaAnimation = new AlphaAnimation(0, 1);
        inAlphaAnimation.setDuration(durationMillis);
        return inAlphaAnimation;
    }

    public static DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.avatar)
            .showImageForEmptyUri(R.drawable.avatar).showImageOnFail(R.drawable.avatar).cacheInMemory(true).cacheOnDisk(true)
            .imageScaleType(ImageScaleType.EXACTLY_STRETCHED).considerExifParams(true)
//            .displayer(new RoundedBitmapDisplayer(77))// 我们的图片大小是48dp 所以我这里半径=48*1.6 ,其他情况可以自己算,或者动态设置
//            .displayer(new CircleBitmapDisplayer())
            .build();
    public static DisplayImageOptions circleImageOptions = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.avatar)
            .showImageForEmptyUri(R.drawable.avatar).showImageOnFail(R.drawable.avatar).cacheInMemory(true).cacheOnDisk(true)
            .imageScaleType(ImageScaleType.EXACTLY_STRETCHED).considerExifParams(true)
//            .displayer(new RoundedBitmapDisplayer(77))// 我们的图片大小是48dp 所以我这里半径=48*1.6 ,其他情况可以自己算,或者动态设置
            .displayer(new CircleBitmapDisplayer())
            .build();

    public static AnimateFirstDisplayListener animateFirstDisplayListener = new AnimateFirstDisplayListener();

    /**
     * 图片加载第一次显示监听器
     *
     * @author Administrator
     */
    public static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                // 是否第一次显示
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    // 图片淡入效果
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }
    }

    /**
     * 档案背景图加载参数设置
     */
    public static DisplayImageOptions coverOptions = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.cover_bg).showImageForEmptyUri(R.drawable.cover_bg)
            .showImageOnFail(R.drawable.cover_bg).cacheInMemory(false).cacheOnDisk(true)
            .imageScaleType(ImageScaleType.EXACTLY_STRETCHED).considerExifParams(true)
            .build();
    /**
     * 图片加载参数
     */
    public static DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.room_pic_default_bcg).showImageForEmptyUri(R.drawable.room_pic_default_bcg)
            .showImageOnFail(R.drawable.room_pic_default_bcg).cacheInMemory(true).cacheOnDisk(true)
            .imageScaleType(ImageScaleType.EXACTLY_STRETCHED).considerExifParams(false)
                    //	.displayer(new RoundedBitmapDisplayer(20))
            .build();
    /**
     * IM图片加载参数
     */
    public static DisplayImageOptions imImageOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.image_load_default_shape).showImageForEmptyUri(R.drawable.image_load_default_shape)
            .showImageOnFail(R.drawable.image_load_default_shape).cacheInMemory(true).cacheOnDisk(true)
            .imageScaleType(ImageScaleType.EXACTLY_STRETCHED).considerExifParams(false)
                    //	.displayer(new RoundedBitmapDisplayer(20))
            .build();
    /**
     * IM web分享的图片加载参数
     */
    public static DisplayImageOptions imWebImageOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.room_pic_default_bcg).showImageForEmptyUri(R.drawable.chat_link_default)
            .showImageOnFail(R.drawable.chat_link_default).cacheInMemory(true).cacheOnDisk(true)
            .imageScaleType(ImageScaleType.EXACTLY_STRETCHED).considerExifParams(false)
                    //	.displayer(new RoundedBitmapDisplayer(20))
            .build();
    /**
     * 人脉圈图片加载参数
     */
    public static DisplayImageOptions rmqImageOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.rmq_image_load_default_shape).showImageForEmptyUri(R.drawable.rmq_image_load_default_shape)
            .showImageOnFail(R.drawable.rmq_image_load_default_shape).cacheInMemory(true).cacheOnDisk(true)
            .imageScaleType(ImageScaleType.EXACTLY_STRETCHED).considerExifParams(false)
                    //	.displayer(new RoundedBitmapDisplayer(20))
            .build();
    /**
     * 查看大图片加载参数
     */
    public static DisplayImageOptions largeImageOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.tran_drawable).showImageForEmptyUri(R.drawable.tran_drawable)
            .showImageOnFail(R.drawable.tran_drawable).cacheInMemory(true).cacheOnDisk(true)
            .imageScaleType(ImageScaleType.EXACTLY_STRETCHED).considerExifParams(false)
                    //	.displayer(new RoundedBitmapDisplayer(20))
            .build();
    /**
     * 人脉圈独有图片加载参数
     */
    public static DisplayImageOptions renmaiImageOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.room_pic_default_bcg).showImageForEmptyUri(R.drawable.room_pic_default_bcg)
            .showImageOnFail(R.drawable.room_pic_default_bcg).cacheInMemory(true).cacheOnDisk(true)
            .imageScaleType(ImageScaleType.NONE).considerExifParams(false)
                    //	.displayer(new RoundedBitmapDisplayer(20))
            .build();
    /**
     * 名片独有图片加载参数
     */
    public static DisplayImageOptions ocrCardImageOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.room_pic_default_bcg).showImageForEmptyUri(R.drawable.room_pic_default_bcg)
            .showImageOnFail(R.drawable.room_pic_default_bcg).cacheInMemory(true).cacheOnDisk(true)
            .imageScaleType(ImageScaleType.NONE).considerExifParams(false)
                    //	.displayer(new RoundedBitmapDisplayer(20))
            .build();
    public static ImageAnimateFirstDisplayListener imageAnimateFirstDisplayListener = new ImageAnimateFirstDisplayListener();

    /**
     * 图片加载第一次显示监听器
     *
     * @author Administrator
     */
    public static class ImageAnimateFirstDisplayListener extends SimpleImageLoadingListener {

        static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                // 是否第一次显示
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    // 图片淡入效果
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }
    }

    // 创建搜索历史数据库
    public SQLiteDatabase openSearchDB() {
        try {
            searchdb = RenheApplication.getInstance().openOrCreateDatabase(SEARCHDBNAME, Context.MODE_PRIVATE, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return searchdb;
    }

    // 建立查询记录的表
    public void createSearchListoryTable() {
        if (null != searchdb && searchdb.isOpen()) {
            searchdb.execSQL("CREATE TABLE if not exists " + SEARCHTABLE
                    + " (keyword VARCHAR, area VARCHAR,areacode int,industry VARCHAR,industrycode int,company VARCHAR,job VARCHAR,name VARCHAR,createTime LONG)");
        }
    }

    public void saveSearchListoryItem(String keyword, String area, int areacode, String industry, int industrycode,
                                      String company, String job, String name) {
        boolean isExist = false;
        try {
            if (null != searchdb && searchdb.isOpen()) {
                // SimpleDateFormat formatter = new SimpleDateFormat(
                // "yyyy-MM-dd HH:mm:ss");
                long curDate = System.currentTimeMillis();// 获取当前时间
                Cursor c2 = searchdb.rawQuery("SELECT * FROM " + SEARCHTABLE, null);
                while (c2.moveToNext()) {
                    StringBuffer sb = new StringBuffer();
                    sb.append(keyword + area + industry + company + job + name);

                    StringBuffer mStringBuffer = new StringBuffer();
                    mStringBuffer.append(c2.getString(c2.getColumnIndex("keyword")));
                    mStringBuffer.append(c2.getString(c2.getColumnIndex("area")));
                    mStringBuffer.append(c2.getString(c2.getColumnIndex("industry")));
                    mStringBuffer.append(c2.getString(c2.getColumnIndex("company")));
                    mStringBuffer.append(c2.getString(c2.getColumnIndex("job")));
                    mStringBuffer.append(c2.getString(c2.getColumnIndex("name")));

                    if (sb.toString().equals(mStringBuffer.toString())) {
                        isExist = true;
                        break;
                    }
                }
                if (!isExist) {
                    if (c2.getCount() >= SEARCH_HISTORY_MAX_COUNT) {
                        int count = c2.getCount() - SEARCH_HISTORY_MAX_COUNT;
                        if (null != searchdb && searchdb.isOpen()) {
                            Cursor c3 = searchdb.rawQuery("SELECT * FROM " + SEARCHTABLE + " ORDER BY createTime ASC ", null);
                            while (count >= 0 && c3.moveToNext()) {
                                searchdb.delete(SEARCHTABLE, "createTime = ?",
                                        new String[]{c3.getLong(c3.getColumnIndex("createTime")) + ""});
                                count--;
                            }
                            c3.close();
                        }
                    }
                    insertSearchItem(keyword, area, areacode, industry, industrycode, company, job, name, curDate);
                }
                c2.close();
            }
        } catch (Exception e) {
        }
    }

    public void insertSearchItem(String keyword, String area, int areacode, String industry, int industrycode, String company,
                                 String job, String name, long createTime) {
        if (null != searchdb && searchdb.isOpen()) {
            searchdb.execSQL("INSERT INTO " + SEARCHTABLE + " VALUES (?,?,?, ?,?, ?,?,?, ?)",
                    new Object[]{keyword, area, areacode, industry, industrycode, company, job, name, createTime});
        }
    }

    public List<SearchHistoryItem> querySearchHistory() {
        List<SearchHistoryItem> list = new ArrayList<SearchHistoryItem>();
        if (null != searchdb && searchdb.isOpen()) {
            Cursor c2 = searchdb.rawQuery("SELECT * FROM " + SEARCHTABLE + " ORDER BY createTime DESC ", null);
            while (c2.moveToNext()) {
                long curDate = c2.getLong(c2.getColumnIndex("createTime"));// 获取当前时间
                //				if (System.currentTimeMillis() - curDate < SEARCH_HISTORY_EXPIRED_TIME) {
                String mkeyword = c2.getString(c2.getColumnIndex("keyword"));
                String marea = c2.getString(c2.getColumnIndex("area"));
                int mareaCode = c2.getInt(c2.getColumnIndex("areacode"));
                String mindustry = c2.getString(c2.getColumnIndex("industry"));
                int mindustryCode = c2.getInt(c2.getColumnIndex("industrycode"));
                String mcompany = c2.getString(c2.getColumnIndex("company"));
                String mjob = c2.getString(c2.getColumnIndex("job"));
                String mname = c2.getString(c2.getColumnIndex("name"));

                SearchHistoryItem searchHistoryItem = new SearchHistoryItem(mkeyword, marea, mindustry, mareaCode, mindustryCode,
                        mcompany, mjob, mname, curDate);
                list.add(searchHistoryItem);
                //				}
                //				else {
                //					searchdb.delete(SEARCHTABLE, "createTime = ?", new String[] { curDate + "" });
                //				}
            }
            c2.close();
        }
        return list;
    }

    public static File getExternalCacheDir(Context context) {
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, context.getPackageName()), "cache");
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                L.w("Unable to create external cache directory");
                return null;
            }
            try {
                new File(appCacheDir, ".nomedia").createNewFile();
            } catch (IOException e) {
                L.i("Can't create \".nomedia\" file in application external cache directory");
            }
        }
        return appCacheDir;
    }

    public void clearSearchHistory() {
        if (null != searchdb && searchdb.isOpen()) {
            searchdb.execSQL("DROP TABLE IF EXISTS " + SEARCHTABLE);
        }
    }
}
