package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.GetCurrentUserUseCase;
import com.ping.android.domain.usecase.user.UpdateUserTransphabetUseCase;
import com.ping.android.domain.usecase.user.UpdateUserMappingUseCase;
import com.ping.android.model.Mapping;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.ManualMappingPresenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
 * Created by tuanluong on 3/21/18.
 */

public class ManualMappingPresenterImpl implements ManualMappingPresenter {
    @Inject
    View view;
    @Inject
    GetCurrentUserUseCase getCurrentUserUseCase;
    @Inject
    UpdateUserMappingUseCase updateUserMappingUseCase;
    @Inject
    UpdateUserTransphabetUseCase updateUserTransphabetUseCase;

    private List<Mapping> mappings = new ArrayList<>();

    @Inject
    public ManualMappingPresenterImpl() {
    }

    @Override
    public void create() {
        getCurrentUserUseCase.execute(new DefaultObserver<User>() {
            @Override
            public void onNext(User user) {
                mappings = getListFromMapping(user.mappings);
                view.updateMapping(mappings);
            }
        }, null);
    }

    @Override
    public void resetMapping() {
        mappings = getDefaultMappingList();
        updateUserTransphabetUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                view.updateMapping(mappings);
            }
        }, getMappingFromList(mappings));
    }

    @Override
    public void changeMapping(String mapKey, String value) {
        for (Mapping mapping : mappings) {
            if (mapping.mapKey.equals(mapKey)) {
                if (!mapping.mapValue.equals(value)) {
                    mapping.mapValue = value;
                    view.updateMapping(mappings);
                    updateMapping(mapping);
                }
            }
        }
    }

    @Override
    public void handleMappingItemClick(int index) {
        if (index >= 0 && index < mappings.size()) {
            Mapping mapping = mappings.get(index);
            view.editMappingItem(mapping);
        }
    }

    @Override
    public void destroy() {
        getCurrentUserUseCase.dispose();
        updateUserMappingUseCase.dispose();
        updateUserTransphabetUseCase.dispose();
    }

    private void updateMapping(Mapping mapping) {
        updateUserMappingUseCase.execute(new DefaultObserver<>(), mapping);
    }

    private List<Mapping> getDefaultMappingList() {
        List<Mapping> mappings = new ArrayList<>();
        for (int i = 0; i < 26; i++) {
            String mapKey = "" + (char) ('A' + i);
            Mapping mapping = new Mapping(mapKey, "");
            mappings.add(mapping);
        }
        Collections.sort(mappings, (lhs, rhs) -> {
            // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
            return lhs.mapKey.compareTo(rhs.mapKey);
        });
        return mappings;
    }

    private List<Mapping> getListFromMapping(Map<String, String> mappingMap) {
        List<Mapping> mappings = new ArrayList<>();
        for (Map.Entry<String, String> entry : mappingMap.entrySet()) {
            Mapping mapping = new Mapping();
            mapping.mapKey = entry.getKey();
            Object value = entry.getValue();
            mapping.mapValue = value.toString();
            mappings.add(mapping);
        }
        Collections.sort(mappings, (lhs, rhs) -> {
            // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
            return lhs.mapKey.compareTo(rhs.mapKey);
        });
        return mappings;
    }

    public Map<String, String> getMappingFromList(List<Mapping> mappings) {
        Map<String, String> mappingMap = new HashMap<>();
        for (Mapping mapping : mappings) {
            mappingMap.put(mapping.mapKey, mapping.mapValue);
        }
        return mappingMap;
    }
}
