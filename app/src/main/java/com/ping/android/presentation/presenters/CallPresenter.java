package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;
import com.ping.android.model.Call;
import com.ping.android.model.User;

/**
 * Created by tuanluong on 1/30/18.
 */

public interface CallPresenter extends BasePresenter {
    void getCalls();

    void handleCallPressed(Call call, boolean isVideo);

    interface View extends BaseView {
        void addCall(Call call);

        void deleteCall(Call call);

        void callUser(User user, boolean isVideo);
    }
}
