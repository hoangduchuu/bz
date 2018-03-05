package com.bzzzchat.flexibleadapter;

import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.bzzzchat.flexibleadapter.baseitems.LoadingItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tuanluong on 3/2/18.
 */

public class FlexibleAdapter<T extends FlexibleItem> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected List<T> items = new ArrayList<T>();
    private SparseArrayCompat<T> viewTypes = new SparseArrayCompat<T>();
    private LoadingItem loadingItem;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return viewTypes.get(viewType).onCreateViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        boolean isLastPosition = position == (getItemCount() - 1);
        items.get(position).onBindViewHolder(holder, isLastPosition);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        int key = items.get(position).getLayoutId();
        T item = viewTypes.get(key);
        if (item == null) {
            viewTypes.put(key, items.get(position));
        }
        return key;
    }

    public void add(T item) {
        int position = items.size();
        this.items.add(item);
        this.notifyItemInserted(position);
    }

    public void add(T item, int position) {
        this.items.add(position, item);
        this.notifyItemInserted(position);
    }

    public void update(T item, int position) {
        this.items.set(position, item);
        this.notifyItemChanged(position);
    }
}
