package com.itcalf.renhe.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.ConversationService;
import com.alibaba.wukong.im.IMEngine;
import com.alibaba.wukong.im.Message;
import com.alibaba.wukong.im.MessageContent;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.bean.ChatLog;
import com.itcalf.renhe.bean.CircleList;
import com.itcalf.renhe.bean.HlContacts;
import com.itcalf.renhe.bean.MemberCircle;
import com.itcalf.renhe.bean.MemberList;
import com.itcalf.renhe.bean.SearchGlobalBean;
import com.itcalf.renhe.bean.SearchRecommendedBean;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.archives.AddFriendAct;
import com.itcalf.renhe.context.innermsg.ReceiveFriend;
import com.itcalf.renhe.dto.ReceiveAddFriend;
import com.itcalf.renhe.utils.CheckUpgradeUtil;
import com.itcalf.renhe.utils.ContactsUtil;
import com.itcalf.renhe.utils.ConversationListUtil;
import com.itcalf.renhe.utils.DateUtil;
import com.itcalf.renhe.utils.HighlightTextUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * @author Chan
 * @description 搜索列表 二级 适配器
 * @date 2015-4-2
 */
public class FindConnectionsListitemAdapter extends BaseAdapter {

    private Context context;
    private List<SearchGlobalBean> sgbList = new ArrayList<>();
    private ImageLoader imageLoader;
    private String keyWord;
    private CheckUpgradeUtil checkUpgradeUtil;

    public FindConnectionsListitemAdapter(Context _ct, List<SearchGlobalBean> _sgbList, String keyWord) {
        this.context = _ct;
        this.sgbList = _sgbList;
        imageLoader = ImageLoader.getInstance();
        this.keyWord = keyWord;
        checkUpgradeUtil = new CheckUpgradeUtil(_ct);
    }

    @Override
    public int getCount() {
        return sgbList.size() > 0 ? sgbList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return sgbList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewhold;
        if (null == convertView) {
            viewhold = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.find_connections_list_sub_item, null);
            viewhold.findconnections_item = (RelativeLayout) convertView.findViewById(R.id.findconnections_item);
            viewhold.headImg = (ImageView) convertView.findViewById(R.id.headImage);// 头像
            viewhold.avatar_txt = (TextView) convertView.findViewById(R.id.avatar_txt);
            viewhold.nameTv = (TextView) convertView.findViewById(R.id.nameTv);// 联系人名字
            viewhold.vipIv = (ImageView) convertView.findViewById(R.id.vipImage);// Vip标志
            viewhold.realNameIv = (ImageView) convertView.findViewById(R.id.realnameImage);// 实名标志
            viewhold.rightIv = (ImageView) convertView.findViewById(R.id.rightImage);// √标志
            viewhold.friendsNumbTv = (TextView) convertView.findViewById(R.id.friendsNumbTv);//好友数量
            viewhold.infoHeadTv = (TextView) convertView.findViewById(R.id.infoHeadTv);
            viewhold.infoTv = (TextView) convertView.findViewById(R.id.infoTv);// 信息简介
            viewhold.preferTv = (TextView) convertView.findViewById(R.id.preferTv);//能提供
            viewhold.accept_btn = (Button) convertView.findViewById(R.id.accept_btn);
            viewhold.added_txt = (TextView) convertView.findViewById(R.id.added_txt);
            //人脉圈
            viewhold.connectionsCircle_item = (RelativeLayout) convertView.findViewById(R.id.connectionsCircle_item);
            viewhold.circle_headImage = (ImageView) convertView.findViewById(R.id.circle_headImage);// 头像
            viewhold.circle_nameTv = (TextView) convertView.findViewById(R.id.circle_nameTv);// 联系人名字
            viewhold.circle_vipImage = (ImageView) convertView.findViewById(R.id.circle_vipImage);// Vip标志
            viewhold.circle_realnameImage = (ImageView) convertView.findViewById(R.id.circle_realnameImage);// 实名标志
            viewhold.circle_rightImage = (ImageView) convertView.findViewById(R.id.circle_rightImage);// √标志
            viewhold.circle_createTimeTv = (TextView) convertView.findViewById(R.id.circle_createTimeTv);//好友数量
            viewhold.circle_infoTv = (TextView) convertView.findViewById(R.id.circle_infoTv);// 信息简介
            viewhold.circle_contextTv = (TextView) convertView.findViewById(R.id.circle_contextTv);// 信息简介
            viewhold.search_item_divide = convertView.findViewById(R.id.search_item_divide);//分割线

            convertView.setTag(viewhold);
        } else {
            viewhold = (ViewHolder) convertView.getTag();
        }
        if (null != sgbList && sgbList.size() > 0) {
            if (position == sgbList.size() - 1)
                viewhold.search_item_divide.setVisibility(View.GONE);
            else
                viewhold.search_item_divide.setVisibility(View.VISIBLE);
            SearchGlobalBean sgb = sgbList.get(position);
            if (sgb != null) {
                /** 类型：0 推荐；1 联系人；2 新的人脉 ；3 人脉圈； 4 聊天记录 ; 5 圈子 **/
                int type = sgb.getType();
                viewhold.infoHeadTv.setVisibility(View.GONE);
                if (type == 0) {
                    viewhold.findconnections_item.setVisibility(View.VISIBLE);
                    viewhold.connectionsCircle_item.setVisibility(View.GONE);
                    viewhold.preferTv.setVisibility(View.GONE);
                    viewhold.accept_btn.setVisibility(View.GONE);
                    viewhold.added_txt.setVisibility(View.GONE);
                    SearchRecommendedBean srb = sgb.getSearchRecommended();
                    if (srb != null) {
                        String avaterUrl = srb.getUserface();
                        try {
                            imageLoader.displayImage(avaterUrl, viewhold.headImg, CacheManager.options);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        String name = srb.getName();
                        viewhold.nameTv.setText(name);

                        int accountType = srb.getAccountType();
                        boolean isrealName = srb.isRealname();
                        switch (accountType) {
                            case 0:
                                viewhold.vipIv.setVisibility(View.GONE);
                                if (isrealName) {
                                    viewhold.rightIv.setVisibility(View.VISIBLE);
                                } else {
                                    viewhold.rightIv.setVisibility(View.GONE);
                                }
                                break;
                            case 1:
                                viewhold.vipIv.setVisibility(View.VISIBLE);
                                viewhold.vipIv.setImageResource(R.drawable.archive_vip_1);
                                break;
                            case 2:
                                viewhold.vipIv.setVisibility(View.VISIBLE);
                                viewhold.vipIv.setImageResource(R.drawable.archive_vip_2);
                                break;
                            case 3:
                                viewhold.vipIv.setVisibility(View.VISIBLE);
                                viewhold.vipIv.setImageResource(R.drawable.archive_vip_3);
                                break;
                            default:
                                break;
                        }

                        int friendsNumb = srb.getConnectionNum();
                        if (friendsNumb > 0) {
                            viewhold.friendsNumbTv.setVisibility(View.GONE);
                            viewhold.friendsNumbTv.setText("好友 " + friendsNumb);
                        } else {
                            viewhold.friendsNumbTv.setVisibility(View.GONE);
                        }
                        String pos = srb.getTitle();
                        String company = srb.getCompany();
                        viewhold.infoTv.setText(pos + "/" + company);
                    }
                } else if (type == 1) {
                    viewhold.findconnections_item.setVisibility(View.VISIBLE);
                    viewhold.connectionsCircle_item.setVisibility(View.GONE);
                    viewhold.preferTv.setVisibility(View.GONE);
                    viewhold.accept_btn.setVisibility(View.GONE);
                    viewhold.added_txt.setVisibility(View.GONE);
                    HlContacts ct = sgb.getContact();
                    if (ct != null) {
                        String name = "";
                        int accountType;
                        boolean isrealName = false;
                        String pos = null;
                        String company = null;
                        if (ct.getType() == HlContacts.HLCONTACTS_CONTACT_MEMBER_TYPE || ct.getType() == HlContacts.HLCONTACTS_CARD_MEMBER_TYPE) {
                            int colorIndex;
                            String shortName;
                            accountType = 0;
                            isrealName = false;
                            pos = null;
                            company = null;
                            if (ct.getType() == HlContacts.HLCONTACTS_CONTACT_MEMBER_TYPE) {
                                name = ct.getHlContactContactMember().getName();
                                colorIndex = ct.getHlContactContactMember().getColorIndex();
                                shortName = ct.getHlContactContactMember().getShortName();
                            } else {
                                name = ct.getHlContactCardMember().getName();
                                colorIndex = ct.getHlContactCardMember().getColorIndex();
                                shortName = ct.getHlContactCardMember().getShortName();
                            }

                            viewhold.headImg.setVisibility(View.GONE);
                            viewhold.avatar_txt.setVisibility(View.VISIBLE);
                            viewhold.avatar_txt.setBackgroundResource(Constants.AVATARBG[colorIndex]);
                            viewhold.avatar_txt.setText(shortName);
                        } else {
                            name = ct.getHlContactRenheMember().getName();
                            accountType = ct.getHlContactRenheMember().getAccountType();
                            isrealName = ct.getHlContactRenheMember().isRealname();
                            pos = ct.getHlContactRenheMember().getTitle();
                            company = ct.getHlContactRenheMember().getCompany();
                            viewhold.headImg.setVisibility(View.VISIBLE);
                            viewhold.avatar_txt.setVisibility(View.GONE);
                            String avaterUrl = ct.getHlContactRenheMember().getUserface();
                            try {
                                imageLoader.displayImage(avaterUrl, viewhold.headImg, CacheManager.options);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        SpannableString nameStr = new HighlightTextUtil().toHighlight(context, keyWord, name);
                        viewhold.nameTv.setText(nameStr);
                        switch (accountType) {
                            case 0:
                                viewhold.vipIv.setVisibility(View.GONE);
                                if (isrealName) {
                                    viewhold.rightIv.setVisibility(View.VISIBLE);
                                } else {
                                    viewhold.rightIv.setVisibility(View.GONE);
                                }
                                break;
                            case 1:
                                viewhold.vipIv.setVisibility(View.VISIBLE);
                                viewhold.vipIv.setImageResource(R.drawable.archive_vip_1);
                                break;
                            case 2:
                                viewhold.vipIv.setVisibility(View.VISIBLE);
                                viewhold.vipIv.setImageResource(R.drawable.archive_vip_2);
                                break;
                            case 3:
                                viewhold.vipIv.setVisibility(View.VISIBLE);
                                viewhold.vipIv.setImageResource(R.drawable.archive_vip_3);
                                break;
                            default:
                                break;
                        }

                        viewhold.friendsNumbTv.setVisibility(View.GONE);

                        if (TextUtils.isEmpty(pos) && TextUtils.isEmpty(company)) {
                            viewhold.infoTv.setText("");
                        } else if (TextUtils.isEmpty(pos)) {
                            SpannableString companyStr = new HighlightTextUtil().toHighlight(context, keyWord, company);
                            viewhold.infoTv.setText(companyStr);
                        } else if (TextUtils.isEmpty(company)) {
                            SpannableString posStr = new HighlightTextUtil().toHighlight(context, keyWord, pos);
                            viewhold.infoTv.setText(posStr);
                        } else {
                            SpannableString infoStr = new HighlightTextUtil().toHighlight(context, keyWord,
                                    (pos + "/" + company));
                            viewhold.infoTv.setText(infoStr);
                        }
                    }
                } else if (type == 11) {
                    viewhold.findconnections_item.setVisibility(View.VISIBLE);
                    viewhold.connectionsCircle_item.setVisibility(View.GONE);
                    viewhold.preferTv.setVisibility(View.GONE);
                    viewhold.accept_btn.setVisibility(View.GONE);
                    viewhold.added_txt.setVisibility(View.GONE);
                    CircleList ml = sgb.getCircleList();
                    if (ml != null) {
                        String avaterUrl = ml.getAvatar();
                        try {
                            imageLoader.displayImage(avaterUrl, viewhold.headImg, CacheManager.options);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        String name = ml.getName();
                        int count = ml.getMemberCount();
                        viewhold.nameTv.setText(name + "(" + count + ")");

                        List<String> includeMembers = ml.getListSearchMembers();
                        if (null != includeMembers && includeMembers.size() > 0) {
                            String members = "";
                            for (String member : includeMembers) {
                                members = members + member + " ";
                            }
                            SpannableString membersStr = new HighlightTextUtil().toHighlight(context, keyWord, members);
                            viewhold.infoTv.setVisibility(View.VISIBLE);
                            viewhold.infoHeadTv.setVisibility(View.VISIBLE);
                            viewhold.infoTv.setText(membersStr);
                        } else {
                            viewhold.infoTv.setVisibility(View.GONE);
                            viewhold.infoHeadTv.setVisibility(View.GONE);
                        }
                    }
                } else if (type == 2) {
                    viewhold.findconnections_item.setVisibility(View.VISIBLE);
                    viewhold.connectionsCircle_item.setVisibility(View.GONE);
                    final MemberList ml = sgb.getMemberList();
                    if (ml != null) {
                        final String avaterUrl = ml.getUserFace();
                        try {
                            imageLoader.displayImage(avaterUrl, viewhold.headImg, CacheManager.options);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        final String name = ml.getName();
                        SpannableString nameStr = new HighlightTextUtil().toHighlight(context, keyWord, name);
                        viewhold.nameTv.setText(nameStr);
                        int accountType = ml.getAccountType();
                        boolean isrealName = ml.isRealname();
                        switch (accountType) {
                            case 0:
                                viewhold.vipIv.setVisibility(View.GONE);
                                if (isrealName) {
                                    viewhold.rightIv.setVisibility(View.VISIBLE);
                                } else {
                                    viewhold.rightIv.setVisibility(View.GONE);
                                }
                                break;
                            case 1:
                                viewhold.vipIv.setVisibility(View.VISIBLE);
                                viewhold.vipIv.setImageResource(R.drawable.archive_vip_1);
                                break;
                            case 2:
                                viewhold.vipIv.setVisibility(View.VISIBLE);
                                viewhold.vipIv.setImageResource(R.drawable.archive_vip_2);
                                break;
                            case 3:
                                viewhold.vipIv.setVisibility(View.VISIBLE);
                                viewhold.vipIv.setImageResource(R.drawable.archive_vip_3);
                                break;
                            default:
                                break;
                        }
                        viewhold.friendsNumbTv.setVisibility(View.GONE);
                        String pos = ml.getCurTitle();
                        String company = ml.getCurCompany();
                        if (TextUtils.isEmpty(pos) && TextUtils.isEmpty(company)) {
                            viewhold.infoTv.setText("");
                        } else if (TextUtils.isEmpty(pos)) {
                            SpannableString companyStr = new HighlightTextUtil().toHighlight(context, keyWord, company);
                            viewhold.infoTv.setText(companyStr);
                        } else if (TextUtils.isEmpty(company)) {
                            SpannableString posStr = new HighlightTextUtil().toHighlight(context, keyWord, pos);
                            viewhold.infoTv.setText(posStr);
                        } else {
                            SpannableString infoStr = new HighlightTextUtil().toHighlight(context, keyWord,
                                    (pos + "/" + company));
                            viewhold.infoTv.setText(infoStr);
                        }
                        //能提供
                        ArrayList<String> perferList = ml.getPreferred();
                        String perfer = "";
                        for (int i = 0, s = perferList.size(); i < s; i++) {
                            perfer = perfer + perferList.get(i) + " ";
                        }
                        if (!TextUtils.isEmpty(perfer)) {
                            viewhold.preferTv.setVisibility(View.VISIBLE);
                            viewhold.preferTv.setText(perfer);
                        } else {
                            viewhold.preferTv.setVisibility(View.GONE);
                        }

                        boolean isConnection = ml.isConnection();
                        boolean isReceived = ml.isReceived();
                        boolean isInvite = ml.isInvite();
                        int inviteId = 0, inviteType = 0;
                        int imId = 0;
                        if (null != ml.getUserInfo() && null != ml.getUserInfo().getContactInfo()) {
                            imId = ml.getUserInfo().getContactInfo().getImId();
                        }
                        final int finalImId = imId;
                        MemberList.BeInvitedInfo beInvitedInfo = ml.getBeInvitedInfo();
                        if (null != beInvitedInfo) {
                            inviteId = beInvitedInfo.getInviteId();
                            inviteType = beInvitedInfo.getInviteType();
                        }
                        // 按钮类型
                        viewhold.accept_btn.setVisibility(View.VISIBLE);
                        viewhold.added_txt.setVisibility(View.VISIBLE);
                        int status = 4;
                        if (isConnection) {
                            status = 1;
                        } else if (isReceived) {
                            status = 6;//已添加
                        } else if (isInvite) {
                            status = 3;
                        } else if (null != beInvitedInfo && beInvitedInfo.isBeInvited()) {
                            status = 0;
                        } else if (ml.getSid().equals(RenheApplication.getInstance().getUserInfo().getSid())) {
                            status = 5;
                        }
                        switch (status) {
                            case 0:// 0代表未处理，可以接受邀请；
                                viewhold.accept_btn.setVisibility(View.VISIBLE);
                                viewhold.accept_btn.setBackgroundResource(R.drawable.blue_im_chat_bt_selected);
                                ColorStateList csl = context.getResources().getColorStateList(R.color.archive_editbt_textcolor_selected);
                                viewhold.accept_btn.setTextColor(csl);
                                viewhold.accept_btn.setText("接受");
                                viewhold.added_txt.setVisibility(View.GONE);
                                break;
                            case 1:// 1代表已处理，已接受邀请；
                                viewhold.accept_btn.setVisibility(View.VISIBLE);
                                viewhold.accept_btn.setBackgroundResource(R.drawable.blue_im_chat_bt_selected);
                                ColorStateList chatCsl = context.getResources().getColorStateList(R.color.archive_editbt_textcolor_selected);
                                viewhold.accept_btn.setTextColor(chatCsl);
                                viewhold.accept_btn.setText("聊天");
                                viewhold.added_txt.setVisibility(View.GONE);
                                break;
                            case 2:// 2代表已处理，已拒绝邀请
                                viewhold.accept_btn.setVisibility(View.GONE);
                                viewhold.added_txt.setVisibility(View.VISIBLE);
                                viewhold.added_txt.setText("已拒绝");
                                break;
                            case 3:// 3代表已添加，等待验证
                                viewhold.accept_btn.setVisibility(View.GONE);
                                viewhold.added_txt.setVisibility(View.VISIBLE);
                                viewhold.added_txt.setText("已邀请");
                                break;
                            case 4:// 4代表 添加
                                viewhold.accept_btn.setVisibility(View.VISIBLE);
                                viewhold.accept_btn.setBackgroundResource(R.drawable.btn_bp3_rectangle_shape);
                                ColorStateList csl1 = context.getResources().getColorStateList(R.color.archive_editbt_textcolor_selected);
                                viewhold.accept_btn.setTextColor(csl1);
                                viewhold.accept_btn.setText("添加");
                                viewhold.added_txt.setVisibility(View.GONE);
                                break;
                            case 5:// 5代表自己
                                viewhold.accept_btn.setVisibility(View.GONE);
                                viewhold.added_txt.setVisibility(View.GONE);
                                break;
                            case 6:// 已添加
                                viewhold.accept_btn.setVisibility(View.GONE);
                                viewhold.added_txt.setVisibility(View.VISIBLE);
                                viewhold.added_txt.setText("已添加");
                                break;
                            default:
                                break;
                        }
                        final int finalStatus = status;
                        final int finalInviteId = inviteId;
                        final int finalInviteType = inviteType;
                        viewhold.accept_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                if (finalStatus == 0) {//接受邀请
                                    acceptAction(position, finalInviteId, finalInviteType);
                                    MobclickAgent.onEvent(context, context.getResources().getString(R.string.newFriend_item_received));
                                } else if (finalStatus == 4) {//去添加
                                    inviteAction(position, ml);
                                    MobclickAgent.onEvent(context, context.getResources().getString(R.string.newFriend_item_add));
                                } else if (finalStatus == 1) {//去聊天
                                    createConversation(finalImId, name, avaterUrl);
                                }
                            }
                        });


                    }
                } else if (type == 3) {
                    viewhold.findconnections_item.setVisibility(View.GONE);
                    viewhold.connectionsCircle_item.setVisibility(View.VISIBLE);
                    viewhold.preferTv.setVisibility(View.GONE);
                    viewhold.accept_btn.setVisibility(View.GONE);
                    viewhold.added_txt.setVisibility(View.GONE);
                    MemberCircle ml = sgb.getMemberCircleList();
                    if (ml != null) {
                        String avaterUrl = ml.getUserFace();
                        try {
                            imageLoader.displayImage(avaterUrl, viewhold.circle_headImage, CacheManager.options);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        String name = ml.getName();
                        if (!TextUtils.isEmpty(name)) {
                            SpannableString nameStr = new HighlightTextUtil().toHighlight(context, keyWord, name);
                            viewhold.circle_nameTv.setText(nameStr);
                        }

                        int accountType = ml.getAccountType();
                        boolean isrealName = ml.isRealname();
                        switch (accountType) {
                            case 0:
                                viewhold.circle_vipImage.setVisibility(View.GONE);
                                if (isrealName) {
                                    viewhold.circle_realnameImage.setVisibility(View.VISIBLE);
                                } else {
                                    viewhold.circle_realnameImage.setVisibility(View.GONE);
                                }
                                break;
                            case 1:
                                viewhold.circle_vipImage.setVisibility(View.VISIBLE);
                                viewhold.circle_vipImage.setImageResource(R.drawable.archive_vip_1);
                                break;
                            case 2:
                                viewhold.circle_vipImage.setVisibility(View.VISIBLE);
                                viewhold.circle_vipImage.setImageResource(R.drawable.archive_vip_2);
                                break;
                            case 3:
                                viewhold.circle_vipImage.setVisibility(View.VISIBLE);
                                viewhold.circle_vipImage.setImageResource(R.drawable.archive_vip_3);
                                break;
                            default:
                                break;
                        }

                        long createTime = ml.getCreatedDate();
                        if (createTime > 0) {
                            DateUtil.string2Date(context, createTime, viewhold.circle_createTimeTv);
                        } else {
                            viewhold.circle_createTimeTv.setText("");
                        }

                        String pos = ml.getCurTitle();
                        String company = ml.getCurCompany();
                        if (TextUtils.isEmpty(pos) && TextUtils.isEmpty(company)) {
                            viewhold.circle_infoTv.setText("");
                        } else if (TextUtils.isEmpty(pos)) {
                            SpannableString companyStr = new HighlightTextUtil().toHighlight(context, keyWord, company);
                            viewhold.circle_infoTv.setText(companyStr);
                        } else if (TextUtils.isEmpty(company)) {
                            SpannableString posStr = new HighlightTextUtil().toHighlight(context, keyWord, pos);
                            viewhold.circle_infoTv.setText(posStr);
                        } else {
                            SpannableString infoStr = new HighlightTextUtil().toHighlight(context, keyWord,
                                    (pos + "/" + company));
                            viewhold.circle_infoTv.setText(infoStr);
                        }

                        String content = ml.getContent();
                        if (!TextUtils.isEmpty(content)) {
                            SpannableString contentStr = new HighlightTextUtil().toHighlight(context, keyWord, content);
                            viewhold.circle_contextTv.setText(contentStr);
                        }
                    }
                } else if (type == 4) {
                    //聊天记录
                    viewhold.findconnections_item.setVisibility(View.VISIBLE);
                    viewhold.connectionsCircle_item.setVisibility(View.GONE);
                    viewhold.preferTv.setVisibility(View.GONE);
                    viewhold.accept_btn.setVisibility(View.GONE);
                    viewhold.added_txt.setVisibility(View.GONE);
                    ChatLog ml = sgb.getChatLog();
                    if (ml != null) {
                        String avaterUrl = ml.getConversation().icon();
                        try {
                            imageLoader.displayImage(avaterUrl, viewhold.headImg, CacheManager.options);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        String name = ml.getConversation().title();
                        viewhold.nameTv.setText(name);

                        List<Message> includeMembers = ml.getMessage();
                        if (null != includeMembers && includeMembers.size() > 0) {
                            viewhold.infoTv.setVisibility(View.VISIBLE);
                            //只有1条数据，显示
                            String members = "";
                            if (includeMembers.size() == 1) {
                                members = ((MessageContent.TextContent) includeMembers.get(0).messageContent()).text();
                                SpannableString membersStr = new HighlightTextUtil().toHighlight(context, keyWord, members);
                                viewhold.infoTv.setText(membersStr);
                            } else {
                                members = includeMembers.size() + "条相关聊天记录";
                                viewhold.infoTv.setText(members);
                            }
                        } else {
                            viewhold.infoTv.setVisibility(View.GONE);
                        }
                    }
                } else if (type == 5) {
                    viewhold.findconnections_item.setVisibility(View.VISIBLE);
                    viewhold.connectionsCircle_item.setVisibility(View.GONE);
                    viewhold.preferTv.setVisibility(View.GONE);
                    viewhold.vipIv.setVisibility(View.GONE);
                    viewhold.realNameIv.setVisibility(View.GONE);
                    viewhold.circle_vipImage.setVisibility(View.GONE);
                    viewhold.circle_realnameImage.setVisibility(View.GONE);
                    viewhold.accept_btn.setVisibility(View.GONE);
                    viewhold.added_txt.setVisibility(View.GONE);
                    CircleList ml = sgb.getCircleList();
                    if (ml != null) {
                        String avaterUrl = ml.getAvatar();
                        try {
                            imageLoader.displayImage(avaterUrl, viewhold.headImg, CacheManager.options);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        String name = ml.getName();
                        int count = ml.getMemberCount();
                        SpannableString nameStr = new HighlightTextUtil().toHighlight(context, keyWord, name + "(" + count + ")");
                        viewhold.nameTv.setText(nameStr);

                        String note = ml.getNote();
                        if (!TextUtils.isEmpty(note)) {
                            viewhold.infoTv.setText(note);
                        } else {
                            viewhold.infoTv.setText("");
                        }
                    }
                }
            }
        }
        return convertView;
    }

    public class ViewHolder {
        RelativeLayout findconnections_item;
        ImageView headImg;
        TextView avatar_txt;
        TextView nameTv;
        ImageView realNameIv;
        ImageView vipIv;
        ImageView rightIv;
        TextView friendsNumbTv;
        TextView infoHeadTv;
        TextView infoTv;
        TextView preferTv;

        RelativeLayout connectionsCircle_item;
        ImageView circle_headImage;
        TextView circle_nameTv;
        ImageView circle_rightImage;
        ImageView circle_vipImage;
        ImageView circle_realnameImage;
        TextView circle_createTimeTv;
        TextView circle_infoTv;
        TextView circle_contextTv;

        View search_item_divide;
        Button accept_btn;
        TextView added_txt;
    }

    //接受好友邀请
    void acceptAction(final int position, int inviteId, int inviteType) {
        new ReceiveFriend(context) {
            @Override
            public void doPre() {
                ((Activity) context).showDialog(2);
            }

            @Override
            public void doPost(ReceiveAddFriend result) {
                ((Activity) context).removeDialog(2);
                if (result == null) {
                    ToastUtil.showErrorToast(context, context.getString(R.string.connect_server_error));
                } else if (result.getState() == 1) {
                    sgbList.get(position).getMemberList().setIsReceived(true);
                    notifyDataSetChanged();
                    //通知人脉列表更新
                    new ContactsUtil(context).SyncContacts();
                    checkUpgradeUtil.checkUpgrade();//检测是否要提醒升级的弹框
                } else if (result.getState() == -1) {
                    ToastUtil.showErrorToast(context, R.string.lack_of_privilege);
                } else if (result.getState() == -2) {
                    ToastUtil.showErrorToast(context, R.string.sorry_of_unknow_exception);
                } else if (result.getState() == -3) {
                    ToastUtil.showErrorToast(context, "邀请序号不存在！");
                } else if (result.getState() == -4) {
                    ToastUtil.showErrorToast(context, "邀请类型不存在！");
                } else if (result.getState() == -5) {
                    ToastUtil.showErrorToast(context, "接受类型不存在！");
                } else if (result.getState() == -6) {
                    ToastUtil.showErrorToast(context, R.string.no_permission_do);
                } else if (result.getState() == -7) {
                    ToastUtil.showErrorToast(context, "您已经通过该请求了！");
                } else if (result.getState() == -8) {
                    ToastUtil.showErrorToast(context, "您已经拒绝过该请求！");
                }
            }
        }.executeOnExecutor(Executors.newCachedThreadPool(), "" + inviteId, "" + inviteType, "true");
    }

    //去添加
    void inviteAction(final int position, MemberList ml) {
        Intent i = new Intent(context, AddFriendAct.class);
        i.putExtra("mSid", ml.getSid());
        i.putExtra("friendName", ml.getName());
        i.putExtra("addfriend_from", "renmaiSearchResultMore");//人脉搜索的更多页面
        i.putExtra("position", position);
        context.startActivity(i);
        ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    /**
     * 创建IM会话
     */
    private void createConversation(int imId, String name, final String userFace) {
        if (imId <= 0)
            return;
        com.itcalf.renhe.context.wukong.im.RenheIMUtil.showProgressDialog(context, R.string.conversation_creating);
        // 会话标题,注意：单聊的话title,icon默认均为对方openid,传入的值无效
        final StringBuffer title = new StringBuffer();
        title.append(name);
        com.alibaba.wukong.im.Message message = null; // 创建会话发送的系统消息,可以不设置
        int convType = Conversation.ConversationType.CHAT; // 会话类型：单聊or群聊
        // 创建会话
        IMEngine.getIMService(ConversationService.class).createConversation(new com.alibaba.wukong.Callback<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                //单聊，给conversation添加扩展字段，存放聊天双发的头像姓名，方便聊天列表页获取
                ConversationListUtil.updateChatConversationExtension(conversation, title.toString(), userFace);
                com.itcalf.renhe.context.wukong.im.RenheIMUtil.dismissProgressDialog();
                Intent intent = new Intent(context, com.itcalf.renhe.context.wukong.im.ChatMainActivity.class);
                intent.putExtra("conversation", conversation);
                intent.putExtra("userName", title.toString());
                intent.putExtra("userFace", userFace);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }

            @Override
            public void onException(String code, String reason) {
                com.itcalf.renhe.context.wukong.im.RenheIMUtil.dismissProgressDialog();
                Toast.makeText(context, "创建会话失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(Conversation data, int progress) {
            }
        }, title.toString(), userFace, message, convType, Long.parseLong(imId + ""));
    }
}
