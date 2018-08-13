package com.ping.android.presentation.view.adapter;

import android.content.res.ColorStateList;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ping.android.R;
import com.ping.android.model.enums.Color;

import java.util.List;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ViewHolder> {
    private List<Color> colors;
    private ColorListener listener;

    public ColorAdapter(List<Color> colors) {
        this.colors = colors;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_color, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Color color = colors.get(position);
        holder.applyColor(color);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(color);
            }
        });
    }

    @Override
    public int getItemCount() {
        return colors.size();
    }

    public void setListener(ColorListener listener) {
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }

        public void applyColor(Color color) {
            itemView.setBackgroundTintList(ColorStateList
                    .valueOf(ContextCompat.getColor(itemView.getContext(), color.getColor())));
        }
    }

    public interface ColorListener {
        void onClick(Color color);
    }
}
