package com.ping.android.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ping.android.activity.R;
import com.ping.android.model.User;
import com.ping.android.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;

public class GroupProfileAdapter extends RecyclerView.Adapter<GroupProfileAdapter.ViewHolder> {

    private static GroupProfileAdapter.ClickListener mClickListener;
    private ArrayList<User> originalContacts;
    private Context mContext;

    public GroupProfileAdapter(Context context, GroupProfileAdapter.ClickListener clickListener) {
        originalContacts = new ArrayList<>();
        mContext = context;
        mClickListener = clickListener;
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
        holder.tvName.setText(contact.getDisplayName());
        holder.tvUsername.setText(contact.pingID);
        holder.contact = contact;

        UiUtils.displayProfileImage(mContext, holder.ivProfileImage, contact);
    }

    @Override
    public int getItemCount() {
        return originalContacts.size();
    }

    public interface ClickListener {
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView tvUsername;
        public ImageView ivProfileImage;
        public TextView tvName;
        public User contact;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.group_profile_item_name);
            tvUsername = (TextView) itemView.findViewById(R.id.group_profile_item_username);
            ivProfileImage = (ImageView) itemView.findViewById(R.id.group_profile_item_image);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
            }
        }
    }

}
