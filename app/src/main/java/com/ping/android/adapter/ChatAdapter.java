package com.ping.android.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ping.android.activity.GameActivity;
import com.ping.android.activity.PuzzleActivity;
import com.ping.android.activity.R;
import com.ping.android.activity.UserDetailActivity;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.Log;
import com.ping.android.utils.UiUtils;
import com.ping.android.view.DoubleClickListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Comparator<Message> {

    private static final int RIGHT_MSG = 0;
    private static final int LEFT_MSG = 1;
    private static final int RIGHT_MSG_IMG = 2;
    private static final int LEFT_MSG_IMG = 3;
    private static final int RIGHT_MSG_AUDIO = 4;
    private static final int LEFT_MSG_AUDIO = 5;
    private static final int RIGHT_MSG_GAME = 6;
    private static final int LEFT_MSG_GAME = 7;

    private List<Message> displayMessages;
    private List<Message> selectMessages;
    private String conversationID, currentUserID;
    private Conversation orginalConversation;
    private Boolean isEditMode = false;
    private MediaPlayer mMediaPlayer;
    private Activity activity;
    private ClickListener clickListener;

    private FirebaseStorage storage;

    public ChatAdapter(String conversationID, String currentUserID, List<Message> displayMessages, Activity activity, ClickListener clickListener) {
        this.activity = activity;
        this.clickListener = clickListener;
        this.conversationID = conversationID;
        this.currentUserID = currentUserID;
        this.displayMessages = displayMessages;
        selectMessages = new ArrayList<>();
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        storage = FirebaseStorage.getInstance();
        this.addPadding();
    }

    public void setOrginalConversation(Conversation orginalConversation) {
        this.orginalConversation = orginalConversation;
    }

    public void addOrUpdate(Message message) {
        boolean isAdd = true;
        int size = displayMessages.size();
        int index = displayMessages.size();
        for (int i = size - 1; i >= 0; i--) {
            Message displayMessage = displayMessages.get(i);
            if (displayMessage.messageType != Constant.MSG_TYPE_TYPING
                    && displayMessage.messageType != Constant.MSG_TYPE_PADDING) {
                if (message.key.equals(displayMessages.get(i).key)) {
                    index = i;
                    isAdd = false;
                    break;
                }
                if (message.timestamp > displayMessage.timestamp) {
                    index = i + 1;
                    break;
                }

            }
        }

        if (isAdd) {
            this.displayMessages.add(index, message);
            notifyItemInserted(index);
            // If new message has come, add to list then update previous message to hide its status
            if (index == this.displayMessages.size() - 1 && index > 0) {
                notifyItemChanged(index - 1);
            }
        } else {
            displayMessages.set(index, message);
            notifyItemChanged(index);
//            notifyDataSetChanged();
        }
//        // TODO should not notify all data here
//        Collections.sort(displayMessages, this);
//        notifyDataSetChanged();
    }

    public void addMessage(Message message) {
        int index = displayMessages.size();
        if (index > 0) {
            Message msg = displayMessages.get(index - 1);
            if (msg.messageType == Constant.MSG_TYPE_TYPING) {
                index = index > 1 ? index - 1 : 0;
            }
        }
        this.displayMessages.add(index, message);
        //this.displayMessages.add(message);
        notifyItemInserted(index);
//        if (displayMessages.size() >= 2) {
//            notifyItemChanged(displayMessages.size() - 2);
//        }
    }

    public void deleteMessage(String messageID) {
        Message deletedMessage = null;
        for (Message message : displayMessages) {
            if (message.key.equals(messageID)) {
                deletedMessage = message;
            }
        }
        if (deletedMessage != null) {
            displayMessages.remove(deletedMessage);
            selectMessages.remove(deletedMessage);
            notifyDataSetChanged();
        }
    }

    public void setEditMode(Boolean isEditMode) {
        this.isEditMode = isEditMode;
        if (!isEditMode) {
            selectMessages.clear();
        }
        notifyDataSetChanged();
    }

    private void addPadding() {
        Message message = new Message();
        message.messageType = Constant.MSG_TYPE_PADDING;
        // Add to start
        this.displayMessages.add(0, message);
    }

    public void showTyping(boolean show) {
        if (isEditMode) return;
        int typingPosition = -1;
        for (int i = 0; i < displayMessages.size(); i++) {
            if (displayMessages.get(i).messageType == Constant.MSG_TYPE_TYPING) {
                typingPosition = i;
                break;
            }
        }
        if (show) {
            if (typingPosition == -1) {
                Message message = new Message();
                message.messageType = Constant.MSG_TYPE_TYPING;
                this.displayMessages.add(message);
                notifyItemInserted(displayMessages.size() - 1);
            }
        } else {
            if (typingPosition != -1) {
                this.displayMessages.remove(typingPosition);
                notifyItemRemoved(typingPosition);
            }
        }
    }

    public List<Message> getSelectMessage() {
        return selectMessages;
    }

    public Message getLastMessage() {
        if (displayMessages.size() >= 2) {
            return displayMessages.get(getItemCount() - 1);
        }
        return null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == Constant.MSG_TYPE_TYPING) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_left_typing, parent, false);
            return new TypingViewHolder(view);
        } else if (viewType == Constant.MSG_TYPE_PADDING) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_padding, parent, false);
            return new PaddingViewHolder(view);
        } else if (viewType == RIGHT_MSG) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_right_msg, parent, false);
            return new ChatAdapter.ChatViewHolder(view);
        } else if (viewType == LEFT_MSG) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_left_msg, parent, false);
            return new ChatAdapter.ChatViewHolder(view);
        } else if (viewType == RIGHT_MSG_GAME) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_right_game, parent, false);
            return new ChatAdapter.ChatViewHolder(view);
        } else if (viewType == LEFT_MSG_GAME) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_left_game, parent, false);
            return new ChatAdapter.ChatViewHolder(view);
        } else if (viewType == RIGHT_MSG_AUDIO) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_right_audio, parent, false);
            return new ChatAdapter.ChatViewHolder(view);
        } else if (viewType == LEFT_MSG_AUDIO) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_left_audio, parent, false);
            return new ChatAdapter.ChatViewHolder(view);
        } else if (viewType == RIGHT_MSG_IMG) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_right_img, parent, false);
            return new ChatAdapter.ChatViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_left_img, parent, false);
            return new ChatAdapter.ChatViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message model = displayMessages.get(position);
        if (model.messageType == Constant.MSG_TYPE_TYPING) {
            return Constant.MSG_TYPE_TYPING;
        } else if (model.messageType == Constant.MSG_TYPE_PADDING) {
            return Constant.MSG_TYPE_PADDING;
        }
        if (model.photoUrl != null) {
            if (model.senderId.equals(currentUserID)) {
                return RIGHT_MSG_IMG;
            } else {
                return LEFT_MSG_IMG;
            }
        } else if (model.gameUrl != null) {
            if (model.senderId.equals(currentUserID)) {
                return RIGHT_MSG_GAME;
            } else {
                return LEFT_MSG_GAME;
            }
        } else if (model.audioUrl != null) {
            if (model.senderId.equals(currentUserID)) {
                return RIGHT_MSG_AUDIO;
            } else {
                return LEFT_MSG_AUDIO;
            }
        } else if (model.senderId.equals(currentUserID)) {
            return RIGHT_MSG;
        } else {
            return LEFT_MSG;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ChatViewHolder) {
            ChatViewHolder viewHolder = (ChatViewHolder) holder;
            Message model = displayMessages.get(position);
            viewHolder.bindData(model, position == getItemCount() - 1);
        }
    }

    @Override
    public int getItemCount() {
        return displayMessages.size();
    }

    @Override
    public int compare(Message o1, Message o2) {
        if (o1.timestamp > o2.timestamp) {
            return 1;
        } else if (o1.timestamp < o2.timestamp) {
            return -1;
        }

        return 0;
    }

    public Message getItem(int position) {
        if (displayMessages == null || displayMessages.size() == 0) {
            return null;
        }

        return displayMessages.get(position);
    }

    public void appendHistoryItems(List<Message> messages) {
        Collections.sort(messages, this);
        int startIndex = displayMessages.size() >= 1 ? 1 : 0;
        int endIndex = messages.size();
        displayMessages.addAll(startIndex, messages);
        notifyItemRangeInserted(startIndex, endIndex);
    }

    public interface ClickListener {
        void onSelect(List<Message> selectMessages);

        void onDoubleTap(Message message, boolean markStatus);
    }

    public static class TypingViewHolder extends RecyclerView.ViewHolder {
        AnimationDrawable rocketAnimation;

        public TypingViewHolder(View itemView) {
            super(itemView);
            ImageView imageView = itemView.findViewById(R.id.typing);
            rocketAnimation = (AnimationDrawable) imageView.getDrawable();
            rocketAnimation.start();
        }
    }

    public static class PaddingViewHolder extends RecyclerView.ViewHolder {

        public PaddingViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        TextView tvText, tvStatus, tvInfo;
        ImageView ivChatProfile;
        ImageView ivChatPhoto;
        ImageButton btPlaySong;
        Boolean markStatus = false;
        RadioButton rbSelect;
        Message message;
        private Callback callback;

        public ChatViewHolder(View itemView) {
            super(itemView);
            initView();
        }

        protected void initView() {
            itemView.setOnClickListener(new DoubleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    handleClick(v);
                }

                @Override
                public void onDoubleClick(View v) {
                    handleDoubleClick(v);
                }

                @Override
                public boolean shouldHandleDoubleClick() {
                    return message.messageType == Constant.MSG_TYPE_TEXT ||
                            message.messageType == Constant.MSG_TYPE_IMAGE;
                }
            });
            tvInfo = itemView.findViewById(R.id.item_chat_info);
            tvText = itemView.findViewById(R.id.item_chat_text);
            ivChatPhoto = itemView.findViewById(R.id.item_chat_image);
            ivChatProfile = itemView.findViewById(R.id.item_chat_user_profile);


            btPlaySong = itemView.findViewById(R.id.item_chat_audio);
            if (btPlaySong != null)
                btPlaySong.setOnClickListener(this::handleClick);
            tvStatus = itemView.findViewById(R.id.item_chat_status);
            tvStatus.setVisibility(View.GONE);

            rbSelect = itemView.findViewById(R.id.item_chat_select);
            rbSelect.setOnClickListener(view -> onClickEditMode(view));
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

        private void onClickEditMode(View view) {
            boolean isSelect;
            switch (view.getId()) {
                case R.id.item_chat_select:
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
                selectMessages.add(message);
            } else {
                selectMessages.remove(message);
            }
            clickListener.onSelect(selectMessages);
        }

        private void handleClick(View view) {
            switch (message.messageType) {
                case Constant.MSG_TYPE_IMAGE:
                    openImage(markStatus);
                    break;
                case Constant.MSG_TYPE_VOICE:
                    playAudio(message.audioUrl);
                    break;
                case Constant.MSG_TYPE_GAME:
                    onGameClick(markStatus);
                    break;
            }
        }

        private void handleDoubleClick(View view) {
            switch (message.messageType) {
                case Constant.MSG_TYPE_TEXT:
                    onMarkTest();
                    break;
                case Constant.MSG_TYPE_IMAGE:
                    markStatus = !markStatus;
                    if (clickListener != null) {
                        clickListener.onDoubleTap(message, markStatus);
                    }
                    break;
            }
        }

        private void openImage(boolean isPuzzled) {
            String photoUrl = !TextUtils.isEmpty(message.photoUrl) ? message.photoUrl : message.thumbUrl;
            if (TextUtils.isEmpty(photoUrl)) return;
            unPuzzleImage(photoUrl, isPuzzled);
        }

        private void onMarkTest() {
            markStatus = !markStatus;
            message.markStatuses.put(currentUserID, markStatus);
            String text = message.message;
            if (markStatus) {
                text = ServiceManager.getInstance().encodeMessage(activity, text);
            }
            if (!TextUtils.isEmpty(conversationID)) {
                if (clickListener != null) {
                    clickListener.onDoubleTap(message, markStatus);
                }
            }
            tvText.setText(text);
        }

        private void onGameClick(boolean isPuzzled) {
            if (!TextUtils.isEmpty(message.gameUrl) && message.gameUrl.startsWith("PPhtotoMessageIdentifier")) {
                return;
            }

            if (currentUserID.equals(message.senderId)) {
                unPuzzleImage(message.gameUrl, isPuzzled);
            } else {
                unPuzzleGame(message.gameUrl, isPuzzled);
            }
        }

        private void unPuzzleImage(String imageURL, Boolean isPuzzled) {
            Intent intent = new Intent(activity, PuzzleActivity.class);
            intent.putExtra("CONVERSATION_ID", conversationID);
            intent.putExtra("MESSAGE_ID", message.key);
            intent.putExtra("IMAGE_URL", imageURL);
            intent.putExtra("PUZZLE_STATUS", isPuzzled);
            activity.startActivity(intent);
        }

        private void unPuzzleGame(String imageURL, Boolean isPuzzled) {
            if (TextUtils.isEmpty(message.gameUrl)) {
                return;
            }
            // Only play game for player
            Long status = ServiceManager.getInstance().getCurrentStatus(message.status);
            if (!currentUserID.equals(message.senderId)) {
                if (status == Constant.MESSAGE_STATUS_GAME_FAIL) {
                    // Game fail don't do anything
                    return;
                } else if (status == Constant.MESSAGE_STATUS_GAME_PASS) {
                    // Game pass, just unpuzzle image
                    unPuzzleImage(imageURL, isPuzzled);
                } else if (status != Constant.MESSAGE_STATUS_GAME_FAIL) {
                    Intent intent = new Intent(activity, GameActivity.class);
                    intent.putExtra("CONVERSATION_ID", conversationID);
                    intent.putExtra("MESSAGE_ID", message.key);
                    intent.putExtra("IMAGE_URL", imageURL);
                    activity.startActivity(intent);
                    return;
                }
            }
            // Show image for current User
            unPuzzleImage(imageURL, isPuzzled);
        }

        private void playAudio(String audioURL) {
            try {
                Log.d(audioURL);
                String audioLocalPath = activity.getExternalFilesDir(null).getAbsolutePath();
                String audioLocalName = CommonMethod.getFileNameFromFirebase(audioURL);
                audioLocalPath = audioLocalPath + File.separator + audioLocalName;
                File audioLocal = new File(audioLocalPath);

                if (audioLocal.exists()) {
                    mMediaPlayer.reset();
                    mMediaPlayer.setDataSource(audioLocalPath);
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                }

            } catch (Exception e) {
                Log.e(e);
            }
        }

        public void setModel(Message message) {
            this.message = message;
            markStatus = ServiceManager.getInstance().getCurrentMarkStatus(message.markStatuses);
        }

        public void setChatText(String text) {
            if (tvText == null) return;
            if (markStatus) {
                text = ServiceManager.getInstance().encodeMessage(activity, text);
            }
            tvText.setText(text);
        }

        public void setIvChatPhoto(String imageURL) {
            if (callback != null) callback = null;
            if (ivChatPhoto == null) return;
            if (TextUtils.isEmpty(imageURL) || imageURL.startsWith("PPhtotoMessageIdentifier")) {
                ivChatPhoto.setImageResource(R.drawable.img_loading);
                return;
            }
            Long status = ServiceManager.getInstance().getCurrentStatus(message.status);
            if (!TextUtils.isEmpty(message.gameUrl) && !currentUserID.equals(message.senderId) &&
                    status == Constant.MESSAGE_STATUS_GAME_FAIL) {
                ivChatPhoto.setImageResource(R.drawable.img_game_over);
                return;
            }

            callback = new Callback() {
                @Override
                public void complete(Object error, Object... data) {
                    if (error == null) {
                        Bitmap bitmap = (Bitmap) data[0];
                        setChatImage(bitmap);
                    }
                }
            };
            UiUtils.loadImage(ivChatPhoto, imageURL, callback);
        }

        public void setAudioSrc(String audioUrl) {
            if (TextUtils.isEmpty(audioUrl)) {
                return;
            }

            String audioLocalPath = activity.getExternalFilesDir(null).getAbsolutePath();
            String audioLocalName = CommonMethod.getFileNameFromFirebase(audioUrl);
            audioLocalPath = audioLocalPath + File.separator + audioLocalName;

            File audioLocal = new File(audioLocalPath);
            String imageLocalFolder = audioLocal.getParent();
            CommonMethod.createFolder(imageLocalFolder);

            if (audioLocal.exists()) {
            } else {
                Log.d("audioUrl = " + audioUrl);
                try {
                    StorageReference audioReference = storage.getReferenceFromUrl(audioUrl);
                    audioReference.getFile(audioLocal).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                        }
                    });
                } catch (Exception ex) {
                    Log.e(ex);
                }
            }
        }

        public void setIvChatProfile() {
            if (ivChatProfile == null) {
                return;
            }

            ivChatProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, UserDetailActivity.class);
                    intent.putExtra(Constant.START_ACTIVITY_USER_ID, message.sender.key);
                    activity.startActivity(intent);
                }
            });

            UiUtils.displayProfileImage(activity, ivChatProfile, message.sender);
        }

        public void setInfo() {
            if (tvInfo == null) {
                return;
            }
            String time = CommonMethod.convertTimestampToTime(message.timestamp);
            if (orginalConversation != null
                    && !currentUserID.equals(message.senderId)
                    && orginalConversation.conversationType == Constant.CONVERSATION_TYPE_GROUP) {
                tvInfo.setText(message.sender.getDisplayName() + " " + time);
            } else {
                tvInfo.setText(time);
            }
            tvInfo.setVisibility(View.VISIBLE);
        }

        private void setChatImage(Bitmap originalBitmap) {
            boolean bitmapMark = markStatus;
            // Game: always  puzzle image for player
            long status = ServiceManager.getInstance().getCurrentStatus(message.status);
            if (!TextUtils.isEmpty(message.gameUrl) && !currentUserID.equals(message.senderId) &&
                    status != Constant.MESSAGE_STATUS_GAME_PASS) {
                bitmapMark = true;
            }

            DisplayMetrics displaymetrics = new DisplayMetrics();
            ((Activity) ivChatPhoto.getContext()).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int maxEdgeLength = Math.round((float) (displaymetrics.widthPixels * .6));

            if (originalBitmap.getWidth() > originalBitmap.getHeight()) {

                ivChatPhoto.getLayoutParams().height = Math.round((float) (maxEdgeLength * originalBitmap.getHeight() / originalBitmap.getWidth()));
                ivChatPhoto.getLayoutParams().width = maxEdgeLength;
            } else {

                ivChatPhoto.getLayoutParams().height = maxEdgeLength;
                ivChatPhoto.getLayoutParams().width = Math.round((float) (maxEdgeLength * originalBitmap.getWidth() / originalBitmap.getHeight()));
            }

            if (!bitmapMark) {
                ivChatPhoto.setImageBitmap(originalBitmap);
            } else {
                Bitmap puzzledBitmap = CommonMethod.puzzleImage(originalBitmap, 3);
                if (puzzledBitmap != null) {
                    ivChatPhoto.setImageBitmap(puzzledBitmap);
                }
            }
            ivChatPhoto.setClipToOutline(true);
        }

        private void setStatus(long messageStatus) {
            if (tvStatus == null) return;
            if (messageStatus == Constant.MESSAGE_STATUS_HIDE) {
                tvStatus.setVisibility(View.GONE);
                return;
            }
            String status = "";
            if (messageStatus == Constant.MESSAGE_STATUS_SENT) {
                status = "Delivered";
            } else if (messageStatus == Constant.MESSAGE_STATUS_DELIVERED) {
                status = "Delivered";
            } else if (messageStatus == Constant.MESSAGE_STATUS_ERROR) {
                status = "Error";
            } else if (messageStatus == Constant.MESSAGE_STATUS_GAME_PASS) {
                status = "Game Passed";
            } else if (messageStatus == Constant.MESSAGE_STATUS_GAME_FAIL) {
                status = "Game Failed";
            } else if (messageStatus == Constant.MESSAGE_STATUS_GAME_INIT) {
                status = "Game";
            } else if (messageStatus == Constant.MESSAGE_STATUS_GAME_DELIVERED) {
                status = "Game Delivered";
            }

            if (!TextUtils.isEmpty(status)) {
                tvStatus.setText(status);
                tvStatus.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }

        public void bindData(Message model, boolean isLastMessage) {
            setModel(model);
            setChatText(model.message);
            long status = ServiceManager.getInstance().getCurrentStatus(model.status);

            setIvChatProfile();
            setInfo();
            if (message.messageType == Constant.MSG_TYPE_IMAGE) {
                setIvChatPhoto(model.thumbUrl);
            } else if (message.messageType == Constant.MSG_TYPE_GAME) {
                setIvChatPhoto(model.gameUrl);
                if (status == Constant.MESSAGE_STATUS_GAME_PASS || status == Constant.MESSAGE_STATUS_GAME_FAIL) {
                    setStatus(status);
                } else if (message.senderId.equals(currentUserID)) {
                    setStatus(Constant.MESSAGE_STATUS_GAME_DELIVERED);
                } else {
                    setStatus(Constant.MESSAGE_STATUS_GAME_INIT);
                }
            } else if (message.messageType == Constant.MSG_TYPE_VOICE) {
                setAudioSrc(model.audioUrl);
            }

            if (message.senderId.equals(currentUserID) && isLastMessage) {
                setStatus(status);
            } else {
                setStatus(Constant.MESSAGE_STATUS_HIDE);
            }

            setEditMode(isEditMode);
            setSelect(selectMessages.contains(model));
        }
    }
}
