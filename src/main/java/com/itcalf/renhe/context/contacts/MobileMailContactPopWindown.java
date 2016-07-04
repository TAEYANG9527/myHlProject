package com.itcalf.renhe.context.contacts;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.bean.ContactsReturn.ContactResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MobileMailContactPopWindown extends AppCompatActivity {

	private ImageView contactAvatar_img;
	private TextView contactAvatar_Tv;
	private RelativeLayout contact_mobile;
	private TextView contact_mobiles;
	private RelativeLayout contact_telephone;
	private TextView contact_telephones;
	private RelativeLayout contact_email;
	private TextView contact_emails;
	private RelativeLayout contact_address;
	private TextView contact_addresses;
	private TextView makesure;
	private View divide1, divide2, divide3;

	private ContactResult cv = null;
	private String mobiles = "", telephones = "", emails = "", addresses = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//		new ActivityTemplate().doInActivity(this, R.layout.mobilemail_contact_info);
		setContentView(R.layout.mobilemail_contact_info);
		findView();
		initData();
		initListener();
	}

	protected void findView() {
		contactAvatar_img = (ImageView) findViewById(R.id.contactAvatar_img);
		contactAvatar_Tv = (TextView) findViewById(R.id.contactAvatar_Tv);

		contact_mobile = (RelativeLayout) findViewById(R.id.contact_mobile);
		contact_telephone = (RelativeLayout) findViewById(R.id.contact_telephone);
		contact_email = (RelativeLayout) findViewById(R.id.contact_email);
		contact_address = (RelativeLayout) findViewById(R.id.contact_address);

		contact_mobiles = (TextView) findViewById(R.id.contact_mobiles);
		contact_telephones = (TextView) findViewById(R.id.contact_telephones);
		contact_emails = (TextView) findViewById(R.id.contact_emails);
		contact_addresses = (TextView) findViewById(R.id.contact_addresses);

		makesure = (TextView) findViewById(R.id.makesure);
		divide1 = (View) findViewById(R.id.divide1);
		divide2 = (View) findViewById(R.id.divide2);
		divide3 = (View) findViewById(R.id.divide3);
	}

	protected void initData() {
		cv = (ContactResult) this.getIntent().getExtras().getSerializable("contact");
		if (cv != null) {
			String firstName = "";
			String name = cv.getName().trim();
			String[] mobile = cv.getMobileItems();
			String[] telephone = cv.getTelephoneItems();
			String[] email = cv.getEmailItems();
			String[] address = cv.getAddressItems();
			//名字
			if (name.length() > 0) {
				//判断name是否含中文
				if (name.length() == 1) {
					firstName = name;
				} else {
					if (isContainChinese(name)) {
						firstName = name.substring(name.length() - 2, name.length());
					} else {
						firstName = name.substring(0, 2);
					}
				}
				contactAvatar_Tv.setText(firstName);
			}
			//手机
			if (null != mobile && mobile.length > 0) {
				for (int i = 0; i < mobile.length; i++) {
					mobiles = mobiles + mobile[i] + "\n";
				}
				if (mobiles.length() > 0) {
					mobiles = mobiles.substring(0, mobiles.length() - 1);
					contact_mobile.setVisibility(View.VISIBLE);
					contact_mobiles.setText(mobiles);
				}
			} else {
				contact_mobile.setVisibility(View.GONE);
			}
			//电话
			if (null != telephone && telephone.length > 0) {
				for (int i = 0; i < telephone.length; i++) {
					telephones = telephones + telephone[i] + "\n";
				}
				if (telephones.length() > 0) {
					telephones = telephones.substring(0, telephones.length() - 1);
					divide1.setVisibility(View.VISIBLE);
					contact_telephone.setVisibility(View.VISIBLE);
					contact_telephones.setText(telephones);
				}
			} else {
				divide1.setVisibility(View.GONE);
				contact_telephone.setVisibility(View.GONE);
			}
			//emails
			if (null != email && email.length > 0) {
				for (int i = 0; i < email.length; i++) {
					emails = emails + email[i] + "\n";
				}
				if (emails.length() > 0) {
					emails = emails.substring(0, emails.length() - 1);
					divide1.setVisibility(View.VISIBLE);
					divide2.setVisibility(View.VISIBLE);
					contact_email.setVisibility(View.VISIBLE);
					contact_emails.setText(emails);
				}
			} else {
				divide1.setVisibility(View.GONE);
				divide2.setVisibility(View.GONE);
				contact_email.setVisibility(View.GONE);
			}
			//地址
			if (null != address && address.length > 0) {
				for (int i = 0; i < address.length; i++) {
					addresses = addresses + address[i] + "\n";
				}
				if (addresses.length() > 0) {
					addresses = addresses.substring(0, addresses.length() - 1);
					divide1.setVisibility(View.VISIBLE);
					divide2.setVisibility(View.VISIBLE);
					divide3.setVisibility(View.VISIBLE);
					contact_address.setVisibility(View.VISIBLE);
					contact_addresses.setText(addresses);
				}
			} else {
				divide1.setVisibility(View.GONE);
				divide2.setVisibility(View.GONE);
				divide3.setVisibility(View.VISIBLE);
				contact_address.setVisibility(View.GONE);
			}
		}
	}

	protected void initListener() {
		makesure.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	/**
	 * 判断是否包含中文
	 */
	public boolean isContainChinese(String str) {

		Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
		Matcher m = p.matcher(str);
		if (m.find()) {
			return true;
		}
		return false;
	}

}
