package com.ping.android.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.Log;
import com.ping.android.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> implements Comparator<Message> {

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
    }

    public void setOrginalConversation(Conversation orginalConversation) {
        this.orginalConversation = orginalConversation;
    }

    public void addOrUpdate(Message message) {
        Boolean isAdd = true;
        for (int i = 0; i < displayMessages.size(); i++) {
            if (displayMessages.get(i).key.equals(message.key)) {
                isAdd = false;
                break;
            }
        }

        if (isAdd) {
            addMessage(message);
        } else {
            updateMessage(message);
        }

        Collections.sort(displayMessages, this);
        notifyDataSetChanged();
    }

    public void addMessage(Message message) {
        this.displayMessages.add(message);
        notifyItemInserted(displayMessages.size() - 1);
        if (displayMessages.size() >= 2) {
            notifyItemChanged(displayMessages.size() - 2);
        }
    }

    public void updateMessage(Message message) {
        for (int i = 0; i < displayMessages.size(); i++) {
            if (displayMessages.get(i).key != null && displayMessages.get(i).key.equals(message.key)) {
                displayMessages.set(i, message);
                notifyItemChanged(i);
                break;
            }
        }
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

    public void showTyping(Boolean show) {
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

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == Constant.MSG_TYPE_TYPING.intValue()) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_left_typing, parent, false);
            return new ChatAdapter.ChatViewHolder(view);
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
            return Constant.MSG_TYPE_TYPING.intValue();
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
    public void onBindViewHolder(ChatViewHolder viewHolder, int position) {
        Message model = displayMessages.get(position);
        viewHolder.setModel(model);
        viewHolder.setChatText(model.message);
        Long status = ServiceManager.getInstance().getCurrentStatus(model.status);

        viewHolder.setIvChatProfile();
        viewHolder.setInfo();
        if (model.photoUrl != null) {
            viewHolder.setIvChatPhoto(model.photoUrl);
        } else if (model.gameUrl != null) {
            viewHolder.setIvChatPhoto(model.gameUrl);
        } else if (model.audioUrl != null) {
            viewHolder.setAudioSrc(model.audioUrl);
        }

        if (model.gameUrl != null) {
            if (status == Constant.MESSAGE_STATUS_GAME_PASS || status == Constant.MESSAGE_STATUS_GAME_FAIL) {
                viewHolder.setStatus(status);
            } else if (getItemViewType(position) == LEFT_MSG_GAME) {
                viewHolder.setStatus(Constant.MESSAGE_STATUS_GAME_INIT);
            } else {
                viewHolder.setStatus(Constant.MESSAGE_STATUS_GAME_DELIVERED);
            }
        } else {
            if (model.senderId.equals(currentUserID) && position == displayMessages.size() - 1) {
                viewHolder.setStatus(status);
            } else {
                viewHolder.setStatus(Constant.MESSAGE_STATUS_HIDE);
            }
        }

        viewHolder.setEditMode(isEditMode);
        if (selectMessages.contains(model)) {
            viewHolder.setSelect(true);
        } else {
            viewHolder.setSelect(false);
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

    public interface ClickListener {
        void onSelect(List<Message> selectMessages);
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private final static int DOUBLE_TAP = 2;
        private final static int SINGLE_TAP = 1;
        private final static int DELAY = 300;
        LinearLayout chatMessageView;
        TextView tvText, tvStatus, tvInfo;
        ImageView ivChatProfile;
        ImageView ivChatPhoto;
        ImageButton btPlaySong;
        Boolean markStatus = false;
        RadioButton rbSelect;
        Message message;
        int clickViewID = 0;
        private boolean mTookFirstEvent = false;

        private android.os.Message mMessage = null;
        private Handler mHandler = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case SINGLE_TAP:
                        mTookFirstEvent = false;
                        handleClick(msg);
                        break;
                    case DOUBLE_TAP:
                        handleDoubleClick(msg);
                        break;
                }
            }

        };

        public ChatViewHolder(View itemView) {
            super(itemView);
            tvInfo = (TextView) itemView.findViewById(R.id.item_chat_info);
            tvText = (TextView) itemView.findViewById(R.id.item_chat_text);
            if (tvText != null) {
                tvText.setOnClickListener(this);
            }
            ivChatPhoto = (ImageView) itemView.findViewById(R.id.item_chat_image);
            if (ivChatPhoto != null)
                ivChatPhoto.setOnClickListener(this);
            ivChatProfile = (ImageView) itemView.findViewById(R.id.item_chat_user_profile);


            btPlaySong = (ImageButton) itemView.findViewById(R.id.item_chat_audio);
            if (btPlaySong != null)
                btPlaySong.setOnClickListener(this);
            tvStatus = (TextView) itemView.findViewById(R.id.item_chat_status);
            tvStatus.setVisibility(View.GONE);
            chatMessageView = (LinearLayout) itemView.findViewById(R.id.item_chat_message);
            if (chatMessageView != null) {
                chatMessageView.setOnClickListener(this);
            }

            rbSelect = (RadioButton) itemView.findViewById(R.id.item_chat_select);
            rbSelect.setOnClickListener(this);
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
            if (message.messageType == Constant.MSG_TYPE_TYPING) {
                return;
            }
            if (isEditMode) {
                onClickEditMode(view);
                return;
            }
            clickViewID = view.getId();
            if (!mTookFirstEvent) {
                mTookFirstEvent = true;
                mMessage = mMessage == null ? new android.os.Message() : mHandler.obtainMessage();
                /*"Recycling" the message, instead creating new instance we get the old one */
                mHandler.removeMessages(SINGLE_TAP);
                mMessage.arg1 = clickViewID;
                mMessage.what = SINGLE_TAP;
                mHandler.sendMessageDelayed(mMessage, DELAY);
            } else {
                mHandler.removeMessages(SINGLE_TAP);
                mMessage = mHandler.obtainMessage();
                /*obtaining old message instead creating new one */
                mMessage.arg1 = clickViewID;
                mMessage.what = DOUBLE_TAP;
                mHandler.sendMessageAtFrontOfQueue(mMessage);
                mTookFirstEvent = false;
            }
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

        private void handleClick(android.os.Message msg) {
            int position = getAdapterPosition();
            if (position < 0) {
                return;
            }
            Message model = displayMessages.get(position);
            switch (msg.arg1) {
                case R.id.item_chat_image:
                    onImageClick(true);
                    break;
                case R.id.item_chat_audio:
                    playAudio(model.audioUrl);
                    break;
            }
        }

        private void handleDoubleClick(android.os.Message msg) {
            int position = getAdapterPosition();
            if (position < 0) {
                return;
            }
            Message model = displayMessages.get(position);
            switch (msg.arg1) {
                case R.id.item_chat_text:
                    onMarkTest();
                    break;
                case R.id.item_chat_image:
                    onImageClick(false);
                    break;
                case R.id.item_chat_message:
                    if (StringUtils.isNotEmpty(message.message)) {
                        onMarkTest();
                    }
                    break;
            }
        }

        private void onMarkTest() {
            markStatus = !markStatus;
            message.markStatuses.put(currentUserID, markStatus);
            String text = message.message;
            if (markStatus) {
                text = ServiceManager.getInstance().encodeMessage(activity, text);
            }
            if (StringUtils.isNotEmpty(conversationID)) {
                ServiceManager.getInstance().updateMarkStatus(conversationID, message.key, markStatus);
            }
            tvText.setText(text);
        }

        private void onImageClick(Boolean isPuzzled) {
            if (!StringUtils.isEmpty(message.photoUrl) && message.photoUrl.startsWith("PPhtotoMessageIdentifier")) {
                return;
            }
            if (!StringUtils.isEmpty(message.gameUrl) && message.gameUrl.startsWith("PPhtotoMessageIdentifier")) {
                return;
            }
            if (!StringUtils.isEmpty(message.photoUrl)) {
                unPuzzleImage(message.photoUrl, isPuzzled);
            } else if (!StringUtils.isEmpty(message.gameUrl)) {
                if (currentUserID.equals(message.senderId)) {
                    unPuzzleImage(message.gameUrl, isPuzzled);
                } else {
                    unPuzzleGame(message.gameUrl, isPuzzled);
                }
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
            if (StringUtils.isEmpty(message.gameUrl)) {
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
            if (ivChatPhoto == null) return;
            if (StringUtils.isEmpty(imageURL) || imageURL.startsWith("PPhtotoMessageIdentifier")) {
                ivChatPhoto.setImageResource(R.drawable.img_loading);
                return;
            }
            Long status = ServiceManager.getInstance().getCurrentStatus(message.status);
            if (StringUtils.isNotEmpty(message.gameUrl) && !currentUserID.equals(message.senderId) &&
                    status == Constant.MESSAGE_STATUS_GAME_FAIL) {
                ivChatPhoto.setImageResource(R.drawable.img_game_over);
                //ivChatPhoto.setVisibility(View.GONE);
                return;
            }

            String imageLocalPath = activity.getExternalFilesDir(null).getAbsolutePath();
            String imageLocalName = CommonMethod.getFileNameFromFirebase(imageURL);
            imageLocalPath = imageLocalPath + File.separator + imageLocalName;
            final String imageStoragePath = imageLocalPath;

            File imageLocal = new File(imageLocalPath);
            String imageLocalFolder = imageLocal.getParent();
            CommonMethod.createFolder(imageLocalFolder);

            if (imageLocal.exists()) {
                setImage(imageLocalPath);
            } else {
                Log.d("imageURL = " + imageURL);
                try {
                    StorageReference imageReference = storage.getReferenceFromUrl(imageURL);
                    imageReference.getFile(imageLocal).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            setImage(imageStoragePath);
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

        public void setAudioSrc(String audioUrl) {
            if (StringUtils.isEmpty(audioUrl)) {
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

        private void setImage(String imageLocalPath) {
            Boolean bitmapMark = markStatus;
            // Game: always  puzzle image for player
            Long status = ServiceManager.getInstance().getCurrentStatus(message.status);
            if (StringUtils.isNotEmpty(message.gameUrl) && !currentUserID.equals(message.senderId) &&
                    status != Constant.MESSAGE_STATUS_GAME_PASS) {
                bitmapMark = true;
            }
            Bitmap originalBitmap = BitmapFactory.decodeFile(imageLocalPath);
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

            if (StringUtils.isNotEmpty(status)) {
                tvStatus.setText(status);
                tvStatus.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }
    }
}
