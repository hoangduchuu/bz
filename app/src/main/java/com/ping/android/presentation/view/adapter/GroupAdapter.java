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

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.bzzzchat.configuration.GlideRequests;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ping.android.R;
import com.ping.android.model.Group;
import com.ping.android.model.User;
import com.ping.android.utils.CommonMethod;
import com.ping.android.utils.DateUtils;
import com.ping.android.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {

    private ArrayList<Group> originalGroups;
    private ArrayList<Group> displayGroups;
    private ArrayList<Group> selectGroups;
    private boolean isEditMode = false;
    private GroupListener groupListener;
    private RequestManager glide;

    public GroupAdapter(RequestManager glide, GroupListener groupListener) {
        this.glide = glide;
        originalGroups = new ArrayList<>();
        displayGroups = new ArrayList<>();
        selectGroups = new ArrayList<>();
        this.groupListener = groupListener;
    }

    public void addOrUpdateConversation(Group group) {
        boolean isAdd = true;
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
        return new GroupAdapter.ViewHolder(view, glide, groupListener);
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

    public interface GroupListener {
        void onSendMessage(Group group);
        void onViewProfile(Group group, Pair<View, String>... sharedElements);
        void onSelect(ArrayList<Group> groups);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final RequestManager glide;
        ImageView ivProfileImage;
        TextView tvGroupName, tvGroupMember, tvCreateTime;
        public Group group;
        private GroupListener groupListener;

        public ViewHolder(View itemView, RequestManager glide, GroupListener groupListener) {
            super(itemView);
            this.glide = glide;
            this.groupListener = groupListener;
            tvGroupName = itemView.findViewById(R.id.group_item_name);
            tvGroupMember = itemView.findViewById(R.id.group_item_members);
            tvCreateTime = itemView.findViewById(R.id.group_item_create_date);
            ivProfileImage = itemView.findViewById(R.id.group_item_profile);
            ivProfileImage.setOnClickListener(this);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.group_item_profile) {
                Pair imagePair = Pair.create(ivProfileImage, "imageProfile" + getAdapterPosition());
                groupListener.onViewProfile(group, imagePair);
            }
            else {
                groupListener.onSendMessage(group);
            }
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
            String time = "Created: " + DateUtils.convertTimestampToDate(group.timestamp);
            tvCreateTime.setText(time);
            if (TextUtils.isEmpty(group.groupAvatar)) {
                ivProfileImage.setImageResource(R.drawable.ic_avatar_gray);
                return;
            }
            StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(group.groupAvatar);

            ((GlideRequests)glide)
                    .load(gsReference)
                    .profileImage()
                    .into(ivProfileImage);
        }
    }
}
