package com.ping.android.presentation.view.flexibleitem.messages;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bzzzchat.flexibleadapter.FlexibleItem;
import com.ping.android.activity.R;

import org.jetbrains.annotations.NotNull;

/**
 * Created by tuanluong on 3/7/18.
 */

public class TypingItem implements FlexibleItem {
    @Override
    public int getLayoutId() {
        return R.layout.item_chat_left_typing;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_left_typing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, boolean lastItem) {

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
