package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;

import java.util.Map;

public interface TransphabetPresenter extends BasePresenter {
    void randomizeTransphabet(Map<String, String> mappings);

    interface View extends BaseView {

    }
}
