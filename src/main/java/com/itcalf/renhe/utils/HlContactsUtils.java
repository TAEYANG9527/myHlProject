package com.itcalf.renhe.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.ConversationService;
import com.alibaba.wukong.im.IMEngine;
import com.alibaba.wukong.im.Message;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.bean.HlContactCardMember;
import com.itcalf.renhe.bean.HlContactContactMember;
import com.itcalf.renhe.bean.HlContactRenheMember;
import com.itcalf.renhe.bean.HlContacts;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.archives.MyContactArchivesActivity;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.hlinterface.ContactInterface;
import com.itcalf.renhe.eventbusbean.ContactDeleteOrAndEvent;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import cn.renhe.heliao.idl.contact.ContactList;

/**
 * 人脉列表工具类，提供人脉本地数据的增删改查等操作
 * Created by wangning on 2016/3/8.
 */
public class HlContactsUtils {
    public static final String DEFAULT_INITIAL = "#";//人脉联系人名字默认首字母
    public static final String OFTEN_USED_DEFAULT_INITIAL = "常用联系人";//常用联系人

    /**
     * 根据通过grpc返回的人脉列表初始化一个HlContacts集合
     *
     * @param response grpc返回的人脉列表
     * @return
     */
    public static void generateHlContactsList(final ContactList.ContactListResponse response,
                                              final Map<String, List<HlContacts>> hlContactsMap,
                                              final List<HlContacts> contactses,
                                              final List<HlContactRenheMember> hlContactRenheMemberList,
                                              final List<HlContactContactMember> hlContactContactMemberList,
                                              final List<HlContactCardMember> hlContactCardMemberList,
                                              final List<HlContactRenheMember> needUpdateRenheMemberList,
                                              final List<HlContactRenheMember> needDeleteRenheMemberList,
                                              final List<HlContactContactMember> needDeleteMobileMemberList,
                                              final List<HlContactCardMember> needDeleteCardMemberList, final ContactInterface contactInterface) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                List<ContactList.RenheMember> hlGrpcContactRenheMembers = response.getRenheMemberList();
                List<ContactList.ContactMember> hlGrpcContactMobileMembers = response.getContactMemberList();
                List<ContactList.CardMember> hlGrpcContactCardMembers = response.getCardMemberList();
                Logger.d("人和好友数量--->" + hlGrpcContactRenheMembers.size());
                Logger.d("通讯录好友数量--->" + hlGrpcContactMobileMembers.size());
                Logger.d("名片好友数量--->" + hlGrpcContactCardMembers.size());
                List<ContactList.RenheMember> hlNeedUpdateGrpcContactRenheMembers = response.getUpdateRenheMemberList();//需要更新的人和网会员
                List<String> hlNeedDeleteGrpcContactRenheMembers = response.getDeleteRenheMemberList();
                List<String> hlNeedDeleteGrpcContactMobileMembers = response.getDeleteContactMemberList();
                List<String> hlNeedDeleteGrpcContactCardMembers = response.getDeleteCardMemberList();
                for (ContactList.RenheMember renheMember : hlGrpcContactRenheMembers) {
                    HlContacts hlContacts = new HlContacts();
                    HlContactRenheMember hlContactRenheMember = generateHlContactRenheMember(renheMember);
                    if (!isMemberExist(hlContactRenheMember, hlContactsMap)) {
                        hlContacts.setHlContactRenheMember(hlContactRenheMember);
                        hlContacts.setType(HlContacts.HLCONTACTS_RENHE_MEMBER_TYPE);
                        contactses.add(hlContacts);
                        hlContactRenheMemberList.add(hlContactRenheMember);
                    }
                }
                for (ContactList.ContactMember contactMember : hlGrpcContactMobileMembers) {
                    HlContacts hlContacts = new HlContacts();
                    HlContactContactMember hlContactContactMember = generateHlContactContactMember(contactMember);
                    hlContacts.setType(HlContacts.HLCONTACTS_CONTACT_MEMBER_TYPE);
                    hlContacts.setHlContactContactMember(hlContactContactMember);
                    contactses.add(hlContacts);
                    hlContactContactMemberList.add(hlContactContactMember);
                }
                for (ContactList.CardMember cardMember : hlGrpcContactCardMembers) {
                    HlContacts hlContacts = new HlContacts();
                    HlContactCardMember hlContactCardMember = generateHlContactCardMember(cardMember);
                    hlContacts.setType(HlContacts.HLCONTACTS_CARD_MEMBER_TYPE);
                    hlContacts.setHlContactCardMember(hlContactCardMember);
                    contactses.add(hlContacts);
                    hlContactCardMemberList.add(hlContactCardMember);
                }
                for (ContactList.RenheMember updateRenheMember : hlNeedUpdateGrpcContactRenheMembers) {
                    HlContactRenheMember hlContactRenheMember = generateHlContactRenheMember(updateRenheMember);
                    needUpdateRenheMemberList.add(hlContactRenheMember);
                }
                for (String deleteRenheMemberSid : hlNeedDeleteGrpcContactRenheMembers) {
                    HlContactRenheMember hlContactRenheMember = new HlContactRenheMember();
                    hlContactRenheMember.setOwnerSid(RenheApplication.getInstance().getUserInfo().getSid());
                    hlContactRenheMember.setSid(deleteRenheMemberSid);
                    needDeleteRenheMemberList.add(hlContactRenheMember);
                }
                for (String deleteMobileMemberSid : hlNeedDeleteGrpcContactMobileMembers) {
                    HlContactContactMember hlContactContactMember = new HlContactContactMember();
                    hlContactContactMember.setOwnerSid(RenheApplication.getInstance().getUserInfo().getSid());
                    hlContactContactMember.setSid(deleteMobileMemberSid);
                    needDeleteMobileMemberList.add(hlContactContactMember);
                }
                for (String deleteCardMemberSid : hlNeedDeleteGrpcContactCardMembers) {
                    HlContactCardMember hlContactCardMember = new HlContactCardMember();
                    hlContactCardMember.setOwnerSid(RenheApplication.getInstance().getUserInfo().getSid());
                    hlContactCardMember.setSid(deleteCardMemberSid);
                    needDeleteCardMemberList.add(hlContactCardMember);
                }
                contactInterface.onGenerateHlContactsListSuccess();
            }
        });
        thread.start();
    }

    public static Map<String, List<HlContacts>> generateHlContactMap(Map<String, List<HlContacts>> hlContactsMap,
                                                                     List<HlContacts> contactses) {
        if (null == contactses)
            return null;
        if (null == hlContactsMap)
            hlContactsMap = new TreeMap<>();
        //生成联系人以首字母分类的treeMap
        for (HlContacts hlContacts : contactses) {
            int type = hlContacts.getType();
            switch (type) {
                case HlContacts.HLCONTACTS_RENHE_MEMBER_TYPE:
                    List<HlContacts> hlRenheContactsesOfInitial = hlContactsMap.get(hlContacts.getHlContactRenheMember().getInitial());
                    if (null == hlRenheContactsesOfInitial) {
                        hlRenheContactsesOfInitial = new ArrayList<>();
                    }
                    hlRenheContactsesOfInitial.add(hlContacts);
                    hlContactsMap.put(hlContacts.getHlContactRenheMember().getInitial(), hlRenheContactsesOfInitial);
                    break;
                case HlContacts.HLCONTACTS_CONTACT_MEMBER_TYPE:
                    List<HlContacts> hlMobileContactsesOfInitial = hlContactsMap.get(hlContacts.getHlContactContactMember().getInitial());
                    if (null == hlMobileContactsesOfInitial) {
                        hlMobileContactsesOfInitial = new ArrayList<>();
                    }
                    hlMobileContactsesOfInitial.add(hlContacts);
                    hlContactsMap.put(hlContacts.getHlContactContactMember().getInitial(), hlMobileContactsesOfInitial);
                    break;
                case HlContacts.HLCONTACTS_CARD_MEMBER_TYPE:
                    List<HlContacts> hlCardContactsesOfInitial = hlContactsMap.get(hlContacts.getHlContactCardMember().getInitial());
                    if (null == hlCardContactsesOfInitial) {
                        hlCardContactsesOfInitial = new ArrayList<>();
                    }
                    hlCardContactsesOfInitial.add(hlContacts);
                    hlContactsMap.put(hlContacts.getHlContactCardMember().getInitial(), hlCardContactsesOfInitial);
                    break;
            }

        }
        return hlContactsMap;
    }

    /**
     * 由于服务端返回的 ”不是人和网好友的人和网会员“ 有可能有重复，所以针对这部分人要做个去重
     *
     * @param hlContactRenheMember 不是人和网好友的人和网会员
     * @return
     */
    public static boolean isMemberExist(HlContactRenheMember hlContactRenheMember, Map<String, List<HlContacts>> hlContactsMap) {
        if (null == hlContactRenheMember)
            return false;
        if (hlContactRenheMember.isFriend())//人和网好友不可能重复，所以不再判断，默认不存在重复
            return false;
        if (null == hlContactsMap || hlContactsMap.isEmpty())
            return false;
        List<HlContacts> hlRenheContactsesOfInitial = hlContactsMap.get(hlContactRenheMember.getInitial());
        if (null == hlRenheContactsesOfInitial || hlRenheContactsesOfInitial.isEmpty())
            return false;
        for (HlContacts hlContacts : hlRenheContactsesOfInitial) {
            if (hlContacts.getType() == HlContacts.HLCONTACTS_RENHE_MEMBER_TYPE) {
                if (hlContactRenheMember.getSid().equals(hlContacts.getHlContactRenheMember().getSid())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 根据grpc返回的人和好友生成一个本地类型的人和好友
     *
     * @param renheMember grpc返回的人和好友
     * @return
     */
    public static HlContactRenheMember generateHlContactRenheMember(ContactList.RenheMember renheMember) {
        HlContactRenheMember hlContactRenheMember = new HlContactRenheMember();
        hlContactRenheMember.setOwnerSid(RenheApplication.getInstance().getUserInfo().getSid());
        hlContactRenheMember.setSid(renheMember.getSid());
        hlContactRenheMember.setName(renheMember.getName());
        hlContactRenheMember.setAccountType(renheMember.getAccountType());
        hlContactRenheMember.setCompany(renheMember.getCompany());
        hlContactRenheMember.setImId(renheMember.getImId());
        hlContactRenheMember.setImValid(renheMember.getImValid());
        hlContactRenheMember.setRealname(renheMember.getIsRealname());
        hlContactRenheMember.setMobile(renheMember.getMobile());
        hlContactRenheMember.setTel(renheMember.getTel());
        hlContactRenheMember.setTitle(renheMember.getTitle());
        hlContactRenheMember.setUserface(renheMember.getUserface());
        hlContactRenheMember.setFriend(renheMember.getIsFriend());
        if (TextUtils.isEmpty(renheMember.getName())) {
            hlContactRenheMember.setName(DEFAULT_INITIAL);
        }
        if (!TextUtils.isEmpty(renheMember.getName())) {
            String fullPinYin = PinyinUtil.cn2Spell(hlContactRenheMember.getName());
            if (!TextUtils.isEmpty(fullPinYin)) {
                hlContactRenheMember.setFullPinYin(fullPinYin);
            } else {
                hlContactRenheMember.setFullPinYin(DEFAULT_INITIAL);
            }
            String initialOfFullPinYin = PinyinUtil.cn2FirstSpell(hlContactRenheMember.getName());
            if (!TextUtils.isEmpty(initialOfFullPinYin)) {
                hlContactRenheMember.setInitialOfFullPinYin(initialOfFullPinYin);
            } else {
                hlContactRenheMember.setInitialOfFullPinYin(DEFAULT_INITIAL);
            }
            if (hlContactRenheMember.getInitialOfFullPinYin().length() > 0)
                hlContactRenheMember.setInitial(hlContactRenheMember.getInitialOfFullPinYin().substring(0, 1).toUpperCase());
            else
                hlContactRenheMember.setInitial(DEFAULT_INITIAL);
        }
        return hlContactRenheMember;
    }

    /**
     * 根据grpc返回的通讯录好友生成一个本地类型的通讯录好友
     *
     * @param contactMember grpc返回的通讯录好友
     * @return
     */
    public static HlContactContactMember generateHlContactContactMember(ContactList.ContactMember contactMember) {
        HlContactContactMember hlContactContactMember = new HlContactContactMember();
        hlContactContactMember.setOwnerSid(RenheApplication.getInstance().getUserInfo().getSid());
        hlContactContactMember.setMobile(contactMember.getMobile());
        hlContactContactMember.setName(contactMember.getName());
        hlContactContactMember.setSid(contactMember.getSid());
        hlContactContactMember.setShortName(contactMember.getShortName());
        hlContactContactMember.setColorIndex(contactMember.getColorIndex());
        if (TextUtils.isEmpty(contactMember.getName())) {
            hlContactContactMember.setName(DEFAULT_INITIAL);
        }
        String fullPinYin = PinyinUtil.cn2Spell(hlContactContactMember.getName());
        if (!TextUtils.isEmpty(fullPinYin)) {
            hlContactContactMember.setFullPinYin(fullPinYin);
        } else {
            hlContactContactMember.setFullPinYin(DEFAULT_INITIAL);
        }
        String initialOfFullPinYin = PinyinUtil.cn2FirstSpell(hlContactContactMember.getName());
        if (!TextUtils.isEmpty(initialOfFullPinYin)) {
            hlContactContactMember.setInitialOfFullPinYin(initialOfFullPinYin);
        } else {
            hlContactContactMember.setInitialOfFullPinYin(DEFAULT_INITIAL);
        }

        if (hlContactContactMember.getInitialOfFullPinYin().length() > 0)
            hlContactContactMember.setInitial(hlContactContactMember.getInitialOfFullPinYin().substring(0, 1).toUpperCase());
        else
            hlContactContactMember.setInitial(DEFAULT_INITIAL);
        return hlContactContactMember;
    }

    /**
     * 根据grpc返回的名片联系人生成一个本地类型的名片联系人
     *
     * @param cardMember grpc返回的名片联系人
     * @return
     */
    public static HlContactCardMember generateHlContactCardMember(ContactList.CardMember cardMember) {
        HlContactCardMember hlContactCardMember = new HlContactCardMember();
        hlContactCardMember.setOwnerSid(RenheApplication.getInstance().getUserInfo().getSid());
        hlContactCardMember.setMobile(cardMember.getMobile());
        hlContactCardMember.setName(cardMember.getName());
        hlContactCardMember.setSid(cardMember.getSid());
        hlContactCardMember.setCardId(cardMember.getCardId());
        hlContactCardMember.setShortName(cardMember.getShortName());
        hlContactCardMember.setColorIndex(cardMember.getColorIndex());
        if (TextUtils.isEmpty(cardMember.getName())) {
            hlContactCardMember.setName(DEFAULT_INITIAL);
        }
        if (!TextUtils.isEmpty(cardMember.getName())) {
            String fullPinYin = PinyinUtil.cn2Spell(hlContactCardMember.getName());
            if (!TextUtils.isEmpty(fullPinYin)) {
                hlContactCardMember.setFullPinYin(fullPinYin);
            } else {
                hlContactCardMember.setFullPinYin(DEFAULT_INITIAL);
            }
            String initialOfFullPinYin = PinyinUtil.cn2FirstSpell(hlContactCardMember.getName());
            if (!TextUtils.isEmpty(initialOfFullPinYin)) {
                hlContactCardMember.setInitialOfFullPinYin(initialOfFullPinYin);
            } else {
                hlContactCardMember.setInitialOfFullPinYin(DEFAULT_INITIAL);
            }
            if (hlContactCardMember.getInitialOfFullPinYin().length() > 0)
                hlContactCardMember.setInitial(hlContactCardMember.getInitialOfFullPinYin().substring(0, 1).toUpperCase());
            else
                hlContactCardMember.setInitial(DEFAULT_INITIAL);
        }
        return hlContactCardMember;
    }

    public static void handleLocalData(final Map<String, List<HlContacts>> hlContactsMap,
                                       final List<HlContacts> hlLocalContactsList,
                                       final List<HlContactRenheMember> hlLocalContactRenheMemberList,
                                       final List<HlContactContactMember> hlLocalContactContactMemberList,
                                       final List<HlContactCardMember> hlLocalContactCardMemberList,
                                       final ContactInterface contactInterface
    ) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                getAllHlContacts(hlLocalContactsList, hlLocalContactRenheMemberList,
                        hlLocalContactContactMemberList, hlLocalContactCardMemberList);
                getLocalCommonContacts(hlContactsMap);
                Logger.d("成功 获取本地人脉列表");
                contactInterface.onHandleLocalDataSuccess();
            }
        });
        thread.start();
    }

    public static void handleLocalMobileContactsData(final Map<String, List<HlContacts>> hlContactsMap,
                                                     final List<HlContacts> hlLocalContactsList,
                                                     final List<HlContactContactMember> hlLocalContactContactMemberList,
                                                     final ContactInterface contactInterface
    ) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                getAllMobileContacts(hlLocalContactsList, hlLocalContactContactMemberList);
                Logger.d("成功 获取本地人脉列表");
                contactInterface.onHandleLocalDataSuccess();
            }
        });
        thread.start();
    }

    public static void handleDataToDbAfterRequest(final List<HlContactRenheMember> hlContactRenheMemberList,
                                                  final List<HlContactContactMember> hlContactContactMemberList,
                                                  final List<HlContactCardMember> hlContactCardMemberList,
                                                  final List<HlContactRenheMember> needUpdateRenheMemberList,
                                                  final List<HlContactRenheMember> needDeleteRenheMemberList,
                                                  final List<HlContactContactMember> needDeleteMobileMemberList,
                                                  final List<HlContactCardMember> needDeleteCardMemberList,
                                                  final ContactInterface contactInterface
    ) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (!saveAllHlContactRenheMember(hlContactRenheMemberList))
                    return;
                if (!saveAllHlContactContactMember(hlContactContactMemberList))
                    return;
                if (!saveAllHlContactCardMember(hlContactCardMemberList))
                    return;
                deleteAllHlContactRenheMember(needDeleteRenheMemberList);
                deleteAllHlContactMobileMember(needDeleteMobileMemberList);
                deleteAllHlContactCardMember(needDeleteCardMemberList);
                updateAllHlContactRenheMember(needUpdateRenheMemberList);
                Logger.d("人脉列表数据 成功 插入/更新到数据库");
                contactInterface.onHandleDataToDbSuccess();
            }
        });
        thread.start();
    }

    /**
     * 保存一条记录，如果hlContacts对应的id存在，则更新，否则插入
     */
    public static void save(HlContacts hlContacts) {
        if (hlContacts == null)
            return;
        int type = hlContacts.getType();
        long id;
        switch (type) {
            case HlContacts.HLCONTACTS_RENHE_MEMBER_TYPE:
                HlContactRenheMember hlContactRenheMember = hlContacts.getHlContactRenheMember();
                if (null != hlContactRenheMember) {
                    id = selectHlContactRenheMemberId(hlContactRenheMember.getSid());
                    try {
                        if (id >= 0) {
                            hlContactRenheMember.update(id);
                        } else {
                            hlContactRenheMember.save();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                break;
            case HlContacts.HLCONTACTS_CONTACT_MEMBER_TYPE:
                HlContactContactMember hlContactContactMember = hlContacts.getHlContactContactMember();
                if (null != hlContactContactMember) {
                    id = selectHlContactContactMemberId(hlContactContactMember.getSid());
                    try {
                        if (id >= 0) {
                            hlContactContactMember.update(id);
                        } else {
                            hlContactContactMember.save();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                break;
            case HlContacts.HLCONTACTS_CARD_MEMBER_TYPE:
                HlContactCardMember hlContactCardMember = hlContacts.getHlContactCardMember();
                if (null != hlContactCardMember) {
                    id = selectHlContactCardMemberId(hlContactCardMember.getSid());
                    try {
                        if (id >= 0) {
                            hlContactCardMember.update(id);
                        } else {
                            hlContactCardMember.save();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }

    }

    /**
     * 保存所有的人和好友到数据库
     */
    public static boolean saveAllHlContactRenheMember(List<HlContactRenheMember> hlContactRenheMemberList) {
        if (hlContactRenheMemberList == null || hlContactRenheMemberList.isEmpty())
            return true;
        try {
            DataSupport.saveAll(hlContactRenheMemberList);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 保存所有的通讯录好友到数据库
     */
    public static boolean saveAllHlContactContactMember(List<HlContactContactMember> hlContactContactMemberList) {
        if (hlContactContactMemberList == null || hlContactContactMemberList.isEmpty())
            return true;
        try {
            DataSupport.saveAll(hlContactContactMemberList);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 保存所有的名片好友到数据库
     */
    public static boolean saveAllHlContactCardMember(List<HlContactCardMember> hlContactCardMemberList) {
        if (hlContactCardMemberList == null || hlContactCardMemberList.isEmpty())
            return true;
        try {
            DataSupport.saveAll(hlContactCardMemberList);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 删除一条人和好友记录，如果hlContactRenheMember对应的id存在，则删除
     */
    public static void deleteHlContactRenheMember(HlContactRenheMember hlContactRenheMember) {
        if (null != hlContactRenheMember) {
            long id = selectHlContactRenheMemberId(hlContactRenheMember.getSid());
            if (id >= 0) {
                try {
                    hlContactRenheMember.delete(HlContactRenheMember.class, id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 删除一条通讯录记录，如果hlContactContactMember对应的id存在，则删除
     */
    public static void deleteHlContactMobileMember(HlContactContactMember hlContactContactMember) {
        if (null != hlContactContactMember) {
            long id = selectHlContactContactMemberId(hlContactContactMember.getSid());
            if (id >= 0) {
                try {
                    hlContactContactMember.delete(HlContactContactMember.class, id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 删除一条名片记录，如果hlContactCardMember对应的id存在，则删除
     */
    public static void deleteHlContactCardMember(HlContactCardMember hlContactCardMember) {
        if (null != hlContactCardMember) {
            long id = selectHlContactCardMemberId(hlContactCardMember.getSid());
            if (id >= 0) {
                try {
                    hlContactCardMember.delete(HlContactCardMember.class, id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 删除一条记录，如果hlContacts对应的id存在，则删除
     */
    public static void delete(HlContacts hlContacts) {
        if (hlContacts == null)
            return;
        int type = hlContacts.getType();
        long id;
        switch (type) {
            case HlContacts.HLCONTACTS_RENHE_MEMBER_TYPE:
                HlContactRenheMember hlContactRenheMember = hlContacts.getHlContactRenheMember();
                if (null != hlContactRenheMember) {
                    id = selectHlContactRenheMemberId(hlContactRenheMember.getSid());
                    if (id >= 0) {
                        try {
                            hlContactRenheMember.delete(HlContactRenheMember.class, id);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case HlContacts.HLCONTACTS_CONTACT_MEMBER_TYPE:
                HlContactContactMember hlContactContactMember = hlContacts.getHlContactContactMember();
                if (null != hlContactContactMember) {
                    id = selectHlContactContactMemberId(hlContactContactMember.getSid());
                    if (id >= 0) {
                        try {
                            hlContactContactMember.delete(HlContactContactMember.class, id);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                break;
            case HlContacts.HLCONTACTS_CARD_MEMBER_TYPE:
                HlContactCardMember hlContactCardMember = hlContacts.getHlContactCardMember();
                if (null != hlContactCardMember) {
                    id = selectHlContactCardMemberId(hlContactCardMember.getSid());
                    if (id >= 0) {
                        try {
                            hlContactCardMember.delete(HlContactCardMember.class, id);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
        }

    }

    /**
     * 删除所有对应的人和好友记录，如果hlContactCardMember对应的id存在，则删除
     */
    public static void deleteAllHlContactRenheMember(List<HlContactRenheMember> hlContactRenheMemberList) {
        for (HlContactRenheMember hlContactRenheMember : hlContactRenheMemberList) {
            deleteHlContactRenheMember(hlContactRenheMember);
        }
    }

    /**
     * 删除所有对应的名片记录，如果hlContactCardMember对应的id存在，则删除
     */
    public static void deleteAllHlContactMobileMember(List<HlContactContactMember> hlContactContactMemberList) {
        for (HlContactContactMember hlContactContactMember : hlContactContactMemberList) {
            deleteHlContactMobileMember(hlContactContactMember);
        }
    }

    /**
     * 删除所有对应的名片记录，如果hlContactCardMember对应的id存在，则删除
     */
    public static void deleteAllHlContactCardMember(List<HlContactCardMember> hlContactCardMemberList) {
        for (HlContactCardMember hlContactCardMember : hlContactCardMemberList) {
            deleteHlContactCardMember(hlContactCardMember);
        }
    }

    /**
     * 删除所有对应的人脉记录
     *
     * @param classz HlContactRenheMember.class HlContactContactMember.class HlContactCardMember.class
     * @param sid    人脉sid
     */
    public static void deleteHlContactMemberBySid(final Class classz, final String sid) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DataSupport.deleteAll(classz, "ownerSid=? and sid=?", RenheApplication.getInstance().getUserInfo().getSid(), sid);
                    ContactDeleteOrAndEvent contactDeleteOrAndEvent = new ContactDeleteOrAndEvent(ContactDeleteOrAndEvent.CONTACT_EVENT_TYPE_DELETE);
                    contactDeleteOrAndEvent.setSid(sid);
                    EventBus.getDefault().post(contactDeleteOrAndEvent);//通知人脉列表删除item
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public static int deleteContactsMapItem(Map<String, List<HlContacts>> mContactsMap, String sid) {
        if (null == mContactsMap || mContactsMap.isEmpty())
            return -1;
        Set<Map.Entry<String, List<HlContacts>>> set = mContactsMap.entrySet();
        for (Map.Entry<String, List<HlContacts>> entry : set) {
            List<HlContacts> contactsList = entry.getValue();
            for (int j = 0; j < contactsList.size(); j++) {
                HlContacts ct = contactsList.get(j);
                switch (ct.getType()) {
                    case HlContacts.HLCONTACTS_RENHE_MEMBER_TYPE:
                        if (ct.getHlContactRenheMember().getSid().equals(sid)) {
                            contactsList.remove(ct);
                            if (contactsList.isEmpty()) {
                                mContactsMap.remove(entry.getKey());
                            }
                            if (!entry.getKey().equals(OFTEN_USED_DEFAULT_INITIAL))//由于删除的人可能是常用联系人里的，所以mContactsMap可能存在两个同样sid的好友，要将它们全部清除
                                return 1;
                        }
                        break;
                    case HlContacts.HLCONTACTS_CONTACT_MEMBER_TYPE:
                        if (ct.getHlContactContactMember().getSid().equals(sid)) {
                            contactsList.remove(ct);
                            if (contactsList.isEmpty()) {
                                mContactsMap.remove(entry.getKey());
                            }
                            return 1;
                        }
                        break;
                    case HlContacts.HLCONTACTS_CARD_MEMBER_TYPE:
                        if (ct.getHlContactCardMember().getSid().equals(sid)) {
                            contactsList.remove(ct);
                            if (contactsList.isEmpty()) {
                                mContactsMap.remove(entry.getKey());
                            }
                            return 1;
                        }
                        break;
                }
            }
        }
        return -1;
    }

    /**
     * 更新已有的用户信息，RenheApplication.getInstance().getUserInfo()中的sid属性必须有值
     */
    public static void update(HlContacts hlContacts) {
        if (hlContacts == null)
            return;
        int type = hlContacts.getType();
        long id;
        switch (type) {
            case HlContacts.HLCONTACTS_RENHE_MEMBER_TYPE:
                HlContactRenheMember hlContactRenheMember = hlContacts.getHlContactRenheMember();
                if (null != hlContactRenheMember) {
                    id = selectHlContactRenheMemberId(hlContactRenheMember.getSid());
                    if (id >= 0) {
                        try {
                            hlContactRenheMember.update(id);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case HlContacts.HLCONTACTS_CONTACT_MEMBER_TYPE:
                HlContactContactMember hlContactContactMember = hlContacts.getHlContactContactMember();
                if (null != hlContactContactMember) {
                    id = selectHlContactContactMemberId(hlContactContactMember.getSid());
                    if (id >= 0) {
                        try {
                            hlContactContactMember.update(id);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                break;
            case HlContacts.HLCONTACTS_CARD_MEMBER_TYPE:
                HlContactCardMember hlContactCardMember = hlContacts.getHlContactCardMember();
                if (null != hlContactCardMember) {
                    id = selectHlContactCardMemberId(hlContactCardMember.getSid());
                    if (id >= 0) {
                        try {
                            hlContactCardMember.update(id);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
        }

    }

    /**
     * 更新已有的用户信息
     */
    public static void updateHlContactRenheMember(HlContactRenheMember hlContactRenheMember) {
        if (null != hlContactRenheMember) {
            long id = selectHlContactRenheMemberId(hlContactRenheMember.getSid());
            if (id >= 0) {
                try {
                    hlContactRenheMember.update(id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 更新已有的用户信息
     */
    public static void updateAllHlContactRenheMember(List<HlContactRenheMember> hlContactRenheMemberList) {
        if (null != hlContactRenheMemberList && !hlContactRenheMemberList.isEmpty()) {
            for (HlContactRenheMember hlContactRenheMember : hlContactRenheMemberList)
                updateHlContactRenheMember(hlContactRenheMember);
        }
    }

    /**
     * 通过type 和sid查询指定联系人信息
     */
    public static HlContacts getHlContactByTypeAndSid(int type, String sid) {
        HlContacts hlContacts = null;
        switch (type) {
            case HlContacts.HLCONTACTS_RENHE_MEMBER_TYPE:
                List<HlContactRenheMember> hlContactRenheMembers = null;
                try {
                    hlContactRenheMembers = DataSupport.where("ownerSid=? and sid=?", RenheApplication.getInstance().getUserInfo().getSid(), sid)
                            .find(HlContactRenheMember.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (hlContactRenheMembers != null && !hlContactRenheMembers.isEmpty()) {
                    hlContacts = new HlContacts();
                    hlContacts.setType(HlContacts.HLCONTACTS_RENHE_MEMBER_TYPE);
                    hlContacts.setHlContactRenheMember(hlContactRenheMembers.get(0));
                    return hlContacts;
                }
                break;
            case HlContacts.HLCONTACTS_CONTACT_MEMBER_TYPE:
                List<HlContactContactMember> hlContactContactMembers = null;
                try {
                    hlContactContactMembers = DataSupport.where("ownerSid=? and sid=?", RenheApplication.getInstance().getUserInfo().getSid(), sid)
                            .find(HlContactContactMember.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (hlContactContactMembers != null && !hlContactContactMembers.isEmpty()) {
                    hlContacts = new HlContacts();
                    hlContacts.setType(HlContacts.HLCONTACTS_CONTACT_MEMBER_TYPE);
                    hlContacts.setHlContactContactMember(hlContactContactMembers.get(0));
                    return hlContacts;
                }

                break;
            case HlContacts.HLCONTACTS_CARD_MEMBER_TYPE:
                List<HlContactCardMember> hlContactCardMembers = null;
                try {
                    hlContactCardMembers = DataSupport.where("ownerSid=? and sid=?", RenheApplication.getInstance().getUserInfo().getSid(), sid)
                            .find(HlContactCardMember.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (hlContactCardMembers != null && !hlContactCardMembers.isEmpty()) {
                    hlContacts = new HlContacts();
                    hlContacts.setType(HlContacts.HLCONTACTS_CARD_MEMBER_TYPE);
                    hlContacts.setHlContactCardMember(hlContactCardMembers.get(0));
                    return hlContacts;
                }
                break;
        }

        return hlContacts;
    }

    /**
     * 获取所有的本地联系人，包括人和网好友、人和网非好友（是通讯录好友，并且是人和网会员，但不是人和网好友）、通讯录好友、名片联系人
     */
    public static List<HlContacts> getAllHlContacts(List<HlContacts> hlContactsList,
                                                    List<HlContactRenheMember> hlContactRenheMemberList,
                                                    List<HlContactContactMember> hlContactContactMemberList,
                                                    List<HlContactCardMember> hlContactCardMemberList) {
        List<HlContactRenheMember> hlContactRenheMembers = null;
        List<HlContactContactMember> hlContactContactMembers = null;
        List<HlContactCardMember> hlContactCardMembers = null;
        try {
            hlContactRenheMembers = DataSupport.where("ownerSid=?", RenheApplication.getInstance().getUserInfo().getSid()).find(HlContactRenheMember.class);
            hlContactContactMembers = DataSupport.where("ownerSid=?", RenheApplication.getInstance().getUserInfo().getSid()).find(HlContactContactMember.class);
            hlContactCardMembers = DataSupport.where("ownerSid=?", RenheApplication.getInstance().getUserInfo().getSid()).find(HlContactCardMember.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != hlContactRenheMembers) {
            for (HlContactRenheMember hlContactRenheMember : hlContactRenheMembers) {
                HlContacts hlContacts = new HlContacts();
                hlContacts.setHlContactRenheMember(hlContactRenheMember);
                hlContacts.setType(HlContacts.HLCONTACTS_RENHE_MEMBER_TYPE);
                hlContactsList.add(hlContacts);
                hlContactRenheMemberList.add(hlContactRenheMember);
            }
        }

        if (null != hlContactContactMembers) {
            for (HlContactContactMember hlContactContactMember : hlContactContactMembers) {
                HlContacts hlContacts = new HlContacts();
                hlContacts.setType(HlContacts.HLCONTACTS_CONTACT_MEMBER_TYPE);
                hlContacts.setHlContactContactMember(hlContactContactMember);
                hlContactsList.add(hlContacts);
                hlContactContactMemberList.add(hlContactContactMember);
            }
        }
        if (null != hlContactCardMembers) {
            for (HlContactCardMember hlContactCardMember : hlContactCardMembers) {
                HlContacts hlContacts = new HlContacts();
                hlContacts.setType(HlContacts.HLCONTACTS_CARD_MEMBER_TYPE);
                hlContacts.setHlContactCardMember(hlContactCardMember);
                hlContactsList.add(hlContacts);
                hlContactCardMemberList.add(hlContactCardMember);
            }
        }

        return hlContactsList;
    }

    /**
     * 获取所有的本地联系人，包括人和网好友、人和网非好友（是通讯录好友，并且是人和网会员，但不是人和网好友）、通讯录好友、名片联系人
     */
    public static List<HlContacts> getAllMobileContacts(List<HlContacts> hlContactsList,
                                                        List<HlContactContactMember> hlContactContactMemberList) {
        List<HlContactContactMember> hlContactContactMembers = null;
        try {
            hlContactContactMembers = DataSupport.where("ownerSid=?", RenheApplication.getInstance().getUserInfo().getSid()).find(HlContactContactMember.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (null != hlContactContactMembers) {
            for (HlContactContactMember hlContactContactMember : hlContactContactMembers) {
                HlContacts hlContacts = new HlContacts();
                hlContacts.setType(HlContacts.HLCONTACTS_CONTACT_MEMBER_TYPE);
                hlContacts.setHlContactContactMember(hlContactContactMember);
                hlContactsList.add(hlContacts);
                hlContactContactMemberList.add(hlContactContactMember);
            }
        }

        return hlContactsList;
    }

    /**
     * 获取给定limit数量的本地联系人，包括人和网好友、人和网非好友（是通讯录好友，并且是人和网会员，但不是人和网好友）、通讯录好友、名片联系人
     *
     * @param limit 要获取的数量
     */
    public static List<HlContacts> getLimitedHlContacts(List<HlContacts> hlContactsList,
                                                        List<HlContactRenheMember> hlContactRenheMemberList,
                                                        List<HlContactContactMember> hlContactContactMemberList,
                                                        List<HlContactCardMember> hlContactCardMemberList, String keyword, int limit) {
        List<HlContactRenheMember> hlContactRenheMembers = null;
        List<HlContactContactMember> hlContactContactMembers = null;
        List<HlContactCardMember> hlContactCardMembers = null;
        try {
            hlContactRenheMembers = DataSupport.where("ownerSid=? and name like ?",
                    RenheApplication.getInstance().getUserInfo().getSid(), "%" + keyword + "%")
                    .limit(limit).find(HlContactRenheMember.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != hlContactRenheMembers) {
            for (HlContactRenheMember hlContactRenheMember : hlContactRenheMembers) {
                HlContacts hlContacts = new HlContacts();
                hlContacts.setHlContactRenheMember(hlContactRenheMember);
                hlContacts.setType(HlContacts.HLCONTACTS_RENHE_MEMBER_TYPE);
                hlContactsList.add(hlContacts);
                hlContactRenheMemberList.add(hlContactRenheMember);
            }
            if (hlContactsList.size() >= limit)
                return hlContactsList;
        }
        try {
            hlContactContactMembers = DataSupport.where("ownerSid=? and name like ?",
                    RenheApplication.getInstance().getUserInfo().getSid(), "%" + keyword + "%")
                    .limit(limit).find(HlContactContactMember.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != hlContactContactMembers) {
            for (HlContactContactMember hlContactContactMember : hlContactContactMembers) {
                HlContacts hlContacts = new HlContacts();
                hlContacts.setType(HlContacts.HLCONTACTS_CONTACT_MEMBER_TYPE);
                hlContacts.setHlContactContactMember(hlContactContactMember);
                hlContactsList.add(hlContacts);
                hlContactContactMemberList.add(hlContactContactMember);
            }
            if (hlContactsList.size() >= limit)
                return hlContactsList;
        }
        try {
            hlContactCardMembers = DataSupport.where("ownerSid=? and name like ?",
                    RenheApplication.getInstance().getUserInfo().getSid(), "%" + keyword + "%")
                    .limit(limit).find(HlContactCardMember.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != hlContactCardMembers) {
            for (HlContactCardMember hlContactCardMember : hlContactCardMembers) {
                HlContacts hlContacts = new HlContacts();
                hlContacts.setType(HlContacts.HLCONTACTS_CARD_MEMBER_TYPE);
                hlContacts.setHlContactCardMember(hlContactCardMember);
                hlContactsList.add(hlContacts);
                hlContactCardMemberList.add(hlContactCardMember);
            }
            if (hlContactsList.size() >= limit)
                return hlContactsList;
        }

        return hlContactsList;
    }

    /**
     * 获取所有的本地人和网好友
     */
    public static List<HlContactRenheMember> getAllHlFriendContacts(List<HlContactRenheMember> hlContactRenheMemberList) {
        if (null == hlContactRenheMemberList)
            hlContactRenheMemberList = new ArrayList<>();
        try {
            hlContactRenheMemberList = DataSupport.where("ownerSid=? and isFriend=?",
                    RenheApplication.getInstance().getUserInfo().getSid(), "1").find(HlContactRenheMember.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hlContactRenheMemberList;
    }

    /**
     * 根据sid查询该人和会员是否存在
     *
     * @param sid 要查找的人和会员的sid
     * @return
     */
    private static long selectHlContactRenheMemberId(String sid) {
        List<HlContactRenheMember> infos = null;
        try {
            infos = DataSupport.where("ownerSid=? and sid=?", RenheApplication.getInstance().getUserInfo().getSid(), sid)
                    .find(HlContactRenheMember.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (infos != null && !infos.isEmpty()) {
            return infos.get(0).getId();
        }
        return -1;
    }

    /**
     * 根据sid查询该通讯录好友是否存在
     *
     * @param sid 要查找的通讯录好友的sid
     * @return
     */
    private static long selectHlContactContactMemberId(String sid) {
        List<HlContactContactMember> infos = null;
        try {
            infos = DataSupport.where("ownerSid=? and sid=?", RenheApplication.getInstance().getUserInfo().getSid(), sid).find(HlContactContactMember.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (infos != null && !infos.isEmpty()) {
            return infos.get(0).getId();
        }
        return -1;
    }

    /**
     * 根据sid查询该名片是否存在
     *
     * @param sid 要查找的名片的sid
     * @return
     */
    private static long selectHlContactCardMemberId(String sid) {
        List<HlContactCardMember> infos = null;
        try {
            infos = DataSupport.where("ownerSid=? and sid=?", RenheApplication.getInstance().getUserInfo().getSid(), sid).find(HlContactCardMember.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (infos != null && !infos.isEmpty()) {
            return infos.get(0).getId();
        }
        return -1;
    }

    /**
     * 清空所有的联系人相关表数据
     */
    public static void clearContactsAllTables() {
        try {
            DataSupport.deleteAll(HlContactRenheMember.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            DataSupport.deleteAll(HlContactContactMember.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            DataSupport.deleteAll(HlContactCardMember.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断本地是否有人和好友 缓存数据
     *
     * @return
     */
    public static boolean renheMemberTableHasCache() {
        int count = 0;
        try {
            count = DataSupport.where("ownerSid=?", RenheApplication.getInstance().getUserInfo().getSid()).count(HlContactRenheMember.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (count > 0) {
            return true;
        }
        return false;
    }

    public static void findResultListByKeyWord(String keyWord, Map<String, List<HlContacts>> hlContactsMap, List<HlContacts> hlFindResultContactsList) {
        hlFindResultContactsList.clear();
        Set<Map.Entry<String, List<HlContacts>>> set = hlContactsMap.entrySet();
        for (Map.Entry<String, List<HlContacts>> entry : set) {
            if (entry.getKey().equals(OFTEN_USED_DEFAULT_INITIAL))
                continue;
            List<HlContacts> contactsList = entry.getValue();
            if (null != contactsList && !contactsList.isEmpty()) {
                for (int j = 0; j < contactsList.size(); j++) {
                    HlContacts ct = contactsList.get(j);
                    String userName = "";
                    String fullPinYin = "";//名字全拼 比如 王宁 是wangning
                    String initialOfFullPinYin = "";//名字单字首字母组合 比如 王宁 是wn
                    String company = "";//增加通过公司名搜索的功能
                    String job = "";//增加通过职务名搜索的功能
                    switch (ct.getType()) {
                        case HlContacts.HLCONTACTS_RENHE_MEMBER_TYPE:
                            userName = ct.getHlContactRenheMember().getName();
                            fullPinYin = ct.getHlContactRenheMember().getFullPinYin();
                            initialOfFullPinYin = ct.getHlContactRenheMember().getInitialOfFullPinYin();
                            company = ct.getHlContactRenheMember().getCompany();
                            job = ct.getHlContactRenheMember().getTitle();
                            break;
                        case HlContacts.HLCONTACTS_CONTACT_MEMBER_TYPE:
                            userName = ct.getHlContactContactMember().getName();
                            fullPinYin = ct.getHlContactContactMember().getFullPinYin();
                            initialOfFullPinYin = ct.getHlContactContactMember().getInitialOfFullPinYin();
                            break;
                        case HlContacts.HLCONTACTS_CARD_MEMBER_TYPE:
                            userName = ct.getHlContactCardMember().getName();
                            fullPinYin = ct.getHlContactCardMember().getFullPinYin();
                            initialOfFullPinYin = ct.getHlContactCardMember().getInitialOfFullPinYin();
                            break;
                    }
                    //搜索条件控制
                    if (!TextUtils.isEmpty(userName) && (userName.toUpperCase().startsWith(keyWord.toUpperCase())
                            || initialOfFullPinYin.startsWith(keyWord.toUpperCase())
                            || fullPinYin.startsWith(keyWord.toUpperCase()))
                            || company.contains(keyWord)
                            || job.contains(keyWord)) {
                        hlFindResultContactsList.add(ct);
                    }
                }
            }
        }
    }


    /**
     * 列表item被单击
     *
     * @param hlContacts
     */
    public static void handleOnitemClick(Context context, HlContacts hlContacts) {
        if (null != hlContacts) {
            int type = hlContacts.getType();
            if (type == HlContacts.HLCONTACTS_RENHE_MEMBER_TYPE) {
                Intent intent = new Intent(context, MyHomeArchivesActivity.class);
                intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, hlContacts.getHlContactRenheMember().getSid());
                intent.putExtra("name", hlContacts.getHlContactRenheMember().getName());
                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            } else if (type == HlContacts.HLCONTACTS_CONTACT_MEMBER_TYPE
                    || type == HlContacts.HLCONTACTS_CARD_MEMBER_TYPE) {
                Intent intent = new Intent(context, MyContactArchivesActivity.class);
                Bundle b = new Bundle();
                b.putSerializable("contact", hlContacts);
                intent.putExtras(b);
                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }

        }
    }

    /**
     * 列表item被长按
     *
     * @param hlContacts
     */
    public static void handleOnitemLongClick(final Context context, HlContacts hlContacts) {
        if (null != hlContacts) {
            int type = hlContacts.getType();
            int openId = 0;
            String mobile = null, tel = null, name = null, userFace = null;
            if (type == HlContacts.HLCONTACTS_RENHE_MEMBER_TYPE) {
                HlContactRenheMember hlContactRenheMember = hlContacts.getHlContactRenheMember();
                openId = hlContactRenheMember.getImId();
                mobile = hlContactRenheMember.getMobile();
                tel = hlContactRenheMember.getTel();
                name = hlContactRenheMember.getName();
                userFace = hlContactRenheMember.getUserface();
                if (openId <= 0 && TextUtils.isEmpty(mobile) && TextUtils.isEmpty(tel)) {
                    return;
                }
            } else if (type == HlContacts.HLCONTACTS_CONTACT_MEMBER_TYPE) {
                HlContactContactMember hlContactContactMember = hlContacts.getHlContactContactMember();
                mobile = hlContactContactMember.getMobile();
                name = hlContactContactMember.getName();
                if (TextUtils.isEmpty(mobile)) {
                    return;
                }
            } else if (type == HlContacts.HLCONTACTS_CARD_MEMBER_TYPE) {
                HlContactCardMember hlContactCardMember = hlContacts.getHlContactCardMember();
                mobile = hlContactCardMember.getMobile();
                name = hlContactCardMember.getName();
                if (TextUtils.isEmpty(mobile)) {
                    return;
                }
            }
            final String mMobile = mobile;
            final String mTel = tel;
            final String mName = name;
            final String mUserFace = userFace;
            final int mOpenId = openId;
            int positiveRes = 0;
            int negativeRes = 0;
            if (!TextUtils.isEmpty(mobile) || !TextUtils.isEmpty(tel)) {
                positiveRes = R.string.contact_fuction_call;
            }
            if (openId > 0) {
                negativeRes = R.string.contact_fuction_chat;
            }

            MaterialDialogsUtil materialDialogsUtil = new MaterialDialogsUtil(context);
            MaterialDialog materialDialog = materialDialogsUtil.getCustomViewBuilder(R.layout.contact_custom_dialog_layout,
                    positiveRes, negativeRes)
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (which == DialogAction.POSITIVE) {
                                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + (TextUtils.isEmpty(mMobile) ? mTel : mMobile)));
                                context.startActivity(intent);
                            } else if (which == DialogAction.NEGATIVE) {
                                createIM(context, mOpenId, mName, mUserFace);
                            }
                        }
                    }).build();
            ImageView avatarIv = (ImageView) materialDialog.getCustomView().findViewById(R.id.avatar_img);
            TextView avatarTv = (TextView) materialDialog.getCustomView().findViewById(R.id.avatar_txt);
            TextView nameTv = (TextView) materialDialog.getCustomView().findViewById(R.id.tx_name);
            if (type == HlContacts.HLCONTACTS_RENHE_MEMBER_TYPE) {
                HlContactRenheMember hlContactRenheMember = hlContacts.getHlContactRenheMember();
                avatarIv.setVisibility(View.VISIBLE);
                avatarTv.setVisibility(View.GONE);
                ImageLoader imageLoader = ImageLoader.getInstance();
                imageLoader.displayImage(hlContactRenheMember.getUserface(), avatarIv, CacheManager.circleImageOptions);
            } else if (type == HlContacts.HLCONTACTS_CONTACT_MEMBER_TYPE
                    || type == HlContacts.HLCONTACTS_CARD_MEMBER_TYPE) {
                int colorIndex = 0;
                String shortName = "";
                if (type == HlContacts.HLCONTACTS_CONTACT_MEMBER_TYPE) {
                    colorIndex = hlContacts.getHlContactContactMember().getColorIndex();
                    shortName = hlContacts.getHlContactContactMember().getShortName();
                } else {
                    colorIndex = hlContacts.getHlContactCardMember().getColorIndex();
                    shortName = hlContacts.getHlContactCardMember().getShortName();
                }
                avatarIv.setVisibility(View.GONE);
                avatarTv.setVisibility(View.VISIBLE);
                avatarTv.setBackgroundResource(Constants.AVATARBG[colorIndex]);
                avatarTv.setText(shortName);
            }
            if (TextUtils.isEmpty(name))
                nameTv.setVisibility(View.GONE);
            else
                nameTv.setText(name);
            materialDialog.show();
        }
    }

    public static void createIM(final Context context, int imId, String userName, final String userFace) {
        com.itcalf.renhe.context.wukong.im.RenheIMUtil.showProgressDialog(context, R.string.conversation_creating);
        // 会话标题,注意：单聊的话title,icon默认均为对方openid,传入的值无效
        final StringBuffer title = new StringBuffer();
        title.append(userName);
        Message message = null; // 创建会话发送的系统消息,可以不设置
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
                context.startActivity(intent);
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

    /**
     * 获取常用联系人，等应用第一次从服务端拉取所有的人脉后调用该方法
     */
    public static void getCommonContacts(final List<HlContacts> hlOftenUsedContacts
            , final ContactInterface contactInterface) {
        if (null == hlOftenUsedContacts)
            return;
        final List<String> commonSids = new ArrayList<>();
        //获取常用联系人
        String ids = SharedPreferencesUtil.getStringShareData(Constants.SHAREDPREFERENCES_KEY.CONTACTS_OFEN_USERD, "", true);
        if (!TextUtils.isEmpty(ids)) {
            String[] cIds;
            if (ids.contains(";")) {
                cIds = ids.split(";");
            } else {
                cIds = new String[]{ids};
            }
            Collections.addAll(commonSids, cIds);
        }
        if (!commonSids.isEmpty()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (String sid : commonSids) {
                        HlContacts hlContacts = getHlContactByTypeAndSid(HlContacts.HLCONTACTS_RENHE_MEMBER_TYPE, sid);
                        if (null != hlContacts && null != hlContacts.getHlContactRenheMember()) {
                            hlOftenUsedContacts.add(hlContacts);
                        }
                    }
                    contactInterface.onGetOftenUsedContactSuccess();
                }
            }).start();
        }
    }

    /**
     * 获取常用联系人,等本地人脉加载完毕之后调用该方法
     */
    public static void getLocalCommonContacts(Map<String, List<HlContacts>> hlContactsMap) {
        List<HlContacts> hlOftenUsedContacts = new ArrayList<>();
        final List<String> commonSids = new ArrayList<>();
        //获取常用联系人
        String ids = SharedPreferencesUtil.getStringShareData(Constants.SHAREDPREFERENCES_KEY.CONTACTS_OFEN_USERD, "", true);
        if (!TextUtils.isEmpty(ids)) {
            String[] cIds;
            if (ids.contains(";")) {
                cIds = ids.split(";");
            } else {
                cIds = new String[]{ids};
            }
            Collections.addAll(commonSids, cIds);
        }
        if (!commonSids.isEmpty()) {
            for (String sid : commonSids) {
                HlContacts hlContacts = getHlContactByTypeAndSid(HlContacts.HLCONTACTS_RENHE_MEMBER_TYPE, sid);
                if (null != hlContacts && null != hlContacts.getHlContactRenheMember()) {
                    hlOftenUsedContacts.add(hlContacts);
                }
            }
        }
        putOftenUsedToHlContactMap(hlContactsMap, hlOftenUsedContacts);
    }

    public static Map<String, List<HlContacts>> putOftenUsedToHlContactMap(Map<String, List<HlContacts>> hlContactsMap,
                                                                           List<HlContacts> hlOftenUsedContacts) {
        if (null == hlOftenUsedContacts)
            return null;
        if (null == hlContactsMap)
            hlContactsMap = new TreeMap<>();
        //生成联系人以首字母分类的treeMap
        for (HlContacts hlContacts : hlOftenUsedContacts) {
            List<HlContacts> hlRenheContactsesOfInitial = hlContactsMap.get(OFTEN_USED_DEFAULT_INITIAL);
            if (null == hlRenheContactsesOfInitial) {
                hlRenheContactsesOfInitial = new ArrayList<>();
            }
            if (!isOftenUsedExist(hlContactsMap, hlContacts))
                hlRenheContactsesOfInitial.add(hlContacts);
            hlContactsMap.put(OFTEN_USED_DEFAULT_INITIAL, hlRenheContactsesOfInitial);
        }
        return hlContactsMap;
    }

    private static boolean isOftenUsedExist(Map<String, List<HlContacts>> hlContactsMap, HlContacts hlContacts) {
        if (null == hlContactsMap)
            return false;
        if (null == hlContacts || null == hlContacts.getHlContactRenheMember())
            return false;
        List<HlContacts> hlRenheContactsesOfInitial = hlContactsMap.get(OFTEN_USED_DEFAULT_INITIAL);
        if (null == hlRenheContactsesOfInitial)
            return false;
        for (HlContacts oftenUsedContacts : hlRenheContactsesOfInitial) {
            if (null != oftenUsedContacts && hlContacts.getHlContactRenheMember().getSid()
                    .equals(oftenUsedContacts.getHlContactRenheMember().getSid())) {
                return true;
            }
        }
        return false;
    }
}

