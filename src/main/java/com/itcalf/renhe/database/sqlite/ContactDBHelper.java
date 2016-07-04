package com.itcalf.renhe.database.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;

import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.bean.ContactIsSave;
import com.itcalf.renhe.bean.ContactResultByMailBox.MailBoxContact;
import com.itcalf.renhe.bean.ContactsReturn.ContactResult;
import com.itcalf.renhe.bean.RenheMemberInfo;
import com.itcalf.renhe.bean.RenheMemberInfo.InvitedInfo;
import com.itcalf.renhe.po.Contact;

import java.util.ArrayList;
import java.util.List;

public class ContactDBHelper extends BaseDBHelper {

    public ContactDBHelper(Context context, String table) {
        super(context, table);
    }

    /**
     * 获取所有联系人
     * 根据sid 去获取  modify by chan 2015.6.30
     *
     * @return
     */
    public synchronized Contact[] findAllContact(String mySid) {
        Cursor cursor = find(null, TablesConstant.CONTACT_TABLE_COLUMN_MYSID + "=?", new String[]{mySid}, null, null,
                TablesConstant.CONTACT_TABLE_COLUMN_NAME + " DESC");
        Contact[] contacts = null;
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                contacts = new Contact[cursor.getCount()];
                do {
                    Contact contact = new Contact();
                    contentValueToPo(cursor, contact);
                    contacts[cursor.getPosition()] = contact;
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return contacts;
    }

    /**
     * 获取所有的和聊好友 (过滤黑名单)
     */
    public synchronized List<Contact> findHLContacts(String mySid) {
        Cursor cursor = find(null, TablesConstant.CONTACT_TABLE_COLUMN_MYSID + "=? and "
                        + TablesConstant.CONTACT_TABLE_COLUMN_PROFILESOURCETYPE + "=? and "
                        + TablesConstant.CONTACT_TABLE_COLUMN_BLOCKEDCONTACT + "=?",
                new String[]{mySid, "0", "0"}, null, null, TablesConstant.CONTACT_TABLE_COLUMN_NAME + " DESC");
        List<Contact> contacts = new ArrayList<>();
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                do {
                    Contact contact = new Contact();
                    contentValueToPo(cursor, contact);
                    contacts.add(contact);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return contacts;
    }

    /**
     * 条件 获取 联系人
     * 根据sid 去获取
     *
     * @return
     */
    public synchronized Contact[] findSearchContact(String mySid, String keyWord, boolean isLimit) {
        /**
         * sql: select * from CONTACT_ADDVIP_ADDIM_ADDBLOCK_HL where MYSID="1149e800e8ac2981" and NAME like '%俊%' order by PROFILESOURCETYPE asc limit 3
         */
        String limit = "";
        if (isLimit)
            limit = " limit " + 3;
        Cursor cursor = find(null, TablesConstant.CONTACT_TABLE_COLUMN_MYSID + "=? and "
                        + TablesConstant.CONTACT_TABLE_COLUMN_NAME + " like ?", new String[]{mySid, "%" + keyWord + "%"}, null, null,
                TablesConstant.CONTACT_TABLE_COLUMN_PROFILESOURCETYPE + " ASC" + limit);
        Contact[] contacts = null;
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                contacts = new Contact[cursor.getCount()];
                do {
                    Contact contact = new Contact();
                    contentValueToPo(cursor, contact);
                    contacts[cursor.getPosition()] = contact;
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return contacts;
    }

    /**
     * 获取单个联系人
     * 根据sid
     *
     * @return
     */
    public synchronized Contact findContactBySid(String mySid, String contactSid) {
        Cursor cursor = find(null,
                TablesConstant.CONTACT_TABLE_COLUMN_MYSID + "=? AND " + TablesConstant.CONTACT_TABLE_COLUMN_ID + "=?",
                new String[]{mySid, contactSid}, null, null, null);
        Contact contact = null;
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                contact = new Contact();
                contentValueToPo(cursor, contact);
            }
            cursor.close();
        }
        return contact;
    }

    /**
     * 插入一个用户账户
     *
     * @param contact
     * @return
     */
    public synchronized long insertOrUpdateUser(Contact contact) {
        ContentValues values = poToContentValues(contact);
        if (isExistContent(TablesConstant.CONTACT_TABLE_COLUMN_ID, contact.getId(), TablesConstant.CONTACT_TABLE_COLUMN_MYSID,
                contact.getMySid())) {
            return updateContact(contact);
        } else {
            return insertData(values);
        }
    }

    /**
     * 获取当前用户（Email）下面所有的Email联系人
     */
    public synchronized List<MailBoxContact> findAllEmailContact(String sid) {
        Cursor cursor = find(null, TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_MYSID + "=?", new String[]{sid}, null, null,
                TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_ID + " DESC");
        List<MailBoxContact> contacts = null;
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                contacts = new ArrayList<MailBoxContact>();
                do {
                    MailBoxContact contact = new MailBoxContact();
                    emailContentValueToPo(cursor, contact);
                    contacts.add(contact);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return contacts;
    }

    /**
     * 获取当前用户（Email）下面所有的Mobile联系人
     */
    public synchronized List<ContactResult> findAllMobileContact(String sid) {
        Cursor cursor = find(null, TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_MYSID + "=?", new String[]{sid}, null, null,
                TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_ID + " DESC");
        List<ContactResult> contacts = null;
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                contacts = new ArrayList<ContactResult>();
                do {
                    ContactResult contact = new ContactResult();
                    mobileContentValueToPo(cursor, contact);
                    contacts.add(contact);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return contacts;
    }

    /**
     * 新增或更新用户的一个email好友
     */
    public synchronized long insertOrUpdateEmailContact(String sid, String time, MailBoxContact contact) {
        ContentValues values = poToEmailContentValues(sid, time, contact);
        boolean isExist = isExistContent(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_ID, "" + contact.getContactId(),
                TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_MYSID, sid);
        if (isExist) {
            return updateEmailContact(contact, sid, time);
        } else {
            return insertData(values);
        }
    }

    /**
     * 新增或更新用户的一个mobile好友
     */
    public synchronized long insertOrUpdateMobileContact(String sid, int maxid, ContactResult contact) {
        ContentValues values = poToMobileContentValues(sid, maxid, contact);
        boolean isExist = isExistContent(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_ID, contact.getContactId(),
                TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_MYSID, sid);
        if (isExist) {
            return updateMobileContact(contact, sid, maxid);
        } else {
            return insertData(values);
        }
    }

    /**
     * 根据用户名sid更新用户账户
     *
     * @param contact
     * @return
     */
    public synchronized long updateContact(Contact contact) {
        ContentValues values = poToContentValues(contact);
        return updateData(values,
                TablesConstant.CONTACT_TABLE_COLUMN_ID + "=? and " + TablesConstant.CONTACT_TABLE_COLUMN_MYSID + "=?",
                new String[]{contact.getId(), contact.getMySid()});
    }

    /**
     * 根据email更新用户好友账户
     *
     * @param sid
     * @return
     */
    public synchronized long updateEmailContact(MailBoxContact contact, String sid, String time) {
        ContentValues values = poToEmailContentValues(sid, time, contact);
        return updateData(values, TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_ID + "=? and "
                + TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_MYSID + "=? ", new String[]{"" + contact.getContactId(), sid});
    }

    /**
     * 根据email更新用户好友账户
     *
     * @param email
     * @return
     */
    public synchronized long updateMobileContact(ContactResult contact, String email, int maxid) {
        ContentValues values = poToMobileContentValues(email, maxid, contact);
        return updateData(
                values, TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_ID + "=? and "
                        + TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_MYSID + "=? ",
                new String[]{"" + contact.getContactId(), email});
    }

    /**
     * 根据用户Sid删除用户账户
     *
     * @param sid
     * @return
     */
    public synchronized long delContactBySid(String sid) {
        return delData(TablesConstant.CONTACT_TABLE_COLUMN_ID + "=?", new String[]{sid});
    }

    /**
     * 删除当前帐号的联系人
     */
    public synchronized boolean delContact(String sql) {
        return delContant(sql);
    }

    /**
     * 删除所有数据
     *
     * @return
     */
    public synchronized long delAll() {
        return delData(null, null);
    }

    /**
     * 新建表是否获取联系人完成
     * modify email 为sid
     *
     * @param sid
     * @return
     */
    public long insertOrUpdateContact(String sid, int maxCid, long maxLastUpdatedDate) {
        ContentValues values = poToContent(sid, maxCid, maxLastUpdatedDate);
        if (isExist(TablesConstant.CONTACTISSAVE_SID, sid)) {
            return updateContactIsSave(sid, maxCid, maxLastUpdatedDate);
        } else {
            return insertData(values);
        }
    }

    //是否获取联系人完成
    public long insertOrUpdateContactSave(ContactIsSave contactIsSave) {
        ContentValues values = poToContentSave(contactIsSave);
        if (isExist(TablesConstant.CONTACTISSAVE_SID, contactIsSave.getSid())) {
            return updateContactIsSave_V2(contactIsSave);
        } else {
            return insertData(values);
        }
    }

    /**
     * 保存联系人相关
     *
     * @param maxCid
     * @return
     */
    public ContentValues poToContent(String sid, int maxCid, long maxLastUpdatedDate) {
        ContentValues values = new ContentValues();
        values.put(TablesConstant.CONTACTISSAVE_SID, sid);
        values.put(TablesConstant.CONTACTISSAVE_MAXCID, maxCid);
        values.put(TablesConstant.CONTACTISSAVE_MAXLASTUPDATEDDATE, maxLastUpdatedDate);
        return values;
    }

    /**
     * 保存联系人相关
     *
     * @param contactIsSave
     * @return
     */
    public ContentValues poToContentSave(ContactIsSave contactIsSave) {
        ContentValues values = new ContentValues();
        values.put(TablesConstant.CONTACTISSAVE_SID, contactIsSave.getSid());
        values.put(TablesConstant.CONTACTISSAVE_MAXCID, contactIsSave.getMaxCid());
        values.put(TablesConstant.CONTACTISSAVE_MAXMOBILEID, contactIsSave.getMaxMobileId());
        values.put(TablesConstant.CONTACTISSAVE_MAXCARDID, contactIsSave.getMaxCardId());
        values.put(TablesConstant.CONTACTISSAVE_MAXLASTUPDATEDDATE, contactIsSave.getMaxLastUpdatedDate());
        return values;
    }

    /**
     * 保存联系人相关
     *
     * @param sid
     * @param maxCid
     * @return
     */
    public synchronized long updateContactIsSave(String sid, int maxCid, long maxLastUpdatedDate) {
        ContentValues values = poToContent(sid, maxCid, maxLastUpdatedDate);
        return updateData(values, TablesConstant.CONTACTISSAVE_SID + "=?", new String[]{sid});
    }

    public synchronized long updateContactIsSave_V2(ContactIsSave contactIsSave) {
        ContentValues values = poToContentSave(contactIsSave);
        return updateData(values, TablesConstant.CONTACTISSAVE_SID + "=?", new String[]{contactIsSave.getSid()});
    }

    /**
     * 保存联系人相关,根据用户email去查maxcid
     *
     * @param email
     * @return 已过时[XXX]
     */
    public synchronized int findContactMaxCidByEmail(String email) {
        return findByTerm(email);
    }

    /**
     * 保存联系人相关,根据用户sid去查maxcid
     *
     * @param sid
     */
    public synchronized int findContactMaxCidBySid(String sid) {
        return findByTerm(sid);
    }

    /**
     * 保存联系人相关,根据用户sid去查MaxLastUpdatedDate
     *
     * @param sid
     */
    public synchronized long findContactMaxLastUpdatedDateBySid(String sid) {
        return findMaxLastUpdatedDateByTerm(sid);
    }

    public synchronized ContactIsSave findContactMaxIdsBySid(String sid) {
        return findMaxIdsByTerm(sid);
    }

    public ContentValues poToContentValues(Contact contact) {
        ContentValues values = new ContentValues();
        values.put(TablesConstant.CONTACT_TABLE_COLUMN_ID, contact.getId());
        values.put(TablesConstant.CONTACT_TABLE_COLUMN_MYSID, contact.getMySid());
        values.put(TablesConstant.CONTACT_TABLE_COLUMN_NAME, contact.getName());
        if (null != contact.getJob()) {
            values.put(TablesConstant.CONTACT_TABLE_COLUMN_JOB, contact.getJob());
        }
        if (null != contact.getCompany()) {
            values.put(TablesConstant.CONTACT_TABLE_COLUMN_COMPANY, contact.getCompany());
        }
        if (null != contact.getContactface()) {
            values.put(TablesConstant.CONTACT_TABLE_COLUMN_CONTACTFACE, contact.getContactface());
        }
        values.put(TablesConstant.CONTACT_TABLE_COLUMN_ACCOUNTTYPE, contact.getAccountType());
        if (contact.isRealname()) {
            values.put(TablesConstant.CONTACT_TABLE_COLUMN_REALNAME, true);
        } else {
            values.put(TablesConstant.CONTACT_TABLE_COLUMN_REALNAME, false);
        }
        if (contact.isBlockedContact()) {
            values.put(TablesConstant.CONTACT_TABLE_COLUMN_BLOCKEDCONTACT, true);
        } else {
            values.put(TablesConstant.CONTACT_TABLE_COLUMN_BLOCKEDCONTACT, false);
        }
        if (contact.isBeBlocked()) {
            values.put(TablesConstant.CONTACT_TABLE_COLUMN_BEBLOCKED, true);
        } else {
            values.put(TablesConstant.CONTACT_TABLE_COLUMN_BEBLOCKED, false);
        }
        if (contact.isImValid()) {
            values.put(TablesConstant.CONTACT_TABLE_COLUMN_ISIMVALID, true);
        } else {
            values.put(TablesConstant.CONTACT_TABLE_COLUMN_ISIMVALID, false);
        }
        values.put(TablesConstant.CONTACT_TABLE_COLUMN_IMID, contact.getImId());
        values.put(TablesConstant.CONTACT_TABLE_COLUMN_MOBILE, contact.getMobile());
        values.put(TablesConstant.CONTACT_TABLE_COLUMN_TEL, contact.getTel());

        values.put(TablesConstant.CONTACT_TABLE_COLUMN_CARDID, contact.getCardId());
        if (null != contact.getVcardContent()) {
            values.put(TablesConstant.CONTACT_TABLE_COLUMN_VCARDCONTENT, contact.getVcardContent());
        }
        values.put(TablesConstant.CONTACT_TABLE_COLUMN_PROFILESOURCETYPE, contact.getProfileSourceType());
        if (null != contact.getEmail()) {
            values.put(TablesConstant.CONTACT_TABLE_COLUMN_EMAIL, contact.getEmail());
        }
        if (null != contact.getShortName()) {
            values.put(TablesConstant.CONTACT_TABLE_COLUMN_SHORTNAME, contact.getShortName());
        }
        values.put(TablesConstant.CONTACT_TABLE_COLUMN_COLORINDEX, contact.getColorIndex());
        return values;
    }

    public void contentValueToPo(Cursor cursor, Contact contact) {
        ContentValues values = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, values);
        contact.setId(values.getAsString(TablesConstant.CONTACT_TABLE_COLUMN_ID));
        contact.setMySid(values.getAsString(TablesConstant.CONTACT_TABLE_COLUMN_MYSID));
        contact.setName(values.getAsString(TablesConstant.CONTACT_TABLE_COLUMN_NAME));
        contact.setJob(values.getAsString(TablesConstant.CONTACT_TABLE_COLUMN_JOB));
        contact.setCompany(values.getAsString(TablesConstant.CONTACT_TABLE_COLUMN_COMPANY));
        contact.setContactface(values.getAsString(TablesConstant.CONTACT_TABLE_COLUMN_CONTACTFACE));
        contact.setAccountType(values.getAsInteger(TablesConstant.CONTACT_TABLE_COLUMN_ACCOUNTTYPE));
        if (values.getAsInteger(TablesConstant.CONTACT_TABLE_COLUMN_REALNAME) == 1) {
            contact.setRealname(true);
        } else {
            contact.setRealname(false);
        }
        if (values.getAsInteger(TablesConstant.CONTACT_TABLE_COLUMN_BLOCKEDCONTACT) == 1) {
            contact.setBlockedContact(true);
        } else {
            contact.setBlockedContact(false);
        }
        if (values.getAsInteger(TablesConstant.CONTACT_TABLE_COLUMN_BEBLOCKED) == 1) {
            contact.setBeBlocked(true);
        } else {
            contact.setBeBlocked(false);
        }
        if (values.getAsInteger(TablesConstant.CONTACT_TABLE_COLUMN_ISIMVALID) == 1) {
            contact.setImValid(true);
        } else {
            contact.setImValid(false);
        }
        contact.setImId(values.getAsInteger(TablesConstant.CONTACT_TABLE_COLUMN_IMID));
        contact.setMobile(values.getAsString(TablesConstant.CONTACT_TABLE_COLUMN_MOBILE));
        contact.setTel(values.getAsString(TablesConstant.CONTACT_TABLE_COLUMN_TEL));
        if (null != values.getAsInteger(TablesConstant.CONTACT_TABLE_COLUMN_CARDID)) {
            contact.setCardId(values.getAsInteger(TablesConstant.CONTACT_TABLE_COLUMN_CARDID));
        }
        if (null != values.getAsString(TablesConstant.CONTACT_TABLE_COLUMN_VCARDCONTENT)) {
            contact.setVcardContent(values.getAsString(TablesConstant.CONTACT_TABLE_COLUMN_VCARDCONTENT));
        }
        if (null != values.getAsInteger(TablesConstant.CONTACT_TABLE_COLUMN_PROFILESOURCETYPE)) {
            contact.setProfileSourceType(values.getAsInteger(TablesConstant.CONTACT_TABLE_COLUMN_PROFILESOURCETYPE));
        }
        if (null != values.getAsString(TablesConstant.CONTACT_TABLE_COLUMN_EMAIL)) {
            contact.setEmail(values.getAsString(TablesConstant.CONTACT_TABLE_COLUMN_EMAIL));
        }
        if (null != values.getAsString(TablesConstant.CONTACT_TABLE_COLUMN_SHORTNAME)) {
            contact.setShortName(values.getAsString(TablesConstant.CONTACT_TABLE_COLUMN_SHORTNAME));
        }
        if (null != values.getAsInteger(TablesConstant.CONTACT_TABLE_COLUMN_COLORINDEX)) {
            contact.setColorIndex(values.getAsInteger(TablesConstant.CONTACT_TABLE_COLUMN_COLORINDEX));
        }
    }

    /*********
     * 用户的email联系人:存
     *************/
    public ContentValues poToEmailContentValues(String sid, String time, MailBoxContact contact) {
        ContentValues values = new ContentValues();
        values.put(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_MYSID, sid);
        values.put(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_CREATETIME, time);
        values.put(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_ID, contact.getContactId());
        if (null != contact.getName()) {
            values.put(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_NAME, contact.getName());
        }
        values.put(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_EMAIL, contact.getEmail());
        if (contact.isRenheMember()) {
            values.put(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_ISRENHEMEMBER, true);
            values.put(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_MEMBERSID, contact.getRenheMemberInfo().getMemberSId());
            if (contact.getRenheMemberInfo().isSelf()) {
                values.put(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_ISSELF, true);
            } else {
                values.put(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_ISSELF, false);
            }
            if (contact.getRenheMemberInfo().isConnection()) {
                values.put(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_ISCONNECTION, true);
            } else {
                values.put(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_ISCONNECTION, false);
            }
            if (contact.getRenheMemberInfo().isInvite()) {
                values.put(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_ISINVITE, true);
                if (contact.getRenheMemberInfo().getBeInvitedInfo().isBeInvited()) {
                    values.put(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_ISBEINVITED, true);
                } else {
                    values.put(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_ISBEINVITED, false);
                }
                values.put(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_INVITEID,
                        contact.getRenheMemberInfo().getBeInvitedInfo().getInviteId());
                values.put(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_INVITETYPE,
                        contact.getRenheMemberInfo().getBeInvitedInfo().getInviteType());
            } else {
                values.put(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_ISINVITE, false);
            }
            //添加头像，公司，职位
            values.put(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_AVATAR, contact.getRenheMemberInfo().getUserface());
            values.put(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_COMPANY, contact.getRenheMemberInfo().getCompany());
            values.put(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_POSITION, contact.getRenheMemberInfo().getTitle());
        } else {
            values.put(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_ISRENHEMEMBER, false);
        }
        return values;
    }

    /*********
     * 用户的email联系人:取
     *************/
    public void emailContentValueToPo(Cursor cursor, MailBoxContact contact) {
        ContentValues values = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, values);
        contact.setContactId(values.getAsInteger(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_ID));
        contact.setEmail(values.getAsString(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_EMAIL));
        contact.setName(values.getAsString(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_NAME));
        if (values.getAsInteger(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_ISRENHEMEMBER) == 1) {
            contact.setRenheMember(true);
            RenheMemberInfo renheMemberInfo = new RenheMemberInfo();
            renheMemberInfo.setMemberSId(values.getAsString(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_MEMBERSID));
            if (values.getAsInteger(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_ISSELF) == 1) {
                renheMemberInfo.setSelf(true);
            } else {
                renheMemberInfo.setSelf(false);
            }
            if (values.getAsInteger(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_ISCONNECTION) == 1) {
                renheMemberInfo.setConnection(true);
            } else {
                renheMemberInfo.setConnection(false);
            }
            if (values.getAsInteger(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_ISINVITE) == 1) {
                renheMemberInfo.setInvite(true);
                InvitedInfo beinvite = new InvitedInfo();
                if (values.getAsInteger(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_ISBEINVITED) == 1) {
                    beinvite.setBeInvited(true);
                } else {
                    beinvite.setBeInvited(false);
                }
                beinvite.setInviteId(values.getAsInteger(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_INVITEID));
                beinvite.setInviteType(values.getAsInteger(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_INVITETYPE));
                renheMemberInfo.setBeInvitedInfo(beinvite);
            } else {
                renheMemberInfo.setInvite(false);
            }
            //添加头像，公司，职位
            renheMemberInfo.setUserface(values.getAsString(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_AVATAR));
            renheMemberInfo.setCompany(values.getAsString(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_COMPANY));
            renheMemberInfo.setTitle(values.getAsString(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_POSITION));
            contact.setRenheMemberInfo(renheMemberInfo);
        } else {
            contact.setRenheMember(false);
        }
    }

    /*********
     * 根据email获取保存联系人中最后的时间
     *******************/
    public synchronized long findContactSaveTimeByEmail(String sid) {
        return findEmailContactTimeByTerm(sid);
    }

    /*********
     * 用户的mobile联系人:存
     *************/
    public ContentValues poToMobileContentValues(String sid, int maxid, ContactResult contact) {
        ContentValues values = new ContentValues();
        values.put(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_MYSID, sid);
        values.put(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_MAXID, maxid);

        values.put(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_ID, contact.getContactId());
        if (null != contact.getName()) {
            values.put(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_NAME, contact.getName());
        }
        if (contact.isRenheMember()) {
            values.put(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_ISRENHEMEMBER, true);
            values.put(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_MEMBERSID, contact.getRenheMemberInfo().getMemberSId());
            if (contact.getRenheMemberInfo().isSelf()) {
                values.put(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_ISSELF, true);
            } else {
                values.put(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_ISSELF, false);
            }
            if (contact.getRenheMemberInfo().isConnection()) {
                values.put(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_ISCONNECTION, true);
            } else {
                values.put(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_ISCONNECTION, false);
            }
            if (contact.getRenheMemberInfo().isInvite()) {
                values.put(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_ISINVITE, true);
                if (contact.getRenheMemberInfo().getBeInvitedInfo().isBeInvited()) {
                    values.put(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_ISBEINVITED, true);
                } else {
                    values.put(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_ISBEINVITED, false);
                }
                values.put(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_INVITEID,
                        contact.getRenheMemberInfo().getBeInvitedInfo().getInviteId());
                values.put(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_INVITETYPE,
                        contact.getRenheMemberInfo().getBeInvitedInfo().getInviteType());
            } else {
                values.put(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_ISINVITE, false);
            }
            //添加头像，公司，职位
            values.put(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_AVATAR, contact.getRenheMemberInfo().getUserface());
            values.put(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_COMPANY, contact.getRenheMemberInfo().getCompany());
            values.put(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_POSITION, contact.getRenheMemberInfo().getTitle());
        } else {
            values.put(TablesConstant.EMAIL_CONTACT_TABLE_COLUMN_ISRENHEMEMBER, false);
        }
        //emails
        if (null != contact.getEmailItems()) {
            String[] emails = contact.getEmailItems();
            String emailItems = "";
            for (int i = 0; i < emails.length; i++) {
                emailItems = emailItems + emails[i] + ",";
            }
            if (emailItems.length() > 0) {
                emailItems = emailItems.substring(0, emailItems.length() - 1);
            }
            values.put(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_EMAILITEMS, emailItems);
        }
        //mobiles
        if (null != contact.getMobileItems()) {
            String[] emails = contact.getMobileItems();
            String emailItems = "";
            for (int i = 0; i < emails.length; i++) {
                emailItems = emailItems + emails[i] + ",";
            }
            if (emailItems.length() > 0) {
                emailItems = emailItems.substring(0, emailItems.length() - 1);
            }
            values.put(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_MOBILEITEMS, emailItems);
        }
        //telephones
        if (null != contact.getTelephoneItems()) {
            String[] emails = contact.getTelephoneItems();
            String emailItems = "";
            for (int i = 0; i < emails.length; i++) {
                emailItems = emailItems + emails[i] + ",";
            }
            if (emailItems.length() > 0) {
                emailItems = emailItems.substring(0, emailItems.length() - 1);
            }
            values.put(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_TELEPHONEITEMS, emailItems);
        }
        //telephoneOtherItems
        if (null != contact.getTelephoneOtherItems()) {
            String[] emails = contact.getTelephoneOtherItems();
            String emailItems = "";
            for (int i = 0; i < emails.length; i++) {
                emailItems = emailItems + emails[i] + ",";
            }
            if (emailItems.length() > 0) {
                emailItems = emailItems.substring(0, emailItems.length() - 1);
            }
            values.put(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_TELEPHONEOTHERITEMS, emailItems);
        }
        //addressItems
        if (null != contact.getAddressItems()) {
            String[] emails = contact.getAddressItems();
            String emailItems = "";
            for (int i = 0; i < emails.length; i++) {
                emailItems = emailItems + emails[i] + ",";
            }
            if (emailItems.length() > 0) {
                emailItems = emailItems.substring(0, emailItems.length() - 1);
            }
            values.put(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_ADDRESSITEMS, emailItems);
        }
        //otherItems
        if (null != contact.getOtherItems()) {
            String[] emails = contact.getOtherItems();
            String emailItems = "";
            for (int i = 0; i < emails.length; i++) {
                emailItems = emailItems + emails[i] + ",";
            }
            if (emailItems.length() > 0) {
                emailItems = emailItems.substring(0, emailItems.length() - 1);
            }
            values.put(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_OTHERITEMS, emailItems);
        }

        return values;
    }

    /*********
     * 用户的mobile联系人:取
     *************/
    public void mobileContentValueToPo(Cursor cursor, ContactResult contact) {
        ContentValues values = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, values);
        contact.setContactId(values.getAsString(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_ID));
        contact.setName(values.getAsString(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_NAME));
        if (values.getAsInteger(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_ISRENHEMEMBER) == 1) {
            contact.setRenheMember(true);
            RenheMemberInfo renheMemberInfo = new RenheMemberInfo();
            renheMemberInfo.setMemberSId(values.getAsString(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_MEMBERSID));
            if (values.getAsInteger(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_ISSELF) == 1) {
                renheMemberInfo.setSelf(true);
            } else {
                renheMemberInfo.setSelf(false);
            }
            if (values.getAsInteger(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_ISCONNECTION) == 1) {
                renheMemberInfo.setConnection(true);
            } else {
                renheMemberInfo.setConnection(false);
            }
            if (values.getAsInteger(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_ISINVITE) == 1) {
                renheMemberInfo.setInvite(true);
                InvitedInfo beinvite = new InvitedInfo();
                if (values.getAsInteger(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_ISBEINVITED) == 1) {
                    beinvite.setBeInvited(true);
                } else {
                    beinvite.setBeInvited(false);
                }
                beinvite.setInviteId(values.getAsInteger(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_INVITEID));
                beinvite.setInviteType(values.getAsInteger(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_INVITETYPE));
                renheMemberInfo.setBeInvitedInfo(beinvite);
            } else {
                renheMemberInfo.setInvite(false);
            }
            //添加头像，公司，职位
            renheMemberInfo.setUserface(values.getAsString(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_AVATAR));
            renheMemberInfo.setCompany(values.getAsString(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_COMPANY));
            renheMemberInfo.setTitle(values.getAsString(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_POSITION));
            contact.setRenheMemberInfo(renheMemberInfo);
        } else {
            contact.setRenheMember(false);
        }

        String email = values.getAsString(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_EMAILITEMS);
        if (email.length() > 0) {
            String[] emailItems = email.split(",");
            contact.setEmailItems(emailItems);
        } else {
            contact.setEmailItems(new String[0]);
        }

        String mobile = values.getAsString(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_MOBILEITEMS);
        if (mobile.length() > 0) {
            String[] mobileItems = mobile.split(",");
            contact.setMobileItems(mobileItems);
        } else {
            contact.setMobileItems(new String[0]);
        }

        String telep = values.getAsString(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_TELEPHONEITEMS);
        if (telep.length() > 0) {
            String[] telepItems = telep.split(",");
            contact.setTelephoneItems(telepItems);
        } else {
            contact.setTelephoneItems(new String[0]);
        }

        String teleOther = values.getAsString(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_TELEPHONEOTHERITEMS);
        if (teleOther.length() > 0) {
            String[] teleOtherItems = teleOther.split(",");
            contact.setTelephoneOtherItems(teleOtherItems);
        } else {
            contact.setTelephoneOtherItems(new String[0]);
        }

        String address = values.getAsString(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_ADDRESSITEMS);
        if (address.length() > 0) {
            String[] addressItems = address.split(",");
            contact.setAddressItems(addressItems);
        } else {
            contact.setAddressItems(new String[0]);
        }

        String other = values.getAsString(TablesConstant.MOBILE_CONTACT_TABLE_COLUMN_OTHERITEMS);
        if (other.length() > 0) {
            String[] otherItems = other.split(",");
            contact.setOtherItems(otherItems);
        } else {
            contact.setOtherItems(new String[0]);
        }

    }

    /*********
     * 根据email获取保存联系人中Maxid
     *******************/
    public synchronized int findMobileContactMaxidByEmail(String sid) {
        return findMobileContactMaxidByTerm(sid);
    }

    public synchronized long updataBlock(String sid, boolean isBlocked) {
        ContentValues values = new ContentValues();
        values.put(TablesConstant.CONTACT_TABLE_COLUMN_BLOCKEDCONTACT, isBlocked);
        return updateData(values,
                TablesConstant.CONTACT_TABLE_COLUMN_ID + "=? and " + TablesConstant.CONTACT_TABLE_COLUMN_MYSID + "=?",
                new String[]{sid, RenheApplication.getInstance().getUserInfo().getSid()});

    }
}
