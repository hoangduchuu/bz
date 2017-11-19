package com.ping.android.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.ping.android.activity.R;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.UiUtils;

import java.util.ArrayList;

public class AddContactAdapter extends RecyclerView.Adapter<AddContactAdapter.ViewHolder> {

    private static Context mContext;
    private static ClickListener mClickListener;
    private ArrayList<User> originalContacts;
    private ArrayList<User> displayContacts;

    public AddContactAdapter(Context context, ArrayList<User> mContacts, ClickListener clickListener) {
        this.originalContacts = mContacts;
        this.displayContacts = (ArrayList<User>) mContacts.clone();
        mContext = context;
        mClickListener = clickListener;
    }

    public void addContact(User contact) {
        originalContacts.add(contact);
        displayContacts.add(contact);
        notifyItemInserted(displayContacts.size() - 1);
    }

    public void filter(String text) {
        displayContacts = new ArrayList<>();
        for (User contact : originalContacts) {
            if (CommonMethod.isFilteredContact(contact, text)) {
                displayContacts.add(contact);
            }
        }
        notifyDataSetChanged();
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

        if (contact.typeFriend != null && contact.typeFriend == Constant.TYPE_FRIEND.IS_FRIEND) {
            holder.tgAddFriend.setChecked(false);
            holder.tgAddFriend.setEnabled(false);
        } else {
            holder.tgAddFriend.setChecked(true);
            holder.tgAddFriend.setEnabled(true);
        }

        UiUtils.displayProfileImage(mContext, holder.ivProfileImage, contact);
    }

    @Override
    public int getItemCount() {
        return displayContacts.size();
    }

    public interface ClickListener {
        void onAddFriend(User contact);

        void onSendMessage(User contact);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView tvName, tvDetail;
        public ToggleButton tgAddFriend;
        public ImageView ivSendMessage;
        public ImageView ivProfileImage;

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
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.contact_add_friend:
                    if (!ServiceManager.getInstance().getNetworkStatus(mContext)) {
                        Toast.makeText(mContext, "Please check network connection", Toast.LENGTH_SHORT).show();
                        tgAddFriend.setChecked(true);
                        return;
                    }
                    tgAddFriend.setChecked(false);
                    tgAddFriend.setEnabled(false);
                    mClickListener.onAddFriend(contact);
                    break;
                case R.id.contact_send_message:
                    mClickListener.onSendMessage(contact);
                    break;
            }
        }
    }
}
