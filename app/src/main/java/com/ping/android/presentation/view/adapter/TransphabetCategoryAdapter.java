package com.ping.android.presentation.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ping.android.R;
import com.ping.android.model.Transphabet;

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
        View divider;
        TextView title;

        Transphabet transphabet;

        public ViewHolder(View itemView) {
            super(itemView);
            divider = itemView.findViewById(R.id.divider);
            title = itemView.findViewById(R.id.tv_title);
        }

        void bindData(Transphabet transphabet) {
            this.transphabet = transphabet;
            title.setText(transphabet.name);
        }
    }

    public interface OnClickListener {
        void onClick(Transphabet transphabet);
    }
}
