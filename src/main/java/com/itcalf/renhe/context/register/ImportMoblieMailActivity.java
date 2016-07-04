package com.itcalf.renhe.context.register;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.controller.GrpcController;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.utils.CheckPermissionUtil;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.Button;
import com.orhanobut.logger.Logger;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.renhe.heliao.idl.contact.ImportContact;

/**
 * 新注册用户，导入通讯录引导
 * Created by Chong on 2015/7/21.
 */
public class ImportMoblieMailActivity extends BaseActivity {
    private Button importBtn;
    private LinearLayout requestDialog;
    /**
     * 手机通讯录获取值
     **/
    private AsyncQueryHandler asyncQuery;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private String lastReadTime;
    private static final int ID_TASK_PUSH_MOBILELIST = TaskManager.getTaskId();//将本地通讯录导入到服务端

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_maillist_guide);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbarTitleTv = (TextView) findViewById(R.id.toolbar_title_tv);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        findView();
        initData();
        initListener();
        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
        }
        findView();
        initListener();
        asyncQuery = new MyAsyncQueryHandler(this.getContentResolver());
        sp = getSharedPreferences("last_upload_mobile_time" + RenheApplication.getInstance().getUserInfo().getSid(), 0);
        editor = sp.edit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("注册成功——导入通讯录"); //统计页面
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("注册成功——导入通讯录"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
    }

    @Override
    protected void findView() {
        super.findView();
        setTextValue("导入通讯录");
        importBtn = (Button) findViewById(R.id.import_btn);
        requestDialog = (LinearLayout) findViewById(R.id.loading_dig);
        TextView textview = (TextView) requestDialog.findViewById(R.id.textview);
        textview.setText("正在导入...");
    }

    @Override
    protected void initListener() {
        super.initListener();
        importBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //先去检查权限
                if (CheckPermissionUtil.isHasMailListPermission(RenheApplication.getInstance())
                        && CheckPermissionUtil.isAllowedMailListPermission(RenheApplication.getInstance())) {
                    editor.putBoolean("isAuthImport", true);
                    editor.commit();
                    //上传通讯录
                    Uri uri = Uri.parse("content://com.android.contacts/data/phones");
                    String[] projection = {"_id", "raw_contact_id", "display_name", "data1", "sort_key"};
                    asyncQuery.startQuery(0, null, uri, projection, null, null,
                            "sort_key COLLATE LOCALIZED asc");
                } else {
                    MaterialDialogsUtil materialDialog = new MaterialDialogsUtil(ImportMoblieMailActivity.this);
                    materialDialog.showStacked(R.string.no_permission_tip, R.string.contactspermission_guide, R.string.set_permission,
                            R.string.cancel_permission).callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            Intent intent = new Intent(Settings.ACTION_SETTINGS);
                            startActivity(intent);
                            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            super.onNegative(dialog);
                        }
                    });
                    materialDialog.show();
                }
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem skipItem = menu.findItem(R.id.item_skip);
        skipItem.setVisible(true);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_skip:
                //记下是否跳过导入
                editor.putBoolean("isAuthImport", false);
                editor.commit();
                Intent intent = new Intent(ImportMoblieMailActivity.this, RegisterProvideGetInfo.class);
                startHlActivity(intent);
//                Intent intent = new Intent(this, TabMainFragmentActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
//                finish();
//                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            //记下是否跳过导入
            editor.putBoolean("isAuthImport", false);
            editor.commit();
            Intent intent = new Intent(ImportMoblieMailActivity.this, RegisterProvideGetInfo.class);
            startHlActivity(intent);
//            Intent intent = new Intent(this, TabMainFragmentActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(intent);
//            finish();
//            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 上传本地手机通讯录的好友
     */
    private void pushMobileContactsList(List<ImportContact.ContactItem> contactItemList) {
        if (TaskManager.getInstance().exist(ID_TASK_PUSH_MOBILELIST)) {
            return;
        }
        requestDialog.setVisibility(View.VISIBLE);
        TaskManager.getInstance().addTask(this, ID_TASK_PUSH_MOBILELIST);
        GrpcController grpcController = new GrpcController();
        grpcController.pushMobileContactsList(ID_TASK_PUSH_MOBILELIST, contactItemList);
    }

    @Override
    public void onSuccess(int type, Object result) {
        super.onSuccess(type, result);
        requestDialog.setVisibility(View.GONE);
        if (null != result) {
            if (result instanceof ImportContact.ImportContactResponse) {
                //存下时间
                editor.putString("lastUpdateTime", lastReadTime);
                editor.commit();
            }
        }
        Intent intent = new Intent(ImportMoblieMailActivity.this, RegisterProvideGetInfo.class);
        startHlActivity(intent);
    }

    @Override
    public void onFailure(int type, String msg) {
        super.onFailure(type, msg);
        requestDialog.setVisibility(View.GONE);
        ToastUtil.showErrorToast(ImportMoblieMailActivity.this, "通讯录同步失败");
        Intent intent = new Intent(ImportMoblieMailActivity.this, RegisterProvideGetInfo.class);
        startHlActivity(intent);
    }

    /**
     * 查找手机通讯录
     */
    private class MyAsyncQueryHandler extends AsyncQueryHandler {

        ContentResolver contentResolver;
        List<ImportContact.ContactItem> list;

        public MyAsyncQueryHandler(ContentResolver cr) {
            super(cr);
            this.contentResolver = cr;
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if (cursor != null && cursor.getCount() > 0) {
                /***只传id ， 名字和号码**/
                list = new ArrayList<>();
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    ImportContact.ContactItem.Builder builder = ImportContact.ContactItem.newBuilder();
                    builder.setId(cursor.getString(1));
                    builder.setName(cursor.getString(2));
                    String[] numbers = new String[]{cursor.getString(3)};
                    for (String mobile : numbers) {
                        if (!TextUtils.isEmpty(mobile))
                            builder.addMobile(mobile);
                    }
                    ImportContact.ContactItem contactItem = builder.build();
                    list.add(contactItem);
//                    ContactUpLoadBean cv = new ContactUpLoadBean();
//                    cursor.moveToPosition(i);
//                    String[] numbers = new String[]{cursor.getString(3)};
//                    cv.setId(cursor.getString(1));
//                    cv.setName(cursor.getString(2));
//                    cv.setMobiles(numbers);
//                    list.add(cv);
                }
                lastReadTime = cursor.getString(4);
                //合并相同的id电话号码；
                for (int i = 0; i < list.size(); i++) {
                    for (int j = list.size() - 1; j > i; j--) {
                        if (list.get(j).getId().equals(list.get(i).getId())) {
                            ImportContact.ContactItem.Builder builder = list.get(i).toBuilder();
                            List<String> jMobileList = list.get(j).toBuilder().getMobileList();
                            for (String jMobile : jMobileList) {
                                if (!TextUtils.isEmpty(jMobile))
                                    builder.addMobile(jMobile);
                            }
//                            String[] numbers = list.get(i).getMobiles();
//                            String[] numbers2 = list.get(j).getMobiles();
//                            list.get(i).setMobiles(ArrayUtils.addAll(numbers, numbers2));
                            list.remove(j);
                        }
                    }
                }
                cursor.close();
                if (null != list && list.size() > 0) {
                    pushMobileContactsList(list);
                }
//                new ImportMoblieMailTask().executeOnExecutor(Executors.newCachedThreadPool(),
//                        RenheApplication.getInstance().getUserInfo().getSid(),
//                        RenheApplication.getInstance().getUserInfo().getAdSId(), mobilelist);
            } else {
                ToastUtil.showToast(ImportMoblieMailActivity.this, R.string.empty_contacts);
                Intent intent = new Intent(ImportMoblieMailActivity.this, RegisterProvideGetInfo.class);
                startHlActivity(intent);
//                startActivity(new Intent(ImportMoblieMailActivity.this, TabMainFragmentActivity.class));
//                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        }
    }

    class ImportMoblieMailTask extends AsyncTask<String, Void, MessageBoardOperation> {
        //		int i = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            requestDialog.setVisibility(View.VISIBLE);
        }

        @Override
        protected MessageBoardOperation doInBackground(String... params) {
            try {
                Map<String, Object> reqParams = new HashMap<String, Object>();
                reqParams.put("sid", params[0]);
                reqParams.put("adSId", params[1]);
                reqParams.put("content", params[2]);
                reqParams.put("deviceType", 0);
                return (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.IMPORT_MOBILE_CONTACTS, reqParams,
                        MessageBoardOperation.class, null);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(MessageBoardOperation result) {
            super.onPostExecute(result);
            requestDialog.setVisibility(View.GONE);
            if (null != result) {
                if (1 == result.getState()) {
                    //存下时间
                    editor.putString("lastUpdateTime", lastReadTime);
                    editor.commit();
                } else {
                    ToastUtil.showErrorToast(ImportMoblieMailActivity.this, "通讯录同步失败");
                }
                Intent intent = new Intent(ImportMoblieMailActivity.this, RegisterProvideGetInfo.class);
                startHlActivity(intent);
//                Intent intent = new Intent(ImportMoblieMailActivity.this, TabMainFragmentActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
//                finish();
//                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            } else {
                ToastUtil.showNetworkError(ImportMoblieMailActivity.this);
            }
        }
    }

}
