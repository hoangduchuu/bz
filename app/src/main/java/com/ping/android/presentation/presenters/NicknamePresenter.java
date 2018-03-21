package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;
import com.ping.android.model.Conversation;
import com.ping.android.model.Nickname;

/**
 * Created by tuanluong on 3/21/18.
 */

public interface NicknamePresenter extends BasePresenter {

    void init(Conversation conversation);

    void updateNickName(Nickname nickname);

    interface View extends BaseView {

    }
}
