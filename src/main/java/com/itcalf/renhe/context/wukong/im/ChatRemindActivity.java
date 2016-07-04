package com.itcalf.renhe.context.wukong.im;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.alibaba.wukong.im.Member;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.bean.ChatRemindItem;
import com.itcalf.renhe.bean.ContactItem;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.utils.PinyinUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.ClearableEditText;
import com.itcalf.renhe.view.PinnedSectionListView;
import com.itcalf.renhe.view.SearchContactsSideBar;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ChatRemindActivity extends BaseActivity {
    private ListView listView;
    private FastScrollAdapterforContact adapter;
    private ClearableEditText keyword_edt;
    private SearchContactsSideBar sideBar;
    private TextView mLetterTxt;
    private Handler mNotifHandler;

    private List<Member> data = new ArrayList<Member>(); //存储上级传递过来的用户
    private TreeMap<String, List<Member>> mapFormat = new TreeMap<String, List<Member>>(); //用户关键字搜索

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //		new ActivityTemplate().doInActivity(this, R.layout.chat_remind_activity);
        new ActivityTemplate().doInActivity(this, R.layout.search_contacts_new);
        //		setTitle("选择提醒的人");
        setTextValue(1, "选择提醒的人");
    }

    @Override
    protected void findView() {
        super.findView();
        keyword_edt = (ClearableEditText) findViewById(R.id.keyword_edt);
        listView = (ListView) findViewById(R.id.contacts_list);
        sideBar = (SearchContactsSideBar) findViewById(R.id.contact_cv);
        mLetterTxt = (TextView) findViewById(R.id.letter_txt);
        sideBar.setTextView(mLetterTxt);
        adapter = new FastScrollAdapterforContact(this, R.layout.chat_remind_list_item, R.id.title_txt);
        listView.setAdapter(adapter);
    }

    @Override
    protected void initListener() {
        super.initListener();
        keyword_edt.addTextChangedListener(tbxEdit_TextChanged);

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatRemindItem item = adapter.getItem(position);
                if (item.type == ChatRemindItem.ITEM) {
                    Intent intent = new Intent();
                    intent.putExtra("name", item.getMember().user().nickname());
                    intent.putExtra("openId", item.getMember().user().openId());
                    setResult(RESULT_OK, intent);
                    finish();
                    overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                }
            }
        });

        // 设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new SearchContactsSideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                // // 该字母首次出现的位置
                int section = adapter.getPositionForTag(s.charAt(0) + "");
                if (-1 != section) {
                    int positon = adapter.getPositionForSection(section);
                    listView.setSelection(positon);
                }
            }
        });
    }

    @Override
    protected void initData() {
        super.initData();
        Bundle bundle = this.getIntent().getExtras();
//		ArrayList list = bundle.getParcelableArrayList("list");
//		HashMap<Long, Member> map = (HashMap<Long, Member>) list.get(0);
        if (null == bundle.getSerializable("list")) {
            ToastUtil.showToast(this, "圈子成员列表获取失败");
            finish();
        }
        HashMap<Long, Member> map = (HashMap<Long, Member>) bundle.getSerializable("list");
        Set set = map.entrySet();
        Iterator it = set.iterator();
        while (it.hasNext()) {
            Map.Entry me = (Map.Entry) it.next();
            if (map.get(me.getKey()).user().openId() != RenheApplication.getInstance().getUserInfo().getImId()) {
                String key = PinyinUtil.cn2FirstSpell(map.get(me.getKey()).user().nicknamePinyin()).toUpperCase().substring(0, 1);
                if (mapFormat.get(key) == null) {
                    List<Member> memberList = new ArrayList<Member>();
                    mapFormat.put(key, memberList);
                }
                List<Member> memberList = mapFormat.get(key);
                memberList.add((Member) me.getValue());
                mapFormat.put(key, memberList);
                data.add((Member) me.getValue());
            }
        }
        populateContacts(null);
        mNotifHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    // 关键字搜索
                    case 0:
                        String keyword = (String) msg.obj;
                        populateContacts(keyword);
                        break;

                }
                return false;
            }
        });
    }

    Handler handler = new Handler();
    Runnable run = new Runnable() {

        @Override
        public void run() {
            Message m = new Message();
            m.obj = keyword_edt.getText().toString();
            m.what = 0;
            mNotifHandler.sendMessage(m);
        }
    };
    /**
     * 搜索框输入状态监听
     **/
    private TextWatcher tbxEdit_TextChanged = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!TextUtils.isEmpty(s.toString())) {
                keyword_edt.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.edittext_search), null,
                        getResources().getDrawable(R.drawable.clearbtn_selected), null);
                keyword_edt.setCompoundDrawablePadding(1);
            } else {
                keyword_edt.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.edittext_search), null,
                        null, null);
            }
            handler.postDelayed(run, 0);
        }

    };

    /**
     * 查询联系人，支持拼音简写、字母查询
     *
     * @param keyword
     */

    private void populateContacts(String keyword) {
        adapter.clear();
        int count = 0;
        if (null != mapFormat && !mapFormat.isEmpty()) {
            if (TextUtils.isEmpty(keyword)) {
                sideBar.setVisibility(View.VISIBLE);
                final int sectionsNumber = mapFormat.size();
                listView.setAdapter(adapter);
                adapter.prepareSections(sectionsNumber);
                int sectionPosition = 0, listPosition = 0;

                // 圈子列表加载
                Set<Map.Entry<String, List<Member>>> set = mapFormat.entrySet();
                Iterator<Map.Entry<String, List<Member>>> it = set.iterator();
                while (it.hasNext()) {
                    Map.Entry<String, List<Member>> entry = (Map.Entry<String, List<Member>>) it.next();
                    ChatRemindItem section = new ChatRemindItem(ChatRemindItem.SECTION, String.valueOf(entry.getKey()));
                    section.sectionPosition = sectionPosition;
                    section.listPosition = listPosition++;
                    section.setMember(null);
                    adapter.onSectionAdded(section, sectionPosition);
                    adapter.add(section);

                    List<Member> memberList = entry.getValue();
                    for (int j = 0; j < memberList.size(); j++) {
                        Member member = memberList.get(j);
                        ++count;
                        ChatRemindItem item2 = new ChatRemindItem(ChatRemindItem.ITEM, member.user().nickname());
                        item2.sectionPosition = sectionPosition;
                        item2.listPosition = listPosition++;
                        item2.setMember(member);
                        adapter.add(item2);
                    }
                    sectionPosition++;
                }
                adapter.notifyDataSetChanged();
            } else {
                sideBar.setVisibility(View.GONE);
                Map<String, List<Member>> mResultsMap = new TreeMap<String, List<Member>>();// 添加首字母
                Set<Map.Entry<String, List<Member>>> set = mapFormat.entrySet();
                Iterator<Map.Entry<String, List<Member>>> it = set.iterator();
                while (it.hasNext()) {
                    Map.Entry<java.lang.String, java.util.List<Member>> entry = (Map.Entry<java.lang.String, java.util.List<Member>>) it
                            .next();
                    List<Member> memberList = entry.getValue();
                    List<Member> resultList = new ArrayList<Member>();
                    if (null != memberList && !memberList.isEmpty()) {
                        for (int j = 0; j < memberList.size(); j++) {
                            Member member = memberList.get(j);
                            if (null != keyword && null != member.user().nickname()
                                    && (member.user().nickname().toUpperCase().startsWith(keyword.toUpperCase()) || PinyinUtil
                                    .cn2FirstSpell(member.user().nickname()).startsWith(keyword.toUpperCase()))) {
                                resultList.add(member);
                                mResultsMap.put(entry.getKey(), resultList);
                            }
                        }
                    }
                }
                adapter.prepareSections(mResultsMap.size());
                listView.setAdapter(adapter);
                // 联系人列表加载
                int sectionPosition = 0, listPosition = 0;
                Set<Map.Entry<String, List<Member>>> resultSet = mResultsMap.entrySet();
                Iterator<Map.Entry<String, List<Member>>> resultIt = resultSet.iterator();
                while (resultIt.hasNext()) {
                    Map.Entry<java.lang.String, java.util.List<Member>> entry = (Map.Entry<java.lang.String, java.util.List<Member>>) resultIt
                            .next();

                    ChatRemindItem section = new ChatRemindItem(ChatRemindItem.SECTION, String.valueOf(entry.getKey()));
                    section.sectionPosition = sectionPosition;
                    section.listPosition = listPosition++;
                    section.setMember(null);
                    adapter.onSectionAdded(section, sectionPosition);
                    adapter.add(section);

                    List<Member> memberList = entry.getValue();
                    for (int j = 0; j < memberList.size(); j++) {
                        Member member = memberList.get(j);
                        ++count;
                        ChatRemindItem item = new ChatRemindItem(ChatRemindItem.ITEM, member.user().nickname());
                        item.sectionPosition = sectionPosition;
                        item.listPosition = listPosition++;
                        item.setMember(member);
                        adapter.add(item);
                    }
                    sectionPosition++;
                }
                // adapter.notifyDataSetChanged();
            }
        } else {
            if (TextUtils.isEmpty(keyword)) {
                sideBar.setVisibility(View.VISIBLE);
                final int sectionsNumber = 1;
                listView.setAdapter(adapter);
                adapter.prepareSections(sectionsNumber);
            } else {
                sideBar.setVisibility(View.GONE);
            }
        }
    }

    class CircleMemberAdapter extends ArrayAdapter<ChatRemindItem> implements PinnedSectionListView.PinnedSectionListAdapter {

        public CircleMemberAdapter(Context context, int resource, int textViewResourceId) {
            super(context, resource, textViewResourceId);
        }

        protected void prepareSections(int sectionsNumber) {
        }

        // 列表添加项
        protected void onSectionAdded(ChatRemindItem section, int sectionPosition) {
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            if (view != null) {
                ImageView iv_avator = (ImageView) view.findViewById(R.id.iv_avator);
                TextView tx_name = (TextView) view.findViewById(R.id.tx_name);
                LinearLayout contentLl = (LinearLayout) view.findViewById(R.id.content_ll);
                LinearLayout titleLl = (LinearLayout) view.findViewById(R.id.title_ll);
                View contactDivider = view.findViewById(R.id.contact_divider);

                ChatRemindItem item = getItem(position);

                if (item.type == ChatRemindItem.SECTION) {
                    titleLl.setVisibility(View.VISIBLE);
                    contentLl.setVisibility(View.GONE);
                    contactDivider.setVisibility(View.GONE);
                } else {
                    titleLl.setVisibility(View.GONE);
                    contentLl.setVisibility(View.VISIBLE);
                    tx_name.setText(item.getMember().user().nickname());
                    ImageLoader.getInstance().displayImage(item.getMember().user().avatar(), iv_avator, CacheManager.circleImageOptions);
                    if (position < getCount() - 1) {
                        ChatRemindItem tmpItem = getItem(position + 1);
                        if (tmpItem.type == ContactItem.SECTION)
                            contactDivider.setVisibility(View.GONE);
                        else {
                            contactDivider.setVisibility(View.VISIBLE);
                        }
                    } else {
                        contactDivider.setVisibility(View.GONE);
                    }
                }
            }
            return view;
        }

        @Override
        public boolean isItemViewTypePinned(int viewType) {
            return viewType == ChatRemindItem.SECTION;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).type;
        }
    }

    public class FastScrollAdapterforContact extends CircleMemberAdapter implements SectionIndexer {
        ChatRemindItem[] sections;

        public FastScrollAdapterforContact(Context context, int resource, int textViewResourceId) {
            super(context, resource, textViewResourceId);
        }

        @Override
        public void prepareSections(int sectionsNumber) {
            sections = new ChatRemindItem[sectionsNumber];
        }

        @Override
        public void onSectionAdded(ChatRemindItem section, int sectionPosition) {
            sections[sectionPosition] = section;
        }

        @Override
        public ChatRemindItem[] getSections() {
            return sections;
        }

        @Override
        public int getPositionForSection(int section) {
            if (section >= sections.length) {
                section = sections.length - 1;
            }
            return sections[section].listPosition;
        }

        @Override
        public int getSectionForPosition(int position) {
            if (position >= getCount()) {
                position = getCount() - 1;
            }
            return getItem(position).sectionPosition;
        }

        /**
         * 通过标题获取标题的位置
         *
         * @param tag
         * @return
         */
        public int getPositionForTag(String tag) {
            if (null != sections && sections.length > 0) {
                if (tag.equals("★")) {
                    return 0;
                }
                for (int i = 0; i < sections.length; i++) {
                    if (null != sections[i] && null != sections[i].text && sections[i].text.equals(tag))
                        return i;
                }
            }
            return -1;
        }
    }
}
