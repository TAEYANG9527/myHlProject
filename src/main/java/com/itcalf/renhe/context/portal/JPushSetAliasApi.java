package com.itcalf.renhe.context.portal;

/**
 * Title: JPushSetAliasApi.java<br>
 * Description: 注册 JPUSH<br>
 * Copyright (c) 人和网版权所有 2013<br>
 * Create DateTime: 2013-12-18 下午6:19:01<br>
 * 
 * @author Conch
 */

public class JPushSetAliasApi {

	private static final String TAG = "JPushSetAliasApi";

	/**
	 * register alias when login
	 * 
	 * @param id
	 * @return
	 * @throws ApiException
	 */
	//	public static void setAlias(final Context ct,long id, String token) throws Exception {
	//		new AsyncTask<String, Void, MessageBoardOperation>() {
	//
	//			@Override
	//			protected MessageBoardOperation doInBackground(String... params) {
	//				
	//				try {
	//						return ((RenheApplication) ct.getApplicationContext()).getMessageBoardCommand().setMyJPush(params[0],
	//								params[1], params[2],params[3], ct);
	//				} catch (Exception e) {
	//					return null;
	//				}
	//			}
	//
	//			@Override
	//			protected void onPreExecute() {
	//				super.onPreExecute();
	//			}
	//
	//			@Override
	//			protected void onPostExecute(MessageBoardOperation result) {
	//				super.onPostExecute(result);
	//				if (null != result) {
	//					if (1 == result.getState()) {
	//						SharedPreferences prefs = ct.getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid()+"setting_info", 0);
	//						SharedPreferences.Editor editorS = prefs.edit();
	//						editorS.putBoolean(Constants.Prefs.HAD_REGIST_JPUSH, true);
	//						editorS.commit();
	//					}
	//				} else {
	//					//ToastUtil.showNetworkError(ct);
	//				}
	//			}
	//
	//		}.executeOnExecutor(Executors.newCachedThreadPool(),String.valueOf(id), token, String.valueOf(com.itcalf.renhe.Constants.RenheJpush.CODE_OS_ANDROID), String.valueOf(com.itcalf.renhe.Constants.RenheJpush.CODE_APP_RENHE));
	//		
	////		ApiParams apiParams = new ApiParams();
	////		apiParams.addParam(Constants.CardJpush.PARAM_ID_STR, id);
	////		apiParams.addParam(Constants.CardJpush.PARAM_TOKEN_STR, token);
	////		apiParams.addParam(Constants.CardJpush.PARAM_OS_STR, Constants.CardJpush.CODE_OS_ANDROID);
	////		apiParams.addParam(Constants.CardJpush.PARAM_APP_STR, Constants.CardJpush.CODE_APP_RENHECARD);
	////
	////		ApiResponse apiResponse = ApiCaller.getInstance().performHttpRequest(PushServerApi.PUSH_REGISTER_ALIAS, apiParams);
	////		JSONObject result = apiResponse.getJsonObject();
	////		if (apiResponse.isSuccess()) {
	////			try {
	////				int state = result.getInt("state");
	////				String msg = result.getString("msg");
	////				if (state == 1) {
	////					return Boolean.TRUE;
	////				}
	////				if (log.isDebugEnabled()) {
	////					log.debug("set alias result meg:" + msg);
	////				}
	////			} catch (JSONException e) {
	////				log.error("parse json data error");
	////			}
	////			return Boolean.FALSE;
	////		} else {
	////			Log.e(TAG, TAG + CardApplication.getInstance().getString(R.string.SERVER_ERROR));
	////			throw new ApiException(CardApplication.getInstance().getString(R.string.SERVER_ERROR), String.valueOf(apiResponse
	////					.getState()));
	////		}
	//	}

	/**
	 * delete register record when logout
	 * 
	 * @param id
	 * @return
	 * @throws ApiException
	 */
	//	public static void delAlias(final Context ct,long id, String token) throws Exception {
	//		new AsyncTask<String, Void, MessageBoardOperation>() {
	//
	//			@Override
	//			protected MessageBoardOperation doInBackground(String... params) {
	//				try {
	//						return ((RenheApplication) ct.getApplicationContext()).getMessageBoardCommand().delMyJPush(params[0],
	//								params[1], params[2],params[3], ct);
	//				} catch (Exception e) {
	//					return null;
	//				}
	//			}
	//
	//			@Override
	//			protected void onPreExecute() {
	//				super.onPreExecute();
	//			}
	//
	//			@Override
	//			protected void onPostExecute(MessageBoardOperation result) {
	//				super.onPostExecute(result);
	//				if (null != result) {
	//					if (1 == result.getState()) {
	//					}
	//				} else {
	//					//ToastUtil.showNetworkError(ct);
	//				}
	//			}
	//
	//		}.executeOnExecutor(Executors.newCachedThreadPool(),String.valueOf(id), token, String.valueOf(com.itcalf.renhe.Constants.RenheJpush.CODE_OS_ANDROID), String.valueOf(com.itcalf.renhe.Constants.RenheJpush.CODE_APP_RENHE));
	////		ApiParams apiParams = new ApiParams();
	////		apiParams.addParam(com.itcalf.renhe.Constants.RenheJpush.PARAM_ID_STR, id);
	////		apiParams.addParam(Constants.CardJpush.PARAM_TOKEN_STR, token);
	////		apiParams.addParam(Constants.CardJpush.PARAM_OS_STR, Constants.CardJpush.CODE_OS_ANDROID);
	////		apiParams.addParam(Constants.CardJpush.PARAM_APP_STR, Constants.CardJpush.CODE_APP_RENHECARD);
	////
	////		ApiResponse apiResponse = ApiCaller.getInstance().performHttpRequest(PushServerApi.PUSH_UNREGISTER_ALIAS, apiParams);
	////		JSONObject result = apiResponse.getJsonObject();
	////		if (apiResponse.isSuccess()) {
	////			try {
	////				int state = result.getInt("state");
	////				String msg = result.getString("msg");
	////				if (state == 1) {
	////					return Boolean.TRUE;
	////				}
	////				Log.e(TAG, "set alias result meg :" + msg);
	////			} catch (JSONException e) {
	////				Log.e(TAG, "parse json data error");
	////			}
	////			return Boolean.FALSE;
	////		} else {
	////			Log.e(TAG, TAG + CardApplication.getInstance().getString(R.string.SERVER_ERROR));
	////			throw new ApiException(CardApplication.getInstance().getString(R.string.SERVER_ERROR), String.valueOf(apiResponse
	////					.getState()));
	////		}
	//	}
}
