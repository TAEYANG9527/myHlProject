package com.itcalf.renhe.context.more;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.bean.AccountLimitIncrease;
import com.itcalf.renhe.bean.AccountLimitIncrease.AccountLimitIncreaseResult;
import com.itcalf.renhe.bean.AccountLimitIncrease.AccountLimitIncreaseResult.IncreaseAddition;
import com.itcalf.renhe.context.archives.EditMyHomeArchivesActivity;
import com.itcalf.renhe.context.auth.NameAuthActivity;
import com.itcalf.renhe.context.contacts.MobileMailList;
import com.itcalf.renhe.context.fragmentMain.TabMainFragmentActivity;
import com.itcalf.renhe.context.register.BindPhoneGuideActivity;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.utils.FadeUitl;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.NetworkUtil;
import com.itcalf.renhe.utils.ToastUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * @author Chan
 * @description 限额升级
 * @date 2015-6-18
 */
public class AccountLimitUpgradeActivity extends BaseActivity {

    //绑定手机号码
    private LinearLayout boundPhoneNumb_Ll;
    private RelativeLayout boundPhoneNumb_Rl;
    private TextView bound_sendInvite_original_txt, bound_sendInvite_upgrade_txt;
    private TextView bound_contacts_original_txt, bound_contacts_upgrade_txt;
    private TextView bound_search_original_txt, bound_search_upgrade_txt;
    //导入通讯录
    private LinearLayout importMaillist_Ll;
    private RelativeLayout importMaillist_Rl;
    private TextView import_sendInvite_original_txt, import_sendInvite_upgrade_txt;
    private TextView import_contacts_original_txt, import_contacts_upgrade_txt;
    private TextView import_search_original_txt, import_search_upgrade_txt;
    //实名认证
    private LinearLayout realNameAuthentication_Ll;
    private RelativeLayout realNameAuthentication_Rl;
    private TextView auth_sendInvite_original_txt, auth_sendInvite_upgrade_txt;
    private TextView auth_contacts_original_txt, auth_contacts_upgrade_txt;
    private TextView auth_search_original_txt, auth_search_upgrade_txt;
    //邀请好友注册
    private LinearLayout inviteFriendsRegister_Ll;
    private RelativeLayout inviteFriendsRegister_Rl;
    private TextView invite_contacts_upgrade_txt;
    private TextView invite_search_upgrade_txt;

    //完善资料
    private LinearLayout completeMemberDataNumb_Ll;
    private RelativeLayout completeMemberDataNumb_Rl;
    private TextView completeMemberData_sendInvite_original_txt, completeMemberData_sendInvite_upgrade_txt;
    private TextView completeMemberData_contacts_original_txt, completeMemberData_contacts_upgrade_txt;
    private TextView completeMemberData_search_original_txt, completeMemberData_search_upgrade_txt;

    //上传头像
    private LinearLayout approvedUserHeadNumb_Ll;
    private RelativeLayout approvedUserHeadNumb_Rl;
    private TextView approvedUserHead_sendInvite_original_txt, approvedUserHead_sendInvite_upgrade_txt;
    private TextView approvedUserHead_contacts_original_txt, approvedUserHead_contacts_upgrade_txt;
    private TextView approvedUserHead_search_original_txt, approvedUserHead_search_upgrade_txt;

    //审批名片
    private LinearLayout approvedCardsNumb_Ll;
    private RelativeLayout approvedCardsNumb_Rl;
    private TextView approvedCards_sendInvite_original_txt, approvedCards_sendInvite_upgrade_txt;
    private TextView approvedCards_contacts_original_txt, approvedCards_contacts_upgrade_txt;
    private TextView approvedCards_search_original_txt, approvedCards_search_upgrade_txt;

    private RelativeLayout rootRl;
    private FadeUitl fadeUitl;
    /**
     * 需要显示的模块
     **/
    private int showModule;
    //注册广播，接收数据，刷新
    private RefreshAccountLimitReceiver refreshAccountLimitReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RenheApplication.getInstance().addActivity(this);
        new ActivityTemplate().doInActivity(this, R.layout.accountlimit_upgrade);
    }

    @Override
    protected void findView() {
        super.findView();

        rootRl = (RelativeLayout) findViewById(R.id.root_rl);

        boundPhoneNumb_Ll = (LinearLayout) findViewById(R.id.boundPhoneNumb_Ll);
        boundPhoneNumb_Rl = (RelativeLayout) findViewById(R.id.boundPhoneNumb_Rl);
        bound_sendInvite_original_txt = (TextView) findViewById(R.id.bound_sendInvite_original_txt);
        bound_sendInvite_original_txt.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        bound_sendInvite_upgrade_txt = (TextView) findViewById(R.id.bound_sendInvite_upgrade_txt);
        bound_contacts_original_txt = (TextView) findViewById(R.id.bound_contacts_original_txt);
        bound_contacts_original_txt.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        bound_contacts_upgrade_txt = (TextView) findViewById(R.id.bound_contacts_upgrade_txt);
        bound_search_original_txt = (TextView) findViewById(R.id.bound_search_original_txt);
        bound_search_original_txt.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        bound_search_upgrade_txt = (TextView) findViewById(R.id.bound_search_upgrade_txt);

        importMaillist_Ll = (LinearLayout) findViewById(R.id.importMaillist_Ll);
        importMaillist_Rl = (RelativeLayout) findViewById(R.id.importMaillist_Rl);
        import_sendInvite_original_txt = (TextView) findViewById(R.id.import_sendInvite_original_txt);
        import_sendInvite_original_txt.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        import_sendInvite_upgrade_txt = (TextView) findViewById(R.id.import_sendInvite_upgrade_txt);
        import_contacts_original_txt = (TextView) findViewById(R.id.import_contacts_original_txt);
        import_contacts_original_txt.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        import_contacts_upgrade_txt = (TextView) findViewById(R.id.import_contacts_upgrade_txt);
        import_search_original_txt = (TextView) findViewById(R.id.import_search_original_txt);
        import_search_original_txt.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        import_search_upgrade_txt = (TextView) findViewById(R.id.import_search_upgrade_txt);

        realNameAuthentication_Ll = (LinearLayout) findViewById(R.id.realNameAuthentication_Ll);
        realNameAuthentication_Rl = (RelativeLayout) findViewById(R.id.realNameAuthentication_Rl);
        auth_sendInvite_original_txt = (TextView) findViewById(R.id.auth_sendInvite_original_txt);
        auth_sendInvite_original_txt.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        auth_sendInvite_upgrade_txt = (TextView) findViewById(R.id.auth_sendInvite_upgrade_txt);
        auth_contacts_original_txt = (TextView) findViewById(R.id.auth_contacts_original_txt);
        auth_contacts_original_txt.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        auth_contacts_upgrade_txt = (TextView) findViewById(R.id.auth_contacts_upgrade_txt);
        auth_search_original_txt = (TextView) findViewById(R.id.auth_search_original_txt);
        auth_search_original_txt.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        auth_search_upgrade_txt = (TextView) findViewById(R.id.auth_search_upgrade_txt);

        inviteFriendsRegister_Ll = (LinearLayout) findViewById(R.id.inviteFriendsRegister_Ll);
        inviteFriendsRegister_Rl = (RelativeLayout) findViewById(R.id.inviteFriendsRegister_Rl);
        invite_contacts_upgrade_txt = (TextView) findViewById(R.id.invite_contacts_upgrade_txt);
        invite_search_upgrade_txt = (TextView) findViewById(R.id.invite_search_upgrade_txt);

        completeMemberDataNumb_Ll = (LinearLayout) findViewById(R.id.completeMemberData_Ll);
        completeMemberDataNumb_Rl = (RelativeLayout) findViewById(R.id.completeMemberData_Rl);
        completeMemberData_sendInvite_original_txt = (TextView) findViewById(R.id.completeMemberData_sendInvite_original_txt);
        completeMemberData_sendInvite_original_txt.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        completeMemberData_sendInvite_upgrade_txt = (TextView) findViewById(R.id.completeMemberData_sendInvite_upgrade_txt);
        completeMemberData_contacts_original_txt = (TextView) findViewById(R.id.completeMemberData_contacts_original_txt);
        completeMemberData_contacts_original_txt.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        completeMemberData_contacts_upgrade_txt = (TextView) findViewById(R.id.completeMemberData_contacts_upgrade_txt);
        completeMemberData_search_original_txt = (TextView) findViewById(R.id.completeMemberData_search_original_txt);
        completeMemberData_search_original_txt.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        completeMemberData_search_upgrade_txt = (TextView) findViewById(R.id.completeMemberData_search_upgrade_txt);

        approvedUserHeadNumb_Ll = (LinearLayout) findViewById(R.id.approvedUserHead_Ll);
        approvedUserHeadNumb_Rl = (RelativeLayout) findViewById(R.id.approvedUserHead_Rl);
        approvedUserHead_sendInvite_original_txt = (TextView) findViewById(R.id.approvedUserHead_sendInvite_original_txt);
        approvedUserHead_sendInvite_original_txt.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        approvedUserHead_sendInvite_upgrade_txt = (TextView) findViewById(R.id.approvedUserHead_sendInvite_upgrade_txt);
        approvedUserHead_contacts_original_txt = (TextView) findViewById(R.id.approvedUserHead_contacts_original_txt);
        approvedUserHead_contacts_original_txt.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        approvedUserHead_contacts_upgrade_txt = (TextView) findViewById(R.id.approvedUserHead_contacts_upgrade_txt);
        approvedUserHead_search_original_txt = (TextView) findViewById(R.id.approvedUserHead_search_original_txt);
        approvedUserHead_search_original_txt.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        approvedUserHead_search_upgrade_txt = (TextView) findViewById(R.id.approvedUserHead_search_upgrade_txt);

        approvedCardsNumb_Ll = (LinearLayout) findViewById(R.id.approvedCards_Ll);
        approvedCardsNumb_Rl = (RelativeLayout) findViewById(R.id.approvedCards_Rl);
        approvedCards_sendInvite_original_txt = (TextView) findViewById(R.id.approvedCards_sendInvite_original_txt);
        approvedCards_sendInvite_original_txt.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        approvedCards_sendInvite_upgrade_txt = (TextView) findViewById(R.id.approvedCards_sendInvite_upgrade_txt);
        approvedCards_contacts_original_txt = (TextView) findViewById(R.id.approvedCards_contacts_original_txt);
        approvedCards_contacts_original_txt.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        approvedCards_contacts_upgrade_txt = (TextView) findViewById(R.id.approvedCards_contacts_upgrade_txt);
        approvedCards_search_original_txt = (TextView) findViewById(R.id.approvedCards_search_original_txt);
        approvedCards_search_original_txt.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        approvedCards_search_upgrade_txt = (TextView) findViewById(R.id.approvedCards_search_upgrade_txt);

        boundPhoneNumb_Ll.setVisibility(View.GONE);
        importMaillist_Ll.setVisibility(View.GONE);
        realNameAuthentication_Ll.setVisibility(View.GONE);
        inviteFriendsRegister_Ll.setVisibility(View.GONE);
        completeMemberDataNumb_Ll.setVisibility(View.GONE);
        approvedCardsNumb_Ll.setVisibility(View.GONE);
        approvedUserHeadNumb_Ll.setVisibility(View.GONE);
    }

    @Override
    protected void initData() {
        super.initData();
        setTextValue(1, "权限提升");

        fadeUitl = new FadeUitl(this, getResources().getString(R.string.loading));
        fadeUitl.addFade(rootRl);

        refreshAccountLimitReceiver = new RefreshAccountLimitReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BroadCastAction.UPDATE_ACCOUNTLIMIT_ACTION);
        registerReceiver(refreshAccountLimitReceiver, intentFilter);

        Bundle b = this.getIntent().getExtras();
        if (b != null) {
            showModule = b.getInt("update");
            new getAccountLimitIncreaseTask().executeOnExecutor(Executors.newCachedThreadPool(),
                    RenheApplication.getInstance().getUserInfo().getSid(),
                    RenheApplication.getInstance().getUserInfo().getAdSId(), "" + showModule);
        }
    }

    @Override
    protected void initListener() {
        super.initListener();
        //绑定手机
        boundPhoneNumb_Rl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AccountLimitUpgradeActivity.this, BindPhoneGuideActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
        //导入通讯录
        importMaillist_Rl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AccountLimitUpgradeActivity.this, MobileMailList.class);
                startActivity(i);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
        //实名认证
        realNameAuthentication_Rl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isRealName = RenheApplication.getInstance().getUserInfo().isRealName();
                int realNameStatus = 0;
                if (isRealName)
                    realNameStatus = 1;
                else
                    realNameStatus = -1;
                NameAuthActivity.launch(AccountLimitUpgradeActivity.this, realNameStatus);
            }
        });
        //邀请好友注册
        inviteFriendsRegister_Rl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AccountLimitUpgradeActivity.this, MobileMailList.class);
                startActivity(i);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
        //完善资料
        completeMemberDataNumb_Rl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击完善资料选项跳转到更新档案页面
                Intent intent = new Intent(AccountLimitUpgradeActivity.this, EditMyHomeArchivesActivity.class);
                intent.putExtra(EditMyHomeArchivesActivity.FLAG_INTENT_DATA, RenheApplication.getInstance().getUserInfo().getSid());
                intent.putExtra(EditMyHomeArchivesActivity.FLAG_INTENT_USEINFO, EditMyHomeArchivesActivity.FLAG_INTENT_USEINFO);
                startHlActivity(intent);
            }
        });
        //上传头像
        approvedUserHeadNumb_Rl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击完善资料选项跳转到更新档案页面
                Intent intent = new Intent(AccountLimitUpgradeActivity.this, EditMyHomeArchivesActivity.class);
                intent.putExtra(EditMyHomeArchivesActivity.FLAG_INTENT_DATA, RenheApplication.getInstance().getUserInfo().getSid());
                intent.putExtra(EditMyHomeArchivesActivity.FLAG_INTENT_USEINFO, EditMyHomeArchivesActivity.FLAG_INTENT_USEINFO);
                startHlActivity(intent);
            }
        });
        //扫描名片
        approvedCardsNumb_Rl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountLimitUpgradeActivity.this, TabMainFragmentActivity.class);
                intent.putExtra("fromAccountLimit", true);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            }
        });
    }

    /**
     * 获取账户限额信息
     */
    class getAccountLimitIncreaseTask extends AsyncTask<String, Void, AccountLimitIncrease> {

        protected AccountLimitIncrease doInBackground(String... params) {

            Map<String, Object> reqParams = new HashMap<String, Object>();
            reqParams.put("sid", params[0]);
            reqParams.put("adSId", params[1]);

            int type = Integer.parseInt(params[2]);
            String url = "";
            if (type == Constants.ACCOUNTLIMITUPGRADE[0]) {
                url = Constants.Http.ACCOUNTLIMIT_INCREASEADDFRIEND;
            } else if (type == Constants.ACCOUNTLIMITUPGRADE[1]) {
                url = Constants.Http.ACCOUNTLIMIT_INCREASEFRIENDAMOUNT;
            } else if (type == Constants.ACCOUNTLIMITUPGRADE[2]) {
                url = Constants.Http.ACCOUNTLIMIT_INCREASERENMAISEARCHLIST;
            } else if (type == Constants.ACCOUNTLIMITUPGRADE[3]) {
                url = Constants.Http.ACCOUNTLIMIT_INCREASEADVANCEDSEARCH;
            } else if (type == Constants.ACCOUNTLIMITUPGRADE[4]) {
                url = Constants.Http.ACCOUNTLIMIT_INCREASEMEMBERNEARBYFILTER;
            }
            try {
                AccountLimitIncrease al = (AccountLimitIncrease) HttpUtil.doHttpRequest(url, reqParams,
                        AccountLimitIncrease.class, null);
                return al;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (-1 != NetworkUtil.hasNetworkConnection(AccountLimitUpgradeActivity.this)) {
            } else {
                if (fadeUitl != null)
                    fadeUitl.removeFade(rootRl);
                ToastUtil.showNetworkError(AccountLimitUpgradeActivity.this);
            }
        }

        @Override
        protected void onPostExecute(AccountLimitIncrease result) {
            super.onPostExecute(result);
            if (fadeUitl != null)
                fadeUitl.removeFade(rootRl);
            if (result != null) {
                if (result.getState() == 1) {
                    AccountLimitIncreaseResult increaseResult = result.getResult();
                    if (increaseResult != null) {
                        int addFriendPerdayLimit = increaseResult.getAddFriendPerdayLimit();
                        int friendAmountLimit = increaseResult.getFriendAmountLimit();
                        int renmaiSearchListLimit = increaseResult.getRenmaiSearchListLimit();
                        IncreaseAddition bindMobileAddition = increaseResult.getBindMobileAddition();
                        IncreaseAddition importContactAddition = increaseResult.getImportContactAddition();
                        IncreaseAddition validateRealNameAddition = increaseResult.getValidateRealNameAddition();
                        IncreaseAddition invitePerMemberAddition = increaseResult.getInvitePerMemberAddition();
                        IncreaseAddition approvedUserHeadImgAddition = increaseResult.getApprovedUserHeadImgAddition();
                        IncreaseAddition approvedCardsAddition = increaseResult.getApprovedCardsAddition();
                        IncreaseAddition completeMemberDataAddition = increaseResult.getCompleteMemberDataAddition();

                        if (bindMobileAddition != null) {
                            int addFriendPerDayAddition = bindMobileAddition.getAddFriendPerDayAddition();
                            int friendAmountLimitAddition = bindMobileAddition.getFriendAmountLimitAddition();
                            int renmaiSearchListLimitAddition = bindMobileAddition.getRenmaiSearchListLimitAddition();
                            //界面显示
                            boundPhoneNumb_Ll.setVisibility(View.VISIBLE);
                            bound_sendInvite_original_txt.setText("" + addFriendPerdayLimit);
                            bound_sendInvite_upgrade_txt.setText("" + (addFriendPerdayLimit + addFriendPerDayAddition));
                            bound_contacts_original_txt.setText("" + friendAmountLimit);
                            bound_contacts_upgrade_txt.setText("" + (friendAmountLimit + friendAmountLimitAddition));
                            bound_search_original_txt.setText("" + renmaiSearchListLimit);
                            bound_search_upgrade_txt.setText("" + (renmaiSearchListLimit + renmaiSearchListLimitAddition));
                        } else {
                            boundPhoneNumb_Ll.setVisibility(View.GONE);
                        }

                        if (importContactAddition != null) {
                            int addFriendPerDayAddition = importContactAddition.getAddFriendPerDayAddition();
                            int friendAmountLimitAddition = importContactAddition.getFriendAmountLimitAddition();
                            int renmaiSearchListLimitAddition = importContactAddition.getRenmaiSearchListLimitAddition();
                            //界面显示
                            importMaillist_Ll.setVisibility(View.VISIBLE);
                            import_sendInvite_original_txt.setText("" + addFriendPerdayLimit);
                            import_sendInvite_upgrade_txt.setText("" + (addFriendPerdayLimit + addFriendPerDayAddition));
                            import_contacts_original_txt.setText("" + friendAmountLimit);
                            import_contacts_upgrade_txt.setText("" + (friendAmountLimit + friendAmountLimitAddition));
                            import_search_original_txt.setText("" + renmaiSearchListLimit);
                            import_search_upgrade_txt.setText("" + (renmaiSearchListLimit + renmaiSearchListLimitAddition));
                        } else {
                            importMaillist_Ll.setVisibility(View.GONE);
                        }

                        if (validateRealNameAddition != null) {
                            int addFriendPerDayAddition = validateRealNameAddition.getAddFriendPerDayAddition();
                            int friendAmountLimitAddition = validateRealNameAddition.getFriendAmountLimitAddition();
                            int renmaiSearchListLimitAddition = validateRealNameAddition.getRenmaiSearchListLimitAddition();
                            //界面显示
                            realNameAuthentication_Ll.setVisibility(View.VISIBLE);
                            auth_sendInvite_original_txt.setText("" + addFriendPerdayLimit);
                            auth_sendInvite_upgrade_txt.setText("" + (addFriendPerdayLimit + addFriendPerDayAddition));
                            auth_contacts_original_txt.setText("" + friendAmountLimit);
                            auth_contacts_upgrade_txt.setText("" + (friendAmountLimit + friendAmountLimitAddition));
                            auth_search_original_txt.setText("" + renmaiSearchListLimit);
                            auth_search_upgrade_txt.setText("" + (renmaiSearchListLimit + renmaiSearchListLimitAddition));
                        } else {
                            realNameAuthentication_Ll.setVisibility(View.GONE);
                        }

                        if (invitePerMemberAddition != null) {
                            int friendAmountLimitAddition = invitePerMemberAddition.getFriendAmountLimitAddition();
                            int renmaiSearchListLimitAddition = invitePerMemberAddition.getRenmaiSearchListLimitAddition();
                            //界面显示
                            inviteFriendsRegister_Ll.setVisibility(View.VISIBLE);
                            invite_contacts_upgrade_txt.setText("+" + friendAmountLimitAddition);
                            invite_search_upgrade_txt.setText("+" + renmaiSearchListLimitAddition);
                        } else {
                            inviteFriendsRegister_Ll.setVisibility(View.GONE);
                        }

                        if (completeMemberDataAddition != null) {
                            int addFriendPerDayAddition = completeMemberDataAddition.getAddFriendPerDayAddition();
                            int friendAmountLimitAddition = completeMemberDataAddition.getFriendAmountLimitAddition();
                            int renmaiSearchListLimitAddition = completeMemberDataAddition.getRenmaiSearchListLimitAddition();
                            //界面显示
                            completeMemberDataNumb_Ll.setVisibility(View.VISIBLE);
                            completeMemberData_sendInvite_original_txt.setText("" + addFriendPerdayLimit);
                            completeMemberData_sendInvite_upgrade_txt
                                    .setText("" + (addFriendPerdayLimit + addFriendPerDayAddition));
                            completeMemberData_contacts_original_txt.setText("" + friendAmountLimit);
                            completeMemberData_contacts_upgrade_txt.setText("" + (friendAmountLimit + friendAmountLimitAddition));
                            completeMemberData_search_original_txt.setText("" + renmaiSearchListLimit);
                            completeMemberData_search_upgrade_txt
                                    .setText("" + (renmaiSearchListLimit + renmaiSearchListLimitAddition));
                        } else {
                            completeMemberDataNumb_Ll.setVisibility(View.GONE);
                        }

                        if (approvedUserHeadImgAddition != null) {
                            int addFriendPerDayAddition = approvedUserHeadImgAddition.getAddFriendPerDayAddition();
                            int friendAmountLimitAddition = approvedUserHeadImgAddition.getFriendAmountLimitAddition();
                            int renmaiSearchListLimitAddition = approvedUserHeadImgAddition.getRenmaiSearchListLimitAddition();
                            //界面显示
                            approvedUserHeadNumb_Ll.setVisibility(View.VISIBLE);
                            approvedUserHead_sendInvite_original_txt.setText("" + addFriendPerdayLimit);
                            approvedUserHead_sendInvite_upgrade_txt
                                    .setText("" + (addFriendPerdayLimit + addFriendPerDayAddition));
                            approvedUserHead_contacts_original_txt.setText("" + friendAmountLimit);
                            approvedUserHead_contacts_upgrade_txt.setText("" + (friendAmountLimit + friendAmountLimitAddition));
                            approvedUserHead_search_original_txt.setText("" + renmaiSearchListLimit);
                            approvedUserHead_search_upgrade_txt
                                    .setText("" + (renmaiSearchListLimit + renmaiSearchListLimitAddition));
                        } else {
                            approvedUserHeadNumb_Ll.setVisibility(View.GONE);
                        }

                        if (approvedCardsAddition != null) {
                            int addFriendPerDayAddition = approvedCardsAddition.getAddFriendPerDayAddition();
                            int friendAmountLimitAddition = approvedCardsAddition.getFriendAmountLimitAddition();
                            int renmaiSearchListLimitAddition = approvedCardsAddition.getRenmaiSearchListLimitAddition();
                            //界面显示
                            approvedCardsNumb_Ll.setVisibility(View.VISIBLE);
                            approvedCards_sendInvite_original_txt.setText("" + addFriendPerdayLimit);
                            approvedCards_sendInvite_upgrade_txt.setText("" + (addFriendPerdayLimit + addFriendPerDayAddition));
                            approvedCards_contacts_original_txt.setText("" + friendAmountLimit);
                            approvedCards_contacts_upgrade_txt.setText("" + (friendAmountLimit + friendAmountLimitAddition));
                            approvedCards_search_original_txt.setText("" + renmaiSearchListLimit);
                            approvedCards_search_upgrade_txt
                                    .setText("" + (renmaiSearchListLimit + renmaiSearchListLimitAddition));
                        } else {
                            approvedCardsNumb_Ll.setVisibility(View.GONE);
                        }

                    } else {
                        //都完成
                        ToastUtil.showToast(AccountLimitUpgradeActivity.this, "已升级到上限，请升级会员等级提升");
                        finish();
                    }
                }
            } else {
                ToastUtil.showConnectError(AccountLimitUpgradeActivity.this);
            }
        }
    }

    /**
     * @description 广播接收，刷新界面处理
     * @date 2015-6-30
     */
    class RefreshAccountLimitReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if (arg1.getAction() != null && arg1.getAction().equals(Constants.BroadCastAction.UPDATE_ACCOUNTLIMIT_ACTION))
                new getAccountLimitIncreaseTask().executeOnExecutor(Executors.newCachedThreadPool(),
                        RenheApplication.getInstance().getUserInfo().getSid(),
                        RenheApplication.getInstance().getUserInfo().getAdSId(), "" + showModule);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (refreshAccountLimitReceiver != null)
            unregisterReceiver(refreshAccountLimitReceiver);
    }
}
