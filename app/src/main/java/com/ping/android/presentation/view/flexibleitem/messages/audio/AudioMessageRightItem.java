package com.ping.android.presentation.view.flexibleitem.messages.audio;

import com.ping.android.R;
import com.ping.android.model.Message;
import com.ping.android.presentation.view.flexibleitem.messages.AudioMessageBaseItem;

import javax.inject.Inject;

/**
 * Created by tuanluong on 3/2/18.
 */

public class AudioMessageRightItem extends AudioMessageBaseItem {
    @Inject
    public AudioMessageRightItem(Message message) {
        super(message);
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_chat_right_audio;
    }
}
