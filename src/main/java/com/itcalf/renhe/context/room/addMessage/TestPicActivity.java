package com.itcalf.renhe.context.room.addMessage;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.context.template.BaseActivity;

import java.io.Serializable;
import java.util.List;

public class TestPicActivity extends BaseActivity {
	// ArrayList<Entity> dataList;//用来装载数据源的列表
	List<ImageBucket> dataList;
	GridView gridView;
	ImageBucketAdapter adapter;// 自定义的适配器
	AlbumHelper helper;
	public static final String EXTRA_IMAGE_LIST = "imagelist";
	public static final int REQUEST_IMAGE_GRID_ACTIVITY_CODE = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setMyContentView(R.layout.activity_image_bucket);
		setTextValue(R.id.title_txt, "相册");
	}

	/**
	 * 初始化数据
	 */
	@Override
	public void initData() {
		helper = AlbumHelper.getHelper();
		helper.init(getApplicationContext());
		dataList = helper.getImagesBucketList(false);
		adapter = new ImageBucketAdapter(TestPicActivity.this, dataList);
		gridView.setAdapter(adapter);

		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				/**
				 * 根据position参数，可以获得跟GridView的子View相绑定的实体类，然后根据它的isSelected状态，
				 * 来判断是否显示选中效果。 至于选中效果的规则，下面适配器的代码中会有说明
				 */
				/**
				 * 通知适配器，绑定的数据发生了改变，应当刷新视图
				 */
				// adapter.notifyDataSetChanged();
				Intent intent = new Intent(TestPicActivity.this, ImageGridActivity.class);
				intent.putExtra(TestPicActivity.EXTRA_IMAGE_LIST, (Serializable) dataList.get(position).imageList);
				startActivityForResult(intent, REQUEST_IMAGE_GRID_ACTIVITY_CODE);
				overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
				//				finish();
			}

		});
	}

	/**
	 * 初始化view视图
	 */
	@Override
	public void findView() {
		super.findView();
		gridView = (GridView) findViewById(R.id.gridview);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && requestCode == REQUEST_IMAGE_GRID_ACTIVITY_CODE) {
			finish();
		}
	}
}
