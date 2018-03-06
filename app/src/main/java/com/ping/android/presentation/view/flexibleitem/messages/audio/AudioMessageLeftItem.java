package com.ping.android.presentation.view.flexibleitem.messages.audio;

import com.ping.android.activity.R;
import com.ping.android.model.Message;
import com.ping.android.presentation.view.flexibleitem.messages.AudioMessageBaseItem;
import com.ping.android.presentation.view.flexibleitem.messages.TextMessageBaseItem;

/**
 * Created by tuanluong on 3/2/18.
 */

public class AudioMessageLeftItem extends AudioMessageBaseItem {
    public AudioMessageLeftItem(Message message) {
        super(message);
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_chat_left_audio;
    }
}
