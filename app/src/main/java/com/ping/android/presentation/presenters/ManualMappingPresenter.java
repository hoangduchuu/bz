package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;
import com.ping.android.form.Mapping;

import java.util.List;

/**
 * Created by tuanluong on 3/21/18.
 */

public interface ManualMappingPresenter extends BasePresenter {
    void resetMapping();

    void changeMapping(String mapKey, String value);

    void handleMappingItemClick(int index);

    interface View extends BaseView {

        void updateMapping(List<Mapping> mappings);

        void editMappingItem(Mapping mapping);
    }
}
