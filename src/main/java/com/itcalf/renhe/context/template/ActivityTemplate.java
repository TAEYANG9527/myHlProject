package com.itcalf.renhe.context.template;

/**
 * Activity模板类
 * 
 * @author piers.xie
 */
public class ActivityTemplate implements ActivityCallBack {

	@Override
	public void doInActivity(BaseActivity activity, int layoutResID) {
		activity.setMyContentView(layoutResID);
//		activity.setContentView(layoutResID);
//		try {
//			activity.findView();
//			activity.initData();
//			activity.initListener();
//		} catch (Exception e) {
//			if (Constants.LOG) {
//				Log.e(Constants.TAG, "ActivityTemplate", e);
//			}
//		}
	}

}
