package com.itcalf.renhe.command;

import com.itcalf.renhe.bean.ContactIsSave;
import com.itcalf.renhe.bean.ContactResultByMailBox.MailBoxContact;
import com.itcalf.renhe.bean.ContactsReturn.ContactResult;
import com.itcalf.renhe.dto.ContactList;
import com.itcalf.renhe.dto.ContactList.Member;
import com.itcalf.renhe.dto.NewInnerMessage;
import com.itcalf.renhe.po.Contact;

import java.util.List;

/**
 * 联系人接口
 *
 * @author xp
 */

public interface IContactCommand {

    /**
     * 获取联系人列表
     *
     * @return
     * @throws Exception
     */
    ContactList getContactList(String viewSId, String sid, String adSId) throws Exception;

    /**
     * 获取联系人列表(分页)别人
     *
     * @return
     * @throws Exception
     */
    ContactList getContactListBypage(String viewSId, String sid, String adSId, int index, int pageSize) throws Exception;

    /**
     * 获取联系人列表(分页)自己的
     *
     * @return
     * @throws Exception
     */
    ContactList getMyContactListByPage(String sid, String adSId, int index, int pageSize) throws Exception;

    /**
     * 获取新的联系人列表
     *
     * @return
     * @throws Exception
     */
    ContactList getNewContactList(String viewSId, String sid, String adSId, int maxCid, long maxLastUpdatedDate) throws Exception;

    /**
     * 获取新的联系人列表自己的
     *
     * @return
     * @throws Exception
     */
    ContactList getMyNewContactList(String sid, String adSId, int maxCid, int maxMobileId, int maxCardId, long maxLastUpdatedDate)
            throws Exception;

    /**
     * 保存联系人列表是否拉取成功
     *
     * @param maxCid maxLastUpdatedDate
     * @param sid    改成sid
     * @return
     * @throws Exception
     */
    long saveContactisSuccess(int maxCid, long maxLastUpdatedDate, String sid) throws Exception;

    long saveContactIsSuccess_V2(ContactIsSave contactIsSave) throws Exception;

    /**
     * 根据用户email去查maxcid
     */
    @Deprecated
    public int getContactMaxCidByEmail(String email) throws Exception;

    /**
     * 根据用户sid去查maxcid
     */
    public int getContactMaxCidBySid(String sid) throws Exception;

    /********************** 根据登入者的sid 找到表中对应的联系人进行保存，更新，删除 **************************************/
    /**
     * 同步、更新联系人
     *
     * @param ml
     * @param mySid 改成 sid 6.30
     * @return
     * @throws Exception
     */
    long saveOrupdateContactList(Member[] ml, String mySid) throws Exception;

    /**
     * 同步、更新联系人
     *
     * @param ml
     * @throws Exception
     */
    long saveOrUpdateContactList_V2(List<Member> ml, String mySid) throws Exception;

    /**
     * 自己mySid 找到表中对应的数据
     * 根据联系人sid删除联系人
     */
    boolean deleteMyContactBySid(String mySid, String sid) throws Exception;

    boolean deleteMyVCardContactBySid(String sid, int vCard) throws Exception;

    /********************** 根据登入者的sid 找到表中对应的联系人进行保存，更新，删除 **************************************/

    /**
     * 从数据库中获取所有联系人列表
     *
     * @param email
     * @return
     * @throws Exception
     */
    Contact[] getAllContact(String email) throws Exception;

    /**
     * 获取所有联系人中所有的和聊会员
     *
     * @param sid
     * @return
     * @throws Exception
     */
    List<Contact> getHLContact(String sid) throws Exception;

    /**
     * 从数据库中获取搜索到相关的联系人列表
     *
     * @param sid
     * @return
     * @throws Exception
     */
    Contact[] getSearchContact(String sid, String keyword, boolean isLimit) throws Exception;

    /**
     * 清空联系人表
     */
    boolean deleteContacts(String email) throws Exception;

    /**
     * 获取新的朋友数量
     *
     * @param sid
     * @param adSId
     * @param sinceId
     * @return
     * @throws Exception
     */
    NewInnerMessage getNewFriendsCount(String sid, String adSId, int sinceId) throws Exception;

    /**
     * 保存用户的email联系人
     *
     * @param ml
     * @param sid
     * @return
     * @throws Exception
     */
    long saveEmailContactList(List<MailBoxContact> ml, String sid, String now) throws Exception;

    /**
     * 查询所有email联系人
     *
     * @param email
     * @return
     * @throws Exception
     */
    List<MailBoxContact> getAllEmailContact(String email) throws Exception;

    /**
     * 获取email联系人保存的最后时间
     *
     * @param email
     * @return
     */
    long getEmailContactSaveTimeByEmail(String email);

    /**
     * 保存用户的mobile联系人
     *
     * @param ml
     * @param email
     * @return
     * @throws Exception
     */
    long saveMobileContactList(List<ContactResult> ml, String email, int maxid) throws Exception;

    /**
     * 查询所有Mobile联系人
     *
     * @param email
     * @return
     * @throws Exception
     */
    List<ContactResult> getAllMobileContact(String email) throws Exception;

    /**
     * 获取Mobile联系人保存的Maxid
     *
     * @param email
     * @return
     */
    int getMobileContactMaxidByEmail(String email);

    boolean deleteMobileContacts(String sid) throws Exception;

    void deleteContactBySid(String sid) throws Exception;

    long getContactMaxLastUpdatedDateBySid(String sid) throws Exception;

    /**
     * 更新好友是否被拉黑
     *
     * @param sid
     * @return
     * @throws Exception
     */
    long updataContactBlock(String sid, boolean isBlocked) throws Exception;

    /**
     * 自己mySid 找到表中对应的数据
     * 根据联系人sid查找联系人
     */
    Contact getMyContactBySid(String mySid, String sid) throws Exception;

    /**
     * 查找本地数据库中所有的最大的ids
     *
     * @param sid
     * @return
     */
    ContactIsSave getContactMaxIdBySid(String sid);

}
