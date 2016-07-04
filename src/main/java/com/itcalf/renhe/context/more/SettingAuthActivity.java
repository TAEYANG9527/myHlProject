package com.itcalf.renhe.context.more;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.relationship.NearbyTask;
import com.itcalf.renhe.context.relationship.NearbyTask.IDataBack;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.dto.NearbyPeople;
import com.itcalf.renhe.service.RenheService;
import com.itcalf.renhe.utils.HttpUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class SettingAuthActivity extends BaseActivity implements OnCheckedChangeListener {
    //	private ToggleButton smoothDragCB;
    //	private ToggleButton exitClearCacheCB;
    //	private RelativeLayout nowClearCacheRl;
    private RelativeLayout addFriendRl1;
    private RelativeLayout addFriendRl2;
    private RelativeLayout addFriendRl3;
    private View addFriendSeperateLine3;
    private ImageView addFriendIv1;
    private ImageView addFriendIv2;
    private ImageView addFriendIv3;

    private SwitchCompat contactSb1;
    private SwitchCompat contactSb2;
    private SwitchCompat contactSb3;

    private RelativeLayout personalRl2;
    private RelativeLayout personalRl3;
    private SwitchCompat personalSb1;
    private SwitchCompat personalSb2;
    private SwitchCompat personalSb3;
    private View personalSeperateLine2;
    private View personalSeperateLine3;
    private SharedPreferences userInfo;
    Editor editor;

    private int type;//说明：1 代表编辑“添加好友权限”；2 代表编辑“猎头直接联系”；3 代表编辑“我不希望档案被搜索引擎收录”；4 代表编辑“隐身访问其他会员档案”；5 代表编辑“只允许付费会员查看我的全部档案”
    private int referralType;// int 1：允许直接添加我为好友 2：只允许通过实名认证的会员添加我为好友 3：只允许付费会员添加我为好友
    //	private int friendLook;// int 说明：1 允许好友查看我的人脉；0 不允许好友查看我的人脉
    private int lietouBeContact;// int 说明：1 允许猎头直接联系；0 不允许猎头直接联系
    //	private int memberLocationStatus;// int 说明：1 打开附近的人脉；0 关闭附近的人脉
    private int seoCannotEmbody;// boolean 说明：为true时代表设置了“我不希望档案被搜索引擎收录”，false代表不设置
    private int stealthViewProfile;// boolean 说明：为true时代表设置了“我希望隐身访问其他会员档案”，false代表不设置
    private int vipViewFullProfile;// boolean 说明：为true时代表设置了“只允许付费会员查看我的全部档案”，false代表不设置
    private TextView signTv;
    private String lat, lng, city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ActivityTemplate().doInActivity(this, R.layout.setting_auth);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("设置——权限设置"); //统计页面
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("设置——权限设置"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
    }

    @Override
    protected void findView() {
        super.findView();
        userInfo = getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0);
        editor = userInfo.edit();
        addFriendRl1 = (RelativeLayout) findViewById(R.id.add_friend_rl1);
        addFriendRl2 = (RelativeLayout) findViewById(R.id.add_friend_rl2);
        addFriendRl3 = (RelativeLayout) findViewById(R.id.add_friend_rl3);
        addFriendSeperateLine3 = (View) findViewById(R.id.add_friend_seperate3);
        addFriendIv1 = (ImageView) findViewById(R.id.add_friend_sb1);
        addFriendIv2 = (ImageView) findViewById(R.id.add_friend_sb2);
        addFriendIv3 = (ImageView) findViewById(R.id.add_friend_sb3);

        contactSb1 = (SwitchCompat) findViewById(R.id.contact_sb1);
        contactSb2 = (SwitchCompat) findViewById(R.id.contact_sb2);
        contactSb3 = (SwitchCompat) findViewById(R.id.contact_sb3);

        personalRl2 = (RelativeLayout) findViewById(R.id.personal_rl2);
        personalRl3 = (RelativeLayout) findViewById(R.id.personal_rl3);
        personalSb1 = (SwitchCompat) findViewById(R.id.personal_sb1);
        personalSb2 = (SwitchCompat) findViewById(R.id.personal_sb2);
        personalSb3 = (SwitchCompat) findViewById(R.id.personal_sb3);
        personalSeperateLine2 = findViewById(R.id.personal_separate_line2);
        personalSeperateLine3 = findViewById(R.id.personal_separate_line3);
        signTv = (TextView) findViewById(R.id.sign_tv);
    }

    @Override
    protected void initData() {
        super.initData();
        setTextValue(R.id.title_txt, "隐私");
        lat = RenheService.latitude;
        lng = RenheService.longitude;
        city = RenheService.cityName;
        checkAuthByShep();
        checkAuth();
    }

    @Override
    protected void initListener() {
        super.initListener();
        addFriendRl1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                type = 1;
                referralType = 1;
                editor.putInt("referralType", 1);
                editor.commit();
                addFriendIv1.setVisibility(View.VISIBLE);
                addFriendIv2.setVisibility(View.INVISIBLE);
                addFriendIv3.setVisibility(View.INVISIBLE);
                editCheckAuth();
            }
        });
        addFriendRl2.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                type = 1;
                referralType = 2;
                editor.putInt("referralType", 2);
                editor.commit();
                addFriendIv1.setVisibility(View.INVISIBLE);
                addFriendIv2.setVisibility(View.VISIBLE);
                addFriendIv3.setVisibility(View.INVISIBLE);
                editCheckAuth();
            }
        });
        addFriendRl3.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                type = 1;
                referralType = 3;
                editor.putInt("referralType", 3);
                editor.commit();
                addFriendIv1.setVisibility(View.INVISIBLE);
                addFriendIv2.setVisibility(View.INVISIBLE);
                addFriendIv3.setVisibility(View.VISIBLE);
                editCheckAuth();
            }
        });
        contactSb1.setOnCheckedChangeListener(this);
        contactSb2.setOnCheckedChangeListener(this);
        contactSb3.setOnCheckedChangeListener(this);
        personalSb1.setOnCheckedChangeListener(this);
        personalSb2.setOnCheckedChangeListener(this);
        personalSb3.setOnCheckedChangeListener(this);

        signTv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                startActivity(new Intent(SettingAuthActivity.this, ContactAuthClauseActivity.class));
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.contact_sb1:
                break;
            case R.id.contact_sb2:
                type = 2;
                if (isChecked) {
                    lietouBeContact = 1;
                    editor.putInt("lietouBeContact", 1);
                } else {
                    lietouBeContact = 0;
                    editor.putInt("lietouBeContact", 0);
                }
                editor.commit();
                editCheckAuth();
                break;
            case R.id.contact_sb3:
                if (isChecked) {
                    new NearbyTask(this, new IDataBack() {

                        @Override
                        public void onPre() {
                        }

                        @Override
                        public void onPost(NearbyPeople result) {
                        }

                    }).executeOnExecutor(Executors.newCachedThreadPool(), -1, lat, lng, city, 1, 1);
                    //				memberLocationStatus = 1;
                    editor.putBoolean("memberLocationStatus", true);
                } else {
                    new CleanLocationTask().executeOnExecutor(Executors.newCachedThreadPool(),
                            ((RenheApplication) SettingAuthActivity.this.getApplicationContext()).getUserInfo().getSid(),
                            ((RenheApplication) SettingAuthActivity.this.getApplicationContext()).getUserInfo().getAdSId(), lat, lng);
                    //				memberLocationStatus = 0;
                    editor.putBoolean("memberLocationStatus", false);
                }
                break;
            case R.id.personal_sb1:
                type = 3;
                if (isChecked) {
                    seoCannotEmbody = 1;
                    editor.putBoolean("seoCannotEmbody", true);
                } else {
                    seoCannotEmbody = 0;
                    editor.putBoolean("seoCannotEmbody", false);
                }
                editor.commit();
                editCheckAuth();
                break;
            case R.id.personal_sb2:
                type = 4;
                if (isChecked) {
                    stealthViewProfile = 1;
                    editor.putBoolean("stealthViewProfile", true);
                } else {
                    stealthViewProfile = 0;
                    editor.putBoolean("stealthViewProfile", false);
                }
                editor.commit();
                editCheckAuth();
                break;
            case R.id.personal_sb3:
                type = 5;
                if (isChecked) {
                    vipViewFullProfile = 1;
                    editor.putBoolean("vipViewFullProfile", true);
                } else {
                    vipViewFullProfile = 0;
                    editor.putBoolean("vipViewFullProfile", false);
                }
                editor.commit();
                editCheckAuth();
                break;
            default:
                break;
        }

    }

    private void checkAuth() {
        new PersonAuthTask(this) {
            public void doPre() {
            }

            ;

            public void doPost(com.itcalf.renhe.dto.PersonAuth result) {
                if (null != result && result.getState() == 1) {
                    if (result.isShowVipAddFriendPrivilege()) {
                        editor.putBoolean("showVipAddFriendPrivilege", true);
                    } else {
                        editor.putBoolean("showVipAddFriendPrivilege", false);
                    }
                    editor.putInt("referralType", result.getReferralType());
                    editor.putInt("lietouBeContact", result.getLietouBeContact());
                    if (result.isViewProfileHidePrivilege()) {
                        editor.putBoolean("viewProfileHidePrivilege", true);
                    } else {
                        editor.putBoolean("viewProfileHidePrivilege", false);
                    }
                    if (result.isPayAccountViewFullProfilePrivilege()) {
                        editor.putBoolean("payAccountViewFullProfilePrivilege", true);
                    } else {
                        editor.putBoolean("payAccountViewFullProfilePrivilege", false);
                    }

                    if (result.isSeoCannotEmbody()) {
                        editor.putBoolean("seoCannotEmbody", true);
                    } else {
                        editor.putBoolean("seoCannotEmbody", false);
                    }
                    if (result.isStealthViewProfile()) {
                        editor.putBoolean("stealthViewProfile", true);
                    } else {
                        editor.putBoolean("stealthViewProfile", false);
                    }
                    if (result.isVipViewFullProfile()) {
                        editor.putBoolean("vipViewFullProfile", true);
                    } else {
                        editor.putBoolean("vipViewFullProfile", false);
                    }
                    if (result.isMemberLocationStatus()) {
                        editor.putBoolean("memberLocationStatus", true);
                    } else {
                        editor.putBoolean("memberLocationStatus", false);
                    }
                    editor.commit();
                    checkAuthByShep();
                }
            }

            ;
        }.executeOnExecutor(Executors.newCachedThreadPool(), RenheApplication.getInstance().getUserInfo().getSid(),
                RenheApplication.getInstance().getUserInfo().getAdSId());
    }

    private void editCheckAuth() {
        new EditPersonAuthTask(this) {
            public void doPre() {
            }

            ;

            public void doPost(MessageBoardOperation result) {
            }

            ;
        }.executeOnExecutor(Executors.newCachedThreadPool(), RenheApplication.getInstance().getUserInfo().getSid(),
                RenheApplication.getInstance().getUserInfo().getAdSId(), type + "", referralType + "", lietouBeContact + "",
                seoCannotEmbody + "", stealthViewProfile + "", vipViewFullProfile + "");
    }

    private void checkAuthByShep() {
        if (userInfo.getBoolean("showVipAddFriendPrivilege", false)) {
            addFriendRl3.setVisibility(View.VISIBLE);
            addFriendSeperateLine3.setVisibility(View.VISIBLE);
        } else {
            addFriendRl3.setVisibility(View.GONE);
            addFriendSeperateLine3.setVisibility(View.GONE);
        }
        referralType = userInfo.getInt("referralType", 0);
        if (userInfo.getInt("referralType", 0) == 1) {
            addFriendIv1.setVisibility(View.VISIBLE);
            addFriendIv2.setVisibility(View.INVISIBLE);
            addFriendIv3.setVisibility(View.INVISIBLE);
        } else if (userInfo.getInt("referralType", 0) == 2) {
            addFriendIv1.setVisibility(View.INVISIBLE);
            addFriendIv2.setVisibility(View.VISIBLE);
            addFriendIv3.setVisibility(View.INVISIBLE);
        } else if (userInfo.getInt("referralType", 0) == 3) {
            addFriendIv1.setVisibility(View.INVISIBLE);
            addFriendIv2.setVisibility(View.INVISIBLE);
            addFriendIv3.setVisibility(View.VISIBLE);
        }
        lietouBeContact = userInfo.getInt("lietouBeContact", 0);
        if (userInfo.getInt("lietouBeContact", 0) == 1) {
            contactSb2.setChecked(true);
        } else {
            contactSb2.setChecked(false);
        }

        if (userInfo.getBoolean("memberLocationStatus", false)) {
            //			memberLocationStatus = 1;
            contactSb3.setChecked(true);
        } else {
            //			memberLocationStatus = 0;
            contactSb3.setChecked(false);
        }

        if (userInfo.getBoolean("viewProfileHidePrivilege", false)) {
            personalRl2.setVisibility(View.VISIBLE);
            personalSeperateLine2.setVisibility(View.VISIBLE);
        } else {
            personalRl2.setVisibility(View.GONE);
            personalSeperateLine2.setVisibility(View.GONE);
        }
        if (userInfo.getBoolean("payAccountViewFullProfilePrivilege", false)) {
            personalRl3.setVisibility(View.VISIBLE);
            personalSeperateLine3.setVisibility(View.VISIBLE);
        } else {
            personalRl3.setVisibility(View.GONE);
            personalSeperateLine3.setVisibility(View.GONE);
        }

        if (userInfo.getBoolean("seoCannotEmbody", false)) {
            seoCannotEmbody = 1;
            personalSb1.setChecked(true);
        } else {
            seoCannotEmbody = 0;
            personalSb1.setChecked(false);
        }
        if (userInfo.getBoolean("stealthViewProfile", false)) {
            stealthViewProfile = 1;
            personalSb2.setChecked(true);
        } else {
            stealthViewProfile = 0;
            personalSb2.setChecked(false);
        }
        if (userInfo.getBoolean("vipViewFullProfile", false)) {
            vipViewFullProfile = 1;
            personalSb3.setChecked(true);
        } else {
            vipViewFullProfile = 0;
            personalSb3.setChecked(false);
        }
    }

    private class CleanLocationTask extends AsyncTask<String, Void, MessageBoardOperation> {

        @Override
        protected MessageBoardOperation doInBackground(String... params) {
            Map<String, Object> reqParams = new HashMap<String, Object>();
            reqParams.put("sid", params[0]);
            reqParams.put("adSId", params[1]);
            try {
                MessageBoardOperation mbo = (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.CLEAN_LOCATION,
                        reqParams, MessageBoardOperation.class, SettingAuthActivity.this);
                return mbo;
            } catch (Exception e) {
                System.out.println(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(MessageBoardOperation result) {
            super.onPostExecute(result);
            if (null != result && result.getState() == 1) {
            }
        }
    }
}
