package com.ping.android.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ping.android.activity.R;
import com.ping.android.model.Group;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.CommonMethod;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {

    private ArrayList<Group> originalGroups;
    private ArrayList<Group> displayGroups;
    private ArrayList<Group> selectGroups;
    private Boolean isEditMode = false;
    private Context mContext;
    private ClickListener clickListener;

    public GroupAdapter(Context context, ClickListener clickListener) {
        originalGroups = new ArrayList<>();
        displayGroups = new ArrayList<>();
        selectGroups = new ArrayList<>();
        mContext = context;
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
        if (StringUtils.isEmpty(text)) {
            return true;
        }
        if (group.groupName.toUpperCase().contains(text.toUpperCase()))
            return true;
        return false;
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
        holder.group = group;
        holder.setGroupName(group.groupName);
        List<String> displayNames = new ArrayList<>();
        for (User contact : group.members) {
            displayNames.add(ServiceManager.getInstance().getFirstName(contact));
        }
        holder.setGroupMember(TextUtils.join(", ", displayNames));
        holder.updateProfileImage();
        holder.setCreateTime(CommonMethod.convertTimestampToDate(group.timestamp).toString());
        holder.setEditMode(isEditMode);
        if (selectGroups.contains(group)) {
            holder.setSelect(true);
        } else {
            holder.setSelect(false);
        }
    }

    @Override
    public int getItemCount() {
        return displayGroups.size();
    }

    public interface ClickListener {
        void onSendMessage(Group group);
        void onViewProfile(Group group);
        void onSelect(ArrayList<Group> groups);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView ivProfileImage;
        public TextView tvGroupName, tvGroupMember, tvCreateTime;
        RadioButton rbSelect;
        public Group group;

        public ViewHolder(View itemView) {
            super(itemView);
            tvGroupName = (TextView) itemView.findViewById(R.id.group_item_name);
            tvGroupMember = (TextView) itemView.findViewById(R.id.group_item_members);
            tvCreateTime = (TextView) itemView.findViewById(R.id.group_item_create_date);
            ivProfileImage = (ImageView) itemView.findViewById(R.id.group_item_profile);
            ivProfileImage.setOnClickListener(this);
            rbSelect = (RadioButton) itemView.findViewById(R.id.group_item_select);
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
                clickListener.onViewProfile(group);
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

        public void setGroupName(String groupName) {
            tvGroupName.setText(groupName);
        }

        public void setGroupMember(String groupMember) {
            tvGroupMember.setText(groupMember);
        }

        public void updateProfileImage() {
//            ivProfileImage.setImages(ServiceManager.getInstance().getProfileImage(group.members));
        }

        public void setCreateTime(String time) {
            tvCreateTime.setText("Created: " + time);
        }

        public void setEditMode(Boolean isEditMode) {
            if (isEditMode) {
                rbSelect.setVisibility(View.VISIBLE);
            } else {
                rbSelect.setVisibility(View.GONE);
            }
        }

        public void setSelect(Boolean isSelect) {
            rbSelect.setChecked(isSelect);
            rbSelect.setSelected(isSelect);
        }
    }
}
