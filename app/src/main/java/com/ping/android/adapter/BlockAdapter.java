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

public class BlockAdapter extends RecyclerView.Adapter<BlockAdapter.ViewHolder> {

    private static ClickListener mClickListener;
    private ArrayList<User> blockContacts;
    private Context mContext;

    public BlockAdapter(ArrayList<User> blockContacts, Context context, ClickListener clickListener) {
        this.blockContacts = blockContacts;
        mContext = context;
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
        User contact = blockContacts.get(position);

        holder.tvName.setText(contact.getDisplayName());
        holder.contact = contact;
        UiUtils.displayProfileImage(mContext, holder.ivProfileImage, contact);
    }

    @Override
    public int getItemCount() {
        return blockContacts.size();
    }

    public interface ClickListener {
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivProfileImage;
        public TextView tvName;
        public User contact;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.item_block_name);
            ivProfileImage = (ImageView) itemView.findViewById(R.id.item_block_profile);
        }
    }
}
