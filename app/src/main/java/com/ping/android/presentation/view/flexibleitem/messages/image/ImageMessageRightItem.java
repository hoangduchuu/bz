package com.ping.android.presentation.view.flexibleitem.messages.image;

import com.ping.android.R;
import com.ping.android.model.Message;
import com.ping.android.presentation.view.flexibleitem.messages.ImageMessageBaseItem;

/**
 * Created by tuanluong on 3/2/18.
 */

public class ImageMessageRightItem extends ImageMessageBaseItem {
    public ImageMessageRightItem(Message message) {
        super(message);
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_chat_right_img;
    }
}
