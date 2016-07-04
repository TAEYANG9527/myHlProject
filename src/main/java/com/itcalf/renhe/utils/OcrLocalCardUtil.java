package com.itcalf.renhe.utils;

import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.bean.OcrLocalCard;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * 存储在本地的名片工具类，用于从迈克寻获取名片列表的过程中，给用户展示用
 * Created by wangning on 2016/1/14.
 */
public class OcrLocalCardUtil {
    /**
     * 根据uuid初始化一个OcrLocalCard
     *
     * @param cardUuid
     * @return
     */
    public static OcrLocalCard instanceNewCardAndSave(String cardUuid) {
        OcrLocalCard ocrLocalCard = new OcrLocalCard();
        ocrLocalCard.setSid(RenheApplication.getInstance().getUserInfo().getSid());
        ocrLocalCard.setCarduuid(cardUuid);
        ocrLocalCard.setCreatetime(System.currentTimeMillis());
        return ocrLocalCard;
    }

    /**
     * 保存一条记录，如果uuid对应的id存在，则更新，否则插入
     *
     * @param ocrLocalCard
     */
    public static void save(OcrLocalCard ocrLocalCard) {
        if (ocrLocalCard == null)
            return;
        long id = selectId(ocrLocalCard.getCarduuid());
        if (id > 0) {
            ocrLocalCard.update(id);
        } else {
            ocrLocalCard.save();
        }
    }

    /**
     * 删除一条记录，如果uuid对应的id存在，则删除
     *
     * @param cardUUid
     */
    public static void delete(String cardUUid) {
        if (cardUUid == null)
            return;
        long id = selectId(cardUUid);
        if (id > 0) {
            DataSupport.deleteAll(OcrLocalCard.class, "carduuid = ?", cardUUid);
//            ocrLocalCard.delete(OcrLocalCard.class, id);
        }
    }

    /**
     * 根据uuid查询主键id值
     *
     * @param cardUuid
     * @return
     */
    private static long selectId(String cardUuid) {
        List<OcrLocalCard> infos = DataSupport.where("carduuid=?", cardUuid).find(OcrLocalCard.class);
        if (infos != null && !infos.isEmpty()) {
            return infos.get(0).getId();
        }
        return -1;
    }

    /**
     * 更新已有的用户信息，userInfo中的sid属性必须有值
     */
    public static int update(OcrLocalCard ocrLocalCard) {
        if (ocrLocalCard == null)
            return 0;
        return ocrLocalCard.updateAll("carduuid=?", ocrLocalCard.getCarduuid());

    }

    /**
     * 通过cardUuid查询指定名片信息
     */
    public static OcrLocalCard selectByUuid(String cardUuid) {
        List<OcrLocalCard> infos = DataSupport.where("carduuid=?", cardUuid).find(OcrLocalCard.class);
        if (infos != null && !infos.isEmpty()) {
            return infos.get(0);
        }
        return null;
    }

    /**
     * 获取OcrLocalCard表中所有数据
     */
    public static List<OcrLocalCard> findAllCards() {
        List<OcrLocalCard> ocrLocalCards = DataSupport.where("sid = ?", RenheApplication.getInstance().getUserInfo().getSid())
                .order("createtime desc").find(OcrLocalCard.class);
        return ocrLocalCards;
    }
}
