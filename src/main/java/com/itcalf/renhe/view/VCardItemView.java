package com.itcalf.renhe.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.itcalf.renhe.R;

public class VCardItemView extends RelativeLayout {

	private View v_parent, v_line;
	private TextView tv_label, tv_content;

	public VCardItemView(Context context) {
		super(context);
		init();
	}

	private void init() {
		v_parent = LayoutInflater.from(getContext()).inflate(R.layout.layout_item_vcarddetail, this);

		tv_label = (TextView) v_parent.findViewById(R.id.tv_label);
		tv_content = (TextView) v_parent.findViewById(R.id.tv_content);
		v_line = v_parent.findViewById(R.id.v_line);
	}

	public void setLabel(String label) {
		tv_label.setText(label);
	}

	public void setLabel(int rid) {
		tv_label.setText(rid);
	}

	public void setContent(String content) {
		tv_content.setText(content);
	}

	public void setContent(int rid) {
		tv_content.setText(rid);
	}

	public void setLineVisiable(int visibility) {
		v_line.setVisibility(visibility);
	}

	public void setClickEvent(int type) {

	}

}
