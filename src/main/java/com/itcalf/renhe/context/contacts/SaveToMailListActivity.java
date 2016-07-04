package com.itcalf.renhe.context.contacts;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.ContactAdapter;
import com.itcalf.renhe.bean.HlContactRenheMember;
import com.itcalf.renhe.bean.HlMemberSaveToMail;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.context.upgrade.UpgradeActivity;
import com.itcalf.renhe.utils.HlContactsUtils;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.utils.WriteContactsUtil;
import com.itcalf.renhe.view.ContactSideBar;
import com.itcalf.renhe.view.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * description :保存到手机通讯录
 * Created by Chans Renhenet
 * 2015/12/22
 */
public class SaveToMailListActivity extends BaseActivity {

    private LinearLayout emptyLl;
    private TextView upgradeImmediately;

    // 联系人快速定位视图组件
    private ContactSideBar sideBar;
    // 联系人列表
    private ListView mContactsListView;
    // 字母显示
    private TextView mLetterTxt;

    private LinearLayout check_Ll;
    private TextView checkAll_Tv, AKeySave_Tv;

    // 带标题分割的Adapter
    private ContactAdapter mAdapter;

    // 联系人数据
    private List<HlMemberSaveToMail> hlMemberSaveToMailList;
    //    private List<Contact> contacts;
    //选中的联系人
    private List<HlMemberSaveToMail> selectContacts;
    private int selectCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.save_to_maillist);
    }

    @Override
    protected void findView() {
        super.findView();

        emptyLl = (LinearLayout) findViewById(R.id.empty_ly);
        upgradeImmediately = (TextView) findViewById(R.id.upgrade_immediately);

        sideBar = (ContactSideBar) findViewById(R.id.contact_cv);
        mLetterTxt = (TextView) findViewById(R.id.letter_txt);
        mContactsListView = (ListView) findViewById(R.id.contacts_list);

        check_Ll = (LinearLayout) findViewById(R.id.check_Ll);
        checkAll_Tv = (TextView) findViewById(R.id.checkAll_Tv);
        AKeySave_Tv = (TextView) findViewById(R.id.AKeySave_Tv);
    }

    @Override
    protected void initData() {
        super.initData();

        setTextValue("存入手机通讯录");
        //付费会员的权限
        if (RenheApplication.getInstance().getUserInfo().getAccountType() > 0) {
            emptyLl.setVisibility(View.GONE);
            mContactsListView.setVisibility(View.VISIBLE);
            sideBar.setVisibility(View.VISIBLE);
            sideBar.setListView(mContactsListView);
            sideBar.setTextView(mLetterTxt);

            hlMemberSaveToMailList = new ArrayList<>();
            selectContacts = new ArrayList<>();
            mContactsListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
            //获取人脉
            localSearch();
        } else {
            emptyLl.setVisibility(View.VISIBLE);
            mContactsListView.setVisibility(View.GONE);
            sideBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void initListener() {
        super.initListener();

        upgradeImmediately.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SaveToMailListActivity.this, UpgradeActivity.class));
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });

        mContactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                check_Ll.setVisibility(View.VISIBLE);
                //显示选择框，单击则进行选择
                boolean state = hlMemberSaveToMailList.get(position).isSelect();
                if (state) {
                    hlMemberSaveToMailList.get(position).setIsSelect(false);
                    if (selectCount > 0)
                        selectCount--;
                } else {
                    hlMemberSaveToMailList.get(position).setIsSelect(true);
                    selectCount++;
                }
                mAdapter.notifyDataSetChanged();
                if (selectCount > 0) {
                    checkAll_Tv.setText("多选（" + selectCount + "）");
                    AKeySave_Tv.setEnabled(true);
                } else {
                    checkAll_Tv.setText("多选");
                    AKeySave_Tv.setEnabled(false);
                }

//                getSelectCount(contacts);
//                //如果是全选状态，点击取消全选
//                if (isAllChecked) {
//                    isAllChecked = false;
//                    checkAll_Tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_recommend_unsel, 0, 0, 0);
//                }
            }
        });

        AKeySave_Tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                materialDialogsUtil.showIndeterminateProgressDialog("正在保存...");
//                materialDialogsUtil.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < hlMemberSaveToMailList.size(); i++) {
                            if (hlMemberSaveToMailList.get(i).isSelect()) {
                                selectContacts.add(hlMemberSaveToMailList.get(i));
                            }
                        }
                        if (null != selectContacts && selectContacts.size() > 0) {
                            final int t = new WriteContactsUtil(SaveToMailListActivity.this).BatchToAdd(selectContacts);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    switch (t) {
                                        case -1:
                                            //没有权限
                                            MaterialDialogsUtil materialDialog = new MaterialDialogsUtil(SaveToMailListActivity.this);
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
                                            break;
                                        case 0:
                                            //保存失败
                                            ToastUtil.showToast(SaveToMailListActivity.this, "保存失败");
                                            break;
                                        case 1:
                                            //保存成功
                                            ToastUtil.showToast(SaveToMailListActivity.this, "保存成功");
                                            finish();
                                            break;
                                    }
                                }
                            });
                        }
                    }
                }).start();
            }
        });
    }

    //获取本地所有和聊联系人
    private void localSearch() {
        showLoadingDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    hlMemberSaveToMailList.clear();
                    List<HlContactRenheMember> hlContactRenheMemberList = new ArrayList<>();
                    hlContactRenheMemberList = HlContactsUtils.getAllHlFriendContacts(hlContactRenheMemberList);
                    for (HlContactRenheMember hlContactRenheMember : hlContactRenheMemberList) {
                        HlMemberSaveToMail hlMemberSaveToMail = new HlMemberSaveToMail();
                        hlMemberSaveToMail.setHlContactRenheMember(hlContactRenheMember);
                        hlMemberSaveToMail.setIsSelect(false);
                        hlMemberSaveToMailList.add(hlMemberSaveToMail);
                    }
                    if (null != hlMemberSaveToMailList && hlMemberSaveToMailList.size() > 0) {
                        Collections.sort(hlMemberSaveToMailList, new PinyinComparator());
                        //ui处理
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideLoadingDialog();
                                mAdapter = new ContactAdapter(SaveToMailListActivity.this, hlMemberSaveToMailList);
                                mContactsListView.setAdapter(mAdapter);
                            }
                        });
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }).start();
    }

    //统计选中的个数
    private void getSelectCount(List<HlMemberSaveToMail> contactSelectList) {
        if (null != contactSelectList && contactSelectList.size() > 0) {
            int count = 0;
            for (int i = 0; i < contactSelectList.size(); i++) {
                if (contactSelectList.get(i).isSelect()) {
                    count++;
                    selectContacts.add(contactSelectList.get(i));
                }
            }
            if (count > 0) {
                checkAll_Tv.setText("多选（" + count + "）");
                AKeySave_Tv.setEnabled(true);
            } else {
                checkAll_Tv.setText("多选");
                AKeySave_Tv.setEnabled(false);
            }
        }
    }
}
