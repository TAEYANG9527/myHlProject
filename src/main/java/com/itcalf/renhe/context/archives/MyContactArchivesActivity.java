package com.itcalf.renhe.context.archives;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.bean.HlContactCardMember;
import com.itcalf.renhe.bean.HlContactContactMember;
import com.itcalf.renhe.bean.HlContacts;
import com.itcalf.renhe.bean.MobileCardContact;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.command.IContactCommand;
import com.itcalf.renhe.context.contacts.NewFriendsAct;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.context.upgrade.UpgradeActivity;
import com.itcalf.renhe.context.wukong.im.MobileContactChatActivity;
import com.itcalf.renhe.dto.ContactList;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.po.Contact;
import com.itcalf.renhe.utils.ContentUtil;
import com.itcalf.renhe.utils.DensityUtil;
import com.itcalf.renhe.utils.HlContactsUtils;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.utils.WriteContactsUtil;
import com.itcalf.renhe.utils.WriteLogThread;
import com.itcalf.renhe.utils.http.okhttp.OkHttpClientManager;
import com.itcalf.renhe.view.Button;
import com.itcalf.renhe.view.TextView;
import com.itcalf.renhe.zxing.card.DeleteVCardsTask;
import com.itcalf.widget.scrollview.ScrollViewX;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.okhttp.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * description :我的手机，名片联系人档案界面
 * Created by Chans Renhenet
 * 2015/8/5
 */
public class MyContactArchivesActivity extends BaseActivity implements ScrollViewX.OnScrollListener {

    public static String FLAG_INTENT_DELETE_ID = "friendId";
    public static String FLAG_INTENT_TYPE = "type";
    public static String FLAG_INTENT_BIZID = "bizId";
    public static String FLAG_INTENT_BIZTYPE = "bizType";
    private LinearLayout btnLl;
    private Button callBtn, sendMsgBtn;
    private ScrollViewX mScrollView;
    private TextView avatarTxt;
    private TextView nameTv;
    private ImageView identityIv;
    private TextView companyTv;
    private TextView industryTv;
    private LinearLayout mContactLayout;
    private ImageView corverIv;

    private String contactId;
    private String name, company, industry;
    private int vCard;
    private String vCardContent;
    /**
     * 1.手机通讯录好友；2.名片好友；
     */
    private int type;
    private String mobile;
    private String[] tels;
    private String[] emails;
    private String shortName;
    private int colorIndex;
    private String cover;

    private int coverHeight;

    private PopupWindow pop;
    private View popView;
    private ListView listView;
    private PupAdapter pupAdapter;
    private List<PupBean> list;
    private IContactCommand contactCommand;

    private String from;
    private String bizSId;
    private int position, fromType, bizId, bizType, friendId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ActivityTemplate().doInActivity(this, R.layout.mycontact_archives_detail);
    }

    @Override
    protected void findView() {
        super.findView();
        callBtn = (Button) findViewById(R.id.call_btn);
        sendMsgBtn = (Button) findViewById(R.id.send_msg_btn);
        btnLl = (LinearLayout) findViewById(R.id.btn_ll);
        mScrollView = (ScrollViewX) findViewById(R.id.scrollView);
        btnLl.setVisibility(View.GONE);
        mScrollView.setVisibility(View.GONE);
        avatarTxt = (TextView) findViewById(R.id.avatar_txt);
        nameTv = (TextView) findViewById(R.id.nameTv);
        identityIv = (ImageView) findViewById(R.id.identity_iv);
        companyTv = (TextView) findViewById(R.id.companyTv);
        industryTv = (TextView) findViewById(R.id.industryTv);
        corverIv = (ImageView) findViewById(R.id.corver_iv);
        mContactLayout = (LinearLayout) findViewById(R.id.contactLayout_other);
        mContactLayout.removeAllViews();
    }

    @Override
    protected void initData() {
        super.initData();
        setTextValue(R.id.title_txt, "联系人");

        list = new ArrayList<>();
        pupAdapter = new PupAdapter();
        PupBean addBean = new PupBean("存入通讯录", R.drawable.icon_save_maillist);
        list.add(addBean);
        PupBean inviteBean = new PupBean("邀请好友", R.drawable.icon_im_add);
        list.add(inviteBean);
        PupBean deleteBean = new PupBean("删除好友", R.drawable.archive_more_delete_friend);
        list.add(deleteBean);
        pupAdapter.notifyDataSetChanged();

        callBtn.setText("打电话");
        callBtn.setVisibility(View.GONE);
        sendMsgBtn.setText("聊天");
        coverHeight = DensityUtil.dip2px(this, 270);

        Intent intent = getIntent();
        HlContacts ct;
        from = intent.getStringExtra("from");
        if (!TextUtils.isEmpty(from) && from.equals("newFriendsAct")) {
            fromType = intent.getIntExtra(FLAG_INTENT_TYPE, 0);//1.名片，2.手机通讯录
            type = fromType == 1 ? 2 : 1;//切换 1.手机通讯录好友；2.名片好友；
            position = intent.getIntExtra("position", 0);
            bizId = intent.getIntExtra(FLAG_INTENT_BIZID, 0);
            bizType = intent.getIntExtra(FLAG_INTENT_BIZTYPE, 0);
            friendId = intent.getIntExtra(FLAG_INTENT_DELETE_ID, 0);
        } else {
            ct = (HlContacts) intent.getSerializableExtra("contact");
            if (null != ct) {
                int hlContactsType = ct.getType();
                switch (hlContactsType) {
                    case HlContacts.HLCONTACTS_RENHE_MEMBER_TYPE:
                        fromType = 3;
                        bizSId = ct.getHlContactRenheMember().getSid();
                        break;
                    case HlContacts.HLCONTACTS_CONTACT_MEMBER_TYPE:
                        fromType = 1;
                        bizSId = ct.getHlContactContactMember().getSid();
                        break;
                    case HlContacts.HLCONTACTS_CARD_MEMBER_TYPE:
                        vCard = ct.getHlContactCardMember().getCardId();
                        fromType = 2;
                        bizSId = ct.getHlContactCardMember().getSid();
                        break;
                }

                type = fromType;
                contactId = bizSId;
            }
        }
        getArchivesInfo(fromType, bizId, bizSId);
    }

    void showView(Contact ct) {
        if (null != ct) {
            if (type == 1) {
                //标识手机
                setTextValue(R.id.title_txt, "手机联系人");
                identityIv.setImageDrawable(getResources().getDrawable(R.drawable.icon_detail_phone));
            } else {
                setTextValue(R.id.title_txt, "名片联系人");
                identityIv.setImageDrawable(getResources().getDrawable(R.drawable.icon_detail_vcard));
            }

            btnLl.setVisibility(View.VISIBLE);
            mScrollView.setVisibility(View.VISIBLE);
            name = ct.getName();
            company = ct.getCompany();
            industry = ct.getJob();
            mobile = ct.getMobile();
            colorIndex = ct.getColorIndex();
            shortName = ct.getShortName();

            cover = ct.getCover();
            if (!TextUtils.isEmpty(cover)) {
                try {
                    ImageLoader.getInstance().displayImage(cover, corverIv, CacheManager.coverOptions);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (!TextUtils.isEmpty(ct.getTel()) && ct.getTel().length() > 0) {
                if (ct.getTel().contains(";")) {
                    tels = ct.getTel().split(";");
                } else {
                    tels = new String[]{ct.getTel()};
                }
            }
            String mEmail = ct.getEmail();
            if (!TextUtils.isEmpty(mEmail) && mEmail.length() > 0) {
                if (mEmail.contains(";")) {
                    emails = mEmail.split(";");
                } else {
                    emails = new String[]{mEmail};
                }
            }

            nameTv.setText(name);
            companyTv.setVisibility(TextUtils.isEmpty(company) ? View.GONE : View.VISIBLE);
            companyTv.setText(company);
            industryTv.setVisibility(TextUtils.isEmpty(industry) ? View.GONE : View.VISIBLE);
            industryTv.setText(industry);

            //手机
            if (!TextUtils.isEmpty(mobile)) {
                final View contactInfoView = LayoutInflater.from(this).inflate(R.layout.mycontact_info, null);
                ((android.widget.TextView) contactInfoView.findViewById(R.id.titleTv)).setText("手机");
                ((android.widget.TextView) contactInfoView.findViewById(R.id.valueTv)).setText(mobile);
                contactInfoView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        new CallPopupWindows(MyContactArchivesActivity.this, contactInfoView, mobile);
                    }
                });
                if ((null != tels && tels.length > 0) || (null != emails && emails.length > 0)
                        || !TextUtils.isEmpty(vCardContent)) {
                    contactInfoView.findViewById(R.id.contact_seperate).setVisibility(View.VISIBLE);
                } else {
                    contactInfoView.findViewById(R.id.contact_seperate).setVisibility(View.GONE);
                }
                mContactLayout.addView(contactInfoView);
            }

            avatarTxt.setText(shortName);
            avatarTxt.setBackgroundResource(Constants.AVATARBG[colorIndex]);
//            if (type == 1) {
//                //其他电话
//                if (null != tels && tels.length > 0) {
//                    for (int i = 0, telsLength = tels.length; i < telsLength; i++) {
//                        final String tel = tels[i];
//                        final View contactInfoView = LayoutInflater.from(this).inflate(R.layout.mycontact_info, null);
//                        ((android.widget.TextView) contactInfoView.findViewById(R.id.titleTv)).setText("手机");
//                        ((android.widget.TextView) contactInfoView.findViewById(R.id.valueTv)).setText(tel);
//                        contactInfoView.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View arg0) {
//                                new CallPopupWindows(MyContactArchivesActivity.this, contactInfoView, tel);
//                            }
//                        });
//                        if (i == telsLength - 1) {
//                            contactInfoView.findViewById(R.id.contact_seperate).setVisibility(View.GONE);
//                        } else {
//                            contactInfoView.findViewById(R.id.contact_seperate).setVisibility(View.VISIBLE);
//                        }
//                        mContactLayout.addView(contactInfoView);
//                    }
//                }
//                //邮箱
//                if (null != emails && emails.length > 0) {
//                    for (int i = 0, emailsLength = emails.length; i < emailsLength; i++) {
//                        String email = emails[i];
//                        final View contactInfoView = LayoutInflater.from(this).inflate(R.layout.mycontact_info, null);
//                        ((android.widget.TextView) contactInfoView.findViewById(R.id.titleTv)).setText("邮箱");
//                        ((android.widget.TextView) contactInfoView.findViewById(R.id.valueTv)).setText(email);
//                        if (i == emailsLength - 1) {
//                            contactInfoView.findViewById(R.id.contact_seperate).setVisibility(View.GONE);
//                        } else {
//                            contactInfoView.findViewById(R.id.contact_seperate).setVisibility(View.VISIBLE);
//                        }
//                        mContactLayout.addView(contactInfoView);
//                    }
//                }
//            } else if (type == 2) {
            vCardContent = ct.getVcardContent();
            if (!TextUtils.isEmpty(vCardContent)) {
                Gson gson = new GsonBuilder().create();
                if (!(vCardContent.startsWith("{") && vCardContent.endsWith("}"))) {
                    vCardContent = "{" + "\"detail\"" + ":" + vCardContent + "}";
                }
                ContactList.Member result = gson.fromJson(vCardContent, ContactList.Member.class);
                if (result != null) {
                    ContactList.Member.MobileDetail[] mbs = result.getDetail();
                    if (null != mbs && mbs.length > 0) {
                        for (int i = 0, mbsLength = mbs.length; i < mbsLength; i++) {
                            ContactList.Member.MobileDetail mb = mbs[i];
                            final View contactInfoView = LayoutInflater.from(this).inflate(R.layout.mycontact_info, null);
                            ((android.widget.TextView) contactInfoView.findViewById(R.id.titleTv)).setText(TextUtils.isEmpty(mb.getSubject()) ? "手机" : mb.getSubject());
                            ((android.widget.TextView) contactInfoView.findViewById(R.id.valueTv)).setText(mb.getContent());
                            if (i == mbsLength - 1) {
                                contactInfoView.findViewById(R.id.contact_seperate).setVisibility(View.GONE);
                            } else {
                                contactInfoView.findViewById(R.id.contact_seperate).setVisibility(View.VISIBLE);
                            }
                            mContactLayout.addView(contactInfoView);
                            //提取邮箱
                            if (!TextUtils.isEmpty(mb.getSubject()) && mb.getSubject().equals("邮箱")) {
                                emails = new String[]{mb.getContent()};
                            }
                        }
                    }
                }
            }
//            }
        }
    }

    @Override
    protected void initListener() {
        super.initListener();
        mScrollView.setOnScrollListener(this);

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(mobile)) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mobile));
                    startActivity(intent);
                }
            }
        });
        sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //					Uri uri = Uri.parse("smsto:" + mobile);
                //					Intent it = new Intent(Intent.ACTION_SENDTO, uri);
                //					startActivity(it);
                if (!TextUtils.isEmpty(mobile)) {
                    if (type == 1) {
                        new WriteLogThread(MyContactArchivesActivity.this, "5.403", null).start();
                    } else if (type == 2) {
                        new WriteLogThread(MyContactArchivesActivity.this, "5.404", null).start();
                    }
                    Intent intent = new Intent(MyContactArchivesActivity.this, MobileContactChatActivity.class);
                    intent.putExtra("name", name);
                    intent.putExtra("type", type);
                    intent.putExtra("mobile", mobile);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onScrollChanged(int x, int y, int oldX, int oldY) {

    }

    @Override
    public void onScrollStopped() {

    }

    @Override
    public void onScrolling() {
        //		if (mScrollView.getScrollY() >= coverHeight) {
        //			getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.archive_actionbar_bcg_shape));
        //		} else {
        //			getSupportActionBar().setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
        //		}
    }

    public class CallPopupWindows extends PopupWindow {

        public CallPopupWindows(final Context mContext, View parent, final String mobile) {
            final View view = View.inflate(mContext, R.layout.item_popupwindows, null);
            view.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_ins));
            LinearLayout ll_popup = (LinearLayout) view.findViewById(R.id.ll_popup);
            ll_popup.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.push_bottom_in_2));

            setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            ColorDrawable cd = new ColorDrawable(-0000);
            setBackgroundDrawable(cd);
            // setBackgroundDrawable(new BitmapDrawable());
            setFocusable(true);
            setOutsideTouchable(true);
            setContentView(view);
            showAtLocation(parent, Gravity.BOTTOM, 0, 0);
            update();
            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {

                    int height = view.findViewById(R.id.ll_popup).getTop();
                    int y = (int) event.getY();
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (y < height) {
                            dismiss();
                        }
                    }
                    return true;
                }
            });
            android.widget.Button bt1 = (android.widget.Button) view.findViewById(R.id.item_popupwindows_camera);
            android.widget.Button bt2 = (android.widget.Button) view.findViewById(R.id.item_popupwindows_Photo);
            android.widget.Button bt3 = (android.widget.Button) view.findViewById(R.id.item_popupwindows_cancel);
            bt1.setText("拨打电话");
            bt2.setText("发送短信");
            bt3.setText("复制");
            bt1.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mobile));
                    mContext.startActivity(intent);
                    dismiss();
                }
            });
            bt2.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Uri uri = Uri.parse("smsto:" + mobile);
                    Intent it = new Intent(Intent.ACTION_SENDTO, uri);
                    startActivity(it);
                    dismiss();
                }
            });
            bt3.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ContentUtil.copy(mobile, mContext);
                    ContentUtil.showToast(mContext, "内容已经复制到剪贴板");
                    dismiss();
                }
            });
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem moreItem = menu.findItem(R.id.archive_more);
        moreItem.setTitle("更多");
        moreItem.setVisible(true);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.archive_more:
                if (pop == null) {
                    createPopupwindow();
                }
                if (pop.isShowing()) {
                    pop.dismiss();
                    return true;
                }
                Rect frame = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
                int statusBarHeight = frame.top;
                pop.showAtLocation(popView, Gravity.RIGHT | Gravity.TOP, 20,
                        getSupportActionBar().getHeight() + statusBarHeight);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private PopupWindow createPopupwindow() {
        LayoutInflater inflater = LayoutInflater.from(this);
        popView = inflater.inflate(R.layout.popupwindow_add_layout, null);
        popView.getBackground().setAlpha(230);
        listView = (ListView) popView.findViewById(R.id.lv_popupwindow_add);
        pop = new PopupWindow(popView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        //pop.setAnimationStyle(R.style.popwin_anim_style);
        pop.setBackgroundDrawable(new BitmapDrawable());
        pop.setOutsideTouchable(true);
        listView.setAdapter(pupAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        //付费会员的权限
                        if (RenheApplication.getInstance().getUserInfo().getAccountType() > 0) {
                            Contact contact = new Contact();
                            contact.setName(name);
                            contact.setMobile(mobile);
                            if (null != emails && emails.length > 0)
                                contact.setEmail(emails[0]);
                            if (!TextUtils.isEmpty(company))
                                contact.setCompany(company);
                            AddFriendToMailList(MyContactArchivesActivity.this, contact);
                        } else {
                            MaterialDialogsUtil materialDialog = new MaterialDialogsUtil(MyContactArchivesActivity.this);
                            materialDialog.getNotitleBuilder(R.string.upgrade_account_dialog, R.string.upgrade_account_dialog_ok, R.string.upgrade_account_dialog_cancel).callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    super.onPositive(dialog);
                                    startActivity(new Intent(MyContactArchivesActivity.this, UpgradeActivity.class));
                                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                                }

                                @Override
                                public void onNegative(MaterialDialog dialog) {
                                    super.onNegative(dialog);
                                }
                            });
                            materialDialog.show();
                        }
                        break;
                    case 1:
                        if (!TextUtils.isEmpty(mobile)) {
                            if (!TextUtils.isEmpty(from) && from.equals("newFriendsAct")) {
                                //加新的好友界面过来的名片，人脉数据
                                addContact();
                            } else {
                                //调接口发短信
                                sendMessage();
                            }
                        } else {
                            ToastUtil.showErrorToast(MyContactArchivesActivity.this, R.string.error_mobile_format);
                        }
                        new WriteLogThread(MyContactArchivesActivity.this, "5.405", null).start();
                        break;
                    case 2:
                        if (!TextUtils.isEmpty(from) && from.equals("newFriendsAct")) {
                            //删除从新的好友传过来的名片，人脉数据
                            deleteNewFriend();
                        } else {
                            deleteFriend();
                        }
                        break;
                }
                if (pop != null) {
                    pop.dismiss();
                }
            }
        });
        return pop;
    }

    class PupAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(MyContactArchivesActivity.this).inflate(R.layout.item_popup_add_layout, parent,
                    false);
            android.widget.TextView textView = (android.widget.TextView) convertView.findViewById(R.id.tv_title);
            PupBean bean = list.get(position);
            textView.setText(bean.title);
            textView.setCompoundDrawablesWithIntrinsicBounds(bean.icon, 0, 0, 0);
            return convertView;
        }
    }

    class PupBean {
        public String title;
        public int icon;

        public PupBean(String title, int icon) {
            this.title = title;
            this.icon = icon;
        }

    }

    private void AddFriendToMailList(final Context context, final Contact contact) {
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
                                ToastUtil.showToast(context, "保存失败");
                                break;
                            case 1:
                                //保存成功
                                ToastUtil.showToast(context, "保存成功");
                                break;
                        }
                    }
                });
            }
        }).start();
    }

    private void addContact() {
        //添加名片/手机通讯录好友
        Map<String, Object> reqParams = new HashMap<>();
        reqParams.put("sid", RenheApplication.getInstance().getUserInfo().getSid());
        reqParams.put("adSId", RenheApplication.getInstance().getUserInfo().getAdSId());
        reqParams.put("bizType", bizType);
        reqParams.put("bizId", bizId);
        try {
            OkHttpClientManager.postAsyn(Constants.Http.INVITE_CARD_MOBILE_FRIEND, reqParams, MessageBoardOperation.class, new OkHttpClientManager.ResultCallback() {
                @Override
                public void onError(Request request, Exception e) {
                    ToastUtil.showConnectError(MyContactArchivesActivity.this);
                }

                @Override
                public void onResponse(Object response) {
                    MessageBoardOperation result = (MessageBoardOperation) response;
                    if (null != result) {
                        switch (result.getState()) {
                            case 1:
                                // 通知新的好友界面更新
                                Intent intent = new Intent(NewFriendsAct.UPDATE_LISTITEM);
                                intent.putExtra("position", position);
                                sendBroadcast(intent);
                                ToastUtil.showToast(MyContactArchivesActivity.this, R.string.send_success);
                                break;
                            default:
                                ToastUtil.showToast(MyContactArchivesActivity.this, R.string.send_failed);
                                break;
                        }
                    }
                }
            }, this.getClass().getSimpleName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteNewFriend() {
        MaterialDialogsUtil materialDialogsUtil = new MaterialDialogsUtil(this);
        materialDialogsUtil.getBuilder("确定要和" + nameTv.getText().toString() + "解除好友关系吗？")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        Map<String, Object> reqParams = new HashMap<>();
                        reqParams.put("sid", RenheApplication.getInstance().getUserInfo().getSid());
                        reqParams.put("adSId", RenheApplication.getInstance().getUserInfo().getAdSId());
                        reqParams.put("friendId", friendId);
                        try {
                            OkHttpClientManager.postAsyn(Constants.Http.DELETE_CARD_MOBILE_FRIEND, reqParams, MessageBoardOperation.class, new OkHttpClientManager.ResultCallback() {
                                @Override
                                public void onError(Request request, Exception e) {
                                    ToastUtil.showConnectError(MyContactArchivesActivity.this);
                                }

                                @Override
                                public void onResponse(Object response) {
                                    MessageBoardOperation result = (MessageBoardOperation) response;
                                    if (null != result) {
                                        switch (result.getState()) {
                                            case 1:
                                                // 通知新的好友界面更新
                                                Intent intent = new Intent(NewFriendsAct.UPDATE_DELETE_LIST_ITEM);
                                                intent.putExtra("position", position);
                                                sendBroadcast(intent);
                                                //通知人脉列表刷新
//                                                new ContactsUtil(MyContactArchivesActivity.this).SyncContacts();
                                                ToastUtil.showToast(MyContactArchivesActivity.this, "删除成功");
                                                finish();
                                                break;
                                            default:
                                                ToastUtil.showToast(MyContactArchivesActivity.this, "删除失败");
                                                break;
                                        }
                                    }
                                }
                            }, this.getClass().getSimpleName());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                    }
                }).cancelable(false);
        materialDialogsUtil.show();

    }

    private void sendMessage() {
        new AsyncTask<String, Void, MessageBoardOperation>() {
            @Override
            protected MessageBoardOperation doInBackground(String... params) {
                Map<String, Object> reqParams = new HashMap<>();
                reqParams.put("sid", params[0]);
                reqParams.put("adSId", params[1]);
                reqParams.put("mobile", params[2]);
                reqParams.put("userName", params[3]);
                try {
                    return (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.INVITE_MOBILE_CONTACTS,
                            reqParams, MessageBoardOperation.class, null);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(MessageBoardOperation result) {
                super.onPostExecute(result);
                if (result != null) {
                    switch (result.getState()) {
                        case 1:
                            ToastUtil.showToast(MyContactArchivesActivity.this, "已发送短信邀请");
                            new WriteLogThread(MyContactArchivesActivity.this, "5.405.1", null).start();
                            break;
                        case -2:
                            ToastUtil.showToast(MyContactArchivesActivity.this, "手机号码为空");
                            break;
                        case -4:
                            ToastUtil.showToast(MyContactArchivesActivity.this, "手机号码格式不对");
                            break;
                        case -5:
                            ToastUtil.showToast(MyContactArchivesActivity.this, "你今天已经邀请过好友");
                            break;
                        default:
                            break;
                    }
                }
            }
        }.executeOnExecutor(Executors.newCachedThreadPool(),
                RenheApplication.getInstance().getUserInfo().getSid(),
                RenheApplication.getInstance().getUserInfo().getAdSId(), mobile, name);
    }

    private void deleteFriend() {
        MaterialDialogsUtil materialDialogsUtil = new MaterialDialogsUtil(this);
        materialDialogsUtil.getBuilder("确定要和" + nameTv.getText().toString() + "解除好友关系吗？")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        //删除本地数据库的数据
                        contactCommand = ((RenheApplication) getApplication()).getContactCommand();
                        if (type == 1) {
                            new DeleteMobileFriendTask().executeOnExecutor(Executors.newCachedThreadPool(),
                                    RenheApplication.getInstance().getUserInfo().getSid(),
                                    RenheApplication.getInstance().getUserInfo().getAdSId(), contactId);
                        } else if (type == 2) {
                            //名片
                            new DeleteVCardsTask(new DeleteVCardsTask.TaskListener() {
                                @Override
                                public void postExecute(int state) {
                                    if (state == 1) {
                                        try {
                                            HlContactsUtils.deleteHlContactMemberBySid(HlContactCardMember.class, String.valueOf(vCard));
//                                            contactCommand.deleteMyVCardContactBySid(
//                                                    RenheApplication.getInstance().getUserInfo().getSid(), vCard);
//                                            Intent brocastIntent = new Intent(
//                                                    Constants.BroadCastAction.REFRESH_CONTACT_RECEIVER_ACTION);
//                                            sendBroadcast(brocastIntent);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        } finally {
                                            finish();
                                        }
                                    } else {
                                        ToastUtil.showToast(MyContactArchivesActivity.this, getString(R.string.vcard_deletefail));
                                    }
                                }
                            }).executeOnExecutor(Executors.newCachedThreadPool(),
                                    RenheApplication.getInstance().getUserInfo().getSid(),
                                    RenheApplication.getInstance().getUserInfo().getAdSId(), String.valueOf(vCard));
                        }
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                    }
                }).cancelable(false);
        materialDialogsUtil.show();
    }

    class DeleteMobileFriendTask extends AsyncTask<String, Void, MessageBoardOperation> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected MessageBoardOperation doInBackground(String... params) {
            try {
                Map<String, Object> reqParams = new HashMap<String, Object>();
                reqParams.put("sid", params[0]);
                reqParams.put("adSId", params[1]);
                reqParams.put("contactSId", params[2]);
                MessageBoardOperation info = (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.DELETE_MOBILE_FRIEND,
                        reqParams, MessageBoardOperation.class, null);
                return info;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(MessageBoardOperation result) {
            super.onPostExecute(result);
            if (null != result) {
                switch (result.getState()) {
                    case 1:
                        try {
//                            contactCommand.deleteMyContactBySid(RenheApplication.getInstance().getUserInfo().getSid(), contactId);
//                            Intent brocastIntent = new Intent(Constants.BroadCastAction.REFRESH_CONTACT_RECEIVER_ACTION);
//                            sendBroadcast(brocastIntent);
                            HlContactsUtils.deleteHlContactMemberBySid(HlContactContactMember.class, contactId);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            finish();
                        }

                        break;
                    case -1:
                        ToastUtil.showErrorToast(MyContactArchivesActivity.this, "权限不足");
                        break;
                    case -2:
                        ToastUtil.showErrorToast(MyContactArchivesActivity.this, "发生未知错误");
                        break;
                    case -3:
                        ToastUtil.showErrorToast(MyContactArchivesActivity.this, "不存在");
                        break;
                    default:
                        break;

                }

            }
        }
    }

    /**
     * 获取信息
     */
    void getArchivesInfo(int type, int bizId, String bizSId) {
        Map<String, Object> reqParams = new HashMap<>();
        reqParams.put("sid", RenheApplication.getInstance().getUserInfo().getSid());
        reqParams.put("adSId", RenheApplication.getInstance().getUserInfo().getAdSId());
        if (!TextUtils.isEmpty(from) && from.equals("newFriendsAct")) {
            reqParams.put("type", type);
            reqParams.put("bizId", bizId);
        } else {
            //本地数据库存的类型：1为手机联系人；2为名片联系人，而本接口需上传的参数（ 1.名片，2.手机通讯录，3、人和网会员）正好相反
            int upType = type == 1 ? 2 : 1;
            reqParams.put("type", upType);
            reqParams.put("bizSId", bizSId);
        }
        try {
            showLoadingDialog();
            OkHttpClientManager.postAsyn(Constants.Http.GET_CARD_MOBILE_INFO, reqParams, MobileCardContact.class, new OkHttpClientManager.ResultCallback() {
                @Override
                public void onError(Request request, Exception e) {
                    hideLoadingDialog();
                    ToastUtil.showConnectError(MyContactArchivesActivity.this);
                }

                @Override
                public void onResponse(Object response) {
                    MobileCardContact result = (MobileCardContact) response;
                    if (null != result) {
                        switch (result.getState()) {
                            case 1:
                                ContactList.Member ml = result.getUserInfo();
                                if (null != ml) {
                                    Contact ct = new Contact();
                                    ct.setName(ml.getName());
                                    ct.setJob(ml.getTitle());
                                    ct.setCompany(ml.getCompany());
                                    ct.setShortName(ml.getShortName());
                                    ct.setColorIndex(ml.getColorIndex());
                                    ct.setMobile(ml.getMobile());
                                    ct.setCover(ml.getCover());
                                    ContactList.Member.MobileDetail[] md = ml.getDetail();
                                    Gson gson = new GsonBuilder().create();
                                    String vCardContact = gson.toJson(md);
                                    ct.setVcardContent(vCardContact);
                                    showView(ct);
                                }
                                break;
                            default:
                                ToastUtil.showErrorToast(MyContactArchivesActivity.this, getString(R.string.user_not_exist));
                                finish();
                                break;
                        }
                    } else {
                        ToastUtil.showErrorToast(MyContactArchivesActivity.this, getString(R.string.user_not_exist));
                        finish();
                    }
                    hideLoadingDialog();
                }
            }, MyContactArchivesActivity.this.getClass().getSimpleName());
        } catch (Exception e) {
            e.printStackTrace();
            hideLoadingDialog();
        }
    }
}
