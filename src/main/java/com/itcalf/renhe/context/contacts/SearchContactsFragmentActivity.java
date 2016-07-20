package com.itcalf.renhe.context.contacts;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.context.fragmentMain.NewContactFragment;

/**
 * description :
 * Created by Chans Renhenet
 * 2015/8/5
 */
public class SearchContactsFragmentActivity extends AppCompatActivity {

    public static FragmentManager fm;
    protected TextView mTitleTxt;
    protected Toolbar toolbar;
    protected TextView toolbarTitleTv;
    private NewContactFragment newContactFragmentVersion2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.search_contacts_fragment);

        fm = getSupportFragmentManager();
        newContactFragmentVersion2 = new NewContactFragment();
        if (fm != null) {
            FragmentTransaction transaction = fm.beginTransaction();
            Bundle bundle = new Bundle();
            bundle.putSerializable("isFromArchive", true);
            newContactFragmentVersion2.setArguments(bundle);
            transaction.add(R.id.searchFragment, newContactFragmentVersion2);
            transaction.commit();
        }
    }

    public void setMyContentView(int layoutRes) {
        setContentView(layoutRes);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbarTitleTv = (TextView) findViewById(R.id.toolbar_title_tv);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        setTitle("");
        if (null != toolbarTitleTv) {
            toolbarTitleTv.setText("我的人脉");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
        }
        return super.onKeyDown(keyCode, event);
    }
}
