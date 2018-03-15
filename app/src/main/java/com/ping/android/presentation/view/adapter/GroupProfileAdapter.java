package com.ping.android.presentation.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ping.android.activity.R;
import com.ping.android.model.User;
import com.ping.android.utils.UiUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupProfileAdapter extends RecyclerView.Adapter<GroupProfileAdapter.ViewHolder> {
    private ArrayList<User> originalContacts;
    private Map<String, String> nickNames = new HashMap<>();

    public GroupProfileAdapter() {
        originalContacts = new ArrayList<>();
    }

    public void addContact(User contact) {
        originalContacts.add(contact);
        notifyItemInserted(originalContacts.size() - 1);
    }

    public void initContact(List<User> contacts) {
        originalContacts.clear();
        originalContacts.addAll(contacts);
        notifyDataSetChanged();
    }

    public void updateContact(User contact) {
        for (int i = 0; i < originalContacts.size(); i++) {
            if (originalContacts.get(i).key.equals(contact.key)) {
                originalContacts.set(i, contact);
                break;
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public GroupProfileAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_profile, parent, false);
        return new GroupProfileAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GroupProfileAdapter.ViewHolder holder, int position) {
        User contact = originalContacts.get(position);
        String nickName = nickNames.get(contact.key);
        holder.tvName.setText(TextUtils.isEmpty(nickName) ? contact.getDisplayName() : nickName);
        holder.tvUsername.setText(contact.pingID);
        holder.contact = contact;

        UiUtils.displayProfileImage(holder.itemView.getContext(), holder.ivProfileImage, contact);
    }

    @Override
    public int getItemCount() {
        return originalContacts.size();
    }

    public void updateNickNames(Map<String, String> nickNames) {
        this.nickNames = nickNames;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView tvUsername;
        public ImageView ivProfileImage;
        public TextView tvName;
        public User contact;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.group_profile_item_name);
            tvUsername = itemView.findViewById(R.id.group_profile_item_username);
            ivProfileImage = itemView.findViewById(R.id.group_profile_item_image);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
            }
        }
    }

}
