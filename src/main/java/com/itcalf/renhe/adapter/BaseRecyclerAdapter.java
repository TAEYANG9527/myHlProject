package com.itcalf.renhe.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.itcalf.renhe.viewholder.RecyclerHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by wangning on 2015/10/12.
 */
public abstract class BaseRecyclerAdapter<T> extends RecyclerView.Adapter<RecyclerHolder> {

    protected List<T> realDatas;
    protected final int mItemLayoutId;
    protected Context cxt;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(View view, Object data, int position);

        boolean onItemLongClick(View view, Object data, int position);
    }

    public BaseRecyclerAdapter(RecyclerView v, Collection<T> datas, int itemLayoutId) {
        if (datas == null) {
            realDatas = new ArrayList<>();
        } else if (datas instanceof List) {
            realDatas = (List<T>) datas;
        } else {
            realDatas = new ArrayList<>(datas);
        }
        mItemLayoutId = itemLayoutId;
        cxt = v.getContext();
    }

    /**
     * Recycler适配器填充方法
     *
     * @param holder viewholder
     * @param item   javabean
     */
    public abstract void convert(RecyclerHolder holder, T item, int position);

    @Override
    public abstract RecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(RecyclerHolder holder, int position) {
        if (null != holder) {
            convert(holder, position < realDatas.size() ? realDatas.get(position) : null, position);
            holder.itemView.setOnClickListener(getOnClickListener(position));
            holder.itemView.setOnLongClickListener(getOnLongClickListener(position));
        }
    }

    @Override
    public int getItemCount() {
        return realDatas.size();
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        listener = l;
    }

    public View.OnClickListener getOnClickListener(final int position) {
        return new View.OnClickListener() {
            @Override
            public void onClick(@Nullable View v) {
                if (listener != null && v != null) {
                    listener.onItemClick(v, position < realDatas.size() ? realDatas.get(position) : null, position);
                }
            }
        };
    }

    public View.OnLongClickListener getOnLongClickListener(final int position) {
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null && v != null) {
                    return listener.onItemLongClick(v, position < realDatas.size() ? realDatas.get(position) : null, position);
                }
                return true;
            }
        };
    }

    public BaseRecyclerAdapter<T> refresh(Collection<T> datas) {
        if (datas == null) {
            realDatas = new ArrayList<>();
        } else if (datas instanceof List) {
            realDatas = (List<T>) datas;
        } else {
            realDatas = new ArrayList<>(datas);
        }
        return this;
    }

    /**
     * 实现者根据需求自己实现mList的排序
     */
    public void sort() {
    }

    public void addItem(T t) {
        if (t == null)
            return;
        if (!realDatas.contains(t)) {
            realDatas.add(t);
            notifyItemInserted(getItemCount());
        }
    }

    public void addFirstItem(T t) {
        if (t == null)
            return;
        if (!realDatas.contains(t)) {
            realDatas.add(0, t);
        }
        notifyItemInserted(0);
        notifyItemRangeChanged(0, getItemCount());
    }

    public void addItems(List<T> list) {
        if (list == null || list.size() == 0)
            return;

        for (T t : list) {
            if (!realDatas.contains(t)) {
                realDatas.add(t);
            }
        }
        sort();
        notifyDataSetChanged();
    }

    public void addFirstItems(List<T> list) {
        if (list == null || list.size() == 0)
            return;

        for (int i = list.size() - 1; i >= 0; i--) {
            if (!realDatas.contains(list.get(i))) {
                realDatas.add(0, list.get(i));
            }
        }
        sort();
        notifyDataSetChanged();
    }

    public void updateItems(List<T> list) {
        if (list == null || list.size() == 0)
            return;

        for (T t : list)
            updateItem(t);
//        notifyDataSetChanged();
    }

    public void updateItem(T t) {
        if (t == null)
            return;

        boolean result = false;
        int targetPosition = 0;

        if (realDatas.size() > 0) {
            Iterator<T> it = realDatas.iterator();

            while (it.hasNext()) {
                T current = it.next();
                if (current.equals(t)) {
                    result = true;
                    break;
                }
                targetPosition++;
            }

            //新的数据替换老的数据
            if (result) {
                realDatas.set(targetPosition, t);
                notifyItemChanged(targetPosition);
            } else {
                addItem(t);
            }
        }

    }

    public void removeItems(List<T> list) {
        if (list == null || list.size() == 0)
            return;

        for (T t : list) {
            if (realDatas.contains(t))
                realDatas.remove(t);
        }

        sort();
        notifyDataSetChanged();
    }
}
