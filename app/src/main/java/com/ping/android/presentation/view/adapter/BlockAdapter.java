package com.ping.android.presentation.view.adapter;

import android.content.Context;
import android.support.transition.TransitionManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ping.android.activity.R;
import com.ping.android.model.User;
import com.ping.android.utils.UiUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlockAdapter extends RecyclerView.Adapter<BlockAdapter.ViewHolder> {

    private static ClickListener mClickListener;
    private ArrayList<User> blockContacts;
    private ArrayList<User> selectedContacts;
    private Context mContext;
    private boolean isEditMode = false;
    private RecyclerView recyclerView;
    private Set<ViewHolder> boundsViewHolder = new HashSet<>();

    public BlockAdapter(ArrayList<User> blockContacts, Context context, ClickListener clickListener) {
        this.blockContacts = blockContacts;
        mContext = context;
        selectedContacts = new ArrayList<>();
        mClickListener = clickListener;
    }

    public void addContact(User contact) {
        blockContacts.add(contact);
        notifyItemInserted(blockContacts.size() - 1);
    }

    public void updateContact(User contact) {
        for (int i = 0; i < blockContacts.size(); i++) {
            if (blockContacts.get(i).key.equals(contact.key)) {
                blockContacts.set(i, contact);
                break;
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_block, parent, false);
        return new BlockAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        boundsViewHolder.add(holder);

        User contact = blockContacts.get(position);
        holder.tvName.setText(contact.getDisplayName());
        holder.contact = contact;
        holder.rbSelect.setChecked(selectedContacts.contains(contact));
        holder.rbSelect.setOnClickListener(view -> {
            boolean isSelected = selectedContacts.contains(holder.contact);
            if (isSelected) {
                selectedContacts.remove(holder.contact);
            } else {
                selectedContacts.add(holder.contact);
            }
            holder.rbSelect.setChecked(!isSelected);
        });
        holder.updateEditMode(isEditMode);
        UiUtils.displayProfileImage(mContext, holder.ivProfileImage, contact);
    }

    @Override
    public int getItemCount() {
        return blockContacts.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = null;
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        boundsViewHolder.remove(holder);
    }

    public List<User> getSelectedContact() {
        return selectedContacts;
    }

    public void toggleEditMode() {
        this.isEditMode = !isEditMode;
        if (!isEditMode) {
            selectedContacts.clear();
        }
        this.recyclerView.postDelayed(() -> {
            TransitionManager.endTransitions(recyclerView);
            TransitionManager.beginDelayedTransition(recyclerView);
            for (ViewHolder holder : boundsViewHolder) {
                holder.updateEditMode(isEditMode);
            }
        }, 10);
    }

    public boolean isInEditMode() {
        return this.isEditMode;
    }

    public void removeContact(String blockID) {
        for (int i = 0; i < blockContacts.size(); i++) {
            if (blockContacts.get(i).key.equals(blockID)) {
                blockContacts.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public interface ClickListener {
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivProfileImage;
        public TextView tvName;
        public User contact;
        public RadioButton rbSelect;
        private boolean isEditMode = false;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.item_block_name);
            ivProfileImage = itemView.findViewById(R.id.item_block_profile);
            rbSelect = itemView.findViewById(R.id.rb_select);
        }

        public void updateEditMode(boolean isEditMode) {
            this.isEditMode = isEditMode;
            rbSelect.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        }
    }
}
