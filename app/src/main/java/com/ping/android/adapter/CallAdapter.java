package com.ping.android.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ping.android.activity.R;
import com.ping.android.model.Call;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.UiUtils;

import java.util.ArrayList;

public class CallAdapter extends RecyclerView.Adapter<CallAdapter.ViewHolder> {

    private ArrayList<Call> originalCalls;
    private ArrayList<Call> displayCalls;
    private User currentUser;
    private Boolean isEditMode = false;
    private Context mContext;
    private ClickListener clickListener;

    public CallAdapter(ArrayList<Call> calls, Context context, ClickListener clickListener) {
        originalCalls = calls;
        displayCalls = (ArrayList<Call>) calls.clone();
        mContext = context;
        this.clickListener = clickListener;
        currentUser = ServiceManager.getInstance().getCurrentUser();
    }

    public void addOrUpdateCall(Call call) {
        Boolean isAdd = true;
        for (int i = 0; i < originalCalls.size(); i++) {
            if (originalCalls.get(i).key.equals(call.key)) {
                isAdd = false;
                break;
            }
        }
        if (isAdd) {
            addCall(call);
        } else {
            updateCall(call);
        }
    }

    public void addCall(Call call) {
        int index = 0;
        for (Call item : originalCalls) {
            if (CommonMethod.compareTimestamp(call.timestamp, item.timestamp))
                index ++;
            else
                break;
        }
        originalCalls.add(index, call);

        index = 0;
        for (Call item : displayCalls) {
            if (CommonMethod.compareTimestamp(call.timestamp, item.timestamp))
                index ++;
            else
                break;
        }
        displayCalls.add(index, call);
        notifyItemInserted(index);
    }

    public void updateCall(Call call) {
        for (int i = 0; i < originalCalls.size(); i++) {
            if (originalCalls.get(i).key.equals(call.key)) {
                originalCalls.set(i, call);
                break;
            }
        }
        for (int i = 0; i < displayCalls.size(); i++) {
            if (displayCalls.get(i).key.equals(call.key)) {
                displayCalls.set(i, call);
                break;
            }
        }
        notifyDataSetChanged();
    }

    public void deleteCall(String callID) {
        Call deletedCall = null;
        for (Call call : originalCalls) {
            if (call.key.equals(callID)) {
                deletedCall = call;
            }
        }
        if (deletedCall != null) {
            originalCalls.remove(deletedCall);
            displayCalls.remove(deletedCall);
            notifyDataSetChanged();
        }
    }

    public void filter(String text, Boolean isAll) {
        displayCalls = new ArrayList<>();
        for (Call call : originalCalls) {
            if (isFiltered(call, text, isAll)) {
                displayCalls.add(call);
            }
        }
        notifyDataSetChanged();
    }

    public void setEditMode(Boolean isEditMode) {
        this.isEditMode = isEditMode;
        notifyDataSetChanged();
    }

    private boolean isFiltered(Call call, String text, Boolean isAll) {
        if (!isAll && call.status.equals(Constant.CALL_STATUS_SUCCESS)) {
            return false;
        }
        if (!CommonMethod.isFiltered(call.opponentUser, text)) {
            return false;
        }
        return true;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_call, parent, false);
        return new CallAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Call call = displayCalls.get(position);
        holder.call = call;
        holder.setName(call.opponentUser.getDisplayName());
        String info = "";
        if (call.status.equals(Constant.CALL_STATUS_SUCCESS)) {
            info = "";
            if(call.senderId.equals(currentUser.key)) {
                info = "Outgoing. ";
            } else {
                info = "Incoming. ";
            }
        } else {
            info = "Missed. ";
        }
        String time = CommonMethod.convertTimestampToTime(call.timestamp).toString();
        holder.setInfo(info + time);
        holder.setInfoColor();
        holder.setEditMode(isEditMode);
        UiUtils.displayProfileImage(mContext, holder.ivProfileImage, call.opponentUser);
    }

    @Override
    public int getItemCount() {
        return displayCalls.size();
    }

    public interface ClickListener {
        void onReCall(Call call, Boolean isVideoCall);

        void onDeleteCall(Call call);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView ivProfileImage;
        ImageView ivVideoCall, ivVoiceCall, ivDeleteCall;
        TextView tvName, tvInfo;
        Call call;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.call_item_name);
            tvInfo = (TextView) itemView.findViewById(R.id.call_item_info);
            ivProfileImage = (ImageView) itemView.findViewById(R.id.call_item_profile);
            ivVideoCall = (ImageView) itemView.findViewById(R.id.item_call_video);
            ivVideoCall.setOnClickListener(this);
            ivVoiceCall = (ImageView) itemView.findViewById(R.id.item_call_voice);
            ivVoiceCall.setOnClickListener(this);
            ivDeleteCall = (ImageView) itemView.findViewById(R.id.item_call_delete);
            ivDeleteCall.setOnClickListener(this);
        }

        public void setName(String name) {
            tvName.setText(name);
        }

        public void setInfo(String info) {
            tvInfo.setText(info);
        }

        public void setInfoColor() {
            if (call.status.equals(Constant.CALL_STATUS_SUCCESS)) {
                tvInfo.setTextColor(mContext.getResources().getColor(R.color.text_color));
            } else {
                tvInfo.setTextColor(mContext.getResources().getColor(R.color.red));
            }
        }

        public void setEditMode(Boolean isEditMode) {
            if (isEditMode) {
                ivDeleteCall.setVisibility(View.VISIBLE);
                ivVideoCall.setVisibility(View.GONE);
                ivVoiceCall.setVisibility(View.GONE);
            } else {
                ivDeleteCall.setVisibility(View.GONE);
                ivVideoCall.setVisibility(View.VISIBLE);
                ivVoiceCall.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.item_call_video:
                    clickListener.onReCall(call, true);
                    break;
                case R.id.item_call_voice:
                    clickListener.onReCall(call, false);
                    break;
                case R.id.item_call_delete:
                    clickListener.onDeleteCall(call);
                    break;
            }
        }
    }
}
