package com.ping.android.presentation.view.flexibleitem.messages.text;

import com.ping.android.R;
import com.ping.android.model.Message;
import com.ping.android.presentation.view.flexibleitem.messages.TextMessageBaseItem;

import javax.inject.Inject;

/**
 * Created by tuanluong on 3/2/18.
 */

public class TextMessageRightItem extends TextMessageBaseItem {
    @Inject
    public TextMessageRightItem(Message message) {
        super(message);
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_chat_right_msg;
    }
}
