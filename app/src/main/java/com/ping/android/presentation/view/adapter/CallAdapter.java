package com.ping.android.presentation.view.adapter;

import android.support.transition.TransitionManager;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ping.android.activity.R;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Call;
import com.ping.android.model.User;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.UiUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CallAdapter extends RecyclerView.Adapter<CallAdapter.ViewHolder> {
    private static final String TAG = CallAdapter.class.getSimpleName();
    private ArrayList<Call> originalCalls;
    private ArrayList<Call> displayCalls;
    private ArrayList<Call> selectCalls;
    private User currentUser;
    private Boolean isEditMode = false;
    private ClickListener clickListener;
    private RecyclerView recyclerView;
    private Set<ViewHolder> boundsViewHolder = new HashSet<>();

    public CallAdapter(ArrayList<Call> calls, ClickListener clickListener) {
        originalCalls = calls;
        displayCalls = (ArrayList<Call>) calls.clone();
        selectCalls = new ArrayList<>();
        this.clickListener = clickListener;
        currentUser = UserManager.getInstance().getUser();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerView = null;
    }

    public void addCall(Call call) {
        int index = 0;
        for (Call item : originalCalls) {
            if (CommonMethod.compareTimestamp(call.timestamp, item.timestamp))
                index++;
            else
                break;
        }
        originalCalls.add(index, call);

        index = 0;
        for (Call item : displayCalls) {
            if (CommonMethod.compareTimestamp(call.timestamp, item.timestamp))
                index++;
            else
                break;
        }
        displayCalls.add(index, call);
        notifyItemInserted(index);
    }

    public void updateCall(Call call) {
        Log.d(TAG, "update Call");
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
            int index = displayCalls.indexOf(deletedCall);
            originalCalls.remove(deletedCall);
            displayCalls.remove(deletedCall);
            selectCalls.remove(deletedCall);
            if (index >= 0) {
                ViewHolder viewHolder = (ViewHolder) recyclerView.findViewHolderForAdapterPosition(index);
                boundsViewHolder.remove(viewHolder);
                notifyItemRemoved(index);
            }
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
        if (this.isEditMode != isEditMode) {
            this.isEditMode = isEditMode;
            if (!isEditMode) {
                selectCalls.clear();
            }
            toggleEditMode();
        }
    }

    public ArrayList<Call> getSelectCall() {
        return selectCalls;
    }

    public void cleanSelectCall() {
        selectCalls.clear();
    }

    private void toggleEditMode() {
        this.recyclerView.postDelayed(() -> {
            TransitionManager.endTransitions(recyclerView);
            TransitionManager.beginDelayedTransition(recyclerView);
            for (ViewHolder holder : boundsViewHolder) {
                holder.setEditMode(isEditMode);
            }
        }, 10);
    }

    private boolean isFiltered(Call call, String text, Boolean isAll) {
        if (!isAll && call.status == Constant.CALL_STATUS_SUCCESS) {
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
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        boundsViewHolder.remove(holder);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        boundsViewHolder.add(holder);
        Call call = displayCalls.get(position);
        holder.bindData(call);
        holder.ivProfileImage.setOnClickListener(view -> {
            Pair imagePair = Pair.create(holder.ivProfileImage, "imageProfile" + position);
            Pair namePair = Pair.create(holder.tvName, "contactName" + position);
            clickListener.onViewProfile(call.opponentUser, imagePair, namePair);
        });
    }

    @Override
    public int getItemCount() {
        return displayCalls.size();
    }

    public interface ClickListener {
        void onReCall(Call call, Boolean isVideoCall);

        void onDeleteCall(Call call);

        void onSelect(ArrayList<Call> selectCalls);

        void onViewProfile(User user, Pair<View, String>... sharedElements);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView ivProfileImage;
        ImageView ivVideoCall, ivVoiceCall;
        TextView tvName, tvInfo;
        RadioButton rbSelect;
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
            rbSelect = (RadioButton) itemView.findViewById(R.id.call_item_select);
            rbSelect.setOnClickListener(this);
            itemView.setOnClickListener(this);
        }

        public void setInfoColor() {
            if (call.status == Constant.CALL_STATUS_SUCCESS) {
                tvInfo.setTextColor(itemView.getContext().getResources().getColor(R.color.text_color));
            } else {
                tvInfo.setTextColor(itemView.getContext().getResources().getColor(R.color.red));
            }
        }

        public void setEditMode(boolean isEditMode) {
            if (isEditMode) {
                rbSelect.setVisibility(View.VISIBLE);
                ivVideoCall.setVisibility(View.GONE);
                ivVoiceCall.setVisibility(View.GONE);
            } else {
                ivVideoCall.setVisibility(View.VISIBLE);
                ivVoiceCall.setVisibility(View.VISIBLE);
                rbSelect.setVisibility(View.GONE);
            }
        }

        public void setSelect(Boolean isSelect) {
            rbSelect.setChecked(isSelect);
            rbSelect.setSelected(isSelect);
        }

        private void onClickEditMode(View view) {
            boolean isSelect;
            switch (view.getId()) {
                case R.id.call_item_select:
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
            selectCall();
        }

        private void selectCall() {
            if (rbSelect.isSelected()) {
                selectCalls.add(call);
            } else {
                selectCalls.remove(call);
            }
            clickListener.onSelect(selectCalls);
        }

        @Override
        public void onClick(View view) {
            if (isEditMode) {
                onClickEditMode(view);
                return;
            }

            switch (view.getId()) {
                case R.id.item_call_video:
                    clickListener.onReCall(call, true);
                    break;
                case R.id.item_call_voice:
                    clickListener.onReCall(call, false);
                    break;
            }
        }

        public void bindData(Call call) {
            this.call = call;
            String info;
            if (call.status == Constant.CALL_STATUS_SUCCESS) {
                if (call.senderId.equals(currentUser.key)) {
                    info = "Outgoing. ";
                } else {
                    info = "Incoming. ";
                }
            } else {
                info = "Missed. ";
            }

            String time = CommonMethod.convertTimestampToTime(call.timestamp).toString();
            tvInfo.setText(info + time);
            tvName.setText(call.opponentUser.getDisplayName());
            setInfoColor();
            setEditMode(isEditMode);
            setSelect(selectCalls.contains(call));
            UiUtils.displayProfileImage(itemView.getContext(), ivProfileImage, call.opponentUser);
            ivProfileImage.setTransitionName("imageProfile" + getAdapterPosition());
            tvName.setTransitionName("contactName" + getAdapterPosition());
        }
    }
}
