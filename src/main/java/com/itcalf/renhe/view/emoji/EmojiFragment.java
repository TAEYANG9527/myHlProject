package com.itcalf.renhe.view.emoji;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alibaba.wukong.im.Conversation;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.adapter.FaceGVAdapter;
import com.itcalf.renhe.adapter.FaceVPAdapter;
import com.itcalf.renhe.context.wukong.im.ChatFragment;
import com.itcalf.renhe.utils.SharedPreferencesUtil;
import com.itcalf.renhe.view.TextView;
import com.itcalf.renhe.widget.emojitextview.Emotion;
import com.itcalf.renhe.widget.emojitextview.Emotions;
import com.itcalf.renhe.widget.emojitextview.EmotionsDB;

import java.util.ArrayList;
import java.util.List;

/**
 * description :
 * Created by Chans Renhenet
 * 2015/11/12
 */
public class EmojiFragment extends Fragment {

    private Context context;
    private View rootView;
    private ViewPager mViewPager;
    private LinearLayout mDotsLayout;
    private List<View> views;
    private LinearLayout mMoreLayout;//更多，发照片，发名片
    private FrameLayout imageGalleryLayout;
    private FrameLayout imageVCardLayout;
    private FrameLayout luckyMoneyLayout;
    private TextView newLuckyMoneyFlagTv;//有红包新功能的new
    private FrameLayout luckyMoneyAdLayout;//红包广告
    private TextView newLuckyMoneyAdFlagTv;//有红包广告新功能的new
    private RelativeLayout luckyMoneyAdRl;

    private static List<Emotion> emotionsList;
    private static String EMOJI_START = "hl_";
    private static String DELETE_KTY = "[删除]";
    private static int COLUMNS = 6;
    private static int ROWS = 4;

    public static EmojiFragment newInstance() {
        return new EmojiFragment();
    }

    private OnEmotionSelectedListener onEmotionSelectedListener;
    private OnMoreSelectedListener onMoreSelectedListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);
        if (null == rootView) {
            rootView = inflater.inflate(R.layout.activity_emoji_viewpage, null);
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        context = getActivity();
        findView(rootView);
        initData();
        initListener();
        return rootView;
    }

    protected void findView(View view) {
        mViewPager = (ViewPager) view.findViewById(R.id.face_viewpager);
        // 表情下小圆点
        mDotsLayout = (LinearLayout) view.findViewById(R.id.face_dots_container);
        mMoreLayout = (LinearLayout) view.findViewById(R.id.chat_imageselect_container);
        imageGalleryLayout = (FrameLayout) view.findViewById(R.id.imageFl);
        imageVCardLayout = (FrameLayout) view.findViewById(R.id.vcardFl);
        luckyMoneyLayout = (FrameLayout) view.findViewById(R.id.luckymoneyFl);
        newLuckyMoneyFlagTv = (TextView) view.findViewById(R.id.new_lucky_flag_tv);
        luckyMoneyAdLayout = (FrameLayout) view.findViewById(R.id.luckymoney_ad_Fl);
        luckyMoneyAdRl = (RelativeLayout) view.findViewById(R.id.luckymoney_ad_Rl);
        newLuckyMoneyAdFlagTv = (TextView) view.findViewById(R.id.new_lucky_ad_flag_tv);
        if (SharedPreferencesUtil.getBooleanShareData(Constants.SHAREDPREFERENCES_KEY.USER_USER_CHAT_WALLET_NEW, true, true))
            newLuckyMoneyFlagTv.setVisibility(View.VISIBLE);
        else
            newLuckyMoneyFlagTv.setVisibility(View.INVISIBLE);
        if (SharedPreferencesUtil.getBooleanShareData(Constants.SHAREDPREFERENCES_KEY.USER_USER_CHAT_LUCKYMONEY_AD_NEW, true, true))
            newLuckyMoneyAdFlagTv.setVisibility(View.VISIBLE);
        else
            newLuckyMoneyAdFlagTv.setVisibility(View.INVISIBLE);
    }


    private void initData() {
        //表情初始化
        initStaticFaces();
        InitViewPager();
        Bundle bundle = getArguments();
        if (null != bundle) {
            int conversationType = bundle.getInt(ChatFragment.CONVERSATION_TYPE_TAG, Conversation.ConversationType.GROUP);
            if (conversationType == Conversation.ConversationType.GROUP) {
                if (null != luckyMoneyAdRl)
                    luckyMoneyAdRl.setVisibility(View.VISIBLE);
            } else if (conversationType == Conversation.ConversationType.CHAT) {
                if (null != luckyMoneyAdRl)
                    luckyMoneyAdRl.setVisibility(View.GONE);
            }
        }
    }

    private void initListener() {
        mViewPager.setOnPageChangeListener(new PageChange());
        imageGalleryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMoreSelectedListener.onImageSelected();
            }
        });
        imageVCardLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMoreSelectedListener.onVCardSelected();
            }
        });
        luckyMoneyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newLuckyMoneyFlagTv.getVisibility() == View.VISIBLE) {
                    newLuckyMoneyFlagTv.setVisibility(View.INVISIBLE);
                    SharedPreferencesUtil.putBooleanShareData(Constants.SHAREDPREFERENCES_KEY.USER_USER_CHAT_WALLET_NEW, false, true);
                }
                onMoreSelectedListener.onluckyMoneySelected();
            }
        });
        luckyMoneyAdLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newLuckyMoneyAdFlagTv.getVisibility() == View.VISIBLE) {
                    newLuckyMoneyAdFlagTv.setVisibility(View.INVISIBLE);
                    SharedPreferencesUtil.putBooleanShareData(Constants.SHAREDPREFERENCES_KEY.USER_USER_CHAT_LUCKYMONEY_AD_NEW, false, true);
                }
                onMoreSelectedListener.onluckyMoneyAdSelected();
            }
        });
    }

    /**
     * 初始表情
     */
    private void InitViewPager() {
        // 获取页数
        int pageCount = getPagerCount();
        views = new ArrayList<>();
        mDotsLayout.removeAllViews();
        for (int i = 0; i < pageCount; i++) {
            views.add(viewPagerItem(i));
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(16, 16);
            mDotsLayout.addView(dotsItem(i), params);
        }
        FaceVPAdapter mVpAdapter = new FaceVPAdapter(views);
        mViewPager.setAdapter(mVpAdapter);
        if (null != mDotsLayout && mDotsLayout.getChildCount() > 0)
            mDotsLayout.getChildAt(0).setSelected(true);
    }

    /**
     * 表情页改变时，dots效果也要跟着改变
     */
    class PageChange implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int arg0) {
            for (int i = 0; i < mDotsLayout.getChildCount(); i++) {
                mDotsLayout.getChildAt(i).setSelected(false);
            }
            mDotsLayout.getChildAt(arg0).setSelected(true);
        }
    }

    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }


    public void setOnEmotionListener(OnEmotionSelectedListener onEmotionSelectedListener) {
        this.onEmotionSelectedListener = onEmotionSelectedListener;
    }

    public void setOnMoreSelectedListener(OnMoreSelectedListener onMoreSelectedListener) {
        this.onMoreSelectedListener = onMoreSelectedListener;
    }

    public interface OnEmotionSelectedListener {
        void onEmotionSelected(Emotion emotion);//发送表情
    }

    public interface OnMoreSelectedListener {
        void onImageSelected();//发送照片

        void onVCardSelected();//发送名片

        void onluckyMoneySelected();//发送红包

        void onluckyMoneyAdSelected();//发送红包广告
    }

    /**
     * 获取表情list
     */
    private void initStaticFaces() {
        // 获取所有表情
        if (null == emotionsList) {
            Emotions emotions = EmotionsDB.getEmotions(EMOJI_START);
            if (null != emotions)
                emotionsList = emotions.getEmotions();
        }
    }

    /**
     * 根据表情数量以及GridView设置的行数和列数计算Pager数量
     */
    public int getPagerCount() {
        if (null == emotionsList)
            return 0;
        int count = emotionsList.size();
        return count % (COLUMNS * ROWS - 1) == 0 ? count / (COLUMNS * ROWS - 1) : count / (COLUMNS * ROWS - 1) + 1;
    }

    /**
     * 加载每一页的表情
     * 每个表情点击之后的操作
     *
     * @param position 第几页位置
     * @return View
     */
    public View viewPagerItem(int position) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.activity_emoji_gridview, null);// 表情布局
        GridView gridview = (GridView) layout.findViewById(R.id.chart_face_gv);
        // 注：因为每一页末尾都有一个删除图标，所以每一页的实际表情columns *　rows　－　1; 空出最后一个位置给删除图标
        final List<Emotion> subList = new ArrayList<>();
        subList.addAll(emotionsList.subList(position * (COLUMNS * ROWS - 1),
                (COLUMNS * ROWS - 1) * (position + 1) > emotionsList.size() ? emotionsList.size()
                        : (COLUMNS * ROWS - 1) * (position + 1)));
        //末尾添加删除图标
        Emotion delete = new Emotion();
        byte[] d = EmotionsDB.getEmotion(DELETE_KTY);
        delete.setKey(DELETE_KTY);
        delete.setData(d);
        subList.add(delete);

        final FaceGVAdapter mGvAdapter = new FaceGVAdapter(subList, context, gridview);
        gridview.setAdapter(mGvAdapter);
        gridview.setNumColumns(COLUMNS);
        // 单击表情执行的操作
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onEmotionSelectedListener.onEmotionSelected(subList.get(position));
            }
        });
        return layout;
    }

    /**
     * viewPage表情底部原点位置
     *
     * @param position 滑块位置
     * @return ImageView
     */
    public ImageView dotsItem(int position) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dot_image, null);
        ImageView iv = (ImageView) layout.findViewById(R.id.face_dot);
        iv.setId(position);
        return iv;
    }

    public void changeShowingView(boolean toShowEmoji) {
        if (toShowEmoji) {
            mViewPager.setVisibility(View.VISIBLE);
            mDotsLayout.setVisibility(View.VISIBLE);
            mMoreLayout.setVisibility(View.GONE);
        } else {
            mViewPager.setVisibility(View.GONE);
            mDotsLayout.setVisibility(View.GONE);
            mMoreLayout.setVisibility(View.VISIBLE);
        }
    }

    public boolean isShowingEmoji() {
        return mViewPager.getVisibility() == View.VISIBLE;
    }
}
