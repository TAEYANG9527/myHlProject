package com.itcalf.renhe.context.archives;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.Followers;
import com.itcalf.renhe.dto.Followers.FollowerList;
import com.itcalf.renhe.dto.Followers.FollowingList;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.XListView;
import com.itcalf.renhe.view.XListView.IXListViewListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class AttentionListActivity extends BaseActivity implements IXListViewListener {
	private XListView mListView;
	private String[] mFrom;
	private int[] mTo;
	private List<Map<String, Object>> mData;
	private SimpleAdapter mSimpleAdapter;

	private static final boolean TEST = false;

	private String mType;

	private int mStart = 0;
	private int mCount = 20;
	private List<Followers.FollowerList> mFollowerLists;
	private List<Followers.FollowingList> mFollowingLists;
	private String mOtherSid;
	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new ActivityTemplate().doInActivity(this, R.layout.archives_attentionlist);
		mHandler = new Handler();
	}

	@Override
	protected void findView() {
		super.findView();
		mListView = (XListView) findViewById(R.id.listView);
		mListView.setPullLoadEnable(true);
		mListView.setPullRefreshEnable(false);
		mListView.setXListViewListener(this);
	}

	@Override
	protected void initData() {
		super.initData();
		mFollowerLists = new ArrayList<Followers.FollowerList>();
		mFollowingLists = new ArrayList<Followers.FollowingList>();
		mType = getIntent().getExtras().get("type").toString();
		if ("1".equals(mType)) {
			mTitleTxt.setText("关注");
		} else {
			mTitleTxt.setText("粉丝");
		}

		mOtherSid = getIntent().getExtras().get(MyHomeArchivesActivity.FLAG_INTENT_DATA).toString();
		mData = new ArrayList<Map<String, Object>>();
		mTo = new int[] { R.id.headImage, R.id.titleTv, R.id.infoTv };
		mFrom = new String[] { "headImage", "titleTv", "infoTv", "accountType", "isRealName" };

		if (TEST) {

		} else {
			mSimpleAdapter = new ImageUpdateAdapter(AttentionListActivity.this, mData, R.layout.archives_attentionlist_item,
					mFrom, mTo);
			mListView.setAdapter(mSimpleAdapter);
			if (getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0)
					.getBoolean("fastdrag", true)) {
				mListView.setFastScrollEnabled(true);
			}
			new LoadTask() {
				@Override
				void doPre() {
					try {
						showDialog(1);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				@Override
				void doPost() {
					try {
						mSimpleAdapter.notifyDataSetInvalidated();
						dismissDialog(1);
						mListView.showFootView();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.executeOnExecutor(Executors.newCachedThreadPool(), mStart, mCount);
			;
		}

	}

	@Override
	protected void initListener() {
		super.initListener();
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				position -= 1;
				if ("1".equals(mType)) {
					if (mFollowingLists.size() > position) {
						Intent intent = new Intent(AttentionListActivity.this, MyHomeArchivesActivity.class);
						intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, mFollowingLists.get(position).getSid());
						startActivity(intent);
					}
				} else {
					if (mFollowerLists.size() > position) {
						Intent intent = new Intent(AttentionListActivity.this, MyHomeArchivesActivity.class);
						intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, mFollowerLists.get(position).getSid());
						startActivity(intent);
					}
				}

			}

		});

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 1:
			return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.waitting).cancelable(false).build();
		default:
			return null;
		}
	}

	abstract class LoadTask extends AsyncTask<Integer, Void, Followers> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			doPre();
			// showDialog(1);
			// mFooterView.setVisibility(View.GONE);
			// mHeaderView.setVisibility(View.GONE);
		}

		abstract void doPre();

		@Override
		protected Followers doInBackground(Integer... params) {
			Map<String, Object> reqParams = new HashMap<String, Object>();
			reqParams.put("sid", getRenheApplication().getUserInfo().getSid());
			reqParams.put("viewSId", mOtherSid);
			reqParams.put("start", params[0]);
			reqParams.put("count", params[1]);
			reqParams.put("adSId", getRenheApplication().getUserInfo().getAdSId());
			try {
				String p;
				if ("1".equals(mType)) {
					p = Constants.Http.SEARCH_FOLLOWERINGS;
				} else {
					p = Constants.Http.SEARCH_FOLLOWERS;
				}
				Followers mb = (Followers) HttpUtil.doHttpRequest(p, reqParams, Followers.class, AttentionListActivity.this);
				return mb;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Followers result) {
			super.onPostExecute(result);

			if (result == null) {
				ToastUtil.showNetworkError(AttentionListActivity.this);
			} else {
				switch (result.getState()) {
				case 1:
					if ("1".equals(mType)) {
						FollowingList[] list = result.getFollowingList();
						for (int i = 0; list != null && i < list.length; i++) {
							mFollowingLists.add(list[i]);
							final Map<String, Object> map = new LinkedHashMap<String, Object>();
							map.put(mFrom[0], R.drawable.avatar);
							if (null != list[i].getUserface()) {
								map.put("avatar_path", list[i].getUserface());
							}
							map.put(mFrom[1], list[i].getName());
							map.put(mFrom[2], list[i].getMessageboardContent());
							map.put(mFrom[3], list[i].getAccountType());
							map.put(mFrom[4], list[i].isRealname());
							mData.add(map);
						}
					} else {
						FollowerList[] list = result.getFollowerList();
						for (int i = 0; list != null && i < list.length; i++) {
							mFollowerLists.add(list[i]);
							final Map<String, Object> map = new LinkedHashMap<String, Object>();
							map.put(mFrom[0], R.drawable.avatar);
							map.put("avatar_path", list[i].getUserface());
							map.put(mFrom[1], list[i].getName());
							map.put(mFrom[2], list[i].getMessageboardContent().trim());
							map.put(mFrom[3], list[i].getAccountType());
							map.put(mFrom[4], list[i].isRealname());
							mData.add(map);
						}
					}
					mSimpleAdapter.notifyDataSetChanged();
					break;
				case -1:
					ToastUtil.showErrorToast(AttentionListActivity.this, R.string.lack_of_privilege);
					break;
				case -2:
					ToastUtil.showErrorToast(AttentionListActivity.this, R.string.sorry_of_unknow_exception);
					break;
				default:
					break;
				}

			}

			doPost();
		}

		abstract void doPost();
	}

	class ImageUpdateAdapter extends SimpleAdapter {

		public ImageUpdateAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
			super(context, data, resource, from, to);
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			convertView = super.getView(position, convertView, parent);
			if (convertView != null && position < mData.size()) {
				String picPath = (String) mData.get(position).get("avatar_path");
				ImageView imageView = (ImageView) convertView.findViewById(mTo[0]);
				// imageView.invalidate();
				//				imageView.setTag(picPath+position);
				if (null != picPath) {

					if (null != imageView) {
						//						SimpleAsyncImageLoad.loadDrawable(null,picPath,
						//								getRenheApplication().getUserInfo().getEmail(),
						//								70, 70, AttentionListActivity.this,
						//								new SimpleAsyncImageLoad.ImageCallback() {
						//
						//									@Override
						//									public void imageLoaded(
						//											Drawable imageDrawable,
						//											String imageUrl) {
						//										ImageView imageViewByTag = (ImageView) mListView
						//												.findViewWithTag(imageUrl+position);
						//										if (imageViewByTag != null) {
						//											imageViewByTag
						//													.setImageDrawable(imageDrawable);
						//										}
						//									}
						//
						//								});
						//方法2
						//						imageView.setTag(picPath);
						//						CacheManager.IMAGE_CACHE.initData(AttentionListActivity.this, "renhe_imagecache");
						//						CacheManager.IMAGE_CACHE.setContext(AttentionListActivity.this);
						//						CacheManager.IMAGE_CACHE.setCacheFolder(CacheManager.DEFAULT_IMAGECACHE_FOLDER);
						//						if (!CacheManager.IMAGE_CACHE.get(picPath, imageView)) {
						//							((ImageView)imageView).setImageDrawable(AttentionListActivity.this.getResources().getDrawable(R.drawable.avatar));
						//						}
						//方法3
						ImageLoader imageLoader = ImageLoader.getInstance();
						try {
							imageLoader.displayImage(picPath, (ImageView) imageView, CacheManager.options,
									CacheManager.animateFirstDisplayListener);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

				ImageView vipIv = (ImageView) convertView.findViewById(R.id.vipImage);
				ImageView realNameIv = (ImageView) convertView.findViewById(R.id.realnameImage);

				Object accountObject = mData.get(position).get("accountType");
				Object realNameObject = mData.get(position).get("isRealName");
				int accountType = 0;
				boolean isRealName = false;
				if (null != accountObject) {
					accountType = (Integer) mData.get(position).get("accountType");
					;//账号vip等级类型：0：普通会员；1：VIP会员；2：黄金会员；3：铂金会员
				}
				if (null != realNameObject) {
					isRealName = (Boolean) mData.get(position).get("isRealName");//是否是实名认证的会员
				}
				switch (accountType) {
				case 0:
					vipIv.setVisibility(View.GONE);
					break;
				case 1:
					vipIv.setVisibility(View.VISIBLE);
					//					vipIv.setImageResource(R.drawable.vip_1);
					vipIv.setImageResource(R.drawable.archive_vip_1);
					break;
				case 2:
					vipIv.setVisibility(View.VISIBLE);
					//					vipIv.setImageResource(R.drawable.vip_2);
					vipIv.setImageResource(R.drawable.archive_vip_2);
					break;
				case 3:
					vipIv.setVisibility(View.VISIBLE);
					//					vipIv.setImageResource(R.drawable.vip_3);
					vipIv.setImageResource(R.drawable.archive_vip_3);
					break;

				default:
					break;
				}
				if (isRealName && accountType <= 0) {
					realNameIv.setVisibility(View.VISIBLE);
					//					realNameIv.setImageResource(R.drawable.realname);
					realNameIv.setImageResource(R.drawable.archive_realname);
				} else {
					realNameIv.setVisibility(View.GONE);
				}
			}
			return convertView;
		}

	}

	private void onLoad() {
		mListView.stopRefresh();
		mListView.stopLoadMore();
		mListView.setRefreshTime("刚刚");
	}

	@Override
	public void onRefresh() {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mStart = 0;
				new LoadTask() {
					@Override
					void doPre() {
						mData.clear();
					}

					@Override
					void doPost() {
						onLoad();
					}
				}.executeOnExecutor(Executors.newCachedThreadPool(), mStart, mCount);
				;
			}
		}, 2000);
	}

	@Override
	public void onLoadMore() {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mStart += mCount;
				new LoadTask() {
					@Override
					void doPre() {
					}

					@Override
					void doPost() {
						onLoad();
					}
				}.executeOnExecutor(Executors.newCachedThreadPool(), mStart, mCount);

			}
		}, 2000);
	}
}
