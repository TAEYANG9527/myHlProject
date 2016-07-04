package com.itcalf.renhe.context.room;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.NewsWeiboAdapter;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.room.RoomNewMsgTask.IRoomBack;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.NewMessageBoards;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.umeng.analytics.MobclickAgent;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Feature:显示留言列表界面 Description:显示未读留言列表界面
 *
 * @author wangning
 */
public class NewMessageBoardActivity extends BaseActivity {
    // 留言列表
    private ListView mWeiboListView;
    // 数据适配器
    private NewsWeiboAdapter mAdapter;
    // 留言显示数据
    private List<Map<String, Object>> mWeiboList = new ArrayList<Map<String, Object>>();
    private List<NewMessageBoards> messageBoardsList = new ArrayList<NewMessageBoards>();
    private SQLiteDatabase db;
    private static final long EXPIRED_TIME = 24 * 60 * 60 * 1000 * 14;
    private RelativeLayout blankLayout;
    private TextView blankTv;
    private ImageView nowifiIv;
    private boolean hasCache = false;
    private boolean isEmpty = true;

    //	private FadeUitl fadeUitl;
    //	private RelativeLayout rootRl;
    private String sid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTemplate().doInActivity(this, R.layout.news_rooms_msg_list);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("未读留言"); // 统计页面
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("未读留言"); // 保证 onPageEnd 在onPause 之前调用,因为
        // onPause 中会保存信息
        MobclickAgent.onPause(this);
    }

    @Override
    protected void findView() {
        super.findView();
        //		rootRl = (RelativeLayout) findViewById(R.id.rootRl);
        mWeiboListView = (ListView) findViewById(R.id.weibo_list);
        // findViewById(R.id.editBt).setVisibility(View.INVISIBLE);
        blankLayout = (RelativeLayout) findViewById(R.id.blank_rl);
        blankTv = (TextView) findViewById(R.id.balnk_rl_tv);
        nowifiIv = (ImageView) findViewById(R.id.noreplyiv);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem sendItem = menu.findItem(R.id.item_delete);
        sendItem.setTitle("清空");
        if (!isEmpty) {
            sendItem.setVisible(true);
            sendItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        } else {
            sendItem.setVisible(false);
            // sendItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_delete:
                MobclickAgent.onEvent(NewMessageBoardActivity.this, "clear_notify");
                clearCache();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearCache() {
        MaterialDialogsUtil materialDialogsUtil = new MaterialDialogsUtil(this);
        materialDialogsUtil.getBuilder(R.string.renmaiquan_unread_message_clear_tip).callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                if (null != mWeiboList) {
                    mWeiboList.clear();
                    if (null != mAdapter) {
                        mAdapter.notifyDataSetChanged();
                    }
                    isEmpty = true;
                    invalidateOptionsMenu();
                }
                if (null != db && db.isOpen()) {
                    db.execSQL("DROP TABLE IF EXISTS hlunreadmsg");
                    blankLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNeutral(MaterialDialog dialog) {
            }

            @Override
            public void onNegative(MaterialDialog dialog) {
            }
        });
        materialDialogsUtil.show();
    }

    @Override
    protected void initData() {
        super.initData();
        // 留言列表适配器
        setTextValue(R.id.title_txt, "提醒");
        mAdapter = new NewsWeiboAdapter(this, mWeiboList, getRenheApplication().getUserInfo().getEmail(), mWeiboListView);
        mWeiboListView.setAdapter(mAdapter);
        showDialog(1);
        sid = RenheApplication.getInstance().getUserInfo().getSid();
        //		fadeUitl = new FadeUitl(this, "加载中...");
        //		fadeUitl.addFade(rootRl);

        // 打开或创建message.db数据库
        //		String email = RenheApplication.getInstance().getUserInfo().getEmail();
        db = CacheManager.getInstance().openDB();
        if (null == db) {
            initLoaded();// 读取服务器数据
        } else {
            new Thread(new ThreadLoadCache()).start();
            //			new Handler().post(new Runnable() {
            //				@Override
            //				public void run() {
            //					// 创建unreadmsg表
            //					db.execSQL("CREATE TABLE if not exists unreadmsg (_id INTEGER PRIMARY KEY AUTOINCREMENT, notifyObjectId VARCHAR, messageBoardObjectId VARCHAR,"
            //							+ "type SMALLINT, sendersid VARCHAR, sendername VARCHAR,"
            //							+ "senderuserface VARCHAR, senderreplyContent VARCHAR, sendercreatedDate VARCHAR,"
            //							+ "fromSource VARCHAR, sourcecontent VARCHAR, sourceobjectId VARCHAR, firstreaddate VARCHAR, noticeType SMALLINT)");
            //					loadCache();
            //				}
            //			});
        }
    }

    class ThreadLoadCache implements Runnable {
        @Override
        public void run() {
            db.execSQL(
                    "CREATE TABLE if not exists hlunreadmsg (_id INTEGER PRIMARY KEY AUTOINCREMENT, notifyObjectId VARCHAR, messageBoardObjectId VARCHAR,"
                            + "type SMALLINT, sendersid VARCHAR, sendername VARCHAR,"
                            + "senderuserface VARCHAR, senderreplyContent VARCHAR, sendercreatedDate VARCHAR,"
                            + "fromSource VARCHAR, sourcecontent VARCHAR, sourceobjectId VARCHAR, firstreaddate VARCHAR, noticeType SMALLINT,sid VARCHAR)");
            loadCache();
        }
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    //完成主界面更新,拿到数据
                    List<Map<String, Object>> mList = (List<Map<String, Object>>) msg.obj;
                    if (mList != null && mList.size() > 0) {
                        mWeiboList.addAll(mList);
                        hasCache = true;
                        isEmpty = false;
                        invalidateOptionsMenu();
                    }
                    mAdapter.notifyDataSetChanged();
                    initLoaded();// 读取服务器数据
                    break;
                default:
                    break;
            }
        }
    };

    private void filterCacheByResult(List<Map<String, Object>> result) {
        for (int i = result.size() - 1; i >= 0; i--) {
            HashMap<String, Object> map = (HashMap<String, Object>) result.get(i);
            if (null != db) {
                Cursor c2 = db.rawQuery("SELECT * FROM hlunreadmsg WHERE notifyObjectId = ?",
                        new String[]{(String) map.get("notifyObjectId")});
                if (!c2.moveToNext()) {
                    mWeiboList.add(0, map);
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private void loadCache() {
        List<Map<String, Object>> mList = new ArrayList<Map<String, Object>>();
        Cursor c = db.rawQuery("SELECT * FROM hlunreadmsg where sid=?", new String[]{sid});
        while (c.moveToNext()) {
            //			int _id = c.getInt(c.getColumnIndex("_id"));
            String unreadObjectId = c.getString(c.getColumnIndex("messageBoardObjectId"));
            String notifyObjectId = c.getString(c.getColumnIndex("notifyObjectId"));
            String sourceObjectId = c.getString(c.getColumnIndex("sourceobjectId"));
            String sourceContent = c.getString(c.getColumnIndex("sourcecontent"));
            String userface = c.getString(c.getColumnIndex("senderuserface"));
            String senderSid = c.getString(c.getColumnIndex("sendersid"));
            String senderUsername = c.getString(c.getColumnIndex("sendername"));
            String datetime = c.getString(c.getColumnIndex("sendercreatedDate"));
            String replyContent = c.getString(c.getColumnIndex("senderreplyContent"));
            String client = c.getString(c.getColumnIndex("fromSource"));
            int noticeType = c.getInt(c.getColumnIndex("noticeType"));
            int type = c.getInt(c.getColumnIndex("type"));
            String firstReadDate = c.getString(c.getColumnIndex("firstreaddate"));
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Date date = dateFormat.parse(firstReadDate);
                if (System.currentTimeMillis() - date.getTime() > EXPIRED_TIME) {
                    // 删除数据
                    db.delete("hlunreadmsg", "notifyObjectId = ? and sid = ?", new String[]{notifyObjectId, sid});
                    continue;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("type", type);
            map.put("noticeType", noticeType);
            map.put("unreadObjectId", unreadObjectId);
            map.put("notifyObjectId", notifyObjectId);
            map.put("sourceObjectId", sourceObjectId);
            map.put("sourceContent", sourceContent);
            map.put("userface", userface);
            map.put("senderSid", senderSid);
            map.put("senderUsername", senderUsername);
            map.put("datetime", datetime);
            map.put("replyContent", replyContent);
            map.put("client", "来自" + client);
            map.put("avatar", R.drawable.avatar);
            mList.add(0, map);
        }
        c.close();

        //需要数据传递，用下面方法；
        Message msg = new Message();
        msg.obj = mList;
        msg.what = 0;
        mHandler.sendMessage(msg);
    }

    @SuppressLint("SimpleDateFormat")
    private void saveCache(List<Map<String, Object>> result) {
        if (null != result) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
            String curDatestr = formatter.format(curDate);
            for (int i = result.size() - 1; i >= 0; i--) {
                HashMap<String, Object> map = (HashMap<String, Object>) result.get(i);
                Cursor c2 = db.rawQuery("SELECT * FROM hlunreadmsg WHERE notifyObjectId = ? and sid=?",
                        new String[]{(String) map.get("notifyObjectId"), sid});
                if (!c2.moveToNext()) {
                    db.execSQL("INSERT INTO hlunreadmsg VALUES (NULL, ?, ?,?, ?,?, ?,?, ?,?, ?,?,?,?,?)",
                            new Object[]{map.get("notifyObjectId"), map.get("unreadObjectId"), map.get("type"),
                                    map.get("senderSid"), map.get("senderUsername"), map.get("userface"), map.get("replyContent"),
                                    map.get("datetime"), map.get("client"), map.get("sourceContent"), map.get("sourceObjectId"),
                                    curDatestr, map.get("noticeType"), sid});
                }
            }
        }
    }

    /**
     * 初始化加载服务端数据
     */
    private void initLoaded() {
        new RoomNewMsgTask(this, new IRoomBack() {
            @Override
            public void doPost(List<Map<String, Object>> result, NewMessageBoards msg) {
                removeDialog(1);
                if (null != result) {
                    if (!result.isEmpty()) {
                        blankLayout.setVisibility(View.GONE);
                        filterCacheByResult(result);
                        // mWeiboList.addAll(0, result);
                        mAdapter.notifyDataSetChanged();
                        isEmpty = false;
                        invalidateOptionsMenu();
                    } else {
                        if (!hasCache) {
                            blankLayout.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    if (!hasCache) {
                        blankLayout.setVisibility(View.VISIBLE);
                        nowifiIv.setImageResource(R.drawable.wifi);
                        blankTv.setText(getString(R.string.no_net_connected));
                    }
                }
                if (null != db) {
                    saveCache(result);
                }
            }

            @Override
            public void onPre() {
            }
        }).executeOnExecutor(Executors.newCachedThreadPool(), getRenheApplication().getUserInfo().getAdSId(),
                getRenheApplication().getUserInfo().getSid());
    }

    private void test() {
        removeDialog(1);
        //		fadeUitl.removeFade(rootRl);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("type", "1");
        map.put("unreadObjectId", "");
        map.put("sourceObjectId", "");
        map.put("avatar", R.drawable.avatar);
        map.put("userface", "");
        map.put("senderSid", "38");
        map.put("senderUsername", "just sender");
        map.put("datetime", "2014-5-14");
        map.put("replyContent", "good![左哼哼]");
        map.put("client", "来自" + "Android客户端");
        map.put("sourceContent", "红红火火[嘻嘻]");
        map.put("sourceReplyNum", 3);
        map.put("favourNumber", 1);
        map.put("isFavour", true);
        mWeiboList.add(map);
        map = new HashMap<String, Object>();
        map.put("type", "2");
        map.put("unreadObjectId", "");
        map.put("sourceObjectId", "");
        map.put("avatar", R.drawable.avatar);
        map.put("userface", "");
        map.put("senderSid", "38");
        map.put("senderUsername", "just sender");
        map.put("datetime", "2014-5-15");
        map.put("replyContent", "这个赞不显示");
        map.put("client", "来自" + "Android客户端");
        map.put("sourceContent", "被赞内容");
        map.put("sourceReplyNum", 3);
        map.put("favourNumber", 1);
        map.put("isFavour", true);
        mWeiboList.add(map);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 1:
                return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.loading).cancelable(false).build();
            default:
                return null;
        }
    }

    @Override
    protected void initListener() {
        super.initListener();
        // 监听留言列表单击事件
        mWeiboListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mWeiboList.size() > (position)) {
                    String objectId = (String) mWeiboList.get(position).get("sourceObjectId");
                    String sid = getRenheApplication().getUserInfo().getSid();
                    Bundle bundle = new Bundle();
                    bundle.putString("sid", sid);
                    bundle.putInt("type", (Integer) mWeiboList.get(position).get("noticeType"));
                    bundle.putString("objectId", objectId);
                    bundle.putBoolean("isFromNoticeList", true);
                    bundle.putInt("loadType", TwitterShowMessageBoardActivity.LOAD_TYPE_FROM_NOTICE);
                    startActivity(TwitterShowMessageBoardActivity.class, bundle);
                }
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        mWeiboList = new ArrayList<Map<String, Object>>();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != messageBoardsList) {
            messageBoardsList.clear();
        }
        if (null != mWeiboList) {
            mWeiboList.clear();
        }
        if (null != db) {
            // 关闭当前数据库
            db.close();
        }
    }

}
