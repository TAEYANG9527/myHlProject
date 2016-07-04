package com.itcalf.renhe.context.contacts;

import com.itcalf.renhe.bean.HlContactRenheMember;
import com.itcalf.renhe.bean.HlMemberSaveToMail;

import java.util.Comparator;

/**
 * description :按拼音排序
 * Created by Chans Renhenet
 * 2015/12/23
 */
public class PinyinComparator implements Comparator {

    @Override
    public int compare(Object arg0, Object arg1) {
        // 按照名字排序
        HlContactRenheMember ct0 = ((HlMemberSaveToMail) arg0).getHlContactRenheMember();
        HlContactRenheMember ct1 = ((HlMemberSaveToMail) arg1).getHlContactRenheMember();
        String catalog0 = "";
        String catalog1 = "";

        if (ct0 != null && ct0.getName() != null
                && ct0.getName().length() > 1)
            catalog0 = ct0.getInitialOfFullPinYin();

        if (ct1 != null && ct1.getName() != null
                && ct1.getName().length() > 1)
            catalog1 = ct1.getInitialOfFullPinYin();
        int flag = catalog0.compareTo(catalog1);
        return flag;
    }
}