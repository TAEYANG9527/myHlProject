package com.itcalf.renhe.controller;

import com.orhanobut.logger.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.renhe.heliao.idl.base.BaseRequest;
import cn.renhe.heliao.idl.circle.CircleMember;
import cn.renhe.heliao.idl.circle.CircleMemberServiceGrpc;
import cn.renhe.heliao.idl.circle.SearchCircle;
import cn.renhe.heliao.idl.circle.SearchCircleServiceGrpc;
import cn.renhe.heliao.idl.collection.CollectServiceGrpc;
import cn.renhe.heliao.idl.collection.MyCollection;
import cn.renhe.heliao.idl.config.ModuleConfig;
import cn.renhe.heliao.idl.config.ModuleConfigServiceGrpc;
import cn.renhe.heliao.idl.contact.AddFriend;
import cn.renhe.heliao.idl.contact.AddFriendServiceGrpc;
import cn.renhe.heliao.idl.contact.ContactList;
import cn.renhe.heliao.idl.contact.ContactListServiceGrpc;
import cn.renhe.heliao.idl.contact.ImportContact;
import cn.renhe.heliao.idl.contact.ImportContactServiceGrpc;
import cn.renhe.heliao.idl.enterprise.Enterprise;
import cn.renhe.heliao.idl.enterprise.EnterpriseServiceGrpc;
import cn.renhe.heliao.idl.helloworld.HelloWorldServiceGrpc;
import cn.renhe.heliao.idl.helloworld.Helloworld;
import cn.renhe.heliao.idl.member.MemberGrade;
import cn.renhe.heliao.idl.member.MemberGradeServiceGrpc;
import cn.renhe.heliao.idl.member.MyModuleNotice;
import cn.renhe.heliao.idl.member.MyModuleNoticeServiceGrpc;
import cn.renhe.heliao.idl.money.pay.ConfirmPayStatusRequest;
import cn.renhe.heliao.idl.money.pay.HeliaoPayServiceGrpc;
import cn.renhe.heliao.idl.money.pay.PayBizType;
import cn.renhe.heliao.idl.money.red.AddFriendRed;
import cn.renhe.heliao.idl.money.red.HeliaoAddFriendRedServiceGrpc;
import cn.renhe.heliao.idl.money.red.HeliaoRobRed;
import cn.renhe.heliao.idl.money.red.HeliaoRobRedServiceGrpc;
import cn.renhe.heliao.idl.money.red.HeliaoSendAdRed;
import cn.renhe.heliao.idl.money.red.HeliaoSendAdRedServiceGrpc;
import cn.renhe.heliao.idl.money.red.HeliaoSendRed;
import cn.renhe.heliao.idl.money.red.HeliaoSendRedServiceGrpc;
import cn.renhe.heliao.idl.money.trade.HeliaoTrade;
import cn.renhe.heliao.idl.money.trade.HeliaoTradeServiceGrpc;
import cn.renhe.heliao.idl.notice.HeLiaoNoticeServiceGrpc;
import cn.renhe.heliao.idl.notice.HeliaoNotice;
import cn.renhe.heliao.idl.payupgrade.PayUpgradeServiceGrpc;
import cn.renhe.heliao.idl.payupgrade.Payupgrade;
import cn.renhe.heliao.idl.team.TeamTopic;
import cn.renhe.heliao.idl.team.TeamTopicServiceGrpc;
import cn.renhe.heliao.idl.vip.HeliaoVipInfo;
import cn.renhe.heliao.idl.vip.HeliaoVipInfoServiceGrpc;
import io.grpc.internal.ManagedChannelImpl;
import io.grpc.stub.StreamObserver;

/**
 * Created by wangning on 2015/12/18.
 */
public class GrpcController extends BaseGrpcController {

    /**
     * 测试grpc链接
     **/
    public void test(final int taskId) {
        Logger.e("开始 测试GRPC接口--->taskId:" + taskId);
        sendRequest(taskId, "测试GRPC接口", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                HelloWorldServiceGrpc.HelloWorldServiceStub stub = HelloWorldServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                Helloworld.HelloRequest request = Helloworld.HelloRequest.newBuilder()
                        .setBase(baseRequest)
                        .setContent("hello josh")
                        .setName("wangning")
                        .build();
                stub.sayHello(request, observer);
            }
        });
    }

    /**
     * 由分享链接进入时 查看圈子成员时获取圈子成员
     */
    public void circleMemberRequest(final int taskId, final String imConversationId) {
        Logger.e("开始 获取圈子成员--->taskId:" + taskId + " imConversationId:" + imConversationId);
        sendRequest(taskId, "获取圈子成员", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                CircleMemberServiceGrpc.CircleMemberServiceStub stub = CircleMemberServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                CircleMember.CircleMemberListRequest request = CircleMember.CircleMemberListRequest.newBuilder()
                        .setBase(baseRequest)
                        .setImConversationId(imConversationId)
                        .build();
                stub.listCircleMemberByImConversationId(request, observer);
            }
        });
    }


    /**
     * 检测是否弹出升级提醒框
     */
    public void checkIfUpgrade(final int taskId) {
        Logger.e("开始 检测是否弹出升级提醒框--->taskId:" + taskId);
        sendRequest(taskId, "检测是否弹出升级提醒框", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                PayUpgradeServiceGrpc.PayUpgradeServiceStub stub = PayUpgradeServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                Payupgrade.PayUpgradeRequest request = Payupgrade.PayUpgradeRequest.newBuilder().setBase(baseRequest)
                        .build();
                stub.getPayUpgrade(request, observer);
            }
        });
    }

    /**
     * 搜索圈子
     *
     * @param taskId
     * @param keyWord   搜索条件
     * @param page      第几页  默认第一页
     * @param pageCount 每页几条 默认20条
     */
    public void searchCircles(final int taskId, final String keyWord, final int page, final int pageCount) {
        Logger.e("开始 搜索圈子--->taskId:" + taskId + " keyWord:" + keyWord + " page:" + page + " pageCount:" + pageCount);
        sendRequest(taskId, "搜索圈子", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                SearchCircleServiceGrpc.SearchCircleServiceStub stub = SearchCircleServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                SearchCircle.SearchCircleRequest request = SearchCircle.SearchCircleRequest.newBuilder()
                        .setBase(baseRequest)
                        .setKeyWord(keyWord)
                        .setPage(page)
                        .setPageCount(pageCount)
                        .build();
                stub.searchCircle(request, observer);
            }
        });
    }

    /**
     * 获取我的界面和财富、赞服务item是否需要显示
     *
     * @param taskId
     */
    public void getModuleConfig(final int taskId) {
        Logger.e("开始 获取和财富、赞服务是否需要显示--->taskId:" + taskId);
        sendRequest(taskId, "获取和财富、赞服务是否需要显示", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                ModuleConfigServiceGrpc.ModuleConfigServiceStub stub = ModuleConfigServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                ModuleConfig.ModuleListRequest request = ModuleConfig.ModuleListRequest.newBuilder()
                        .setBase(baseRequest).build();
                stub.moduleList(request, observer);
            }
        });
    }

    /**
     * 点击我的界面和财富、赞服务item通知服务端
     *
     * @param taskId
     */
    public void clickModuleConfig(final int taskId, final int moduleId) {
        Logger.e("开始 点击我的界面和财富、赞服务item通知服务端--->taskId:" + taskId);
        sendRequest(taskId, "点击我的界面和财富、赞服务item通知服务端", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                ModuleConfigServiceGrpc.ModuleConfigServiceStub stub = ModuleConfigServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                ModuleConfig.ClickModuleRequest request = ModuleConfig.ClickModuleRequest.newBuilder()
                        .setBase(baseRequest).setModuleId(moduleId).build();
                stub.clickModule(request, observer);
            }
        });
    }

    /**
     * 为好友打分,一个星星代表2分，满分10分
     *
     * @param taskId
     */
    public void userGrade(final int taskId, final MemberGrade.MemberGradeRequest.Type type,
                          final int userId, final int score) {
        Logger.e("开始 为好友打分--->taskId:" + taskId + " type:" + type + " userId:" + userId + " score:" + score);
        sendRequest(taskId, "为好友打分", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                MemberGradeServiceGrpc.MemberGradeServiceStub stub = MemberGradeServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                MemberGrade.MemberGradeRequest request = MemberGrade.MemberGradeRequest.newBuilder()
                        .setBase(baseRequest)
                        .setType(type)
                        .setUserId(userId)
                        .setScore(score)
                        .build();
                stub.memberGrade(request, observer);
            }
        });
    }

    /**
     * 为好友评价
     *
     * @param taskId
     */
    public void userEvaluate(final int taskId, final int userId, final String comment) {
        Logger.e("开始 为好友评价--->taskId:" + taskId + " userId:" + userId + " comment:" + comment);
        sendRequest(taskId, "为好友评价", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                MemberGradeServiceGrpc.MemberGradeServiceStub stub = MemberGradeServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                MemberGrade.MemberEvaluateRequest request = MemberGrade.MemberEvaluateRequest.newBuilder()
                        .setBase(baseRequest)
                        .setUserId(userId)
                        .setComment(comment)
                        .build();
                stub.memberEvaluate(request, observer);
            }
        });
    }

    /**
     * 获取对好友的打分、评价
     *
     * @param taskId
     */
    public void getGradeAndComment(final int taskId, final int userId) {
        Logger.e("开始 获取对好友的打分、评价--->taskId:" + taskId + " userId:" + userId);
        sendRequest(taskId, "获取对好友的打分、评价", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                MemberGradeServiceGrpc.MemberGradeServiceStub stub = MemberGradeServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                MemberGrade.MemberGradeGainRequest request = MemberGrade.MemberGradeGainRequest.newBuilder()
                        .setBase(baseRequest)
                        .setUserId(userId)
                        .build();
                stub.getMemberGradeByMemberId(request, observer);
            }
        });
    }

    /**
     * 收藏
     *
     * @param taskId
     * @param messageBoardType 如果收藏的是人脉圈，需要提供类型
     */
    public void addCollect(final int taskId, final MyCollection.CollectResquest.CollectionType type,
                           final String collectionId, final String collectionOwnerMemberId, final int messageBoardType) {
        Logger.e("开始 收藏--->taskId:" + taskId + " type:" + type + " collectionId:" + collectionId + " collectionOwnerMemberId:"
                + collectionOwnerMemberId + " messageBoardType:" + messageBoardType);
        sendRequest(taskId, "收藏", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                CollectServiceGrpc.CollectServiceStub stub = CollectServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                MyCollection.CollectResquest request = MyCollection.CollectResquest.newBuilder()
                        .setBase(baseRequest)
                        .setCollectionType(type)
                        .setCollectionId(collectionId)
                        .setMessageBoardType(messageBoardType)
                        .setCollectionOwnerMemberId(collectionOwnerMemberId)
                        .build();
                stub.collect(request, observer);
            }
        });
    }

    /**
     * 取消 人脉/圈子 收藏
     *
     * @param taskId
     */
    public void deleteCollect(final int taskId, final MyCollection.CollectResquest.CollectionType type, final String collectionId) {
        Logger.e("开始 删除 人脉/圈子 收藏--->taskId:" + taskId + " type:" + type + " collectionId:" + collectionId);
        sendRequest(taskId, "删除 人脉/圈子 收藏", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                CollectServiceGrpc.CollectServiceStub stub = CollectServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                MyCollection.DeleteCollectionResquest request = MyCollection.DeleteCollectionResquest.newBuilder()
                        .setBase(baseRequest)
                        .setCollectionType(type)
                        .setCollectionId(collectionId)
                        .build();
                stub.deleteCollection(request, observer);
            }
        });
    }

    /**
     * 取消 人脉圈 收藏
     *
     * @param taskId
     */
    public void deleteRmqCollect(final int taskId, final int rmqCollectId, final MyCollection.CollectResquest.CollectionType type, final String collectionId) {
        Logger.e("开始 删除 人脉圈 收藏--->taskId:" + taskId + " rmqCollectId:" + rmqCollectId + " type:" + type + " collectionId:" + collectionId);
        sendRequest(taskId, "删除 人脉圈 收藏", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                CollectServiceGrpc.CollectServiceStub stub = CollectServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                MyCollection.DeleteCollectionResquest request = MyCollection.DeleteCollectionResquest.newBuilder()
                        .setBase(baseRequest)
                        .setId(rmqCollectId)
                        .setCollectionType(type)
                        .setCollectionId(collectionId)
                        .build();
                stub.deleteCollection(request, observer);
            }
        });
    }

    /**
     * 查询收藏列表
     *
     * @param taskId
     * @param pageIndex    当前页
     * @param requestCount 一页的数量
     */
    public void searchCollects(final int taskId, final int pageIndex, final int requestCount) {
        Logger.e("开始 查询收藏列表--->taskId:" + taskId + " pageIndex:" + pageIndex);
        sendRequest(taskId, "查询收藏列表 第" + pageIndex + "页", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                CollectServiceGrpc.CollectServiceStub stub = CollectServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                MyCollection.ListCollectionsResquest request = MyCollection.ListCollectionsResquest.newBuilder()
                        .setBase(baseRequest)
                        .setPage(pageIndex)
                        .setPageCount(requestCount)
                        .build();
                stub.listCollections(request, observer);
            }
        });
    }

    /**
     * 我的页面，完善资料是否需要显示小红点
     *
     * @param taskId
     */
    public void myModuleCompleteArchiveNotice(final int taskId) {
        Logger.e("开始 完善资料的小红点--->taskId:" + taskId);
        sendRequest(taskId, "完善资料的小红点", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                MyModuleNoticeServiceGrpc.MyModuleNoticeServiceStub stub = MyModuleNoticeServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                MyModuleNotice.MyModuleNoticeRequest request = MyModuleNotice.MyModuleNoticeRequest.newBuilder()
                        .setBase(baseRequest)
                        .build();
                stub.hasNotify(request, observer);
            }
        });
    }

    /**
     * 获取完善资料页面哪些item需要显示小红点
     *
     * @param taskId
     */
    public void completeArchivePromptData(final int taskId) {
        Logger.e("开始 获取完善资料页面哪些item需要显示小红点--->taskId:" + taskId);
        sendRequest(taskId, "获取完善资料页面哪些item需要显示小红点", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                MyModuleNoticeServiceGrpc.MyModuleNoticeServiceStub stub = MyModuleNoticeServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                MyModuleNotice.PromptDataRequest request = MyModuleNotice.PromptDataRequest.newBuilder()
                        .setBase(baseRequest)
                        .build();
                stub.promptData(request, observer);
            }
        });
    }

    /**
     * 获取人脉列表
     *
     * @param maxUpdatTime         上一次检测更新时间
     * @param maxContactHlmemberId 上次返回的最大memberId
     * @param maxContactMobileId   上次返回的最大mobileId
     * @param maxContactCardId     上次返回的最大cardId
     * @param requestCount         获取个数限制，默认设为200
     */
    public void getContactsList(final int taskId, final long maxUpdatTime, final int maxContactHlmemberId, final int maxContactMobileId
            , final int maxContactCardId, final int requestCount) {
        Logger.e("开始 获取人脉列表--->taskId:" + taskId + " maxUpdatTime:" + maxUpdatTime + " maxContactHlmemberId:" + maxContactHlmemberId + " maxContactMobileId:" + maxContactMobileId
                + " maxContactCardId:" + maxContactCardId + " requestCount:" + requestCount);
        sendRequest(taskId, "获取人脉列表", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                ContactListServiceGrpc.ContactListServiceStub stub = ContactListServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                ContactList.ContactListRequest request = ContactList.ContactListRequest.newBuilder()
                        .setBase(baseRequest)
                        .setMaxUpdateTime(maxUpdatTime)
                        .setMaxMemberId(maxContactHlmemberId)
                        .setMaxMobileId(maxContactMobileId)
                        .setMaxCardId(maxContactCardId)
                        .setLimit(requestCount)
                        .build();
                stub.contactList(request, observer);
            }
        });
    }

    /**
     * 获取人和网人脉列表
     *
     * @param maxUpdatTime         上一次检测更新时间
     * @param maxContactHlmemberId 上次返回的最大memberId
     * @param requestCount         获取个数限制，默认设为200
     */
    public void getHeliaoContactsList(final int taskId, final long maxUpdatTime, final int maxContactHlmemberId, final int requestCount) {
        Logger.e("开始 获取人和网人脉列表--->taskId:" + taskId + " maxUpdatTime:" + maxUpdatTime +
                " maxContactHlmemberId:" + maxContactHlmemberId + " requestCount:" + requestCount);
        sendRequest(taskId, "获取人和网人脉列表", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                ContactListServiceGrpc.ContactListServiceStub stub = ContactListServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                ContactList.HeliaoContactRequest request = ContactList.HeliaoContactRequest.newBuilder()
                        .setBase(baseRequest)
                        .setMaxUpdateTime(maxUpdatTime)
                        .setMaxMemberId(maxContactHlmemberId)
                        .setLimit(requestCount)
                        .build();
                stub.heliaoContactList(request, observer);
            }
        });
    }

    /**
     * 获取手机通讯录人脉列表
     *
     * @param maxMobileContactsMemberId 上次返回的最大memberId
     * @param requestCount              获取个数限制，默认设为200
     */
    public void getMobileContactsList(final int taskId, final int maxMobileContactsMemberId, final int requestCount) {
        Logger.e("开始 获取手机通讯录人脉列表--->taskId:" + taskId + " maxMobileContactsMemberId:" + maxMobileContactsMemberId + " requestCount:" + requestCount);
        sendRequest(taskId, "获取手机通讯录人脉列表", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                ContactListServiceGrpc.ContactListServiceStub stub = ContactListServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                ContactList.MobileContactRequest request = ContactList.MobileContactRequest.newBuilder()
                        .setBase(baseRequest)
                        .setMaxMobileId(maxMobileContactsMemberId)
                        .setLimit(requestCount)
                        .build();
                stub.mobileContactList(request, observer);
            }
        });
    }

    /**
     * 获取名片通讯录人脉列表
     *
     * @param maxCardContactsMemberId 上次返回的最大memberId
     * @param requestCount            获取个数限制，默认设为200
     */
    public void getCardContactsList(final int taskId, final int maxCardContactsMemberId, final int requestCount) {
        Logger.e("开始 获取名片通讯录人脉列表--->taskId:" + taskId + " maxCardContactsMemberId:" + maxCardContactsMemberId + " requestCount:" + requestCount);
        sendRequest(taskId, "获取名片通讯录人脉列表", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                ContactListServiceGrpc.ContactListServiceStub stub = ContactListServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                ContactList.CardContactRequest request = ContactList.CardContactRequest.newBuilder()
                        .setBase(baseRequest)
                        .setMaxCardId(maxCardContactsMemberId)
                        .setLimit(requestCount)
                        .build();
                stub.cardContactList(request, observer);
            }
        });
    }

    /**
     * 上传本地手机通讯录的好友
     *
     * @param contactItemList 手机通讯录列表
     */
    public void pushMobileContactsList(final int taskId, final List<ImportContact.ContactItem> contactItemList) {
        Logger.e("开始 上传本地手机通讯录的好友--->taskId:" + taskId);
        sendRequest(taskId, "上传本地手机通讯录的好友", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                ImportContactServiceGrpc.ImportContactServiceStub stub = ImportContactServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                ImportContact.ImportContactRequest request = ImportContact.ImportContactRequest.newBuilder()
                        .setBase(baseRequest)
                        .addAllContactItem(contactItemList)
                        .build();
                stub.importContact(request, observer);
//                ImportContact.ContactItem.Builder builder = ImportContact.ContactItem.newBuilder();
//                builder.setName()
            }
        });
    }

    /**
     * 和聊应用内所有的消息提醒
     */
    public void getHeLiaoNotice(final int taskId) {
        Logger.e("开始 和聊应用内所有的消息提醒--->taskId:" + taskId);
        sendRequest(taskId, "和聊应用内所有的消息提醒", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                HeLiaoNoticeServiceGrpc.HeLiaoNoticeServiceStub stub = HeLiaoNoticeServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                HeliaoNotice.NewFriendNearByResquest request = HeliaoNotice.NewFriendNearByResquest.newBuilder()
                        .setBase(baseRequest)
                        .build();
                stub.hasNewFriendNearBy(request, observer);
            }
        });
    }

    /**
     * 展示升级界面
     */
    public void getUpgradeInfo(final int taskId) {
        Logger.e("开始 展示升级界面--->taskId:" + taskId);
        sendRequest(taskId, "展示升级界面", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                HeliaoVipInfoServiceGrpc.HeliaoVipInfoServiceStub stub = HeliaoVipInfoServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                HeliaoVipInfo.VipInfoRequest request = HeliaoVipInfo.VipInfoRequest.newBuilder()
                        .setBase(baseRequest)
                        .build();
                stub.getVipInfo(request, observer);
            }
        });
    }

    /**
     * #话题# 发布话题
     */
    public void postTopic(final int taskId, final int circleId, final String title, final String content) {
        Logger.e("开始 发布话题--->taskId:" + taskId);
        sendRequest(taskId, "发布话题", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                TeamTopicServiceGrpc.TeamTopicServiceStub stub = TeamTopicServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                TeamTopic.PostRequest request = TeamTopic.PostRequest.newBuilder()
                        .setBase(baseRequest)
                        .setCircleId(circleId)
                        .setTitle(title)
                        .setContent(content)
                        .build();
                stub.postTopic(request, observer);
            }
        });
    }

    /**
     * #话题# 编辑话题
     */
    public void editTopic(final int taskId, final int topicId, final int postId, final String title, final String content) {
        Logger.e("开始 编辑话题--->taskId:" + taskId);
        sendRequest(taskId, "编辑话题", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                TeamTopicServiceGrpc.TeamTopicServiceStub stub = TeamTopicServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                TeamTopic.EditRequest request = TeamTopic.EditRequest.newBuilder()
                        .setBase(baseRequest)
                        .setTopicId(topicId)
                        .setPostId(postId)
                        .setTitle(title)
                        .setContent(content)
                        .build();
                stub.editTopic(request, observer);
            }
        });
    }

    /**
     * /检查红包是否还有剩余
     * // 有则显示开红包的窗口,用户点击开,调用接口openRed
     * // 没有直接调getRedResult接口,显示红包结果
     *
     * @param redSid 红包sid
     */

    public void checkLuckyMoneyRemain(final int taskId, final String redSid) {
        Logger.e("开始 检查红包是否还有剩余--->taskId:" + taskId + ",redSid:" + redSid);
        sendRequest(taskId, "检查红包是否还有剩余", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                HeliaoRobRedServiceGrpc.HeliaoRobRedServiceStub stub = HeliaoRobRedServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                HeliaoRobRed.CheckRemainRequest request = HeliaoRobRed.CheckRemainRequest.newBuilder()
                        .setBase(baseRequest)
                        .setRedSid(redSid)
                        .build();
                stub.checkRemain(request, observer);
            }
        });
    }

    /**
     * 打开红包
     *
     * @param redSid 红包sid
     */

    public void openLuckyMoney(final int taskId, final String redSid) {
        Logger.e("开始 打开红包--->taskId:" + taskId + ",redSid:" + redSid);
        sendRequest(taskId, "打开红包", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                HeliaoRobRedServiceGrpc.HeliaoRobRedServiceStub stub = HeliaoRobRedServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                HeliaoRobRed.OpenRedRequest request = HeliaoRobRed.OpenRedRequest.newBuilder()
                        .setBase(baseRequest)
                        .setRedSid(redSid)
                        .build();
                stub.openRed(request, observer);
            }
        });
    }

    /**
     * 获取红包被抢结果详情
     *
     * @param redSid 红包sid
     */

    public void getLuckyMoneyResult(final int taskId, final String redSid) {
        Logger.e("开始 获取红包被抢结果详情--->taskId:" + taskId + ",redSid:" + redSid);
        sendRequest(taskId, "获取红包被抢结果详情", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                HeliaoRobRedServiceGrpc.HeliaoRobRedServiceStub stub = HeliaoRobRedServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                HeliaoRobRed.GetRedResultRequest request = HeliaoRobRed.GetRedResultRequest.newBuilder()
                        .setBase(baseRequest)
                        .setRedSid(redSid)
                        .build();
                stub.getRedResult(request, observer);
            }
        });
    }

    /**
     * 获取红包配置
     *
     * @param converstaionId 悟空的回话id
     */

    public void getLuckyMoneyConfig(final int taskId, final String converstaionId) {
        Logger.e("开始 获取红包配置--->taskId:" + taskId + ",converstaionId:" + converstaionId);
        sendRequest(taskId, "获取红包配置", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                HeliaoSendRedServiceGrpc.HeliaoSendRedServiceStub stub = HeliaoSendRedServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                HeliaoSendRed.RedConfigRequest request = HeliaoSendRed.RedConfigRequest.newBuilder()
                        .setBase(baseRequest)
                        .setConversationId(converstaionId)
                        .build();
                stub.redConfig(request, observer);
            }
        });
    }

    /**
     * 发布红包(获取将要发送的红包的相关信息，用这些信息进行支付、发送)
     *
     * @param taskId
     * @param luckyType       红包类型，0为定向单个红包 1为拼手气红包，2为普通红包
     * @param luckyCount      红包个数
     * @param luckyTotalMoney 红包总金额
     * @param luckyNote       红包祝福语
     * @param converstaionId  悟空的回话id
     */

    public void sendLucky(final int taskId, final int luckyType, final int luckyCount, final String luckyTotalMoney, final String luckyNote, final String converstaionId) {
        Logger.e("开始 发布红包-->taskId:" + taskId + " luckyType:" + luckyType + " luckyCount:" + luckyCount,
                " luckyTotalMoney:" + luckyTotalMoney + " luckyNote:" + luckyNote + " converstaionId:" + converstaionId);
        sendRequest(taskId, "发布红包", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                HeliaoSendRedServiceGrpc.HeliaoSendRedServiceStub stub = HeliaoSendRedServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                HeliaoSendRed.SendRedRequest request = HeliaoSendRed.SendRedRequest.newBuilder()
                        .setBase(baseRequest)
                        .setType(luckyType)
                        .setRedCount(luckyCount)
                        .setTotalAmount(luckyTotalMoney)
                        .setNote(luckyNote)
                        .setConversationId(converstaionId)
                        .build();
                stub.sendRedV2(request, observer);
            }
        });
    }

    /**
     * 确认支付红包
     */

    public void payLuckyMoney(final int taskId, final String luckySid, final int payType, final String payPassword) {
        Logger.e("开始 确认支付红包--->taskId:" + taskId + ",luckySid:" + luckySid + ",payType:" + payType + ",payPassword:" + payPassword);
        sendRequest(taskId, "确认支付红包", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                HeliaoSendRedServiceGrpc.HeliaoSendRedServiceStub stub = HeliaoSendRedServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                HeliaoSendRed.PayRedRequest request = HeliaoSendRed.PayRedRequest.newBuilder()
                        .setBase(baseRequest)
                        .setRedSid(luckySid)
                        .setPayType(payType)
                        .setPayPassword(payPassword)
                        .build();
                stub.payRed(request, observer);
            }
        });
    }

    /**
     * 设置/重设 支付密码
     *
     * @param taskId
     * @param password
     * @param checkToken 为上一步验证的token,如果首次设置支付密码,可以不传check_token
     */
    public void setOrRestPayPassword(final int taskId, final String password, final String checkToken) {
        Logger.e("开始 设置/重设 支付密码--->taskId:" + taskId + ",password:" + password + ",checkToken:" + checkToken);
        sendRequest(taskId, "设置/重设 支付密码", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                HeliaoTradeServiceGrpc.HeliaoTradeServiceStub stub = HeliaoTradeServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                HeliaoTrade.ResetPayPasswordRequest request = HeliaoTrade.ResetPayPasswordRequest.newBuilder()
                        .setBase(baseRequest)
                        .setNewPayPassword(password)
                        .setCheckToken(checkToken)
                        .build();
                stub.resetPayPassword(request, observer);
            }
        });
    }

    /**
     * 忘记支付密码
     *
     * @param taskId
     * @param password
     */
    public void forgetPayPassword(final int taskId, final String password) {
        Logger.e("开始 忘记支付密码--->taskId:" + taskId + ",password:" + password);
        sendRequest(taskId, "忘记支付密码", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                HeliaoTradeServiceGrpc.HeliaoTradeServiceStub stub = HeliaoTradeServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                HeliaoTrade.CheckLoginPasswordRequest request = HeliaoTrade.CheckLoginPasswordRequest.newBuilder()
                        .setBase(baseRequest)
                        .setLoginPassword(password)
                        .build();
                stub.checkLoginPassword(request, observer);
            }
        });
    }

    /**
     * 验证原支付密码（修改支付密码时用）
     *
     * @param taskId
     */
    public void checkOldPayPassword(final int taskId, final String oldPassword) {
        Logger.e("开始 验证原支付密码--->taskId:" + taskId + ",oldPassword:" + oldPassword);
        sendRequest(taskId, "验证原支付密码", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                HeliaoTradeServiceGrpc.HeliaoTradeServiceStub stub = HeliaoTradeServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                HeliaoTrade.CheckPayPasswordRequest request = HeliaoTrade.CheckPayPasswordRequest.newBuilder()
                        .setBase(baseRequest)
                        .setPayPassword(oldPassword)
                        .build();
                stub.checkPayPassword(request, observer);
            }
        });
    }

    /**
     * 获取余额
     *
     * @param taskId
     */
    public void getMemberBalance(final int taskId) {
        Logger.e("开始 获取余额--->taskId:" + taskId);
        sendRequest(taskId, "获取余额", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                HeliaoTradeServiceGrpc.HeliaoTradeServiceStub stub = HeliaoTradeServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                HeliaoTrade.MemberBalanceRequest request = HeliaoTrade.MemberBalanceRequest.newBuilder()
                        .setBase(baseRequest)
                        .build();
                stub.getMemberBalance(request, observer);
            }
        });
    }

    /**
     * 获取交易明细
     *
     * @param taskId
     */
    public void getTradeRecordList(final int taskId, final int page, final int pageSize) {
        Logger.e("开始 获取交易明细--->taskId:" + taskId);
        sendRequest(taskId, "获取交易明细", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                HeliaoTradeServiceGrpc.HeliaoTradeServiceStub stub = HeliaoTradeServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                HeliaoTrade.TradeRecordListRequest request = HeliaoTrade.TradeRecordListRequest.newBuilder()
                        .setBase(baseRequest)
                        .setPage(page)
                        .setPageSize(pageSize)
                        .build();
                stub.getTradeRecordList(request, observer);
            }
        });
    }

    /**
     * 获取提现信息
     *
     * @param taskId
     */
    public void viewWithdrawal(final int taskId) {
        Logger.e("开始 获取提现信息--->taskId:" + taskId);
        sendRequest(taskId, "获取提现信息", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                HeliaoTradeServiceGrpc.HeliaoTradeServiceStub stub = HeliaoTradeServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                HeliaoTrade.ViewWithdrawalRequest request = HeliaoTrade.ViewWithdrawalRequest.newBuilder()
                        .setBase(baseRequest)
                        .build();
                stub.viewWithdrawal(request, observer);
            }
        });
    }

    /**
     * 提现
     *
     * @param taskId
     */
    public void withdrawal(final int taskId, final String cardNo, final String moneyAmount) {
        Logger.e("开始 提现--->taskId:" + taskId + ",cardNo:" + cardNo + ",moneyAmount:" + moneyAmount);
        sendRequest(taskId, "提现", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                HeliaoTradeServiceGrpc.HeliaoTradeServiceStub stub = HeliaoTradeServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                HeliaoTrade.WithdrawalRequest request = HeliaoTrade.WithdrawalRequest.newBuilder()
                        .setBase(baseRequest)
                        .setCardNo(cardNo)
                        .setAmount(moneyAmount)
                        .build();
                stub.withdrawal(request, observer);
            }
        });
    }

    /**
     * 获取红包广告配置
     *
     * @param converstaionId 悟空的回话id
     */

    public void getLuckyMoneyAdConfig(final int taskId, final String converstaionId) {
        Logger.e("开始 获取红包广告配置--->taskId:" + taskId + ",converstaionId:" + converstaionId);
        sendRequest(taskId, "获取红包广告配置", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                HeliaoSendAdRedServiceGrpc.HeliaoSendAdRedServiceStub stub = HeliaoSendAdRedServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                HeliaoSendAdRed.AdRedConfigRequest request = HeliaoSendAdRed.AdRedConfigRequest.newBuilder()
                        .setBase(baseRequest)
                        .setConversationId(converstaionId)
                        .build();
                stub.getAdRedConfig(request, observer);
            }
        });
    }

    /**
     * 预生成红包广告
     *
     * @param adContent 广告内容
     * @param logoUrl   广告图片
     * @param adLink    广告地址
     */

    public void preBuildLuckyMoneyAd(final int taskId, final String adContent, final String logoUrl, final String adLink) {
        Logger.e("开始 预生成红包广告--->taskId:" + taskId + ",adContent:" + adContent + ",logoUrl:" + logoUrl + ",adLink:" + adLink);
        sendRequest(taskId, "预生成红包广告", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                HeliaoSendAdRedServiceGrpc.HeliaoSendAdRedServiceStub stub = HeliaoSendAdRedServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                HeliaoSendAdRed.BuildAdRedRequest request = HeliaoSendAdRed.BuildAdRedRequest.newBuilder()
                        .setBase(baseRequest)
                        .setContent(adContent)
                        .setThumbnail(logoUrl)
                        .setAdUrl(adLink)
                        .build();
                stub.buildAdRed(request, observer);
            }
        });
    }


    /**
     * 发布红包广告(获取将要发送的红包广告的相关信息，用这些信息进行支付、发送)
     *
     * @param taskId
     * @param luckyAdSid
     * @param luckyAdType
     * @param luckyAdCount
     * @param luckyAdTotalMoney
     * @param converstaionId
     */
    public void sendLuckyAd(final int taskId, final String luckyAdSid, final int luckyAdType, final int luckyAdCount, final String luckyAdTotalMoney, final String converstaionId) {
        Logger.e("开始 发布红包广告-->taskId:" + taskId + " luckyAdSid:" + luckyAdSid + " luckyAdType:" + luckyAdType,
                " luckyAdCount:" + luckyAdCount + " luckyAdTotalMoney:" + luckyAdTotalMoney + " converstaionId:" + converstaionId);
        sendRequest(taskId, "发布红包广告", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                HeliaoSendAdRedServiceGrpc.HeliaoSendAdRedServiceStub stub = HeliaoSendAdRedServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                HeliaoSendAdRed.SendAdRedRequest request = HeliaoSendAdRed.SendAdRedRequest.newBuilder()
                        .setBase(baseRequest)
                        .setAdSid(luckyAdSid)
                        .setType(luckyAdType)
                        .setRedCount(luckyAdCount)
                        .setTotalAmount(luckyAdTotalMoney)
                        .setConversationId(converstaionId)
                        .build();
                stub.sendAdRed(request, observer);
            }
        });
    }

    /**
     * 获取广告红包信息
     *
     * @param taskId
     * @param luckyAdSid
     */
    public void getAdRedInfo(final int taskId, final String luckyAdSid) {
        Logger.e("开始 获取广告红包信息-->taskId:" + taskId + " luckyAdSid:" + luckyAdSid);
        sendRequest(taskId, "获取广告红包信息", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                HeliaoSendAdRedServiceGrpc.HeliaoSendAdRedServiceStub stub = HeliaoSendAdRedServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                HeliaoSendAdRed.AdRedInfoRequest request = HeliaoSendAdRed.AdRedInfoRequest.newBuilder()
                        .setBase(baseRequest)
                        .setRedSid(luckyAdSid)
                        .build();
                stub.getAdRedInfo(request, observer);
            }
        });
    }

    /**
     * 领取广告红包
     *
     * @param taskId
     * @param luckyAdSid
     */
    public void receiveAdRed(final int taskId, final String luckyAdSid) {
        Logger.e("开始 领取广告红包-->taskId:" + taskId + " luckyAdSid:" + luckyAdSid);
        sendRequest(taskId, "领取广告红包", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                HeliaoSendAdRedServiceGrpc.HeliaoSendAdRedServiceStub stub = HeliaoSendAdRedServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                HeliaoSendAdRed.ReceiveAdRedRequest request = HeliaoSendAdRed.ReceiveAdRedRequest.newBuilder()
                        .setBase(baseRequest)
                        .setRedSid(luckyAdSid)
                        .build();
                stub.receiveAdRed(request, observer);
            }
        });
    }

    /**
     * 获取加好友红包配置
     *
     * @param taskId
     */
    public void getAddFriendRedConfig(final int taskId) {
        Logger.e("开始 获取加好友红包配置-->taskId:" + taskId);
        sendRequest(taskId, "获取加好友红包配置", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                HeliaoAddFriendRedServiceGrpc.HeliaoAddFriendRedServiceStub stub = HeliaoAddFriendRedServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                AddFriendRed.AddFriendRedConfigRequest request = AddFriendRed.AddFriendRedConfigRequest.newBuilder()
                        .setBase(baseRequest)
                        .build();
                stub.addFriendRedConfig(request, observer);
            }
        });
    }

    /**
     * 发布加好友红包
     *
     * @param taskId
     */
    public void sendAddFriendRed(final int taskId, final String luckyAmount, final String toMemberSid, final String note) {
        Logger.e("开始 发布加好友红包-->taskId:" + taskId);
        sendRequest(taskId, "发布加好友红包", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                HeliaoAddFriendRedServiceGrpc.HeliaoAddFriendRedServiceStub stub = HeliaoAddFriendRedServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                AddFriendRed.SendAddFriendRedRequest request = AddFriendRed.SendAddFriendRedRequest.newBuilder()
                        .setBase(baseRequest)
                        .setRedAmount(luckyAmount)
                        .setToMemberSid(toMemberSid)
                        .setNote(note)
                        .build();
                stub.sendAddFriendRed(request, observer);
            }
        });
    }

    /**
     * 确认支付
     */

    public void confirmPayStatus(final int taskId, final String bizSid, final PayBizType bizType) {
        Logger.e("开始 确认支付--->taskId:" + taskId + ",bizSid:" + bizSid + ",bizType:" + bizType);
        sendRequest(taskId, "确认支付", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                HeliaoPayServiceGrpc.HeliaoPayServiceStub stub = HeliaoPayServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                ConfirmPayStatusRequest request = ConfirmPayStatusRequest.newBuilder()
                        .setBase(baseRequest)
                        .setBizSid(bizSid)
                        .setBizType(bizType)
                        .build();
                stub.confirmPayStatus(request, observer);
            }
        });
    }

    /**
     * 获取发送人脉快递特权
     */

    public void getPrivilege(final int taskId, final String toMemberSid) {
        Logger.e("开始 获取发送人脉快递特权--->taskId:" + taskId + ",toMemberSid:" + toMemberSid);
        sendRequest(taskId, "获取发送人脉快递特权", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                AddFriendServiceGrpc.AddFriendServiceStub stub = AddFriendServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                AddFriend.CheckSendRequest request = AddFriend.CheckSendRequest.newBuilder()
                        .setBase(baseRequest)
                        .setToMemberSid(toMemberSid)
                        .build();
                stub.getPrivilege(request, observer);
            }
        });
    }

    /**
     * 极速邀请添加好友
     */

    public void connectionsCourier(final int taskId, final String toMemberSid, final String fromContent, final int fromType) {
        Logger.e("开始 极速邀请添加好友--->taskId:" + taskId + ",toMemberSid:" + toMemberSid);
        sendRequest(taskId, "极速邀请添加好友", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                AddFriendServiceGrpc.AddFriendServiceStub stub = AddFriendServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                AddFriend.ConnectionsCourierRequest request = AddFriend.ConnectionsCourierRequest.newBuilder()
                        .setBase(baseRequest)
                        .setToMemberSid(toMemberSid)
                        .setFromContent(fromContent)
                        .setFromType(fromType)
                        .build();
                stub.connectionsCourier(request, observer);
            }
        });
    }

    /**
     * 判断人脉秘书有没有特权
     */

    public void checkSendSecretary(final int taskId) {
        Logger.e("开始 判断人脉秘书有没有特权--->taskId:" + taskId);
        sendRequest(taskId, "判断人脉秘书有没有特权", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                AddFriendServiceGrpc.AddFriendServiceStub stub = AddFriendServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                AddFriend.CheckSendSecretaryRequest request = AddFriend.CheckSendSecretaryRequest.newBuilder()
                        .setBase(baseRequest)
                        .build();
                stub.checkSendSecretary(request, observer);
            }
        });
    }

    /**
     * 发送人脉秘书
     */

    public void secretary(final int taskId, final String toMemberSid, final String fromContent, final String mobile) {
        Logger.e("开始 发送人脉秘书--->taskId:" + taskId);
        sendRequest(taskId, "发送人脉秘书", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                AddFriendServiceGrpc.AddFriendServiceStub stub = AddFriendServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                AddFriend.SecretaryRequest request = AddFriend.SecretaryRequest.newBuilder()
                        .setBase(baseRequest)
                        .setToMemberSid(toMemberSid)
                        .setFromContent(fromContent)
                        .setMobile(mobile)
                        .build();
                stub.secretary(request, observer);
            }
        });
    }

    /**
     * 余额充值订单
     */

    public void enterpriseSearchPayOrder(final int taskId) {
        Logger.e("开始 余额充值订单--->taskId:" + taskId);
        sendRequest(taskId, "余额充值订单", new ObserverCallback() {
            @Override
            public void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer) {
                EnterpriseServiceGrpc.EnterpriseServiceStub stub = EnterpriseServiceGrpc.newStub(channel)
                        .withDeadlineAfter(BaseGrpcController.GRPC_TIME_OUT_SECONDS, TimeUnit.SECONDS);
                Enterprise.EnterpriseSearchPayOrderRequest request = Enterprise.EnterpriseSearchPayOrderRequest.newBuilder()
                        .setBase(baseRequest)
                        .build();
                stub.enterpriseSearchPayOrder(request, observer);
            }
        });
    }
}
