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
import android.widget.LinearLayout;
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
import com.itcalf.renhe.context.contacts.SearchForContactsActivity;
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
 * description :找人脉 ListItem 优化版
 * Created by Chans Renhenet
 * 2015/8/6
 */
public class SearchForContactAdapter extends BaseAdapter {

    private Context context;
    private ImageLoader imageLoader;
    private List<SearchGlobalBean> fcList = new ArrayList<>();
    //搜索关键字
    private String keyword = "";
    private CheckUpgradeUtil checkUpgradeUtil;

    public SearchForContactAdapter(Context context, List<SearchGlobalBean> fcList) {
        this.context = context;
        this.fcList = fcList;
        imageLoader = ImageLoader.getInstance();
        checkUpgradeUtil = new CheckUpgradeUtil(context);
    }

    @Override
    public int getCount() {
        return fcList.size();
    }

    @Override
    public Object getItem(int i) {
        return fcList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        viewHolderer viewHolderer;
        if (convertView == null) {
            viewHolderer = new viewHolderer();
            convertView = LayoutInflater.from(context).inflate(R.layout.searchforcontact_list_item, null);
            viewHolderer.titleLl = (LinearLayout) convertView.findViewById(R.id.title_ll);
            viewHolderer.titleTxt = (TextView) convertView.findViewById(R.id.title_txt);

            viewHolderer.moreLl = (LinearLayout) convertView.findViewById(R.id.loadmore_Ll);
            viewHolderer.loadMoreTxt = (TextView) convertView.findViewById(R.id.loadmore_txt);

            viewHolderer.itemLl = (LinearLayout) convertView.findViewById(R.id.findconnections_item_ll);
            //联系人
            viewHolderer.findconnections_item = (RelativeLayout) convertView.findViewById(R.id.findconnections_item);
            viewHolderer.headImg = (ImageView) convertView.findViewById(R.id.headImage);// 头像
            viewHolderer.avatar_txt = (TextView) convertView.findViewById(R.id.avatar_txt);
            viewHolderer.nameTv = (TextView) convertView.findViewById(R.id.nameTv);// 联系人名字

            viewHolderer.vipIv = (ImageView) convertView.findViewById(R.id.vipImage);// Vip标志
            viewHolderer.realNameIv = (ImageView) convertView.findViewById(R.id.realnameImage);// 实名标志
            viewHolderer.rightIv = (ImageView) convertView.findViewById(R.id.rightImage);// √标志

            viewHolderer.friendsNumbTv = (TextView) convertView.findViewById(R.id.friendsNumbTv);//好友数量
            viewHolderer.infoHeadTv = (TextView) convertView.findViewById(R.id.infoHeadTv);
            viewHolderer.infoTv = (TextView) convertView.findViewById(R.id.infoTv);// 信息简介
            viewHolderer.preferTv = (TextView) convertView.findViewById(R.id.preferTv);//能提供
            viewHolderer.accept_btn = (Button) convertView.findViewById(R.id.accept_btn);
            viewHolderer.added_txt = (TextView) convertView.findViewById(R.id.added_txt);
            //人脉圈
            viewHolderer.connectionsCircle_item = (RelativeLayout) convertView.findViewById(R.id.connectionsCircle_item);
            viewHolderer.circle_headImage = (ImageView) convertView.findViewById(R.id.circle_headImage);// 头像
            viewHolderer.circle_nameTv = (TextView) convertView.findViewById(R.id.circle_nameTv);// 联系人名字

            viewHolderer.circle_vipImage = (ImageView) convertView.findViewById(R.id.circle_vipImage);// Vip标志
            viewHolderer.circle_realnameImage = (ImageView) convertView.findViewById(R.id.circle_realnameImage);// 实名标志
            viewHolderer.circle_rightImage = (ImageView) convertView.findViewById(R.id.circle_rightImage);// √标志

            viewHolderer.circle_createTimeTv = (TextView) convertView.findViewById(R.id.circle_createTimeTv);//好友数量
            viewHolderer.circle_infoTv = (TextView) convertView.findViewById(R.id.circle_infoTv);// 信息简介
            viewHolderer.circle_contextTv = (TextView) convertView.findViewById(R.id.circle_contextTv);// 信息简介

            viewHolderer.search_item_divide = convertView.findViewById(R.id.search_item_divide);
            convertView.setTag(viewHolderer);
        } else {
            viewHolderer = (viewHolderer) convertView.getTag();
        }
        //获取搜索关键字
        keyword = ((SearchForContactsActivity) context).getKeyword();
        if (position >= 0) {
            SearchGlobalBean searchBean = fcList.get(position);
            if (null != searchBean) {
                final int type = searchBean.getType();
                final String typeName = searchBean.getTypeName();
                boolean isTitle = searchBean.isTitle();
                boolean isFooter = searchBean.isFooter();
                if (isTitle) {
                    viewHolderer.titleLl.setVisibility(View.VISIBLE);
                    viewHolderer.moreLl.setVisibility(View.GONE);
                    viewHolderer.itemLl.setVisibility(View.GONE);
                    viewHolderer.titleTxt.setText(typeName);
                } else if (isFooter) {
                    viewHolderer.titleLl.setVisibility(View.GONE);
                    viewHolderer.moreLl.setVisibility(View.VISIBLE);
                    viewHolderer.itemLl.setVisibility(View.GONE);
                    viewHolderer.loadMoreTxt.setText(typeName);
                } else {
                    viewHolderer.search_item_divide.setVisibility(searchBean.isShowDivideLine() ? View.GONE : View.VISIBLE);

                    viewHolderer.titleLl.setVisibility(View.GONE);
                    viewHolderer.moreLl.setVisibility(View.GONE);
                    viewHolderer.itemLl.setVisibility(View.VISIBLE);

                    viewHolderer.infoHeadTv.setVisibility(View.GONE);
                    if (type == 0) {
                        viewHolderer.findconnections_item.setVisibility(View.VISIBLE);
                        viewHolderer.connectionsCircle_item.setVisibility(View.GONE);
                        viewHolderer.preferTv.setVisibility(View.GONE);
                        viewHolderer.accept_btn.setVisibility(View.GONE);
                        viewHolderer.added_txt.setVisibility(View.GONE);
                        SearchRecommendedBean srb = searchBean.getSearchRecommended();
                        if (srb != null) {
                            String avaterUrl = srb.getUserface();
                            try {
                                imageLoader.displayImage(avaterUrl, viewHolderer.headImg, CacheManager.options);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            String name = srb.getName();
                            viewHolderer.nameTv.setText(name);

                            int accountType = srb.getAccountType();
                            boolean isrealName = srb.isRealname();
                            switch (accountType) {
                                case 0:
                                    viewHolderer.vipIv.setVisibility(View.GONE);
                                    if (isrealName) {
                                        viewHolderer.rightIv.setVisibility(View.VISIBLE);
                                    } else {
                                        viewHolderer.rightIv.setVisibility(View.GONE);
                                    }
                                    break;
                                case 1:
                                    viewHolderer.vipIv.setVisibility(View.VISIBLE);
                                    viewHolderer.vipIv.setImageResource(R.drawable.archive_vip_1);
                                    break;
                                case 2:
                                    viewHolderer.vipIv.setVisibility(View.VISIBLE);
                                    viewHolderer.vipIv.setImageResource(R.drawable.archive_vip_2);
                                    break;
                                case 3:
                                    viewHolderer.vipIv.setVisibility(View.VISIBLE);
                                    viewHolderer.vipIv.setImageResource(R.drawable.archive_vip_3);
                                    break;
                                default:
                                    break;
                            }

                            int friendsNumb = srb.getConnectionNum();
                            if (friendsNumb > 0) {
                                viewHolderer.friendsNumbTv.setVisibility(View.GONE);
                                viewHolderer.friendsNumbTv.setText("好友 " + friendsNumb);
                            } else {
                                viewHolderer.friendsNumbTv.setVisibility(View.GONE);
                            }
                            String pos = srb.getTitle();
                            String company = srb.getCompany();
                            viewHolderer.infoTv.setText(pos + "/" + company);
                        }
                    } else if (type == 1) {
                        viewHolderer.findconnections_item.setVisibility(View.VISIBLE);
                        viewHolderer.connectionsCircle_item.setVisibility(View.GONE);
                        viewHolderer.preferTv.setVisibility(View.GONE);
                        viewHolderer.accept_btn.setVisibility(View.GONE);
                        viewHolderer.added_txt.setVisibility(View.GONE);
                        HlContacts ct = searchBean.getContact();
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

                                viewHolderer.headImg.setVisibility(View.GONE);
                                viewHolderer.avatar_txt.setVisibility(View.VISIBLE);
                                viewHolderer.avatar_txt.setBackgroundResource(Constants.AVATARBG[colorIndex]);
                                viewHolderer.avatar_txt.setText(shortName);
                            } else {
                                name = ct.getHlContactRenheMember().getName();
                                accountType = ct.getHlContactRenheMember().getAccountType();
                                isrealName = ct.getHlContactRenheMember().isRealname();
                                pos = ct.getHlContactRenheMember().getTitle();
                                company = ct.getHlContactRenheMember().getCompany();
                                viewHolderer.headImg.setVisibility(View.VISIBLE);
                                viewHolderer.avatar_txt.setVisibility(View.GONE);
                                String avaterUrl = ct.getHlContactRenheMember().getUserface();
                                try {
                                    imageLoader.displayImage(avaterUrl, viewHolderer.headImg, CacheManager.options);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            SpannableString nameStr = new HighlightTextUtil().toHighlight(context, keyword, name);
                            viewHolderer.nameTv.setText(nameStr);
                            switch (accountType) {
                                case 0:
                                    viewHolderer.vipIv.setVisibility(View.GONE);
                                    if (isrealName) {
                                        viewHolderer.rightIv.setVisibility(View.VISIBLE);
                                    } else {
                                        viewHolderer.rightIv.setVisibility(View.GONE);
                                    }
                                    break;
                                case 1:
                                    viewHolderer.vipIv.setVisibility(View.VISIBLE);
                                    viewHolderer.vipIv.setImageResource(R.drawable.archive_vip_1);
                                    break;
                                case 2:
                                    viewHolderer.vipIv.setVisibility(View.VISIBLE);
                                    viewHolderer.vipIv.setImageResource(R.drawable.archive_vip_2);
                                    break;
                                case 3:
                                    viewHolderer.vipIv.setVisibility(View.VISIBLE);
                                    viewHolderer.vipIv.setImageResource(R.drawable.archive_vip_3);
                                    break;
                                default:
                                    break;
                            }

                            viewHolderer.friendsNumbTv.setVisibility(View.GONE);
                            if (TextUtils.isEmpty(pos) && TextUtils.isEmpty(company)) {
                                viewHolderer.infoTv.setText("");
                            } else if (TextUtils.isEmpty(pos)) {
                                SpannableString companyStr = new HighlightTextUtil().toHighlight(context, keyword, company);
                                viewHolderer.infoTv.setText(companyStr);
                            } else if (TextUtils.isEmpty(company)) {
                                SpannableString posStr = new HighlightTextUtil().toHighlight(context, keyword, pos);
                                viewHolderer.infoTv.setText(posStr);
                            } else {
                                SpannableString infoStr = new HighlightTextUtil().toHighlight(context, keyword,
                                        (pos + "/" + company));
                                viewHolderer.infoTv.setText(infoStr);
                            }
                        }
                    } else if (type == 11) {
                        viewHolderer.findconnections_item.setVisibility(View.VISIBLE);
                        viewHolderer.connectionsCircle_item.setVisibility(View.GONE);
                        viewHolderer.preferTv.setVisibility(View.GONE);
                        viewHolderer.accept_btn.setVisibility(View.GONE);
                        viewHolderer.added_txt.setVisibility(View.GONE);
                        CircleList ml = searchBean.getCircleList();
                        if (ml != null) {
                            String avaterUrl = ml.getAvatar();
                            try {
                                imageLoader.displayImage(avaterUrl, viewHolderer.headImg, CacheManager.options);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            String name = ml.getName();
                            int count = ml.getMemberCount();
                            viewHolderer.nameTv.setText(name + "(" + count + ")");

                            List<String> includeMembers = ml.getListSearchMembers();
                            if (null != includeMembers && includeMembers.size() > 0) {
                                String members = "";
                                for (String member : includeMembers) {
                                    members = members + member + " ";
                                }
                                SpannableString membersStr = new HighlightTextUtil().toHighlight(context, keyword, members);
                                viewHolderer.infoTv.setVisibility(View.VISIBLE);
                                viewHolderer.infoHeadTv.setVisibility(View.VISIBLE);
                                viewHolderer.infoTv.setText(membersStr);
                            } else {
                                viewHolderer.infoTv.setVisibility(View.GONE);
                                viewHolderer.infoHeadTv.setVisibility(View.GONE);
                            }
                        }
                    } else if (type == 2) {
                        viewHolderer.findconnections_item.setVisibility(View.VISIBLE);
                        viewHolderer.connectionsCircle_item.setVisibility(View.GONE);
                        viewHolderer.accept_btn.setVisibility(View.VISIBLE);
                        viewHolderer.added_txt.setVisibility(View.VISIBLE);
                        final MemberList ml = searchBean.getMemberList();
                        if (ml != null) {
                            final String avaterUrl = ml.getUserFace();
                            try {
                                imageLoader.displayImage(avaterUrl, viewHolderer.headImg, CacheManager.options);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            final String name = ml.getName();
                            SpannableString nameStr = new HighlightTextUtil().toHighlight(context, keyword, name);
                            viewHolderer.nameTv.setText(nameStr);

                            int accountType = ml.getAccountType();
                            boolean isrealName = ml.isRealname();
                            switch (accountType) {
                                case 0:
                                    viewHolderer.vipIv.setVisibility(View.GONE);
                                    if (isrealName) {
                                        viewHolderer.rightIv.setVisibility(View.VISIBLE);
                                    } else {
                                        viewHolderer.rightIv.setVisibility(View.GONE);
                                    }
                                    break;
                                case 1:
                                    viewHolderer.vipIv.setVisibility(View.VISIBLE);
                                    viewHolderer.vipIv.setImageResource(R.drawable.archive_vip_1);
                                    break;
                                case 2:
                                    viewHolderer.vipIv.setVisibility(View.VISIBLE);
                                    viewHolderer.vipIv.setImageResource(R.drawable.archive_vip_2);
                                    break;
                                case 3:
                                    viewHolderer.vipIv.setVisibility(View.VISIBLE);
                                    viewHolderer.vipIv.setImageResource(R.drawable.archive_vip_3);
                                    break;
                                default:
                                    break;
                            }

                            viewHolderer.friendsNumbTv.setVisibility(View.GONE);
                            String pos = ml.getCurTitle();
                            String company = ml.getCurCompany();
                            if (TextUtils.isEmpty(pos) && TextUtils.isEmpty(company)) {
                                viewHolderer.infoTv.setText("");
                            } else if (TextUtils.isEmpty(pos)) {
                                SpannableString companyStr = new HighlightTextUtil().toHighlight(context, keyword, company);
                                viewHolderer.infoTv.setText(companyStr);
                            } else if (TextUtils.isEmpty(company)) {
                                SpannableString posStr = new HighlightTextUtil().toHighlight(context, keyword, pos);
                                viewHolderer.infoTv.setText(posStr);
                            } else {
                                SpannableString infoStr = new HighlightTextUtil().toHighlight(context, keyword,
                                        (pos + "/" + company));
                                viewHolderer.infoTv.setText(infoStr);
                            }
                            //能提供
                            ArrayList<String> perferList = ml.getPreferred();
                            String perfer = "";
                            for (int i = 0, s = perferList.size(); i < s; i++) {
                                perfer = perfer + perferList.get(i) + " ";
                            }
                            if (!TextUtils.isEmpty(perfer)) {
                                viewHolderer.preferTv.setVisibility(View.VISIBLE);
                                viewHolderer.preferTv.setText(perfer);
                            } else {
                                viewHolderer.preferTv.setVisibility(View.GONE);
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
                            viewHolderer.accept_btn.setVisibility(View.VISIBLE);
                            viewHolderer.added_txt.setVisibility(View.VISIBLE);
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
                                    viewHolderer.accept_btn.setVisibility(View.VISIBLE);
                                    viewHolderer.accept_btn.setBackgroundResource(R.drawable.blue_im_chat_bt_selected);
                                    ColorStateList csl = context.getResources().getColorStateList(R.color.archive_editbt_textcolor_selected);
                                    viewHolderer.accept_btn.setTextColor(csl);
                                    viewHolderer.accept_btn.setText("接受");
                                    viewHolderer.added_txt.setVisibility(View.GONE);
                                    break;
                                case 1:// 1代表已处理，已接受邀请；
                                    viewHolderer.accept_btn.setVisibility(View.VISIBLE);
                                    viewHolderer.accept_btn.setBackgroundResource(R.drawable.blue_im_chat_bt_selected);
                                    ColorStateList chatCsl = context.getResources().getColorStateList(R.color.archive_editbt_textcolor_selected);
                                    viewHolderer.accept_btn.setTextColor(chatCsl);
                                    viewHolderer.accept_btn.setText("聊天");
                                    viewHolderer.added_txt.setVisibility(View.GONE);
                                    break;
                                case 2:// 2代表已处理，已拒绝邀请
                                    viewHolderer.accept_btn.setVisibility(View.GONE);
                                    viewHolderer.added_txt.setVisibility(View.VISIBLE);
                                    viewHolderer.added_txt.setText("已拒绝");
                                    break;
                                case 3:// 3代表已添加，等待验证
                                    viewHolderer.accept_btn.setVisibility(View.GONE);
                                    viewHolderer.added_txt.setVisibility(View.VISIBLE);
                                    viewHolderer.added_txt.setText("已邀请");
                                    break;
                                case 4:// 4代表 添加
                                    viewHolderer.accept_btn.setVisibility(View.VISIBLE);
                                    viewHolderer.accept_btn.setBackgroundResource(R.drawable.btn_bp3_rectangle_shape);
                                    ColorStateList csl1 = context.getResources().getColorStateList(R.color.archive_editbt_textcolor_selected);
                                    viewHolderer.accept_btn.setTextColor(csl1);
                                    viewHolderer.accept_btn.setText("添加");
                                    viewHolderer.added_txt.setVisibility(View.GONE);
                                    break;
                                case 5:// 5代表自己
                                    viewHolderer.accept_btn.setVisibility(View.GONE);
                                    viewHolderer.added_txt.setVisibility(View.GONE);
                                    break;
                                case 6:// 已添加
                                    viewHolderer.accept_btn.setVisibility(View.GONE);
                                    viewHolderer.added_txt.setVisibility(View.VISIBLE);
                                    viewHolderer.added_txt.setText("已添加");
                                    break;
                                default:
                                    break;
                            }
                            final int finalStatus = status;
                            final int finalInviteId = inviteId;
                            final int finalInviteType = inviteType;
                            viewHolderer.accept_btn.setOnClickListener(new View.OnClickListener() {
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
                        viewHolderer.findconnections_item.setVisibility(View.GONE);
                        viewHolderer.connectionsCircle_item.setVisibility(View.VISIBLE);
                        viewHolderer.preferTv.setVisibility(View.GONE);
                        viewHolderer.accept_btn.setVisibility(View.GONE);
                        viewHolderer.added_txt.setVisibility(View.GONE);
                        MemberCircle ml = searchBean.getMemberCircleList();
                        if (ml != null) {
                            String avaterUrl = ml.getUserFace();
                            try {
                                imageLoader.displayImage(avaterUrl, viewHolderer.circle_headImage, CacheManager.options);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            String name = ml.getName();
                            if (!TextUtils.isEmpty(name)) {
                                SpannableString nameStr = new HighlightTextUtil().toHighlight(context, keyword, name);
                                viewHolderer.circle_nameTv.setText(nameStr);
                            }

                            int accountType = ml.getAccountType();
                            boolean isrealName = ml.isRealname();
                            switch (accountType) {
                                case 0:
                                    viewHolderer.circle_vipImage.setVisibility(View.GONE);
                                    if (isrealName) {
                                        viewHolderer.circle_realnameImage.setVisibility(View.VISIBLE);
                                    } else {
                                        viewHolderer.circle_realnameImage.setVisibility(View.GONE);
                                    }
                                    break;
                                case 1:
                                    viewHolderer.circle_vipImage.setVisibility(View.VISIBLE);
                                    viewHolderer.circle_vipImage.setImageResource(R.drawable.archive_vip_1);
                                    break;
                                case 2:
                                    viewHolderer.circle_vipImage.setVisibility(View.VISIBLE);
                                    viewHolderer.circle_vipImage.setImageResource(R.drawable.archive_vip_2);
                                    break;
                                case 3:
                                    viewHolderer.circle_vipImage.setVisibility(View.VISIBLE);
                                    viewHolderer.circle_vipImage.setImageResource(R.drawable.archive_vip_3);
                                    break;
                                default:
                                    break;
                            }

                            long createTime = ml.getCreatedDate();
                            if (createTime > 0) {
                                DateUtil.string2Date(context, createTime, viewHolderer.circle_createTimeTv);
                            } else {
                                viewHolderer.circle_createTimeTv.setText("");
                            }

                            String pos = ml.getCurTitle();
                            String company = ml.getCurCompany();
                            if (TextUtils.isEmpty(pos) && TextUtils.isEmpty(company)) {
                                viewHolderer.circle_infoTv.setText("");
                            } else if (TextUtils.isEmpty(pos)) {
                                SpannableString companyStr = new HighlightTextUtil().toHighlight(context, keyword, company);
                                viewHolderer.circle_infoTv.setText(companyStr);
                            } else if (TextUtils.isEmpty(company)) {
                                SpannableString posStr = new HighlightTextUtil().toHighlight(context, keyword, pos);
                                viewHolderer.circle_infoTv.setText(posStr);
                            } else {
                                SpannableString infoStr = new HighlightTextUtil().toHighlight(context, keyword,
                                        (pos + "/" + company));
                                viewHolderer.circle_infoTv.setText(infoStr);
                            }

                            String content = ml.getContent();
                            if (!TextUtils.isEmpty(content)) {
                                SpannableString contentStr = new HighlightTextUtil().toHighlight(context, keyword, content);
                                viewHolderer.circle_contextTv.setText(contentStr);
                            }
                        }
                    } else if (type == 4) {
                        //聊天记录
                        viewHolderer.findconnections_item.setVisibility(View.VISIBLE);
                        viewHolderer.connectionsCircle_item.setVisibility(View.GONE);
                        viewHolderer.preferTv.setVisibility(View.GONE);
                        viewHolderer.accept_btn.setVisibility(View.GONE);
                        viewHolderer.added_txt.setVisibility(View.GONE);
                        ChatLog ml = searchBean.getChatLog();
                        if (ml != null) {
                            String avaterUrl = ml.getConversation().icon();
                            try {
                                imageLoader.displayImage(avaterUrl, viewHolderer.headImg, CacheManager.options);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            String name = ml.getConversation().title();
                            viewHolderer.nameTv.setText(name);

                            List<Message> includeMembers = ml.getMessage();
                            if (null != includeMembers && includeMembers.size() > 0) {
                                viewHolderer.infoTv.setVisibility(View.VISIBLE);
                                //只有1条数据，显示
                                String members;
                                if (includeMembers.size() == 1) {
                                    members = ((MessageContent.TextContent) includeMembers.get(0).messageContent()).text();
                                    SpannableString membersStr = new HighlightTextUtil().toHighlight(context, keyword, members);
                                    viewHolderer.infoTv.setText(membersStr);
                                } else {
                                    members = includeMembers.size() + "条相关聊天记录";
                                    viewHolderer.infoTv.setText(members);
                                }
                            } else {
                                viewHolderer.infoTv.setVisibility(View.GONE);
                            }
                        }
                    } else if (type == 5) {
                        viewHolderer.findconnections_item.setVisibility(View.VISIBLE);
                        viewHolderer.connectionsCircle_item.setVisibility(View.GONE);
                        viewHolderer.preferTv.setVisibility(View.GONE);
                        viewHolderer.accept_btn.setVisibility(View.GONE);
                        viewHolderer.added_txt.setVisibility(View.GONE);
                        viewHolderer.circle_vipImage.setVisibility(View.GONE);
                        viewHolderer.vipIv.setVisibility(View.GONE);
                        viewHolderer.circle_realnameImage.setVisibility(View.GONE);
                        viewHolderer.realNameIv.setVisibility(View.GONE);
                        CircleList ml = searchBean.getCircleList();
                        if (ml != null) {
                            String avaterUrl = ml.getAvatar();
                            try {
                                imageLoader.displayImage(avaterUrl, viewHolderer.headImg, CacheManager.options);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            String name = ml.getName();
                            int count = ml.getMemberCount();
                            SpannableString nameStr = new HighlightTextUtil().toHighlight(context, keyword, name + "(" + count + ")");
                            viewHolderer.nameTv.setText(nameStr);

                            String note = ml.getNote();
                            if (!TextUtils.isEmpty(note)) {
                                viewHolderer.infoTv.setText(note);
                            } else {
                                viewHolderer.infoTv.setText("");
                            }
                        }
                    }
                }
            }
        }
        return convertView;
    }

    public class viewHolderer {
        //头部布局
        LinearLayout titleLl;
        TextView titleTxt;
        //加载更多
        LinearLayout moreLl;
        TextView loadMoreTxt;
        //item布局
        LinearLayout itemLl;
        //联系人
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
        //圈子
        RelativeLayout connectionsCircle_item;
        ImageView circle_headImage;
        TextView circle_nameTv;
        ImageView circle_rightImage;
        ImageView circle_vipImage;
        ImageView circle_realnameImage;
        TextView circle_createTimeTv;
        TextView circle_infoTv;
        TextView circle_contextTv;
        //分割线
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
                    fcList.get(position).getMemberList().setIsReceived(true);
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
        i.putExtra("addfriend_from", "renmaiSearchResult");//人脉搜索的页面
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
