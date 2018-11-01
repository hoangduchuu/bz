package com.ping.android.presentation.view.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ping.android.R;
import com.ping.android.model.Transphabet;
import com.ping.android.presentation.view.custom.SettingItem;

import java.util.List;

public class TransphabetCategoryAdapter extends RecyclerView.Adapter<TransphabetCategoryAdapter.ViewHolder> {
    List<Transphabet> data;
    private OnClickListener listener;

    public TransphabetCategoryAdapter(List<Transphabet> data) {
        this.data = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_language_transphabet, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Transphabet transphabet = data.get(position);
        holder.bindData(transphabet);
        holder.itemView.setOnClickListener(view -> {
            if (listener != null) {
                listener.onClick(transphabet);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setItemClickListener(OnClickListener listener) {
        this.listener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        SettingItem settingItem;
        Transphabet transphabet;

        public ViewHolder(View itemView) {
            super(itemView);
            settingItem = itemView.findViewById(R.id.item);
        }

        void bindData(Transphabet transphabet) {
            this.transphabet = transphabet;
            settingItem.setTitle(transphabet.name);
        }
    }

    public interface OnClickListener {
        void onClick(Transphabet transphabet);
    }
}
