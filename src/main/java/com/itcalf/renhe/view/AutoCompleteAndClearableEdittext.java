package com.itcalf.renhe.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;

import java.util.regex.Pattern;

/**
   * Title: AutoCompleteAndClearableEdittext.java<br>
   * Description: <br>
   * Copyright (c) 人和网版权所有 2014    <br>
   * Create DateTime: 2014-5-29 上午11:03:20 <br>
   * @author wangning
   */
/**
   * Title: AutoCompleteAndClearableEdittext.java<br>
   * Description: <br>
   * Copyright (c) 人和网版权所有 2014    <br>
   * Create DateTime: 2014-5-29 上午11:03:20 <br>
   * @author wangning
   */
public class AutoCompleteAndClearableEdittext extends AutoCompleteTextView {

	private String[] emailSufixs = new String[] { "@qq.com", "@163.com", "@126.com", "@hotmail.com", "@gmail.com", "@sina.com",
			"@vip.sina.com", "@vip.qq.com", "@vip.163.com" };

	public AutoCompleteAndClearableEdittext(Context context) {
		super(context);
		autoinit(context);
		init(context);
	}

	public AutoCompleteAndClearableEdittext(Context context, AttributeSet attrs) {
		super(context, attrs);
		autoinit(context);
		init(context);
	}

	public AutoCompleteAndClearableEdittext(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		autoinit(context);
		init(context);
	}

	public void setAdapterString(String[] es) {
		if (es != null && es.length > 0)
			this.emailSufixs = es;
	}

	private void autoinit(final Context context) {
		if (Constants.DEBUG_MODE) {
			emailSufixs = new String[] { "@qq.com", "@163.com", "@126.com", "@hotmail.com", "@gmail.com", "@sina.com",
					"@vip.sina.com", "@vip.qq.com", "@vip.163.com", "@renhenet.com.cn" };
		}
		//adapter中使用默认的emailSufixs中的数据，可以通过setAdapterString来更改
		this.setAdapter(new EmailAutoCompleteAdapter(context, android.R.layout.simple_list_item_1, emailSufixs));

		//使得在输入1个字符之后便开启自动完成
		this.setThreshold(1);

		this.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					String text = AutoCompleteAndClearableEdittext.this.getText().toString();
					//当该文本域重新获得焦点后，重启自动完成
					if (!"".equals(text))
						performFiltering(text, 0);
				} else {
					//当文本域丢失焦点后，检查输入email地址的格式
					AutoCompleteAndClearableEdittext ev = (AutoCompleteAndClearableEdittext) v;
					String text = ev.getText().toString();
					//这里正则写的有点粗暴:)
					//					if (text != null && text.matches("^[a-zA-Z0-9_]+@[a-zA-Z0-9]+\\.[a-zA-Z0-9]+$")) {
					//						Toast to = new Toast(context);
					//						ImageView i = new ImageView(context);
					//						//							i.setBackgroundResource(R.drawable.img_success);
					//						to.setView(i);
					//						to.show();
					//					} else {
					//						Toast toast = Toast.makeText(context, "邮件地址格式不正确", Toast.LENGTH_SHORT);
					//						toast.setGravity(Gravity.TOP, 0, 50);
					//						toast.show();
					//					}
				}
			}
		});
	}

	@Override
	protected void replaceText(CharSequence text) {
		//当我们在下拉框中选择一项时，android会默认使用AutoCompleteTextView中Adapter里的文本来填充文本域
		//因为这里Adapter中只是存了常用email的后缀
		//因此要重新replace逻辑，将用户输入的部分与后缀合并
		String t = this.getText().toString();
		int index = t.indexOf("@");
		if (index != -1)
			t = t.substring(0, index);
		super.replaceText(t + text);
	}

	@Override
	protected void performFiltering(CharSequence text, int keyCode) {
		//该方法会在用户输入文本之后调用，将已输入的文本与adapter中的数据对比，若它匹配
		//adapter中数据的前半部分，那么adapter中的这条数据将会在下拉框中出现
		String t = text.toString();

		//因为用户输入邮箱时，都是以字母，数字开始，而我们的adapter中只会提供以类似于"@163.com"
		//的邮箱后缀，因此在调用super.performFiltering时，传入的一定是以"@"开头的字符串
		int index = t.indexOf("@");
		if (index == -1) {
			if (t.matches("^[a-zA-Z0-9_]+$")) {
				super.performFiltering("@", keyCode);
			} else
				this.dismissDropDown();//当用户中途输入非法字符时，关闭下拉提示框
		} else {
			super.performFiltering(t.substring(index), keyCode);
		}
	}

	private class EmailAutoCompleteAdapter extends ArrayAdapter<String> {

		public EmailAutoCompleteAdapter(Context context, int textViewResourceId, String[] email_s) {
			super(context, textViewResourceId, email_s);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null)
				v = LayoutInflater.from(getContext()).inflate(R.layout.auto_complete_item, null);
			TextView tv = (TextView) v.findViewById(R.id.tv);

			String t = AutoCompleteAndClearableEdittext.this.getText().toString();
			int index = t.indexOf("@");
			if (index != -1)
				t = t.substring(0, index);
			//将用户输入的文本与adapter中的email后缀拼接后，在下拉框中显示
			tv.setText(t + getItem(position));
			return v;
		}
	}

	/*---------------------------------------------
	 * 删除按钮
	 * ----------------------------------------------
	 */
	public String defaultValue = "";
	final Drawable imgX =
	//	getResources().getDrawable(android.R.drawable.presence_offline ); // X
	getResources().getDrawable(R.drawable.relationship_input_del); // X

	// image

	//	public ClearableEditText(Context context) {
	//		super(context);
	//		init(context);
	//	}
	//
	//	/**
	//	 * @param context
	//	 * @param attrs
	//	 */
	//	public ClearableEditText(Context context, AttributeSet attrs) {
	//		super(context, attrs);
	//		init(context);
	//	}
	//
	//	/**
	//	 * @param context
	//	 * @param attrs
	//	 * @param defStyle
	//	 */
	//	public ClearableEditText(Context context, AttributeSet attrs, int defStyle) {
	//		super(context, attrs, defStyle);
	//		init(context);
	//	}

	private void init(Context ctx) {
		// Set bounds of our X button
		imgX.setBounds(0, 0, imgX.getIntrinsicWidth(), imgX.getIntrinsicHeight());

		// There may be initial text in the field, so we may need to display the
		// button
		manageClearButton();

		this.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				AutoCompleteAndClearableEdittext et = AutoCompleteAndClearableEdittext.this;

				// Is there an X showing?
				if (et.getCompoundDrawables()[2] == null)
					return false;
				// Only do this for up touches
				if (event.getAction() != MotionEvent.ACTION_UP)
					return false;
				// Is touch on our clear button?
				if (event.getX() > et.getWidth() - et.getPaddingRight() - imgX.getIntrinsicWidth() - 20) {
					et.setText("");
					AutoCompleteAndClearableEdittext.this.removeClearButton();
				}
				return false;
			}

		});

		this.addTextChangedListener(new TextWatcher() {
			@SuppressLint("NewApi")
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String str = s.toString();
				if (isNumeric(str)) {
					AutoCompleteAndClearableEdittext.this.setThreshold(10000);
				} else {
					AutoCompleteAndClearableEdittext.this.setThreshold(1);
				}
				AutoCompleteAndClearableEdittext.this.manageClearButton();
			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
		});
		this.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				EditText editText = (EditText) arg0;
				if (arg1) {
					if (editText.getText().toString().trim().length() > 0) {
						editText.setCompoundDrawablesWithIntrinsicBounds(null, null,
								getResources().getDrawable(R.drawable.relationship_input_del), null);
					}
				} else {
					editText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
				}
			}
		});
		//使用自定义字体
		if ((isInEditMode()) || (Constants.APP_TYPEFACE == null))
			return;
		setTypeface(Constants.APP_TYPEFACE);
	}

	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		return pattern.matcher(str).matches();
	}

	void manageClearButton() {
		if (this.getText().toString().equals(""))
			removeClearButton();
		else
			addClearButton();
	}

	void addClearButton() {
		this.setCompoundDrawables(this.getCompoundDrawables()[0], this.getCompoundDrawables()[1], imgX,
				this.getCompoundDrawables()[3]);
	}

	void removeClearButton() {
		this.setCompoundDrawables(this.getCompoundDrawables()[0], this.getCompoundDrawables()[1], null,
				this.getCompoundDrawables()[3]);
	}

}
