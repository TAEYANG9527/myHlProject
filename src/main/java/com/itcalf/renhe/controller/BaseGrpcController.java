package com.itcalf.renhe.controller;

import android.os.Handler;
import android.os.Looper;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.dto.UserInfo;
import com.itcalf.renhe.http.Callback;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.utils.DeviceUitl;
import com.itcalf.renhe.utils.NetworkUtil;
import com.orhanobut.logger.Logger;

import java.lang.reflect.Field;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.renhe.heliao.idl.base.BaseRequest;
import cn.renhe.heliao.idl.base.BaseResponse;
import cn.renhe.heliao.idl.contact.ContactList;
import io.grpc.internal.ManagedChannelImpl;
import io.grpc.okhttp.NegotiationType;
import io.grpc.okhttp.OkHttpChannelBuilder;
import io.grpc.stub.StreamObserver;

/**
 * Created by wangning on 2015/12/18 14:46.
 */
public abstract class BaseGrpcController<T> {
    public static final int GRPC_TIME_OUT_SECONDS = 15;//超时时间15s
    protected static ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(6, 6, 15,
            TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());

    public static BaseRequest getInitialedBaseRequest(int taskId) {
        BaseRequest.Builder builder = BaseRequest.newBuilder()
                .setAppVersion(RenheApplication.getInstance().getVersionName())
                .setDeviceType(BaseRequest.DeviceType.ANDROID).setDeviceId(DeviceUitl.getDeviceInfo())
                .setBundle("com.itcalf.renhe");
//        if (RenheApplication.getInstance().isLogin()) {
        UserInfo userInfo = RenheApplication.getInstance().getUserInfo();
        if (null != userInfo) {
            builder.setSid(userInfo.getSid()).setToken(userInfo.getAdSId());
        }
//        }
        return builder.build();
    }

    public void postRight(int taskId, String taskDesp, T result, ManagedChannelImpl channel) {
        new Handler(Looper.getMainLooper()).post(new GrpcRunnable(taskId, taskDesp, result, null, channel));
    }

    public void postError(int taskId, String taskDesp, String msg, ManagedChannelImpl channel) {
        new Handler(Looper.getMainLooper()).post(new GrpcRunnable(taskId, taskDesp, null, msg, channel));
    }

    public static String checkErrType(Throwable t) {
//        if (t instanceof StatusRuntimeException) {
//            StatusRuntimeException status = (StatusRuntimeException) t;
//            int code = status.getStatus().getCode().value();
//            if (code == Status.INTERNAL.getCode().value()) {
//                return "网络不可用";
//            } else if (code == Status.UNAVAILABLE.getCode().value()) {
//                return "该接口不可用";
//            } else if (code == Status.UNIMPLEMENTED.getCode().value()) {
//                return "服务器未实现该功能";
//            } else if (code == Status.UNAUTHENTICATED.getCode().value()) {
//                return "服务器验证失败";
//            } else if (code == Status.PERMISSION_DENIED.getCode().value()) {
//                return "请求被拒绝";
//            } else if (code == Status.INVALID_ARGUMENT.getCode().value()) {
//                return "无效的参数";
//            } else if (code == Status.ABORTED.getCode().value()) {
//                return "请求被中断,稍候重试";
//            } else if (code == Status.CANCELLED.getCode().value()) {
//                return "请求被取消";
//            } else if (code == Status.DEADLINE_EXCEEDED.getCode().value()) {
//                return "请求超时";
//            }
//        } else if (t instanceof ConnectException) {
//            return "网络未连接";
//        } else if (t instanceof SocketTimeoutException) {
//            return "连接超时";
//        } else if (t instanceof UnknownHostException) {
//            return "服务器不可用";
//        }
//        return "请求失败,稍候重试";
        return RenheApplication.getInstance().getApplicationContext().getString(R.string.network_error_message);
    }

    public ManagedChannelImpl getChannel() {
        return OkHttpChannelBuilder.forAddress(Constants.GRPC.getGrcpHost(), Constants.GRPC.getGrcpPort())
                .negotiationType(NegotiationType.PLAINTEXT).build();
    }

    //    public static interface TaskCallback {
//        void process(BaseRequest baseRequest, ManagedChannelImpl channel);
//
//    }

    /**
     * 调用GRPC
     *
     * @param taskId   任务ID，全局唯一
     * @param taskDesp 任务描述，用于Log日志打印
     * @param callback 回调
     */
    public void sendRequest(final int taskId, final String taskDesp, final ObserverCallback callback) {

        poolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                BaseRequest baseRequest = getInitialedBaseRequest(taskId);
                ManagedChannelImpl channel = getChannel();
                if (-1 == NetworkUtil.hasNetworkConnection(RenheApplication.getInstance().getApplicationContext())) {//网络未连接
                    postError(taskId, taskDesp, RenheApplication.getInstance().
                            getApplicationContext().getString(R.string.network_error_message), channel);
                    return;
                }
                try {
                    if (callback != null)
                        callback.process(baseRequest, channel, new GrpcStreamObserver(taskId, taskDesp, channel));
                } catch (Exception e) {
                    e.printStackTrace();
                    postError(taskId, taskDesp, RenheApplication.getInstance().
                            getApplicationContext().getString(R.string.network_error_message), channel);
                }
            }
        });
    }

    public static interface ObserverCallback {
        void process(BaseRequest baseRequest, ManagedChannelImpl channel, StreamObserver observer);
    }


    public class GrpcStreamObserver implements StreamObserver<T> {
        int taskId;
        String taskDesp;
        ManagedChannelImpl channel;

        public GrpcStreamObserver(int taskId, String taskDesp, ManagedChannelImpl channel) {
            this.taskId = taskId;
            this.taskDesp = taskDesp;
            this.channel = channel;
        }

        @Override
        public void onNext(T value) {
            postRight(taskId, taskDesp, value, channel);
        }

        @Override
        public void onError(Throwable t) {
            t.printStackTrace();
            postError(taskId, taskDesp, checkErrType(t), channel);
        }

        @Override
        public void onCompleted() {
        }
    }

    public static class GrpcRunnable<T> implements Runnable {
        int id;
        String taskDesp;
        Object result;
        ManagedChannelImpl channel;
        String msg;

        public GrpcRunnable(int id, String taskDesp, T result, String msg, ManagedChannelImpl channel) {
            this.id = id;
            this.taskDesp = taskDesp;
            this.result = result;
            this.msg = msg;
            this.channel = channel;
        }


        @Override
        public void run() {
            Callback callback = TaskManager.getInstance().get(id);
            if (callback == null) {
                return;
            }
//            try {
//                if (channel != null) {
//                    channel.shutdown().awaitTerminated(1, TimeUnit.SECONDS);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

            if (result != null) {
                try {
                    Field field = result.getClass().getDeclaredField("base_");
                    field.setAccessible(true);
                    BaseResponse baseResponse = (BaseResponse) field.get(result);
                    // 错误码, 默认 >= 0 为成功
                    // 错误信息, error_code < 0 时, 包含错误信息
                    if (null != baseResponse) {
                        if (baseResponse.getErrorCode() >= 0) {
                            callback.onSuccess(id, result);
                            if (Constants.DEBUG_MODE && !(result instanceof ContactList.ContactListResponse)) {
                                Logger.e(taskDesp + "  成功 request taskId-->>" + id + " , successResult-->>" + result + " , errorMsg-->>" + msg);
                            }
                        } else {
                            callback.onFailure(id, baseResponse.getErrorInfo());
                            if (Constants.DEBUG_MODE) {
                                Logger.e(taskDesp + "  失败 request taskId-->>" + id + " , errorInfo--->>" + baseResponse.getErrorInfo().toString() + " , errorMsg-->>" + msg);
                            }
                        }
                    } else {
                        callback.onFailure(id, RenheApplication.getInstance().getApplicationContext().getString(R.string.network_error_message));
                        if (Constants.DEBUG_MODE) {
                            Logger.e(taskDesp + "  失败 request taskId-->>" + id + " , successResult-->>" + result + " , errorMsg-->>" + msg);
                        }
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                    callback.onFailure(id, RenheApplication.getInstance().getApplicationContext().getString(R.string.network_error_message));
                    if (Constants.DEBUG_MODE) {
                        Logger.e(taskDesp + "  失败 request taskId-->>" + id + " , successResult-->>" + result + " , errorMsg-->>" + msg);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    callback.onFailure(id, RenheApplication.getInstance().getApplicationContext().getString(R.string.network_error_message));
                    if (Constants.DEBUG_MODE) {
                        Logger.e(taskDesp + "  失败 request taskId-->>" + id + " , successResult-->>" + result + " , errorMsg-->>" + msg);
                    }
                }
            } else {
                callback.onFailure(id, msg);
                if (Constants.DEBUG_MODE) {
                    Logger.e(taskDesp + "  失败 request taskId-->>" + id + " , successResult-->>" + result + " , errorMsg-->>" + msg);
                }
            }
        }
    }


}
