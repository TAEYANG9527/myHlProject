package com.itcalf.renhe.command.impl;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.bean.ContactIsSave;
import com.itcalf.renhe.bean.ContactResultByMailBox.MailBoxContact;
import com.itcalf.renhe.bean.ContactsReturn.ContactResult;
import com.itcalf.renhe.command.IContactCommand;
import com.itcalf.renhe.database.sqlite.ContactDBHelper;
import com.itcalf.renhe.database.sqlite.TablesConstant;
import com.itcalf.renhe.dto.ContactList;
import com.itcalf.renhe.dto.ContactList.Member;
import com.itcalf.renhe.dto.NewInnerMessage;
import com.itcalf.renhe.po.Contact;
import com.itcalf.renhe.utils.HttpUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @功能说明 获取联系人
 */
public class ContactCommandImpl implements IContactCommand {

    private Application mRenheApplication;

    public ContactCommandImpl(Application application) {
        super();
        mRenheApplication = application;
    }

    @Override
    public ContactList getContactList(String viewSId, String sid, String adSId) throws Exception {
        Map<String, Object> reqParams = new HashMap<>();
        reqParams.put("viewSId", viewSId);
        reqParams.put("sid", sid);
        reqParams.put("adSId", adSId);
        return (ContactList) HttpUtil.doHttpRequest(Constants.Http.CONTACTLIST, reqParams, ContactList.class, mRenheApplication);
    }

    /**
     * 分页返回联系人信息（好友的）
     */
    @Override
    public ContactList getContactListBypage(String viewSId, String sid, String adSId, int index, int pageSize) throws Exception {
        // TODO Auto-generated method stub
        Map<String, Object> reqParams = new HashMap<>();
        reqParams.put("viewSId", viewSId);
        reqParams.put("sid", sid);
        reqParams.put("adSId", adSId);
        reqParams.put("skip", index);
        reqParams.put("limit", pageSize);
        return (ContactList) HttpUtil.doHttpRequest(Constants.Http.CONTACTLISTBYPAGE, reqParams, ContactList.class,
                mRenheApplication);
    }

    /**
     * 分页返回联系人信息（自己的）
     */
    @Override
    public ContactList getMyContactListByPage(String sid, String adSId, int index, int pageSize) throws Exception {
        Map<String, Object> reqParams = new HashMap<>();
        reqParams.put("sid", sid);
        reqParams.put("adSId", adSId);
        reqParams.put("skip", index);
        reqParams.put("limit", pageSize);
        return (ContactList) HttpUtil.doHttpRequest(Constants.Http.MYCONTACTLISTBYPAGE, reqParams, ContactList.class,
                mRenheApplication);
    }

    /**
     * 获取新的联系人
     */
    public ContactList getNewContactList(String viewSId, String sid, String adSId, int maxCid, long maxLastUpdatedDate)
            throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("viewSId", viewSId);
        reqParams.put("sid", sid);
        reqParams.put("adSId", adSId);
        reqParams.put("maxCid", maxCid);
        reqParams.put("maxLastUpdatedDate", maxLastUpdatedDate);
        return (ContactList) HttpUtil.doHttpRequest(Constants.Http.CONTACTLISTBYNEW, reqParams, ContactList.class,
                mRenheApplication);
    }

    @Override
    public ContactList getMyNewContactList(String sid, String adSId, int maxCid, int maxMobileId, int maxCardId,
                                           long maxLastUpdatedDate) throws Exception {
        Map<String, Object> reqParams = new HashMap<>();
        reqParams.put("sid", sid);
        reqParams.put("adSId", adSId);
        reqParams.put("maxCid", maxCid);
        reqParams.put("maxMobileId", maxMobileId);
        reqParams.put("maxCardId", maxCardId);
        reqParams.put("maxLastUpdatedDate", maxLastUpdatedDate);
        return (ContactList) HttpUtil.doHttpRequest(Constants.Http.MYCONTACTLISTBYNEW, reqParams, ContactList.class,
                mRenheApplication);
    }

    /**
     * 同步联系人 / 更新联系人
     *
     * @param ml
     * @param mySid 改成 sid 6.30
     * @return
     * @throws Exception
     */
    @Override
    public long saveOrupdateContactList(Member[] ml, String mySid) throws Exception {
        ContactDBHelper ctDBHelper = new ContactDBHelper(mRenheApplication.getApplicationContext(), TablesConstant.CONTACT_TABLE);
        long count = 0;
        try {
            for (int i = 0; i < ml.length; i++) {
                Contact ct = new Contact();
                ct.setId(ml[i].getSid());
                ct.setName(ml[i].getName());
                ct.setJob(ml[i].getTitle());
                ct.setCompany(ml[i].getCompany());
                ct.setContactface(ml[i].getUserface());
                ct.setMySid(mySid);
                ct.setAccountType(ml[i].getAccountType());
                ct.setRealname(ml[i].isRealname());
                ct.setBlockedContact(ml[i].isBlockedContact());
                ct.setBeBlocked(ml[i].isBeBlocked());
                ct.setImValid(ml[i].isImValid());
                ct.setImId(ml[i].getImId());
                ct.setMobile(ml[i].getMobile());
                ct.setTel(ml[i].getTel());
                count += ctDBHelper.insertOrUpdateUser(ct);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            ctDBHelper.closeDB();
        }
        return count;
    }

    /**
     * 同步联系人 / 更新联系人  V2
     *
     * @param ml
     * @param mySid 改成 sid 6.30
     * @return
     * @throws Exception
     */
    @Override
    public long saveOrUpdateContactList_V2(List<Member> ml, String mySid) throws Exception {
        ContactDBHelper ctDBHelper = new ContactDBHelper(mRenheApplication.getApplicationContext(), TablesConstant.CONTACT_TABLE);
        long count = 0;
        try {
            for (int i = 0; i < ml.size(); i++) {
                Contact ct = new Contact();

                //判断是否是手机通讯录
                if (ml.get(i).getProfileSourceType() == 1) {
                    Member.MobileDetail[] md = ml.get(i).getDetail();
                    if (null != md && md.length > 0) {
                        String mobiles = "", emails = "";
                        for (Member.MobileDetail mobileDetail : md) {
                            if ("1".equals(mobileDetail.getContentType())) {//email
                                emails = emails + mobileDetail.getContent() + ";";
                            } else {//mobile
                                mobiles = mobiles + mobileDetail.getContent() + ";";
                            }
                        }
                        if (mobiles.length() > 1 && mobiles.endsWith(";")) {
                            mobiles = mobiles.substring(0, mobiles.length() - 1);
                            ct.setTel(mobiles);
                        }
                        if (emails.length() > 1 && emails.endsWith(";")) {
                            emails = emails.substring(0, emails.length() - 1);
                            ct.setEmail(emails);
                        }
                    }
                    ct.setShortName(ml.get(i).getShortName());
                    ct.setColorIndex(ml.get(i).getColorIndex());
                } else if (ml.get(i).getProfileSourceType() == 2) {//名片
                    Member.MobileDetail[] md = ml.get(i).getDetail();
                    Gson gson = new GsonBuilder().create();
                    String vCardContact = gson.toJson(md);
                    ct.setVcardContent(vCardContact);
                    ct.setShortName(ml.get(i).getShortName());
                    ct.setColorIndex(ml.get(i).getColorIndex());
                } else {
                    ct.setTel(ml.get(i).getTel());
                }

                ct.setId(ml.get(i).getSid());
                ct.setName(ml.get(i).getName());
                ct.setJob(ml.get(i).getTitle());
                ct.setCompany(ml.get(i).getCompany());
                ct.setContactface(ml.get(i).getUserface());
                ct.setMySid(mySid);
                ct.setAccountType(ml.get(i).getAccountType());
                ct.setRealname(ml.get(i).isRealname());
                ct.setBlockedContact(ml.get(i).isBlockedContact());
                ct.setBeBlocked(ml.get(i).isBeBlocked());
                ct.setImValid(ml.get(i).isImValid());
                ct.setImId(ml.get(i).getImId());
                ct.setMobile(ml.get(i).getMobile());
                ct.setProfileSourceType(ml.get(i).getProfileSourceType());
                ct.setCardId(ml.get(i).getCardId());
                count += ctDBHelper.insertOrUpdateUser(ct);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            ctDBHelper.closeDB();
        }
        return count;
    }

    /**
     * 自己 mySid 找到表中对应的数据
     * 根据联系人sid删除联系人
     */
    public boolean deleteMyContactBySid(String mySid, String sid) throws Exception {
        boolean flag = true;
        String sqlStr = "delete from " + TablesConstant.CONTACT_TABLE + " where " + TablesConstant.CONTACT_TABLE_COLUMN_MYSID
                + " = '" + mySid + "'" + " and " + TablesConstant.CONTACT_TABLE_COLUMN_ID + " = '" + sid + "'";
        ContactDBHelper ctDBHelper = new ContactDBHelper(mRenheApplication.getApplicationContext(), TablesConstant.CONTACT_TABLE);
        try {
            flag = ctDBHelper.delContact(sqlStr);
        } catch (Exception e) {
            throw e;
        } finally {
            ctDBHelper.closeDB();
        }
        return flag;
    }

    public boolean deleteMyVCardContactBySid(String sid, int vCard) throws Exception {
        boolean flag = true;
        String sqlStr = "delete from " + TablesConstant.CONTACT_TABLE + " where " + TablesConstant.CONTACT_TABLE_COLUMN_MYSID
                + " = '" + sid + "'" + " and " + TablesConstant.CONTACT_TABLE_COLUMN_CARDID + " = '" + vCard + "'";
        ContactDBHelper ctDBHelper = new ContactDBHelper(mRenheApplication.getApplicationContext(), TablesConstant.CONTACT_TABLE);
        try {
            flag = ctDBHelper.delContact(sqlStr);
        } catch (Exception e) {
            throw e;
        } finally {
            ctDBHelper.closeDB();
        }
        return flag;
    }

    /**
     * 自己 mySid 找到表中对应的数据
     * 根据联系人sid查找联系人
     */
    public Contact getMyContactBySid(String mySid, String sid) throws Exception {
        ContactDBHelper ctDBHelper = new ContactDBHelper(mRenheApplication.getApplicationContext(), TablesConstant.CONTACT_TABLE);
        try {
            return ctDBHelper.findContactBySid(mySid, sid);
        } catch (Exception e) {
            throw e;
        } finally {
            ctDBHelper.closeDB();
        }
    }

    /**
     * 保存用户拉取联系人列表是否成功
     * 改成根据sid
     * 续存 maxLastUpdatedDate modify by chan 2015.6.29
     */
    public long saveContactisSuccess(int maxCid, long maxLastUpdatedDate, String sid) throws Exception {
        ContactDBHelper ctDBHelper = new ContactDBHelper(mRenheApplication.getApplicationContext(),
                TablesConstant.CONTACTISSAVE_TABLE);
        long count = 0;
        try {
            count += ctDBHelper.insertOrUpdateContact(sid, maxCid, maxLastUpdatedDate);
        } catch (Exception e) {
            throw e;
        } finally {
            ctDBHelper.closeDB();
        }
        return count;
    }

    /**
     * 保存用户拉取联系人列表是否成功
     * 改成根据sid
     */
    public long saveContactIsSuccess_V2(ContactIsSave contactIsSave) throws Exception {
        ContactDBHelper ctDBHelper = new ContactDBHelper(mRenheApplication.getApplicationContext(),
                TablesConstant.CONTACTISSAVE_TABLE);
        long count = 0;
        try {
            count += ctDBHelper.insertOrUpdateContactSave(contactIsSave);
        } catch (Exception e) {
            throw e;
        } finally {
            ctDBHelper.closeDB();
        }
        return count;
    }

    /**
     * 根据用户email去查maxcid 已过时 2015.4.13
     */
    @Deprecated
    public int getContactMaxCidByEmail(String email) throws Exception {
        ContactDBHelper ctDBHelper = new ContactDBHelper(mRenheApplication.getApplicationContext(),
                TablesConstant.CONTACTISSAVE_TABLE);
        try {
            return ctDBHelper.findContactMaxCidByEmail(email);
        } catch (Exception e) {
            throw e;
        } finally {
            ctDBHelper.closeDB();
        }
    }

    /**
     * 根据用户sid 去查 maxcid
     */
    public int getContactMaxCidBySid(String sid) throws Exception {
        ContactDBHelper ctDBHelper = new ContactDBHelper(mRenheApplication.getApplicationContext(),
                TablesConstant.CONTACTISSAVE_TABLE);
        try {
            return ctDBHelper.findContactMaxCidBySid(sid);
        } catch (Exception e) {
            throw e;
        } finally {
            ctDBHelper.closeDB();
        }
    }

    /**
     * 根据用户sid 去查 MaxLastUpdatedDate add by chan 2015.6.29
     */
    public long getContactMaxLastUpdatedDateBySid(String sid) throws Exception {
        ContactDBHelper ctDBHelper = new ContactDBHelper(mRenheApplication.getApplicationContext(),
                TablesConstant.CONTACTISSAVE_TABLE);
        try {
            return ctDBHelper.findContactMaxLastUpdatedDateBySid(sid);
        } catch (Exception e) {
            throw e;
        } finally {
            ctDBHelper.closeDB();
        }
    }

    /**
     * 根据联系人sid查找最大的id
     */
    @Override
    public ContactIsSave getContactMaxIdBySid(String sid) {
        ContactIsSave contactIsSave = null;
        ContactDBHelper ctDBHelper = new ContactDBHelper(mRenheApplication.getApplicationContext(),
                TablesConstant.CONTACTISSAVE_TABLE);
        try {
            contactIsSave = ctDBHelper.findContactMaxIdsBySid(sid);
        } catch (Exception e) {
            throw e;
        } finally {
            ctDBHelper.closeDB();
        }
        return contactIsSave;
    }

    /**
     * 获取所有联系人 把email变更为mySid 为查询
     */
    @Override
    public Contact[] getAllContact(String mySid) throws Exception {
        ContactDBHelper ctDBHelper = new ContactDBHelper(mRenheApplication.getApplicationContext(), TablesConstant.CONTACT_TABLE);
        try {
            return ctDBHelper.findAllContact(mySid);
        } catch (Exception e) {
            throw e;
        } finally {
            ctDBHelper.closeDB();
        }
    }

    /**
     * 获取所有和聊联系人
     */
    @Override
    public List<Contact> getHLContact(String mySid) throws Exception {
        ContactDBHelper ctDBHelper = new ContactDBHelper(mRenheApplication.getApplicationContext(), TablesConstant.CONTACT_TABLE);
        try {
            return ctDBHelper.findHLContacts(mySid);
        } catch (Exception e) {
            throw e;
        } finally {
            ctDBHelper.closeDB();
        }
    }

    /**
     * 获取keyword指定条件下所有联系人 把email变更为mySid 为查询
     * isLimit:是否查询条数限制，true则返回3条
     */
    @Override
    public Contact[] getSearchContact(String mySid, String keyword, boolean isLimit) throws Exception {
        ContactDBHelper ctDBHelper = new ContactDBHelper(mRenheApplication.getApplicationContext(), TablesConstant.CONTACT_TABLE);
        try {
            return ctDBHelper.findSearchContact(mySid, keyword, isLimit);
        } catch (Exception e) {
            throw e;
        } finally {
            ctDBHelper.closeDB();
        }
    }

    /**
     * 清空联系人缓存
     */
    public boolean deleteContacts(String sid) throws Exception {
        boolean flag = true;
        String sqlStr = "delete from " + TablesConstant.CONTACT_TABLE + " where " + TablesConstant.CONTACT_TABLE_COLUMN_MYSID
                + " = '" + sid + "'";
        ContactDBHelper ctDBHelper = new ContactDBHelper(mRenheApplication.getApplicationContext(), TablesConstant.CONTACT_TABLE);
        try {
            flag = ctDBHelper.delContact(sqlStr);
        } catch (Exception e) {
            throw e;
        } finally {
            ctDBHelper.closeDB();
        }
        return flag;
    }

    /**
     * 获取新的朋友
     */
    public NewInnerMessage getNewFriendsCount(String sid, String adSId, int sinceId) throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("sid", sid);
        reqParams.put("adSId", adSId);
        reqParams.put("sinceId", sinceId);// 上次请求获取到的最大sinceId，提醒只提醒sinceId之后的朋友数量,默认为0
        return (NewInnerMessage) HttpUtil.doHttpRequest(Constants.Http.GETNEWFRIENDS_COUNT, reqParams, NewInnerMessage.class,
                mRenheApplication);
    }

    /**
     * 保存 Email 联系人
     */
    @Override
    public long saveEmailContactList(List<MailBoxContact> ml, String sid, String time) throws Exception {
        ContactDBHelper ctDBHelper = new ContactDBHelper(mRenheApplication.getApplicationContext(),
                TablesConstant.EMAIL_CONTACT_TABLE);
        long count = 0;
        try {
            for (int i = 0; i < ml.size(); i++) {
                count += ctDBHelper.insertOrUpdateEmailContact(sid, time, ml.get(i));
            }
        } catch (Exception e) {
            throw e;
        } finally {
            ctDBHelper.closeDB();
        }
        return count;
    }

    /**
     * 查询所有 Email 联系人
     */
    @Override
    public List<MailBoxContact> getAllEmailContact(String sid) throws Exception {
        ContactDBHelper ctDBHelper = new ContactDBHelper(mRenheApplication.getApplicationContext(),
                TablesConstant.EMAIL_CONTACT_TABLE);
        try {
            return ctDBHelper.findAllEmailContact(sid);
        } catch (Exception e) {
            throw e;
        } finally {
            ctDBHelper.closeDB();
        }
    }

    /**
     * 清空Email联系人缓存
     */
    public boolean deleteEmailContacts(String sid) throws Exception {
        // TODO Auto-generated method stub
        boolean flag = true;
        String sqlStr = "delete from " + TablesConstant.EMAIL_CONTACT_TABLE + " where "
                + TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_MYSID + " = '" + sid + "'";
        ContactDBHelper ctDBHelper = new ContactDBHelper(mRenheApplication.getApplicationContext(),
                TablesConstant.EMAIL_CONTACT_TABLE);
        try {
            flag = ctDBHelper.delContact(sqlStr);
        } catch (Exception e) {
            throw e;
        } finally {
            ctDBHelper.closeDB();
        }
        return flag;
    }

    /**
     * 获取email联系人的最后保存时间
     */
    @Override
    public long getEmailContactSaveTimeByEmail(String sid) {
        ContactDBHelper ctDBHelper = new ContactDBHelper(mRenheApplication.getApplicationContext(),
                TablesConstant.EMAIL_CONTACT_TABLE);
        try {
            return ctDBHelper.findContactSaveTimeByEmail(sid);
        } catch (Exception e) {
            return 0;
        } finally {
            ctDBHelper.closeDB();
        }
    }

    /****
     * 查询手机联系人
     ****/
    @Override
    public long saveMobileContactList(List<ContactResult> ml, String sid, int maxid) throws Exception {
        ContactDBHelper ctDBHelper = new ContactDBHelper(mRenheApplication.getApplicationContext(),
                TablesConstant.MOBILE_CONTACT_TABLE);
        long count = 0;
        try {
            for (int i = 0; i < ml.size(); i++) {
                count += ctDBHelper.insertOrUpdateMobileContact(sid, maxid, ml.get(i));
            }
        } catch (Exception e) {
            throw e;
        } finally {
            ctDBHelper.closeDB();
        }
        return count;
    }

    public List<ContactResult> getAllMobileContact(String sid) throws Exception {
        ContactDBHelper ctDBHelper = new ContactDBHelper(mRenheApplication.getApplicationContext(),
                TablesConstant.MOBILE_CONTACT_TABLE);
        try {
            return ctDBHelper.findAllMobileContact(sid);
        } catch (Exception e) {
            throw e;
        } finally {
            ctDBHelper.closeDB();
        }
    }

    /**
     * 根据sid去获取手机联系人的maxid
     */
    @Override
    public int getMobileContactMaxidByEmail(String sid) {
        ContactDBHelper ctDBHelper = new ContactDBHelper(mRenheApplication.getApplicationContext(),
                TablesConstant.MOBILE_CONTACT_TABLE);
        try {
            return ctDBHelper.findMobileContactMaxidByEmail(sid);
        } catch (Exception e) {
            return 0;
        } finally {
            ctDBHelper.closeDB();
        }
    }

    /**
     * 清空Mobile联系人缓存
     */
    public boolean deleteMobileContacts(String sid) throws Exception {
        // TODO Auto-generated method stub
        boolean flag = true;
        String sqlStr = "delete from " + TablesConstant.MOBILE_CONTACT_TABLE + " where "
                + TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_MYSID + " = '" + sid + "'";
        ContactDBHelper ctDBHelper = new ContactDBHelper(mRenheApplication.getApplicationContext(),
                TablesConstant.MOBILE_CONTACT_TABLE);
        try {
            flag = ctDBHelper.delContact(sqlStr);
        } catch (Exception e) {
            throw e;
        } finally {
            ctDBHelper.closeDB();
        }
        return flag;
    }

    /**
     * 删除联系人
     */
    public void deleteContactBySid(String sid) throws Exception {
        ContactDBHelper ctDBHelper = new ContactDBHelper(mRenheApplication.getApplicationContext(), TablesConstant.CONTACT_TABLE);
        try {
            ctDBHelper.delContactBySid(sid);
        } catch (Exception e) {
            throw e;
        } finally {
            ctDBHelper.closeDB();
        }
    }

    @Override
    public long updataContactBlock(String sid, boolean isBlocked) throws Exception {
        ContactDBHelper ctDBHelper = new ContactDBHelper(mRenheApplication.getApplicationContext(), TablesConstant.CONTACT_TABLE);
        long result;
        try {
            result = ctDBHelper.updataBlock(sid, isBlocked);
        } catch (Exception e) {
            throw e;
        } finally {
            ctDBHelper.closeDB();
        }
        return result;
    }

}
