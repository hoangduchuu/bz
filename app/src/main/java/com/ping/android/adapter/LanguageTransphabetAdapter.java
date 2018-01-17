package com.ping.android.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ping.android.activity.R;
import com.ping.android.fragment.transphabet.SelectiveLanguagesFragment;
import com.ping.android.model.Language;

import java.util.List;

public class LanguageTransphabetAdapter extends RecyclerView.Adapter<LanguageTransphabetAdapter.ViewHolder> {
    List<Language> data;
    private OnClickListener listener;

    public LanguageTransphabetAdapter(List<Language> data) {
        this.data = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_language_transphabet, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Language language = data.get(position);
        holder.bindData(language);
        holder.itemView.setOnClickListener(view -> {
            if (listener != null) {
                listener.onClick(language);
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

        Language language;

        public ViewHolder(View itemView) {
            super(itemView);
            divider = itemView.findViewById(R.id.divider);
            title = itemView.findViewById(R.id.tv_title);
        }

        void bindData(Language language) {
            this.language = language;
            title.setText(language.name);
        }
    }

    public interface OnClickListener {
        void onClick(Language language);
    }
}
