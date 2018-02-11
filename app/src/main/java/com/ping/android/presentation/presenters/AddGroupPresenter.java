package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;
import com.ping.android.domain.usecase.group.CreateGroupUseCase;

/**
 * Created by tuanluong on 2/8/18.
 */

public interface AddGroupPresenter extends BasePresenter {
    void createGroup(CreateGroupUseCase.Params params);

    interface View extends BaseView {
        void moveToChatScreen(String conversationId);
    }
}
