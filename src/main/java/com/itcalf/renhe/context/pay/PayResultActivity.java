package com.itcalf.renhe.context.pay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.context.fragmentMain.TabMainFragmentActivity;
import com.itcalf.renhe.context.template.BaseActivity;

public class PayResultActivity extends BaseActivity {

    /**
     * @param context
     * @param goodName
     * @param price
     * @param payTime
     * @param payWay         0微信；1支付宝
     * @param upgradeVipType 要升级到的会员等级
     */
    public static void launch(Activity context, String goodName, String price, long payTime, int payWay,
                              int upgradeVipType) {
        Intent intent = new Intent(context, PayResultActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("goodName", goodName);
        bundle.putString("price", price);
        bundle.putLong("payTime", payTime);
        bundle.putInt("payWay", payWay);
        bundle.putInt("upgradeVipType", upgradeVipType);
        intent.putExtras(bundle);
        context.startActivity(intent);
        context.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    private TextView payResultTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.pay_result_layout);

    }

    @Override
    public void findView() {
        setTextValue("");
        payResultTv = (TextView) findViewById(R.id.pay_result_tv);
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            return;
        }
        int upgradeVipType = bundle.getInt("upgradeVipType");
        switch (upgradeVipType) {
            case 1:
                setTextValue(getString(R.string.upgrade_VIP));
                payResultTv.setText("恭喜您成为VIP会员");
                break;
            case 2:
                setTextValue(getString(R.string.upgrade_GOLD));
                payResultTv.setText("恭喜您成为黄金会员");
                break;
            case 3:
                setTextValue(getString(R.string.upgrade_PT));
                payResultTv.setText("恭喜您成为铂金会员");
                break;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem optionItem = menu.findItem(R.id.item_save);
        optionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        optionItem.setIcon(null);
        optionItem.setTitle("完成");
        optionItem.setVisible(true);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, TabMainFragmentActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                break;
            case R.id.item_save:
                Intent mIntent = new Intent(this, TabMainFragmentActivity.class);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mIntent);
                finish();
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, TabMainFragmentActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
