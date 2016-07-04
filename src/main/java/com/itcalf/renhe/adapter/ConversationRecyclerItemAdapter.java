package com.itcalf.renhe.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.alibaba.wukong.im.Conversation;
import com.itcalf.renhe.R;
import com.itcalf.renhe.dto.ConversationItem;
import com.itcalf.renhe.utils.ConversationListUtil;
import com.itcalf.renhe.viewholder.ConversationViewHolder;
import com.itcalf.renhe.viewholder.EmptyViewHolder;
import com.itcalf.renhe.viewholder.RecyclerHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * 人脉圈列表的recycler adapter
 * Author:wang ning
 */
public class ConversationRecyclerItemAdapter extends BaseRecyclerAdapter<ConversationItem> {
    //会话类型
    public static final int ITEM_TYPE_NORMAL_CONVERSATIION = 1;//普通会话，包括IM会话、行业头条、和聊助手

    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private RecyclerView mRecyclerView;
    private ConversationListUtil conversationListUtil;

    /**
     * 该adapter被对话列表、转发选择最近聊天联系人列表公用，用此字段区分是否是对话列表，如果不是，则未读数不需要显示
     */
    private boolean isNormalConversationList = true;

    public ConversationRecyclerItemAdapter(Context context, RecyclerView view, ArrayList<ConversationItem> datas, ConversationListUtil conversationListUtil) {
        super(view, datas, 0);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        this.mRecyclerView = view;
        this.conversationListUtil = conversationListUtil;
    }

    public ConversationRecyclerItemAdapter(Context context, RecyclerView view, ArrayList<ConversationItem> datas, ConversationListUtil conversationListUtil
            , boolean isNormalConversationList) {
        this(context, view, datas, conversationListUtil);
        this.isNormalConversationList = isNormalConversationList;
    }

    @Override
    public void convert(RecyclerHolder holder, ConversationItem item, int position) {
        if (null != holder)
            holder.initView(holder, item, position);
    }

    @Override
    public RecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_TYPE_NORMAL_CONVERSATIION:
                return new ConversationViewHolder(mContext, mLayoutInflater.inflate(R.layout.conversation_list_recycler_item,
                        parent, false), mRecyclerView, this);
            default:
                break;
        }
        return new EmptyViewHolder(mContext, mLayoutInflater.inflate(R.layout.renmaiquan_item_empty_layout, parent, false), this);
    }

    @Override
    public void onBindViewHolder(RecyclerHolder holder, int position) {
        super.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemViewType(int position) {
        if (position < realDatas.size()) {
            return ITEM_TYPE_NORMAL_CONVERSATIION;
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return realDatas == null ? 0 : realDatas.size();
    }

    public void addConversationItems(ArrayList<ConversationItem> conversationItems) {
        if (conversationItems == null || conversationItems.size() == 0)
            return;

        for (ConversationItem t : conversationItems) {
            if (!isConversationExist(t)) {
                realDatas.add(t);
            }
        }
        sort();
        notifyDataSetChanged();
    }

    public void addFirstConversationItems(ArrayList<ConversationItem> conversationItems) {
        if (conversationItems == null || conversationItems.size() == 0)
            return;

        for (int i = conversationItems.size() - 1; i >= 0; i--) {
            if (!isConversationExist(conversationItems.get(i))) {
                realDatas.add(0, conversationItems.get(i));
            }
        }
        sort();
        notifyDataSetChanged();
    }

    public boolean isConversationExist(ConversationItem conversationItem) {
        for (ConversationItem conversationItem1 : realDatas) {
            if (conversationItem1.getType() == conversationItem.getType()) {
                if (conversationItem.getType() == ConversationItem.IM_CONVERSATION_TYPE) {
                    if (conversationItem1.getConversation().conversationId().equals(conversationItem.getConversation().conversationId()))
                        return true;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 更新单个message
     *
     * @param conversationItem
     */
    public void updateConversationItem(ConversationItem conversationItem) {
        if (conversationItem == null)
            return;
        boolean result = false;
        int targetPosition = 0;
        Iterator<ConversationItem> it = realDatas.iterator();
        while (it.hasNext()) {
            ConversationItem current = it.next();
            if (current.getType() == conversationItem.getType()) {
                if (conversationItem.getType() == ConversationItem.IM_CONVERSATION_TYPE) {
                    if (conversationItem.getConversation().conversationId().equals(current.getConversation().conversationId())) {
                        result = true;
                        break;
                    }
                } else {
                    result = true;
                    break;
                }
            }
            targetPosition++;
        }

        //新的数据替换老的数据
        if (result) {
            realDatas.set(targetPosition, conversationItem);
//                notifyItemChanged(targetPosition);
        } else {
            addFirstItem(conversationItem);
        }
    }

    /**
     * 更新多个message
     *
     * @param conversationItems
     */
    public void updateConversationItems(ArrayList<ConversationItem> conversationItems) {
        if (conversationItems == null || conversationItems.isEmpty())
            return;
        for (ConversationItem conversationItem : conversationItems)
            updateConversationItem(conversationItem);
        sort();
        notifyDataSetChanged();
    }

    /**
     * 删除单条message
     *
     * @param conversationItem
     */
    public void deleteConversationItem(ConversationItem conversationItem) {
        if (conversationItem == null)
            return;
        boolean result = false;
        int targetPosition = 0;

        if (realDatas.size() > 0) {
            Iterator<ConversationItem> it = realDatas.iterator();
            while (it.hasNext()) {
                ConversationItem current = it.next();
                if (current.getType() == conversationItem.getType()) {
                    if (conversationItem.getType() == ConversationItem.IM_CONVERSATION_TYPE) {
                        if (conversationItem.getConversation().conversationId().equals(current.getConversation().conversationId())) {
                            result = true;
                            break;
                        }
                    } else {
                        result = true;
                        break;
                    }
                }
                targetPosition++;
            }

            //新的数据替换老的数据
            if (result) {
                realDatas.remove(targetPosition);
                notifyItemRangeChanged(targetPosition, getItemCount());
            }
        }
    }

    /**
     * 删除多条message
     *
     * @param conversationItems
     */
    public void deleteConversationItems(ArrayList<ConversationItem> conversationItems) {
        if (conversationItems == null || conversationItems.isEmpty())
            return;
        realDatas.removeAll(conversationItems);
        notifyDataSetChanged();
    }

    public List<ConversationItem> getDatasList() {
        return realDatas;
    }

    @Override
    public void sort() {
        Collections.sort(realDatas, new Comparator<ConversationItem>() {
            @Override
            public int compare(ConversationItem lhs, ConversationItem rhs) {
                int value = 0;
                long lhsTime, rhsTime;
                if (lhs.getType() == ConversationItem.IM_CONVERSATION_TYPE) {
                    Conversation lhsConversation = lhs.getConversation();
                    lhsTime = lhsConversation.latestMessage() == null ? lhsConversation.createdAt()
                            : lhsConversation.latestMessage().createdAt();
                } else {
                    lhsTime = lhs.getConversationListOtherItem().getCreateTime();
                }
                if (rhs.getType() == ConversationItem.IM_CONVERSATION_TYPE) {
                    Conversation rhsConversation = rhs.getConversation();
                    rhsTime = rhsConversation.latestMessage() == null ? rhsConversation.createdAt()
                            : rhsConversation.latestMessage().createdAt();
                } else {
                    rhsTime = rhs.getConversationListOtherItem().getCreateTime();
                }
                if (lhsTime < rhsTime)
                    value = 1;
                else if (lhsTime > rhsTime)
                    value = -1;
                else
                    value = 0;
                return value;

            }
        });
    }

    public ConversationListUtil getConversationListUtil() {
        return conversationListUtil;
    }

    public void setConversationListUtil(ConversationListUtil conversationListUtil) {
        this.conversationListUtil = conversationListUtil;
    }

    public boolean isNormalConversationList() {
        return isNormalConversationList;
    }

    public void setIsNormalConversationList(boolean isNormalConversationList) {
        this.isNormalConversationList = isNormalConversationList;
    }
}
