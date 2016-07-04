package com.itcalf.renhe.utils;

import com.alibaba.sdk.android.man.MANHitBuilders;
import com.alibaba.sdk.android.man.MANService;
import com.alibaba.sdk.android.man.MANServiceProvider;

import java.util.Map;

/**
 * Created by wangning on 2015/12/9.
 */
public class StatisticsUtil {
    /**
     * 自定义事件埋点
     * <p>
     * 自定义事件埋点可用于满足用户的定制化需求。
     * <p>
     * 自定义事件可包含以下几个部分内容：
     * <p>
     * 1.事件名称（event_label）
     * <p>
     * 2.事件从开始到完成消耗的时长
     * <p>
     * 3.事件所携带的属性
     * <p>
     * 4.事件对应的页面
     *
     * @param eventLabel    事件名称
     * @param eventDuration 事件从开始到完成消耗的时长(暂未使用)
     * @param eventPage     事件对应的页面(暂未使用)
     * @param propertyMap   事件所携带的属性
     */
    public static void statisticsCustomClickEvent(String eventLabel, long eventDuration, String eventPage,
                                                  Map<String, String> propertyMap) {
        // 事件名称：play_music
        MANHitBuilders.MANCustomHitBuilder hitBuilder = new MANHitBuilders.MANCustomHitBuilder(eventLabel);

// 可使用如下接口设置时长：3分钟
//        hitBuilder.setDurationOnEvent(3 * 60 * 1000);

// 设置关联的页面名称：聆听
//        hitBuilder.setEventPage("Listen");
//// 设置属性：类型摇滚
//        hitBuilder.setProperty("type", "rock");
//// 设置属性：歌曲标题
//        hitBuilder.setProperty("title", "wonderful tonight");
        if (null != propertyMap)
            hitBuilder.setProperties(propertyMap);
// 发送自定义事件打点
        MANService manService = MANServiceProvider.getService();
        if (null != manService && null != manService.getMANAnalytics() && null != manService.getMANAnalytics().getDefaultTracker())
            manService.getMANAnalytics().getDefaultTracker().send(hitBuilder.build());
    }
}
