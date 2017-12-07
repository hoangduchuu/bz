package com.ping.android.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ping.android.activity.R;
import com.ping.android.model.User;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;

public class SelectContactAdapter extends RecyclerView.Adapter<SelectContactAdapter.ViewHolder> {

    private ArrayList<String> selectPingIDs;
    private ClickListener mClickListener;
    private ArrayList<User> originalContacts;
    private ArrayList<User> displayContacts;
    private Context mContext;

    public SelectContactAdapter(Context context, ArrayList<User> mContacts, ClickListener clickListener) {
        this.originalContacts = mContacts;
        this.displayContacts = (ArrayList<User>) mContacts.clone();
        selectPingIDs = new ArrayList<>();
        mContext = context;
        mClickListener = clickListener;
    }

    public void updateData(ArrayList<User> users) {
        this.originalContacts = users;
        this.displayContacts = (ArrayList<User>) users.clone();
        notifyDataSetChanged();
    }

    public void addContact(User contact) {
        originalContacts.add(contact);
        displayContacts.add(contact);
        notifyItemInserted(displayContacts.size() - 1);
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

    public ArrayList<String> getSelectPingIDs() {
        return selectPingIDs;
    }

    public void setSelectPingIDs(List<String> selectPingIDs) {
        this.selectPingIDs = new ArrayList<String>(selectPingIDs);
        notifyDataSetChanged();
    }

    public ArrayList<String> getSelectUserIDs() {
        ArrayList<String> selectUserIDs = new ArrayList<>();
        for (User contact : originalContacts) {
            if (selectPingIDs.contains(contact.pingID))
                selectUserIDs.add(contact.key);
        }
        return selectUserIDs;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User contact = displayContacts.get(position);
        holder.setClickListener((contact1, isSelected1) -> {
            boolean currentStatus = selectPingIDs.contains(contact1.pingID);
            holder.rbSelect.setChecked(!currentStatus);
            boolean isSelected = !currentStatus;
            if (isSelected) {
                selectPingIDs.add(contact1.pingID);
            } else {
                selectPingIDs.remove(contact1.pingID);
            }
            if (mClickListener != null) {
                mClickListener.onSelect(contact1, isSelected);
            }
        });
        holder.tvName.setText(contact.getDisplayName());
        holder.contact = contact;

        if (selectPingIDs.contains(contact.pingID)) {
            holder.rbSelect.setChecked(true);
        } else {
            holder.rbSelect.setChecked(false);
        }

        UiUtils.displayProfileImage(mContext, holder.ivProfileImage, contact);
    }

    @Override
    public int getItemCount() {
        return displayContacts.size();
    }

    public interface ClickListener {
        void onSelect(User contact, Boolean isSelected);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView tvName;
        public RadioButton rbSelect;
        public User contact;
        public ImageView ivProfileImage;
        public ClickListener mClickListener;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.contact_item_name);
            rbSelect = (RadioButton) itemView.findViewById(R.id.contact_item_select);
            rbSelect.setOnClickListener(this);
            ivProfileImage = (ImageView) itemView.findViewById(R.id.contact_item_profile);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.contact_item_select:
                    selectContact();
                    break;
            }
        }

        public void setClickListener(ClickListener clickListener) {
            this.mClickListener = clickListener;
        }

        private void selectContact() {
//            boolean isSelect = !rbSelect.isSelected();
//            rbSelect.setChecked(isSelect);
//            rbSelect.setSelected(isSelect);

            if (mClickListener != null)
                mClickListener.onSelect(contact, rbSelect.isChecked());
        }
    }
}
