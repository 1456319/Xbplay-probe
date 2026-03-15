package com.studio08.xbgamestream.ui.builder;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
/* loaded from: /app/base.apk/classes3.dex */
public class ControllerBuilderViewModel extends ViewModel {
    private MutableLiveData<String> mText;

    public ControllerBuilderViewModel() {
        MutableLiveData<String> mutableLiveData = new MutableLiveData<>();
        this.mText = mutableLiveData;
        mutableLiveData.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return this.mText;
    }
}
