package com.itcalf.renhe.context.archives;

import android.content.Context;

import com.itcalf.renhe.BaseAsyncTask;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.context.auth.NameAuthActivity;
import com.itcalf.renhe.context.upgrade.UpgradeActivity;
import com.itcalf.renhe.dto.AddFriend;
import com.itcalf.renhe.utils.CheckUpgradeUtil;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.ToastUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 好友添加 参数是被添加好友的SID
 *
 * @author Administrator
 */
public class AddFriendTask extends BaseAsyncTask<AddFriend> {
    // 数据回调
    private IAddFriendCallBack iAddFriendCallBack;
    private Context context;

    public AddFriendTask(Context mContext, IAddFriendCallBack iAddFriendCallBack) {
        super(mContext);
        this.context = mContext;
        this.iAddFriendCallBack = iAddFriendCallBack;
    }

    @Override
    protected AddFriend doInBackground(String... params) {
        Map<String, Object> reqParams = new HashMap<String, Object>();

        reqParams.put("sid", getMyApplication().getUserInfo().getSid());// 发起添加好友请求的会员sid
        reqParams.put("adSId", getMyApplication().getUserInfo().getAdSId());// 加密后用户id和密码的信息以后的每次请求中都要带上它
        reqParams.put("toMemberSId", params[0]);// 被添加好友的会员sid
        reqParams.put("purpose", "");
        reqParams.put("fromContent", params[1]);
        reqParams.put("fromType", params[3]);
        reqParams.put("deviceType", 0);

        try {
            AddFriend mb = (AddFriend) HttpUtil.doHttpRequest(Constants.Http.ADDFRIEND_V3, reqParams, AddFriend.class, null);
            return mb;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void doPre() {
        iAddFriendCallBack.onPre();
    }

    @Override
    public void doPost(AddFriend result) {
        iAddFriendCallBack.doPost(result);
        if (null != result) {
            if (result.getState() == -1) {
                ToastUtil.showErrorToast(context, R.string.lack_of_privilege);
            } else if (result.getState() == -2) {
                ToastUtil.showErrorToast(context, R.string.unkown_error);
            } else if (result.getState() == -3) {
                ToastUtil.showErrorToast(context, R.string.error_frind_sid);
            } else if (result.getState() == -4) {
                ToastUtil.showErrorToast(context, R.string.repeat_add_friend);
            } else if (result.getState() == -5) {
                ToastUtil.showErrorToast(context, R.string.cannot_add_self);
            } else if (result.getState() == -6) {
                ToastUtil.showErrorToast(context, R.string.already_become_friend);
            } else if (result.getState() == -7) {
                // 超过每日加好友的限制
                CheckUpgradeUtil.showUpgradeGuideDialog(context, R.drawable.upgrade_guide_3, R.string.upgrade_guide_today_add_friend_has_limit_title,
                        R.string.upgrade_guide_today_add_friend_has_limit_sub_title, R.string.upgrade_upgrade_now, UpgradeActivity.class, 4);
            } else if (result.getState() == -8) {
                // 好友数量超过了会员等级限制
                CheckUpgradeUtil.showUpgradeGuideDialog(context, R.drawable.upgrade_guide_3, R.string.upgrade_guide_friend_num_has_limit_title,
                        R.string.upgrade_guide_friend_num_has_limit_sub_title, R.string.upgrade_upgrade_now, UpgradeActivity.class, 3);
            } else if (result.getState() == -9) {
                ToastUtil.showErrorToast(context, R.string.beyond_same_account_friend_count);
            } else if (result.getState() == -10) {
                ToastUtil.showErrorToast(context, R.string.only_permission_introduced_to_add);
            } else if (result.getState() == -11) {
                // 超过每日加好友的限制
                CheckUpgradeUtil.showUpgradeGuideDialog(context, R.drawable.upgrade_guide_3, R.string.upgrade_guide_realname_limit_title,
                        R.string.upgrade_guide_realname_limit_sub_title, R.string.upgrade_realname_now, NameAuthActivity.class, 2);
            } else if (result.getState() == -12) {
                // 好友数量超过了会员等级限制
                CheckUpgradeUtil.showUpgradeGuideDialog(context, R.drawable.upgrade_guide_3, R.string.upgrade_guide_advanced_memeber_limit_title,
                        R.string.upgrade_guide_advanced_memeber_limit_sub_title, R.string.upgrade_upgrade_now, UpgradeActivity.class, 7);
            }
        }
    }

    public interface IAddFriendCallBack {

        void onPre();

        void doPost(AddFriend result);

    }
}
