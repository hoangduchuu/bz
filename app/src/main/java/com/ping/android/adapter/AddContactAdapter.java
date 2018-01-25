package com.ping.android.adapter;

import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.ping.android.activity.R;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.UiUtils;

import java.util.ArrayList;

public class AddContactAdapter extends RecyclerView.Adapter<AddContactAdapter.ViewHolder> {
    private ClickListener mClickListener;
    private ArrayList<User> displayContacts;

    public AddContactAdapter(ArrayList<User> mContacts, ClickListener clickListener) {
        this.displayContacts = mContacts;
        mClickListener = clickListener;
    }

    public void updateData(ArrayList<User> users) {
        this.displayContacts = users;
        notifyDataSetChanged();
    }

    public void addContact(User contact) {
        displayContacts.add(contact);
        notifyItemInserted(displayContacts.size() - 1);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User contact = displayContacts.get(position);

        holder.tvName.setText(contact.getDisplayName());
        holder.tvDetail.setText(contact.pingID);
        holder.contact = contact;
        holder.setClickListener(mClickListener);

        if (contact.typeFriend != null && contact.typeFriend == Constant.TYPE_FRIEND.IS_FRIEND) {
            holder.tgAddFriend.setChecked(false);
            holder.tgAddFriend.setEnabled(false);
        } else {
            holder.tgAddFriend.setChecked(true);
            holder.tgAddFriend.setEnabled(true);
        }
        UiUtils.displayProfileImage(holder.ivProfileImage.getContext(), holder.ivProfileImage, contact);
    }

    @Override
    public int getItemCount() {
        return displayContacts.size();
    }

    public interface ClickListener {
        void onAddFriend(User contact);

        void onSendMessage(User contact);

        void onViewProfile(User contact, Pair<View, String>... sharedElements);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView tvName, tvDetail;
        public ToggleButton tgAddFriend;
        public ImageView ivSendMessage;
        public ImageView ivProfileImage;
        private ClickListener clickListener;

        public User contact;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.contact_item_name);
            tvDetail = (TextView) itemView.findViewById(R.id.contact_item_detail);
            tgAddFriend = (ToggleButton) itemView.findViewById(R.id.contact_add_friend);
            tgAddFriend.setOnClickListener(this);
            ivSendMessage = (ImageView) itemView.findViewById(R.id.contact_send_message);
            ivSendMessage.setOnClickListener(this);
            ivProfileImage = (ImageView) itemView.findViewById(R.id.contact_item_profile);
            ivProfileImage.setOnClickListener(this);
        }

        public void setClickListener(ClickListener clickListener) {
            this.clickListener = clickListener;
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.contact_add_friend:
                    if (!ServiceManager.getInstance().getNetworkStatus(itemView.getContext())) {
                        tgAddFriend.setChecked(true);
                        return;
                    }
                    tgAddFriend.setChecked(false);
                    tgAddFriend.setEnabled(false);
                    clickListener.onAddFriend(contact);
                    break;
                case R.id.contact_send_message:
                    clickListener.onSendMessage(contact);
                    break;
                case R.id.contact_item_profile:
                    Pair imagePair = Pair.create(ivProfileImage, "imageProfile" + getAdapterPosition());
                    Pair namePair = Pair.create(tvName, "contactName" + getAdapterPosition());
                    clickListener.onViewProfile(contact, imagePair, namePair);
                    break;
            }
        }
    }
}
