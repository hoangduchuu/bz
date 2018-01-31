package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;
import com.ping.android.model.Call;

/**
 * Created by tuanluong on 1/30/18.
 */

public interface CallPresenter extends BasePresenter {
    void getCalls();

    interface View extends BaseView {
        void addCall(Call call);
        void deleteCall(Call call);
    }
}
