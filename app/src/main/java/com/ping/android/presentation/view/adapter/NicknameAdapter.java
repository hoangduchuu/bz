package com.ping.android.presentation.view.adapter;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ping.android.activity.R;
import com.ping.android.model.Nickname;
import com.ping.android.utils.UiUtils;

import java.util.List;

public class NicknameAdapter extends RecyclerView.Adapter<NicknameAdapter.ViewHolder> {
    private List<Nickname> data;
    private NickNameListener listener;

    public NicknameAdapter(List<Nickname> nicknames) {
        this.data = nicknames;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nickname, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Nickname nickname = data.get(position);
        holder.bindData(nickname);
        holder.itemView.setOnClickListener(view -> {
            if (listener != null) {
                listener.onClick(nickname);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setListener(NickNameListener listener) {
        this.listener = listener;
    }

    public void updateNickName(Nickname nickname) {
        int index = data.indexOf(nickname);
        if (index != RecyclerView.NO_POSITION) {
            data.set(index, nickname);
            notifyItemChanged(index);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView nickName;
        private TextView displayName;
        private ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            displayName = itemView.findViewById(R.id.tv_name);
            nickName = itemView.findViewById(R.id.tv_nickname);
            imageView = itemView.findViewById(R.id.iv_profile);
        }

        public void bindData(Nickname nickname) {
            displayName.setText(nickname.displayName);
            if (!TextUtils.isEmpty(nickname.imageUrl)) {
                UiUtils.displayProfileAvatar(imageView, nickname.imageUrl);
            }
            if (TextUtils.isEmpty(nickname.nickName)) {
                this.nickName.setText("Set Nickname");
                this.nickName.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_color_grey));
            } else {
                this.nickName.setText(nickname.nickName);
                this.nickName.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.black));
            }
        }
    }

    public interface NickNameListener {
        void onClick(Nickname nickname);
    }
}
