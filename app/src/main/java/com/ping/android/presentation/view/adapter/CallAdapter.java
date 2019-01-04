package com.ping.android.presentation.view.adapter;

import androidx.transition.TransitionManager;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ping.android.R;
import com.ping.android.model.Call;
import com.ping.android.model.Conversation;
import com.ping.android.model.User;
import com.ping.android.utils.CommonMethod;
import com.ping.android.utils.DateUtils;
import com.ping.android.utils.UiUtils;
import com.ping.android.utils.configs.Constant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CallAdapter extends RecyclerView.Adapter<CallAdapter.ViewHolder> {
    private static final String TAG = CallAdapter.class.getSimpleName();
    private ArrayList<Call> originalCalls;
    private ArrayList<Call> displayCalls;
    private ArrayList<Call> selectCalls;
    private Boolean isEditMode = false;
    private ClickListener clickListener;
    private RecyclerView recyclerView;
    private Set<ViewHolder> boundsViewHolder = new HashSet<>();

    public CallAdapter(ArrayList<Call> calls, ClickListener clickListener) {
        originalCalls = calls;
        displayCalls = (ArrayList<Call>) calls.clone();
        selectCalls = new ArrayList<>();
        this.clickListener = clickListener;
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

    public void addOrUpdateCall(Call call) {
        boolean isAdd = true;
        for (int i = 0, size = originalCalls.size(); i < size; i++) {
            Call item = originalCalls.get(i);
            if (item.key.equals(call.key)) {
                originalCalls.set(i, call);
                isAdd = false;
            }
        }
        if (!isAdd) {
//            for (int i = 0, size = displayCalls.size(); i < size; i++) {
//                Call item = displayCalls.get(i);
//                if (item.key.equals(call.key)) {
//                    displayCalls.set(i, call);
//                    notifyItemChanged(i);
//                    break;
//                }
//            }
            return;
        }
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
        if (!isAll && (call.type == Call.CallType.OUTGOING || call.type == Call.CallType.INCOMING)) {
            return false;
        }
        return CommonMethod.isFiltered(call.opponentUser, text);
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

    public void updateNickNames(String conversationId, String nickname) {
        for (Call call : originalCalls) {
            if (conversationId.equals(call.conversationId)) {
                call.opponentName = nickname;
            }
        }
        for (Call call : displayCalls) {
            if (conversationId.equals(call.conversationId)) {
                call.opponentName = nickname;
            }
        }
        notifyDataSetChanged();
    }

    public void updateData(List<Call> callList) {
        this.originalCalls.addAll(callList);
        this.displayCalls.addAll(callList);
        notifyDataSetChanged();
    }

    public void appendCalls(List<Call> calls) {
        Collections.sort(calls, (o1, o2) -> Double.compare(o2.timestamp, o1.timestamp));
        int size = displayCalls.size();
        this.originalCalls.addAll(calls);
        this.displayCalls.addAll(calls);
        notifyItemRangeInserted(size, calls.size());
    }

    public void updateConversationAvatar(Conversation conversation) {
        for (Call call : originalCalls) {
            if (conversation.key.equals(call.conversationId)) {
                call.opponentUser.profile = conversation.conversationAvatarUrl;
            }
        }
        for (Call call : displayCalls) {
            if (conversation.key.equals(call.conversationId)) {
                call.opponentUser.profile = conversation.conversationAvatarUrl;
            }
        }
        notifyDataSetChanged();
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
            tvName = itemView.findViewById(R.id.call_item_name);
            tvInfo = itemView.findViewById(R.id.call_item_info);
            ivProfileImage = itemView.findViewById(R.id.call_item_profile);
            ivVideoCall = itemView.findViewById(R.id.item_call_video);
            ivVideoCall.setOnClickListener(this);
            ivVoiceCall = itemView.findViewById(R.id.item_call_voice);
            ivVoiceCall.setOnClickListener(this);
            rbSelect = itemView.findViewById(R.id.call_item_select);
            rbSelect.setOnClickListener(this);
            itemView.setOnClickListener(this);
        }

        public void setInfoColor(int status) {
            if (status == Constant.CALL_STATUS_SUCCESS) {
                tvInfo.setTextColor(itemView.getContext().getResources().getColor(R.color.text_color_grey));
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
            if (call.type == Call.CallType.OUTGOING) {
                info = "Outgoing. ";
                setInfoColor(Constant.CALL_STATUS_SUCCESS);
            } else if (call.type == Call.CallType.INCOMING) {
                info = "Incoming. ";
                setInfoColor(Constant.CALL_STATUS_SUCCESS);
            } else {
                info = "Missed. ";
                setInfoColor(Constant.CALL_STATUS_MISS);
            }

            String time = info + DateUtils.convertTimestampToDate2(call.timestamp);
            tvInfo.setText(time);
            tvName.setText(call.opponentName);
            setEditMode(isEditMode);
            setSelect(selectCalls.contains(call));
            UiUtils.displayProfileImage(itemView.getContext(), ivProfileImage, call.opponentUser);
            ivProfileImage.setTransitionName("imageProfile" + getAdapterPosition());
            tvName.setTransitionName("contactName" + getAdapterPosition());
        }
    }
}
