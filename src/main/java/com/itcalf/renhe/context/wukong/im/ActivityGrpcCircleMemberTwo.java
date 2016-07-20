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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.bean.ChatRemindItem;
import com.itcalf.renhe.bean.ContactItem;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.controller.GrpcController;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.utils.FadeUitl;
import com.itcalf.renhe.utils.PinyinUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.ClearableEditText;
import com.itcalf.renhe.view.PinnedSectionListView;
import com.itcalf.renhe.view.SearchContactsSideBar;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import cn.renhe.heliao.idl.circle.CircleMember;

/**
 * 实现非圈子成员可查看圈子成员列表并
 * Created by wujian on 2015/12/30.
 */
public class ActivityGrpcCircleMemberTwo extends BaseActivity<CircleMember.CircleMemberListResponse> {
    private RelativeLayout rootRl;
    private ClearableEditText keyword_edt;
    private ListView listView;
    private SearchContactsSideBar sideBar;
    private TextView mLetterTxt;
    private FastScrollAdapterforContact adapter;
    private String imConversationId;
    private Handler mNotifHandler;
    private List<CircleMember.MemberInfo> memberInfoList;
    private TreeMap<String, List<CircleMember.MemberInfo>> mapFormat = new TreeMap<String, List<CircleMember.MemberInfo>>(); //用户关键字搜索
    private CircleMember.MemberInfo circleMasterInfo;
    private int ID_TASK_CircleMember = TaskManager.getTaskId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ActivityTemplate().doInActivity(this, R.layout.search_contacts_new);
        setTextValue(1, "圈子成员");
    }

    @Override
    protected void findView() {
        super.findView();
        rootRl = (RelativeLayout) findViewById(R.id.rootRl);
        keyword_edt = (ClearableEditText) findViewById(R.id.keyword_edt);//输入拼音首字母查询
        listView = (ListView) findViewById(R.id.contacts_list);//联系人列表
        sideBar = (SearchContactsSideBar) findViewById(R.id.contact_cv);//字母列表
        mLetterTxt = (TextView) findViewById(R.id.letter_txt);//正中显示的首字母
        sideBar.setTextView(mLetterTxt);
        adapter = new FastScrollAdapterforContact(this, R.layout.chat_remind_list_item, R.id.title_txt);//成员列表显示的适配器
        listView.setAdapter(adapter);
        sideBar.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void initListener() {
        super.initListener();
        keyword_edt.addTextChangedListener(tbxEdit_TextChanged);
        //点击listview item 进入成员档案页面
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatRemindItem item = adapter.getItem(position);
                if (item.type == ChatRemindItem.ITEM) {
                    CircleMember.MemberInfo member = item.getMemberInfo();
                    if (null != member) {
                        Intent intent = new Intent(ActivityGrpcCircleMemberTwo.this, MyHomeArchivesActivity.class);
                        intent.putExtra("name", member.getName());
                        intent.putExtra("openId", (long) member.getOpenid());
                        intent.putExtra("from", Constants.ADDFRIENDTYPE[7]);
                        intent.putExtra("noCircle", true);
                        intent.putExtra("flag", true);
                        startActivity(intent);
                        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    }
                }
            }
        });

        // 设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new SearchContactsSideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
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
        fadeUitl = new FadeUitl(this, "加载中...");
        fadeUitl.addFade(rootRl);
        imConversationId = getIntent().getStringExtra("imConversationId");//圈子会话id
        //使用grpc获取圈子成员
        obtainCircleMeber();
        //实现关键字搜索
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

    /**
     * 当非圈子成员时调用grpc获取圈子成员
     */
    private void obtainCircleMeber() {
        if (TaskManager.getInstance().exist(ID_TASK_CircleMember)) {
            return;
        }
        if (grpcController == null)
            grpcController = new GrpcController();
        TaskManager.getInstance().addTask(this, ID_TASK_CircleMember);
        grpcController.circleMemberRequest(ID_TASK_CircleMember, imConversationId);
    }

    /**
     * GRPC 调用的回调方法
     * <p>
     * 成功进入onSuccess
     * 失败进入onFailure
     */
    @Override
    public void onSuccess(int type, CircleMember.CircleMemberListResponse result) {
        super.onSuccess(type, result);
        if (fadeUitl.isFadeShow(rootRl))
            fadeUitl.removeFade(rootRl);
        CircleMember.CircleMemberListResponse response = result;
        //从服务端获取圈子成员列表
        memberInfoList = response.getMemberListList();
        if (memberInfoList.get(0).getIsCircleOwner()) {
            circleMasterInfo = memberInfoList.get(0);
        }
        handleMember(memberInfoList);
    }

    /**
     * GRPC 调用的回调方法
     * <p>
     * 成功进入onSuccess
     * 失败进入onFailure
     */
    @Override
    public void onFailure(int type, String msg) {
        super.onFailure(type, msg);
        if (fadeUitl.isFadeShow(rootRl))
            fadeUitl.removeFade(rootRl);
        ToastUtil.showToast(ActivityGrpcCircleMemberTwo.this, R.string.load_circle_members_error);
        finish();
    }

    private void handleMember(List<CircleMember.MemberInfo> memberInfoList) {
        if (null != mapFormat) {
            mapFormat.clear();
        }
        for (CircleMember.MemberInfo memberInfo : memberInfoList) {
            //获取拼音首字母
            String key = "#";
            if (!TextUtils.isEmpty(memberInfo.getName())) {
                if (memberInfo.getName().length() == 1) {
                    key = memberInfo.getName();
                } else if (memberInfo.getName().length() > 1) {
                    key = PinyinUtil.cn2FirstSpell(PinyinUtil.cn2Spell(memberInfo.getName()).toUpperCase().substring(0, 1));
                }
            }
            if (mapFormat.get(key) == null) {
                List<CircleMember.MemberInfo> memberList = new ArrayList<CircleMember.MemberInfo>();
                mapFormat.put(key, memberList);
            }
            List<CircleMember.MemberInfo> memberList = mapFormat.get(key);
            memberList.add(memberInfo);
            mapFormat.put(key, memberList);
        }
        populateContacts(null);
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
        if (null != mapFormat && !mapFormat.isEmpty()) {
            if (TextUtils.isEmpty(keyword)) {
                final int sectionsNumber = mapFormat.size() + 1;//+1是因为圈主是额外添加的item
                listView.setAdapter(adapter);
                adapter.prepareSections(sectionsNumber);
                int sectionPosition = 0, listPosition = 0;
                //圈主
                //判断圈主
                if (null != circleMasterInfo) {
                    ChatRemindItem section = new ChatRemindItem(ChatRemindItem.SECTION, "圈主");
                    section.sectionPosition = sectionPosition;
                    section.listPosition = listPosition++;
                    section.setMember(null);
                    adapter.onSectionAdded(section, sectionPosition);
                    adapter.add(section);
                    ChatRemindItem circleMasterItem = new ChatRemindItem(ChatRemindItem.ITEM, circleMasterInfo.getName());
                    circleMasterItem.sectionPosition = sectionPosition;
                    circleMasterItem.listPosition = listPosition++;
                    circleMasterItem.setMemberInfo(circleMasterInfo);
                    adapter.add(circleMasterItem);
                    sectionPosition++;
                }

                // 圈子列表加载
                Set<Map.Entry<String, List<CircleMember.MemberInfo>>> set = mapFormat.entrySet();
                Iterator<Map.Entry<String, List<CircleMember.MemberInfo>>> it = set.iterator();
                while (it.hasNext()) {
                    Map.Entry<String, List<CircleMember.MemberInfo>> entry = (Map.Entry<String, List<CircleMember.MemberInfo>>) it.next();
                    ChatRemindItem section = new ChatRemindItem(ChatRemindItem.SECTION, String.valueOf(entry.getKey()));
                    section.sectionPosition = sectionPosition;
                    section.listPosition = listPosition++;
                    section.setMemberInfo(null);
                    adapter.onSectionAdded(section, sectionPosition);
                    adapter.add(section);

                    List<CircleMember.MemberInfo> memberList = entry.getValue();
                    for (int j = 0; j < memberList.size(); j++) {
                        CircleMember.MemberInfo member = memberList.get(j);
                        ChatRemindItem item2 = new ChatRemindItem(ChatRemindItem.ITEM, member.getName());
                        item2.sectionPosition = sectionPosition;
                        item2.listPosition = listPosition++;
                        item2.setMemberInfo(member);
                        adapter.add(item2);
                    }
                    sectionPosition++;
                }
                adapter.notifyDataSetChanged();
            } else {
                Map<String, List<CircleMember.MemberInfo>> mResultsMap = new TreeMap<String, List<CircleMember.MemberInfo>>();// 添加首字母
                Set<Map.Entry<String, List<CircleMember.MemberInfo>>> set = mapFormat.entrySet();
                Iterator<Map.Entry<String, List<CircleMember.MemberInfo>>> it = set.iterator();
                while (it.hasNext()) {
                    Map.Entry<String, List<CircleMember.MemberInfo>> entry = (Map.Entry<String, List<CircleMember.MemberInfo>>) it.next();
                    List<CircleMember.MemberInfo> memberList = entry.getValue();
                    List<CircleMember.MemberInfo> resultList = new ArrayList<CircleMember.MemberInfo>();
                    if (null != memberList && !memberList.isEmpty()) {
                        for (int j = 0; j < memberList.size(); j++) {
                            CircleMember.MemberInfo member = memberList.get(j);
                            if (null != keyword && null != member.getName()
                                    && (member.getName().toUpperCase().startsWith(keyword.toUpperCase()) || PinyinUtil
                                    .cn2FirstSpell(member.getName()).startsWith(keyword.toUpperCase()))) {
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
                Set<Map.Entry<String, List<CircleMember.MemberInfo>>> resultSet = mResultsMap.entrySet();
                Iterator<Map.Entry<String, List<CircleMember.MemberInfo>>> resultIt = resultSet.iterator();
                while (resultIt.hasNext()) {
                    Map.Entry<String, List<CircleMember.MemberInfo>> entry = (Map.Entry<String, List<CircleMember.MemberInfo>>) resultIt.next();
                    ChatRemindItem section = new ChatRemindItem(ChatRemindItem.SECTION, String.valueOf(entry.getKey()));
                    section.sectionPosition = sectionPosition;
                    section.listPosition = listPosition++;
                    section.setMember(null);
                    adapter.onSectionAdded(section, sectionPosition);
                    adapter.add(section);
                    List<CircleMember.MemberInfo> memberList = entry.getValue();
                    for (int j = 0; j < memberList.size(); j++) {
                        CircleMember.MemberInfo member = memberList.get(j);
                        ChatRemindItem item = new ChatRemindItem(ChatRemindItem.ITEM, member.getName());
                        item.sectionPosition = sectionPosition;
                        item.listPosition = listPosition++;
                        item.setMemberInfo(member);
                        adapter.add(item);
                    }
                    sectionPosition++;
                }
                // adapter.notifyDataSetChanged();
            }
        } else {
            if (TextUtils.isEmpty(keyword)) {
                final int sectionsNumber = 1;
                listView.setAdapter(adapter);
                adapter.prepareSections(sectionsNumber);
            }
        }
        if (null != mapFormat && !mapFormat.isEmpty()) {
            sideBar.setVisibility(View.VISIBLE);
        } else {
            sideBar.setVisibility(View.INVISIBLE);
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
                ImageView iv_avator = (ImageView) view.findViewById(R.id.iv_avator);//成员头像
                TextView tx_name = (TextView) view.findViewById(R.id.tx_name);//成员姓名
                TextView job_txt = (TextView) view.findViewById(R.id.job_txt);//职务
                TextView company_txt = (TextView) view.findViewById(R.id.company_txt);//公司
                LinearLayout contentLl = (LinearLayout) view.findViewById(R.id.content_ll);//item选项
                LinearLayout titleLl = (LinearLayout) view.findViewById(R.id.title_ll);//不同组的分隔线
                View contactDivider = view.findViewById(R.id.contact_divider);//item间的分隔线

                ChatRemindItem item = getItem(position);
                if (item.type == ChatRemindItem.SECTION) {
                    titleLl.setVisibility(View.VISIBLE);
                    contentLl.setVisibility(View.GONE);
                    contactDivider.setVisibility(View.GONE);
                } else {
                    titleLl.setVisibility(View.GONE);
                    contentLl.setVisibility(View.VISIBLE);
                    tx_name.setText(item.getMemberInfo().getName());
                    String job = item.getMemberInfo().getTitle();
                    String company = item.getMemberInfo().getCompany();
                    if (!TextUtils.isEmpty(job)) {
                        job_txt.setVisibility(View.VISIBLE);
                        job_txt.setText(job);
                    } else {
                        job_txt.setVisibility(View.GONE);
                    }
                    if (!TextUtils.isEmpty(company)) {
                        company_txt.setVisibility(View.VISIBLE);
                        company_txt.setText(company);
                    } else {
                        company_txt.setVisibility(View.GONE);
                    }
                    ImageLoader.getInstance().displayImage(item.getMemberInfo().getUserfaceUrl(), iv_avator, CacheManager.circleImageOptions);
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
