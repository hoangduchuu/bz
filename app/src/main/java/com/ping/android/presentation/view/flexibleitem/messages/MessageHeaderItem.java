package com.ping.android.presentation.view.flexibleitem.messages;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bzzzchat.flexibleadapter.FlexibleItem;
import com.ping.android.activity.R;
import com.ping.android.utils.DateUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

public class MessageHeaderItem implements FlexibleItem<MessageHeaderItem.ViewHolder> {
    private TreeMap<Double, MessageBaseItem> childItemTreeMap;
    private List<MessageBaseItem> childItems;

    public MessageHeaderItem() {
        childItemTreeMap = new TreeMap<>();
        childItems = new ArrayList<>();
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_header;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(getLayoutId(), parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull ViewHolder holder, boolean lastItem) {
        holder.bindData(getFirstDate());
    }

    private Date getFirstDate() {
        MessageBaseItem baseItem = childItemTreeMap.firstEntry().getValue();
        long timestamp = (long) (baseItem.message.timestamp * 1000);
        return new Date(timestamp);
    }

    public boolean addChildItem(MessageBaseItem item) {
        boolean isAdded = true;
        if (childItemTreeMap.get(item.message.timestamp) != null) {
            isAdded = false;
        }
        childItemTreeMap.put(item.message.timestamp, item);
        childItems = new ArrayList<>(childItemTreeMap.values());
        return isAdded;
    }

    public List<MessageBaseItem> getChildItems() {
        return childItems;
    }

    public int findChildIndex(MessageBaseItem item) {
        int index = childItems.indexOf(item);
        return index > 0 ? index : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;

        public ViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date);
        }

        public void bindData(Date date) {
            tvDate.setText(DateUtils.toHeaderString(date));
        }
    }
}
