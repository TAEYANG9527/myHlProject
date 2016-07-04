package com.itcalf.renhe.bean;

import cn.renhe.heliao.idl.config.ModuleConfig;

/**
 * Created by wangning on 2016/1/15.
 */
public class MeItem {
    public static final int ME_HECAIFU = 1;
    public static final int ME_ZANFUWU = 2;
    public static final int ME_LUCKYMONEY = 3;
    public static final int ME_AUTH = 4;
    public static final int ME_IDENTIFY = 5;
    public static final int ME_COMPLETE_PROFILE = 6;
    public static final int ME_SETTING = 7;
    private String logo;
    private String name;
    private String tip;
    private int type;
    private boolean isArchiveComplete;//是否完善了资料，只有在“完善资料”item有用
    private ModuleConfig.ModuleItem moduleItem;
    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isArchiveComplete() {
        return isArchiveComplete;
    }

    public void setIsArchiveComplete(boolean isArchiveComplete) {
        this.isArchiveComplete = isArchiveComplete;
    }

    public ModuleConfig.ModuleItem getModuleItem() {
        return moduleItem;
    }

    public void setModuleItem(ModuleConfig.ModuleItem moduleItem) {
        this.moduleItem = moduleItem;
    }
}