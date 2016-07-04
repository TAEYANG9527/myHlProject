package com.itcalf.renhe.context.archives.edit;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.archives.edit.task.EditSummaryInfoProvideAndGetTask;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.dto.Profile;
import com.itcalf.renhe.dto.Profile.UserInfo.AimTagInfo;
import com.itcalf.renhe.dto.Profile.UserInfo.PreferredTagInfo;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.widget.FlowLayout;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Title: EditSelfInfo.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-7-14 下午4:38:14 <br>
 *
 * @author wangning
 */
public class EditProvideGetInfo extends BaseActivity {
    private FlowLayout canProvideGroup;
    private Button addProvideIB;
    private FlowLayout wantGetGroup;
    private Button wantGetIB;

    private final static int CAN_PROVIDE_COUNT = 4;
    private final static int WANT_GET_COUNT = 4;
    private final static int REMOVE_PROVIDE = 0;
    private final static int REMOVE_GET = 1;
    private int provedeCount = 0;
    private int getCount = 0;

    private RelativeLayout provideRl;
    private RelativeLayout getRl;
    private boolean isModify = false;

    private Profile.UserInfo.AimTagInfo[] aimTagInfo;
    private Profile.UserInfo.PreferredTagInfo[] preferredTagInfos;
    private Profile pf;
    private static final int WANT_GET = 0;
    private static final int CAN_PROVIDE = 1;
    private View wantGetInfoButton;
    private View canProvideInfoButton;
    private Dialog mAlertDialog;
    private TextView skipTv;

    //判断是否来自人脉完善
    private boolean fromPerfectInfo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RenheApplication.getInstance().addActivity(this);
        new ActivityTemplate().doInActivity(this, R.layout.register_provide_getinfo);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("编辑供求信息"); //统计页面
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("编辑供求信息"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
    }

    @Override
    protected void findView() {
        super.findView();
        setTextValue(R.id.title_txt, "定制我的商机");
        canProvideGroup = (FlowLayout) findViewById(R.id.can_provide_group);
        addProvideIB = (Button) findViewById(R.id.add_can_provide_ib);
        wantGetGroup = (FlowLayout) findViewById(R.id.want_get_group);
        wantGetIB = (Button) findViewById(R.id.add_want_get_ib);
        provideRl = (RelativeLayout) findViewById(R.id.can_provide_rl);
        getRl = (RelativeLayout) findViewById(R.id.want_get_rl);
        skipTv = (TextView) findViewById(R.id.skip);
        skipTv.setVisibility(View.GONE);
        canProvideGroup.removeAllViews();
        wantGetGroup.removeAllViews();
        createCanProvideButton();
        createWantGetButton();

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem saveItem = menu.findItem(R.id.item_save);
        saveItem.setVisible(true);
        saveItem.setTitle("保存");
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_save) {
            editMemberPreferredAimTags();
        } else if (item.getItemId() == android.R.id.home) {
            goBack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createWantGetButton() {
        if (getCount < WANT_GET_COUNT) {
            if (null == wantGetInfoButton) {
                wantGetInfoButton = LayoutInflater.from(EditProvideGetInfo.this).inflate(R.layout.regist_canprovide_info_item,
                        null);
                wantGetGroup.addView(wantGetInfoButton, wantGetGroup.getChildCount());
                Button item = (Button) wantGetInfoButton.findViewById(R.id.item);
                item.setText("+添加");
                item.setBackgroundResource(R.drawable.archive_provide_get_item_shape);
                Resources resource = getBaseContext().getResources();
                ColorStateList csl = resource.getColorStateList(R.color.add_btn_textcolor_selector);
                item.setTextColor(csl);
                item.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        showEditTextDialog(WANT_GET, R.string.please_input, R.string.want_get);
                    }
                });
                getCount++;
            }
        }
        if (getCount == WANT_GET_COUNT) {
            wantGetIB.setVisibility(View.GONE);
        }
    }

    private void mcreateWantGetButton() {
        if (getCount < WANT_GET_COUNT) {
            if (null == wantGetInfoButton) {
                wantGetInfoButton = LayoutInflater.from(EditProvideGetInfo.this).inflate(R.layout.regist_canprovide_info_item,
                        null);
                wantGetGroup.addView(wantGetInfoButton, wantGetGroup.getChildCount());
                Button item = (Button) wantGetInfoButton.findViewById(R.id.item);
                item.setText("+添加");
                item.setBackgroundResource(R.drawable.archive_provide_get_item_shape);
                Resources resource = getBaseContext().getResources();
                ColorStateList csl = resource.getColorStateList(R.color.add_btn_textcolor_selector);
                item.setTextColor(csl);
                item.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        showEditTextDialog(WANT_GET, R.string.please_input, R.string.want_get);
                    }
                });
                //				getCount++;
            }
        }
        if (getCount == WANT_GET_COUNT) {
            wantGetIB.setVisibility(View.GONE);
        }
    }

    private void createCanProvideButton() {
        if (provedeCount < CAN_PROVIDE_COUNT) {
            if (null == canProvideInfoButton) {
                canProvideInfoButton = LayoutInflater.from(EditProvideGetInfo.this).inflate(R.layout.regist_canprovide_info_item,
                        null);
                Button item = (Button) canProvideInfoButton.findViewById(R.id.item);
                item.setText("+添加");
                item.setBackgroundResource(R.drawable.archive_provide_get_item_shape);
                Resources resource = getBaseContext().getResources();
                ColorStateList csl = resource.getColorStateList(R.color.add_btn_textcolor_selector);
                item.setTextColor(csl);
                item.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        showEditTextDialog(CAN_PROVIDE, R.string.please_input, R.string.can_provide);
                    }
                });
                canProvideGroup.addView(canProvideInfoButton, canProvideGroup.getChildCount());
                provedeCount++;
            }
        }
        if (provedeCount == CAN_PROVIDE_COUNT) {
            addProvideIB.setVisibility(View.GONE);
        }
    }

    private void mcreateCanProvideButton() {
        if (provedeCount < CAN_PROVIDE_COUNT) {
            if (null == canProvideInfoButton) {
                canProvideInfoButton = LayoutInflater.from(EditProvideGetInfo.this).inflate(R.layout.regist_canprovide_info_item,
                        null);
                Button item = (Button) canProvideInfoButton.findViewById(R.id.item);
                item.setText("+添加");
                item.setBackgroundResource(R.drawable.archive_provide_get_item_shape);
                Resources resource = getBaseContext().getResources();
                ColorStateList csl = resource.getColorStateList(R.color.add_btn_textcolor_selector);
                item.setTextColor(csl);
                item.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        showEditTextDialog(CAN_PROVIDE, R.string.please_input, R.string.can_provide);
                    }
                });
                canProvideGroup.addView(canProvideInfoButton, canProvideGroup.getChildCount());
            }
        }
        if (provedeCount == CAN_PROVIDE_COUNT) {
            addProvideIB.setVisibility(View.GONE);
        }
    }

    @Override
    protected void initData() {
        super.initData();

        provideRl.setVisibility(View.VISIBLE);
        getRl.setVisibility(View.VISIBLE);

        fromPerfectInfo = getIntent().getBooleanExtra("fromPerfectInfo", false);
        if (getIntent().getSerializableExtra("Profile") != null) {
            pf = (Profile) getIntent().getSerializableExtra("Profile");
        }
        preferredTagInfos = pf.getUserInfo().getPreferredTagInfo();
        aimTagInfo = pf.getUserInfo().getAimTagInfo();

        if (null != preferredTagInfos) {
            for (int i = 0; i < preferredTagInfos.length; i++) {
                if (!TextUtils.isEmpty(preferredTagInfos[i].getTitle())) {
                    if (provedeCount < CAN_PROVIDE_COUNT) {
                        View canProvideInfoView = LayoutInflater.from(EditProvideGetInfo.this)
                                .inflate(R.layout.regist_canprovide_info_item, null);

                        Button item = (Button) canProvideInfoView.findViewById(R.id.item);
                        item.setText(preferredTagInfos[i].getTitle());
                        item.setOnClickListener(new RemoveItemListener(canProvideGroup, canProvideInfoView, REMOVE_PROVIDE));
                        canProvideGroup.addView(canProvideInfoView, canProvideGroup.getChildCount() - 1);
                        provedeCount++;
                    }
                    if (provedeCount == CAN_PROVIDE_COUNT) {
                        addProvideIB.setVisibility(View.GONE);
                        if (null != canProvideGroup && null != canProvideInfoButton) {
                            canProvideGroup.removeView(canProvideInfoButton);
                            canProvideInfoButton = null;
                        }
                    }
                }
            }
        }
        if (null != aimTagInfo) {
            for (int i = 0; i < aimTagInfo.length; i++) {
                if (!TextUtils.isEmpty(aimTagInfo[i].getTitle())) {
                    if (getCount < WANT_GET_COUNT) {
                        View wantGetInfoView = LayoutInflater.from(EditProvideGetInfo.this)
                                .inflate(R.layout.regist_canprovide_info_item, null);
                        wantGetGroup.addView(wantGetInfoView, wantGetGroup.getChildCount() - 1);
                        Button item = (Button) wantGetInfoView.findViewById(R.id.item);
                        item.setText(aimTagInfo[i].getTitle());
                        item.setOnClickListener(new RemoveItemListener(wantGetGroup, wantGetInfoView, REMOVE_GET));
                        getCount++;
                    }
                    if (getCount == WANT_GET_COUNT) {
                        if (null != wantGetInfoButton && null != wantGetInfoButton) {
                            wantGetGroup.removeView(wantGetInfoButton);
                            wantGetInfoButton = null;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void initListener() {
        super.initListener();
    }

    private void editMemberPreferredAimTags() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(skipTv.getWindowToken(), 0);
        boolean isProvideNull = true;
        boolean isGetNull = true;
        String[] prefreStrings = null;
        String[] aimStrings = null;
        //初始化我想得到
        List<String> tempList = new ArrayList<>();
        if (null != canProvideGroup) {
            if (provedeCount >= CAN_PROVIDE_COUNT) {
                for (int i = 0; i < CAN_PROVIDE_COUNT - 1; i++) {
                    String s = ((Button) canProvideGroup.getChildAt(i).findViewById(R.id.item)).getText().toString().trim();
                    if (!TextUtils.isEmpty(s)) {
                        isProvideNull = false;
                        tempList.add(s);
                    }
                }
            } else {
                for (int i = 0; i < provedeCount - 1; i++) {
                    String s = ((Button) canProvideGroup.getChildAt(i).findViewById(R.id.item)).getText().toString().trim();
                    if (!TextUtils.isEmpty(s)) {
                        isProvideNull = false;
                        tempList.add(s);
                    }
                }
            }
        }
        if (isProvideNull) {
            prefreStrings = new String[0];
        } else {
            prefreStrings = new String[tempList.size()];
            for (int i = 0; i < prefreStrings.length; i++) {
                if (!TextUtils.isEmpty(tempList.get(i))) {
                    prefreStrings[i] = tempList.get(i);
                }
            }
        }
        //初始化我能提供
        List<String> tempAimList = new ArrayList<>();
        if (null != wantGetGroup) {
            if (getCount >= WANT_GET_COUNT) {
                for (int i = 0; i < WANT_GET_COUNT - 1; i++) {
                    String s = ((Button) wantGetGroup.getChildAt(i).findViewById(R.id.item)).getText().toString().trim();
                    if (!TextUtils.isEmpty(s)) {
                        isGetNull = false;
                        tempAimList.add(s);
                    }
                }
            } else {
                for (int i = 0; i < getCount - 1; i++) {
                    String s = ((Button) wantGetGroup.getChildAt(i).findViewById(R.id.item)).getText().toString().trim();
                    if (!TextUtils.isEmpty(s)) {
                        isGetNull = false;
                        tempAimList.add(s);
                    }
                }
            }
        }
        if (isGetNull) {
            aimStrings = new String[0];
        } else {
            aimStrings = new String[tempAimList.size()];
            for (int i = 0; i < aimStrings.length; i++) {
                if (!TextUtils.isEmpty(tempAimList.get(i))) {
                    aimStrings[i] = tempAimList.get(i);
                }
            }
        }

        PreferredTagInfo[] mpreferredTagInfos = null;
        if (tempList.size() > 0) {
            mpreferredTagInfos = new PreferredTagInfo[tempList.size()];
            for (int i = 0; i < tempList.size(); i++) {
                PreferredTagInfo pInfo = new PreferredTagInfo();
                pInfo.setTitle(tempList.get(i));
                mpreferredTagInfos[i] = pInfo;
            }
        }
        AimTagInfo[] mAimTagInfos = null;
        mAimTagInfos = new AimTagInfo[tempAimList.size()];
        for (int i = 0; i < tempAimList.size(); i++) {
            AimTagInfo aInfo = new AimTagInfo();
            aInfo.setTitle(tempAimList.get(i));
            mAimTagInfos[i] = aInfo;
        }
        String[] preStrings = new String[0];
        if (null != mAimTagInfos) {
            preStrings = new String[mAimTagInfos.length];
            for (int i = 0; i < preStrings.length; i++) {
                if (!TextUtils.isEmpty(mAimTagInfos[i].getTitle())) {
                    preStrings[i] = mAimTagInfos[i].getTitle();
                }
            }
        }
        //来自资料完善
        if (fromPerfectInfo) {
            Intent intent = new Intent();
            intent.putExtra("prefreStrings", prefreStrings);
            intent.putExtra("preStrings", preStrings);
            setResult(RESULT_OK, intent);
            finish();
            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
        } else {
            new EditSummaryInfoProvideAndGetTask(EditProvideGetInfo.this, prefreStrings, preStrings) {

                @Override
                public void doPre() {
                    super.doPre();
                    showDialog(1);
                }

                @Override
                public void doPost(MessageBoardOperation result) {
                    super.doPost(result);
                    if (result == null) {
                        removeDialog(0);
                        ToastUtil.showToast(EditProvideGetInfo.this, getResources().getString(R.string.network_anomaly));
                    } else if (result.getState() == 1) {
                        new ProfileTask().executeOnExecutor(Executors.newCachedThreadPool(),
                                getRenheApplication().getUserInfo().getSid(), getRenheApplication().getUserInfo().getSid(),
                                getRenheApplication().getUserInfo().getAdSId());
                    } else if (result.getState() == -3) {
                        removeDialog(0);
                        ToastUtil.showToast(EditProvideGetInfo.this,
                                getResources().getString(R.string.editprovide_single_length_limit));
                    } else if (result.getState() == -4) {
                        removeDialog(0);
                        ToastUtil.showToast(EditProvideGetInfo.this,
                                getResources().getString(R.string.editprovide_single_numb_limit));
                    } else if (result.getState() == -5) {
                        removeDialog(0);
                        ToastUtil.showToast(EditProvideGetInfo.this,
                                getResources().getString(R.string.editget_single_length_limit));
                    } else if (result.getState() == -6) {
                        removeDialog(0);
                        ToastUtil.showToast(EditProvideGetInfo.this,
                                getResources().getString(R.string.editget_single_numb_limit));
                    } else {
                        removeDialog(0);
                        ToastUtil.showToast(EditProvideGetInfo.this, getResources().getString(R.string.unknown_error));
                    }
                }
            }.execute(getRenheApplication().getUserInfo().getSid(), getRenheApplication().getUserInfo().getAdSId());
        }
    }

    private void showEditTextDialog(final int flag, int title, int message) {
        MaterialDialogsUtil materialDialog = new MaterialDialogsUtil(this);
        materialDialog.getBuilder(title, message)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME
                        | InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                .inputMaxLength(Constants.PROVIDE_WANT).cancelable(false).input(0, 0, true, new MaterialDialog.InputCallback() {
            @Override
            public void onInput(MaterialDialog dialog, CharSequence input) {
                String name = input.toString();
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(name.trim())) {
                    ToastUtil.showToast(EditProvideGetInfo.this,
                            getResources().getString(R.string.editprovideget_content_unempty));
                    return;
                }
                if (name.length() > 10) {
                    ToastUtil.showToast(EditProvideGetInfo.this,
                            getResources().getString(R.string.editprovideget_content_limit));
                    return;
                }
                dialog.dismiss();

                if (flag == WANT_GET) {//我想得到
                    if (getCount < WANT_GET_COUNT) {
                        View wantGetInfoView = LayoutInflater.from(EditProvideGetInfo.this)
                                .inflate(R.layout.regist_canprovide_info_item, null);
                        wantGetGroup.addView(wantGetInfoView, wantGetGroup.getChildCount() - 1);
                        Button item = (Button) wantGetInfoView.findViewById(R.id.item);
                        item.setText(name);
                        item.setOnClickListener(new RemoveItemListener(wantGetGroup, wantGetInfoView, REMOVE_GET));
                        getCount++;
                        isModify = true;
                    }
                    if (getCount == WANT_GET_COUNT) {
                        if (null != wantGetInfoButton && null != wantGetInfoButton) {
                            wantGetGroup.removeView(wantGetInfoButton);
                            wantGetInfoButton = null;
                        }
                    }
                } else if (flag == CAN_PROVIDE) {//我能提供
                    if (provedeCount < CAN_PROVIDE_COUNT) {
                        View canProvideInfoView = LayoutInflater.from(EditProvideGetInfo.this)
                                .inflate(R.layout.regist_canprovide_info_item, null);

                        Button item = (Button) canProvideInfoView.findViewById(R.id.item);
                        item.setText(name);
                        item.setOnClickListener(
                                new RemoveItemListener(canProvideGroup, canProvideInfoView, REMOVE_PROVIDE));
                        canProvideGroup.addView(canProvideInfoView, canProvideGroup.getChildCount() - 1);
                        provedeCount++;
                        isModify = true;
                    }
                    if (provedeCount == CAN_PROVIDE_COUNT) {
                        if (null != canProvideGroup && null != canProvideInfoButton) {
                            canProvideGroup.removeView(canProvideInfoButton);
                            canProvideInfoButton = null;
                        }
                    }
                }
            }
        });
        materialDialog.show();
    }

    public void goBack() {
        if (isModify) {
            MaterialDialogsUtil materialDialog = new MaterialDialogsUtil(this);
            materialDialog
                    .getBuilder(R.string.material_dialog_title, R.string.is_save, R.string.material_dialog_sure,
                            R.string.material_dialog_cancel, R.string.material_dialog_give_up)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onNeutral(MaterialDialog dialog) {
                            finish();
                            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                        }

                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            editMemberPreferredAimTags();
                        }
                    });
            materialDialog.show();
        } else {
            finish();
            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
        }
    }

    class RemoveItemListener implements OnClickListener {
        FlowLayout groupView;
        View itemView;
        int type;

        public RemoveItemListener(FlowLayout groupView, View itemView, int type) {
            this.groupView = groupView;
            this.itemView = itemView;
            this.type = type;
        }

        @Override
        public void onClick(View arg0) {
            isModify = true;
            createDialog(EditProvideGetInfo.this, groupView, itemView, type);
        }

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 1:
                return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.saving).cancelable(false).build();
            default:
                return null;
        }
    }

    public void createDialog(Context context, final FlowLayout groupView, final View mItemView, final int type) {
        MaterialDialogsUtil materialDialog = new MaterialDialogsUtil(context);
        materialDialog.showSelectList(R.array.delete_item).itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                switch (which) {
                    case 0:
                        if (null != groupView) {
                            groupView.removeView(mItemView);
                        }
                        switch (type) {
                            case REMOVE_PROVIDE:
                                provedeCount--;
                                if (provedeCount < CAN_PROVIDE_COUNT) {
                                    mcreateCanProvideButton();
                                }
                                break;
                            case REMOVE_GET:
                                getCount--;
                                if (getCount < WANT_GET_COUNT) {
                                    mcreateWantGetButton();
                                }
                                break;
                            default:
                                break;
                        }
                        break;
                    case 1:
                        break;
                    default:
                        break;
                }
            }
        });
        materialDialog.show();
    }

    class ProfileTask extends AsyncTask<String, Void, Profile> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Profile doInBackground(String... params) {
            try {
                return getRenheApplication().getProfileCommand().showProfile(params[0], params[1], params[2],
                        EditProvideGetInfo.this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Profile result) {
            super.onPostExecute(result);
            removeDialog(1);
            if (null != result) {
                if (1 == result.getState() && null != result.getUserInfo()) {
                    Intent brocastIntent = new Intent(MyHomeArchivesActivity.REFRESH_ARCHIEVE_RECEIVER_ACTION);
                    brocastIntent.putExtra("Profile", result);
                    sendBroadcast(brocastIntent);
                    Intent intent = new Intent();
                    intent.putExtra("Profile", result);
                    setResult(RESULT_OK, intent);
                    finish();
                    overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                }
            } else {
                ToastUtil.showNetworkError(EditProvideGetInfo.this);
            }
        }
    }

    @Override
    public void onBackPressed() {
        goBack();
    }
}
