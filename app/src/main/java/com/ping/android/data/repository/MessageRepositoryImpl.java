package com.ping.android.data.repository;

import com.bzzzchat.rxfirebase.RxFirebaseDatabase;
import com.bzzzchat.rxfirebase.database.ChildEvent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.ping.android.data.db.AppDatabase;
import com.ping.android.data.entity.ChildData;
import com.ping.android.data.entity.MessageEntity_Table;
import com.ping.android.data.mappers.MessageMapper;
import com.ping.android.domain.repository.MessageRepository;
import com.ping.android.data.entity.MessageEntity;
import com.ping.android.model.Message;
import com.ping.android.utils.configs.Constant;
import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.rx2.language.RXSQLite;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.transaction.ITransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by tuanluong on 2/26/18.
 */

public class MessageRepositoryImpl implements MessageRepository {
    FirebaseDatabase database;
    @Inject
    MessageMapper messageMapper;

    @Inject
    public MessageRepositoryImpl() {
        database = FirebaseDatabase.getInstance();
    }

    @Override
    public Observable<List<MessageEntity>> getLastMessages(String conversationId) {
        DatabaseReference reference = database.getReference("messages").child(conversationId);
        reference.keepSynced(true);
        Query query = reference
                .orderByChild("timestamp")
                .limitToLast(Constant.LATEST_RECENT_MESSAGES);
        return RxFirebaseDatabase.getInstance(query)
                .onSingleValueEvent()
                .map(dataSnapshot -> {
                    List<MessageEntity> messages = new ArrayList<>();
                    if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            if (!child.exists()) continue;
                            MessageEntity message = messageMapper.transform(child);
                            messages.add(message);
                        }
                    }
                    return messages;
                })
                .toObservable();
    }

    @Override
    public Observable<List<MessageEntity>> loadMoreMessages(String conversationId, double endTimestamp) {
        Query query = database.getReference("messages").child(conversationId)
                .orderByChild("timestamp")
                .endAt(endTimestamp)
                .limitToLast(Constant.LOAD_MORE_MESSAGE_AMOUNT + 1);
        return RxFirebaseDatabase.getInstance(query)
                .onSingleValueEvent()
                .toObservable()
                .map(dataSnapshot -> {
                    List<MessageEntity> messages = new ArrayList<>();
                    if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            if (!child.exists()) continue;
                            MessageEntity message = messageMapper.transform(child);
                            messages.add(message);
                        }
                    }
                    return messages;
                });
    }

    @Override
    public Observable<List<MessageEntity>> loadConversationMedia(String conversationId, double lastTimestamp) {
        DatabaseReference reference = database.getReference("media").child(conversationId);
        reference.keepSynced(true);
        Query query = reference
                .orderByChild("timestamp")
                .endAt(lastTimestamp)
                .limitToLast(20);
        return RxFirebaseDatabase.getInstance(query)
                .onSingleValueEvent()
                .toObservable()
                .map(dataSnapshot -> {
                    List<MessageEntity> messages = new ArrayList<>();
                    if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            if (!child.exists()) continue;
                            MessageEntity message = messageMapper.transform(child);
                            messages.add(message);
                        }
                    }
                    return messages;
                });
    }

    @Override
    public Observable<ChildData<MessageEntity>> observeMessageUpdate(String conversationId) {
        Query query = database.getReference("messages").child(conversationId);
        return RxFirebaseDatabase.getInstance(query)
                .onChildEvent()
                .map(childEvent -> {
                    if (childEvent.dataSnapshot.exists() && childEvent.type == ChildEvent.Type.CHILD_CHANGED) {
                        MessageEntity message = messageMapper.transform(childEvent.dataSnapshot);
                        return new ChildData<>(message, childEvent.type);
                    } else {
                        return new ChildData<MessageEntity>(null, childEvent.type);
                    }
                });
    }

    @Override
    public Observable<ChildData<MessageEntity>> observeLastMessage(String conversationId) {
        DatabaseReference reference = database.getReference("messages").child(conversationId);
        reference.keepSynced(true);
        Query query = reference
                .orderByChild("timestamp")
                .limitToLast(1);
        return RxFirebaseDatabase.getInstance(query)
                .onChildEvent()
                .map(childEvent -> {
                    if (childEvent.dataSnapshot.exists() && childEvent.type == ChildEvent.Type.CHILD_ADDED) {
                        MessageEntity message = messageMapper.transform(childEvent.dataSnapshot);
                        return new ChildData<>(message, childEvent.type);
                    } else {
                        return new ChildData<MessageEntity>(null, childEvent.type);
                    }
                });
    }

    @Override
    public Observable<Boolean> updateMessageStatus(String conversationId, String messageId, String userId, int status) {
        DatabaseReference reference = database.getReference("messages").child(conversationId).child(messageId).child("status").child(userId);
        return RxFirebaseDatabase.setValue(reference, status)
                .map(reference1 -> true)
                .toObservable();
    }

    @Override
    public Observable<ChildData<MessageEntity>> observeMediaUpdate(String conversationId) {
        DatabaseReference reference = database.getReference("media").child(conversationId);
        return RxFirebaseDatabase.getInstance(reference)
                .onChildEvent()
                .map(childEvent -> {
                    if (childEvent.dataSnapshot.exists() && childEvent.type == ChildEvent.Type.CHILD_ADDED) {
                        MessageEntity message = messageMapper.transform(childEvent.dataSnapshot);
                        return new ChildData<>(message, childEvent.type);
                    } else {
                        return new ChildData<MessageEntity>(null, childEvent.type);
                    }
                });
    }

    @Override
    public Observable<String> updateThumbnailImage(String conversationKey, String messageKey, String filePath) {
        DatabaseReference reference = database.getReference("messages").child(conversationKey).child(messageKey).child("thumbUrl");
        return RxFirebaseDatabase.setValue(reference, filePath)
                .map(databaseReference -> filePath)
                .toObservable();
    }

    @Override
    public Observable<String> updateImage(String conversationKey, String messageKey, String filePath) {
        DatabaseReference reference = database.getReference("messages").child(conversationKey).child(messageKey).child("photoUrl");
        return RxFirebaseDatabase.setValue(reference, filePath)
                .map(databaseReference -> filePath)
                .toObservable();
    }

    @Override
    public Observable<MessageEntity> addChildMessage(String conversationKey, String messageKey, MessageEntity data) {
        Map<String, Object> updateValue = new HashMap<>();
        updateValue.put(String.format("messages/%s/%s/childMessages/%s", conversationKey, messageKey, data.key), data.toMap());
        updateValue.put(String.format("media/%s/%s/childMessages/%s", conversationKey, messageKey, data.key), data.toMap());
        return updateBatchData(updateValue)
                .map(aBoolean -> data);
    }

    public Observable<MessageEntity> addChildMedia(String conversationKey, String messageKey, MessageEntity data) {
        DatabaseReference reference = database.getReference("media")
                .child(conversationKey)
                .child(messageKey)
                .child("childMessages").child(data.key);
        return RxFirebaseDatabase.setValue(reference, data.toMap())
                .map(databaseReference -> data)
                .toObservable();
    }

    @Override
    public Observable<MessageEntity> sendMediaMessage(String conversationId, MessageEntity message) {
        DatabaseReference reference = database.getReference("media")
                .child(conversationId).child(message.key);
        return RxFirebaseDatabase.setValue(reference, message.toMap())
                .map(databaseReference -> message)
                .toObservable();
    }

    @Override
    public String populateChildMessageKey(String conversationId, String messageId) {
        return database.getReference("messages").child(conversationId).child(messageId).child("childMessages").push().getKey();
    }

    @Override
    public Observable<Boolean> updateChildMessageImage(String conversationId, String parentMessageKey, String messageKey, String thumbnail, String image) {
        Map<String, Object> updateValue = new HashMap<>();
        updateValue.put(String.format("messages/%s/%s/childMessages/%s/thumbUrl", conversationId, parentMessageKey, messageKey), thumbnail);
        updateValue.put(String.format("messages/%s/%s/childMessages/%s/photoUrl", conversationId, parentMessageKey, messageKey), image);
        updateValue.put(String.format("media/%s/%s/childMessages/%s/thumbUrl", conversationId, parentMessageKey, messageKey), thumbnail);
        updateValue.put(String.format("media/%s/%s/childMessages/%s/photoUrl", conversationId, parentMessageKey, messageKey), image);
        return updateBatchData(updateValue);
    }

    @Override
    public Observable<Boolean> updateChildMessageGame(String conversationId, String parentMessageKey, String messageKey, String gameUrl) {
        Map<String, Object> updateValue = new HashMap<>();
        updateValue.put(String.format("messages/%s/%s/childMessages/%s/gameUrl", conversationId, parentMessageKey, messageKey), gameUrl);
        updateValue.put(String.format("media/%s/%s/childMessages/%s/gameUrl", conversationId, parentMessageKey, messageKey), gameUrl);
        return updateBatchData(updateValue);
    }

    @Override
    public void deleteCacheMessage(String messageKey) {
        SQLite.delete()
                .from(MessageEntity.class)
                .where(MessageEntity_Table.key.eq(messageKey))
                .execute();
    }

    @Override
    public void deleteCacheMessages(String conversationId) {
        SQLite.delete()
                .from(MessageEntity.class)
                .where(MessageEntity_Table.conversationId.eq(conversationId))
                .execute();
    }

    @Override
    public Observable<List<MessageEntity>> getCachedMessages(String conversationId) {
        return RXSQLite.rx(
                SQLite.select()
                        .from(MessageEntity.class)
                        .where(MessageEntity_Table.conversationId.eq(conversationId))
                        .orderBy(MessageEntity_Table.timestamp, false)
                        .limit(20)
        )
                .queryList()
                .toObservable();
    }

    @Override
    public void saveMessage(MessageEntity entity) {
        DatabaseDefinition database = FlowManager.getDatabase(AppDatabase.class);
        Transaction transaction = database.beginTransactionAsync(databaseWrapper -> {
            entity.save();
        }).build();
        transaction.execute();
    }

    @Override
    public void saveMessages(List<MessageEntity> messageEntities) {
        DatabaseDefinition database = FlowManager.getDatabase(AppDatabase.class);
        Transaction transaction = database.beginTransactionAsync(databaseWrapper -> {
            for (MessageEntity entity : messageEntities) {
                entity.save();
            }
        }).build();
        transaction.execute(); // execute
    }

    @Override
    public void updateLocalMaskStatus(String message, boolean isMask) {
        SQLite.update(MessageEntity.class)
                .set(MessageEntity_Table.isMask.eq(isMask))
                .where(MessageEntity_Table.key.eq(message))
                .execute();

    }

    private Observable<Boolean> updateBatchData(Map<String, Object> updateValue) {
        return RxFirebaseDatabase.updateBatchData(database.getReference(), updateValue)
                .toObservable();
    }
}
