package com.ping.android.presentation.view.adapter;

import android.graphics.Typeface;
import android.support.transition.TransitionManager;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ping.android.activity.R;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Conversation;
import com.ping.android.model.Group;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.DateUtils;
import com.ping.android.utils.UiUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static boolean isEditMode = false;
    private ArrayList<Conversation> originalConversations;
    private ArrayList<Conversation> displayConversations;
    private ArrayList<Conversation> selectConversations;
    private RecyclerView recyclerView;
    private Set<MessageViewHolder> boundsViewHolder = new HashSet<>();

    private ConversationItemListener listener;

    public MessageAdapter(ArrayList<Conversation> conversations) {
        isEditMode = false;
        this.originalConversations = conversations;
        this.displayConversations = (ArrayList<Conversation>) conversations.clone();
        selectConversations = new ArrayList<>();
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

    @Override
    public MessageAdapter.MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_text, parent, false);
        return new MessageAdapter.MessageViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(MessageAdapter.MessageViewHolder holder, int position) {
        boundsViewHolder.add(holder);
        Conversation model = displayConversations.get(position);
        holder.bindData(model, selectConversations.contains(model));
        holder.setClickListener(conversation -> {
            boolean status = selectConversations.contains(conversation);
            if (status) {
                selectConversations.remove(conversation);
                holder.rbSelect.setChecked(false);
            } else {
                selectConversations.add(conversation);
                holder.rbSelect.setChecked(true);
            }
            if (listener != null) {
                listener.onSelect(holder.conversation);
            }
        });
    }

    @Override
    public int getItemCount() {
        return displayConversations.size();
    }

    public int unreadNum() {
        int unread = 0;
        for (Conversation conversation : originalConversations) {
            if (!conversation.isRead) {
                unread++;
            }
        }
        return unread;
    }

    public void updateConversation(Conversation conversation, boolean ignoreWhenDuplicate) {
        boolean isAdd = true;
        int index = -1;
        int previousIndex = -1;
        for (int i = 0; i < originalConversations.size(); i++) {
            if (originalConversations.get(i).key.equals(conversation.key)) {
                originalConversations.remove(i);
                break;
            }
        }
        for (int i = 0; i < displayConversations.size(); i++) {
            Conversation item = displayConversations.get(i);
            if (item.key.equals(conversation.key)) {
                if (ignoreWhenDuplicate) {
                    return;
                }
                isAdd = false;
                previousIndex = i;
                if (index == -1) {
                    index = i;
                }
            }
            if (conversation.timesstamps >= item.timesstamps) {
                if (index == -1) {
                    index = i;
                }
            }
        }
        if (index == -1) {
            index = displayConversations.size();
        }
        if (index >= 0) {
            if (isAdd) {
                displayConversations.add(index, conversation);
                notifyItemInserted(index);
            } else {
                if (previousIndex == index) {
                    displayConversations.set(index, conversation);
                    notifyItemChanged(index);
                } else if (previousIndex > index) {
                    displayConversations.remove(previousIndex);
                    displayConversations.add(index, conversation);
                    notifyItemMoved(previousIndex, index);
                } else {
                    displayConversations.add(index, conversation);
                    displayConversations.remove(previousIndex);
                    notifyItemMoved(previousIndex, index);
                }
            }
        }
        originalConversations.add(conversation);
        Collections.sort(originalConversations, (o1, o2) -> Double.compare(o2.timesstamps, o1.timesstamps));
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
                notifyItemRemoved(index);
//                MessageViewHolder viewHolder = (MessageViewHolder) recyclerView.findViewHolderForAdapterPosition(index);
//                boundsViewHolder.remove(viewHolder);
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

    public void setEditMode(boolean isOn) {
        if (isEditMode != isOn) {
            isEditMode = isOn;
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
        return conversation.filterText.contains(text);
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

    public void setListener(ConversationItemListener listener) {
        this.listener = listener;
    }

    public void updateGroupConversation(Group group) {
        Conversation updateConversation = null;
        int index = -1;
        for (int i = 0, size = originalConversations.size(); i < size; i++) {
            if (originalConversations.get(i).key.equals(group.conversationID)) {
                updateConversation = originalConversations.get(i);
                index = i;
                break;
            }
        }
        if (updateConversation != null) {
            int displayIndex = displayConversations.indexOf(updateConversation);
            // Update data for original list
            updateConversation.group = group;
            originalConversations.set(index, updateConversation);
            if (displayIndex >= 0 && displayIndex < displayConversations.size()) {
                displayConversations.set(displayIndex, updateConversation);
                notifyItemChanged(displayIndex);
            }
        }
    }

    public void updateData(List<Conversation> conversations) {
//        Collections.sort(callList, (o1, o2) -> Double.compare(o2.timesstamps, o1.timesstamps));
        this.originalConversations = new ArrayList<>(conversations);
        this.displayConversations = new ArrayList<>(conversations);
        notifyDataSetChanged();
    }

    public void appendConversations(List<Conversation> conversations) {
        Collections.sort(conversations, (o1, o2) -> Double.compare(o2.timesstamps, o1.timesstamps));
        int size = displayConversations.size();
        this.originalConversations.addAll(conversations);
        this.displayConversations.addAll(conversations);
        notifyItemRangeInserted(size, conversations.size());
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView ivProfileImage;
        LinearLayout messageItem;
        TextView tvSender, tvMessage, tvTime;
        RadioButton rbSelect;
        Conversation conversation;
        private ConversationItemListener listener;
        private ClickListener clickListener;

        public MessageViewHolder(View itemView, ConversationItemListener listener) {
            super(itemView);
            messageItem = (LinearLayout) itemView;
            ivProfileImage = itemView.findViewById(R.id.message_item_profile);
            tvSender = itemView.findViewById(R.id.message_item_sender);
            tvMessage = itemView.findViewById(R.id.message_item_message);
            tvTime = itemView.findViewById(R.id.message_item_time);
            rbSelect = itemView.findViewById(R.id.message_item_select);
            rbSelect.setOnClickListener(this);
            itemView.setOnClickListener(this);
            this.listener = listener;
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
                rbSelect.setChecked(false);
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
            if (listener != null) {
                String nameTransitionKey = "transitionName" + getAdapterPosition();
                tvSender.setTransitionName(nameTransitionKey);
                Pair namePair = Pair.create(tvSender, nameTransitionKey);
                listener.onOpenChatScreen(this.conversation, namePair);
            }
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
            if (clickListener != null) {
                clickListener.onSelect(conversation);
            }
        }

        public void setClickListener(ClickListener clickListener) {
            this.clickListener = clickListener;
        }

        private String getDisplayTime(double timestamps) {
            Date date = new Date((long)timestamps * 1000);
            if (DateUtils.isSameDay(date.getTime(), new Date().getTime())) {
                return DateUtils.toString("h:mm a", date);
            } else if (DateUtils.isYesterday(date)) {
                return "Yesterday";
            } else {
                if (DateUtils.withinOneWeek(date)) {
                    return DateUtils.toString("EEEE", date);
                } else {
                    return DateUtils.toString("MMMM d, yyyy", date);
                }
            }
        }

        public void bindData(Conversation model, boolean isSelected) {
            this.conversation = model;
            this.tvSender.setText(model.conversationName);
            this.tvTime.setText(getDisplayTime(model.timesstamps));
            String message = "";
            if (model.messageType == Constant.MSG_TYPE_TEXT) {
                if (ServiceManager.getInstance().getCurrentMarkStatus(model.markStatuses, model.maskMessages)) {
                    message = ServiceManager.getInstance().encodeMessage(itemView.getContext(), model.message);
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
            this.setReadStatus(model.isRead);
            this.setEditMode(isEditMode);
            this.setSelect(isSelected);

            String imageTransitionKey = "transitionImage" + getAdapterPosition();
            ivProfileImage.setTransitionName(imageTransitionKey);
            if (model.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
                String nameTransitionKey = "transitionName" + getAdapterPosition();
                tvSender.setTransitionName(nameTransitionKey);
                ivProfileImage.setOnClickListener(v -> {
                    if (listener != null) {
                        Pair imagePair = Pair.create(ivProfileImage, imageTransitionKey);
                        Pair namePair = Pair.create(tvSender, nameTransitionKey);
                        listener.onOpenUserProfile(conversation, imagePair, namePair);
                    }
                });
            } else {
                ViewCompat.setTransitionName(ivProfileImage, model.groupID);
                ivProfileImage.setOnClickListener(v -> {
                    if (listener != null) {
                        Pair imagePair = Pair.create(ivProfileImage, imageTransitionKey);
                        listener.onOpenGroupProfile(conversation, imagePair);
                    }
                });
            }
            UiUtils.displayProfileAvatar(ivProfileImage, model.conversationAvatarUrl);
        }
    }

    public interface ClickListener {
        void onSelect(Conversation conversation);
    }

    public interface ConversationItemListener {
        void onOpenUserProfile(Conversation conversation, Pair<View, String>... sharedElements);
        void onOpenGroupProfile(Conversation conversation, Pair<View, String>... sharedElements);
        void onOpenChatScreen(Conversation conversation, Pair<View, String>... sharedElements);
        void onSelect(Conversation conversation);
    }
}
