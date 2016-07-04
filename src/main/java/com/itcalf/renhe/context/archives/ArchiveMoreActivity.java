package com.itcalf.renhe.context.archives;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.bean.HlContactRenheMember;
import com.itcalf.renhe.context.room.WebViewActivityForReport;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.context.upgrade.UpgradeActivity;
import com.itcalf.renhe.controller.GrpcController;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.dto.MessageBoardOperationWithErroInfo;
import com.itcalf.renhe.dto.Profile;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.po.Contact;
import com.itcalf.renhe.task.AddBlackListTask;
import com.itcalf.renhe.task.AsyncTaskCallBack;
import com.itcalf.renhe.task.RemoveBlackListTask;
import com.itcalf.renhe.utils.ContactsUtil;
import com.itcalf.renhe.utils.HlContactsUtils;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.SharedPreferencesUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.utils.WriteContactsUtil;
import com.itcalf.renhe.view.ShareProfilePopupWindow;
import com.itcalf.renhe.view.TextView;

import java.text.MessageFormat;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.renhe.heliao.idl.collection.MyCollection;
import cn.renhe.heliao.idl.member.MemberGrade;

/**
 * 好友/非好友 查看其档案点击右上角进入的子页面，可以进行更多的操作，比如分享、收藏、举报等
 * Created by wangning on 2016/2/25.
 */
public class ArchiveMoreActivity extends BaseActivity {

    @BindView(R.id.archive_more_add_into_contacts_rl)
    RelativeLayout archiveMoreAddIntoContactsRl;
    @BindView(R.id.share_rl)
    RelativeLayout shareRl;
    @BindView(R.id.collect_cb)
    SwitchCompat collectCb;
    @BindView(R.id.collect_rl)
    RelativeLayout collectRl;
    @BindView(R.id.grade_rl)
    RelativeLayout gradeRl;
    @BindView(R.id.blacklist_cb)
    SwitchCompat blacklistCb;
    @BindView(R.id.black_list_rl)
    RelativeLayout blackListRl;
    @BindView(R.id.report_rl)
    RelativeLayout reportRl;
    @BindView(R.id.delete_rl)
    RelativeLayout deleteRl;
    @BindView(R.id.rootrl)
    RelativeLayout rootrl;
    @BindView(R.id.grade_tip_tv)
    TextView gradeTipTv;
    @BindView(R.id.report_divider_view)
    View reportDividerView;
    @BindView(R.id.new_flag_tv)
    TextView newFlagTv;
    @BindView(R.id.secretary_title_tv)
    TextView secretaryTitleTv;
    @BindView(R.id.secretary_rl)
    RelativeLayout secretaryRl;
    @BindView(R.id.secretary_new_flag_tv)
    TextView secretaryNewFlagTv;
    @BindView(R.id.secretary_sended_tv)
    TextView secretarySendedTv;

    private ShareProfilePopupWindow shareProfilePopupWindow;

    private Profile mProfile;
    private boolean isBlocked;
    private boolean isDeleted;
    private boolean isCollected;
    private MemberGrade.MemberGradeInfo memberGradeInfo;//获取到的打分、评价
    private int ID_TASK_ARCHIVE_MORE_ADDCOLLECT = TaskManager.getTaskId();//收藏好友
    private int ID_TASK_ARCHIVE_MORE_DELETECOLLECT = TaskManager.getTaskId();//取消收藏好友

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.archive_more_activity_layout);
    }

    @Override
    protected void findView() {
        super.findView();
        setTextValue("更多");
        ButterKnife.bind(this);
    }

    @Override
    protected void initData() {
        super.initData();
        if (null != getIntent().getSerializableExtra("profile")) {
            mProfile = (Profile) getIntent().getSerializableExtra("profile");
            isBlocked = mProfile.getUserInfo().isBlocked();
            shareProfilePopupWindow = new ShareProfilePopupWindow(ArchiveMoreActivity.this, rootrl,
                    mProfile.getUserInfo().getName(),
                    mProfile.getUserInfo().getCompany() + mProfile.getUserInfo().getIndustry(),
                    mProfile.getUserInfo().getUserface(), mProfile.getUserInfo().getSid(),
                    mProfile, true);
            if (!mProfile.isConnection()) {
                archiveMoreAddIntoContactsRl.setVisibility(View.GONE);
                gradeRl.setVisibility(View.GONE);
                blackListRl.setVisibility(View.GONE);
                deleteRl.setVisibility(View.GONE);
                reportDividerView.setVisibility(View.VISIBLE);
                if (mProfile.isSendSecretary()) {
                    secretarySendedTv.setVisibility(View.VISIBLE);
                    secretaryRl.setClickable(false);
                }
                if (mProfile.getBeInvitedInfo().isBeInvited()) {
                    secretaryRl.setVisibility(View.GONE);
                }
            } else {
                secretaryRl.setVisibility(View.GONE);
            }
            gradeTipTv.setText(MessageFormat.format(getString(R.string.archive_more_grade_tip), mProfile.getUserInfo().getName()));
            if (mProfile.isCollected()) {
                collectCb.setChecked(true);
            } else {
                collectCb.setChecked(false);
            }
            if (mProfile.getUserInfo().isBlocked()) {
                blacklistCb.setChecked(true);
            } else {
                blacklistCb.setChecked(false);
            }
        } else {
            gradeTipTv.setVisibility(View.GONE);
        }
        if (SharedPreferencesUtil.getBooleanShareData(Constants.SHAREDPREFERENCES_KEY.USER_GRADE_NEW, true, true))
            newFlagTv.setVisibility(View.VISIBLE);
        if (SharedPreferencesUtil.getBooleanShareData(Constants.SHAREDPREFERENCES_KEY.USER_USER_ARCHIVE_SECRETARY_NEW, true, true))
            secretaryNewFlagTv.setVisibility(View.VISIBLE);
    }

    @Override
    protected void initListener() {
        super.initListener();
        collectCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    addCollect();
                } else {
                    deleteCollect();
                }
            }
        });
        blacklistCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isBlocked = true;
                    addToBlackList();
                } else {
                    isBlocked = false;
                    removeFromBlackList();
                }
            }
        });
    }

    @Override
    public void onSuccess(int type, Object result) {
        super.onSuccess(type, result);
        if (null != result) {
            if (result instanceof MyCollection.CollectResponse) {
                isCollected = true;
//                ToastUtil.showToast(ArchiveMoreActivity.this, R.string.collect_success);
            } else if (result instanceof MyCollection.DeleteCollectionResponse) {
                isCollected = false;
//                ToastUtil.showToast(ArchiveMoreActivity.this, R.string.un_collect_success);
            }
        }
    }

    @Override
    public void onFailure(int type, String msg) {
        super.onFailure(type, msg);
    }

    @OnClick({R.id.archive_more_add_into_contacts_rl, R.id.share_rl, R.id.collect_rl, R.id.grade_rl,
            R.id.black_list_rl, R.id.report_rl, R.id.delete_rl, R.id.secretary_rl})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.archive_more_add_into_contacts_rl:
                goPutIntoContact();
                break;
            case R.id.share_rl:
                goShare();
                break;
            case R.id.collect_rl:
                collectCb.toggle();
                break;
            case R.id.grade_rl:
                Intent intent = new Intent(ArchiveMoreActivity.this, UserGradeActivity.class);
                intent.putExtra("profile", mProfile);
                intent.putExtra("memberGradeInfo", memberGradeInfo);
                startHlActivityForResult(intent, Constants.USER_GRADE_REQUEST_CODE.USER_GRADE_REQUEST);
                if (newFlagTv.getVisibility() == View.VISIBLE) {
                    SharedPreferencesUtil.putBooleanShareData(Constants.SHAREDPREFERENCES_KEY.USER_GRADE_NEW, false, true);
                }
                break;
            case R.id.black_list_rl:
                blacklistCb.toggle();
                break;
            case R.id.report_rl:
                Intent reportIntent = new Intent(ArchiveMoreActivity.this, WebViewActivityForReport.class);
                reportIntent.putExtra("sid", mProfile.getUserInfo().getSid());
                reportIntent.putExtra("type", 2);
                startHlActivity(reportIntent);
                break;
            case R.id.delete_rl:
                deleteFriend();
                break;
            case R.id.secretary_rl:
                Intent secretaryIntent = new Intent(ArchiveMoreActivity.this, SecretaryInviteActivity.class);
                secretaryIntent.putExtra("profile", mProfile);
                startHlActivityForResult(secretaryIntent, Constants.USER_GRADE_REQUEST_CODE.USER_SECRETARY_REQUEST);
                if (secretaryNewFlagTv.getVisibility() == View.VISIBLE) {
                    secretaryNewFlagTv.setVisibility(View.GONE);
                    SharedPreferencesUtil.putBooleanShareData(Constants.SHAREDPREFERENCES_KEY.USER_USER_ARCHIVE_SECRETARY_NEW, false, true);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Constants.USER_GRADE_REQUEST_CODE.USER_GRADE_REQUEST:
                    newFlagTv.setVisibility(View.GONE);
                    if (null != data.getSerializableExtra("memberGradeInfo")) {
                        memberGradeInfo = (MemberGrade.MemberGradeInfo) data.getSerializableExtra("memberGradeInfo");
                    }
                    break;
                case Constants.USER_GRADE_REQUEST_CODE.USER_SECRETARY_REQUEST:
                    secretarySendedTv.setVisibility(View.VISIBLE);
                    secretaryRl.setClickable(false);
                    break;
            }
        }
    }

    /**
     * 添加到通讯录
     */
    private void goPutIntoContact() {
        if (null == mProfile)
            return;
        //付费会员的权限
        if (RenheApplication.getInstance().getUserInfo().getAccountType() > 0) {
            if (null != mProfile && null != mProfile.getUserInfo()) {
                Contact contact = new Contact();
                contact.setName(mProfile.getUserInfo().getName());
                Profile.UserInfo.ContactInfo ci = mProfile.getUserInfo().getContactInfo();
                if (null != ci) {
                    contact.setMobile(ci.getMobile());
                    contact.setEmail(ci.getEmail());
                }
                contact.setCompany(mProfile.getUserInfo().getCompany());
                addFriendToMailList(ArchiveMoreActivity.this, contact);
            }
        } else {
            MaterialDialogsUtil materialDialog = new MaterialDialogsUtil(ArchiveMoreActivity.this);
            materialDialog.getNotitleBuilder(R.string.upgrade_account_dialog, R.string.upgrade_account_dialog_ok,
                    R.string.upgrade_account_dialog_cancel).onAny(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(MaterialDialog dialog, DialogAction which) {
                    switch (which) {
                        case POSITIVE:
                            startActivity(new Intent(ArchiveMoreActivity.this, UpgradeActivity.class));
                            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                            break;
                        case NEGATIVE:
                            break;
                        default:
                            break;
                    }
                }
            });
            materialDialog.show();
        }
    }

    /**
     * 添加好友到本地手机通讯录
     */
    private void addFriendToMailList(final Context context, final Contact contact) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int t = new WriteContactsUtil(context).SignalToAdd(contact);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (t) {
                            case -1:
                                //没有权限
                                MaterialDialogsUtil materialDialog = new MaterialDialogsUtil(context);
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
                                ToastUtil.showToast(context, R.string.archive_more_add_into_contacts_fail);
                                break;
                            case 1:
                                //保存成功
                                ToastUtil.showToast(context, R.string.archive_more_add_into_contacts_success);
                                break;
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * 分享
     */
    private void goShare() {
        if (null == mProfile || null == shareProfilePopupWindow)
            return;
        shareProfilePopupWindow.show();
    }

    private void addToBlackList() {
        if (null == mProfile)
            return;
        AddBlackListTask addBlackListTask = new AddBlackListTask(this, new AsyncTaskCallBack() {
            @Override
            public void onPre() {

            }

            @Override
            public void doPost(Object result) {
                if (null != result && result instanceof MessageBoardOperation) {
                    MessageBoardOperation messageBoardOperation = (MessageBoardOperation) result;
                    if (messageBoardOperation.getState() == 1) {
                        ToastUtil.showToast(ArchiveMoreActivity.this, "成功加入黑名单");
                        isBlocked = true;
                        updateContactData(1);
                    } else {
                        isBlocked = false;
                        ToastUtil.showToast(ArchiveMoreActivity.this, "发生未知错误");
                    }
                } else {
                    isBlocked = false;
                    ToastUtil.showToast(ArchiveMoreActivity.this, "发生未知错误");
                }
            }
        });
        addBlackListTask.executeOnExecutor(Executors.newCachedThreadPool(), RenheApplication.getInstance().getUserInfo().getSid(),
                RenheApplication.getInstance().getUserInfo().getAdSId(), mProfile.getUserInfo().getSid());
    }

    private void removeFromBlackList() {
        if (null == mProfile)
            return;
        RemoveBlackListTask removeBlackListTask = new RemoveBlackListTask(this, new AsyncTaskCallBack() {
            @Override
            public void onPre() {

            }

            @Override
            public void doPost(Object result) {
                if (null != result && result instanceof MessageBoardOperation) {
                    MessageBoardOperation messageBoardOperation = (MessageBoardOperation) result;
                    if (messageBoardOperation.getState() == 1) {
                        ToastUtil.showToast(ArchiveMoreActivity.this, "成功移除黑名单");
                        isBlocked = false;
                        Intent intent = new Intent(Constants.BroadCastAction.REMOVE_BLACK_LIST);
                        intent.putExtra("sid", mProfile.getUserInfo().getSid());
                        sendBroadcast(intent);
                        updateContactData(2);
                    } else {
                        ToastUtil.showToast(ArchiveMoreActivity.this, "发生未知错误");
                    }
                } else {
                    ToastUtil.showToast(ArchiveMoreActivity.this, "发生未知错误");
                }
            }
        });
        removeBlackListTask.executeOnExecutor(Executors.newCachedThreadPool(), RenheApplication.getInstance().getUserInfo().getSid(),
                RenheApplication.getInstance().getUserInfo().getAdSId(), mProfile.getUserInfo().getSid());
    }

    public void updateContactData(final int type) {
        new ContactsUtil(ArchiveMoreActivity.this).updateContactBlockState(mProfile.getUserInfo().getContactInfo().getImId(), type,
                mProfile.getUserInfo().getName(), mProfile.getUserInfo().getUserface());
    }

    private void deleteFriend() {
        if (null == mProfile)
            return;
        isDeleted = true;
        materialDialogsUtil.getBuilder("确定要和" + mProfile.getUserInfo().getName() + "解除好友关系吗？")
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        switch (which) {
                            case POSITIVE:
                                new DeleteFriendTask(ArchiveMoreActivity.this) {
                                    @Override
                                    public void doPre() {
                                        materialDialogsUtil.showIndeterminateProgressDialog(R.string.deleting).cancelable(false).build();
                                        materialDialogsUtil.show();
                                    }

                                    @Override
                                    public void doPost(MessageBoardOperationWithErroInfo result) {
                                        materialDialogsUtil.dismiss();
                                        if (result == null) {
                                            ToastUtil.showErrorToast(ArchiveMoreActivity.this,
                                                    getString(R.string.connect_server_error));
                                            return;
                                        } else if (result.getState() == 1) {
                                            //通知会话列表删除被删除的好友对话
                                            Intent intent = new Intent(
                                                    Constants.BroadCastAction.DELETE_CONVERSATION_BY_OPENID_CONTACTS);
                                            intent.putExtra("deletedOpenId", mProfile.getUserInfo().getContactInfo().getImId() + "");
                                            sendBroadcast(intent);
                                            try {
                                                HlContactsUtils.deleteHlContactMemberBySid(HlContactRenheMember.class, mProfile.getUserInfo().getSid());
//                                                contactCommand.deleteMyContactBySid(RenheApplication.getInstance().getUserInfo().getSid(),
//                                                        mProfile.getUserInfo().getSid());
//                                                Intent brocastIntent = new Intent(Constants.BroadCastAction.REFRESH_CONTACT_RECEIVER_ACTION);
//                                                sendBroadcast(brocastIntent);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            } finally {
                                                Intent deleteIntent = new Intent();
                                                deleteIntent.putExtra("isDeleted", isDeleted);
                                                setResult(RESULT_OK, deleteIntent);
                                                finish();
                                            }

                                        } else {
                                            ToastUtil.showErrorToast(ArchiveMoreActivity.this, result.getErrorInfo());
                                        }
                                    }

                                }.executeOnExecutor(Executors.newCachedThreadPool(), mProfile.getUserInfo().getSid());
                                break;
                            case NEGATIVE:
                                break;
                            default:
                                break;
                        }
                    }
                });
        materialDialogsUtil.show();
    }

    private void addCollect() {
        if (null == mProfile)
            return;
        if (TaskManager.getInstance().exist(ID_TASK_ARCHIVE_MORE_ADDCOLLECT)) {
            return;
        }
        TaskManager.getInstance().addTask(this, ID_TASK_ARCHIVE_MORE_ADDCOLLECT);
        if (null == grpcController)
            grpcController = new GrpcController();
        grpcController.addCollect(ID_TASK_ARCHIVE_MORE_ADDCOLLECT, MyCollection.CollectResquest.CollectionType.RENMAI,
                mProfile.getUserInfo().getSid(), mProfile.getUserInfo().getSid(), 0);
    }

    private void deleteCollect() {
        if (null == mProfile)
            return;
        if (TaskManager.getInstance().exist(ID_TASK_ARCHIVE_MORE_DELETECOLLECT)) {
            return;
        }
        TaskManager.getInstance().addTask(this, ID_TASK_ARCHIVE_MORE_DELETECOLLECT);
        if (null == grpcController)
            grpcController = new GrpcController();
        grpcController.deleteCollect(ID_TASK_ARCHIVE_MORE_DELETECOLLECT, MyCollection.CollectResquest.CollectionType.RENMAI,
                mProfile.getUserInfo().getSid());
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("isDeleted", isDeleted);
        intent.putExtra("isBlocked", isBlocked);
        intent.putExtra("isCollected", isCollected);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }
}
