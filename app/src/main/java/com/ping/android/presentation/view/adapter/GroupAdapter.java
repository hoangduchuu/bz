package com.ping.android.presentation.view.adapter;

import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ping.android.R;
import com.ping.android.model.Group;
import com.ping.android.model.User;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {

    private ArrayList<Group> originalGroups;
    private ArrayList<Group> displayGroups;
    private ArrayList<Group> selectGroups;
    private Boolean isEditMode = false;
    private ClickListener clickListener;

    public GroupAdapter(ClickListener clickListener) {
        originalGroups = new ArrayList<>();
        displayGroups = new ArrayList<>();
        selectGroups = new ArrayList<>();
        this.clickListener = clickListener;
    }

    public void addOrUpdateConversation(Group group) {
        Boolean isAdd = true;
        for (int i = 0; i < originalGroups.size(); i++) {
            if (originalGroups.get(i).key.equals(group.key)) {
                isAdd = false;
                break;
            }
        }
        if (isAdd) {
            addGroup(group);
        } else {
            updateGroup(group);
        }
    }

    public void addGroup(Group group) {
        int index = 0;
        for (Group item : originalGroups) {
            if (CommonMethod.compareTimestamp(group.timestamp, item.timestamp))
                index ++;
            else
                break;
        }
        originalGroups.add(index, group);

        index = 0;
        for (Group item : displayGroups) {
            if (CommonMethod.compareTimestamp(group.timestamp, item.timestamp))
                index ++;
            else
                break;
        }

        displayGroups.add(index, group);
        notifyItemInserted(index);
    }

    public void updateGroup(Group group) {
        for (int i = 0; i < originalGroups.size(); i++) {
            if (originalGroups.get(i).key.equals(group.key)) {
                originalGroups.set(i, group);
                break;
            }
        }
        for (int i = 0; i < displayGroups.size(); i++) {
            if (displayGroups.get(i).key.equals(group.key)) {
                displayGroups.set(i, group);
                break;
            }
        }
        notifyDataSetChanged();
    }

    public void deleteGroup(String groupID) {
        Group deletedGroup = null;
        for (Group group : originalGroups) {
            if (group.key.equals(groupID)) {
                deletedGroup = group;
            }
        }
        if (deletedGroup != null) {
            originalGroups.remove(deletedGroup);
            displayGroups.remove(deletedGroup);
            selectGroups.remove(deletedGroup);
            notifyDataSetChanged();
        }
    }

    public void filter(String text) {
        displayGroups = new ArrayList<>();
        for (Group group : originalGroups) {
            if (isFiltered(group, text)) {
                displayGroups.add(group);
            }
        }
        notifyDataSetChanged();
    }

    private boolean isFiltered(Group group, String text) {
        if (TextUtils.isEmpty(text)) {
            return true;
        }
        return group.groupName.toUpperCase().contains(text.toUpperCase());
    }

    public void setEditMode(Boolean isEditMode) {
        this.isEditMode = isEditMode;
        if (!isEditMode) {
            selectGroups.clear();
        }
        notifyDataSetChanged();
    }

    public ArrayList<Group> getSelectGroup() {
        return selectGroups;
    }

    public void cleanSelectGroup() {
        selectGroups.clear();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
        return new GroupAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Group group = displayGroups.get(position);
        holder.bindData(group);
    }

    @Override
    public int getItemCount() {
        return displayGroups.size();
    }

    public void removeGroup(String key) {
        Group removedGroup = null;
        for (int i = 0, size = displayGroups.size(); i < size; i++) {
            if (displayGroups.get(i).key.equals(key)) {
                removedGroup = displayGroups.get(i);
                displayGroups.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
        if (removedGroup != null) {
            originalGroups.remove(removedGroup);
        }
    }

    public interface ClickListener {
        void onSendMessage(Group group);
        void onViewProfile(Group group, Pair<View, String>... sharedElements);
        void onSelect(ArrayList<Group> groups);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView ivProfileImage;
        public TextView tvGroupName, tvGroupMember, tvCreateTime;
        RadioButton rbSelect;
        public Group group;

        public ViewHolder(View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.group_item_name);
            tvGroupMember = itemView.findViewById(R.id.group_item_members);
            tvCreateTime = itemView.findViewById(R.id.group_item_create_date);
            ivProfileImage = itemView.findViewById(R.id.group_item_profile);
            ivProfileImage.setOnClickListener(this);
            rbSelect = itemView.findViewById(R.id.group_item_select);
            rbSelect.setOnClickListener(this);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (isEditMode) {
                onClickEditMode(view);
                return;
            }

            if (view.getId() == R.id.group_item_profile) {
                Pair imagePair = Pair.create(ivProfileImage, "imageProfile" + getAdapterPosition());
                clickListener.onViewProfile(group, imagePair);
            }
            else {
                clickListener.onSendMessage(group);
            }
        }

        private void onClickEditMode(View view) {
            boolean isSelect;
            switch (view.getId()) {
                case R.id.group_item_select:
                    isSelect = !rbSelect.isSelected();
                    rbSelect.setChecked(isSelect);
                    rbSelect.setSelected(isSelect);
                    break;
                default:
                    isSelect = !rbSelect.isSelected();
                    rbSelect.setChecked(isSelect);
                    rbSelect.setSelected(isSelect);
                    break;
            }
            selectGroup();
        }

        private void selectGroup() {
            if (rbSelect.isChecked()) {
                selectGroups.add(group);
            } else {
                selectGroups.remove(group);
            }
            clickListener.onSelect(selectGroups);
        }

        public void setEditMode(boolean isEditMode) {
            rbSelect.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        }

        public void setSelect(Boolean isSelect) {
            rbSelect.setChecked(isSelect);
            rbSelect.setSelected(isSelect);
        }

        public void bindData(Group group) {
            this.group = group;
            tvGroupName.setText(group.groupName);
            List<String> displayNames = new ArrayList<>();
            for (User contact : group.members) {
                if (group.deleteStatuses.containsKey(contact.key)) continue;
                displayNames.add(contact.getFirstName());
            }
            tvGroupMember.setText(TextUtils.join(", ", displayNames));
            ivProfileImage.setTransitionName("imageProfile" + getAdapterPosition());
            UiUtils.displayProfileAvatar(ivProfileImage, group.groupAvatar);
            tvCreateTime.setText("Created: " + CommonMethod.convertTimestampToDate(group.timestamp));
            setEditMode(isEditMode);
            if (isEditMode) {
                setSelect(selectGroups.contains(group));
            }
        }
    }
}
