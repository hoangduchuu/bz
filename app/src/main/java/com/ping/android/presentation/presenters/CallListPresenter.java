package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;
import com.ping.android.model.Call;
import com.ping.android.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tuanluong on 1/30/18.
 */

public interface CallListPresenter extends BasePresenter {
    void getCalls();

    void handleCallPressed(Call call, boolean isVideo);

    void deleteCalls(ArrayList<Call> selectedCalls);

    void loadMore();

    interface View extends BaseView {
        void addCall(Call call);

        void deleteCall(Call call);

        void callUser(User currentUser, User user, boolean isVideo);

        void updateCalls(List<Call> callList);

        void appendCalls(List<Call> callList);
    }
}
