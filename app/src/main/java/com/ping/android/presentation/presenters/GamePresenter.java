package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.ping.android.model.Conversation;

/**
 * Created by tuanluong on 3/22/18.
 */

public interface GamePresenter extends BasePresenter {
    void sendGameStatus(Conversation conversation, boolean isPass);

    interface View {
    }
}
