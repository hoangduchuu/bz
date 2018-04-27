package com.ping.android.presentation.view.flexibleitem.messages;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bzzzchat.flexibleadapter.FlexibleItem;
import com.ping.android.R;
import com.ping.android.model.Message;
import com.ping.android.utils.DateUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

public class MessageHeaderItem implements FlexibleItem<MessageHeaderItem.ViewHolder> {
    private TreeMap<Double, MessageBaseItem> childItemTreeMap;
    private List<MessageBaseItem> childItems;
    private List<MessageBaseItem> newItems;

    public MessageHeaderItem() {
        childItemTreeMap = new TreeMap<>();
        childItems = new ArrayList<>();
        newItems = new ArrayList<>();
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
        prepareMessage(item);
        return isAdded;
    }

    public void addNewItem(MessageBaseItem item) {
        newItems.add(item);
    }

    public int removeMessage(MessageBaseItem data) {
        int index = findChildIndex(data);
        childItemTreeMap.remove(data.message.timestamp);
        childItems = new ArrayList<>(childItemTreeMap.values());
        return index;
    }

    public List<MessageBaseItem> getChildItems() {
        return childItems;
    }

    public List<MessageBaseItem> getNewItems() {
        return newItems;
    }

    public int findChildIndex(MessageBaseItem item) {
        int index = childItems.indexOf(item);
        return index > 0 ? index : 0;
    }

    private void prepareMessage(MessageBaseItem item) {
        int newAddedIndex = findChildIndex(item);
        if (newAddedIndex > 0 && newAddedIndex < childItems.size()) {
            MessageBaseItem previousMessage = childItems.get(newAddedIndex - 1);
            item.message.showExtraInfo = !item.message.senderId.equals(previousMessage.message.senderId);
        }
    }

    public MessageBaseItem getChildItem(Message data) {
        return childItemTreeMap.get(data.timestamp);
    }

    public void processNewItems() {
        for (MessageBaseItem item : newItems) {
            addChildItem(item);
        }
        newItems = new ArrayList<>();
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
