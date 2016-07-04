package com.itcalf.renhe.context.room.addMessage;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.itcalf.renhe.R;
import com.itcalf.renhe.context.room.addMessage.ImageGridAdapter.TextCallback;
import com.itcalf.renhe.context.template.BaseActivity;

import java.util.Collections;
import java.util.List;

public class ImageGridActivity extends BaseActivity {
	public static final String EXTRA_IMAGE_LIST = "imagelist";

	// ArrayList<Entity> dataList;//鐢ㄦ潵瑁呰浇鏁版嵁婧愮殑鍒楄〃
	List<ImageItem> dataList;
	GridView gridView;
	ImageGridAdapter adapter;// 鑷畾涔夌殑閫傞厤鍣�
	AlbumHelper helper;
	Button bt;

	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				Toast.makeText(ImageGridActivity.this, "最多选择" + Bimp.MAX_SIZE + "张图片", 400).show();
				break;

			default:
				break;
			}
		}
	};

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setMyContentView(R.layout.activity_image_grid);
		setTextValue(R.id.title_txt, "相册");
		helper = AlbumHelper.getHelper();
		helper.init(getApplicationContext());

		dataList = (List<ImageItem>) getIntent().getSerializableExtra(EXTRA_IMAGE_LIST);
		Collections.reverse(dataList);
		initView();
		bt = (Button) findViewById(R.id.bt);
		if (Bimp.drr.size() <= Bimp.MAX_SIZE) {
			bt.setText("完成" + "(" + Bimp.drr.size() + "/" + Bimp.MAX_SIZE + ")");
		}
		bt.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				//				ArrayList<String> list = new ArrayList<String>();
				//				Collection<String> c = adapter.map.values();
				//				Iterator<String> it = c.iterator();
				//				for (; it.hasNext();) {
				//					list.add(it.next());
				//				}

				//				if (Bimp.act_bool) {
				////					Intent intent = new Intent(ImageGridActivity.this,
				////							PublishedActivity.class);
				////					startActivity(intent);
				//					Bimp.act_bool = false;
				//				}
				//				Collections.reverse(list);
				//				for (int i = 0; i < list.size(); i++) {
				//					if (Bimp.drr.size() < Bimp.MAX_SIZE) {
				//						Bimp.drr.add(list.get(i));
				//					}
				//				}
				if (null != Bimp.curDrr && Bimp.curDrr.size() > 0) {
					Bimp.drr.addAll(Bimp.curDrr);
					Bimp.curDrr.clear();
				}
				setResult(RESULT_OK);
				finish();
			}

		});
	}

	private void initView() {
		gridView = (GridView) findViewById(R.id.gridview);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		adapter = new ImageGridAdapter(ImageGridActivity.this, dataList, mHandler);
		gridView.setAdapter(adapter);
		adapter.setTextCallback(new TextCallback() {
			public void onListen(int count) {
				if (count <= Bimp.MAX_SIZE) {
					bt.setText("完成" + "(" + count + "/" + Bimp.MAX_SIZE + ")");
				}
			}
		});

		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				//				 if(dataList.get(position).isSelected()){
				//				 dataList.get(position).setSelected(false);
				//				 }else{
				//				 dataList.get(position).setSelected(true);
				//				 }
				//			
				adapter.notifyDataSetChanged();
			}

		});

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			//			Bimp.drr.clear();
			Bimp.curDrr.clear();
			finish();
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		Bimp.curDrr.clear();
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
		finish();
	}

}
