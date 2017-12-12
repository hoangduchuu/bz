package com.ping.android.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.transition.TransitionManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ping.android.activity.ChatActivity;
import com.ping.android.activity.GroupProfileActivity;
import com.ping.android.activity.R;
import com.ping.android.activity.UserDetailActivity;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Conversation;
import com.ping.android.model.Group;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.UiUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private ArrayList<Conversation> originalConversations;
    private ArrayList<Conversation> displayConversations;
    private ArrayList<Conversation> selectConversations;
    private User currentUser;
    private Boolean isEditMode = false;
    private Activity activity;
    private ClickListener clickListener;
    private RecyclerView recyclerView;
    private Set<MessageViewHolder> boundsViewHolder = new HashSet<>();

    public MessageAdapter(ArrayList<Conversation> conversations, Activity activity, ClickListener clickListener) {
        this.originalConversations = conversations;
        this.displayConversations = (ArrayList<Conversation>) conversations.clone();
        selectConversations = new ArrayList<>();
        this.activity = activity;
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

    @Override
    public void onViewRecycled(MessageViewHolder holder) {
        super.onViewRecycled(holder);
        boundsViewHolder.remove(holder);
    }

    public void addOrUpdateConversation(Conversation conversation) {
        boolean isAdd = true;
        for (int i = 0; i < originalConversations.size(); i++) {
            if (originalConversations.get(i).key.equals(conversation.key)) {
                isAdd = false;
                break;
            }
        }
        if (isAdd) {
            addConversation(conversation);
        } else {
            updateConversation(conversation);
        }
    }

    private void addConversation(Conversation conversation) {
        int index = 0;
        for (Conversation item : originalConversations) {
            if (CommonMethod.compareTimestamp(conversation.timesstamps, item.timesstamps))
                index ++;
            else
                break;
        }

        originalConversations.add(index, conversation);
        displayConversations.add(index, conversation);
        notifyItemInserted(index);
    }

    private void updateConversation(Conversation conversation) {
        for (int i = 0; i < originalConversations.size(); i++) {
            if (originalConversations.get(i).key.equals(conversation.key)) {
                originalConversations.remove(i);
                break;
            }
        }
        for (int i = 0; i < displayConversations.size(); i++) {
            if (displayConversations.get(i).key.equals(conversation.key)) {
                displayConversations.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
        addConversation(conversation);
    }

    public void deleteConversation(String conversationID) {
        Conversation deletedConversation = null;
        int index = -1;
        for (Conversation conversation : originalConversations) {
            if (conversation.key.equals(conversationID)) {
                deletedConversation = conversation;
            }
        }
        if (deletedConversation != null) {
            index = displayConversations.indexOf(deletedConversation);
            originalConversations.remove(deletedConversation);
            displayConversations.remove(deletedConversation);
            selectConversations.remove(deletedConversation);

            if (index >= 0) {
                MessageViewHolder viewHolder = (MessageViewHolder) recyclerView.findViewHolderForAdapterPosition(index);
                boundsViewHolder.remove(viewHolder);
                notifyItemRemoved(index);
            }
        }
    }

    public void filter(String text) {
        displayConversations = new ArrayList<>();
        for (Conversation conversation : originalConversations) {
            if (isFiltered(conversation, text)) {
                displayConversations.add(conversation);
            }
        }
        notifyDataSetChanged();
    }

    public void setEditMode(boolean isEditMode) {
        if (this.isEditMode != isEditMode) {
            this.isEditMode = isEditMode;
            if (!isEditMode) {
                selectConversations.clear();
            }
            toggleEditMode();
        }
    }

    public ArrayList<Conversation> getSelectConversation() {
        return selectConversations;
    }

    public void cleanSelectConversation() {
        selectConversations.clear();
    }

    private boolean isFiltered(Conversation conversation, String text) {
        for (User user : conversation.members) {
            if (!user.key.equals(currentUser.key)) {
                if (CommonMethod.isFiltered(user, text)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void toggleEditMode() {
        this.recyclerView.postDelayed(() -> {
            TransitionManager.endTransitions(recyclerView);
            TransitionManager.beginDelayedTransition(recyclerView);
            for (MessageViewHolder holder : boundsViewHolder) {
                holder.setEditMode(isEditMode);
            }
        }, 10);
    }

    @Override
    public MessageAdapter.MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_text, parent, false);
        return new MessageAdapter.MessageViewHolder(view);
//        if (viewType == Constant.MSG_TYPE_TEXT.intValue()){
//            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_text,parent,false);
//            return new MessageAdapter.MessageViewHolder(view);
//        }else if (viewType == Constant.MSG_TYPE_IMAGE.intValue()){
//            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_img,parent,false);
//            return new MessageAdapter.MessageViewHolder(view);
//        }else if (viewType == Constant.MSG_TYPE_VOICE.intValue()){
//            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_voice,parent,false);
//            return new MessageAdapter.MessageViewHolder(view);
//        }else {
//            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_game,parent,false);
//            return new MessageAdapter.MessageViewHolder(view);
//        }
    }

    @Override
    public int getItemViewType(int position) {
        Conversation model = displayConversations.get(position);
        return model.messageType;
    }

    @Override
    public void onBindViewHolder(MessageAdapter.MessageViewHolder holder, int position) {
        boundsViewHolder.add(holder);
        Conversation model = displayConversations.get(position);
        holder.bindData(model);
    }

    @Override
    public int getItemCount() {
        return displayConversations.size();
    }

    public void updateData(ArrayList<Conversation> conversations) {
        this.originalConversations = conversations;
        this.displayConversations = (ArrayList<Conversation>) conversations.clone();
        notifyDataSetChanged();
    }

    public interface ClickListener {
        void onSelect(ArrayList<Conversation> selectConversations);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView ivProfileImage;
        LinearLayout messageItem;
        TextView tvSender, tvMessage, tvTime;
        RadioButton rbSelect;
        Conversation conversation;

        public MessageViewHolder(View itemView) {
            super(itemView);
            messageItem = (LinearLayout) itemView;
            ivProfileImage = (ImageView) itemView.findViewById(R.id.message_item_profile);
            tvSender = (TextView) itemView.findViewById(R.id.message_item_sender);
            tvMessage = (TextView) itemView.findViewById(R.id.message_item_message);
            tvTime = (TextView) itemView.findViewById(R.id.message_item_time);
            rbSelect = (RadioButton) itemView.findViewById(R.id.message_item_select);
            rbSelect.setOnClickListener(this);
            itemView.setOnClickListener(this);
        }

        public void setReadStatus(Boolean readStatus) {
            if (readStatus) {
                tvSender.setTypeface(Typeface.DEFAULT);
                tvMessage.setTypeface(Typeface.DEFAULT);
                tvTime.setTypeface(Typeface.DEFAULT);
            } else {
                tvSender.setTypeface(Typeface.DEFAULT_BOLD);
                tvMessage.setTypeface(Typeface.DEFAULT_BOLD);
                tvTime.setTypeface(Typeface.DEFAULT_BOLD);
            }
        }

        public void setEditMode(Boolean isEditMode) {
            if (isEditMode) {
                rbSelect.setVisibility(View.VISIBLE);
            } else {
                rbSelect.setVisibility(View.GONE);
            }
        }

        public void setSelect(Boolean isSelect) {
            rbSelect.setChecked(isSelect);
            rbSelect.setSelected(isSelect);
        }

        @Override
        public void onClick(View view) {
            if (isEditMode) {
                onClickEditMode(view);
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putParcelable("CONVERSATION", conversation);
            Intent intent = new Intent(activity, ChatActivity.class);
            intent.putExtra(ChatActivity.CONVERSATION_ID, conversation.key);
            intent.putExtras(bundle);
            activity.startActivity(intent);
        }

        private void onClickEditMode(View view) {
            boolean isSelect;
            switch (view.getId()) {
                case R.id.message_item_select:
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
            selectConversation();
        }

        private void selectConversation() {
            if (rbSelect.isSelected()) {
                selectConversations.add(conversation);
            } else {
                selectConversations.remove(conversation);
            }
            clickListener.onSelect(selectConversations);
        }

        public void bindData(Conversation model) {
            this.conversation = model;
            String conversationName = "";
            if (model.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
                conversationName = model.opponentUser.getDisplayName();
            } else {
                conversationName = model.group.groupName;
            }
            this.tvSender.setText(conversationName);
            this.tvTime.setText(CommonMethod.convertTimestampToTime(model.timesstamps));
            String message = "";
            if (model.messageType == Constant.MSG_TYPE_TEXT) {
                if (ServiceManager.getInstance().getCurrentMarkStatus(model.markStatuses, model.maskMessages)) {
                    message = ServiceManager.getInstance().encodeMessage(activity, model.message);
                } else {
                    message = model.message;
                }
            } else if (model.messageType == Constant.MSG_TYPE_IMAGE) {
                message = "[Picture]";
            } else if (model.messageType == Constant.MSG_TYPE_VOICE) {
                message = "[Voice]";
            } else if (model.messageType == Constant.MSG_TYPE_GAME) {
                message = "[Game]";
            }
            this.tvMessage.setText(message);
            this.setReadStatus(ServiceManager.getInstance().getCurrentReadStatus(model.readStatuses));
            this.setEditMode(isEditMode);
            this.setSelect(selectConversations.contains(model));

            if (model.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
                UiUtils.displayProfileImage(activity, ivProfileImage, model.opponentUser);
                ivProfileImage.setOnClickListener(v -> {
                    Intent intent = new Intent(activity, UserDetailActivity.class);
                    intent.putExtra(Constant.START_ACTIVITY_USER_ID, model.opponentUser.key);
                    activity.startActivity(intent);
                });
            } else {
                ivProfileImage.setOnClickListener(v -> {
                    Intent intent = new Intent(activity, GroupProfileActivity.class);
                    intent.putExtra(Constant.START_ACTIVITY_GROUP_ID, model.groupID);
                    activity.startActivity(intent);
                });
                UiUtils.displayProfileAvatar(ivProfileImage, model.group.groupAvatar);
            }
        }
    }
}
