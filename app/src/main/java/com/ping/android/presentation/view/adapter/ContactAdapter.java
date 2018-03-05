package com.ping.android.presentation.view.adapter;

import android.content.Context;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ping.android.activity.R;
import com.ping.android.model.User;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.utils.UiUtils;

import java.util.ArrayList;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private ClickListener mClickListener;
    private ArrayList<User> originalContacts;
    private ArrayList<User> displayContacts;
    private Context mContext;

    public ContactAdapter(Context context, ClickListener clickListener) {
        originalContacts = new ArrayList<>();
        displayContacts = new ArrayList<>();
        mContext = context;
        mClickListener = clickListener;
    }

    public void addContact(User contact) {
        originalContacts.add(contact);
        displayContacts.add(contact);
        notifyItemInserted(displayContacts.size() - 1);
    }

    public void updateContact(User contact) {
        boolean isExisted = false;
        for (int i = 0; i < originalContacts.size(); i++) {
            if (originalContacts.get(i).key.equals(contact.key)) {
                originalContacts.set(i, contact);
                isExisted = true;
                break;
            }
        }
        for (int i = 0; i < displayContacts.size(); i++) {
            if (displayContacts.get(i).key.equals(contact.key)) {
                displayContacts.set(i, contact);
                break;
            }
        }
        if(!isExisted) {
            addContact(contact);
            return;
        }
        notifyDataSetChanged();
    }

    public void deleteContact(String id) {
        for (int i = 0; i < originalContacts.size(); i++) {
            if (originalContacts.get(i).key.equals(id)) {
                originalContacts.remove(i);
                break;
            }
        }
        for (int i = 0; i < displayContacts.size(); i++) {
            if (displayContacts.get(i).key.equals(id)) {
                displayContacts.remove(i);
                break;
            }
        }
        notifyDataSetChanged();
    }

    public void filter(String text) {
        displayContacts = new ArrayList<>();
        for (User contact : originalContacts) {
            if (CommonMethod.isFiltered(contact, text)) {
                displayContacts.add(contact);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User contact = displayContacts.get(position);
        holder.tvName.setText(contact.getDisplayName());
        holder.contact = contact;
        //holder.tgStatus.setChecked(contact.loginStatus);
        holder.tvName.setTransitionName("contactName" + position);
        holder.ivProfileImage.setTransitionName("imageProfile" + position);
        UiUtils.displayProfileImage(mContext, holder.ivProfileImage, contact);
    }

    @Override
    public int getItemCount() {
        return displayContacts.size();
    }

    public interface ClickListener {
        void onSendMessage(User contact);
        void onVoiceCall(User contact);
        void onVideoCall(User contact);
        void onOpenProfile(User contact, Pair<View, String>... sharedElements);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView ivProfileImage;
        public TextView tvName;
        //public ToggleButton tgStatus;
        public User contact;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.friend_name);
            //tgStatus = (ToggleButton) itemView.findViewById(R.id.friend_status);
            ivProfileImage = (ImageView) itemView.findViewById(R.id.friend_profile);
            itemView.setOnClickListener(this);
            itemView.findViewById(R.id.friend_call_video).setOnClickListener(this);
            itemView.findViewById(R.id.friend_call_voice).setOnClickListener(this);
            itemView.findViewById(R.id.friend_message).setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.friend_call_video:
                    mClickListener.onVideoCall(contact);
                    break;
                case R.id.friend_call_voice:
                    mClickListener.onVoiceCall(contact);
                    break;
                case R.id.friend_message:
                    mClickListener.onSendMessage(contact);
                    break;
                default:
                    Pair imagePair = Pair.create(ivProfileImage, "imageProfile" + getAdapterPosition());
                    Pair namePair = Pair.create(tvName, "contactName" + getAdapterPosition());
                    mClickListener.onOpenProfile(contact, imagePair, namePair);
            }
        }
    }
}
