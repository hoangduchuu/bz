package com.ping.android.presentation.view.flexibleitem.messages.text;

import com.ping.android.R;
import com.ping.android.model.Message;
import com.ping.android.presentation.view.flexibleitem.messages.TextMessageBaseItem;

/**
 * Created by tuanluong on 3/2/18.
 */

public class TextMessageLeftItem extends TextMessageBaseItem {
    public TextMessageLeftItem(Message message) {
        super(message);
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_chat_left_msg;
    }
}
