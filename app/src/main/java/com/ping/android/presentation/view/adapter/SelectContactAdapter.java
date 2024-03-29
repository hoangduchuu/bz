package com.ping.android.presentation.view.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ping.android.R;
import com.ping.android.model.User;
import com.ping.android.utils.CommonMethod;
import com.ping.android.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;

public class SelectContactAdapter extends RecyclerView.Adapter<SelectContactAdapter.ViewHolder> {

    private ArrayList<String> selectPingIDs;
    private ClickListener mClickListener;
    private ArrayList<User> originalContacts;
    private ArrayList<User> displayContacts;

    public SelectContactAdapter(ArrayList<User> mContacts, ClickListener clickListener) {
        this.originalContacts = mContacts;
        this.displayContacts = (ArrayList<User>) mContacts.clone();
        selectPingIDs = new ArrayList<>();
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

    public void toggleSelect(User contact, boolean isSelect) {
        if (isSelect) {
            selectPingIDs.add(contact.pingID);
        } else {
            selectPingIDs.remove(contact.pingID);
        }
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
        holder.itemView.setOnClickListener(view -> toggleSelect(holder));
        holder.rbSelect.setOnClickListener(view -> toggleSelect(holder));
        holder.tvName.setText(contact.getDisplayName());
        holder.tvPingId.setText(contact.pingID);
        holder.contact = contact;

        if (selectPingIDs.contains(contact.pingID)) {
            holder.rbSelect.setChecked(true);
        } else {
            holder.rbSelect.setChecked(false);
        }

        UiUtils.displayProfileImage(holder.ivProfileImage.getContext(), holder.ivProfileImage, contact);
    }

    private void toggleSelect(ViewHolder holder) {
        boolean currentStatus = selectPingIDs.contains(holder.contact.pingID);
        holder.rbSelect.setChecked(!currentStatus);
        boolean isSelected = !currentStatus;
        if (isSelected) {
            selectPingIDs.add(holder.contact.pingID);
        } else {
            selectPingIDs.remove(holder.contact.pingID);
        }
        mClickListener.onSelect(holder.contact, !currentStatus);
    }

    @Override
    public int getItemCount() {
        return displayContacts.size();
    }

    public interface ClickListener {
        void onSelect(User contact, Boolean isSelected);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvPingId;
        RadioButton rbSelect;
        ImageView ivProfileImage;
        public User contact;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.contact_item_name);
            tvPingId = itemView.findViewById(R.id.contact_item_detail);
            rbSelect = itemView.findViewById(R.id.contact_item_select);
            ivProfileImage = itemView.findViewById(R.id.contact_item_profile);
        }
    }
}
